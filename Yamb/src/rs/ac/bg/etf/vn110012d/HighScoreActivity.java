package rs.ac.bg.etf.vn110012d;

import java.util.ArrayList;
import java.util.List;

import rs.ac.bg.etf.vn110012d.GameplayActivity.ScoreCellAdapter;

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
		
		ListView lv = (ListView) findViewById(R.id.high_scores);
		
		ArrayList<String> strs = new ArrayList<String>();
		ArrayList<Integer> scores = new ArrayList<Integer>();
		for (int i = 0; i < 20; i++) {
			strs.add("Name" + i);
			scores.add(i * 10 + i*i);
		}
		

		ArrayAdapter<String> adapter = new HighScoreAdapter(strs, scores, this);
		lv.setAdapter(adapter);
	}
	
	public class HighScoreAdapter extends ArrayAdapter<String> {

		private List<String> nameList;
		private List<Integer> scoreList;
		private Context context;

		public HighScoreAdapter(List<String> strlist,  List<Integer> scoreList, Context ctx) {
			super(ctx, R.layout.grid_cell, strlist);
			this.nameList = strlist;
			this.scoreList = scoreList;
			this.context = ctx;
		}

		public View getView(int position, View convertView, ViewGroup parent) {

			if (convertView == null) {
				// This a new view we inflate the new layout
				LayoutInflater inflater = (LayoutInflater) context
						.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
				convertView = inflater.inflate(R.layout.highscore, parent,
						false);
			}
			TextView tvName = (TextView) convertView.findViewById(R.id.tv_name);
			TextView tvScore = (TextView) convertView.findViewById(R.id.tv_score);
			
			String name = nameList.get(position);
			int score = scoreList.get(position);

			tvName.setText("" + (position + 1) + ". " + name);
			tvScore.setText("" + score);

			return convertView;
		}
	}
	
}
