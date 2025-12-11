import { Injectable, signal, computed } from '@angular/core';
import { HttpClient, HttpErrorResponse } from '@angular/common/http';
import { catchError, tap, throwError } from 'rxjs';
import {
  GameResponse,
  Hand,
  PlayGameRequest,
  RegisterUserRequest,
  UserResponse,
  UserStatisticsResponse
} from '../models/game.model';
import {environment} from '../../environments/environment';

@Injectable({
  providedIn: 'root'
})
export class GameService {
  private readonly gameEndpointUrl = `${environment.apiUrl}/game/play`;
  private readonly userEndpointUrl = `${environment.apiUrl}/game/user`;
  private readonly statisticsEndpointUrl = `${environment.apiUrl}/game/statistics`;

  private readonly _currentGame = signal<GameResponse | null>(null);
  private readonly _isLoading = signal<boolean>(false);
  private readonly _error = signal<string | null>(null);
  private readonly _username = signal<string | null>(null);
  private readonly _statistics = signal<UserStatisticsResponse | null>(null);
  private readonly _isNewUser = signal<boolean>(false);

  readonly currentGame = this._currentGame.asReadonly();
  readonly isLoading = this._isLoading.asReadonly();
  readonly error = this._error.asReadonly();
  readonly username = this._username.asReadonly();
  readonly statistics = this._statistics.asReadonly();
  readonly isNewUser = this._isNewUser.asReadonly();

  readonly winRate = computed(() => {
    const stats = this._statistics();
    return stats && stats.gamesPlayed > 0
      ? Math.round((stats.wins / stats.gamesPlayed) * 100)
      : 0;
  });

  constructor(private http: HttpClient) {}

  registerUser(username: string) {
    this._isLoading.set(true);
    this._error.set(null);

    const request: RegisterUserRequest = { username };

    return this.http.post<UserResponse>(this.userEndpointUrl, request)
      .pipe(
        tap((response) => {
          this._username.set(response.username);
          this._isNewUser.set(true);
          this._isLoading.set(false);
        }),
        catchError((error: HttpErrorResponse) => this.handleError(error))
      );
  }

  loadStatistics(username: string) {
    this._isLoading.set(true);
    this._error.set(null);

    return this.http.get<UserStatisticsResponse>(`${this.statisticsEndpointUrl}/${username}`)
      .pipe(
        tap((response) => {
          this._statistics.set(response);
          this._username.set(response.username);
          this._isNewUser.set(false);
          this._isLoading.set(false);
        }),
        catchError((error: HttpErrorResponse) => this.handleError(error))
      );
  }

  playGame(hand: Hand) {
    const username = this._username();
    if (!username) {
      this._error.set('Username is required');
      return throwError(() => new Error('Username is required'));
    }

    this._isLoading.set(true);
    this._error.set(null);

    const request: PlayGameRequest = { username, playerHand: hand };

    return this.http.post<GameResponse>(this.gameEndpointUrl, request)
      .pipe(
        tap((response) => {
          this._currentGame.set(response);
          this._isLoading.set(false);
          this.loadStatistics(username).subscribe();
        }),
        catchError((error: HttpErrorResponse) => this.handleError(error))
      );
  }

  setUsername(username: string) {
    this._username.set(username);
  }

  clearError() {
    this._error.set(null);
  }

  clearAll() {
    this._currentGame.set(null);
    this._error.set(null);
    this._username.set(null);
    this._statistics.set(null);
    this._isNewUser.set(false);
    this._isLoading.set(false);
  }

  private handleError(error: HttpErrorResponse) {
    this._isLoading.set(false);

    const errorMessage = error.error?.message ||
      error.statusText ||
      'An unexpected error occurred';

    this._error.set(errorMessage);
    return throwError(() => error);
  }
}
