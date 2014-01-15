package put.ai.snort.alphabetaplayer;

import java.util.List;
import java.util.Random;

import put.ai.snort.game.Board;
import put.ai.snort.game.Move;
import put.ai.snort.game.Player;

public class AlphaBetaPlayer extends Player {

	private static Integer[][] matrix = null;
	private Random random=new Random(0xdeadbeef);
	
	public int AlphaBetaNS(Board board, Color color, int depth, int alpha, int beta) {

		if (depth == 0) {
			return eval(board);	
		}
		
		if (board.getWinner() == color) {
			return eval(board) + 100;
		}
		
		int val = -9999, cur, n = beta;

		for (Move leaf : board.getMovesFor(color)) {
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
		return val;
	}
	
	private static void generateValues(Board b) {
		int bs = b.getSize();
		matrix = new Integer[bs][bs];
		for(int i=0; i<(bs+1)/2; ++i) {
			for(int j=i; j<bs-i; ++j) {
				for(int k=i; k<bs-i; ++k) {
					matrix[j][k] = i;
					matrix[k][j] = i;
				}
			}
		}
		for(int i=0; i<bs; ++i) {
			for(int j=0; j < bs; ++j) {
				System.out.print(getValue(i,j));
			}
			System.out.println();
		}
	}
	
	@Override
	public String getName() {
		return "Player AlphaBeta";
	}
	
	private static int getValue(int x, int y) {
		return matrix[x][y];
	}
	
	private int eval(Board b) {
		if(matrix == null) {
			generateValues(b);
		}
		int res = 0;
		int bs = b.getSize();
		for(int i=0; i<bs; ++i) {
			for(int j=0; j<bs; ++j) {
				if(b.getState(i, j) == getColor()) {
					res += getValue(i,j);
				}
			}
		}
		return res;
	}

	@Override
	public Move nextMove(Board board) {
		List<Move> moves = board.getMovesFor(getColor());
		Move bestMove = null;
		int bestValue = -1;
		Board tmpBoard = board.clone();
		for(Move move : moves) {
			tmpBoard.doMove(move);
			int value = Math.abs(AlphaBetaNS(tmpBoard, getOpponent(getColor()), 4, -99999, 99999));
			//System.out.println(String.format("%s: %d", move, value));
			if(value > bestValue) {
				bestMove = move;
				bestValue = value;
			}
			tmpBoard.undoMove(move);
		}
		return bestMove;
	}

}
