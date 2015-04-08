package rs.ac.bg.etf.vn110012d;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class MySQLiteHelper extends SQLiteOpenHelper {

	private static final String DATABASE = "scores.db";
	private static final int DATABASE_VERSION = 1;

	// Table names
	public static final String SCORE_TABLE = "scores";
	public static final String GAME_TABLE = "games";
	public static final String MOVE_TABLE = "moves";
	public static final String PLAYER_TABLE = "players";
	public static final String ROLL_TABLE = "rolls";

	// Common column name
	public static final String ID = "_id";
	public static final String GAME_ID = "game_id";
	public static final String PLAYER_ID = "player_id";

	// SCORES table columns
	public static final String SCORE = "score";

	// GAMES table columns
	public static final String START = "start";
	public static final String END = "end";

	// PLAYERS table columns
	public static final String NAME = "name";

	// MOVES table columns
	public static final String ORD = "ord";
	public static final String BOARD_X = "board_x";
	public static final String BOARD_Y = "board_y";
	public static final String VALUE = "value";

	// ROLLS table columns
	public static final String RESULT = "result";
	public static final String LOCKED = "locked";
	public static final String MOVE_ID = "move_id";

	private static final String CREATE_TABLE_GAME = "create table "
			+ GAME_TABLE + "(" + ID + " integer primary key autoincrement"
			+ "," + START + " datetime," + END + " datetime);";

	private static final String CREATE_TABLE_PLAEYR = "create table "
			+ PLAYER_TABLE + "(" + ID + " integer primary key autoincrement"
			+ "," + NAME + " text not null" + ");";

	private static final String CREATE_TABLE_SCORE = "create table "
			+ SCORE_TABLE + "(" + ID + " integer primary key autoincrement"
			+ "," + PLAYER_ID + " integer not null," + GAME_ID
			+ " integer not null," + SCORE + " integer not null);";

	private static final String CREATE_TABLE_MOVE = "create table "
			+ MOVE_TABLE + "(" + ID + " integer primary key autoincrement"
			+ ORD + " integer not null," + BOARD_X + " integer not null, "
			+ VALUE + " integer not null," + BOARD_Y + " integer not null,"
			+ GAME_ID + " integer not null," + PLAYER_ID
			+ " integer not null);";

	private static final String CREATE_TABLE_ROLL = "create table "
			+ ROLL_TABLE + "(" + ID + " integer primary key autoincrement"
			+ ORD + " integet not null," + RESULT + " text not null," + LOCKED
			+ " text not null" + MOVE_ID + " integer not null);";

	public MySQLiteHelper(Context context) {
		super(context, DATABASE, null, DATABASE_VERSION);
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		db.execSQL(CREATE_TABLE_GAME);
		db.execSQL(CREATE_TABLE_PLAEYR);
		db.execSQL(CREATE_TABLE_SCORE);
		db.execSQL(CREATE_TABLE_MOVE);
		db.execSQL(CREATE_TABLE_ROLL);
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		onCreate(db);
	}
}
