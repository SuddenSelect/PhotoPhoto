package pl.edu.pw.mmajews2.photophoto.taking.tasks;

/**
 * Created by Maciej Majewski on 2016-05-03.
 */
public class AnimatedTransitionTask extends CameraTask {
    public AnimatedTransitionTask(CameraTaskContext context) {
        super(context);
    }

    @Override
    public void run() {
        context.getUiHandler().post(new Runnable() {
            @Override
            public void run() {
                context.getViewSwitcher().showNext();
            }
        });
        postNextTask();
    }
}
