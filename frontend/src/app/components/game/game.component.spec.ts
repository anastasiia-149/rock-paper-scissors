import {ComponentFixture, TestBed} from '@angular/core/testing';
import {Router} from '@angular/router';
import {signal} from '@angular/core';
import {of, throwError} from 'rxjs';
import {GameComponent} from './game.component';
import {GameService} from '../../services/game.service';
import {GameResponse, UserStatisticsResponse} from '../../models/game.model';

describe('GameComponent', () => {
  let component: GameComponent;
  let fixture: ComponentFixture<GameComponent>;
  let mockGameService: jasmine.SpyObj<GameService>;
  let mockRouter: jasmine.SpyObj<Router>;

  const mockGameResponse: GameResponse = {
    gameId: '123e4567-e89b-12d3-a456-426614174000',
    playerHand: 'ROCK',
    computerHand: 'SCISSORS',
    result: 'WIN',
    timestamp: '2025-12-10T14:30:00Z'
  };

  const mockStatistics: UserStatisticsResponse = {
    username: 'testuser',
    gamesPlayed: 10,
    wins: 5,
    losses: 3,
    draws: 2,
    lastGameId: 'game-123',
    lastGamePlayedAt: '2025-12-11T10:00:00Z'
  };

  beforeEach(async () => {
    const currentGameSignal = signal<GameResponse | null>(null);
    const isLoadingSignal = signal<boolean>(false);
    const errorSignal = signal<string | null>(null);
    const statisticsSignal = signal<UserStatisticsResponse | null>(mockStatistics);
    const usernameSignal = signal<string | null>('testuser');
    const winRateSignal = signal<number>(50);
    const isNewUserSignal = signal<boolean>(false);

    mockGameService = jasmine.createSpyObj('GameService', [
      'playGame',
      'clearError',
      'loadStatistics',
      'clearAll'
    ], {
      currentGame: currentGameSignal,
      isLoading: isLoadingSignal,
      error: errorSignal,
      statistics: statisticsSignal,
      username: usernameSignal,
      winRate: winRateSignal,
      isNewUser: isNewUserSignal
    });

    mockRouter = jasmine.createSpyObj('Router', ['navigate']);

    await TestBed.configureTestingModule({
      imports: [GameComponent],
      providers: [
        { provide: GameService, useValue: mockGameService },
        { provide: Router, useValue: mockRouter }
      ]
    }).compileComponents();

    mockGameService.loadStatistics.and.returnValue(of(mockStatistics));

    fixture = TestBed.createComponent(GameComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  describe('Hand Selection', () => {
    it('should call game service when hand is selected', () => {
      mockGameService.playGame.and.returnValue(of(mockGameResponse));

      component.onHandSelect('ROCK');

      expect(mockGameService.playGame).toHaveBeenCalledWith('ROCK');
      expect(component.selectedHand()).toBe('ROCK');
    });

    it('should clear error before playing game', () => {
      mockGameService.playGame.and.returnValue(of(mockGameResponse));

      component.onHandSelect('PAPER');

      expect(mockGameService.clearError).toHaveBeenCalled();
    });

    it('should handle API error gracefully', () => {
      mockGameService.playGame.and.returnValue(
        throwError(() => new Error('Network error'))
      );

      component.onHandSelect('ROCK');

      expect(component.showResult()).toBe(false);
    });
  });

  describe('Reset Functionality', () => {
    it('should reset game state when resetGame is called', () => {
      component.selectedHand.set('ROCK');
      component.showResult.set(true);

      component.resetGame();

      expect(component.selectedHand()).toBeNull();
      expect(component.showResult()).toBe(false);
      expect(mockGameService.clearError).toHaveBeenCalled();
    });

  });

  describe('Result Display Helpers', () => {
    it('should return correct color class for WIN', () => {
      expect(component.getResultColor('WIN')).toBe('success');
    });

    it('should return correct color class for LOSE', () => {
      expect(component.getResultColor('LOSE')).toBe('danger');
    });

    it('should return correct color class for DRAW', () => {
      expect(component.getResultColor('DRAW')).toBe('warning');
    });

    it('should return empty string for unknown result', () => {
      expect(component.getResultColor(undefined)).toBe('');
    });

    it('should return correct message for WIN', () => {
      expect(component.getResultMessage('WIN')).toBe('🎉 You Win!');
    });

    it('should return correct message for LOSE', () => {
      expect(component.getResultMessage('LOSE')).toBe('😔 You Lose!');
    });

    it('should return correct message for DRAW', () => {
      expect(component.getResultMessage('DRAW')).toBe('🤝 It\'s a Draw!');
    });

    it('should return question mark for unknown hand', () => {
      expect(component.getHandIcon('INVALID')).toBe('❓');
      expect(component.getHandIcon(undefined)).toBe('❓');
    });
  });

  describe('Component State', () => {
    it('should have three hand options available', () => {
      expect(component.hands.length).toBe(3);
      expect(component.hands.map(h => h.value)).toEqual(['ROCK', 'PAPER', 'SCISSORS']);
    });

    it('should expose service signals', () => {
      expect(component.currentGame).toBe(mockGameService.currentGame);
      expect(component.isLoading).toBe(mockGameService.isLoading);
      expect(component.error).toBe(mockGameService.error);
      expect(component.statistics).toBe(mockGameService.statistics);
      expect(component.username).toBe(mockGameService.username);
      expect(component.winRate).toBe(mockGameService.winRate);
    });
  });

  describe('Initialization', () => {
    it('should load statistics for existing user on init', () => {
      expect(mockGameService.loadStatistics).toHaveBeenCalledWith('testuser');
    });
  });

  describe('Logout', () => {
    it('should clear all data and navigate to login', () => {
      component.logout();

      expect(mockGameService.clearAll).toHaveBeenCalled();
      expect(mockRouter.navigate).toHaveBeenCalledWith(['/login']);
    });
  });

  describe('Last Game Played At', () => {
    it('should compute last game played date from statistics', () => {
      const lastGameDate = component.lastGamePlayedAt();

      expect(lastGameDate).toBeInstanceOf(Date);
      expect(lastGameDate?.toISOString()).toBe('2025-12-11T10:00:00.000Z');
    });
  });
});
