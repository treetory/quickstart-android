package com.google.firebase.quickstart.fcm.java;

import android.content.Context;
import androidx.annotation.NonNull;
import android.util.Log;
import android.widget.Toast;

import androidx.work.Data;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

public class MyWorker extends Worker {

    private static final String TAG = "MyWorker";

    public MyWorker(@NonNull Context appContext, @NonNull WorkerParameters workerParams) {
        super(appContext, workerParams);
    }

    @NonNull
    @Override
    public Result doWork() {
        Log.d(TAG, "Performing long running task in scheduled job");
        // TODO(developer): add long running task here.

        String title = getInputData().getString("title");
        Log.d(TAG, String.format("찍히는지 모르겠습니다. >> %s", title));

        String body = getInputData().getString("body");
        Log.d(TAG, String.format("찍히는지 모르겠습니다. >> %s", body));

        Data output = new Data.Builder()
                .putString("title", title)
                .putString("body", body)
                .build();

        return Result.success(output);
    }

}
