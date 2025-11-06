import { Component } from '@angular/core';
import { AuthService } from '../../../services/auth/auth.service';
import { Router } from '@angular/router';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';

@Component({
  selector: 'app-login-register',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './login-register.component.html',})
export class LoginRegisterComponent {
  email: string = '';
  password: string='';
  errorMessage: string | null = null 


  constructor (
    private authService: AuthService,
    private router: Router
  ){}

  onSubmit() {
    this.authService.login(this.email, this.password).subscribe({
      next: () => {
        this.router.navigate(['/transactions']); 
      },
      error: (err) => {
        console.error(err);
        this.errorMessage = 'Invalid email or password';
      },
    });
  }



}
