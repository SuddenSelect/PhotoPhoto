package pl.edu.pw.mmajews2.photophoto.taking.tasks;

/**
 * Created by Maciej Majewski on 2016-05-03.
 */
public class AnimatedSavingTask extends CameraTask {
    public AnimatedSavingTask(CameraTaskContext context) {
        super(context);
    }

    @Override
    public void run() {
        context.getUiHandler().post(new Runnable() {
            @Override
            public void run() {
//                context.getViewSwitcher().showNext();
                context.getSavingIndicatorView().setAlpha((float) 0.94);
                context.getSavingIndicatorView().startAnimation(context.getSavingAnimation());
            }
        });

        postNextTask();
    }
}
