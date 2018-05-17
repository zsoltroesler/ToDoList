package com.example.android.todolist.ui.main;

import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;
import android.arch.lifecycle.LiveData;
import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;

import com.example.android.todolist.R;
import com.example.android.todolist.database.AppDatabase;
import com.example.android.todolist.database.TaskEntry;
import com.example.android.todolist.utilities.ResourceProvider;

import java.util.List;

public class MainViewModel extends AndroidViewModel {

    // For logging
    private static final String TAG = MainViewModel.class.getSimpleName();

    private LiveData<List<TaskEntry>> tasks;

    private ResourceProvider resourceProvider = new ResourceProvider(getApplication());

    public MainViewModel(@NonNull Application application) {
        super(application);

        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(getApplication());
        String orderBy = sharedPrefs.getString(resourceProvider.getString(R.string.pref_key_order),
                resourceProvider.getString(R.string.pref_default_order));

        AppDatabase database = AppDatabase.getsInstance(this.getApplication());

        if (orderBy.equals(resourceProvider.getString(R.string.pref_value_chronological))) {
            tasks = database.taskDao().loadAllTasksByDate();
        } else {
            tasks = database.taskDao().loadAllTasksByPriority();
        }
    }

    public LiveData <List<TaskEntry>> getTasks() {
        return tasks;
    }


}
