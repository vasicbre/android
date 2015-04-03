package rs.ac.bg.etf.vn110012d;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import android.view.View;

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

	// cell unavailable because roll isn't made yet
	public static final int RUNAVAILABLE = -2;
	// cell unavailable because of ordering, move or else...
	public static final int UNAVAILABLE = 0;
	public static final int AVAILABLE = 1;

	public static final int EMPTY = -1;

	public static final int BOARD_HEIGHT = 13;
	public static final int BOARD_WIDTH = 6;

	public static final int NUMBERS = 0; // part of matrix with regular numbers
	public static final int EXTREMES = 6; // part of matrix with extreme values
											// (min, max)
	public static final int SPECIALS = 8; // part of matrix with special values
											// (poker, triling, yamb...)

	public static final int TRILING_BONUS = 20;
	public static final int FULL_BONUS = 30;
	public static final int POKER_BONUS = 40;
	public static final int YAMB_BONUS = 50;

	public static final int FIRST_ROLL_STRAIGHT = 66;
	public static final int SECOND_ROLL_STRAIGHT = 56;
	public static final int THIRD_ROLL_STRAIGHT = 46;

	private int[][] board = new int[BOARD_HEIGHT][BOARD_WIDTH];
	private int[][] availabilityMatrix = new int[BOARD_HEIGHT][BOARD_WIDTH];

	// sums
	private int[] numberSum = new int[6];
	private int[] extremeSum = new int[6];
	private int[] specialSum = new int[6];

	private int move, roll, playerId;

	private Player.Callback cb;

	boolean callLocked = false;

	String name;

	public Player(Player.Callback cb, int playerId, String name) {
		this.cb = cb;
		this.playerId = playerId;
		this.name = name;

		initBoard();
		initSums();
		initAvailability();

		// lock all cells before first roll
		rollLock();
	}

	public String getName() {
		return name;
	}

	private void initBoard() {
		for (int i = 0; i < BOARD_HEIGHT * BOARD_WIDTH; i++)
			board[i / BOARD_WIDTH][i % BOARD_WIDTH] = EMPTY;
	}

	private void initSums() {
		for (int i = 0; i < BOARD_WIDTH; i++) {
			numberSum[i] = EMPTY;
			extremeSum[i] = EMPTY;
			specialSum[i] = EMPTY;
		}
	}

	public void incRoll() {
		roll++;

		// unlock available cells after first roll
		if (roll == 1) {
			rollUnlock();
			cb.refreshView();
		} else if (roll == 2) {
			handLock();
			if (!callLocked)
				callLock();
			cb.refreshView();
		}
	}

	public void resetRoll() {
		roll = 0;
		rollLock();
		cb.refreshView();
	}

	public int getRoll() {
		return roll;
	}

	public int getMove() {
		return move;
	}

	public void incMove() {
		move++;
		resetRoll();
	}

	public int getId() {
		return playerId;
	}

	public interface Callback {
		void refreshView();

		void enterValue(View view, int position, int parentId);
	}

	public boolean isCall(int id, int col) {
		return col == CALL;
	}

	public void call(View view, int position, int parentId) {
		int base = getRowBase(parentId);
		if (callLocked) {
			cb.enterValue(view, position, parentId);
			callLocked = false;
		} else {
			callLocked = true;
			for (int i = 0; i < BOARD_HEIGHT * BOARD_WIDTH; i++) {
				int row = i / BOARD_WIDTH;
				int col = i % BOARD_WIDTH;
				if (availabilityMatrix[row][col] == AVAILABLE)
					availabilityMatrix[row][col] = RUNAVAILABLE;
			}
			availabilityMatrix[base + position / BOARD_WIDTH][CALL] = AVAILABLE;
			cb.refreshView();
		}
	}

	public boolean isCallMade() {
		return callLocked;
	}

	private void initAvailability() {
		availabilityMatrix[0][DOWN] = AVAILABLE;

		for (int i = 0; i < BOARD_HEIGHT; i++) {
			availabilityMatrix[i][FREE] = AVAILABLE;
			availabilityMatrix[i][HAND] = AVAILABLE;
			availabilityMatrix[i][CALL] = AVAILABLE;
		}

		availabilityMatrix[MAX][MIDDLE] = AVAILABLE;
		availabilityMatrix[MIN][MIDDLE] = AVAILABLE;

		availabilityMatrix[YAMB][UP] = AVAILABLE;
	}

	// locks all cells before roll
	private void rollLock() {
		for (int i = 0; i < BOARD_HEIGHT * BOARD_WIDTH; i++) {
			int row = i / BOARD_WIDTH;
			int col = i % BOARD_WIDTH;
			if (availabilityMatrix[row][col] == AVAILABLE)
				availabilityMatrix[row][col] = RUNAVAILABLE;
		}
	}

	// unlocks available cells in all columns
	private void rollUnlock() {
		for (int i = 0; i < BOARD_HEIGHT * BOARD_WIDTH; i++) {
			int row = i / BOARD_WIDTH;
			int col = i % BOARD_WIDTH;
			if (availabilityMatrix[row][col] == RUNAVAILABLE)
				availabilityMatrix[row][col] = AVAILABLE;
		}
	}

	// lock available cells in hand column
	private void handLock() {
		for (int i = 0; i < BOARD_HEIGHT * BOARD_WIDTH; i++) {
			int row = i / BOARD_WIDTH;
			int col = i % BOARD_WIDTH;
			if (col == HAND && availabilityMatrix[row][col] == AVAILABLE)
				availabilityMatrix[row][col] = RUNAVAILABLE;
		}
	}

	// locks available cells in call column
	private void callLock() {
		for (int i = 0; i < BOARD_HEIGHT * BOARD_WIDTH; i++) {
			int row = i / BOARD_WIDTH;
			int col = i % BOARD_WIDTH;
			if (col == CALL && availabilityMatrix[row][col] == AVAILABLE)
				availabilityMatrix[row][col] = RUNAVAILABLE;
		}
	}

	private int getRowBase(long id) {
		if (id == R.id.num_board_grid)
			return NUMBERS;
		else if (id == R.id.min_max_grid)
			return EXTREMES;
		else
			return SPECIALS;
	}

	// check is cell available for entry
	public boolean isAvailable(int id, int row, int col) {
		int base = getRowBase(id);
		return availabilityMatrix[base + row][col] == AVAILABLE;
	}

	// set value on clicked cell
	public void set(int id, int row, int col, int value) {
		int base = getRowBase(id);
		availabilityMatrix[base + row][col] = UNAVAILABLE;

		setNextAvailable(base + row, col);

		board[base + row][col] = value;

		switch (base) {
		case NUMBERS:

			if (numberSum[col] == EMPTY)
				numberSum[col] = 0;

			numberSum[col] += value;
			// fall through to update extreme values if ones are entered
			if (row != 0)
				break;
		case EXTREMES:
			// required for calculating sum are fields: ones, max, min
			if (board[0][col] != EMPTY && board[MAX][col] != EMPTY
					&& board[MIN][col] != EMPTY)
				extremeSum[col] = (board[MAX][col] - board[MIN][col])
						* board[0][col];
			break;
		case SPECIALS:

			if (specialSum[col] == EMPTY)
				specialSum[col] = 0;

			specialSum[col] += value;
			break;
		}

		cb.refreshView();

	}

	public int getTotalScore() {
		int totalScore = 0;
		for (int i = 0; i < BOARD_WIDTH; i++) {
			totalScore += (numberSum[i] != EMPTY ? numberSum[i] : 0)
					+ (extremeSum[i] != EMPTY ? extremeSum[i] : 0)
					+ (specialSum[i] != EMPTY ? specialSum[i] : 0);
		}

		return totalScore;
	}

	// set next cell available for entry in case of ordered columns
	private void setNextAvailable(int row, int col) {
		if (col == DOWN)
			if (row != BOARD_HEIGHT - 1)
				availabilityMatrix[row + 1][col] = AVAILABLE;

		if (col == UP)
			if (row != 0)
				availabilityMatrix[row - 1][col] = AVAILABLE;

		if (col == MIDDLE) {
			// if clicked to last available in up direction, free upper cell
			if (row != 0 && !availableUp(row, col))
				availabilityMatrix[row - 1][col] = AVAILABLE;
			else if (row != BOARD_HEIGHT - 1 && availableUp(row, col))
				availabilityMatrix[row + 1][col] = AVAILABLE;
		}
	}

	// check if there is available cell from the row position, all the way up
	private boolean availableUp(int row, int col) {
		int rowNum = row - 1;
		while (rowNum >= 0)
			if (availabilityMatrix[rowNum--][col] == AVAILABLE)
				return true;

		return false;
	}

	private int calculateExtremeValue(int row, int[] diceValues) {
		int value = 0;

		// sorts in ascending order
		Arrays.sort(diceValues);

		// if calculating max, reverse array
		if (row == MAX)
			for (int i = 0; i < diceValues.length / 2; i++)
				swap(diceValues, i, diceValues.length - i - 1);

		for (int i = 0; i < 5; i++)
			value += diceValues[i];

		return value;
	}

	private int calculateRegularValue(int row, int[] diceValues) {
		int value = 0;
		for (int i = 0; i < diceValues.length; i++)
			value += (diceValues[i] == row + 1) ? row + 1 : 0;

		return value;
	}

	// returns highest 3-times-repeating character
	private int repeating(int[] diceValues, int requiredCnt) {
		int prev = 0;
		int repCnt = 1;
		int repValue = 0;
		for (int i = 1; i < diceValues.length; i++) {
			if (diceValues[i] == diceValues[prev]) {
				repCnt++;
				if (repCnt == requiredCnt)
					repValue = diceValues[i];
			} else
				prev = i;
		}

		return repValue;
	}

	private boolean straightCheck(int[] diceValues) {
		Set<Integer> set = new HashSet<Integer>();
		for (int i = 0; i < diceValues.length; i++) {
			set.add(diceValues[i]);
		}

		return set.size() >= 5;
	}

	// returns double and triple repeating sequences
	private void findRepeatingSequences(int[] diceValues,
			List<Integer> doubleRep, List<Integer> tripleRep) {
		int prev = 0;
		int repCnt = 1;
		for (int i = 1; i < diceValues.length; i++) {
			if (diceValues[i] == diceValues[prev]) {
				repCnt++;
				if (repCnt == 2)
					doubleRep.add(diceValues[i]);
				if (repCnt == 3)
					tripleRep.add(diceValues[i]);
			} else {
				prev = i;
				repCnt = 1;
			}
		}
	}

	// calculates value of full hand
	private int fullValue(int[] diceValues) {
		List<Integer> doubleRep = new ArrayList<Integer>();
		List<Integer> tripleRep = new ArrayList<Integer>();
		findRepeatingSequences(diceValues, doubleRep, tripleRep);

		int tripleValue = 0;
		int doubleValue = 0;

		if (!tripleRep.isEmpty()) {
			tripleValue = Collections.max(tripleRep);
			// remove triple repeating value from double repeating list
			doubleRep.remove(Integer.valueOf(tripleValue));

			if (!doubleRep.isEmpty()) {
				doubleValue = Collections.max(doubleRep);
				return doubleValue * 2 + tripleValue * 3;
			} else {
				return 0;
			}
		} else
			return 0;
	}

	private int calculateSpecialValue(int row, int[] diceValues) {
		int value = 0;

		Arrays.sort(diceValues);

		switch (row) {
		case TRILING: {
			value = repeating(diceValues, 3) * 3;
			return value > 0 ? value + TRILING_BONUS : 0;
		}
		case STRAIGHT: {
			if (straightCheck(diceValues))
				return roll == 1 ? 66 : (roll == 2 ? 56 : 46);
			else
				return 0;
		}
		case FULL: {
			value = fullValue(diceValues);
			return value > 0 ? value + FULL_BONUS : 0;
		}
		case POKER: {
			value = repeating(diceValues, 4) * 4;
			return value > 0 ? value + POKER_BONUS : 0;
		}
		case YAMB: {
			value = repeating(diceValues, 5) * 5;
			return value > 0 ? value + YAMB_BONUS : 0;
		}
		}
		return value;
	}

	// calculate value to be entered in the cell
	public int calculateValue(int id, int row, int col, int[] diceValues) {
		int base = getRowBase(id);
		row += base;

		switch (base) {
		case NUMBERS: {
			return calculateRegularValue(row, diceValues);
		}
		case EXTREMES: {
			return calculateExtremeValue(row, diceValues);
		}
		case SPECIALS: {
			return calculateSpecialValue(row, diceValues);
		}
		default:
			return 0;
		}

	}

	public int getSumValue(int id, int col) {
		int base = getRowBase(id);
		switch (id) {
		case R.id.num_sum:
			return numberSum[col];
		case R.id.min_max_sum:
			return extremeSum[col];
		case R.id.spec_sum:
			return specialSum[col];
		default:
			return 0;
		}
	}

	public int getValue(int id, int row, int col) {
		int base = getRowBase(id);
		return board[base + row][col];
	}

	private void swap(int[] array, int pos1, int pos2) {
		int temp = array[pos1];
		array[pos1] = array[pos2];
		array[pos2] = temp;
	}
}
