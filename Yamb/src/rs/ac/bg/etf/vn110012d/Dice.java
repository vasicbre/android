package rs.ac.bg.etf.vn110012d;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import android.view.View;
import android.widget.ImageView;

public class Dice {

	int[] diceValues;
	boolean[] selectedDice;
	boolean[] lockedDice;

	GameplayActivity activity;

	public Dice(GameplayActivity activity) {
		this.activity = activity;

		diceValues = new int[6];
		selectedDice = new boolean[6];
		lockedDice = new boolean[6];

		init();
		setListeners();
	}

	private void init() {
		for (int i = 0; i < 6; i++) {
			diceValues[i] = i + 1;
			selectedDice[i] = false;
			lockedDice[i] = false;
		}
	}

	private void setListeners() {
		ImageView[] dice = new ImageView[6];
		for (int i = 0; i < 6; i++) {
			dice[i] = (ImageView) activity.findViewById(diceSlotId(i));
			dice[i].setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					selectDice(v);
				}
			});
		}
	}

	// calculate entered value in upper part of the board
	public int calculateRegularValue(int row) {
		int value = 0;
		for (int i = 0; i < diceValues.length; i++)
			value += (diceValues[i] == row + 1) ? row + 1 : 0;

		return value;
	}

	// calculate entered value in middle part of the board
	public int calculateExtremeValue(int row) {
		int value = 0;

		// sorts in ascending order
		Arrays.sort(diceValues);

		// if calculating max, reverse array
		if (row == Player.MAX)
			for (int i = 0; i < diceValues.length / 2; i++)
				swap(diceValues, i, diceValues.length - i - 1);

		for (int i = 0; i < 5; i++)
			value += diceValues[i];

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

	// calculate entered value in lower part of the board
	public int calculateSpecialValue(int row, int roll) {
		int value = 0;

		Arrays.sort(diceValues);

		switch (row) {
		case Player.TRILING: {
			value = repeating(diceValues, 3) * 3;
			return value > 0 ? value + Player.TRILING_BONUS : 0;
		}
		case Player.STRAIGHT: {
			if (straightCheck(diceValues))
				return roll == 1 ? 66 : (roll == 2 ? 56 : 46);
			else
				return 0;
		}
		case Player.FULL: {
			value = fullValue(diceValues);
			return value > 0 ? value + Player.FULL_BONUS : 0;
		}
		case Player.POKER: {
			value = repeating(diceValues, 4) * 4;
			return value > 0 ? value + Player.POKER_BONUS : 0;
		}
		case Player.YAMB: {
			value = repeating(diceValues, 5) * 5;
			return value > 0 ? value + Player.YAMB_BONUS : 0;
		}
		}
		return value;
	}

	private void selectDice(View v) {
		switch (v.getId()) {
		case R.id.die1:
			selectDie((ImageView) v, 0);
			break;
		case R.id.die2:
			selectDie((ImageView) v, 1);
			break;
		case R.id.die3:
			selectDie((ImageView) v, 2);
			break;
		case R.id.die4:
			selectDie((ImageView) v, 3);
			break;
		case R.id.die5:
			selectDie((ImageView) v, 4);
			break;
		case R.id.die6:
			selectDie((ImageView) v, 5);
			break;
		}

	}

	public void selectDie(ImageView iv, int ord) {
		// change selection is available only for dice that are not selected in
		// previous moves after the first move
		if (activity.getCurrentPlayer().getRoll() > 0 && !lockedDice[ord]) {
			selectedDice[ord] = !selectedDice[ord];
			iv.setImageResource(diceId(diceValues[ord] - 1, selectedDice[ord]));
		}
	}

	public void reset() {
		for (int i = 0; i < 6; i++) {
			selectedDice[i] = false;
			lockedDice[i] = false;
			ImageView iv = (ImageView) activity.findViewById(diceSlotId(i));
			iv.setImageResource(diceId(diceValues[i] - 1, selectedDice[i]));
		}
	}

	// roll unselected dice
	public void roll() {
		for (int i = 0; i < 6; i++) {
			if (!selectedDice[i]) {
				diceValues[i] = (int) (Math.random() * 6) + 1;
				ImageView iv = (ImageView) activity.findViewById(diceSlotId(i));
				iv.setImageResource(diceId(diceValues[i] - 1, selectedDice[i]));
			} else {
				// if die is selected, lock it so it's selection cannot be
				// changed in the next move
				lockedDice[i] = true;
			}
		}
	}

	private int diceSlotId(int ord) {
		switch (ord) {
		case 0:
			return R.id.die1;
		case 1:
			return R.id.die2;
		case 2:
			return R.id.die3;
		case 3:
			return R.id.die4;
		case 4:
			return R.id.die5;
		case 5:
			return R.id.die6;
		default:
			return -1;
		}
	}

	private int diceId(int ord, boolean selected) {
		switch (ord) {
		case 0:
			if (selected)
				return R.drawable.one_die_red;
			else
				return R.drawable.one_die;
		case 1:
			if (selected)
				return R.drawable.two_die_red;
			else
				return R.drawable.two_die;
		case 2:
			if (selected)
				return R.drawable.three_die_red;
			else
				return R.drawable.three_die;
		case 3:
			if (selected)
				return R.drawable.four_die_red;
			else
				return R.drawable.four_die;
		case 4:
			if (selected)
				return R.drawable.five_die_red;
			else
				return R.drawable.five_die;
		case 5:
			if (selected)
				return R.drawable.six_die_red;
			else
				return R.drawable.six_die;
		default:
			return -1;
		}
	}

	private void swap(int[] array, int pos1, int pos2) {
		int temp = array[pos1];
		array[pos1] = array[pos2];
		array[pos2] = temp;
	}
}
