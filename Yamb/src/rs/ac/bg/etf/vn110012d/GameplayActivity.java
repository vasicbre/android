package rs.ac.bg.etf.vn110012d;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.content.Context;
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
import android.widget.TextView;
import android.widget.Toast;

public class GameplayActivity extends Activity {


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
	}

	private void loadDice() {
				
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
				toast.show();
			}
		});

	}

	private void populateScoreCells(int id) {

		GridView sum_grid = (GridView) findViewById(id);
		ArrayList<String> strs = new ArrayList<String>();
		for (int i = 0; i < 6; i++)
			strs.add("");

		ArrayAdapter<String> adapter = new DarkCellAdapter(strs, this);

		sum_grid.setAdapter(adapter);

		sum_grid.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {

				Toast toast = Toast
						.makeText(getApplicationContext(), "Sum: " + position
								/ 6 + "," + position % 6, Toast.LENGTH_SHORT);
				toast.show();
			}
		});
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

			// First let's verify the convertView is not null
			if (convertView == null) {
				// This a new view we inflate the new layout
				LayoutInflater inflater = (LayoutInflater) context
						.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
				convertView = inflater.inflate(R.layout.grid_cell, parent,
						false);
			}
			// Now we can fill the layout with the right values
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

			// First let's verify the convertView is not null
			if (convertView == null) {
				// This a new view we inflate the new layout
				LayoutInflater inflater = (LayoutInflater) context
						.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
				convertView = inflater
						.inflate(R.layout.sum_cell, parent, false);
			}
			// Now we can fill the layout with the right values
			TextView tv = (TextView) convertView.findViewById(R.id.num);
			String s = strlist.get(position);

			tv.setText(s);

			return convertView;
		}
	}
}
