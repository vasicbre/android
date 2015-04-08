package rs.ac.bg.etf.vn110012d;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class MySQLiteHelper extends SQLiteOpenHelper {

	public static final String SCORE_TABLE = "scores";
	public static final String ID = "_id";
	public static final String NAME = "name";
	public static final String SCORE = "score";

	private static final String DATABASE = "scores.db";
	private static final int DATABASE_VERSION = 1;

	private static final String DATABASE_CREATE = "create table " + SCORE_TABLE
			+ "(" + ID + " integer primary key autoincrement" + "," + NAME
			+ " text not null" + "," + SCORE + " integer not null" + ");";

	public MySQLiteHelper(Context context) {
		super(context, DATABASE, null, DATABASE_VERSION);
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		db.execSQL(DATABASE_CREATE);
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		onCreate(db);
	}
}
