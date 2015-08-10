package com.startup.colleague.dao;

import java.util.ArrayList;
import java.util.List;

import com.startup.colleague.model.NoteModel;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;

public class NoteDao {
	private static final String TAG = "NoteDao";
	private static final int DB_VERSION = 1;
	private static final String NOTETABLE = "noteTable";
	private static final String DBNAME = "colleagueSay.db";
	
	private final Context context;
	private SQLiteDatabase db;
	private DBOpenHelper dbOpenHelper;

	public NoteDao(Context paramContext) {
		this.context = paramContext;
	}

	private NoteModel ConvertToRecord(Cursor paramCursor) {
		NoteModel note = new NoteModel();
		note.setId(paramCursor.getInt(0));
		note.setUserId(paramCursor.getInt(1));
		note.setCompanyId(paramCursor.getInt(2));
		note.setContent(paramCursor.getString(3));
		note.setViewCnt(paramCursor.getInt(4));
		note.setCommentCnt(paramCursor.getInt(5));
		note.setReportCnt(paramCursor.getInt(6));
		note.setTimeStm(paramCursor.getString(7));
		
		return note;
	}

	private List<NoteModel> ConvertToRecords(Cursor paramCursor) {
		List<NoteModel> noteList = new ArrayList<NoteModel>();
		NoteModel note;
		while (paramCursor.moveToNext()) {
			note = new NoteModel();
			note.setId(paramCursor.getInt(0));
			note.setUserId(paramCursor.getInt(1));
			note.setCompanyId(paramCursor.getInt(2));
			note.setContent(paramCursor.getString(3));
			note.setViewCnt(paramCursor.getInt(4));
			note.setCommentCnt(paramCursor.getInt(5));
			note.setReportCnt(paramCursor.getInt(6));
			note.setTimeStm(paramCursor.getString(7));
			noteList.add(note);
			paramCursor.moveToNext();
		}
		return noteList;
	}
	public void close() {
		if (this.db == null)
			return;
		this.db.close();
		this.db = null;
	}

	public long deleteAllData() {
		return this.db.delete(NOTETABLE, null, null);
	}

	public long insert(NoteModel paramNote) {
		ContentValues localContentValues = new ContentValues();
		
		localContentValues.put("id", paramNote.getId());
		localContentValues.put("userId", paramNote.getUserId());
		localContentValues.put("companyId", paramNote.getCompanyId());
		localContentValues.put("content", paramNote.getContent());
		localContentValues.put("viewCnt", paramNote.getViewCnt());
		localContentValues.put("commentCnt", paramNote.getCommentCnt());
		localContentValues.put("reportCnt", paramNote.getReportCnt());
		localContentValues.put("timeStm", paramNote.getTimeStm());
		
		return this.db.insert(NOTETABLE, null, localContentValues);
	}

	public void open() throws SQLiteException {
		DBOpenHelper localDBOpenHelper = new DBOpenHelper(DBNAME, null, 1);
		this.dbOpenHelper = localDBOpenHelper;
		try {
			SQLiteDatabase localSQLiteDatabase1 = this.dbOpenHelper
					.getWritableDatabase();
			this.db = localSQLiteDatabase1;
			return;
		} catch (SQLiteException localSQLiteException) {
			SQLiteDatabase localSQLiteDatabase2 = this.dbOpenHelper
					.getReadableDatabase();
			this.db = localSQLiteDatabase2;
		}
	}

	public List<NoteModel> queryAllData() {
		SQLiteDatabase localSQLiteDatabase = this.db;
		String[] arrayColumns = null;
		String[] whereClause = null;
		
		String havingClause = null;
		String orderByClause = null;
		String limitClause = null;
		Cursor localCursor = localSQLiteDatabase.query(NOTETABLE,
				arrayColumns, null, whereClause, havingClause, orderByClause, limitClause);
		return ConvertToRecords(localCursor);
	}
	
	public NoteModel queryDataById(Integer id) {
		SQLiteDatabase localSQLiteDatabase = this.db;
		String[] arrayColumns = null;
		
		String whereClause = "id=?";
		String[] selectionArgs = {id.toString()};
		String havingClause = null;
		String orderByClause = null;
		String limitClause = null;
		Cursor localCursor = localSQLiteDatabase.query(NOTETABLE,
				arrayColumns, whereClause, selectionArgs, havingClause, orderByClause, limitClause);
		return ConvertToRecord(localCursor);
	}
	
	class DBOpenHelper extends SQLiteOpenHelper {
		public DBOpenHelper(String paramCursorFactory,
				SQLiteDatabase.CursorFactory paramInt, int arg4) {
			super(context, paramCursorFactory, paramInt, DB_VERSION);
		}

		public void onCreate(SQLiteDatabase paramSQLiteDatabase) {
			paramSQLiteDatabase.execSQL("create table " + NOTETABLE
					+ " (id int,userId int,companyId int,content text,viewCnt int,commentCnt int,reportCnt int,timeStm text);");
		}

		public void onUpgrade(SQLiteDatabase paramSQLiteDatabase,
				int paramInt1, int paramInt2) {
			paramSQLiteDatabase.execSQL("DROP TABLE IF EXISTS " + NOTETABLE);
			onCreate(paramSQLiteDatabase);
		}
	}
}
