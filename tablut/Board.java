package tablut;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Stack;
import java.util.List;
import java.util.HashSet;
import java.util.Formatter;

import static tablut.Piece.*;
import static tablut.Square.*;
import static tablut.Move.mv;


/** The state of a Tablut Game.
 *  @author Fourth Teerakapibal*/
class Board {

    /** The number of squares on a side of the board. */
    static final int SIZE = 9;

    /** The throne (or castle) square and its four surrounding squares.. */
    static final Square THRONE = sq(4, 4),
        NTHRONE = sq(4, 5),
        STHRONE = sq(4, 3),
        WTHRONE = sq(3, 4),
        ETHRONE = sq(5, 4);

    /** Initial positions of attackers. */
    static final Square[] INITIAL_ATTACKERS = {
        sq(0, 3), sq(0, 4), sq(0, 5), sq(1, 4),
        sq(8, 3), sq(8, 4), sq(8, 5), sq(7, 4),
        sq(3, 0), sq(4, 0), sq(5, 0), sq(4, 1),
        sq(3, 8), sq(4, 8), sq(5, 8), sq(4, 7)
    };

    /** Initial positions of defenders of the king. */
    static final Square[] INITIAL_DEFENDERS = {
        NTHRONE, ETHRONE, STHRONE, WTHRONE,
        sq(4, 6), sq(4, 2), sq(2, 4), sq(6, 4)
    };

    /** Initializes a game board with SIZE squares on a side in the
     *  initial position. */
    Board() {
        init();
    }

    /** Initializes a copy of MODEL. */
    Board(Board model) {
        copy(model);
    }

    /** Copies MODEL into me. */
    void copy(Board model) {
        if (model == this) {
            return;
        }
        init();
        for (Square sq : SQUARE_LIST) {
            _board.replace(sq, model._board.get(sq));
        }
        this._turn = model._turn;
        this._moveCount = model._moveCount;
        this._movelim = model._movelim;
        this._undostack = model._undostack;
        this._repeated = model._repeated;

    }

    /** Clears the board to the initial position. */
    void init() {
        this._board = new HashMap<>();
        for (int row = 0; row < this.SIZE; row++) {
            for (int col = 0; col < this.SIZE; col++) {
                Square cur = sq(col, row);
                _board.put(cur, EMPTY);
            }
        }
        for (Square black: INITIAL_ATTACKERS) {
            _board.replace(black, BLACK);
        }
        for (Square white: INITIAL_DEFENDERS) {
            _board.replace(white, WHITE);
        }
        _board.replace(THRONE, KING);
        _turn = BLACK;
        _winner = null;
        clearUndo();
        _moveCount = 0;
        _movelim = -3;

    }

    /** Set the move limit to LIM.  It is an error if 2*LIM <= moveCount().
     * @param n new setting for limit. */
    void setMoveLimit(int n) {
        if (2 * n <= moveCount()) {
            throw new AssertionError("wrong lim");
        }
        this._movelim = n;
    }

    /** Return a Piece representing whose move it is (WHITE or BLACK). */
    Piece turn() {
        return _turn;
    }

    /** Return the winner in the current position, or null if there is no winner
     *  yet. */
    Piece winner() {
        return _winner;
    }

    /** Returns true iff this is a win due to a repeated position. */
    boolean repeatedPosition() {
        return _repeated;
    }

    /** Record current position and set winner() next mover if the current
     *  position is a repeat. */
    private void checkRepeated() {
        if (_undostack.search(_board) != -1) {
            if (turn() == BLACK) {
                _winner = WHITE;
            } else {
                _winner = BLACK;
            }
            _repeated = true;
        }
    }

    /** Return the number of moves since the initial position that have not been
     *  undone. */
    int moveCount() {
        return _moveCount;
    }

    /** Return location of the king. */
    Square kingPosition() {

        for (Square sq : SQUARE_LIST) {
            if (_board.get(sq) == KING) {
                return sq;
            }
        }
        _winner = BLACK;
        return null;
    }

    /** Return the contents the square at S. */
    final Piece get(Square s) {
        return get(s.col(), s.row());
    }

    /** Return the contents of the square at (COL, ROW), where
     *  0 <= COL, ROW <= 9. */
    final Piece get(int col, int row) {
        return _board.get(Square.sq(col, row));
    }

    /** Return the contents of the square at COL ROW. */
    final Piece get(char col, char row) {
        return get(col - 'a', row - '1');
    }

    /** Set square S to P. */
    final void put(Piece p, Square s) {
        _board.replace(s, p);
    }

    /** Set square S to P and record for undoing. */
    final void revPut(Piece p, Square s) {
        this.put(p, s);
    }

    /** Set square COL ROW to P. */
    final void put(Piece p, char col, char row) {
        put(p, sq(col - 'a', row - '1'));
    }

    /** Return true iff FROM - TO is an unblocked rook move on the current
     *  board.  For this to be true, FROM-TO must be a rook move and the
     *  squares along it, other than FROM, must be empty. */
    boolean isUnblockedMove(Square from, Square to) {
        if ((from.row() != to.row() && from.col() != to.col()) || from == to) {
            return false;
        }
        int frow = from.row();
        int fcol = from.col();
        int trow = to.row();
        int tcol = to.col();
        if (frow == trow) {
            if (fcol < tcol) {
                for (int i = fcol + 1; i <= tcol; i++) {
                    Square check = sq(i, frow);
                    if (!_board.get(check).equals(EMPTY)) {
                        return false;
                    }
                }
            }
            if (fcol > tcol) {
                for (int i = fcol - 1; i >= tcol; i--) {
                    Square check = sq(i, frow);
                    if (!_board.get(check).equals(EMPTY)) {
                        return false;
                    }
                }
            }
        }
        if (fcol == tcol) {
            if (frow < trow) {
                for (int i = frow + 1; i <= trow; i++) {
                    Square check = sq(fcol, i);
                    if (!_board.get(check).equals(EMPTY)) {
                        return false;
                    }
                }
            }
            if (frow > trow) {
                for (int i = frow - 1; i >= trow; i--) {
                    Square check = sq(fcol, i);
                    if (!_board.get(check).equals(EMPTY)) {
                        return false;
                    }
                }
            }
        }
        return true;
    }

    /** Return true iff FROM is a valid starting square for a move. */
    boolean isLegal(Square from) {
        return get(from).side() == _turn;
    }

    /** Return true iff FROM-TO is a valid move. */
    boolean isLegal(Square from, Square to) {
        if (isLegal(from) && isUnblockedMove(from, to) && from.isRookMove(to)) {
            if (to == THRONE) {
                return _board.get(from) == KING;
            } else {
                return true;
            }
        }
        return false;
    }

    /** Return true iff MOVE is a legal move in the current
     *  position. */
    boolean isLegal(Move move) {
        return isLegal(move.from(), move.to());
    }

    /** Move FROM-TO, assuming this is a legal move. */
    void makeMove(Square from, Square to) {
        if (!isLegal(from, to)) {
            return;
        }
        assert isLegal(from, to);
        HashMap<Square, Piece> newstate = deepCopy(_board);
        _undostack.push(newstate);
        Piece piece =  _board.get(from);
        put(piece, to);
        put(EMPTY, from);
        if (kingPosition() == null) {
            int h = 1;
        }
        if (winner() == null && kingPosition().isEdge()) {
            _winner = WHITE;
        }

        checkRepeated();
        capturehelper(to);

        _turn = _turn.opponent();
        if (!hasMove(_turn)) {
            _winner = _turn.opponent();
        }
        _moveCount++;
        if (moveCount() >= 2 * _movelim && _movelim > 0) {
            _winner = _turn;
        }
    }
    /** Deep copies the hashmap.
     * @return copy hashmap
     * @param board */
    public HashMap<Square, Piece> deepCopy(HashMap<Square, Piece> board) {
        HashMap<Square, Piece> copy = new HashMap<>();
        for (Square sq: SQUARE_LIST) {
            copy.put(sq, board.get(sq));
        }
        return copy;
    }

    /** Checks all the directions of the square and calls capture if needed.
     * @param sq */
    void capturehelper(Square sq) {
        for (int i = 0; i < 4; i++) {
            Square sq2 = sq.rookMove(i, 2);
            if (sq2 != null) {
                capture(sq, sq2);
            }
        }
    }

    /** Move according to MOVE, assuming it is a legal move. */
    void makeMove(Move move) {
        makeMove(move.from(), move.to());
    }

    /** Capture the piece between SQ0 and SQ2, assuming a piece just moved to
     *  SQ0 and the necessary conditions are satisfied. */
    private void capture(Square sq0, Square sq2) {
        Piece sp0 = _board.get(sq0); Piece sp2 = _board.get(sq2);
        if (sp0 == KING) {
            sp0 = WHITE;
        }
        if (sp2 == KING) {
            sp2 = WHITE;
        }
        Square capsq = sq0.between(sq2);
        if (_board.get(capsq) == KING && (capsq == NTHRONE || capsq == ETHRONE
                || capsq == STHRONE || capsq == WTHRONE || capsq == THRONE)) {
            if (kingcap(capsq)) {
                _board.replace(capsq, EMPTY);
                _winner = BLACK;
            }

        } else {
            if (sq2 == THRONE) {
                if (_board.get(THRONE) == EMPTY) {
                    if (_board.get(capsq) != _board.get(sq0)) {
                        put(EMPTY, capsq);
                    }
                } else {
                    if (surthrown()) {
                        if (_board.get(capsq) != _board.get(sq0)) {
                            put(EMPTY, capsq);
                        }
                    } else {
                        if (_board.get(sq0) == WHITE
                                && _board.get(capsq) == BLACK) {
                            put(EMPTY, capsq);
                        }
                    }
                }

            } else {
                if (sp0 == sp2) {
                    if (_board.get(capsq) != _board.get(sq0)) {
                        if (_board.get(capsq) == KING) {
                            _winner = BLACK;
                        }
                        put(EMPTY, capsq);
                    }
                }
            }
        }
    }
    /** check if the king gets captured.
     * @return true or false
     * @param sq */
    boolean kingcap(Square sq) {
        for (int i = 0; i < 4; i++) {
            Square sq2 = sq.rookMove(i, 1);
            if (_board.get(sq2).side() != BLACK && sq2 != THRONE) {
                if (_board.get(sq2).side() == WHITE
                        || _board.get(sq2).side() == EMPTY) {
                    return false;
                }

            }
        }
        return true;
    }
    /** check if thrown is surrounded by 3 black squares.
     * @ returns true false*/
    boolean surthrown() {
        boolean[] check = new boolean[4];
        int count = 0;
        if (_board.get(NTHRONE).side() == BLACK) {
            check[0] = true;
        }
        if (_board.get(ETHRONE).side() == BLACK) {
            check[1] = true;
        }
        if (_board.get(STHRONE).side() == BLACK) {
            check[2] = true;
        }
        if (_board.get(WTHRONE).side() == BLACK) {
            check[3] = true;
        }
        for (int i = 0; i < 4; i++) {
            if (check[i]) {
                count++;
            }
        }
        return count == 3;
    }

    /** Undo one move.  Has no effect on the initial board. */
    void undo() {
        if (_moveCount > 0) {
            undoPosition();
            _moveCount -= 1;
            _turn = _turn.opponent();
            _winner = null;
        }
    }

    /** Remove record of current position in the set of positions encountered,
     *  unless it is a repeated position or we are at the first move. */
    private void undoPosition() {
        _board = _undostack.pop();
        _repeated = false;
    }

    /** Clear the undo stack and board-position counts. Does not modify the
     *  current position or win status. */
    void clearUndo() {
        _undostack.empty();

    }

    /** Return a new mutable list of all legal moves on the current board for
     *  SIDE (ignoring whose turn it is at the moment). */
    List<Move> legalMoves(Piece side) {
        HashSet<Square> location = pieceLocations(side);
        ArrayList<Move> movearr = new ArrayList<>();
        for (Square piecesq : location) {
            for (Square tosq : SQUARE_LIST) {
                if (isLegal(piecesq, tosq)) {
                    Move move = mv(piecesq, tosq);
                    movearr.add(move);
                }
            }
        }
        return movearr;

    }

    /** Return true iff SIDE has a legal move. */
    boolean hasMove(Piece side) {
        return !legalMoves(side).isEmpty();
    }

    @Override
    public String toString() {
        return toString(true);
    }

    /** Return a text representation of this Board.  If COORDINATES, then row
     *  and column designations are included along the left and bottom sides.
     */
    String toString(boolean coordinates) {
        Formatter out = new Formatter();
        for (int r = SIZE - 1; r >= 0; r -= 1) {
            if (coordinates) {
                out.format("%2d", r + 1);
            } else {
                out.format("  ");
            }
            for (int c = 0; c < SIZE; c += 1) {
                out.format(" %s", get(c, r));
            }
            out.format("%n");
        }
        if (coordinates) {
            out.format("  ");
            for (char c = 'a'; c <= 'i'; c += 1) {
                out.format(" %c", c);
            }
            out.format("%n");
        }
        return out.toString();
    }

    /** Return the locations of all pieces on SIDE. */
    private HashSet<Square> pieceLocations(Piece side) {
        assert side != EMPTY;
        HashSet<Square> location = new HashSet<>();
        for (Square sq : SQUARE_LIST) {
            if (_board.get(sq) == side) {
                location.add(sq);
            }
            if (_board.get(sq) == KING && side == WHITE) {
                location.add(sq);
            }
        }
        return location;
    }

    /** Return the contents of _board in the order of SQUARE_LIST as a sequence
     *  of characters: the toString values of the current turn and Pieces. */
    String encodedBoard() {
        char[] result = new char[Square.SQUARE_LIST.size() + 1];
        result[0] = turn().toString().charAt(0);
        for (Square sq : SQUARE_LIST) {
            result[sq.index() + 1] = get(sq).toString().charAt(0);
        }
        return new String(result);
    }

    /** Return the number of black pieces on the board.*/
    int numblack() {
        return pieceLocations(BLACK).size();
    }

    /** Return the number of white pieces on the board.*/
    int numwhite() {
        return pieceLocations(WHITE).size() + 1;
    }

    /** Piece whose turn it is (WHITE or BLACK). */
    private Piece _turn;
    /** Cached value of winner on this board, or EMPTY if it has not been
     *  computed. */
    private Piece _winner;
    /** Number of (still undone) moves since initial position. */
    private int _moveCount;
    /** True when current board is a repeated position (ending the game). */
    private boolean _repeated;

    /** Create the _board.*/
    private HashMap<Square, Piece> _board;

    /** move limit.*/
    private int _movelim;

    /** Stack with all the previous moves. */
    private Stack<HashMap<Square, Piece>> _undostack = new Stack<>();
}
