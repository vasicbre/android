package rs.ac.bg.etf.vn110012d;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
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

	public long addPlayer(String name, long gameId, int ord) {
		ContentValues values = new ContentValues();
		values.put(MySQLiteHelper.NAME, name);
		values.put(MySQLiteHelper.GAME_ID, gameId);
		values.put(MySQLiteHelper.ORD, ord);
		return database.insert(MySQLiteHelper.PLAYER_TABLE, null, values);
	}

	public long addMove(int ord, long gameId, long playerId) {
		ContentValues values = new ContentValues();
		values.put(MySQLiteHelper.BOARD_X, 0);
		values.put(MySQLiteHelper.BOARD_Y, 0);
		values.put(MySQLiteHelper.VALUE, 0);
		values.put(MySQLiteHelper.GAME_ID, gameId);
		values.put(MySQLiteHelper.PLAYER_ID, playerId);
		values.put(MySQLiteHelper.ORD, ord);
		return database.insert(MySQLiteHelper.MOVE_TABLE, null, values);
	}

	public long addRoll(int ord, String result, String locked, long moveId) {
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
	
	public void updateRoll(long rollId, String locked) {
		String strFilter = MySQLiteHelper.ID + "=" + rollId;
		ContentValues values = new ContentValues();
		values.put(MySQLiteHelper.LOCKED, locked);
		database.update(MySQLiteHelper.ROLL_TABLE, values, strFilter, null);
	}

	public void getAllScores(List<String> names, List<Integer> scores, List<Integer> ids) {

		/*String[] args = { MySQLiteHelper.PLAYER_TABLE,
				MySQLiteHelper.SCORE_TABLE,
				MySQLiteHelper.PLAYER_TABLE + "." + MySQLiteHelper.ID,
				MySQLiteHelper.SCORE_TABLE + "." + MySQLiteHelper.PLAYER_ID,
				MySQLiteHelper.SCORE };*/

		// TODO check why this works hard coded, and fails with ?s and arguments
		Cursor cursor = database
				.rawQuery(
						"select name, score, scores.game_id " +
						"from players, scores " +
						"where players._id = scores.player_id " +
						"order by score desc",
						null);

		cursor.moveToFirst();
		while (!cursor.isAfterLast()) {
			names.add(cursor.getString(0));
			scores.add(cursor.getInt(1));
			ids.add(cursor.getInt(2));
			cursor.moveToNext();
		}

		cursor.close();
	}
	
	public void getAllScores(List<String> names, List<Integer> scores, int id) {
		Cursor cursor = database
				.rawQuery(
						"select name, score " +
						"from players, scores " +
						"where players._id = scores.player_id and scores.game_id = " + id + " " + 
						"order by score desc",
						null);

		cursor.moveToFirst();
		while (!cursor.isAfterLast()) {
			names.add(cursor.getString(0));
			scores.add(cursor.getInt(1));
			cursor.moveToNext();
		}

		cursor.close();
	}
	
	public List<String> getPlayerNames(int id) {
		Cursor cursor = database
				.rawQuery(
						"select name " +
						"from players " +
						"where game_id = " + id + " " + 
						"order by ord asc",
						null);
		
		List<String> names = new ArrayList<String>();

		cursor.moveToFirst();
		while (!cursor.isAfterLast()) {
			names.add(cursor.getString(0));
			cursor.moveToNext();
		}

		cursor.close();
		return names;
	}
	
	public List<Move> getMoves(int playerId) {
		Cursor cursor = database
				.rawQuery(
						"select _id, ord, board_x, board_y, value " +
						"from moves " +
						"where player_id = " + playerId + " " + 
						"order by ord asc",
						null);
		
		List<Move> moves = new ArrayList<Move>();

		cursor.moveToFirst();
		while (!cursor.isAfterLast()) {
			int id = cursor.getInt(0);
			int ord = cursor.getInt(1);
			int x = cursor.getInt(2);
			int y = cursor.getInt(3);
			int value = cursor.getInt(4);
			
			List<Roll> rolls = getRolls(id);
			
			moves.add(new Move(id, x, y, ord, value, playerId, rolls));
			cursor.moveToNext();
		}

		cursor.close();
		return moves;
	}
	
	public List<Roll> getRolls(int moveId) {
		Cursor cursor = database
				.rawQuery(
						"select ord, result, locked " +
						"from rolls " +
						"where move_id = " + moveId + " " + 
						"order by ord asc",
						null);
		
		List<Roll> rolls = new ArrayList<Roll>();

		cursor.moveToFirst();
		while (!cursor.isAfterLast()) {
			int ord = cursor.getInt(0);
			String result = cursor.getString(1);
			String locked = cursor.getString(2);
			
			rolls.add(new Roll(ord, moveId,result, locked));
			cursor.moveToNext();
		}

		cursor.close();
		return rolls;
	}
	
	public List<Player> getPlayers(int gameId) {
		Cursor cursor = database
				.rawQuery(
						"select _id, name, ord " +
						"from players " +
						"where game_id = " + gameId + " " + 
						"order by ord asc",
						null);
		
		List<Player> players = new ArrayList<Player>();

		cursor.moveToFirst();
		while (!cursor.isAfterLast()) {
			int id = cursor.getInt(0);
			String name = cursor.getString(1);
			int ord = cursor.getInt(2);
			
			List<Move> moves = getMoves(id);
			
			players.add(new Player(id, name, ord, moves));
			cursor.moveToNext();
		}

		cursor.close();
		return players;
	}

	private String getDateTime() {
		SimpleDateFormat dateFormat = new SimpleDateFormat(
				"yyyy-MM-dd HH:mm:ss", Locale.getDefault());
		Date date = new Date();
		return dateFormat.format(date);
	}
}
