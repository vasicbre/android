package rs.ac.bg.etf.vn110012d;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.NumberPicker;

public class MainActivity extends Activity implements
		NumberPicker.OnValueChangeListener {

	Button newgame, settings, history, resume;
	static Dialog dialog;

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
				showDialog();
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

	public void showDialog() {
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
				Intent intent = new Intent(getApplicationContext(),
						GameplayActivity.class);
				startActivity(intent);
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
		// TODO Auto-generated method stub

	}
}
