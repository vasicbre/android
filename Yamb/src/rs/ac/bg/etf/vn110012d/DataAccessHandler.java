package rs.ac.bg.etf.vn110012d;

import java.util.List;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;

public class DataAccessHandler {

	private SQLiteDatabase database;
	private MySQLiteHelper dbHelper;
	
	private String[] allColumns = { MySQLiteHelper.ID,
		      MySQLiteHelper.NAME, MySQLiteHelper.SCORE };

	public DataAccessHandler(Context context) {
		dbHelper = new MySQLiteHelper(context);
	}

	public void open() throws SQLException {
		database = dbHelper.getWritableDatabase();
	}

	public void close() {
		dbHelper.close();
	}

	public long addScore(String name, int score) {
		ContentValues values = new ContentValues();
		values.put(MySQLiteHelper.NAME, name);
		values.put(MySQLiteHelper.SCORE, score);
		return database.insert(MySQLiteHelper.SCORE_TABLE, null, values);
	}

	public void getAllScores(List<String> names, List<Integer> scores) {
		Cursor cursor = database.query(MySQLiteHelper.SCORE_TABLE, allColumns, null, null, null, null, null);
		cursor.moveToFirst();
		while(!cursor.isAfterLast()) {
			names.add(cursor.getString(1));
			scores.add(cursor.getInt(2));
			cursor.moveToNext();
		}
		
		cursor.close();
	}
}
