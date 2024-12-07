/* Group 2 - https://codeshare.io/1YyQWB
* Backup Repo: https://drive.google.com/drive/folders/1eWwW2Yh3yeRl12HR-4fShJ16syx-5jnW?usp=sharing
* Members: Mohammad, Tu, Jereimie, Gari
* 
* General Divide of work:
* 
* Mohammad: Game Drawing
* Tu: Piece movement logic
* Gari: Piece movement logic
* Jereimie: Inital setup/Organization, Mouse Evenets/Mouse gfx, ChessUtil
*/

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import javax.imageio.ImageIO;
import javax.swing.JFrame;
import javax.swing.JPanel;
import java.awt.FontMetrics;

class Position
{
	int row;
	int col;

	public Position()
	{
		this.row = -1;
		this.col = -1;
	}

	public Position(Position p)
	{
		this();

		if( p != null )
		{
			this.row = p.row;
			this.col = p.col;
		}
	}

	public Position(int inRow, int inCol)
	{
		this.row = inRow;
		this.col = inCol;
	}

	public boolean equals(Object obj)
	{
		if( obj != null && obj instanceof Position )
		{
			Position pos = (Position) obj;

			if( pos == this || (pos.row == this.row && pos.col == this.col) )
			{
				return true;
			}
		}

		return false;
	}

	public String toString()
	{
		return "[" + this.row + "][" + this.col + "]";
	}
}

abstract class ChessPiece
{
	int player = 0;
	protected boolean hasMoved = false;
	private Position myPosition = new Position();
	private ChessPiece[][] myBoard = null;

	public ChessPiece()
	{
	}

	// clears position and Sets myBoard = to board
	// If the board exists and position is valid sets it and returns true.
	// otherwise sets it to [-1][-1] and returns false (board is changed).
	public boolean setPos(ChessPiece[][] board, int row, int col)
	{
		this.clearPos();
		this.setBoard(board);

		if( ChessUtil.posValid(this.getBoard(), row, col) )
		{
			this.setRow(row);
			this.setCol(col);
			this.getBoard()[this.getRow()][this.getCol()] = this;

			if( this.getBoard() == ChessGame.board && this instanceof Pawn
					&& ((this.player == 2 && row == 0) || (this.player == 1 && row == 7)) )
			{
				char selectPiece = 'Q';
				ChessPiece p = null;

				this.clearPos();

				// @formatter:off
				if (selectPiece == 'H') {p = new Knight();}
				else                    {p = new Queen();}
				// @formatter:on

				p.player = this.player;
				p.setPos(board, row, col);
			}

			return true;
		}

		this.setRow(-1);
		this.setCol(-1);
		return false;
	}

	public boolean setPos(int row, int col)
	{
		return this.setPos(this.getBoard(), row, col);
	}

	// clears the location and board. Returns true if successful
	public void clearPos()
	{
		if( this.getBoard() != null )
		{
			this.getBoard()[this.getRow()][this.getCol()] = null;
			this.setBoard(null);
		}

		this.setRow(-1);
		this.setCol(-1);
	}

	public int getRow()
	{
		return this.myPosition.row;
	}

	public void setRow(int rowIn)
	{
		this.myPosition.row = rowIn;
	}

	public int getCol()
	{
		return this.myPosition.col;
	}

	public void setCol(int colIn)
	{
		this.myPosition.col = colIn;
	}

	public ChessPiece[][] getBoard()
	{
		return this.myBoard;
	}

	public void setBoard(ChessPiece[][] boardIn)
	{
		this.myBoard = boardIn;
	}

	public Position getPosition() // set happens via setPos
	{
		return this.myPosition;
	}

	// scans along step path to reach target space/off the board/or blocked by a piece returns position of stopping
	// piece/square be it a piece or target move or end of grid returns null if it is itself.
	protected Position scanMove(Position tPos, Position cPos, Position iPos) // targetRow/currentRow/incrementRow
	{
		Position rVal = null;

		if( tPos != null && cPos != null && iPos != null && ChessUtil.posValid(this.getBoard(), cPos.row, cPos.col)
				&& this.getBoard()[cPos.row][cPos.col] != this )
		{
			if( this.getBoard()[cPos.row][cPos.col] == null && (cPos.row != tPos.row || cPos.col != tPos.col) )
			{
				rVal = scanMove(tPos, new Position(cPos.row + iPos.row, cPos.col + iPos.col), iPos);
			}

			if( rVal == null )
			{
				rVal = new Position(cPos.row, cPos.col);
			}
		}

		return rVal;
	}

	boolean isLegalMove(int row, int col)
	{
		if( this.getBoard() != null )
		{
			return this.isLegalMove(this.getBoard(), row, col);
		}

		return false;
	}

	public void drawPiece(Graphics g, int x, int y, int size)
	{
		g.drawImage(this.getImg(), x, y, size, size, null);
	}

	/*
	 * Abstract Methods
	 */
	public abstract boolean isLegalMove(ChessPiece[][] board, int row, int col);

	public abstract char getC();

	public abstract BufferedImage getImg();
}

class Pawn extends ChessPiece
{
	private static BufferedImage img1;
	private static BufferedImage img2;

	public boolean isLegalMove(ChessPiece[][] board, int row, int col)
	{
		if( !ChessUtil.posValid(board, row, col) )
		{
			return false;
		}

		ChessPiece target = board[row][col];

		if( target == null && this.player == 1 && this.getCol() == col && this.getRow() == (row - 1) )
		{
			return true;
		}
		if( target == null && this.player == 2 && this.getCol() == col && this.getRow() == (row + 1) )
		{
			return true;
		}
		if( target == null && this.player == 1 && this.getRow() == 1 && this.getCol() == col
				&& (this.getRow() == (row - 2)) && board[row - 1][col] == null )
		{
			return true;
		}
		if( target == null && this.player == 2 && this.getRow() == 6 && this.getCol() == col
				&& (this.getRow() == (row + 2)) && board[row + 1][col] == null )
		{
			return true;
		}
		if( target != null && this.player == 1 && target.player != this.player && this.getRow() == (row - 1)
				&& (this.getCol() == (col - 1) || this.getCol() == (col + 1)) )
		{
			return true;
		}
		if( target != null && this.player == 2 && target.player != this.player && this.getRow() == (row + 1)
				&& (this.getCol() == (col + 1) || this.getCol() == (col - 1)) )
		{
			return true;
		}
		return false;
	}

	public static void bufferImgs()
	{
		Pawn.img1 = ChessUtil.loadPieceImg(ChessUtil.basePath + "Pawn white.PNG", getC(1));
		Pawn.img2 = ChessUtil.loadPieceImg(ChessUtil.basePath + "Pawn black.PNG", getC(1));
		// For p2 Img Using p1 getC for capital letter on piece img if failed load
	}

	public BufferedImage getImg()
	{
		return getImg(this.player);
	}

	public static BufferedImage getImg(int player)
	{
		if( player == 1 )
		{
			return Pawn.img1;
		}

		return Pawn.img2;
	}

	public char getC()
	{
		return getC(this.player);
	}

	public static char getC(int player)
	{
		if( player == 1 )
			return 'P';
		else
			return 'p';
	}
}

class Rook extends ChessPiece
{
	private static BufferedImage img1;
	private static BufferedImage img2;

	public boolean isLegalMove(ChessPiece[][] board, int row, int col)
	{
		if( !ChessUtil.posValid(board, row, col) )
		{
			return false;
		}

		ChessPiece target = board[row][col];
		// checks if the spot is taken or not

		if( this.getCol() == col && ChessUtil.posValid(this.getBoard(), row, col) )
		{
			if( target == null || (target != null && target.player != this.player) )
			{
				if( row > this.getRow() )
				// checks the top as it goes down, if not, pulls down to else statement
				{
					// moving up
					for( int i = row - 1; i > this.getRow(); i-- )
					{
						if( board[i][col] != null )
						{
							return false;
						}
					}
				} else
				{
					// moving down
					for( int i = row + 1; i < this.getRow(); i++ )
					{
						if( board[i][col] != null )
						{
							return false;
						}
					}
				}

				return true;
			}
		}
		if( this.getRow() == row && ChessUtil.posValid(this.getBoard(), row, col) )
		{
			if( target == null || (target != null && target.player != this.player) )
			{
				if( col > this.getCol() )
				{
					// moving right
					for( int i = col - 1; i > this.getCol(); i-- )
					{
						if( board[row][i] != null )
						{
							return false;
						}
					}
				} else
				{
					// moving left
					for( int i = col + 1; i < this.getCol(); i++ )
					{
						if( board[row][i] != null )
						{
							return false;
						}
					}
				}

				return true;
			}
		}

		return false;
	}

	public static void bufferImgs()
	{
		Rook.img1 = ChessUtil.loadPieceImg(ChessUtil.basePath + "Rook white.PNG", getC(1));
		Rook.img2 = ChessUtil.loadPieceImg(ChessUtil.basePath + "Rook black.PNG", getC(1));
		// For p2 Img Using p1 getC for capital letter on piece img if failed load
	}

	public BufferedImage getImg()
	{
		return getImg(this.player);
	}

	public static BufferedImage getImg(int player)
	{
		if( player == 1 )
		{
			return Rook.img1;
		}

		return Rook.img2;
	}

	public char getC()
	{
		return getC(this.player);
	}

	public static char getC(int player)
	{
		if( player == 1 )
			return 'R';
		else
			return 'r';
	}
}

class Knight extends ChessPiece
{
	private static BufferedImage img1;
	private static BufferedImage img2;

	public boolean isLegalMove(ChessPiece[][] board, int row, int col)
	{
		if( !ChessUtil.posValid(board, row, col) )
		{
			return false;
		}

		ChessPiece target = board[row][col];
		if( target == null && ((this.getRow() == (row + 2) || this.getRow() == (row - 2))
				&& (this.getCol() == (col + 1) || this.getCol() == (col - 1))
				|| (this.getRow() == (row + 1) || this.getRow() == (row - 1))
						&& (this.getCol() == (col + 2) || this.getCol() == (col - 2))) )
		{
			return true;
		}

		if( target != null && this.player != target.player
				&& ((this.getRow() == (row + 2) || this.getRow() == (row - 2))
						&& (this.getCol() == (col + 1) || this.getCol() == (col - 1))
						|| (this.getRow() == (row + 1) || this.getRow() == (row - 1))
								&& (this.getCol() == (col + 2) || this.getCol() == (col - 2))) )
		{
			return true;
		}
		return false;
	}

	public static void bufferImgs()
	{
		Knight.img1 = ChessUtil.loadPieceImg(ChessUtil.basePath + "Knight white.PNG", getC(1));
		Knight.img2 = ChessUtil.loadPieceImg(ChessUtil.basePath + "Knight black.PNG", getC(1));
		// For p2 Img Using p1 getC for capital letter on piece img if failed load
	}

	public BufferedImage getImg()
	{
		return getImg(this.player);
	}

	public static BufferedImage getImg(int player)
	{
		if( player == 1 )
		{
			return Knight.img1;
		}

		return Knight.img2;
	}

	public char getC()
	{
		return getC(this.player);
	}

	public static char getC(int player)
	{
		if( player == 1 ) // H is for hourse, K is for king
			return 'H';
		else
			return 'h';
	}
}

class Bishop extends ChessPiece
{
	private static BufferedImage img1;
	private static BufferedImage img2;

	public boolean isLegalMove(ChessPiece[][] board, int row, int col)
	{
		// make sure we are not trying to move to this same square
		if( ChessUtil.posValid(board, row, col) && board[row][col] != this )
		{
			int dRow = row - this.getRow();
			int dCol = col - this.getCol();
			double m = (dCol == 0 ? 0.0 : (double) dRow / dCol);

			if( m * m == 1.0 ) // slope of +/-1 = diagonal aka valid position
			{
				Position targetPos = null;
				Position iPos = new Position(); // Amount to modify row and col to move towards target
				iPos.row = row - this.getRow(); // get the difference
				iPos.col = col - this.getCol();

				if( iPos.row != 0 )
					iPos.row /= Math.abs(iPos.row); // then div by number to convert diff to +/-1
				if( iPos.col != 0 )
					iPos.col /= Math.abs(iPos.col);

				targetPos = super.scanMove(new Position(row, col),
						new Position(this.getRow() + iPos.row, this.getCol() + iPos.col), iPos);

				if( targetPos != null )
				{
					ChessPiece targetPiece = board[targetPos.row][targetPos.col];

					if( targetPos.row == row && targetPos.col == col
							&& (targetPiece == null || (targetPiece != null && targetPiece.player != this.player)) )
					{
						return true;
					}
				}
			}
		}
		return false;
	}

	public static void bufferImgs()
	{
		Bishop.img1 = ChessUtil.loadPieceImg(ChessUtil.basePath + "Pushup white.PNG", getC(1));
		Bishop.img2 = ChessUtil.loadPieceImg(ChessUtil.basePath + "Pushup black.PNG", getC(1));
		// For p2 Img Using p1 getC for capital letter on piece img if failed load
	}

	public BufferedImage getImg()
	{
		return getImg(this.player);
	}

	public static BufferedImage getImg(int player)
	{
		if( player == 1 )
		{
			return Bishop.img1;
		}

		return Bishop.img2;
	}

	public char getC()
	{
		return getC(this.player);
	}

	public static char getC(int player)
	{
		if( player == 1 )
			return 'B';
		else
			return 'b';
	}
}

class King extends ChessPiece
{
	private static BufferedImage img1;
	private static BufferedImage img2;

	public boolean isLegalMove(ChessPiece[][] board, int row, int col)
	{
		if( !ChessUtil.posValid(board, row, col) )
		{
			return false;
		}

		ChessPiece target = board[row][col];

		if( (this.getRow() - 1 <= row && row <= this.getRow() + 1)
				&& (this.getCol() - 1 <= col && col <= this.getCol() + 1) )
		{
			if( target == null || (target != null && target.player != this.player) )
			{
				// is safe and allowing king to cap a king patch a bug allowing king movement close to other king.
				// a king cant move close enough to do so, but the isLegalMove for king does a "mock" movement and
				// then a loop to test pieces if they can capture the mock moved king.
				if( isSafeSq(row, col) || target instanceof King )
				{
					return true;
				}
			}
		}

		if( this.hasMoved == false && this.player == 1 && board[0][2] == null && board[0][1] == null
				&& board[0][0].hasMoved == false && row == 0 && col == 1 )
		{
			return true;
		}
		if( this.hasMoved == false && this.player == 1 && board[0][4] == null && board[0][5] == null
				&& board[0][6] == null && board[0][7] != null && board[0][7].hasMoved == false && row == 0 && col == 5 )
		{
			return true;
		}
		if( this.hasMoved == false && this.player == 2 && board[7][4] == null && board[7][5] == null
				&& board[7][6] == null && board[7][7] != null && board[7][7].hasMoved == false && row == 7 && col == 5 )
		{
			return true;
		}
		if( this.hasMoved == false && this.player == 2 && board[7][2] == null && board[7][1] == null
				&& board[7][0] != null && board[7][0].hasMoved == false && row == 7 && col == 1 )
		{
			return true;
		}

		return false;
	}

	private boolean isSafeSq(int row, int col)
	{
		boolean rVal = true;

		if( this.getBoard() != null )
		{
			ChessPiece dest = this.getBoard()[row][col];
			int thisRow = this.getRow();
			int thisCol = this.getCol();

			this.setPos(row, col);

			for( int i = 0; i < this.getBoard().length; i++ )
			{
				for( int j = 0; j < this.getBoard()[i].length; j++ )
				{
					ChessPiece test = this.getBoard()[i][j];

					if( test != null && test.isLegalMove(row, col) )
					{
						rVal = false;
						break;
					}
				}
			}

			this.setPos(thisRow, thisCol);
			if( dest != null )
			{
				dest.setPos(row, col);
			}
		} else
		{
			rVal = false;
		}

		return rVal;
	}

	public static void bufferImgs()
	{
		King.img1 = ChessUtil.loadPieceImg(ChessUtil.basePath + "King white.PNG", getC(1));
		King.img2 = ChessUtil.loadPieceImg(ChessUtil.basePath + "King black.PNG", getC(1));
		// For p2 Img Using p1 getC for capital letter on piece img if failed load
	}

	public BufferedImage getImg()
	{
		return getImg(this.player);
	}

	public static BufferedImage getImg(int player)
	{
		if( player == 1 )
		{
			return King.img1;
		}

		return King.img2;
	}

	public char getC()
	{
		return getC(this.player);
	}

	public static char getC(int player)
	{
		if( player == 1 )
			return 'K';
		else
			return 'k';
	}
}

class Queen extends ChessPiece
{
	private static BufferedImage img1;
	private static BufferedImage img2;

	public boolean isLegalMove(ChessPiece[][] board, int row, int col)
	{
		// make sure we are not trying to move to this same square
		if( ChessUtil.posValid(board, row, col) && board[row][col] != this )
		{
			int dRow = row - this.getRow();
			int dCol = col - this.getCol();
			// find slope of move; using 0 for div by 0 case for this prog
			double m = (dCol == 0 ? 0.0 : (double) dRow / dCol);

			// slope of 0 (or /0) = left right/up down; slope of +/-1 = diagonal aka valid position
			if( m == 0 || m * m == 1 )
			{
				Position targetPos = null;
				Position iPos = new Position(); // Amount to modify row and col to move towards target
				iPos.row = row - this.getRow(); // get the difference
				iPos.col = col - this.getCol();

				if( iPos.row != 0 )
					iPos.row /= Math.abs(iPos.row); // then div by number to convert diff to +/-1
				if( iPos.col != 0 )
					iPos.col /= Math.abs(iPos.col);

				targetPos = super.scanMove(new Position(row, col),
						new Position(this.getRow() + iPos.row, this.getCol() + iPos.col), iPos);

				if( targetPos != null )
				{
					ChessPiece targetPiece = board[targetPos.row][targetPos.col];

					if( targetPos.row == row && targetPos.col == col
							&& (targetPiece == null || (targetPiece != null && targetPiece.player != this.player)) )
					{
						return true;
					}
				}
			}
		}

		return false;
	}

	public static void bufferImgs()
	{
		Queen.img1 = ChessUtil.loadPieceImg(ChessUtil.basePath + "Queen white.PNG", getC(1));
		Queen.img2 = ChessUtil.loadPieceImg(ChessUtil.basePath + "Queen black.PNG", getC(1));
		// For p2 Img Using p1 getC for capital letter on piece img if failed load
	}

	public BufferedImage getImg()
	{
		return getImg(this.player);
	}

	public static BufferedImage getImg(int player)
	{
		if( player == 1 )
		{
			return Queen.img1;
		}

		return Queen.img2;
	}

	public char getC()
	{
		return getC(this.player);
	}

	public static char getC(int player)
	{
		if( player == 1 )
			return 'Q';
		else
			return 'q';
	}
}

public class ChessGame
{
	private static int windowHeight = 600;
	private static int windowWidth = 3 * windowHeight / 2; // width set = 3/2 height
	private static JPanel contentPane = null;
	private static int gameState = 0; // bit flag 0 = loading, 1 = player 1 turn, 2 = player 2 turn, 4 = end of game

	private static Position mSrcPos = null;
	private static ChessPiece mSrcPiece = null;
	private static MouseEvent mEvent = null;

	public static ChessPiece[][] board = new ChessPiece[8][8];
	private static ChessPiece[][] p1Cap = new ChessPiece[8][2];
	private static ChessPiece[][] p2Cap = new ChessPiece[8][2];

	private static int boardSqSize = 0; // I need this data for mouse clicks to convert to row/col
	private static int boardX = 0;
	private static int boardY = 0;

	public static BufferedImage vic1;
	public static BufferedImage vic2;

	private static void drawGame(Graphics g, int x, int y, int height, int width)
	{
		width = height * 3 / 2;
		int ofsetX = (width / 2) - (height / 2);
		drawBoard(g, x + ofsetX, 0, height);
		drawCaptureArea(g, x + ofsetX, 0, height);

		// saving/updating data for mouse events (used to convert
		boardX = x + ofsetX;
		boardY = y;// + any y offset if there is one in the future.
		boardSqSize = height / 8;
	}

	private static void loadingScreen(Graphics g, int x, int y, int height, int width)
	{
		g.setColor(Color.black);
		g.fillRect(x, y, width, height);

		Font f = new Font("Fixedsys", Font.PLAIN, 50);
		g.setFont(f);
		g.setColor(Color.white);
		g.drawString("Loading...", width * 2 / 5, height / 2);
	}

	private static void victoryScreen(Graphics g, int x, int y, int height, int width)
	{
		if( ChessGame.getWinningPlayer() == 1 )
		{
			g.drawImage(ChessGame.vic1, x, y, width, height, null);
		}
		if( ChessGame.getWinningPlayer() == 2 )
		{
			g.drawImage(ChessGame.vic2, x, y, width, height, null);
		}
	}

	private static void drawBoard(Graphics g, int x, int y, int size)
	{
		g.setColor(Color.blue);
		g.fillRect(x - x, y - y, size * 2, size * 2);
		int sqSize = size / 8;
		int j;
		for( int i = 0; i < 8; i++ )
		{
			if( i % 2 == 0 )
			{
				g.setColor(Color.white);
			} else
			{
				g.setColor(Color.black);
			}
			for( j = 0; j < 8; j++ )
			{
				int xCord = x + j * sqSize;
				int yCord = y + i * sqSize;
				g.fillRect(xCord, yCord, sqSize, sqSize);

				// border affects block
				Color origC = g.getColor();

				if( ChessGame.mSrcPiece != null && ChessGame.mSrcPiece.isLegalMove(i, j) )
				{
					drawSqBorder(g, xCord, yCord, sqSize, 5, Color.green);
				} else if( ChessGame.board[i][j] != null && ChessGame.board[i][j].player == ChessGame.gameState )
				{
					drawSqBorder(g, xCord, yCord, sqSize, 5, Color.gray);
				}

				if( ChessGame.board[i][j] != null && ChessGame.board[i][j] instanceof King
						&& ChessUtil.playerInCheck((King) ChessGame.board[i][j]) )
				{
					drawSqBorder(g, xCord, yCord, sqSize, 5, Color.red);
				}

				g.setColor(Color.black);
				g.drawRect(xCord, yCord, sqSize, sqSize);
				g.setColor(origC);
				// end border effects

				if( ChessGame.board[i][j] != null )
				{
					ChessGame.board[i][j].drawPiece(g, xCord, yCord + 5, sqSize);
				}
				if( g.getColor() == Color.black )
				{
					g.setColor(Color.white);
				} else
				{
					g.setColor(Color.black);
				}
			}
		}
	}

	private static void drawCaptureArea(Graphics g, int x, int y, int size)
	{
		int sqSize = (size / 8);
		int capturedSqSize = sqSize * 2 / 3;
		g.setColor(Color.black);
		g.fillRect(0, sqSize, capturedSqSize * 11 / 5, capturedSqSize * 8);
		g.setColor(Color.white);
		g.fillRect(size + sqSize * 3, sqSize, capturedSqSize * 11 / 5, capturedSqSize * 8);
		drawCapturedPieces(g, 0, sqSize, size * 1 / 6, ChessGame.p1Cap);
		drawCapturedPieces(g, size + sqSize * 3, sqSize, size * 1 / 6, ChessGame.p2Cap);
	}

	private static void drawCapturedPieces(Graphics g, int x, int y, int size, ChessPiece[][] capBoard)
	{
		int sqSize = (size / 2);
		for( int i = 0; i < capBoard.length; i++ )
		{
			for( int j = 0; j < capBoard[i].length; j++ )
			{
				if( capBoard[i][j] != null )
				{
					capBoard[i][j].drawPiece(g, j * sqSize + x, i * sqSize + y + 5, sqSize);
				}
			}
		}
	}

	private static void drawSqBorder(Graphics g, int x, int y, int size, int thickness, Color c)
	{
		Color oldC = g.getColor();
		g.setColor(c);
		g.fillRect(x, y, size, thickness);
		g.fillRect(x, y + size - thickness, size, thickness);
		g.fillRect(x, y + thickness, thickness, size - 2 * thickness);
		g.fillRect(x + size - thickness, y + thickness, thickness, size - 2 * thickness);
		g.setColor(oldC);
	}

	private static void drawMouseEvents(Graphics g)
	{
		if( mEvent != null && mSrcPos != null )
		{
			ChessPiece target = ChessGame.board[ChessGame.mSrcPos.row][ChessGame.mSrcPos.col];

			if( target != null )
			{
				target.drawPiece(g, mEvent.getX() - boardSqSize / 2, mEvent.getY() - boardSqSize / 2, boardSqSize);
			}
		}
	}

	private static Position convertXYtoRC(int x, int y, int xOffset, int yOffset, int maxRows, int maxCols, int sqSize)
	{
		Position rPos = new Position();

		rPos.row = (y - yOffset) / (sqSize);
		rPos.col = (x - xOffset) / (sqSize);

		if( x - xOffset < 0 || y - yOffset < 0 || rPos.row >= 8 || rPos.col >= 8 ) // not valid
		{
			rPos.row = -1;
			rPos.col = -1;
		}

		return rPos;
	}

	public static void setupGameBoards()
	{
		// @formatter:off
		String boardStr = "RHBKQBHR\n"
		                 +"PPPPPPPP\n"
		                 +"        \n"
		                 +"        \n"
		                 +"        \n"
		                 +"        \n"
		                 +"pppppppp\n"
		                 +"rhbkqbhr\n";
		// @formatter:on
		board = ChessUtil.genBoard(boardStr);

		ChessGame.p1Cap = new ChessPiece[8][2]; // throw existing into garbage collector and make new
		ChessGame.p2Cap = new ChessPiece[8][2]; // throw existing into garbage collector and make new
	}

	public static boolean gameEnded()
	{
		return (ChessGame.gameState & 4) != 0;
	}

	public static int getWinningPlayer()
	{
		return ChessGame.gameState & 3;
	}

	public static void main(String[] args)
	{
		JFrame window = new JFrame("Chess Game - Group 2");
		window.setLocationByPlatform(true);

		@SuppressWarnings("serial")
		final JPanel panel = new JPanel()
		{
			protected void paintComponent(Graphics gx)
			{
				Graphics2D g = (Graphics2D) gx;
				int width = getWidth();
				int height = getHeight();

				ChessGame.windowHeight = height;
				ChessGame.windowWidth = width;

				g.setBackground(Color.WHITE);
				g.clearRect(0, 0, width, height);
				g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
				g.setColor(Color.BLACK);

				if( (ChessGame.gameState & 4) != 0 )
				{
					victoryScreen(g, 0, 0, height, width);
				} else if( ChessGame.gameState != 0 )
				{
					drawGame(g, 0, 0, height, width);
					drawMouseEvents(g);
				} else
				{
					loadingScreen(g, 0, 0, height, width);
				}
			}
		};

		panel.addMouseListener(new MouseAdapter()
		{
			@Override
			public void mousePressed(MouseEvent e)
			{
				// this was left in as a way to force a refresh if it ever gets stuck for some reason
				if( ChessGame.gameState == 0 || (ChessGame.gameState & 4) != 0 )
				{
					ChessGame.gameState = 1;
					ChessGame.setupGameBoards();
				} else
				{
					if( mEvent == null )
					{
						ChessGame.mSrcPos = convertXYtoRC(e.getX(), e.getY(), ChessGame.boardX, ChessGame.boardY, 8, 8,
								ChessGame.boardSqSize);

						if( ChessGame.mSrcPos.row >= 0 && ChessGame.mSrcPos.col >= 0 )
						{
							ChessPiece target = ChessGame.board[ChessGame.mSrcPos.row][ChessGame.mSrcPos.col];

							if( ChessUtil.posValid(ChessGame.board, ChessGame.mSrcPos.row, ChessGame.mSrcPos.col)
									&& target != null && target.player == ChessGame.gameState )
							{
								ChessGame.mEvent = e;
								ChessGame.mSrcPiece = target;
							} else
							{
								ChessGame.mSrcPos = null;
								ChessGame.mSrcPiece = null;
								ChessGame.mEvent = null;
							}
						}
					} else
					{
						Position targetPos = convertXYtoRC(e.getX(), e.getY(), ChessGame.boardX, ChessGame.boardY, 8, 8,
								ChessGame.boardSqSize);
						Position srcPos = new Position(ChessGame.mSrcPiece.getPosition());
						int capi = -1;
						int capj = -1;
						boolean capture = false; // used to undo move if moving into check
						boolean castling = false; // cheap hack but it works, opps cant castle out of check
						                          // found that at 10:59 pm before due so... house rules?
						ChessPiece[][] capBoard = null;

						if( ChessGame.mSrcPos != null
								&& ChessUtil.posValid(ChessGame.board, ChessGame.mSrcPos.row, ChessGame.mSrcPos.col)
								&& ChessGame.mSrcPiece != null
								&& ChessGame.mSrcPiece.isLegalMove(targetPos.row, targetPos.col) )
						{
							// Handle capture
							if( ChessGame.board[targetPos.row][targetPos.col] != null )
							{
								capBoard = p1Cap;

								if( mSrcPiece.player == 2 )
								{
									capBoard = p2Cap;
								}

								loop: for( capi = 0; capi < capBoard.length; capi++ )
								{
									for( capj = 0; capj < capBoard[capi].length; capj++ )
									{
										if( capBoard[capi][capj] == null )
										{
											ChessGame.board[targetPos.row][targetPos.col].setPos(capBoard, capi, capj);
											capture = true;
											break loop;
										}
									}
								}
							}

							ChessGame.mSrcPiece.setPos(targetPos.row, targetPos.col);

							// handle castling (ie move rook over) and end of game/check logic from king
							if( ChessGame.mSrcPiece instanceof King )
							{
								if( !ChessGame.mSrcPiece.hasMoved )
								{
									if( targetPos.col == 1 )
									{
										ChessGame.board[ChessGame.mSrcPiece.getRow()][0]
												.setPos(ChessGame.mSrcPiece.getRow(), 2);
										castling = true;
									} else if( targetPos.col == 5 )
									{
										ChessGame.board[ChessGame.mSrcPiece.getRow()][7]
												.setPos(ChessGame.mSrcPiece.getRow(), 4);
										castling = true;
									}
								}
							}

							// cant make move, make error message? just ending for now
							if( ChessUtil.playerInCheck(ChessGame.mSrcPiece.player) )
							{
								// undo move
								ChessGame.mSrcPiece.setPos(srcPos.row, srcPos.col);
								if( castling )
								{
									if( targetPos.col == 1 )
									{
										ChessGame.board[ChessGame.mSrcPiece.getRow()][2]
												.setPos(ChessGame.mSrcPiece.getRow(), 0);
									} else if( targetPos.col == 5 )
									{
										ChessGame.board[ChessGame.mSrcPiece.getRow()][4]
												.setPos(ChessGame.mSrcPiece.getRow(), 7);
									}
								}
								if( capture )
								{
									capBoard[capi][capj].setPos(ChessGame.board, targetPos.row, targetPos.col);
								}

							} else // sucessful Move! yay
							{
								if( ChessGame.mSrcPiece.hasMoved == false )
								{
									ChessGame.mSrcPiece.hasMoved = true;
								}

								ChessGame.gameState ^= 3; // toggle player turn between 1 & 2

								// not most ideal place but easy
								// checkmate logic to call end of game and show vic screen.
								if( ChessUtil.playerInCheckMate(1) )
								{
									ChessGame.gameState = 6; // 2|4
								} else if( ChessUtil.playerInCheckMate(2) )
								{
									ChessGame.gameState = 5; // 1|4
								}
								// end checkmate logic
							}
						}

						ChessGame.mEvent = null;
						ChessGame.mSrcPos = null;
						ChessGame.mSrcPiece = null;
					}
				}

				ChessGame.contentPane.repaint();
			}
		});

		panel.addMouseMotionListener(new MouseAdapter()
		{
			@Override
			public void mouseMoved(MouseEvent e)
			{
				if( mEvent != null )
				{
					ChessGame.mEvent = e;
					ChessGame.contentPane.repaint();
				}
			}
		});

		contentPane = panel;

		window.setLayout(new BorderLayout());
		window.setSize(windowHeight * 3 / 2, windowHeight);

		window.add(panel, BorderLayout.CENTER);
		window.setVisible(true);
		window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		ChessGame.contentPane.repaint(); // draw loading screen since gameState = 0

		System.out.println("Main-> basePath: " + ChessUtil.basePath);

		Pawn.bufferImgs();
		Rook.bufferImgs();
		Knight.bufferImgs();
		Bishop.bufferImgs();
		King.bufferImgs();
		Queen.bufferImgs();

		ChessGame.vic1 = ChessUtil.loadVic(ChessUtil.basePath + "WhiteWon.png", windowWidth, windowHeight);
		ChessGame.vic2 = ChessUtil.loadVic(ChessUtil.basePath + "BlackWon.png", windowWidth, windowHeight);

		ChessGame.setupGameBoards();

		ChessGame.gameState = 1; // set to player 1 turn
		ChessGame.contentPane.repaint();
	}
}

class ChessUtil
{
	public final static String basePath = System.getProperty("user.dir") + "\\"
			+ ((System.getProperty("user.dir").toLowerCase().contains("eclipse")) ? "src\\" : "");

	public static King findKing(int player)
	{
		ChessPiece tKing = null;
		// find the king
		loop: for( int i = 0; i < ChessGame.board.length; i++ )
		{
			for( int j = 0; j < ChessGame.board[0].length; j++ )
			{
				tKing = ChessGame.board[i][j];

				if( tKing != null && tKing instanceof King && tKing.player == player )
				{
					break loop;
				}
			}
		}

		return (King) tKing;
	}

	public static boolean playerInCheck(int player)
	{
		return playerInCheck(findKing(player));
	}

	public static boolean playerInCheck(King tKing)
	{
		if( tKing != null )
		{
			for( int i = 0; i < ChessGame.board.length; i++ )
			{
				for( int j = 0; j < ChessGame.board[i].length; j++ )
				{
					ChessPiece test = ChessGame.board[i][j];

					if( test != null && test.isLegalMove(tKing.getRow(), tKing.getCol()) )
					{
						return true;
					}
				}
			}
		}

		return false;
	}

	public static boolean playerInCheckMate(int player)
	{
		return playerInCheckMate(findKing(player));
	}

	public static boolean playerInCheckMate(King tKing)
	{
		boolean canMove = false;

		if( tKing != null )
		{
			for( int i = -1; i < 2; i++ )
			{
				for( int j = -1; j < 2; j++ )
				{
					if( tKing.isLegalMove(tKing.getRow() + i, tKing.getCol() + j) )
					{
						canMove = true;
					}
				}
			}
		}

		return playerInCheck(tKing) && !canMove;
	}

	public static BufferedImage loadPieceImg(String fn, char c)
	{
		return loadPieceImg(fn, c, 500, 500);
	}

	// I should have made a helper function to start with but too late not changing that
	public static BufferedImage loadVic(String fn, int init_width, int init_height)
	{
		BufferedImage rImg = null;

		try
		{
			rImg = ImageIO.read(new File(fn));
		} catch (Exception e)
		{
			rImg = new BufferedImage(init_width, init_height, BufferedImage.TYPE_INT_ARGB);
			Font f = new Font("Fixedsys", Font.PLAIN, init_height / 8);
			Graphics2D g2d = rImg.createGraphics();
			int winningPlayer = 0;

			System.out.println("Error loading IMG: " + fn);
			g2d.setColor(Color.black);
			g2d.fillRect(0, 0, init_width, init_height);

			if( fn.toLowerCase().contains("white") )
			{
				winningPlayer = 1;
			} else if( fn.toLowerCase().contains("black") )
			{
				winningPlayer = 2;
			}

			g2d.setFont(f);
			g2d.setColor(Color.white);
			g2d.drawString("Player " + winningPlayer + " Wins!", init_width * 2 / 7, init_height / 2);

			g2d.dispose();
		}

		return rImg;
	}

	public static BufferedImage loadPieceImg(String fn, char c, int init_width, int init_height)
	{
		// https://examples.javacodegeeks.com/desktop-java/imageio/create-image-file-from-graphics-object/
		// & https://docs.oracle.com/javase/7/docs/api/java/awt/image/BufferedImage.html
		// & https://docs.oracle.com/javase/7/docs/api/java/awt/Font.html
		// & https://docs.oracle.com/javase/7/docs/api/java/awt/image/BufferedImage.html
		// & https://docs.oracle.com/javase/7/docs/api/java/awt/class-use/FontMetrics.html

		BufferedImage rImg = null;

		try
		{
			rImg = ImageIO.read(new File(fn));
		} catch (Exception e)
		{
			double sf1 = 0.8; // large circle scale factor of init
			double sf2 = 0.8; // small circle scale factor of (sf1*init)
			Color c1 = Color.red;
			Color c2 = Color.black;
			int ovH = (int) (init_height * sf1); // size down due to shift down in piece draw
			int ovW = (int) (init_width * sf1); // size down due to shift
			rImg = new BufferedImage(init_width, init_height, BufferedImage.TYPE_INT_ARGB);
			Font f = new Font("Fixedsys", Font.PLAIN, init_height / 2);
			Graphics2D g2d = rImg.createGraphics();
			FontMetrics fm = g2d.getFontMetrics(f);

			System.out.println("Error loading IMG: " + fn);

			if( fn.toLowerCase().contains("white") )
			{
				c1 = Color.white;
			} else if( fn.toLowerCase().contains("black") )
			{
				c1 = Color.black;
				c2 = Color.white;
			}

			g2d.setColor(Color.gray);
			g2d.fillOval((int) (init_width / 2 - ovW / 2), 0, ovW, ovH);
			g2d.setColor(c1);
			g2d.fillOval((int) (init_width / 2 - ovW * sf2 / 2), (int) (ovH / 2 - ovH * sf2 / 2), (int) (ovW * sf2),
					(int) (ovH * sf2));

			g2d.setFont(f);
			g2d.setColor(c2);
			g2d.drawString(c + "", (int) (init_width / 2 - fm.stringWidth(c + "") / 2),
					(int) (init_width / 2 + fm.getHeight() / 6));

			g2d.dispose();
		}

		return rImg;
	}

	public static String getBoardDispStr(ChessPiece[] boardData)
	{
		ChessPiece[][] convBoard = { boardData };
		return getBoardDispStr(convBoard);
	}

	public static boolean posValid(ChessPiece[][] board, int row, int col)
	{
		return (board != null && 0 <= row && row < board.length && 0 <= col && col < board[row].length);
	}

	public static String getBoardDispStr(ChessPiece[][] boardData)
	{
		String boardDisp = "";

		for( int row = 0; row < boardData.length; row++ )
		{
			for( int col = 0; col < boardData[row].length; col++ )
			{
				if( boardData[row][col] != null )
				{
					boardDisp += boardData[row][col].getC();
				} else
				{
					boardDisp += "_";
				}

				// boardDisp += " "; // added extra space to make text output look square
				// not good for saving/reading data tho
			}

			boardDisp += '\n';
		}

		return boardDisp;
	}

	public static ChessPiece[][] genBoard()
	{
		return genBoard("");
	}

	public static ChessPiece[][] genBoard(String strBoard)
	{
		ChessPiece[][] newBoard = new ChessPiece[8][8];

		if( !strBoard.isEmpty() )
		{
			for( int i = 0, row = 0, col = 0; i < strBoard.length() && row < 8; i++ )
			{
				ChessPiece newPiece = null;
				char nextC = strBoard.charAt(i);
				int player = 1;

				if( nextC == '\n' )
				{
					col = 0;
					row++;
					continue;
				}

				if( col > 7 )
					continue;

				if( nextC > 96 )
				{
					player = 2;
					nextC -= 32;
				}

				switch (nextC)
				{
				case 'P':
					newPiece = new Pawn();
					break;
				case 'R':
					newPiece = new Rook();
					break;
				case 'H':
					newPiece = new Knight();
					break;
				case 'B':
					newPiece = new Bishop();
					break;
				case 'Q':
					newPiece = new Queen();
					break;
				case 'K':
					newPiece = new King();
					break;
				default:
					newPiece = null;
					break; // technically not needed
				}
				;

				if( newPiece != null )
				{
					newPiece.player = player;
					newPiece.setPos(newBoard, row, col);
					newPiece.hasMoved = false;
				}

				col++;
			}
		} else // make a random board
		{
			// for each player[1/2]: King(0), Queen(1), Bishop(2), Knight/Horse(3), Rook(4), Pawn(5)
			int[][] pieceCount = { { 1, 1, 2, 2, 2, 8 }, { 1, 1, 2, 2, 2, 8 } };

			for( int row = 0; row < newBoard.length; row++ )
			{
				for( int col = 0; col < newBoard[row].length; col++ )
				{
					int rand = (int) (Math.random() * 100);
					int player = 0;
					int piece = 0;

					if( rand < 3 && pieceCount[0][0] + pieceCount[1][0] > 0 )
					{
						newBoard[row][col] = new King();

						if( pieceCount[0][0] > 0 )
						{
							player = 0;
							piece = 0;
						} else
						{
							player = 1;
							piece = 0;
						}
					} else if( rand < 6 && pieceCount[0][1] + pieceCount[1][1] > 0 )
					{
						newBoard[row][col] = new Queen();

						if( pieceCount[0][1] > 0 )
						{
							player = 0;
							piece = 1;
						} else
						{
							player = 1;
							piece = 1;
						}
					} else if( rand < 12 && pieceCount[0][2] + pieceCount[1][2] > 0 )
					{
						newBoard[row][col] = new Bishop();

						if( pieceCount[0][2] > 0 )
						{
							player = 0;
							piece = 2;
						} else
						{
							player = 1;
							piece = 2;
						}
					} else if( rand < 18 && pieceCount[0][3] + pieceCount[1][3] > 0 )
					{
						newBoard[row][col] = new Knight();

						if( pieceCount[0][3] > 0 )
						{
							player = 0;
							piece = 3;
						} else
						{
							player = 1;
							piece = 3;
						}
					} else if( rand < 24 && pieceCount[0][4] + pieceCount[1][4] > 0 )
					{
						newBoard[row][col] = new Rook();

						if( pieceCount[0][4] > 0 )
						{
							player = 0;
							piece = 4;
						} else
						{
							player = 1;
							piece = 4;
						}
					} else if( rand < 50 && pieceCount[0][5] + pieceCount[1][5] > 0 )
					{
						newBoard[row][col] = new Pawn();

						if( pieceCount[0][5] > 0 )
						{
							player = 0;
							piece = 5;
						} else
						{
							player = 1;
							piece = 5;
						}
					}

					pieceCount[player][piece]--;
					if( newBoard[row][col] != null )
					{
						newBoard[row][col].player = player;
						newBoard[row][col].setPos(newBoard, row, col); // used to update piece variables
						newBoard[row][col].hasMoved = false;
					}
				}
			}
		}

		return newBoard;
	}
}
// @formatter:on