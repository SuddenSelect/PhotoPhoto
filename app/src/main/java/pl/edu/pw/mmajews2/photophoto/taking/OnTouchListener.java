package pl.edu.pw.mmajews2.photophoto.taking;

import android.view.MotionEvent;
import android.view.View;

/**
 * Created by Maciej Majewski on 2016-05-01.
 */
public class OnTouchListener implements View.OnTouchListener {
    private float x;
    private float y;
    private OnTouchCallback callback;

    public OnTouchListener(OnTouchCallback callback) {
        this.callback = callback;
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        if(event.getAction() == MotionEvent.ACTION_DOWN){
            x = event.getX();
            y = event.getY();
        }

        if(event.getAction() == MotionEvent.ACTION_UP){
            float xDiff = Math.abs(event.getX() - x);
            float yDiff = Math.abs(event.getY() - y);
            if(xDiff < event.getXPrecision()*10 && yDiff < event.getYPrecision()*10) {
                callback.onTouched();
            }
        }
        return true;
    }
}
