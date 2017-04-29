package ppt.reshi.besttodo.view;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import ppt.reshi.besttodo.R;
import ppt.reshi.besttodo.model.Task;
import ppt.reshi.besttodo.view.task.TaskDetailsActivity;
import ppt.reshi.besttodo.view.task.TaskDetailsFragment;
import ppt.reshi.besttodo.view.tasks.TaskListFragment;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link TaskListAndDetailFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link TaskListAndDetailFragment#newInstance} factory method to
 * create an instance of this fragment.
 *
 * Holder fragment for To-do list fragment and details fragment on wider screens
 */
public class TaskListAndDetailFragment extends Fragment
implements TaskListFragment.OnTodoInteractionListener,
            TaskDetailsFragment.OnTodoDetailsInteractionListener {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private final static String TAG = "TaskListAndDetailFrag";
    private static final String ARG_TODOS_TAG = "tag";


    private String mTodosTag;


    private boolean mDoublePane;

    private OnFragmentInteractionListener mListener;

    public TaskListAndDetailFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param todosTag Parameter 1.
     * @return A new instance of fragment TaskListAndDetailFragment.
     */
    public static TaskListAndDetailFragment newInstance(String todosTag) {
        TaskListAndDetailFragment fragment = new TaskListAndDetailFragment();
        Bundle args = new Bundle();
        args.putString(ARG_TODOS_TAG, todosTag);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mTodosTag = getArguments().getString(ARG_TODOS_TAG);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_task_list_detail, container, false);
//        ViewGroup listContainer = (ViewGroup) view.findViewById(R.id.container_todo_details);
        ViewGroup detailsContainer = (ViewGroup) view.findViewById(R.id.container_todo_details);
        if (detailsContainer != null) {
            mDoublePane = true;
        }

        getChildFragmentManager().beginTransaction()
                .replace(R.id.container_todo_list, TaskListFragment.newInstance(mTodosTag, this))
        .commit();
        // if double pane: only create the details fragment when a task is selected
        return view;
    }


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    @Override
    public void onTodoInteraction(Task task) {
        if (mDoublePane) {
            getChildFragmentManager().beginTransaction()
                .replace(R.id.container_todo_details,
                        TaskDetailsFragment.newInstance(mTodosTag, task, this))
            .commit();
        } else {
            // open activity
            Intent intent = new Intent(getContext(), TaskDetailsActivity.class);
            intent.putExtra(TaskDetailsActivity.KEY_TASK, task);
            intent.putExtra(TaskDetailsActivity.KEY_TAG, mTodosTag);
            startActivity(intent);
        }
    }

    @Override
    public void onTaskSaved(Task task, boolean edit) {
        Log.d(TAG, "task saved: " + task);
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
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }
}
