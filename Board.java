import java.util.*;

class Board {

    public static final int NUM_ROW_COL = 8;
    private Piece[][] board;

    public Board() {
	board = new Piece[NUM_ROW_COL][NUM_ROW_COL]; 
	setup();
    }

    public void setup() {
	// Initialize our board to every other piece is empty, and the center elements of the board to empty.
	
	for(int r = 0; r < NUM_ROW_COL; r++){
	    for( int c = 0; c < NUM_ROW_COL; c++) {
		// We dont want to place our pieces on every square as this is not chess.
		if( r % 2 == c % 2 ) {
		    if( r < 3 )
			board[r][c] = new Piece("BLACK");//BLACK;
		    else if ( r > 4 )
			board[r][c] = new Piece("RED"); //RED;
		    else
			board[r][c] = new Piece("EMPTY"); // EMPTY;
		}
		else
		    board[r][c] = new Piece("EMPTY"); //EMPTY;
	    }
	}
    }

    public Piece getPiece (int r, int c) { return board[r][c]; }
    public void setPiece (int r, int c, Piece p) { board[r][c] = p; }
    public void movePiece(Move m) {
	// m must be a valid move.
	// make the move and set the pieces original position to empty

	int toR = m.toR;
	int toC = m.toC;
	int fromR = m.fromR;
	int fromC = m.fromC;
	
	Piece p = board[fromR][fromC];
	board[toR][toC] = board[fromR][fromC];
	board[fromR][fromC] = p;

	// If was jump, we must erase the jumped over piece.
	if(m.isJump()) {
	    int jumpR = (fromR + toR) / 2;
	    int jumpC = (fromC + toC) / 2;
	    board[jumpR][jumpC] = new Piece("EMPTY");
	}
	
	// If a red piece makes it to the end, king them
	if(toR == 0 && board[toR][toC].isRed()) {
	    board[toR][toC] = new Piece("RED_KING");
	}
	if(toR == NUM_ROW_COL -1 && board[toR][toC].isBlack()) {
	    board[toR][toC] = new Piece("BLACK_KING");
	}
    }

    public MoveSequence getValidMoves(Player p) {
	// if no moves available return null.
	// else return a MoveSequence of all possible moves for player p.
	// this includes all valid jumps

	ArrayList<Move> moves = new ArrayList<Move>();

	
	// look for jumps
	for(int r = 0; r < NUM_ROW_COL; r++) {
	    for(int c = 0; c < NUM_ROW_COL; c++) {
		// For all red pieces, check to see if it can jump for all 4 diagonals.
		if(board[r][c].isPlayer(p)) {
		    if(canJump(r, c, r+1, c+1, r+2, c+2, p)) {
			moves.add(new Move(r,c, r+2,c+2));
		    }
		    if(canJump(r, c, r-1, c+1, r-2, c+2, p)) {
			moves.add(new Move(r,c, r-2, c+2));
		    }
		    if(canJump(r, c, r+1, c-1, r+2, c-2, p)) {
			moves.add(new Move(r, c, r+2, c-2));
		    }
		    if(canJump(r, c, r-1, c-1, r-2, c-2, p)){
			moves.add(new Move(r, c, r-2, c-2));
		    }
		}
	    }
	}

	// Pask, p.122, states, “All jumping moves are compulsory.” Every opportunity to jump must be taken.
	// In the case where there are different jump sequences available, the player may chose which sequence
	// to make, whether it results in the most pieces being taken or not.

	// Since if a jump is available the user must jump, dont get more moves unless moves is empty
	if(moves.isEmpty()) {
	    // moves wasnt empty, look for reqular moves.
	    for(int r = 0; r < NUM_ROW_COL; r++) {
		for(int c = 0; c < NUM_ROW_COL; c++) {
		    if(board[r][c].isPlayer(p)) {
			if(canMove(r, c, r+1, c+1, p))
			    moves.add(new Move(r, c, r+1, c+1));
			if(canMove(r, c, r+1, c-1, p))
			    moves.add(new Move(r, c, r+1, c-1));
			if(canMove(r, c, r-1, c+1, p))
			    moves.add(new Move(r, c, r-1, c+1));
			if(canMove(r, c, r-1, c-1, p))
			    moves.add(new Move(r, c, r-1, c-1));
		    }
		}
	    }
	}

	if(moves.isEmpty())
	    return null; // no valid moves found
	else {
	    MoveSequence mSeq = new MoveSequence(moves);
	    return mSeq;
	}
    }

    private boolean canJump(int fromR, int fromC, int middleR, int middleC, int toR, int toC, Player p) {
	
	if( toC < 0 || toC >= NUM_ROW_COL || toC < 0 || toC >= NUM_ROW_COL)
	    return false; // the move was off the board.

	if(!board[toR][toC].isEmpty())
	    return false; // Landing on a piece

	// If the player is red check red else check black
	if(p.isRed()) {
	    // Red cannot move backwards unless king
	    if((board[fromR][fromC].getPiece().equals("NORMAL")) && toR > fromR)
		return false;

	    // Red cannot jump red.
	    if(board[middleR][middleC].isRed())
		return false;

	    // Else its a valid jump.
	    return true;
	}

	else {

	    // Black normal pieces cannot move up
	    if((board[fromR][fromC].getPiece().equals("NORMAL")) && toR < fromR)
		return false;

	    // Black cannot jump black
	    if(board[middleR][middleC].isBlack())
		return false;

	    // Else its a valid jump
	    return true;
	}
    }

    private boolean canMove(int fromR, int fromC, int toR, int toC, Player p) {

	if(toR < 0 || toR >= NUM_ROW_COL || toC < 0 || toC >= NUM_ROW_COL)
	    return false; // move was off the board.

	if(!board[toR][toC].isEmpty())
	    return false; // Cannot move to empty spaces

	if(p.isRed()) {
	    // normal red pieces cannot move up
	    if(board[fromR][fromC].getPiece().equals("NORMAL") && fromR < toR)
		return false;
	    return true;
	}
	else {
	    // normal black pieces cannot move down
	    if(board[fromR][fromC].getPiece().equals("NORMAL") && fromR > toR)
		return false;
	    return true;
	}
    }

    public MoveSequence getValidJumps(int fromR, int fromC, Player p) {
	// Returns a MoveSequence of all valid jumps for the given piece at fromR, fromC
	// will return null if there are no valid jumps or if the piece is not the players.

	ArrayList<Move> moves = new ArrayList<Move>();

	if(board[fromR][fromC].isPlayer(p)) {
	    if(canJump(fromR, fromC, fromR+1, fromC+1, fromR+2, fromC+2, p))
		moves.add(new Move(fromR, fromC, fromR+2, fromC+2));
	    if(canJump(fromR, fromC, fromR+1, fromC-1, fromR+2, fromC-2, p))
		moves.add(new Move(fromR, fromC, fromR+2, fromC-2));
	    if(canJump(fromR, fromC, fromR-1, fromC+1, fromR-2, fromC+2, p))
		moves.add(new Move(fromR, fromC, fromR-2, fromC+2));
	    if(canJump(fromR, fromC, fromR-1, fromC-1, fromR-2, fromC-2, p))
		moves.add(new Move(fromR, fromC, fromR-2, fromC-2));
	}

	if(moves.isEmpty())
	    return null; // no jumps possible
	else
	    return new MoveSequence(moves);
	    
    }
    
}