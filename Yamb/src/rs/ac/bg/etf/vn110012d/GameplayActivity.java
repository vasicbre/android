package rs.ac.bg.etf.vn110012d;

import java.util.ArrayList;

import java.util.List;
import android.app.Activity;
import android.app.Dialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ListView;
import android.widget.TextView;
import rs.ac.bg.etf.vn110012d.HighScoreActivity.*;

public class GameplayActivity extends Activity implements Shaker.Callback,
		Board.Callback, Runnable,
		SharedPreferences.OnSharedPreferenceChangeListener {

	public static final int MOVE_LIMIT = 78;
	public static final int MOVE_LIMIT_TEST = 1;

	private MediaPlayer mp;
	Shaker shaker;

	Board[] playerBoards;
	Board currentBoard;
	Dice dice;

	int playerCnt, moveLimit, shakingTreshold = Shaker.MIN_TRESHOLD;

	TextView tvMove, tvRoll, tvPlayer;

	DataAccessHandler dataHandler;
	long gameId;
	long[] playerIds;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.game_layout);

		dice = new Dice(this);
		dataHandler = new DataAccessHandler(this);
		dataHandler.open();
		gameId = dataHandler.addGame();

		initBoards();
		initView();
		populateBoard();
		updateInfo();
		getPreferences();
		initShaker();
	}

	protected void onPause() {
		super.onPause();
		shaker.unregister();
	}

	protected void onResume() {
		super.onResume();
		shaker.register();
	}

	@Override
	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences,
			String key) {
		getPreferences();
		shaker.setTrashold(shakingTreshold);
	}

	private void initBoards() {
		playerCnt = getIntent().getExtras().getInt("NUMBER_OF_PLAYERS");
		playerBoards = new Board[playerCnt];
		playerIds = new long[playerCnt];

		for (int i = 0; i < playerCnt; i++) {
			String name = getIntent().getExtras().getString("PLAYER_" + i);
			playerBoards[i] = new Board(this, i, name, dice);
			playerIds[i] = dataHandler.addPlayer(name);
		}

		currentBoard = playerBoards[0];
	}

	private void initShaker() {
		shaker = new Shaker(this, shakingTreshold, Shaker.END_SHAKE_TIME_GAP,
				this);
		shaker.register();

		PreferenceManager.getDefaultSharedPreferences(this)
				.registerOnSharedPreferenceChangeListener(this);
	}

	private void initView() {
		tvMove = (TextView) findViewById(R.id.move_id);
		tvRoll = (TextView) findViewById(R.id.roll_id);
		tvPlayer = (TextView) findViewById(R.id.player_id);
	}

	private void getPreferences() {
		SharedPreferences prefs = PreferenceManager
				.getDefaultSharedPreferences(this);
		int pref = prefs.getInt("sensitivity", 50);
		boolean testMode = prefs.getBoolean("testingMode", false);

		moveLimit = testMode ? MOVE_LIMIT_TEST : MOVE_LIMIT;

		shakingTreshold = (int) (Shaker.MIN_TRESHOLD + (pref / 100.f)
				* (Shaker.MAX_TRESHOLD - Shaker.MIN_TRESHOLD));
	}

	public Board getCurrentPlayer() {
		return currentBoard;
	}

	// populate grid view entries with text views
	private void populateBoard() {
		populateInputCells(R.id.num_board_grid, 36);
		populateInputCells(R.id.min_max_grid, 12);
		populateInputCells(R.id.spec_grid, 30);
		populateScoreCells(R.id.num_sum);
		populateScoreCells(R.id.min_max_sum);
		populateScoreCells(R.id.spec_sum);
	}

	private void populateInputCells(int id, int cnt) {
		GridView numGrid = (GridView) findViewById(id);

		List<String> strs = new ArrayList<String>();
		for (int i = 0; i < cnt; i++)
			strs.add("");

		ArrayAdapter<String> adapter = new InputCellAdapter(strs, this,
				currentBoard);

		numGrid.setAdapter(adapter);

		numGrid.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {

				// if field is available for entering value
				if (currentBoard.isAvailable(parent.getId(), position / 6,
						position % 6)) {

					// if field is in call column
					if (currentBoard.isCall(parent.getId(), position % 6)) {

						// if call is already made
						if (currentBoard.isCallMade()) {
							parent.post(GameplayActivity.this);
							shaker.unregister();
						}
						currentBoard.call(view, position, parent.getId());
					} else {
						enterValue(view, position, parent.getId());
						parent.post(GameplayActivity.this);
						shaker.unregister();
					}
				}
			}
		});
	}

	public void enterValue(View view, int position, int parentId) {
		TextView tv = (TextView) view.findViewById(R.id.num);
		tv.setBackgroundResource(R.color.invalid_blue);
		int value = currentBoard.set(parentId, position / 6, position % 6);
		tv.setText("" + value);
		refreshView();
	}

	// show dialog to ask player if ready to roll to avoid accidental shaking
	// due to device passing from hand to hand in multiplayer mode
	void nextMovePrompt() {

		if (checkEnd())
			return;

		int nextId = (currentBoard.getId() + 1) % playerCnt;

		final Dialog dialog = new Dialog(this);
		dialog.setTitle(playerBoards[nextId].getName() + " is on the move");
		dialog.setContentView(R.layout.next_move_dialog);

		TextView tv = (TextView) dialog.findViewById(R.id.tv_question);

		tv.setText(playerBoards[nextId].getName() + ", are you ready to roll?");

		final Button yes = (Button) dialog.findViewById(R.id.yes_button);

		yes.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				dialog.dismiss();
				nextMove();
			}
		});

		dialog.setOnCancelListener(new DialogInterface.OnCancelListener() {

			@Override
			public void onCancel(DialogInterface dialog) {
				nextMovePrompt();
			}
		});

		dialog.show();

	}

	// prepare dice for next move, called when value is entered
	private void nextMove() {
		int nextId = (currentBoard.getId() + 1) % playerCnt;
		currentBoard.incMove();
		updateInfo();

		currentBoard = playerBoards[nextId];

		dice.reset();

		refreshView();
		shaker.register();
	}

	private boolean checkEnd() {
		// if last player is done with the last move
		if (currentBoard.getMove() >= moveLimit
				&& currentBoard.getId() == playerCnt - 1) {
			showScores();
			return true;
		}

		return false;
	}

	// called when game is finished
	private void showScores() {

		final Dialog dialog = new Dialog(this);
		dialog.setTitle("Game over");
		dialog.setContentView(R.layout.score_dialog);

		ListView lv = (ListView) dialog.findViewById(R.id.scores_list);

		ArrayList<String> strs = new ArrayList<String>();
		ArrayList<Integer> scores = new ArrayList<Integer>();

		for (int i = 0; i < playerCnt; i++) {
			String name = playerBoards[i].getName();
			int score = playerBoards[i].getTotalScore();
			strs.add(name);
			scores.add(score);
			dataHandler.addScore(score, gameId, playerIds[i]);
		}

		ArrayAdapter<String> adapter = new ScoreAdapter(strs, scores,
				dialog.getContext(), R.layout.highscore);
		lv.setAdapter(adapter);

		final Button ok = (Button) dialog.findViewById(R.id.ok_button);

		ok.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				dialog.dismiss();
				GameplayActivity.this.finish();
			}
		});

		dialog.setOnCancelListener(new DialogInterface.OnCancelListener() {

			@Override
			public void onCancel(DialogInterface dialog) {
				dialog.dismiss();
				GameplayActivity.this.finish();
			}
		});

		dialog.show();

	}

	// populate grid view scores with text views
	private void populateScoreCells(int id) {

		GridView sumGrid = (GridView) findViewById(id);
		ArrayList<String> strs = new ArrayList<String>();
		for (int i = 0; i < 6; i++)
			strs.add("");

		ArrayAdapter<String> adapter = new ScoreCellAdapter(strs, this);
		sumGrid.setAdapter(adapter);
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
			Intent i = new Intent(GameplayActivity.this, Settings.class);
			startActivity(i);
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	public class InputCellAdapter extends ArrayAdapter<String> {

		private List<String> strlist;
		private Context context;
		private Board p;

		public InputCellAdapter(List<String> strlist, Context ctx, Board p) {
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

	void rollDice() {
		dice.roll();
	}

	public void shakingStarted() {
		if (currentBoard.getRoll() == 3)
			return;

		if (mp == null || !mp.isPlaying()) {
			mp = MediaPlayer.create(getApplicationContext(), R.raw.shake);
			mp.start();
		}

		rollDice();
	}

	public void shakingStopped() {
		if (currentBoard.getRoll() == 3)
			return;

		if (mp != null) {
			mp.stop();
		}

		mp = MediaPlayer.create(getApplicationContext(), R.raw.roll);
		mp.start();
		currentBoard.incRoll();
		updateInfo();
	}

	// update info about move, roll and player on the top of the screen
	public void updateInfo() {
		tvMove.setText("move: " + currentBoard.getMove());
		tvRoll.setText("roll: " + currentBoard.getRoll() + "/3");
		tvPlayer.setText(currentBoard.getName());

	}

	// set cells available for entry
	private void refreshView(int id) {
		GridView gv = (GridView) findViewById(id);
		for (int i = 0; i < gv.getChildCount(); i++) {
			TextView tv = (TextView) gv.getChildAt(i).findViewById(R.id.num);
			if (currentBoard.isAvailable(id, i / 6, i % 6)) {
				tv.setBackgroundResource(R.color.lighter_blue);
			} else {
				tv.setBackgroundResource(R.color.invalid_blue);
			}

			int value = currentBoard.getValue(id, i / 6, i % 6);
			if (value != Board.EMPTY)
				tv.setText("" + value);
			else
				tv.setText("");
		}
	}

	private void refreshSums(int id) {
		GridView gv = (GridView) findViewById(id);
		for (int i = 0; i < gv.getChildCount(); i++) {
			int value = currentBoard.getSumValue(id, i);
			TextView tv = (TextView) gv.getChildAt(i).findViewById(
					R.id.sum_value);
			if (value != Board.EMPTY)
				tv.setText("" + value);
			else
				tv.setText("");
		}
	}

	public void refreshTotalScore() {
		TextView tv = (TextView) findViewById(R.id.total_score);
		tv.setText("total score: " + currentBoard.getTotalScore());
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
		nextMovePrompt();
	}
}
