package ppt.reshi.besttodo.view.task;

import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TimePicker;

import org.joda.time.DateTime;
import org.joda.time.LocalDate;
import org.joda.time.LocalTime;
import org.joda.time.format.DateTimeFormat;

import java.util.Arrays;
import java.util.Calendar;
import java.util.List;

import io.reactivex.Observable;
import ppt.reshi.besttodo.R;
import ppt.reshi.besttodo.model.Task;
import ppt.reshi.besttodo.model.db.TaskDataSource;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link OnTodoDetailsInteractionListener} interface
 * to handle interaction events.
 * Use the {@link TaskDetailsFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class TaskDetailsFragment extends Fragment implements
        DatePickerDialog.OnDateSetListener,
        TimePickerDialog.OnTimeSetListener {
    private final static String TAG = "TaskDetailsFragment";


    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_MAIN_TAG = "mainTag";
    private static final String ARG_TODO = "todo";
    private String mMainTag;

    private Task mEditedTask;
    private boolean mIsExistingTask;

    private EditText mDeadline;
    private DatePickerDialog mDatePicker;
    private TimePickerDialog mTimePicker;
    private EditText mTitle;
    private EditText mDescription;
    private EditText mTags;

    private LocalDate mDate;
    private LocalTime mTime;


    private OnTodoDetailsInteractionListener mListener;

    public TaskDetailsFragment() {
        // Required empty public constructor
    }


    public static TaskDetailsFragment newInstance(String mainTag,
                                                  Task task,
                                                  OnTodoDetailsInteractionListener listener) {
        TaskDetailsFragment fragment = new TaskDetailsFragment();
        fragment.mListener = listener;
        Bundle args = new Bundle();
        args.putString(ARG_MAIN_TAG, mainTag);
        if (task != null) {
            args.putSerializable(ARG_TODO, task);
        }
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mMainTag = getArguments().getString(ARG_MAIN_TAG);
            mEditedTask = (Task) getArguments().getSerializable(ARG_TODO);
            mIsExistingTask = mEditedTask != null;
        }
    }

    @SuppressLint("SetTextI18n")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_task_details, container, false);
        Calendar calendar = Calendar.getInstance();
        mDeadline = (EditText) view.findViewById(R.id.task_edit_deadline);
        mDatePicker = new DatePickerDialog(getContext(), TaskDetailsFragment.this, calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH));
        mTimePicker = new TimePickerDialog(getContext(), TaskDetailsFragment.this, calendar.get(Calendar.HOUR_OF_DAY),
                calendar.get(Calendar.MINUTE), true);
        mDeadline.setOnClickListener(v -> mDatePicker.show());
        mTitle = (EditText) view.findViewById(R.id.task_edit_title);
        mTags = (EditText) view.findViewById(R.id.task_edit_tags);

        mDescription = (EditText) view.findViewById(R.id.task_edit_description);
        view.findViewById(R.id.new_todo_save).setOnClickListener(v -> createTodo());
        if (mIsExistingTask) {
            populateFields(mEditedTask);
        } else {

            mTags.setText(mMainTag + ", ");
        }
        return view;
    }

    private void populateFields(Task task) {
        mTitle.setText(task.title());
        if (task.deadline() != null) {
            mDeadline.setText(task.formattedDeadline());
            mDate = task.deadline().toLocalDate();
            mTime = task.deadline().toLocalTime();
        }
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < task.tags().size(); i++) {
            sb.append(task.tags().get(i).getTitle());
            if (i < task.tags().size() - 1) {
                sb.append(",");
            }
        }
        mTags.setText(sb);
        mDescription.setText(task.description());

    }


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (mListener == null) {
            if (context instanceof OnTodoDetailsInteractionListener) {
                mListener = (OnTodoDetailsInteractionListener) context;
            } else {
                Log.w(TAG, "No listener registered at onAttach()");
//                throw new RuntimeException(context.toString()
//                        + " must implement OnTodoDetailsInteractionListener");
            }
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    @Override
    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
        // months are zero-indexed in java.util.Calendar, but one-indexed in joda
        mDate = new LocalDate(year, month+1, dayOfMonth);
        mTimePicker.show();
    }

    @Override
    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
        mTime = new LocalTime(hourOfDay, minute);
        mDeadline.setText(mDate.toLocalDateTime(mTime).toString(DateTimeFormat.longDateTime()));
    }

    private void createTodo() {
        if (mTitle.getText() == null || mTitle.getText().toString().equals("")) {
            // don't allow empty titles
            Snackbar.make(mTitle, "Title is missing", Snackbar.LENGTH_LONG).show();
            return;
        }
        List<String> tags = Arrays.asList(mTags.getText().toString().split(",( +)?"));
        Observable<Task> taskObservable;
        if (mIsExistingTask) {
            mEditedTask.title(mTitle.getText().toString())
                    .description(mDescription.getText().toString())
                    .deadline(assembleDeadline())
                    .tags(null);
            taskObservable = new TaskDataSource(getContext())
                    .updateTask(mEditedTask, tags);
        } else { // new task
            taskObservable = new TaskDataSource(getContext())
                    .createTask(mTitle.getText().toString(),
                        mDescription.getText().toString(),
                        assembleDeadline(),
                        tags);
        }
        taskObservable.subscribe(task -> {
            Log.d(TAG, "saved: " + task);
            mListener.onTaskSaved(task, mIsExistingTask);
        });
    }

    private DateTime assembleDeadline() {
        if (mDate != null && mTime != null) {
            return mDate.toDateTime(mTime);
        }
        return null;
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
    public interface OnTodoDetailsInteractionListener {
        // TODO: Update argument type and name
        void onTaskSaved(Task tod, boolean edit);
    }
}
