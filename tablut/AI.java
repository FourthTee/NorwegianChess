package tablut;

import java.util.List;

import static java.lang.Math.*;

import static tablut.Piece.*;

/** A Player that automatically generates moves.
 *  @author Fourth Teerakapibal
 */
class AI extends Player {

    /** A position-score magnitude indicating a win (for white if positive,
     *  black if negative). */
    private static final int WINNING_VALUE = Integer.MAX_VALUE - 20;
    /** A position-score magnitude indicating a forced win in a subsequent
     *  move.  This differs from WINNING_VALUE to avoid putting off wins. */
    private static final int WILL_WIN_VALUE = Integer.MAX_VALUE - 40;
    /** A magnitude greater than a normal value. */
    private static final int INFTY = Integer.MAX_VALUE;

    /** A new AI with no piece or controller (intended to produce
     *  a template). */
    AI() {
        this(null, null);
    }

    /** A new AI playing PIECE under control of CONTROLLER. */
    AI(Piece piece, Controller controller) {
        super(piece, controller);
    }

    @Override
    Player create(Piece piece, Controller controller) {
        return new AI(piece, controller);
    }

    @Override
    String myMove() {
        Move move = findMove();
        _controller.reportMove(move);
        return move.toString();
    }

    @Override
    boolean isManual() {
        return false;
    }

    /** Return a move for me from the current position, assuming there
     *  is a move. */
    private Move findMove() {
        Board b = new Board(board());
        if (_myPiece == BLACK) {
            findMove(b, maxDepth(b), true, -1, -INFTY, INFTY);
        } else {
            findMove(b, maxDepth(b), true, 1, -INFTY, INFTY);
        }
        return _lastFoundMove;
    }

    /** The move found by the last call to one of the ...FindMove methods
     *  below. */
    private Move _lastFoundMove;

    /** Find a move from position BOARD and return its value, recording
     *  the move found in _lastFoundMove iff SAVEMOVE. The move
     *  should have maximal value or have value > BETA if SENSE==1,
     *  and minimal value or value < ALPHA if SENSE==-1. Searches up to
     *  DEPTH levels.  Searching at level 0 simply returns a static estimate
     *      *  of the board value and does not set _lastMoveFound. */
    private int findMove(Board board, int depth, boolean saveMove,
                         int sense, int alpha, int beta) {
        if (board.winner() != null || depth == 0) {
            return staticScore(board);
        }

        if (sense == -1) {
            int minimize = INFTY;
            List<Move> possmoves = board.legalMoves(BLACK);
            for (int i = 0; i < possmoves.size(); i++) {
                Move next = possmoves.get(i);
                board.makeMove(next);
                int max = findMove(board, depth - 1, false, 1, alpha, beta);
                minimize = Math.min(minimize, max);
                board.undo();
                if (minimize < beta) {

                    if (saveMove) {
                        _lastFoundMove = next;
                    }
                    beta = minimize;
                }
                if (beta <= alpha) {
                    break;
                }
            }
            return minimize;
        } else {
            int maximize = -INFTY;
            List<Move> possmoves = board.legalMoves(WHITE);
            for (int i = 0; i < possmoves.size(); i++) {
                Move next = possmoves.get(i);
                board.makeMove(next);
                int min = findMove(board, depth - 1, false, -1, alpha, beta);
                maximize = Math.max(maximize, min);
                board.undo();

                if (alpha < maximize) {
                    if (saveMove) {
                        _lastFoundMove = next;
                    }
                    alpha = maximize;
                }
                if (beta <= alpha) {
                    break;
                }
            }
            return maximize;
        }
    }

    /** Range for selectioning depth search.*/
    static final int RANGE2 = 23;

    /** Range for selecting depth search.*/
    static final int RANGE3 = 40;


    /** Return a heuristically determined maximum search depth
     *  based on characteristics of BOARD. */
    private static int maxDepth(Board board) {
        return 1;

    }

    /** Return a heuristic value for BOARD. */
    private int staticScore(Board board) {
        int numlegalmoves = board.legalMoves(WHITE).size();
        int distking = disttoedge(board);
        int dif = diffblackwhite(board);
        return  dif + distking;
    }

    /** Return distance from king to edge.
     * @param board current board
     * */
    private int disttoedge(Board board) {
        if (board.winner() == BLACK) {
            return -9 * 11;
        }
        if (board.winner() == WHITE) {
            return +9 * 11;
        }
        return 0;

    }

    /** Return number of black pieces surroudning king.
     * @param king king's square
     * @param board current board
     * */
    private int aroundking(Square king, Board board) {
        int black = 0;
        if (king == null) {
            return 0;
        }
        if (board == null) {
            return 0;
        }
        for (int i = 0; i < 4; i++) {
            Square sq2 = king.rookMove(i, 2);
            if (sq2 == null) {
                continue;
            }
            if (board.get(sq2).equals(BLACK)) {
                black++;
            }
        }
        return black;
    }

    /** Return the difference between white and black pieces.
     * @param board current board
     * */
    private int diffblackwhite(Board board) {
        int white = board.numwhite();
        int black = board.numblack();
        return white - black;
    }
}
