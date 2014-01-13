package put.ai.snort.alphabetaplayer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

import put.ai.snort.game.Board;
import put.ai.snort.game.Move;
import put.ai.snort.game.Player;

public class AlphaBetaPlayer extends Player {

	private static Integer[][] matrix = null;
	private Random random=new Random(0xdeadbeef);
	
	public int AlphaBeta(Board board, Color color, int depth, int alpha, int beta) {

		if (board.getWinner() != null || depth == 0) {
			return eval(board);	
		}
		
		int val;
		//Color color = (initDepth - depth) % 2 == 0 ? getColor() : getOpponent(getColor());
		for (Move leaf : board.getMovesFor(color)) {
			board.doMove(leaf);
			val = -AlphaBeta(board, getOpponent(color), depth-1, -beta, -alpha);
			board.undoMove(leaf);
			if (val > alpha) {
				alpha = val;
			}
			if (alpha >= beta) {
				return beta;
			}
		}
		return alpha;
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
			int value = AlphaBeta(board.clone(), getColor(), 7, -99999, 99999);
			System.out.println(String.format("%s: %d", move, value));
			if(value > bestValue) {
				bestMove = move;
				bestValue = value;
			}
			tmpBoard.undoMove(move);
		}
		return bestMove;
	}

}
