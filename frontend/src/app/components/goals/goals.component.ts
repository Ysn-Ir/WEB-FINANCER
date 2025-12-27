import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { SidebarComponent } from '../layout/sidebar/sidebar.component';
import { GoalService } from '../../services/goal.service';
import { TransactionService } from '../../services/transaction.service';
import { FormsModule } from '@angular/forms';

@Component({
  selector: 'app-goals',
  standalone: true,
  imports: [CommonModule, SidebarComponent, FormsModule],
  templateUrl: './goals.component.html',
  styleUrl: './goals.component.css'
})
export class GoalsComponent implements OnInit {
  goals: any[] = [];
  isAdding: boolean = false;
  isEditing: boolean = false;
  currentId: number | null = null;
  newGoal: any = {
    name: '',
    targetAmount: 0,
    currentAmount: 0,
    deadline: ''
  };

  // Contribution State
  activeContributionId: number | null = null;
  contributionAmount: number = 0;

  // Smart Analytics State
  monthlyIncome: number = 0;
  monthlyExpenses: number = 0;
  monthlySurplus: number = 0;
  totalRequiredMonthly: number = 0;
  feasibilityStatus: 'Easy' | 'Tight' | 'Impossible' = 'Easy';

  constructor(
    private goalService: GoalService,
    private transactionService: TransactionService
  ) { }

  ngOnInit() {
    this.loadGoals();
    this.loadFinancialContext();
  }

  loadFinancialContext() {
    // Calculate average monthly surplus from transactions
    this.transactionService.getAllTransactions().subscribe({
      next: (txs: any[]) => {
        const now = new Date();
        const threeMonthsAgo = new Date();
        threeMonthsAgo.setMonth(now.getMonth() - 3);

        // Filter last 3 months
        const recentTxs = txs.filter(t => new Date(t.date) >= threeMonthsAgo);

        let totalInc = 0;
        let totalExp = 0;

        recentTxs.forEach(t => {
          if (t.type === 'INCOME') totalInc += t.amount;
          else if (t.type === 'EXPENSE') totalExp += t.amount;
        });

        // Average over 3 months (or actual duration if less)
        this.monthlyIncome = totalInc / 3;
        this.monthlyExpenses = totalExp / 3;
        this.monthlySurplus = Math.max(0, this.monthlyIncome - this.monthlyExpenses);

        this.updateAnalysis();
      },
      error: (err) => console.error('Failed to load financial context', err)
    });
  }

  loadGoals() {
    this.goalService.getAllGoals().subscribe({
      next: (data) => {
        this.goals = data;
        this.updateAnalysis();
      },
      error: (err) => console.error('Failed to load goals', err)
    });
  }

  updateAnalysis() {
    if (!this.goals) return;

    this.totalRequiredMonthly = this.goals.reduce((sum, g) => sum + this.getRequiredMonthlyContribution(g), 0);

    // Determine feasibility
    // Easy: Required is < 50% of surplus
    // Tight: Required is > 50% but < 100%
    // Impossible: Required > Surplus
    if (this.totalRequiredMonthly > this.monthlySurplus) {
      this.feasibilityStatus = 'Impossible';
    } else if (this.totalRequiredMonthly > (this.monthlySurplus * 0.5)) {
      this.feasibilityStatus = 'Tight';
    } else {
      this.feasibilityStatus = 'Easy';
    }
  }

  getSmartRecommendation(): string {
    if (this.feasibilityStatus === 'Impossible') {
      const shortage = this.totalRequiredMonthly - this.monthlySurplus;
      return ` Goal ambition exceeds monthly surplus by $${shortage.toFixed(0)}. Try extending deadlines.`;
    } else if (this.feasibilityStatus === 'Tight') {
      return ` Tight Budget: You are using >50% of your surplus for goals. Ensure essential expenses are covered.`;
    } else {
      return ` Good Health: You can comfortably meet these goals with your current income.`;
    }
  }

  calculateProgress(goal: any): number {
    if (!goal.targetAmount || goal.targetAmount === 0) return 0;
    return Math.min(100, (goal.currentAmount / goal.targetAmount) * 100);
  }

  getDaysRemaining(deadline: string): number {
    if (!deadline) return 0;
    const target = new Date(deadline);
    const today = new Date();
    const diff = target.getTime() - today.getTime();
    return Math.ceil(diff / (1000 * 3600 * 24));
  }

  getRequiredMonthlyContribution(goal: any): number {
    if (!goal.deadline || goal.currentAmount >= goal.targetAmount) return 0;
    const days = this.getDaysRemaining(goal.deadline);
    if (days <= 0) return 0;
    const months = days / 30.44;
    return (goal.targetAmount - goal.currentAmount) / months;
  }

  getProgressColor(percentage: number): string {
    if (percentage >= 100) return '#10b981'; // Green
    if (percentage >= 50) return '#3b82f6'; // Blue
    if (percentage >= 25) return '#f59e0b'; // Orange
    return '#ef4444'; // Red
  }

  // Contribution Logic
  startContribution(goal: any) {
    this.activeContributionId = goal.id;
    this.contributionAmount = 0;
  }

  cancelContribution() {
    this.activeContributionId = null;
    this.contributionAmount = 0;
  }

  useRecommendedAmount(goal: any) {
    const recommended = this.getRequiredMonthlyContribution(goal);
    this.contributionAmount = Math.ceil(recommended);
  }

  submitContribution(goal: any) {
    if (this.contributionAmount <= 0) return;

    // 1. Create Transaction (Expense)
    const transaction = {
      description: `Goal Contribution: ${goal.name}`,
      amount: this.contributionAmount,
      type: 'EXPENSE',
      category: 'Savings',
      date: new Date().toISOString().split('T')[0]
    };

    this.transactionService.createTransaction(transaction).subscribe({
      next: (tx) => {
        console.log('Transaction created', tx);

        // 2. Update Goal
        const updatedGoal = {
          ...goal,
          currentAmount: goal.currentAmount + this.contributionAmount
        };

        this.goalService.updateGoal(goal.id, updatedGoal).subscribe({
          next: (saved) => {
            console.log('Contribution recorded', saved);
            this.cancelContribution();
            this.loadGoals(); // Refresh analysis
            this.loadFinancialContext(); // Refresh financials as surplus changed
          },
          error: (err) => console.error('Failed to update goal', err)
        });
      },
      error: (err) => {
        console.error('Failed to create transaction', err);
        alert('Could not process contribution. Ensure you have enough funds.');
      }
    });
  }

  toggleAdd() {
    this.isAdding = !this.isAdding;
    if (!this.isAdding) this.resetForm();
  }

  saveGoal() {
    if (!this.newGoal.name || !this.newGoal.targetAmount) return;

    if (this.isEditing && this.currentId) {
      this.goalService.updateGoal(this.currentId, this.newGoal).subscribe({
        next: (saved) => {
          this.resetForm();
          this.loadGoals();
        },
        error: (err) => console.error('Failed to update goal', err)
      });
    } else {
      this.goalService.createGoal(this.newGoal).subscribe({
        next: (saved) => {
          this.resetForm();
          this.loadGoals();
        },
        error: (err) => console.error('Failed to create goal', err)
      });
    }
  }

  editGoal(goal: any) {
    this.isAdding = true;
    this.isEditing = true;
    this.currentId = goal.id;
    this.newGoal = { ...goal };
  }

  deleteGoal(id: number) {
    if (confirm('Delete this goal?')) {
      this.goalService.deleteGoal(id).subscribe({
        next: () => {
          this.loadGoals();
        },
        error: (err) => console.error('Failed to delete goal', err)
      });
    }
  }

  resetForm() {
    this.isAdding = false;
    this.isEditing = false;
    this.currentId = null;
    this.newGoal = { name: '', targetAmount: 0, currentAmount: 0, deadline: '' };
  }
}
