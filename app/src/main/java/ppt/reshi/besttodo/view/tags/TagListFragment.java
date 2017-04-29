package ppt.reshi.besttodo.view.tags;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import java.util.ArrayList;

import ppt.reshi.besttodo.R;
import ppt.reshi.besttodo.model.Tag;
import ppt.reshi.besttodo.model.db.TaskDataSource;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link OnTagSelected} interface
 * to handle interaction events.
 * Use the {@link TagListFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class TagListFragment extends Fragment {
    private final static String TAG = "TagListFragment";

    private TaskDataSource mDataSource;
    private ListView mList;
    private TagsAdapter mAdapter;

    private OnTagSelected mListener;

    public TagListFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment TagListFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static TagListFragment newInstance() {
        TagListFragment fragment = new TagListFragment();
        Bundle args = new Bundle();

        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate()");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        Log.d(TAG, "onCreateView()");
        View view = inflater.inflate(R.layout.fragment_tag_list, container, false);
        mList = (ListView) view.findViewById(R.id.list_tags);
        mList.setOnItemClickListener((parent, clickedView, position, id) ->
                mListener.onTagSelected(mAdapter.getItem(position)));
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.d(TAG, "onResume()");
        mDataSource = new TaskDataSource(getContext());

        mAdapter = new TagsAdapter(getContext(), new ArrayList<>());
        mList.setAdapter(mAdapter);
        mDataSource.getTags()
                .subscribe(tags -> {
                    Log.d(TAG, "Got tags: " + tags);
                    mAdapter.replaceDataWith(tags);
                });
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnTagSelected) {
            mListener = (OnTagSelected) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnTagSelected");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
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
    public interface OnTagSelected {
        // TODO: Update argument type and name
        void onTagSelected(Tag tag);
    }
}
