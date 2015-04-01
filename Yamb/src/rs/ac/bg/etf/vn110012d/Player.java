package rs.ac.bg.etf.vn110012d;

public class Player {

	public static final int DOWN = 0;
	public static final int FREE = 1;
	public static final int UP = 2;
	public static final int MIDDLE = 3;
	public static final int HAND = 4;
	public static final int CALL = 5;

	public static final int MAX = 6;
	public static final int MIN = 7;
	public static final int TRILING = 8;
	public static final int STRAIGHT = 9;
	public static final int FULL = 10;
	public static final int POKER = 11;
	public static final int YAMB = 12;	

	public static final int UNAVAILABLE = 0;
	public static final int AVAILABLE = 1;
	
	public static final int BOARD_HEIGHT = 13;
	public static final int BOARD_WIDTH = 6;

	int[][] board = new int[BOARD_HEIGHT][BOARD_WIDTH];
	int[][] availabilityMatrix = new int[BOARD_HEIGHT][BOARD_WIDTH];

	// sums
	int[] numSum = new int[6];
	int[] minMaxSum = new int[6];
	int[] specSum = new int[6];

	public Player() {
		initAvailability();
	}

	private void initAvailability() {
		availabilityMatrix[0][DOWN] = AVAILABLE;

		for (int i = 0; i < 13; i++) {
			availabilityMatrix[i][FREE] = AVAILABLE;
			availabilityMatrix[i][HAND] = AVAILABLE;
			availabilityMatrix[i][CALL] = AVAILABLE;
		}

		availabilityMatrix[MAX][MIDDLE] = AVAILABLE;
		availabilityMatrix[MIN][MIDDLE] = AVAILABLE;

		availabilityMatrix[YAMB][UP] = AVAILABLE;
	}
	private int getRowBase(long id) {
		if (id == R.id.num_board_grid)
			return 0;
		else if (id == R.id.min_max_grid)
			return 6;
		else
			return 8;
	}

	// check is cell available for entry
	public boolean isAvailable(long id, int row, int col) {
		int base = getRowBase(id);
		return availabilityMatrix[base + row][col] == AVAILABLE;
	}

	// set value on clicked cell
	public void set(int id, int row, int col, int value) {
		int base = getRowBase(id);
		availabilityMatrix[base + row][col] = UNAVAILABLE;
		
		setNextAvailable(base + row, col);
		
		board[base + row][col] = value;
	}
	
	// set next cell available for entry in case of ordered columns 
	private void setNextAvailable(int row, int col) { 
		if(col == DOWN)
			if(row != BOARD_HEIGHT - 1)
				availabilityMatrix[row + 1][col] = AVAILABLE;
		
		if(col == UP)
			if(row != 0)
				availabilityMatrix[row - 1][col] = AVAILABLE;
		
		if(col == MIDDLE) {
			// if clicked to last available in up direction, free upper cell
			if(row != 0 && !availableUp(row, col))
				availabilityMatrix[row - 1][col] = AVAILABLE;
			else if(row != BOARD_HEIGHT - 1 && availableUp(row, col))
				availabilityMatrix[row + 1][col] = AVAILABLE;
		}
	}
	
	private boolean availableUp(int row, int col) {
		int rowNum = row - 1;
		while(rowNum >=0)
			if(availabilityMatrix[rowNum--][col] == AVAILABLE)
				return true;
		
		return false;
	}
}
