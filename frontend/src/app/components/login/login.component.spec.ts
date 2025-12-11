import { TestBed, ComponentFixture } from '@angular/core/testing';
import { Router } from '@angular/router';
import { of, throwError } from 'rxjs';
import { LoginComponent } from './login.component';
import { GameService } from '../../services/game.service';
import { UserStatisticsResponse } from '../../models/game.model';

describe('LoginComponent', () => {
  let component: LoginComponent;
  let fixture: ComponentFixture<LoginComponent>;
  let mockGameService: jasmine.SpyObj<GameService>;
  let mockRouter: jasmine.SpyObj<Router>;

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
    mockGameService = jasmine.createSpyObj('GameService', [
      'loadStatistics',
      'registerUser',
      'setUsername'
    ]);

    mockRouter = jasmine.createSpyObj('Router', ['navigate']);

    await TestBed.configureTestingModule({
      imports: [LoginComponent],
      providers: [
        { provide: GameService, useValue: mockGameService },
        { provide: Router, useValue: mockRouter }
      ]
    }).compileComponents();

    fixture = TestBed.createComponent(LoginComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  describe('Form Validation', () => {
    it('should show error when username is empty', () => {
      component.username.set('');
      component.onSubmit();

      expect(component.notificationMessage()).toBe('Please enter a username');
      expect(component.notificationType()).toBe('error');
      expect(mockGameService.loadStatistics).not.toHaveBeenCalled();
    });

    it('should show error when username is too short', () => {
      component.username.set('ab');
      component.onSubmit();

      expect(component.notificationMessage()).toBe('Username must be between 3 and 50 characters');
      expect(component.notificationType()).toBe('error');
      expect(mockGameService.loadStatistics).not.toHaveBeenCalled();
    });

    it('should show error when username is too long', () => {
      const longUsername = 'a'.repeat(51);
      component.username.set(longUsername);
      component.onSubmit();

      expect(component.notificationMessage()).toBe('Username must be between 3 and 50 characters');
      expect(component.notificationType()).toBe('error');
      expect(mockGameService.loadStatistics).not.toHaveBeenCalled();
    });

    it('should accept valid username with 3 characters', () => {
      component.username.set('abc');
      mockGameService.loadStatistics.and.returnValue(of(mockStatistics));

      component.onSubmit();

      expect(component.isLoading()).toBe(false);
      expect(mockGameService.loadStatistics).toHaveBeenCalledWith('abc');
    });

    it('should accept valid username with 50 characters', () => {
      const validUsername = 'a'.repeat(50);
      component.username.set(validUsername);
      mockGameService.loadStatistics.and.returnValue(of(mockStatistics));

      component.onSubmit();

      expect(mockGameService.loadStatistics).toHaveBeenCalledWith(validUsername);
    });
  });

  describe('Existing User Login', () => {
    it('should load statistics and navigate to game for existing user', (done) => {
      const username = 'existinguser';
      component.username.set(username);
      mockGameService.loadStatistics.and.returnValue(of(mockStatistics));

      component.onSubmit();

      setTimeout(() => {
        expect(component.isLoading()).toBe(false);
        expect(mockGameService.loadStatistics).toHaveBeenCalledWith(username);
        expect(component.notificationMessage()).toBe(`Welcome back, ${username}!`);
        expect(component.notificationType()).toBe('success');

        setTimeout(() => {
          expect(mockRouter.navigate).toHaveBeenCalledWith(['/game']);
          done();
        }, 1600);
      }, 100);
    });

    it('should handle error when loading statistics fails', () => {
      const username = 'testuser';
      component.username.set(username);
      mockGameService.loadStatistics.and.returnValue(
        throwError(() => ({ status: 500, error: { message: 'Server error' } }))
      );
      mockGameService.registerUser.and.returnValue(of({ username, createdAt: '2025-12-11T10:00:00Z' }));

      component.onSubmit();

      expect(mockGameService.loadStatistics).toHaveBeenCalledWith(username);
    });
  });

  describe('New User Registration', () => {
    it('should register new user when statistics return 404', (done) => {
      const username = 'newuser';
      component.username.set(username);
      mockGameService.loadStatistics.and.returnValue(
        throwError(() => ({ status: 404 }))
      );
      mockGameService.registerUser.and.returnValue(
        of({ username, createdAt: '2025-12-11T10:00:00Z' })
      );

      component.onSubmit();

      setTimeout(() => {
        expect(mockGameService.loadStatistics).toHaveBeenCalledWith(username);
        expect(mockGameService.registerUser).toHaveBeenCalledWith(username);
        expect(mockGameService.setUsername).toHaveBeenCalledWith(username);
        expect(component.notificationMessage()).toBe(`Welcome, ${username}! Your account has been created.`);
        expect(component.notificationType()).toBe('success');
        expect(component.isLoading()).toBe(false);

        setTimeout(() => {
          expect(mockRouter.navigate).toHaveBeenCalledWith(['/game']);
          done();
        }, 1600);
      }, 100);
    });

    it('should handle registration error', (done) => {
      const username = 'newuser';
      component.username.set(username);
      mockGameService.loadStatistics.and.returnValue(
        throwError(() => ({ status: 404 }))
      );
      mockGameService.registerUser.and.returnValue(
        throwError(() => ({ status: 500, error: { message: 'Registration failed' } }))
      );

      component.onSubmit();

      setTimeout(() => {
        expect(component.isLoading()).toBe(false);
        expect(component.notificationMessage()).toBe('Registration failed');
        expect(component.notificationType()).toBe('error');
        done();
      }, 100);
    });

    it('should handle username already exists error by loading statistics', (done) => {
      const username = 'existinguser';
      component.username.set(username);

      let callCount = 0;
      mockGameService.loadStatistics.and.callFake(() => {
        callCount++;
        if (callCount === 1) {
          return throwError(() => ({ status: 404 }));
        } else {
          return of(mockStatistics);
        }
      });

      mockGameService.registerUser.and.returnValue(
        throwError(() => ({
          status: 400,
          error: { message: 'Username already exists: existinguser' }
        }))
      );

      component.onSubmit();

      setTimeout(() => {
        expect(mockGameService.registerUser).toHaveBeenCalledWith(username);
        expect(mockGameService.loadStatistics).toHaveBeenCalledTimes(2);
        expect(component.notificationMessage()).toBe(`Welcome back, ${username}!`);
        expect(component.notificationType()).toBe('success');
        expect(component.isLoading()).toBe(false);

        setTimeout(() => {
          expect(mockRouter.navigate).toHaveBeenCalledWith(['/game']);
          done();
        }, 1600);
      }, 100);
    });
  });

  describe('Notification Display', () => {
    it('should show success notification for existing user', (done) => {
      const username = 'testuser';
      component.username.set(username);
      mockGameService.loadStatistics.and.returnValue(of(mockStatistics));

      component.onSubmit();

      setTimeout(() => {
        expect(component.notificationMessage()).toBe(`Welcome back, ${username}!`);
        expect(component.notificationType()).toBe('success');
        done();
      }, 100);
    });

    it('should show success notification for new user', (done) => {
      const username = 'newuser';
      component.username.set(username);
      mockGameService.loadStatistics.and.returnValue(
        throwError(() => ({ status: 404 }))
      );
      mockGameService.registerUser.and.returnValue(
        of({ username, createdAt: '2025-12-11T10:00:00Z' })
      );

      component.onSubmit();

      setTimeout(() => {
        expect(component.notificationMessage()).toBe(`Welcome, ${username}! Your account has been created.`);
        expect(component.notificationType()).toBe('success');
        done();
      }, 100);
    });
  });
});
