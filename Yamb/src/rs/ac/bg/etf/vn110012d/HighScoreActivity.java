package rs.ac.bg.etf.vn110012d;

import java.util.ArrayList;
import java.util.List;
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

public class HighScoreActivity extends Activity {

	DataAccessHandler dataHandler;
	ArrayList<String> names = new ArrayList<String>();
	ArrayList<Integer> scores = new ArrayList<Integer>();
	ArrayList<Integer> ids = new ArrayList<Integer>();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.highscores);
		dataHandler = new DataAccessHandler(this);
		dataHandler.open();
		populateScores();
	}

	private void populateScores() {
		ListView lv = (ListView) findViewById(R.id.high_scores);

		dataHandler.getAllScores(names, scores, ids);

		ArrayAdapter<String> adapter = new ScoreAdapter(names, scores, this,
				R.layout.highscore);
		lv.setAdapter(adapter);

		lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				showInfo(ids.get(position));
			}
		});
	}

	private void showInfo(final int id) {
		final Dialog dialog = new Dialog(this);
		dialog.setTitle("Game info");
		dialog.setContentView(R.layout.game_info_dialog);

		ListView lv = (ListView) dialog.findViewById(R.id.scores_list);

		ArrayList<String> strs = new ArrayList<String>();
		ArrayList<Integer> scores = new ArrayList<Integer>();

		dataHandler.getAllScores(strs, scores, id);

		ArrayAdapter<String> adapter = new ScoreAdapter(strs, scores,
				dialog.getContext(), R.layout.highscore);
		lv.setAdapter(adapter);

		final Button simulate = (Button) dialog.findViewById(R.id.start_button);

		simulate.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				Intent intent = new Intent(HighScoreActivity.this,
						GameplayActivity.class);
				intent.putExtra("SIMULATION_ID", id);

				List<String> players = dataHandler.getPlayerNames(id);
				
				intent.putExtra("NUMBER_OF_PLAYERS", players.size());
				
				int i = 0;
				for (String name : players)
					intent.putExtra("PLAYER_" + (i++), name);

				startActivity(intent);
				dialog.dismiss();
			}
		});

		final Button close = (Button) dialog.findViewById(R.id.close_button);

		close.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				dialog.dismiss();
			}
		});

		dialog.show();
	}

	public static class ScoreAdapter extends ArrayAdapter<String> {

		private List<String> nameList;
		private List<Integer> scoreList;
		private Context context;
		private int resource;

		public ScoreAdapter(List<String> strlist, List<Integer> scoreList,
				Context ctx, int resource) {
			super(ctx, R.layout.grid_cell, strlist);
			this.nameList = strlist;
			this.scoreList = scoreList;
			this.context = ctx;
			this.resource = resource;
		}

		public View getView(int position, View convertView, ViewGroup parent) {
			if (convertView == null) {
				// This a new view we inflate the new layout
				LayoutInflater inflater = (LayoutInflater) context
						.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
				convertView = inflater.inflate(resource, parent, false);
			}

			TextView tvName = (TextView) convertView.findViewById(R.id.tv_name);
			TextView tvScore = (TextView) convertView
					.findViewById(R.id.tv_score);

			String name = nameList.get(position);
			int score = scoreList.get(position);

			tvName.setText("" + (position + 1) + ". " + name);
			tvScore.setText("" + score);

			return convertView;
		}
	}

}
