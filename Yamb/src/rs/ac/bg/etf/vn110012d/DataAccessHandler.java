package rs.ac.bg.etf.vn110012d;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;

public class DataAccessHandler {

	private SQLiteDatabase database;
	private MySQLiteHelper dbHelper;

	private String[] allColumns = { MySQLiteHelper.ID, MySQLiteHelper.NAME,
			MySQLiteHelper.SCORE };

	public DataAccessHandler(Context context) {
		dbHelper = new MySQLiteHelper(context);
	}

	public void open() throws SQLException {
		database = dbHelper.getWritableDatabase();
	}

	public void close() {
		dbHelper.close();
	}

	public long addScore(int score, long gameId, long playerId) {
		ContentValues values = new ContentValues();
		values.put(MySQLiteHelper.GAME_ID, gameId);
		values.put(MySQLiteHelper.PLAYER_ID, playerId);
		values.put(MySQLiteHelper.SCORE, score);
		return database.insert(MySQLiteHelper.SCORE_TABLE, null, values);
	}

	public long addGame() {
		ContentValues values = new ContentValues();
		values.put(MySQLiteHelper.START, getDateTime());
		values.put(MySQLiteHelper.END, getDateTime());
		return database.insert(MySQLiteHelper.GAME_TABLE, null, values);
	}

	public long addPlayer(String name) {
		ContentValues values = new ContentValues();
		values.put(MySQLiteHelper.NAME, name);
		return database.insert(MySQLiteHelper.PLAYER_TABLE, null, values);
	}

	public long addMove(int ord, int x, int y, int value, long gameId,
			long playerId) {
		ContentValues values = new ContentValues();
		values.put(MySQLiteHelper.BOARD_X, 0);
		values.put(MySQLiteHelper.BOARD_Y, 0);
		values.put(MySQLiteHelper.VALUE, 0);
		values.put(MySQLiteHelper.GAME_ID, gameId);
		values.put(MySQLiteHelper.PLAYER_ID, playerId);
		values.put(MySQLiteHelper.ORD, ord);
		return database.insert(MySQLiteHelper.MOVE_TABLE, null, values);
	}
	
	public long addRoll(int ord, String result, String locked, int moveId) {
		ContentValues values = new ContentValues();
		values.put(MySQLiteHelper.ORD, ord);
		values.put(MySQLiteHelper.MOVE_ID, moveId);
		values.put(MySQLiteHelper.RESULT, result);
		values.put(MySQLiteHelper.LOCKED, locked);
		return database.insert(MySQLiteHelper.ROLL_TABLE, null, values);
	}
	
	public void updateGame(long gameId) {
		String strFilter = MySQLiteHelper.ID + "=" + gameId;
		ContentValues values = new ContentValues();
		values.put(MySQLiteHelper.END, getDateTime());
		database.update(MySQLiteHelper.GAME_TABLE, values, strFilter, null);
	}
	
	public void updateMove(long moveId, int x, int y, int value) {
		String strFilter = MySQLiteHelper.ID + "=" + moveId;
		ContentValues values = new ContentValues();
		values.put(MySQLiteHelper.BOARD_X, x);
		values.put(MySQLiteHelper.BOARD_Y, y);
		values.put(MySQLiteHelper.VALUE, value);
		database.update(MySQLiteHelper.MOVE_TABLE, values, strFilter, null);
	}
	
	public void getAllScores(List<String> names, List<Integer> scores) {
		Cursor cursor = database.query(MySQLiteHelper.SCORE_TABLE, allColumns,
				null, null, null, null, null);
		cursor.moveToFirst();
		while (!cursor.isAfterLast()) {
			names.add(cursor.getString(1));
			scores.add(cursor.getInt(2));
			cursor.moveToNext();
		}

		cursor.close();
	}

	private String getDateTime() {
		SimpleDateFormat dateFormat = new SimpleDateFormat(
				"yyyy-MM-dd HH:mm:ss", Locale.getDefault());
		Date date = new Date();
		return dateFormat.format(date);
	}
}
