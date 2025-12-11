import { Injectable, signal, computed } from '@angular/core';
import { HttpClient, HttpErrorResponse } from '@angular/common/http';
import { catchError, tap, throwError } from 'rxjs';
import { GameResponse, Hand, PlayGameRequest } from '../models/game.model';
import {environment} from '../../environments/environment';
import {DRAW, LOSE, WIN} from '../constants/contstants';

@Injectable({
  providedIn: 'root'
})
export class GameService {
  private readonly endpointUrl = `${environment.apiUrl}/play`;

  private readonly _currentGame = signal<GameResponse | null>(null);
  private readonly _isLoading = signal<boolean>(false);
  private readonly _error = signal<string | null>(null);
  private readonly _gamesPlayed = signal<number>(0);
  private readonly _wins = signal<number>(0);
  private readonly _losses = signal<number>(0);
  private readonly _draws = signal<number>(0);

  readonly currentGame = this._currentGame.asReadonly();
  readonly isLoading = this._isLoading.asReadonly();
  readonly error = this._error.asReadonly();

  readonly winRate = computed(() => {
    const total = this._gamesPlayed();
    return total > 0 ? Math.round((this._wins() / total) * 100) : 0;
  });

  readonly statistics = computed(() => ({
    gamesPlayed: this._gamesPlayed(),
    wins: this._wins(),
    losses: this._losses(),
    draws: this._draws(),
    winRate: this.winRate()
  }));

  constructor(private http: HttpClient) {}

  playGame(hand: Hand) {
    this._isLoading.set(true);
    this._error.set(null);

    const request: PlayGameRequest = { playerHand: hand };

    return this.http.post<GameResponse>(this.endpointUrl, request)
      .pipe(
        tap((response) => {
          this._currentGame.set(response);
          this._gamesPlayed.update(count => count + 1);

          switch (response.result) {
            case WIN:
              this._wins.update(count => count + 1);
              break;
            case LOSE:
              this._losses.update(count => count + 1);
              break;
            case DRAW:
              this._draws.update(count => count + 1);
              break;
          }

          this._isLoading.set(false);
        }),
        catchError((error: HttpErrorResponse) => {
          this._isLoading.set(false);

          const errorMessage = error.error?.message ||
            error.statusText ||
            'An unexpected error occurred';

          this._error.set(errorMessage);
          return throwError(() => error);
        })
      );
  }

  resetStatistics() {
    this._gamesPlayed.set(0);
    this._wins.set(0);
    this._losses.set(0);
    this._draws.set(0);
    this._currentGame.set(null);
    this._error.set(null);
  }

  clearError() {
    this._error.set(null);
  }
}
