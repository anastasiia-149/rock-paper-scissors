import { Component, inject, signal, OnInit, computed } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router } from '@angular/router';
import { GameService } from '../../services/game.service';
import { Hand } from '../../models/game.model';
import { TRANSLATIONS } from '../../constants/translations';
import { DRAW, LOSE, WIN } from '../../constants/contstants';

@Component({
  selector: 'app-game',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './game.component.html',
  styleUrls: ['./game.component.scss']
})
export class GameComponent implements OnInit {
  readonly gameService = inject(GameService);
  readonly router = inject(Router);
  readonly T = TRANSLATIONS;

  selectedHand = signal<Hand | null>(null);
  showResult = signal<boolean>(false);
  currentGame = this.gameService.currentGame;
  isLoading = this.gameService.isLoading;
  error = this.gameService.error;
  statistics = this.gameService.statistics;
  username = this.gameService.username;
  winRate = this.gameService.winRate;

  lastGamePlayedAt = computed(() => {
    const stats = this.statistics();
    if (!stats?.lastGamePlayedAt) return null;
    return new Date(stats.lastGamePlayedAt);
  });

  ngOnInit() {
    const username = this.username();
    if (!username) {
      this.router.navigate(['/login']);
      return;
    }

    if (!this.gameService.isNewUser()) {
      this.gameService.loadStatistics(username).subscribe({
        error: (error) => {
          console.error('Error loading statistics:', error);
        }
      });
    }
  }

  readonly hands: Array<{ value: Hand; icon: string; label: string }> = [
    { value: 'ROCK', icon: this.T.ICON_ROCK, label: this.T.HAND_ROCK },
    { value: 'PAPER', icon: this.T.ICON_PAPER, label: this.T.HAND_PAPER },
    { value: 'SCISSORS', icon: this.T.ICON_SCISSORS, label: this.T.HAND_SCISSORS }
  ];

  onHandSelect(hand: Hand) {
    this.selectedHand.set(hand);
    this.showResult.set(false);
    this.gameService.clearError();

    this.gameService.playGame(hand).subscribe({
      next: () => {
        this.showResult.set(true);
      },
      error: (error) => {
        console.error('Error playing game:', error);
        this.showResult.set(false);
      }
    });
  }

  resetGame() {
    this.selectedHand.set(null);
    this.showResult.set(false);
    this.gameService.clearError();
  }

  logout() {
    this.gameService.clearAll();
    this.router.navigate(['/login']);
  }

  getResultColor(result: string | undefined): string {
    switch (result) {
      case WIN:
        return 'success';
      case LOSE:
        return 'danger';
      case DRAW:
        return 'warning';
      default:
        return '';
    }
  }

  getResultMessage(result: string | undefined): string {
    switch (result) {
      case WIN:
        return this.T.RESULT_WIN;
      case LOSE:
        return this.T.RESULT_LOSE;
      case DRAW:
        return this.T.RESULT_DRAW;
      default:
        return '';
    }
  }

  getHandIcon(hand: string | undefined): string {
    const handObj = this.hands.find(h => h.value === hand);
    return handObj?.icon || this.T.ICON_UNKNOWN;
  }
}
