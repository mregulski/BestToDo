package ppt.reshi.besttodo.view;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import ppt.reshi.besttodo.R;
import ppt.reshi.besttodo.model.Tag;
import ppt.reshi.besttodo.model.Task;
import ppt.reshi.besttodo.view.tags.TagListFragment;
import ppt.reshi.besttodo.view.tasks.TaskListActivity;
import ppt.reshi.besttodo.view.tasks.TaskListFragment;

public class MainActivity extends AppCompatActivity
    implements TagListFragment.OnTagSelected,
        TaskListFragment.OnTodoInteractionListener
{
    private final static String TAG = "MainActivity";
    private String mDefaultTag;

//    @IdRes private final int mContainerId = R.id.content;

    private TabLayout mNavigation;
    private ViewPager mPager;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getDefaultTag();
        mPager = (ViewPager) findViewById(R.id.pager);
        setupViewPager(mPager);
        mNavigation = (TabLayout) findViewById(R.id.navigation);
        mNavigation.setupWithViewPager(mPager);
//        mNavigation = (BottomNavigationView) findViewById(R.id.navigation);
//        mNavigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);

    }


    private void getDefaultTag() {
        SharedPreferences pref = getSharedPreferences("ppt.reshi.besttodo", MODE_PRIVATE);
        mDefaultTag = pref.getString("defaultTag", "todo");
    }

    private void setupViewPager(ViewPager viewPager) {
        PagerAdapter adapter = new MainViewPagerAdapter(getSupportFragmentManager());
        viewPager.setAdapter(adapter);

    }

    @Override
    public void onTagSelected(Tag tag) {
        Log.d(TAG, "Tag interaction: " + tag);
        Intent intent = new Intent(this, TaskListActivity.class);
        intent.putExtra(TaskListActivity.KEY_TAG, tag.getTitle());
        startActivity(intent);
    }

    @Override
    public void onTodoInteraction(Task task) {
        Log.d(TAG, "Task interaction: " + task);

    }

    private class MainViewPagerAdapter extends FragmentStatePagerAdapter {
        public MainViewPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            switch (position) {
                case 0: return TaskListAndDetailFragment.newInstance(mDefaultTag);
                case 1: return TagListFragment.newInstance();
                case 2: return getItem(0);
            }
            return null;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            switch (position) {
                case 0:
                    return "Default";
                case 1:
                    return "All lists";
                case 2:
                    return "Calendar";
            }
            return  "";
        }

        @Override
        public int getCount() {
            return 3;
        }
    }
}
