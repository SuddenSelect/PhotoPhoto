package pl.edu.pw.mmajews2.photophoto.taking.tasks;

import android.util.Log;

/**
 * Created by Maciej Majewski on 2016-05-02.
 */
public abstract class CameraTask implements Runnable {
    protected static final String TAG = "PhotoPhoto-CameraTask";

    private Runnable nextTask = null;
    protected final CameraTaskContext context;

    public CameraTask(CameraTaskContext context){
        this.context = context;
    }

    public void setNextTask(Runnable nextTask) {
        this.nextTask = nextTask;
    }

    protected void postNextTask(){
        Log.d(TAG, "NextTask: "+nextTask);
        if(nextTask != null){
            context.getTaskHandler().post(nextTask);
        }
    }

    public Runnable getNextTask() {
        return nextTask;
    }

}
