import { TestBed, ComponentFixture } from '@angular/core/testing';
import { signal } from '@angular/core';
import { of, throwError } from 'rxjs';
import { GameComponent } from './game.component';
import { GameService } from '../../services/game.service';
import { GameResponse } from '../../models/game.model';

describe('GameComponent', () => {
  let component: GameComponent;
  let fixture: ComponentFixture<GameComponent>;
  let mockGameService: jasmine.SpyObj<GameService>;

  const mockGameResponse: GameResponse = {
    gameId: '123e4567-e89b-12d3-a456-426614174000',
    playerHand: 'ROCK',
    computerHand: 'SCISSORS',
    result: 'WIN',
    timestamp: '2025-12-10T14:30:00Z'
  };

  beforeEach(async () => {
    const currentGameSignal = signal<GameResponse | null>(null);
    const isLoadingSignal = signal<boolean>(false);
    const errorSignal = signal<string | null>(null);
    const statisticsSignal = signal({
      gamesPlayed: 0,
      wins: 0,
      losses: 0,
      draws: 0,
      winRate: 0
    });

    mockGameService = jasmine.createSpyObj('GameService', [
      'playGame',
      'clearError',
      'resetStatistics'
    ], {
      currentGame: currentGameSignal,
      isLoading: isLoadingSignal,
      error: errorSignal,
      statistics: statisticsSignal
    });

    await TestBed.configureTestingModule({
      imports: [GameComponent],
      providers: [
        { provide: GameService, useValue: mockGameService }
      ]
    }).compileComponents();

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

    it('should reset statistics and game state', () => {
      component.selectedHand.set('PAPER');
      component.showResult.set(true);

      component.resetStatistics();

      expect(mockGameService.resetStatistics).toHaveBeenCalled();
      expect(component.selectedHand()).toBeNull();
      expect(component.showResult()).toBe(false);
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
    });
  });
});
