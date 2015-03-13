package cl.snatch.snatch.activities;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.GestureDetector;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.ViewFlipper;

import cl.snatch.snatch.R;
import cl.snatch.snatch.helpers.IndicatorViewFlipper;

public class TutorialActivity extends ActionBarActivity {

    private IndicatorViewFlipper mViewFlipper;
    private GestureDetector mGestureDetector;

    int[] resources = {
            R.drawable.tutorial_1,
            R.drawable.tutorial_2,
            R.drawable.tutorial_3,
            R.drawable.tutorial_4
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tutorial);

        SharedPreferences sharedPref = getSharedPreferences("p", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putBoolean("tutorial", true);
        editor.apply();

        // Get the ViewFlipper
        mViewFlipper = (IndicatorViewFlipper) findViewById(R.id.pager);

        // Add all the images to the ViewFlipper
        for (int resource : resources) {
            ImageView imageView = new ImageView(this);
            imageView.setImageResource(resource);
            imageView.setScaleType(ImageView.ScaleType.FIT_CENTER);
            mViewFlipper.addView(imageView);
        }

        // Set in/out flipping animations
        mViewFlipper.setInAnimation(this, android.R.anim.fade_in);
        mViewFlipper.setOutAnimation(this, android.R.anim.fade_out);

        CustomGestureDetector customGestureDetector = new CustomGestureDetector();
        mGestureDetector = new GestureDetector(this, customGestureDetector);

        findViewById(R.id.next).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(TutorialActivity.this, LoginActivity.class);
                startActivity(intent);
                finish();
            }
        });
    }

    class CustomGestureDetector extends GestureDetector.SimpleOnGestureListener {
        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {

            // Swipe left (next)
            if (e1.getX() > e2.getX()) {
                mViewFlipper.setInAnimation(TutorialActivity.this, R.anim.left_in);
                mViewFlipper.setOutAnimation(TutorialActivity.this, R.anim.left_out);

                mViewFlipper.showNext();
            }

            // Swipe right (previous)
            if (e1.getX() < e2.getX()) {
                mViewFlipper.setInAnimation(TutorialActivity.this, R.anim.right_in);
                mViewFlipper.setOutAnimation(TutorialActivity.this, R.anim.right_out);

                mViewFlipper.showPrevious();
            }

            return super.onFling(e1, e2, velocityX, velocityY);
        }
    }


    @Override
    public boolean onTouchEvent(MotionEvent event) {
        mGestureDetector.onTouchEvent(event);

        return super.onTouchEvent(event);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_tutorial, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
