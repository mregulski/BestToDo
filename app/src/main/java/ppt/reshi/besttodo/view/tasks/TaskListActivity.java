package ppt.reshi.besttodo.view.tasks;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import ppt.reshi.besttodo.R;
import ppt.reshi.besttodo.model.Task;
import ppt.reshi.besttodo.view.TaskListAndDetailFragment;
import ppt.reshi.besttodo.view.task.TaskDetailsActivity;

public class TaskListActivity extends AppCompatActivity implements TaskListFragment.OnTodoInteractionListener {
    public static final String KEY_TAG = "tag";
    private String mTag;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_task_list);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        mTag = getIntent().getStringExtra(KEY_TAG);
        setTitle(mTag);

        getSupportFragmentManager().beginTransaction()
                .replace(R.id.task_list_root, TaskListAndDetailFragment.newInstance(mTag))
        .commit();
    }

    @Override
    public void onTodoInteraction(Task task) {
        Intent intent = new Intent(this, TaskDetailsActivity.class);
        intent.putExtra(TaskDetailsActivity.KEY_TAG, mTag);
        intent.putExtra(TaskDetailsActivity.KEY_TASK, task);
        startActivity(intent);
    }
}
