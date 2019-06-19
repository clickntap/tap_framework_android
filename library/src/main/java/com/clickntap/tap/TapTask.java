package com.clickntap.tap;

import android.content.Context;
import android.os.AsyncTask;


public class TapTask extends AsyncTask {
    private Context context;
    private Task task;

    public TapTask(Task task) {
        this.task = task;
        this.context = null;
    }

    public TapTask(Task task, Context context) {
        this.task = task;
        this.context = context;
    }

    protected Object doInBackground(Object[] objects) {
        try {
            task.exec();
        } catch (Exception e) {
            TapUtils.log(e);
        }
        return null;
    }

    public interface Task {
        void exec() throws Exception;
    }
}
