package ppt.reshi.besttodo.view.task;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import ppt.reshi.besttodo.R;
import ppt.reshi.besttodo.model.Task;

/**
 * Wrapper for TaskDetailFragment for smaller screens, where it's displayed
 * on it's own rather than in a {@link ppt.reshi.besttodo.view.TaskListAndDetailFragment}
 */
public class TaskDetailsActivity extends AppCompatActivity
implements TaskDetailsFragment.OnTodoDetailsInteractionListener {

    private final static String TAG = "TaskDetailsActivity";

    public static final String KEY_TASK = "task";
    public static final String KEY_TAG = "tag";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_todo);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        Intent intent = getIntent();
        Task task = (Task) intent.getSerializableExtra(KEY_TASK);
        if (task != null) {
            Log.d(TAG, "Displaying an existing task: " + task);
            setTitle(task.title());
        } else {
            setTitle("New task");
        }
        String mainTag = intent.getStringExtra(KEY_TAG);
        getSupportFragmentManager().beginTransaction()
            .replace(R.id.activity_todo_details_root, TaskDetailsFragment.newInstance(mainTag, task, this))
        .commit();
    }


    @Override
    public void onTaskSaved(Task task, boolean edit) {
        finish();
    }
}
