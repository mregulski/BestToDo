package ppt.reshi.besttodo.model.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import org.joda.time.DateTime;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;
import ppt.reshi.besttodo.model.Tag;
import ppt.reshi.besttodo.model.Task;
import ppt.reshi.besttodo.model.db.contract.TaskContract;

/**
 * Created by Marcin Regulski on 26.04.2017.
 */

public class TaskDataSource {
    private final static String TAG = "TaskDataSource";
    private TaskDbHelper dbHelper;
    // todo: for colors generation - probably should be refactored out
    private Random rng;

    public TaskDataSource(Context context) {
        dbHelper = new TaskDbHelper(context);
        rng = new Random();
    }


    /**
     * Create a new task.
     * @param title task's title
     * @param description task's description
     * @param deadline tasks's deadline (null if there is no deadline)
     * @param tags list of tag names to associate this task with. Any non-existent tags
     *             found are automatically created.
     * @return an Observable resolving to the newly inserted task
     */
    public Observable<Task> createTask(String title, String description,
                                       DateTime deadline, List<String> tags) {
        return Observable.fromCallable(() -> {
            SQLiteDatabase database = dbHelper.getWritableDatabase();
            ContentValues values = new ContentValues();
            values.put(TaskContract.TaskEntry.COLUMN_NAME_TITLE, title);
            values.put(TaskContract.TaskEntry.COLUMN_NAME_DESCRIPTION, description);
            if (deadline != null) {
                values.put(TaskContract.TaskEntry.COLUMN_NAME_DEADLINE, deadline.toDateTimeISO().toString());
            }
            // tags are inserted later because they need the new task's id
            long insertId = database.insert(TaskContract.TaskEntry.TABLE_NAME, null, values);
            String[] args = {Long.toString(insertId)};
            Cursor cursor = database.rawQuery(TaskDbHelper.SQL_SELECT_TASK_WITH_TAGS, args);
            cursor.moveToNext();
            Task newTask = taskFromCursor(cursor);
            cursor.close();
            for (String tagName : tags) {
                Tag tag = createTag(tagName, database);
                newTask.tag(tag);
                link(newTask, tag);
            }
            database.close();
            return newTask;
        }).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread());
    }

    /**
     * Create a new tag.
     * Uses an existing database connection because it's only called as a utility method
     * @param tagName - title for the tag
     * @param database - connection to use
     * @return the created tag, or an existing one if the name was found
     */
    private Tag createTag(String tagName, SQLiteDatabase database) {
        ContentValues vals = new ContentValues();
        vals.put(TaskContract.TagEntry.COLUMN_NAME_TITLE, tagName);
        vals.put(TaskContract.TagEntry.COLUMN_NAME_COLOR, getRandomHexColor());
        Tag tag = getTag(tagName);
        if (tag == null) {
            Long tagId = database.insert(TaskContract.TagEntry.TABLE_NAME, null, vals);
            if (tagId < 1) {
                Log.e(TAG, "Inserted id < 1: " + tagId + ", when inserting " + vals);
            }
            tag = getTag(tagName);
        }
        return tag;
    }

    /**
     * Return all tasks with a given tag.
     * @param tag tag name to search by
     * @return an Observable resolving to a list of all tasks tagged with the tag
     */
    public Observable<List<Task>> getTasks(String tag) {
        return Observable.fromCallable(() -> {
            SQLiteDatabase database = dbHelper.getReadableDatabase();
            String[] args = new String[1];
            args[0] = tag == null ? "%" : "%" + tag + "%";
            Cursor cursor = database.rawQuery(TaskDbHelper.SQL_SELECT_TASKS_WITH_TAGS_FOR_TAG, args);
            List<Task> tasks = new ArrayList<>(cursor.getCount());
            while (cursor.moveToNext()) {
                tasks.add(taskFromCursor(cursor));
            }
            database.close();
            return tasks;
        }).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread());
    }

    /**
     * Get a list of all tags in the database, including ones without any tasks.
     * @return an Observable resolving to a list of all tags
     */
    public Observable<List<Tag>> getTags() {
        return Observable.fromCallable(() -> {
            SQLiteDatabase database = dbHelper.getReadableDatabase();
            Cursor cursor = database.query(TaskContract.TagEntry.TABLE_NAME,
                    TaskContract.TagEntry.ALL_COLUMNS, null, null, null, null, null);
            List<Tag> tags = new ArrayList<>(cursor.getCount());
            while (cursor.moveToNext()) {
                tags.add(cursorToTag(cursor));
            }
            Log.d(TAG, "returning tags: " + tags);
            return tags;
        }).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread());
    }

    private Tag getTag(String name) {
        SQLiteDatabase database = dbHelper.getReadableDatabase();
        String[] args = {name};
        Tag tag;
        Cursor cursor = database.query(TaskContract.TagEntry.TABLE_NAME,
                TaskContract.TagEntry.ALL_COLUMNS,
                TaskContract.TagEntry.COLUMN_NAME_TITLE + " = ?",
                args, null, null, null, null);
        if (cursor.getCount() > 0) {
            cursor.moveToNext();
            tag = cursorToTag(cursor);
        } else {
            tag = null;
        }
        cursor.close();
        return tag;
    }


    public Observable<Task> updateTask(Task task, List<String> newTags) {
        return Observable.fromCallable(() -> {
            SQLiteDatabase database = dbHelper.getWritableDatabase();
            // insert updated task
            ContentValues values = contentFromTask(task);
            String[] args = {Integer.toString(task.id())};
            database.update(TaskContract.TaskEntry.TABLE_NAME,
                    values,
                    TaskContract.TaskEntry._ID + "= ?",
                    args);
            // clear tag links
            removeAllTagLinks(task.id());
            // recreate tag links
            for (String tagName : newTags) {
                Tag tag = createTag(tagName, database);
                task.tag(tag);
                link(task, tag);
            }
            return task;
        }).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread());
    }

    public Observable<Task> markDone(Task task, boolean done) {
        return Observable.fromCallable(()-> {
            SQLiteDatabase database = dbHelper.getWritableDatabase();
            ContentValues values = new ContentValues();
            values.put(TaskContract.TaskEntry._ID, task.id());
            values.put(TaskContract.TaskEntry.COLUMN_NAME_DONE, done);
            String[] args = {Integer.toString(task.id())};
            database.update(TaskContract.TaskEntry.TABLE_NAME,
                    values,
                    TaskContract.TaskEntry._ID + " = ?",
                    args);
            return task.done(done);
        }).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread());
    }


    public Observable<Boolean> deleteTask(Task task) {
        return Observable.fromCallable(()-> {
            SQLiteDatabase database = dbHelper.getWritableDatabase();
            String[] args = {task.id().toString()};
            database.delete(TaskContract.TaskEntry.TABLE_NAME,
                    TaskContract.TaskEntry._ID + " = ?",
                    args);
            database.delete(TaskContract.TaskTagsEntry.TABLE_NAME,
                    TaskContract.TaskTagsEntry.COLUMN_NAME_TASK + " = ?",
                    args);
            return true;
        }).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread());
    }

    // delete all tag links from task by id
    private void removeAllTagLinks(Integer taskId) {
        SQLiteDatabase database = dbHelper.getWritableDatabase();
        String[] args = {Integer.toString(taskId)};
        database.delete(TaskContract.TaskTagsEntry.TABLE_NAME,
                        TaskContract.TaskTagsEntry.COLUMN_NAME_TASK + " = ?",
                        args);
    }



    /**
     * Put a task into ContentValues
     * @param task a Task to convert
     * @return a ContentValues with task's data
     */
    private ContentValues contentFromTask(Task task) {
        ContentValues values = new ContentValues();
        values.put(TaskContract.TaskEntry.COLUMN_NAME_TITLE, task.title());
        values.put(TaskContract.TaskEntry.COLUMN_NAME_DESCRIPTION, task.description());
        if (task.deadline() != null) {
            values.put(TaskContract.TaskEntry.COLUMN_NAME_DEADLINE, task.deadline().toDateTimeISO().toString());
        }
        return values;
    }

    /**
     * Create entry linking a task with a todo
     * @param task task to connect
     * @param tag tag to connect with
     * @return true if insert was successful
     */
    private boolean link(Task task, Tag tag) {
        SQLiteDatabase database = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(TaskContract.TaskTagsEntry.COLUMN_NAME_TAG, tag.getId());
        values.put(TaskContract.TaskTagsEntry.COLUMN_NAME_TASK, task.id());
        return database.insert(TaskContract.TaskTagsEntry.TABLE_NAME, null, values) > 0;
    }

    /**
     * Create a task from cursor data
     * @param cursor - a Cursor containing task's data
     * @return a Task representing cursor's current row
     */
    private Task taskFromCursor(Cursor cursor) {
        int idCol = cursor.getColumnIndex(TaskContract.TaskEntry._ID);
        int titleCol = cursor.getColumnIndex(TaskContract.TaskEntry.COLUMN_NAME_TITLE);
        int descriptionCol = cursor.getColumnIndex(TaskContract.TaskEntry.COLUMN_NAME_DESCRIPTION);
        int deadlineCol = cursor.getColumnIndex(TaskContract.TaskEntry.COLUMN_NAME_DEADLINE);
        int tagNamesCol = cursor.getColumnIndex("tag names");
        int tagColorsCol = cursor.getColumnIndex("tag colors");
        int doneCol = cursor.getColumnIndex(TaskContract.TaskEntry.COLUMN_NAME_DONE);

        DateTime deadline = null;
        String dateString = cursor.getString(deadlineCol);
        if (dateString != null) {
            try {
                deadline = DateTime.parse(dateString);
            } catch (IllegalArgumentException err) {
                Log.e(TAG, "Invalid deadline format: " + dateString);
            }
        }
        Task task = new Task()
                .id(cursor.getInt(idCol))
                .title(cursor.getString(titleCol))
                .description(cursor.getString(descriptionCol))
                .deadline(deadline)
                .done(cursor.getInt(doneCol) == 1);


        if (cursor.getString(tagNamesCol) != null && cursor.getString(tagColorsCol) != null) {
            String[] tagNames;
            String[] tagColors;
            tagNames = cursor.getString(tagNamesCol).split(",");
            tagColors = cursor.getString(tagColorsCol).split(",");
            for (int i = 0; i < tagNames.length; i++) {
                task.tag(new Tag().title(tagNames[i]).color(tagColors[i]));
            }
        }

        return task;
    }

    /**
     * Create a tag from cursor data
     * @param cursor - a Cursor containing tag's record
     * @return a Tag represneting curosr's current row
     */
    private Tag cursorToTag(Cursor cursor) {
        int idCol = cursor.getColumnIndex(TaskContract.TagEntry._ID);
        int titleCol = cursor.getColumnIndex(TaskContract.TagEntry.COLUMN_NAME_TITLE);
        int colorCol = cursor.getColumnIndex(TaskContract.TagEntry.COLUMN_NAME_COLOR);
        return new Tag()
                .id(cursor.getInt(idCol))
                .title(cursor.getString(titleCol))
                .color(cursor.getString(colorCol));
    }

    // todo: probably should be refactored out
    // todo: also, maybe just keep the color in DB as a number?
    private String getRandomHexColor() {
        int r = rng.nextInt(256);
        int g = rng.nextInt(256);
        int b = rng.nextInt(256);
        return "#" + String.format("%02X", r) + String.format("%02X", g) + String.format("%02X", b);
    }
}
