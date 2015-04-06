package rs.ac.bg.etf.vn110012d;

import java.util.ArrayList;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.NumberPicker;

public class MainActivity extends Activity implements
		NumberPicker.OnValueChangeListener {

	public static final int MAX_PLAYERS = 5;

	Button newgame, settings, history, resume;

	int playerCnt = 1;

	private ListView nameList;
	private EditTextAdapter editTextAdapter;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		newgame = (Button) findViewById(R.id.new_game_button);
		settings = (Button) findViewById(R.id.settings_button);
		history = (Button) findViewById(R.id.history_button);
		resume = (Button) findViewById(R.id.resume_button);
		resume.setVisibility(View.GONE);

		newgame.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				showNumberPicker();
			}
		});
		
		history.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(MainActivity.this, HighScoreActivity.class);
				startActivity(intent);
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

	public void showNameDialog() {
		final Dialog dialog = new Dialog(MainActivity.this);
		dialog.setTitle("Enter names");
		dialog.setContentView(R.layout.name_dialog);

		nameList = (ListView) dialog.findViewById(R.id.list_view);
		nameList.setItemsCanFocus(true);
		editTextAdapter = new EditTextAdapter(playerCnt);
		nameList.setAdapter(editTextAdapter);

		final Button start = (Button) dialog.findViewById(R.id.start_button);
		Button close = (Button) dialog.findViewById(R.id.close_button);

		start.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				// change focus so all values are collected from edit text list
				start.requestFocus();
			}
		});

		start.setOnFocusChangeListener(new View.OnFocusChangeListener() {
			@Override
			public void onFocusChange(View v, boolean hasFocus) {
				Intent intent = new Intent(getApplicationContext(),
						GameplayActivity.class);
				intent.putExtra("NUMBER_OF_PLAYERS", playerCnt);

				for (int i = 0; i < playerCnt; i++) {
					intent.putExtra("PLAYER_" + i,
							(String) editTextAdapter.getItem(i));
				}

				dialog.dismiss();
				startActivity(intent);
			}
		});

		close.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				dialog.dismiss();
			}
		});

		dialog.show();
	}

	public void showNumberPicker() {
		final Dialog dialog = new Dialog(MainActivity.this);
		dialog.setTitle("Set number of players");
		dialog.setContentView(R.layout.dialog);

		Button play = (Button) dialog.findViewById(R.id.play_button);
		Button cancel = (Button) dialog.findViewById(R.id.cancel_button);

		final NumberPicker np = (NumberPicker) dialog
				.findViewById(R.id.number_picker);

		np.setMaxValue(5);
		np.setMinValue(1);
		np.setWrapSelectorWheel(false);
		np.setOnValueChangedListener(this);

		play.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				dialog.dismiss();
				showNameDialog();
			}
		});

		cancel.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				dialog.dismiss();
			}
		});

		dialog.show();

	}

	@Override
	public void onValueChange(NumberPicker picker, int oldVal, int newVal) {
		playerCnt = newVal;
	}

	// Adapter for inflating list view with edit text components
	public class EditTextAdapter extends BaseAdapter {

		private LayoutInflater mInflater;
		public ArrayList<ListItem> myItems = new ArrayList<ListItem>();

		public EditTextAdapter(int n) {
			mInflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);

			for (int i = 0; i < n; i++) {
				ListItem listItem = new ListItem();
				listItem.caption = "Player " + (i + 1);
				myItems.add(listItem);
			}
			notifyDataSetChanged();
		}

		public int getCount() {
			return myItems.size();
		}

		public Object getItem(int position) {
			return myItems.get(position).caption;
		}

		public long getItemId(int position) {
			return position;
		}

		@SuppressLint("InflateParams")
		public View getView(int position, View convertView, ViewGroup parent) {
			ViewHolder holder;

			if (convertView == null) {
				holder = new ViewHolder();
				convertView = mInflater.inflate(R.layout.name, null);
				holder.caption = (EditText) convertView.findViewById(R.id.name);
				convertView.setTag(holder);
			} else {
				holder = (ViewHolder) convertView.getTag();
			}

			// Fill EditText with the value you have in data source
			holder.caption.setText(myItems.get(position).caption);
			holder.caption.setId(position);

			// we need to update adapter once we finish with editing
			holder.caption
					.setOnFocusChangeListener(new View.OnFocusChangeListener() {
						public void onFocusChange(View v, boolean hasFocus) {
							if (!hasFocus) {
								final int position = v.getId();
								final EditText Caption = (EditText) v;
								myItems.get(position).caption = Caption
										.getText().toString();
							}
						}
					});

			return convertView;
		}
	}

	class ViewHolder {
		EditText caption;
	}

	class ListItem {
		String caption;
	}
}
