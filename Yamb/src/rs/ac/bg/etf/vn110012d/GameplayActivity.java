package rs.ac.bg.etf.vn110012d;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;

public class GameplayActivity extends Activity implements Shaker.Callback,
		Player.Callback, Runnable {

	int[] diceValues;
	boolean[] selectedDice;
	boolean[] lockedDice;

	private static final int SHAKE_THRESHOLD = 400;
	private static final int END_SHAKE_TIME_GAP = 600;

	private MediaPlayer mp;
	Shaker shaker;
	Player currentPlayer;

	Player[] players;
	int playerCnt;

	TextView tvMove, tvRoll, tvPlayer;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.game_layout);

		playerCnt = getIntent().getExtras().getInt("NUMBER_OF_PLAYERS");
		players = new Player[playerCnt];

		for (int i = 0; i < playerCnt; i++) {
			players[i] = new Player(this, i, getIntent().getExtras().getString("PLAYER_" + i));
		}

		tvMove = (TextView) findViewById(R.id.move_id);
		tvRoll = (TextView) findViewById(R.id.roll_id);
		tvPlayer = (TextView) findViewById(R.id.player_id);

		currentPlayer = players[0];

		populateBoard();
		loadDice();
		updateInfo();

		shaker = new Shaker(this, SHAKE_THRESHOLD, END_SHAKE_TIME_GAP, this);
		shaker.register();
	}

	protected void onPause() {
		super.onPause();
		shaker.unregister();
	}

	protected void onResume() {
		super.onResume();
		shaker.register();
	}
	
	private void populateBoard() {
		populateInputCells(R.id.num_board_grid, 36);
		populateInputCells(R.id.min_max_grid, 12);
		populateInputCells(R.id.spec_grid, 30);
		populateScoreCells(R.id.num_sum);
		populateScoreCells(R.id.min_max_sum);
		populateScoreCells(R.id.spec_sum);
	}

	private void loadDice() {
		ImageView[] dice = new ImageView[6];
		diceValues = new int[6];
		selectedDice = new boolean[6];
		lockedDice = new boolean[6];

		// init dice
		for (int i = 0; i < 6; i++) {
			diceValues[i] = i + 1;
			selectedDice[i] = false;
			lockedDice[i] = false;
		}

		for (int i = 0; i < 6; i++) {
			dice[i] = (ImageView) findViewById(diceSlotId(i));
			dice[i].setOnClickListener(new View.OnClickListener() {

				@Override
				public void onClick(View v) {
					switch (v.getId()) {
					case R.id.die1:
						selectDice((ImageView) v, 0);
						break;
					case R.id.die2:
						selectDice((ImageView) v, 1);
						break;
					case R.id.die3:
						selectDice((ImageView) v, 2);
						break;
					case R.id.die4:
						selectDice((ImageView) v, 3);
						break;
					case R.id.die5:
						selectDice((ImageView) v, 4);
						break;
					case R.id.die6:
						selectDice((ImageView) v, 5);
						break;
					}

				}
			});
		}
	}

	private void selectDice(ImageView iv, int ord) {
		// change selection is available only for dice that are not selected in
		// previous moves after the first move
		if (currentPlayer.getRoll() > 0 && !lockedDice[ord]) {
			selectedDice[ord] = !selectedDice[ord];
			iv.setImageResource(diceId(diceValues[ord] - 1, selectedDice[ord]));
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

	boolean endOfMove = false;

	private void populateInputCells(int id, int cnt) {
		GridView num_grid = (GridView) findViewById(id);

		List<String> strs = new ArrayList<String>();
		for (int i = 0; i < cnt; i++)
			strs.add("");

		ArrayAdapter<String> adapter = new InputCellAdapter(strs, this,
				currentPlayer);

		num_grid.setAdapter(adapter);

		num_grid.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {

				if (currentPlayer.isAvailable(parent.getId(), position / 6,
						position % 6)) {
					if (currentPlayer.isCall(parent.getId(), position % 6)) {
						if(currentPlayer.isCallMade()) {
							parent.postDelayed(GameplayActivity.this, 2000);
						}
						currentPlayer.call(view, position, parent.getId());
					} else {
						enterValue(view, position, parent.getId());
						parent.postDelayed(GameplayActivity.this, 2000);
					}
				}

			}
		});
	}

	public void enterValue(View view, int position, int parentId) {
		TextView tv = (TextView) view.findViewById(R.id.num);
		tv.setBackgroundResource(R.color.invalid_blue);
		int value = currentPlayer.calculateValue(parentId, position / 6,
				position % 6, Arrays.copyOf(diceValues, diceValues.length));
		currentPlayer.set(parentId, position / 6, position % 6, value);
		tv.setText("" + value);
		refreshView();
	}

	// prepare dice for next move, called when value is entered
	private void nextMove() {

		currentPlayer.incMove();
		updateInfo();

		int nextId = (currentPlayer.getId() + 1) % playerCnt;
		currentPlayer = players[nextId];

		for (int i = 0; i < 6; i++) {
			selectedDice[i] = false;
			lockedDice[i] = false;
			ImageView iv = (ImageView) findViewById(diceSlotId(i));
			iv.setImageResource(diceId(diceValues[i] - 1, selectedDice[i]));
		}

		refreshView();
	}

	private void populateScoreCells(int id) {

		GridView sum_grid = (GridView) findViewById(id);
		ArrayList<String> strs = new ArrayList<String>();
		for (int i = 0; i < 6; i++)
			strs.add("");

		ArrayAdapter<String> adapter = new ScoreCellAdapter(strs, this);

		sum_grid.setAdapter(adapter);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	public class InputCellAdapter extends ArrayAdapter<String> {

		private List<String> strlist;
		private Context context;
		private Player p;

		public InputCellAdapter(List<String> strlist, Context ctx, Player p) {
			super(ctx, R.layout.grid_cell, strlist);
			this.strlist = strlist;
			this.context = ctx;
			this.p = p;
		}

		public View getView(int position, View convertView, ViewGroup parent) {

			if (convertView == null) {
				// This a new view we inflate the new layout
				LayoutInflater inflater = (LayoutInflater) context
						.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
				convertView = inflater.inflate(R.layout.grid_cell, parent,
						false);
			}
			TextView tv = (TextView) convertView.findViewById(R.id.num);
			String s = strlist.get(position);

			tv.setText(s);

			if (!p.isAvailable(parent.getId(), position / 6, position % 6))
				tv.setBackgroundResource(R.color.invalid_blue);

			return convertView;
		}
	}

	public class ScoreCellAdapter extends ArrayAdapter<String> {

		private List<String> strlist;
		private Context context;

		public ScoreCellAdapter(List<String> strlist, Context ctx) {
			super(ctx, R.layout.sum_cell, strlist);
			this.strlist = strlist;
			this.context = ctx;
		}

		public View getView(int position, View convertView, ViewGroup parent) {
			if (convertView == null) {
				// This a new view we inflate the new layout
				LayoutInflater inflater = (LayoutInflater) context
						.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
				convertView = inflater
						.inflate(R.layout.sum_cell, parent, false);
			}
			TextView tv = (TextView) convertView.findViewById(R.id.sum_value);
			String s = strlist.get(position);

			tv.setText(s);

			return convertView;
		}
	}

	// roll unselected dice
	void rollDice() {
		for (int i = 0; i < 6; i++) {
			if (!selectedDice[i]) {
				diceValues[i] = (int) (Math.random() * 6) + 1;
				ImageView iv = (ImageView) findViewById(diceSlotId(i));
				iv.setImageResource(diceId(diceValues[i] - 1, selectedDice[i]));
			} else {
				// if die is selected, lock it so it's selection cannot be
				// changed in the next move
				lockedDice[i] = true;
			}
		}
	}

	public void shakingStarted() {
		if (currentPlayer.getRoll() == 3)
			return;

		if (mp == null || !mp.isPlaying()) {
			mp = MediaPlayer.create(getApplicationContext(), R.raw.shake);
			mp.start();
		}

		rollDice();
	}

	public void shakingStopped() {
		if (currentPlayer.getRoll() == 3)
			return;

		if (mp != null) {
			mp.stop();
		}

		mp = MediaPlayer.create(getApplicationContext(), R.raw.roll);
		mp.start();
		currentPlayer.incRoll();
		updateInfo();
	}

	// update info about move, roll and player on the top of the screen
	public void updateInfo() {
		tvMove.setText("move: " + currentPlayer.getMove());
		tvRoll.setText("roll: " + currentPlayer.getRoll() + "/3");
		tvPlayer.setText(currentPlayer.getName());

	}

	// set cells available for entry
	private void refreshView(int id) {
		GridView gv = (GridView) findViewById(id);
		for (int i = 0; i < gv.getChildCount(); i++) {
			TextView tv = (TextView) gv.getChildAt(i).findViewById(R.id.num);
			if (currentPlayer.isAvailable(id, i / 6, i % 6)) {
				tv.setBackgroundResource(R.color.lighter_blue);
			} else {
				tv.setBackgroundResource(R.color.invalid_blue);
			}

			int value = currentPlayer.getValue(id, i / 6, i % 6);
			if (value != Player.EMPTY)
				tv.setText("" + value);
			else
				tv.setText("");
		}
	}

	private void refreshSums(int id) {
		GridView gv = (GridView) findViewById(id);
		for (int i = 0; i < gv.getChildCount(); i++) {
			int value = currentPlayer.getSumValue(id, i);
			TextView tv = (TextView) gv.getChildAt(i).findViewById(
					R.id.sum_value);
			if (value != Player.EMPTY)
				tv.setText("" + value);
			else
				tv.setText("");
		}
	}

	public void refreshTotalScore() {
		TextView tv = (TextView) findViewById(R.id.total_score);
		tv.setText("total score: " + currentPlayer.getTotalScore());
	}

	@Override
	public void refreshView() {
		refreshView(R.id.num_board_grid);
		refreshView(R.id.min_max_grid);
		refreshView(R.id.spec_grid);
		refreshSums(R.id.num_sum);
		refreshSums(R.id.min_max_sum);
		refreshSums(R.id.spec_sum);
		refreshTotalScore();
		updateInfo();
	}

	@Override
	public void run() {
		try {
			Thread.sleep(2000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		nextMove();
	}
}
