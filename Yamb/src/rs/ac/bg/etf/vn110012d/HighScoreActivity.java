package rs.ac.bg.etf.vn110012d;

import java.util.ArrayList;
import java.util.List;
import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

public class HighScoreActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.highscores);
		populateScores();
	}

	private void populateScores() {
		ListView lv = (ListView) findViewById(R.id.high_scores);

		ArrayList<String> strs = new ArrayList<String>();
		ArrayList<Integer> scores = new ArrayList<Integer>();
		for (int i = 0; i < 20; i++) {
			strs.add("Name" + i);
			scores.add(i * 10 + i * i);
		}

		ArrayAdapter<String> adapter = new ScoreAdapter(strs, scores, this,
				R.layout.highscore);
		lv.setAdapter(adapter);
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
