import { Component } from '@angular/core';
import { Router, RouterLink } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { AuthService } from '../../../services/auth.service';

@Component({
  selector: 'app-register',
  standalone: true,
  imports: [FormsModule, RouterLink],
  templateUrl: './register.component.html',
  styleUrl: './register.component.css'
})
export class RegisterComponent {
  fullname = '';
  username = '';
  email = '';
  password = '';

  constructor(private authService: AuthService, private router: Router) { }

  register() {
    const user = {
      fullName: this.fullname,
      username: this.username,
      email: this.email,
      password: this.password
    };

    this.authService.register(user).subscribe({
      next: (res) => {
        alert('Identity Established. Proceed to Login.');
        this.router.navigate(['/login']);
      },
      error: (err) => {
        console.error(err);
        const errorMessage = err.error?.error || err.error || 'Unknown Error';
        alert('Initialization Failed: ' + errorMessage);
      }
    });
  }
}
