package com.moosti;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TaskStackBuilder;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.MediaPlayer;
import android.os.CountDownTimer;
import android.os.Vibrator;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.format.DateUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;


public class MainActivity extends ActionBarActivity implements View.OnClickListener, SeekBar.OnSeekBarChangeListener {

    private static final String TAG = MainActivity.class.getSimpleName();
    public static final int VIBRATION_INTERVAL_IN_MILLIS = 2000;

    private CountDownTimer timer;

    private Button buttonFocus;
    private Button buttonShortBreak;
    private Button buttonLongBreak;
    private Button buttonStop;
    private TextView textView;
    private StringBuilder recycled;
    private MediaPlayer player;
    private SeekBar seekBarFocus;
    private SeekBar seekBarShortBreak;
    private SeekBar seekBarLongBreak;

    private int focusInFuture;
    private int shortBreakInFuture;
    private int longBreakInFuture;

    private static final String KEY_FOCUS_IN_FUTURE = "KEY_FOCUS_IN_FUTURE";
    private static final String KEY_STOPBREAK_IN_FUTURE = "KEY_STOPBREAK_IN_FUTURE";
    private static final String KEY_LONGBREAK_IN_FUTURE = "KEY_LONGBREAK_IN_FUTURE";

    private SharedPreferences preferences;



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


        preferences = getSharedPreferences("moosti_prefs", MODE_PRIVATE);

        focusInFuture = preferences.getInt(KEY_FOCUS_IN_FUTURE, 30);
        shortBreakInFuture = preferences.getInt(KEY_STOPBREAK_IN_FUTURE, 5);
        longBreakInFuture = preferences.getInt(KEY_LONGBREAK_IN_FUTURE, 20);


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

        player = MediaPlayer.create(this, R.raw.ring_end);


        initSeekBars();


    }

    private void initSeekBars() {

        seekBarFocus = (SeekBar) findViewById(R.id.seekBrFocus);
        seekBarFocus.setOnSeekBarChangeListener(this);
        seekBarFocus.setProgress(focusInFuture);
        buttonFocus.setText(getResources().getString(R.string.button_focus_text) + "(" + String.format("%d", seekBarFocus.getProgress()) + ")");


        seekBarShortBreak = (SeekBar) findViewById(R.id.seekBrShortBreak);
        seekBarShortBreak.setOnSeekBarChangeListener(this);
        seekBarShortBreak.setProgress(shortBreakInFuture);
        buttonShortBreak.setText(getResources().getString(R.string.button_short_break_text) + "(" + String.format("%d", seekBarShortBreak.getProgress()) + ")");


        seekBarLongBreak = (SeekBar) findViewById(R.id.seekBrLongBreak);
        seekBarLongBreak.setOnSeekBarChangeListener(this);
        seekBarLongBreak.setProgress(longBreakInFuture);
        buttonLongBreak.setText(getResources().getString(R.string.button_long_break_text) + "(" + String.format("%d", seekBarLongBreak.getProgress()) + ")");


    }

    @Override
    protected void onPause() {
        if (player != null) {
            if (player.isPlaying()) {
                player.stop();
            }
        }

        final SharedPreferences.Editor editor = preferences.edit();

        editor.putInt(KEY_FOCUS_IN_FUTURE, focusInFuture);
        editor.putInt(KEY_LONGBREAK_IN_FUTURE, longBreakInFuture);
        editor.putInt(KEY_STOPBREAK_IN_FUTURE, shortBreakInFuture);

        editor.commit();

        super.onPause();
    }

    @Override
    protected void onDestroy() {
        if (player != null) {
            player.stop();
        }
        super.onDestroy();
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

                startTimer(focusInFuture);

                break;
            case R.id.btn_short_break:

                state = CHRONOSTATE.SHORT_BREAKED;

                startTimer(shortBreakInFuture);


                break;
            case R.id.btn_long_break:

                state = CHRONOSTATE.LONG_BREAKED;

                startTimer(longBreakInFuture);

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

                sendNotification();
                vibrate();
                play();
                textView.setText("00:00");
            }
        };
        timer.start();
    }

    private void sendNotification() {

        final Intent intent = new Intent(this, MainActivity.class);

        final TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);

        stackBuilder.addParentStack(MainActivity.class);
        stackBuilder.addNextIntent(intent);


        PendingIntent pendIntent = stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);

        final Notification notification = new Notification.Builder(this)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle("Moosti.com: A task is finished")
                .setContentInfo("Last task was finished sucessefully")
                .setContentIntent(pendIntent)
                .build();

        final NotificationManager nm = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

        nm.notify(0, notification);

    }

    private void vibrate() {

        final Vibrator vibratorServise = (Vibrator)getSystemService(VIBRATOR_SERVICE);
        vibratorServise.vibrate(VIBRATION_INTERVAL_IN_MILLIS);

    }

    private void play() {
        player.start();
    }

    private void stopTimer() {
        if (timer != null) {
            timer.cancel();
            textView.setText("00:00");
        }
    }

    //Seek bars handlers

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

        final int id = seekBar.getId();

        switch (id) {
            case R.id.seekBrFocus:

                focusInFuture = progress;
                buttonFocus.setText(getResources().getString(R.string.button_focus_text) + "(" + String.format("%d", progress) + ")");

                break;
            case R.id.seekBrShortBreak:

                shortBreakInFuture = progress;
                buttonShortBreak.setText(getResources().getString(R.string.button_short_break_text) + "(" + String.format("%d", progress) + ")");

                break;
            case R.id.seekBrLongBreak:

                longBreakInFuture = progress;
                buttonLongBreak.setText(getResources().getString(R.string.button_long_break_text) + "(" + String.format("%d", progress) + ")");

                break;
        }
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {

    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {

    }


}
