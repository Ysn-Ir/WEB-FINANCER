import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { SidebarComponent } from '../layout/sidebar/sidebar.component';
import { AssetService } from '../../services/asset.service';
import { FormsModule } from '@angular/forms';

@Component({
  selector: 'app-vault',
  standalone: true,
  imports: [CommonModule, SidebarComponent, FormsModule],
  templateUrl: './vault.component.html',
  styleUrl: './vault.component.css'
})
export class VaultComponent implements OnInit {
  assets: any[] = [];
  isAdding: boolean = false;
  isEditing: boolean = false;
  currentId: number | null = null;
  newAsset: any = {
    symbol: '',
    name: '',
    quantity: 0,
    avgCost: 0,
    type: 'STOCK'
  };

  constructor(private assetService: AssetService) { }

  ngOnInit() {
    this.loadAssets();
  }

  loadAssets() {
    this.assetService.getAllAssets().subscribe({
      next: (data) => {
        this.assets = data.map(asset => ({
          ...asset,
          // Use backend provided price if available, else avgCost
          currentPrice: asset.currentPrice || asset.avgCost
        }));
      },
      error: (err) => console.error('Failed to load assets', err)
    });
  }

  calculateROI(asset: any): number {
    const totalCost = asset.quantity * asset.avgCost;
    const currentValue = asset.quantity * asset.currentPrice;
    if (totalCost === 0) return 0;
    return ((currentValue - totalCost) / totalCost) * 100;
  }

  toggleAdd() {
    this.isAdding = !this.isAdding;
    if (!this.isAdding) this.resetForm();
  }

  saveAsset() {
    if (!this.newAsset.symbol || !this.newAsset.quantity) return;

    if (this.isEditing && this.currentId) {
      this.assetService.updateAsset(this.currentId, this.newAsset).subscribe({
        next: (saved) => {
          console.log('Asset updated', saved);
          this.resetForm();
          this.loadAssets();
        },
        error: (err) => console.error('Failed to update asset', err)
      });
    } else {
      this.assetService.createAsset(this.newAsset).subscribe({
        next: (saved) => {
          console.log('Asset saved', saved);
          this.resetForm();
          this.loadAssets();
        },
        error: (err) => console.error('Failed to create asset', err)
      });
    }
  }

  editAsset(asset: any) {
    this.isAdding = true;
    this.isEditing = true;
    this.currentId = asset.id;
    this.newAsset = { ...asset };
  }

  deleteAsset(id: number) {
    if (confirm('Delete this asset?')) {
      this.assetService.deleteAsset(id).subscribe({
        next: () => {
          this.loadAssets();
        },
        error: (err) => console.error('Failed to delete asset', err)
      });
    }
  }

  resetForm() {
    this.isAdding = false;
    this.isEditing = false;
    this.currentId = null;
    this.newAsset = { symbol: '', name: '', quantity: 0, avgCost: 0, type: 'STOCK' };
  }

  refreshPrices() {
    this.assetService.refreshPrices().subscribe({
      next: () => {
        console.log('Prices refreshed');
        this.loadAssets();
      },
      error: (err) => console.error('Failed to refresh prices', err)
    });
  }
}
