package zladnrms.defytech.firsttcp;

import android.annotation.TargetApi;
import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.Build;
import android.os.CountDownTimer;
import android.os.IBinder;
import android.os.SystemClock;
import android.util.Log;

public class Service_get_message extends Service {

    private static final int MILLISINFUTURE = 1000*1000;
    private static final int COUNT_DOWN_INTERVAL = 1000;

    private CountDownTimer countDownTimer;

    public Service_get_message() {
    }

    @Override
    public void onCreate() {
        super.onCreate();

        unregisterRestartAlarm();
        initData();
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        startForeground(1,new Notification());

        /**
         * startForeground 를 사용하면 notification 을 보여주어야 하는데 없애기 위한 코드
         */
        NotificationManager nm = (NotificationManager)getSystemService(NOTIFICATION_SERVICE);
        Notification notification;

        notification = new Notification.Builder(getApplicationContext())
                .setContentTitle("")
                .setContentText("")
                .build();

        nm.notify(startId, notification);
        nm.cancel(startId);

        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        Log.i("Service_get_message" , "onDestroy" );
        countDownTimer.cancel();

        /**
         * 서비스 종료 시 알람 등록을 통해 서비스 재 실행
         */
        registerRestartAlarm();
    }

    /**
     * 데이터 초기화
     */
    private void initData(){


        countDownTimer();
        countDownTimer.start();
    }

    public void countDownTimer(){

        countDownTimer = new CountDownTimer(MILLISINFUTURE, COUNT_DOWN_INTERVAL) {
            public void onTick(long millisUntilFinished) {

                Log.i("PersistentService","onTick");
            }
            public void onFinish() {

                Log.i("PersistentService","onFinish");
            }
        };
    }

    /**
     * 알람 매니져에 서비스 등록
     */
    private void registerRestartAlarm(){

        Log.i("000 Service_get_message" , "registerRestartAlarm" );
        Intent intent = new Intent(Service_get_message.this,Brdcst_RestartService.class);
        intent.setAction("ACTION.RESTART.Service_get_message");
        PendingIntent sender = PendingIntent.getBroadcast(Service_get_message.this,0,intent,0);

        long firstTime = SystemClock.elapsedRealtime();
        firstTime += 1*1000;

        AlarmManager alarmManager = (AlarmManager)getSystemService(ALARM_SERVICE);

        /**
         * 알람 등록
         */
        alarmManager.setRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP,firstTime,1*1000,sender);

    }

    /**
     * 알람 매니져에 서비스 해제
     */
    private void unregisterRestartAlarm(){

        Log.i("000 Service_get_message" , "unregisterRestartAlarm" );

        Intent intent = new Intent(Service_get_message.this,Brdcst_RestartService.class);
        intent.setAction("ACTION.RESTART.Service_get_message");
        PendingIntent sender = PendingIntent.getBroadcast(Service_get_message.this,0,intent,0);

        AlarmManager alarmManager = (AlarmManager)getSystemService(ALARM_SERVICE);

        /**
         * 알람 취소
         */
        alarmManager.cancel(sender);
    }

    @Override
    public IBinder onBind(Intent intent) {
        throw new UnsupportedOperationException("Not yet implemented");
    }
}