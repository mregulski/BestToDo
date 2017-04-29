package ppt.reshi.besttodo.view.tasks;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;

import org.joda.time.format.DateTimeFormat;

import java.util.List;

import ppt.reshi.besttodo.R;
import ppt.reshi.besttodo.model.Task;

/**
 * Created by Marcin Regulski on 26.04.2017.
 */

public class TaskListAdapter extends ArrayAdapter<Task> {

    OnTaskDoneListener mListener;
    List<Task> mData;
    public TaskListAdapter(Context context, List<Task> tasks, OnTaskDoneListener listener) {
        super(context, 0, tasks);
        mListener = listener;
        mData = tasks;
    }

    public void setData(List<Task> newItems) {
        clear();
        if (newItems != null) {
            addAll(newItems);
            mData = newItems;
        }
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        ViewHolder holder;
        if (convertView == null) {
            LayoutInflater inflater = LayoutInflater.from(getContext());
            convertView = inflater.inflate(R.layout.task_item, parent, false);
            holder = new ViewHolder();
            holder.title = (TextView) convertView.findViewById(R.id.task_title);
            holder.deadline = (TextView) convertView.findViewById(R.id.task_deadline);
            holder.description = (TextView) convertView.findViewById(R.id.task_description);
            holder.isDone = (CheckBox) convertView.findViewById(R.id.task_checkbox);

            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        Task task = getItem(position);

        CompoundButton.OnCheckedChangeListener listener = (buttonView, isChecked) -> {
            mListener.onTaskDone(task, isChecked, (updated) -> {
                setColors(holder, updated);
            });
        };
        holder.title.setText(task.title());
        holder.isDone.setOnCheckedChangeListener(null);
        holder.isDone.setChecked(task.done());
        holder.isDone.setOnCheckedChangeListener(listener);

        if (task.deadline() == null) {
            holder.deadline.setVisibility(View.GONE);
        } else {
            holder.deadline.setText(task.deadline().toString(DateTimeFormat.longDateTime()));
            if (task.deadline().isBeforeNow()) {
                holder.title.setCompoundDrawablesWithIntrinsicBounds(
                        getContext().getDrawable(R.drawable.ic_error_red_24dp),
                        null, null, null);
            holder.deadline.setVisibility(View.VISIBLE);
            }
        }
        String description = task.description();
        if (description.equals("")) {
            holder.description.setVisibility(View.GONE);
        } else {
//            holder.description.setText(description.length() > 31 ? description.substring(0, 31) : description);
            holder.description.setText(description);
            holder.description.setVisibility(View.VISIBLE);
        }

        return convertView;
    }

    private void setColors(ViewHolder holder, Task updated) {
        holder.title.setText((updated.done() ? "done: " : "waiting: ") + updated.title());
    }

    public void delete(Task taskToDelete) {
        boolean anyDeleted = mData.removeIf((task) -> task.id().equals(taskToDelete.id()));
        if (anyDeleted) {
            setData(mData);
        }
    }


    static class ViewHolder {
        public TextView title;
        public TextView deadline;
        public TextView description;
        public CheckBox isDone;
    }

    interface OnTaskDoneListener {
        void onTaskDone(Task task, boolean isDone, Callback callback);
        interface Callback {
            void call(Task task);
        }
    }
}
