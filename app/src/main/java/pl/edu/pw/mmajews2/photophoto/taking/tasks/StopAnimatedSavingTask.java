package pl.edu.pw.mmajews2.photophoto.taking.tasks;

/**
 * Created by Maciej Majewski on 2016-05-03.
 */
public class StopAnimatedSavingTask extends CameraTask {
    public StopAnimatedSavingTask(CameraTaskContext context) {
        super(context);
    }

    @Override
    public void run() {
        context.getUiHandler().post(new Runnable() {
            @Override
            public void run() {
                context.getSavingIndicatorView().setAlpha((float) 0.0);
                context.getSavingIndicatorView().startAnimation(context.getSavingAnimation());
            }
        });

        postNextTask();
    }
}
