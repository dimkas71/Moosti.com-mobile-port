package com.moosti;

import android.os.CountDownTimer;
import android.os.SystemClock;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Chronometer;
import android.widget.TextView;
import android.widget.Toast;


public class MainActivity extends ActionBarActivity implements View.OnClickListener {

    private static final String TAG = MainActivity.class.getSimpleName();

    private CountDownTimer timer;

    private Button buttonFocus;
    private Button buttonShortBreak;
    private Button buttonLongBreak;
    private Button buttonStop;
    private TextView textView;
    private StringBuilder recycled;

    enum CHRONOSTATE {
        FOCUSED,
        SHORT_BREAKED,
        LONG_BREAKED,
        STOPPED
    }

    private CHRONOSTATE state;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        final Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setLogo(R.mipmap.ic_launcher);

        textView = (TextView)findViewById(R.id.textView);

        state = CHRONOSTATE.STOPPED;

        buttonFocus = (Button)findViewById(R.id.btn_focus);
        buttonFocus.setOnClickListener(this);

        buttonShortBreak = (Button)findViewById(R.id.btn_short_break);
        buttonShortBreak.setOnClickListener(this);

        buttonLongBreak = (Button)findViewById(R.id.btn_long_break);
        buttonLongBreak.setOnClickListener(this);

        buttonStop = (Button)findViewById(R.id.btn_stop);
        buttonStop.setOnClickListener(this);



    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);

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

    @Override
    public void onClick(View v) {
        final int id = v.getId();

        switch (id) {
            case R.id.btn_focus:

                state = CHRONOSTATE.FOCUSED;

                startTimer(30);

                break;
            case R.id.btn_short_break:

                state = CHRONOSTATE.SHORT_BREAKED;

                startTimer(5);


                break;
            case R.id.btn_long_break:

                state = CHRONOSTATE.LONG_BREAKED;

                startTimer(15);

                break;
            case R.id.btn_stop:

                state = CHRONOSTATE.STOPPED;

                stopTimer();

                break;

        }

    }

    private void startTimer(int minutesInFuture) {
        stopTimer();

        timer = new CountDownTimer(minutesInFuture * 60 * 1000, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                long seconds = millisUntilFinished / 1000;
                textView.setText(DateUtils.formatElapsedTime(recycled, seconds));
            }

            @Override
            public void onFinish() {
                textView.setText("00:00");
            }
        };
        timer.start();
    }

    private void stopTimer() {
        if (timer != null) {
            timer.cancel();
            textView.setText("00:00");
        }
    }


}
