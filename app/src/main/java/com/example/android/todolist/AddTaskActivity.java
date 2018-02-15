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

package com.example.android.todolist;

import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.support.v4.content.CursorLoader;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.Toast;

import com.example.android.todolist.data.TaskContract;


public class AddTaskActivity extends AppCompatActivity implements
        LoaderManager.LoaderCallbacks<Cursor>{

    // Member variable to update already existing task
    private EditText mTaskDescription;

    // Member variable to keep track of a task's selected mPriority
    private int mPriority;

    // Member variable to decide it is a new entry or an already existing task
    private Uri mTaskUri;

    // Member variable for add/update button
    private Button mButton;

    // Identifier for task loader
    private static final int EXISTING_TASK_LOADER = 0;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_task);

        mTaskDescription = (EditText) findViewById(R.id.editTextTaskDescription);

        mButton = (Button) findViewById(R.id.addButton);

        // Examine the intent that was used to launch this activity,
        // in order to figure out if we're creating a new task or editing an existing one.
        mTaskUri = getIntent().getData();

        // If the intent does not contain a task content URI, then we know that we are
        // creating a new task.
        if(mTaskUri == null) {
            setTitle(getString(R.string.add_task_activity_name));
            mButton.setText(R.string.add_button);

            // Initialize to highest mPriority by default (mPriority = 1)
            ((RadioButton) findViewById(R.id.radButton1)).setChecked(true);
            mPriority = 1;

        } else {
            setTitle(getString(R.string.update_task_activity_name));
            mButton.setText(R.string.update_button);

            // Initialize a loader to read the task data from the database
            // and display the current values in the editor
            getSupportLoaderManager().initLoader(EXISTING_TASK_LOADER, null, this);
        }
    }

    @Override
    public Loader<Cursor> onCreateLoader(int loaderId, Bundle args) {
        // Define a projection that contain all columns from the tasks table
        String[] projection = {
                TaskContract.TaskEntry.COLUMN_DESCRIPTION,
                TaskContract.TaskEntry.COLUMN_PRIORITY};
        switch (loaderId) {
            case EXISTING_TASK_LOADER:
                return new CursorLoader(this,
                        mTaskUri,
                        projection,
                        null,
                        null,
                        null);
            default:
                throw new RuntimeException("Loader Not Implemented: " + loaderId);
        }
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        // Bail early if the cursor is null or there is less than 1 row in the cursor
        if (cursor == null || cursor.getCount() < 1) {
            return;
        }

        // Proceed with moving to the first row of the cursor and reading data from it
        // (This should be the only row in the cursor)
        if (cursor.moveToFirst()) {
            // Find the columns of pet attributes that we're interested in
            int taskColumnIndex = cursor.getColumnIndex(TaskContract.TaskEntry.COLUMN_DESCRIPTION);
            int priorityColumnIndex = cursor.getColumnIndex(TaskContract.TaskEntry.COLUMN_PRIORITY);

            // Extract out the value from the Cursor for the given column index
            String task = cursor.getString(taskColumnIndex);
            int priority = cursor.getInt(priorityColumnIndex);

            // Update the views on the screen with the values from the database
            mTaskDescription.setText(task);
            mPriority = priority;
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        // If the loader is invalidated, clear out all the data from the input fields.
        mTaskDescription.setText("");
        ((RadioButton) findViewById(R.id.radButton1)).setChecked(true);
        mPriority = 1;
    }

    /**
     * onClickAddTask is called when the "ADD" button is clicked.
     * It retrieves user input and inserts that new task data into the underlying database.
     */
    public void onClickAddTask(View view) {
        // Check if EditText is empty, if not retrieve input and store it in a ContentValues object
        // If the EditText input is empty -> don't create an entry
        String input = ((EditText) findViewById(R.id.editTextTaskDescription)).getText().toString();
        if (input.length() == 0) {
            Toast.makeText(this, R.string.task_message, Toast.LENGTH_SHORT).show();
            return;
        }

            // Create new empty ContentValues object
            ContentValues contentValues = new ContentValues();
            // Put the task description and selected mPriority into the ContentValues
            contentValues.put(TaskContract.TaskEntry.COLUMN_DESCRIPTION, input);
            contentValues.put(TaskContract.TaskEntry.COLUMN_PRIORITY, mPriority);

            // If this is a new entry insert the data into database
            if(mTaskUri == null) {
                // Insert the content values via a ContentResolver
                Uri uri = getContentResolver().insert(TaskContract.TaskEntry.CONTENT_URI, contentValues);
                Toast.makeText(this, R.string.task_added, Toast.LENGTH_SHORT).show();
            }
            // Update the corresponding task
            else {
                String selection = TaskContract.TaskEntry._ID + "=?";
                String[] selectionArgs = new String[] {String.valueOf(ContentUris.parseId(mTaskUri))};
                int taskUpdated = getContentResolver().update(mTaskUri,contentValues, selection, selectionArgs);
                Toast.makeText(this, R.string.task_updated, Toast.LENGTH_SHORT).show();
            }

        // Finish activity (this returns back to MainActivity)
        finish();
    }

    /**
     * onPrioritySelected is called whenever a priority button is clicked.
     * It changes the value of mPriority based on the selected button.
     */
    public void onPrioritySelected(View view) {
        if (((RadioButton) findViewById(R.id.radButton1)).isChecked()) {
            mPriority = 1;
        } else if (((RadioButton) findViewById(R.id.radButton2)).isChecked()) {
            mPriority = 2;
        } else if (((RadioButton) findViewById(R.id.radButton3)).isChecked()) {
            mPriority = 3;
        }
    }
}
