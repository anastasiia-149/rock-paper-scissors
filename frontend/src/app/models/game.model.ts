export type Hand = 'ROCK' | 'PAPER' | 'SCISSORS';

export type GameResult = 'WIN' | 'LOSE' | 'DRAW';

export interface PlayGameRequest {
  username: string;
  playerHand: Hand;
}

export interface GameResponse {
  gameId: string;
  playerHand: Hand;
  computerHand: Hand;
  result: GameResult;
  timestamp: string;
}

export interface RegisterUserRequest {
  username: string;
}

export interface UserResponse {
  username: string;
  createdAt: string;
}

export interface UserStatisticsResponse {
  username: string;
  gamesPlayed: number;
  wins: number;
  losses: number;
  draws: number;
  lastGameId: string | null;
  lastGamePlayedAt: string | null;
}
