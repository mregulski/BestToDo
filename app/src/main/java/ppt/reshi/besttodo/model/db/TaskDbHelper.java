package ppt.reshi.besttodo.model.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import ppt.reshi.besttodo.model.db.contract.TaskContract;

/**
 * Created by Marcin Regulski on 25.04.2017.
 */

public class TaskDbHelper extends SQLiteOpenHelper {

    public static final int DATABASE_VERSION = 6;
    public static final String DATABASE_NAME = "todos.db";

    public TaskDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(SQL_CREATE_TASKS);
        db.execSQL(SQL_CREATE_TAGS);
        db.execSQL(SQL_CREATE_TASKTAGS);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("drop table if exists " + TaskContract.TaskEntry.TABLE_NAME);
        db.execSQL("drop table if exists " + TaskContract.TagEntry.TABLE_NAME);
        db.execSQL("drop table if exists " + TaskContract.TaskTagsEntry.TABLE_NAME);
        db.execSQL(SQL_CREATE_TASKS);
        db.execSQL(SQL_CREATE_TAGS);
        db.execSQL(SQL_CREATE_TASKTAGS);
    }

    @Override
    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("drop table if exists " + TaskContract.TaskEntry.TABLE_NAME);
        db.execSQL("drop table if exists " + TaskContract.TagEntry.TABLE_NAME);
        db.execSQL("drop table if exists " + TaskContract.TaskTagsEntry.TABLE_NAME);
        db.execSQL(SQL_CREATE_TASKS);
        db.execSQL(SQL_CREATE_TAGS);
        db.execSQL(SQL_CREATE_TASKTAGS);
    }

    static final String SQL_CREATE_TASKS =
            "create table " + TaskContract.TaskEntry.TABLE_NAME + "("
                    + TaskContract.TaskEntry._ID + " integer primary key,"
                    + TaskContract.TaskEntry.COLUMN_NAME_TITLE + " text,"
                    + TaskContract.TaskEntry.COLUMN_NAME_DESCRIPTION + " text,"
                    + TaskContract.TaskEntry.COLUMN_NAME_DEADLINE + " text,"
                    + TaskContract.TaskEntry.COLUMN_NAME_DONE + " boolean);";

    static final String SQL_CREATE_TAGS = "create table " + TaskContract.TagEntry.TABLE_NAME + "("
                    + TaskContract.TagEntry._ID + " integer primary key,"
                    + TaskContract.TagEntry.COLUMN_NAME_TITLE + " text,"
                    + TaskContract.TagEntry.COLUMN_NAME_COLOR + " text,"
                    + " unique (" + TaskContract.TagEntry.COLUMN_NAME_TITLE + "));";

    static final String SQL_CREATE_TASKTAGS = "create table " + TaskContract.TaskTagsEntry.TABLE_NAME + "("
                    + TaskContract.TaskTagsEntry.COLUMN_NAME_TASK + " integer,"
                    + TaskContract.TaskTagsEntry.COLUMN_NAME_TAG + " integer, "
                    + "foreign key (" + TaskContract.TaskTagsEntry.COLUMN_NAME_TASK
                    + ") references " + TaskContract.TaskEntry.TABLE_NAME + ","
                    + "foreign key (" + TaskContract.TaskTagsEntry.COLUMN_NAME_TAG
                    + ") references " + TaskContract.TagEntry.TABLE_NAME + ")";

    static final String SQL_SELECT_TASK_WITH_TAGS =
            "SELECT "
                    + "_todo.*, "
                    + "group_concat(" + "_tag." + TaskContract.TagEntry.COLUMN_NAME_TITLE + ") as \"tag names\", "
                    + "group_concat(" + "_tag." + TaskContract.TagEntry.COLUMN_NAME_COLOR + ") as \"tag colors\" "
                    + "FROM "
                    + TaskContract.TaskEntry.TABLE_NAME + " as _todo left outer join "
                    + TaskContract.TaskTagsEntry.TABLE_NAME + " as _todotag"
                    + " on _todo." + TaskContract.TaskEntry._ID
                    + " = _todotag." + TaskContract.TaskTagsEntry.COLUMN_NAME_TASK
                    + " left join " + TaskContract.TagEntry.TABLE_NAME + " as _tag on _todotag.tag = _tag._id"
                    + " where _todo._id = ? group by _todo._id";

    static final String SQL_SELECT_TASKS_WITH_TAGS_FOR_TAG =
            "SELECT"
                    + " _todo.*,"
                    + " group_concat(" + "_tag." + TaskContract.TagEntry.COLUMN_NAME_TITLE + ") as 'tag names',"
                    + " group_concat(" + "_tag." + TaskContract.TagEntry.COLUMN_NAME_COLOR + ") as 'tag colors'"
                    + " FROM "
                    + TaskContract.TaskEntry.TABLE_NAME + " as _todo left outer join "
                    + TaskContract.TaskTagsEntry.TABLE_NAME + " as _todotag"
                    + " on _todo." + TaskContract.TaskEntry._ID
                    + " = _todotag." + TaskContract.TaskTagsEntry.COLUMN_NAME_TASK
                    + " left join " + TaskContract.TagEntry.TABLE_NAME + " as _tag on _todotag.tag = _tag._id"
                    + " group by _todo._id"
                    + " having " + "group_concat(" + "_tag." + TaskContract.TagEntry.COLUMN_NAME_TITLE + ") like ? "
                    + " order by _todo.done";
    // unused
    static final String SQL_INSERT_TAG_UNIQUE =
            "insert or ignore into " + TaskContract.TagEntry.TABLE_NAME
            + "(" + TaskContract.TagEntry.COLUMN_NAME_TITLE + ","
                    + TaskContract.TagEntry.COLUMN_NAME_COLOR+ ") "
            + " values (?,?)";
}
