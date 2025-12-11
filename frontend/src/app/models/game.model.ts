export type Hand = 'ROCK' | 'PAPER' | 'SCISSORS';

export type GameResult = 'WIN' | 'LOSE' | 'DRAW';

export interface PlayGameRequest {
  playerHand: Hand;
}

export interface GameResponse {
  gameId: string;
  playerHand: Hand;
  computerHand: Hand;
  result: GameResult;
  timestamp: string;
}
