import { Component } from '@angular/core';
import { AuthService } from '../../../services/auth/auth.service';
import { Router, RouterLink } from '@angular/router';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';

@Component({
  selector: 'app-login-register',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterLink],
  templateUrl: './login-register.component.html',
  styleUrls: ['./login-register.component.css']
})
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
        this.errorMessage = 'Email o contraseña incorrectas';
      },
    });
  }



}
