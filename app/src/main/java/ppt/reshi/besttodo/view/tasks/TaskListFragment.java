package ppt.reshi.besttodo.view.tasks;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ListView;

import java.util.ArrayList;

import ppt.reshi.besttodo.R;
import ppt.reshi.besttodo.model.Task;
import ppt.reshi.besttodo.model.db.TaskDataSource;
import ppt.reshi.besttodo.view.task.TaskDetailsActivity;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link OnTodoInteractionListener} interface
 * to handle interaction events.
 * Use the {@link TaskListFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class TaskListFragment extends Fragment
implements ListView.OnScrollListener, TaskListAdapter.OnTaskDoneListener {
    private final static String TAG = "TaskListFragment";
    private static final String ARG_TASKS_TAG = "tasksTag";

    private String tasksTag;

    private TaskDataSource mDataSource;
    private ListView mList;
    private TaskListAdapter mAdapter;

    private FloatingActionButton mNewTodoButton;
    private float mFabY;


    private OnTodoInteractionListener mListener;

    public TaskListFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param tag - show to-dos tagged with this tasksTag
     * @return A new instance of fragment TaskListFragment.
     */
    public static TaskListFragment newInstance(String tag) {
        TaskListFragment fragment = new TaskListFragment();
        Bundle args = new Bundle();
        args.putString(ARG_TASKS_TAG, tag);
        fragment.setArguments(args);
        return fragment;
    }

    public static TaskListFragment newInstance(String tag, OnTodoInteractionListener listener) {
        TaskListFragment fragment = new TaskListFragment();
        Bundle args = new Bundle();
        args.putString(ARG_TASKS_TAG, tag);
        fragment.setArguments(args);
        fragment.mListener = listener;
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            tasksTag = getArguments().getString(ARG_TASKS_TAG);
        }
        Log.d(TAG, "listing tasks for tag: '" + tasksTag + "'");

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_task_list, container, false);
        mNewTodoButton = (FloatingActionButton) view.findViewById(R.id.fab_new_todo);
        mNewTodoButton.setOnClickListener(v -> {
            Intent intent = new Intent(getContext(), TaskDetailsActivity.class);
            intent.putExtra(TaskDetailsActivity.KEY_TAG, tasksTag);
            startActivity(intent);
        });
        mList = (ListView) view.findViewById(R.id.list_tasks);
        mList.setOnItemClickListener((parent, clickedView, position, id) -> {
            Task task = mAdapter.getItem(position);
            Log.d(TAG, "receieved click for task id " + task.id());
            showDetails(task);
        });
        registerForContextMenu(mList);
        mList.setOnCreateContextMenuListener((menu, v, menuInfo) -> {
            AdapterView.AdapterContextMenuInfo acmi =
                    (AdapterView.AdapterContextMenuInfo) menuInfo;
            Log.d(TAG, "context menu requested: " + acmi.position);
            MenuInflater menuInflater = getActivity().getMenuInflater();
            menuInflater.inflate(R.menu.context_task, menu);
        });
        mList.setEmptyView(view.findViewById(R.id.list_tasks_empty));

        return view;
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        AdapterView.AdapterContextMenuInfo info =
                (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
        Log.d(TAG, "Context menu: selected " + info.position);
        Task taskToDelete = (Task) mList.getItemAtPosition(info.position);
        mDataSource.deleteTask(taskToDelete)
        .subscribe((result) -> {
            Snackbar.make(mList,
                    "Deleted task '" + taskToDelete.title() + "'",
                    Snackbar.LENGTH_SHORT)
                .show();
            mAdapter.delete(taskToDelete);

        });
        return true;
    }

    private void showDetails(Task task) {
        Log.d(TAG, "Requested details view for " + task);
        mListener.onTodoInteraction(task);
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.d(TAG, "onResume()");
        mDataSource = new TaskDataSource(getContext());

        mAdapter = new TaskListAdapter(getContext(), new ArrayList<>(), this);
        mList.setAdapter(mAdapter);
        mDataSource.getTasks(tasksTag)
                .subscribe(todos -> {
                    Log.d(TAG, "got todos: " + todos);
                    mAdapter.setData(todos);
                });
        mFabY = mNewTodoButton.getY();
        mList.setOnScrollListener(this);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (mListener == null) {
            if (context instanceof OnTodoInteractionListener) {
                mListener = (OnTodoInteractionListener) context;
            } else {
                throw new RuntimeException(context.toString()
                        + " must implement OnTagSelected");
            }
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onScrollStateChanged(AbsListView view, int scrollState) {
        // not used
    }



    @Override
    public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
        int lastVisible = mList.getLastVisiblePosition();
        View lastChild = mList.getChildAt(visibleItemCount - 1);
        if (lastChild == null) { return; }
        if (lastVisible == mList.getCount() - 1 && lastChild.getBottom() >= view.getBottom()) {

            // bottom of list
            hideNewTaskButton();
        } else {
            showNewTaskButton();
        }

    }

    private void showNewTaskButton() {
        if (mNewTodoButton.getY() == mFabY) {
            return;
        }

        mNewTodoButton.animate()
                .translationY(0)
                .scaleX(1.0f)
                .scaleY(1.0f)
                .setDuration(100);
    }

    private void hideNewTaskButton() {
        mNewTodoButton.animate()
                .scaleX(0.1f)
                .scaleY(0.1f)
                .setDuration(200)
                .translationY(50)
                .withEndAction(() -> mNewTodoButton
                        .animate()
                        .translationY(150)
                        .setDuration(100)
                );

    }

    @Override
    public void onTaskDone(Task task, boolean isDone, TaskListAdapter.OnTaskDoneListener.Callback callback) {
        Log.d(TAG, (isDone ? "done: " : "not done: ") + task);
        if (callback == null) {
            callback = (result) -> {};
        }
        mDataSource.markDone(task, isDone)
                .subscribe(callback::call);
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnTodoInteractionListener {
        // TODO: Update argument type and name
        void onTodoInteraction(Task task);
    }


}
