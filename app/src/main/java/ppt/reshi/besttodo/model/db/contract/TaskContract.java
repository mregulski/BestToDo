package ppt.reshi.besttodo.model.db.contract;

import android.provider.BaseColumns;


/**
 * Created by Marcin Regulski on 25.04.2017.
 */

public final class TaskContract {
    // do not instantiate
    private TaskContract() {}

    public static class TaskEntry implements BaseColumns {
        public static final String TABLE_NAME = "todo";
        public static final String COLUMN_NAME_TITLE = "title";
        public static final String COLUMN_NAME_DESCRIPTION = "description";
        public static final String COLUMN_NAME_DEADLINE = "deadline";
        public static final String COLUMN_NAME_DONE = "done";
    }

    public static class TagEntry implements BaseColumns {
        public static final String TABLE_NAME = "tag";
        public static final String COLUMN_NAME_TITLE = "title";
        public static final String COLUMN_NAME_COLOR = "color";
        public static final String[] ALL_COLUMNS = {_ID, COLUMN_NAME_TITLE, COLUMN_NAME_COLOR};
    }

    public static class TaskTagsEntry {
        public static final String TABLE_NAME = "todotag";
        public static final String COLUMN_NAME_TASK = "todo";
        public static final String COLUMN_NAME_TAG = "tag";
    }
}
