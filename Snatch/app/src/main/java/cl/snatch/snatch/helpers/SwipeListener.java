package cl.snatch.snatch.helpers;

import android.view.GestureDetector;
import android.view.MotionEvent;

public class SwipeListener extends GestureDetector.SimpleOnGestureListener {

    private static final int SWIPE_MIN_DISTANCE = 120;
    private static final int SWIPE_MAX_OFF_PATH = 250;
    private static final int SWIPE_THRESHOLD_VELOCITY = 200;

    private SwipeHandler handler;

    public SwipeHandler getHandler() {
        return handler;
    }

    public void setHandler(SwipeHandler handler) {
        this.handler = handler;
    }

    public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {

        if (Math.abs(e1.getY() - e2.getY()) > SWIPE_MAX_OFF_PATH)
            return false;

        // left
        if (e1.getX() - e2.getX() > SWIPE_MIN_DISTANCE
                && Math.abs(velocityX) > SWIPE_THRESHOLD_VELOCITY) {

            if (handler != null) {
                handler.onLeft();

                return true;
            }

            // right
        } else if (e2.getX() - e1.getX() > SWIPE_MIN_DISTANCE
                && Math.abs(velocityX) > SWIPE_THRESHOLD_VELOCITY) {

            if (handler != null) {
                handler.onRight();

                return true;
            }
        }

        return super.onFling(e1, e2, velocityX, velocityY);
    }

    public static abstract class SwipeHandler{
        public void onLeft() {}
        public void onRight() {}
    }
}