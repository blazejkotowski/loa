package put.ai.snort.alphabetaplayer;

import java.util.Collections;
import java.util.List;
import java.util.Random;

import put.ai.snort.game.Board;
import put.ai.snort.game.Move;
import put.ai.snort.game.Player;
import put.ai.snort.game.Player.Color;
import put.ai.snort.game.moves.MoveMove;

public class AlphaBetaPlayer extends Player {

	private static Integer[][] matrix = null;
	private Random random=new Random(0xdeadbeef);
	private MoveMove lastMove;
	private Board lastBoard;
	private int initialDepth;
	
	public int AlphaBetaNS(Board board, Color color, int depth, int alpha, int beta) {

		if (depth == 0) {
			return eval(board);	
		}
		
		if (board.getWinner() == color) {
			if(depth == initialDepth) {
				return eval(board) + 1000;
			} else {
				return eval(board) + 200;
			}
		}
		
		if(lastBoard != null && lastMove != null && depth == initialDepth) {
			if(lastBoard.getState(lastMove.getDstX(), lastMove.getDstY()) == getOpponent(getColor())
				&& getValue(lastMove.getDstX(), lastMove.getDstY()) > 3) {
				return eval(board) + 9*getValue(lastMove.getDstX(), lastMove.getDstY());
			}
		}
		
		int val = -9999, cur, n = beta;

		for (Move leaf : getMovesFor(board, color)) {
			if(movePossible(board, leaf)) {
				lastBoard = board.clone();
				lastMove = (MoveMove) leaf;
				board.doMove(leaf);
				cur = -AlphaBetaNS(board, getOpponent(color), depth-1, -n, -alpha);
				if (cur > val) {
					val = (n == beta || depth <= 2) ? cur : -AlphaBetaNS(board, getOpponent(color), depth-1, -beta, -cur);
				}
				board.undoMove(leaf);
	
				if (val > alpha) 
					alpha = val;
	
				if (alpha >= beta) 
					return alpha;
	
				n = alpha + 1;
			}
		}
		return val;
	}
	
	private boolean movePossible(Board board, Move move) {
		MoveMove mmove = (MoveMove) move;
		return (board.getState(mmove.getDstX(), mmove.getDstY()) != mmove.getColor());
	}
	
	private static void generateValues(Board b) {
		int bs = b.getSize();
		matrix = new Integer[bs][bs];
		for(int i=0; i<(bs+1)/2; ++i) {
			for(int j=(int)i; j<bs-i; ++j) {
				for(int k=(int)i; k<bs-i; ++k) {
					if(i == 0 || k == 0) {
						matrix[j][k] = matrix[k][j] = 1;
					} else {
						matrix[j][k] = matrix[k][j] = i*3;
					}
				}
			}
		}
		matrix[0][0] = matrix[0][bs-1] = matrix[bs-1][0] = matrix[bs-1][bs-1] = 0;
	}
	
	List<Move> getMovesFor(Board board, Color color) {
		List<Move> moves = board.getMovesFor(color);
		int maxVal = -9999, maxIndex = 0, tmp;
		for (int i = 0; i < moves.size(); ++i) {
			board.doMove(moves.get(i));
			if ((tmp = eval(board)) > maxVal) {
				maxVal = tmp;
				maxIndex = i;
			}
			board.undoMove(moves.get(i));
		}
		Collections.swap(moves, 0, maxIndex);
		return moves;
	}
	
	@Override
	public String getName() {
		return "Piotr & Blazej";
	}
	
	private static int getValue(int x, int y) {
		return matrix[x][y];
	}
	
	private int eval(Board b) {
		int res = 0;
		int bs = b.getSize();
		for(int i=0; i<bs; ++i) {
			for(int j=0; j<bs; ++j) {
				if(b.getState(i, j) == getColor()) {
					res += getValue(i,j);
				}/* else if(b.getState(i, j) == getOpponent(getColor())) {
					res -= getValue(i,j)/3;
					if(res < 0) res = 0;
				}*/
			}
		}
		return res;
	}

	@Override
	public Move nextMove(Board board) {
		if(matrix == null) generateValues(board);
		Board tmpBoard = board.clone();
		//List<Move> moves = board.getMovesFor(getColor());
		List<Move> moves = getMovesFor(tmpBoard, getColor());
		Move bestMove = null;
		int bestValue = -10000;
		initialDepth = 4;
		for(Move move : moves) {
			if(movePossible(tmpBoard, move)) {
				lastBoard = tmpBoard.clone();
				lastMove = (MoveMove) move;
				tmpBoard.doMove(move);
				int value = Math.abs(AlphaBetaNS(tmpBoard, getOpponent(getColor()), initialDepth, -99999, 99999));
				if(value > bestValue || (value == bestValue && random.nextInt(100) % 2 == 0)) {
					bestMove = move;
					bestValue = value;
				}
				tmpBoard.undoMove(move);
			}
		}
		return bestMove;
	}

}
