package rs.ac.bg.etf.vn110012d;

import java.util.ArrayList;
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
import android.widget.Toast;

public class GameplayActivity extends Activity implements Shaker.Callback {

	int[] diceValues;
	boolean[] selectedDice;
	boolean[] lockedDice;

	int move;

	private static final int SHAKE_THRESHOLD = 400;
	private static final int END_SHAKE_TIME_GAP = 600;

	private MediaPlayer mp;
	Shaker s;	
	Player p;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.game_layout);
		populateInputCells(R.id.num_board_grid, 36);
		populateInputCells(R.id.min_max_grid, 12);
		populateInputCells(R.id.spec_grid, 30);
		populateScoreCells(R.id.num_sum);
		populateScoreCells(R.id.min_max_sum);
		populateScoreCells(R.id.spec_sum);
		loadDice();

		s = new Shaker(this, SHAKE_THRESHOLD, END_SHAKE_TIME_GAP, this);
		s.register();
		
		p = new Player();
	}
	
	protected void onPause() {
		super.onPause();
		s.unregister();
	}
	
	protected void onResume() {
		super.onResume();
		s.register();
	}

	private void loadDice() {
		ImageView[] dice = new ImageView[6];
		diceValues = new int[6];
		selectedDice = new boolean[6];
		lockedDice = new boolean[6];
		
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
		if (move > 0 && !lockedDice[ord]) {
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

	private void populateInputCells(int id, int cnt) {
		GridView num_grid = (GridView) findViewById(id);

		List<String> strs = new ArrayList<String>();
		for (int i = 0; i < cnt; i++)
			strs.add("");

		ArrayAdapter<String> adapter = new LightCellAdapter(strs, this);

		num_grid.setAdapter(adapter);

		num_grid.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {

				Toast toast = Toast.makeText(getApplicationContext(),
						"Numbers: " + position / 6 + "," + position % 6,
						Toast.LENGTH_SHORT);
				
				//view.findViewById(R.id.num).setBackgroundResource(R.color.invalid_blue);
				
				toast.show();
								
				resetMove();
			}
		});

	}
	
	private void resetMove() {
		move = 0;
		
		for(int i = 0; i < 6; i++) {
			selectedDice[i] = false;
			ImageView iv = (ImageView) findViewById(diceSlotId(i));
			iv.setImageResource(diceId(diceValues[i] - 1, selectedDice[i]));
		}
	}

	private void populateScoreCells(int id) {

		GridView sum_grid = (GridView) findViewById(id);
		ArrayList<String> strs = new ArrayList<String>();
		for (int i = 0; i < 6; i++)
			strs.add("");

		ArrayAdapter<String> adapter = new DarkCellAdapter(strs, this);

		sum_grid.setAdapter(adapter);

		/*sum_grid.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {

				Toast toast = Toast
						.makeText(getApplicationContext(), "Sum: " + position
								/ 6 + "," + position % 6, Toast.LENGTH_SHORT);
				toast.show();
			}
		});*/
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

	public class LightCellAdapter extends ArrayAdapter<String> {

		private List<String> strlist;
		private Context context;

		public LightCellAdapter(List<String> strlist, Context ctx) {
			super(ctx, R.layout.grid_cell, strlist);
			this.strlist = strlist;
			this.context = ctx;
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

			return convertView;
		}
	}

	public class DarkCellAdapter extends ArrayAdapter<String> {

		private List<String> strlist;
		private Context context;

		public DarkCellAdapter(List<String> strlist, Context ctx) {
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
			TextView tv = (TextView) convertView.findViewById(R.id.num);
			String s = strlist.get(position);

			tv.setText(s);

			return convertView;
		}
	}

	void rollDice() {
		for (int i = 0; i < 6; i++) {
			if (!selectedDice[i]) {
				diceValues[i] = (int) (Math.random() * 6) + 1;
				ImageView iv = (ImageView) findViewById(diceSlotId(i));
				iv.setImageResource(diceId(diceValues[i] - 1, selectedDice[i]));
			}
			else {
				lockedDice[i] = true;
			}
		}
	}

	public void shakingStarted() {
		if(move == 3)
			return;
		
		if (mp == null || !mp.isPlaying()) {
			mp = MediaPlayer.create(getApplicationContext(), R.raw.shake);
			mp.start();
		}

		rollDice();
	}

	public void shakingStopped() {
		if(move == 3)
			return;
		
		if (mp != null) {
			mp.stop();
		}

		mp = MediaPlayer.create(getApplicationContext(), R.raw.roll);
		mp.start();
		move++;
	}
}
