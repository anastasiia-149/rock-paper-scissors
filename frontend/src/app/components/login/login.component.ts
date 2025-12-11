import { Component, signal } from '@angular/core';
import { Router } from '@angular/router';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { GameService } from '../../services/game.service';

@Component({
  selector: 'app-login',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './login.component.html',
  styleUrl: './login.component.scss'
})
export class LoginComponent {
  username = signal<string>('');
  notificationMessage = signal<string | null>(null);
  notificationType = signal<'success' | 'error' | null>(null);
  isLoading = signal<boolean>(false);

  constructor(
    private gameService: GameService,
    private router: Router
  ) {}

  onSubmit() {
    const usernameValue = this.username().trim();

    if (!usernameValue) {
      this.showNotification('Please enter a username', 'error');
      return;
    }

    if (usernameValue.length < 3 || usernameValue.length > 50) {
      this.showNotification('Username must be between 3 and 50 characters', 'error');
      return;
    }

    this.isLoading.set(true);
    this.gameService.loadStatistics(usernameValue).subscribe({
      next: () => {
        this.isLoading.set(false);
        this.showNotification(`Welcome back, ${usernameValue}!`, 'success');
        this.navigateToGame();
      },
      error: (error) => {
        if (error.status === 404) {
          this.registerNewUser(usernameValue);
        } else {
          this.isLoading.set(false);
          this.showNotification('An error occurred. Please try again.', 'error');
        }
      }
    });
  }

  private registerNewUser(username: string) {
    this.gameService.registerUser(username).subscribe({
      next: () => {
        this.showNotification(`Welcome, ${username}! Your account has been created.`, 'success');
        this.gameService.setUsername(username);
        this.isLoading.set(false);
        this.navigateToGame();
      },
      error: (error) => {
        if (error.status === 400 && error.error?.message?.includes('already exists')) {
          this.gameService.loadStatistics(username).subscribe({
            next: () => {
              this.isLoading.set(false);
              this.showNotification(`Welcome back, ${username}!`, 'success');
              this.navigateToGame();
            },
            error: () => {
              this.isLoading.set(false);
              this.showNotification('An error occurred. Please try again.', 'error');
            }
          });
        } else {
          this.isLoading.set(false);
          const errorMessage = error.error?.message || 'Failed to create account. Please try again.';
          this.showNotification(errorMessage, 'error');
        }
      }
    });
  }

  private showNotification(message: string, type: 'success' | 'error') {
    this.notificationMessage.set(message);
    this.notificationType.set(type);

    setTimeout(() => {
      if (type === 'error') {
        this.clearNotification();
      }
    }, 5000);
  }

  private clearNotification() {
    this.notificationMessage.set(null);
    this.notificationType.set(null);
  }

  private navigateToGame() {
    setTimeout(() => {
      this.router.navigate(['/game']);
    }, 1500);
  }
}
