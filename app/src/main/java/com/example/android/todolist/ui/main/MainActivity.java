/*
 * Copyright (C) 2016 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example.android.todolist.ui.main;

import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.helper.ItemTouchHelper;

import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;

import com.example.android.todolist.AppExecutors;
import com.example.android.todolist.R;
import com.example.android.todolist.adapter.TaskAdapter;
import com.example.android.todolist.database.AppDatabase;
import com.example.android.todolist.database.TaskEntry;
import com.example.android.todolist.settings.SettingsActivity;
import com.example.android.todolist.ui.add.AddTaskActivity;
import com.example.android.todolist.utilities.SwipeToDeleteCallback;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

import static android.widget.LinearLayout.VERTICAL;


public class MainActivity extends AppCompatActivity implements TaskAdapter.ItemClickListener {

    private static final String TAG = MainActivity.class.getSimpleName();

    @BindView(R.id.coordinatorLayout)
    CoordinatorLayout mCoordinatorLayout;
    @BindView(R.id.recyclerViewTasks)
    RecyclerView mRecyclerView;
    @BindView(R.id.empty_list)
    LinearLayout mLinearLayout;
    @BindView(R.id.fab)
    FloatingActionButton mFab;

    private TaskAdapter mAdapter;
    private AppDatabase mDb;
    private List<TaskEntry> mTasks;
    private TaskEntry mTask;
    private int recentlyDeletedTaskPosition;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mDb = AppDatabase.getsInstance(getApplicationContext());

        setUpViews();

        setupViewModel();

    }

    private void setupViewModel() {
        MainViewModel viewModel = ViewModelProviders.of(this).get(MainViewModel.class);
        viewModel.getTasks().observe(this, new Observer<List<TaskEntry>>() {
            @Override
            public void onChanged(@Nullable List<TaskEntry> taskEntries) {
                Log.d(TAG, "Updating list of tasks from LiveData in ViewModel");
                mTasks = taskEntries;
                mAdapter.setTasks(mTasks);
                if (mTasks == null || mTasks.isEmpty()) {
                    mLinearLayout.setVisibility(View.VISIBLE);
                } else {
                    mLinearLayout.setVisibility(View.INVISIBLE);
                }
            }
        });
    }

    private void setUpViews() {
        ButterKnife.bind(this);

        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        mAdapter = new TaskAdapter(this, this);
        mRecyclerView.setAdapter(mAdapter);

        DividerItemDecoration decoration = new DividerItemDecoration(getApplicationContext(), VERTICAL);
        mRecyclerView.addItemDecoration(decoration);

        swipeToDelete();
    }

    @OnClick(R.id.fab)
    public void startAddActivity() {
        Intent addTaskIntent = new Intent(MainActivity.this, AddTaskActivity.class);
        startActivity(addTaskIntent);
    }

    private void swipeToDelete() {
        SwipeToDeleteCallback swipeToDeleteCallback = new SwipeToDeleteCallback(this) {
            @Override
            public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {
                // With swiping the task removed from the list but not deleted from the db yet
                int position = viewHolder.getAdapterPosition();
                mTasks = mAdapter.getTasks();
                mTask = mTasks.get(position);
                recentlyDeletedTaskPosition = position;
                mTasks.remove(position);
                mAdapter.notifyItemRemoved(recentlyDeletedTaskPosition);
                showUndoSnackbar();
            }
        };

        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(swipeToDeleteCallback);
        itemTouchHelper.attachToRecyclerView(mRecyclerView);
    }

    private void showUndoSnackbar() {
        Snackbar snackbar = Snackbar.make(mCoordinatorLayout, R.string.delete_snackbar, Snackbar.LENGTH_LONG);
        snackbar.setAction(R.string.delete_undo, new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Item would be restored into the list by pressing "UNDO"
                undoDelete();
            }
        }).addCallback(new Snackbar.Callback() {
            @Override
            public void onDismissed(Snackbar transientBottomBar, int event) {
                super.onDismissed(transientBottomBar, event);
                // Item will ultimately be deleted from db
                if (event != DISMISS_EVENT_ACTION) {
                    AppExecutors.getInstance().diskIO().execute(new Runnable() {
                        @Override
                        public void run() {
                            mDb.taskDao().deleteTask(mTask);
                        }
                    });
                }
            }
        });
        snackbar.setActionTextColor(Color.parseColor("#609BBC"));
        snackbar.show();
    }

    private void undoDelete() {
        mTasks.add(recentlyDeletedTaskPosition, mTask);
        mAdapter.notifyItemInserted(recentlyDeletedTaskPosition);
    }

    @Override
    public void onItemClickListener(int itemId) {
        // Launch AddTaskActivity adding the itemId as an extra in the intent
        Intent intent = new Intent(MainActivity.this, AddTaskActivity.class);
        intent.putExtra(AddTaskActivity.EXTRA_TASK_ID, itemId);
        startActivity(intent);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.todolist_menu, menu);

        SearchView searchView = (SearchView) menu.findItem(R.id.action_search)
                .getActionView();

        searchView.setSubmitButtonEnabled(true);
        searchView.setOnQueryTextListener(onQueryTextListener);

        return super.onCreateOptionsMenu(menu);
    }

    private SearchView.OnQueryTextListener onQueryTextListener =
            new SearchView.OnQueryTextListener() {
                @Override
                public boolean onQueryTextSubmit(String query) {
                    getTasksFromDb(query);
                    return true;
                }

                @Override
                public boolean onQueryTextChange(String newText) {
                    getTasksFromDb(newText);
                    return true;
                }

                private void getTasksFromDb(String searchText) {
                    searchText = "%" + searchText + "%";
                    MainViewModel viewModel = ViewModelProviders.of(MainActivity.this).get(MainViewModel.class);
                    viewModel.getTasksByQuery(searchText).observe(MainActivity.this, new Observer<List<TaskEntry>>() {
                        @Override
                        public void onChanged(@Nullable List<TaskEntry> taskEntries) {
                            Log.d(TAG, "Updating list of tasks from LiveData in ViewModel");
                            if (taskEntries == null) {
                                return;
                            }
                            mTasks = taskEntries;
                            mAdapter.setTasks(mTasks);
                        }
                    });
                }
            };

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case R.id.action_settings:
                Intent startSettingsActivity = new Intent(this, SettingsActivity.class);
                startActivity(startSettingsActivity);
                break;
            case R.id.action_delete:
                showDeleteConfirmationDialog();
                break;

        }

        return super.onOptionsItemSelected(item);
    }

    private void showDeleteConfirmationDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.delete_confirmation_msg);
        builder.setPositiveButton(R.string.delete_yes, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                if (mTasks == null || mTasks.isEmpty()) {
                    dialog.dismiss();
                } else {
                    AppExecutors.getInstance().diskIO().execute(new Runnable() {
                        @Override
                        public void run() {
                            mDb.taskDao().deleteAllTasks();
                        }
                    });
                }
            }
        });
        builder.setNegativeButton(R.string.delete_no, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        });

        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }
}