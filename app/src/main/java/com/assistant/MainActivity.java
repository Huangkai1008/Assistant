package com.assistant;

import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends AppCompatActivity implements android.os.Handler.Callback{

    private final String TAG = MainActivity.class.getSimpleName();
    //循环取当前时刻的步数中间的间隔时间
    private long TIME_INTERVAL = 500;
    private TextView step_num;
    private TextView step_target_num;
    private TextView target_progress;
    private LinearLayout linearLayout_target;
    private Messenger messenger;
    private Messenger mGetReplyMessenger = new Messenger(new Handler(this));

    //数据库
    private StepDataDao stepDataDao;
    private String curSelDate;
    private ProgressSeek progress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        step_num=(TextView)findViewById(R.id.step_num);
        step_target_num=(TextView) findViewById(R.id.step_target_num);
        target_progress=(TextView) findViewById(R.id.target_progress);
        linearLayout_target=(LinearLayout)findViewById(R.id.setTarget);

        menu();

        /**
         * 目标进度条
         */
        progress = (ProgressSeek) findViewById(R.id.progress2);
        progress.init(0);

        curSelDate = TimeUtil.getCurrentDate();
        /**
         * 这里判断当前设备是否支持计步
         */
        if (StepCountCheckUtil.isSupportStepCountSensor(this)) {
            setupService();
        } else {
            Toast.makeText(this,"此设备不支持计步",Toast.LENGTH_LONG).show();
            Log.e("警告","此设备不支持计步");
        }
    }

    private boolean isBind = false;
    /**
     * 开启计步服务
     */
    private void setupService() {
        Intent intent = new Intent(this, StepService.class);
        isBind = bindService(intent, conn, Context.BIND_AUTO_CREATE);
        Log.e("状态","intent加载");
        startService(intent);
    }

    /**
     * 定时任务
     */
    private TimerTask timerTask;
    private Timer timer;
    /**
     * 用于查询应用服务（application Service）的状态的一种interface，
     * 更详细的信息可以参考Service 和 context.bindService()中的描述，
     * 和许多来自系统的回调方式一样，ServiceConnection的方法都是进程的主线程中调用的。
     */
    private ServiceConnection conn = new ServiceConnection() {

        /**
         * 在建立起于Service的连接时会调用该方法，目前Android是通过IBind机制实现与服务的连接。
         * @param name 实际所连接到的Service组件名称
         * @param service 服务的通信信道的IBind，可以通过Service访问对应服务
         */
        @Override
        public void onServiceConnected(ComponentName name, final IBinder service) {

            /**
             * 设置定时器，每个1秒钟去更新一次运动步数
             */
            timerTask = new TimerTask() {
                @Override
                public void run() {
                    try {
                        messenger = new Messenger(service);
                        Message msg = Message.obtain(null, Constant.MSG_FROM_CLIENT);
                        msg.replyTo = mGetReplyMessenger;
                        messenger.send(msg);
                        Log.e("数据",msg.getData().toString());
                    } catch (RemoteException e) {
                        e.printStackTrace();
                    }
                }
            };
            timer = new Timer();
            timer.schedule(timerTask, 0, 1000);
        }

        /**
         * 当与Service之间的连接丢失的时候会调用该方法，
         * 这种情况经常发生在Service所在的进程崩溃或者被Kill的时候调用，
         * 此方法不会移除与Service的连接，当服务重新启动的时候仍然会调用 onServiceConnected()。
         * @param name 丢失连接的组件名称
         */
        @Override
        public void onServiceDisconnected(ComponentName name) {

        }
    };

    @Override
    public boolean handleMessage(Message msg) {

        //获取当前时间
        String date = TimeUtil.getCurrentDate();
        int step_target=-1;
    /*    TargetEntity targetEntity=stepDataDao.getCurTargetByDate(date);
        if (targetEntity==null){
            step_target=-1;
        }else {
            step_target = Integer.parseInt(targetEntity.getTargetSteps());
        }*/
        switch (msg.what) {

            //这里用来获取到Service发来的数据
            case Constant.MSG_FROM_SERVER:

                //如果是今天则更新数据
                if (curSelDate.equals(TimeUtil.getCurrentDate())) {
                    //记录运动步数
                    int steps = msg.getData().getInt("steps");

                    //设置的步数
                    step_num.setText(String.valueOf(steps));
                    if (step_target>0){
                        if (step_target>steps){
                            int n=(steps/step_target)*100;
                            target_progress.setText(n+"");
                            progress = (ProgressSeek) findViewById(R.id.progress2);
                            progress.init(n);
                        }else {
                            target_progress.setText("100");
                            progress = (ProgressSeek) findViewById(R.id.progress2);
                            progress.init(100);
                        }
                    }else {
                        step_target_num.setText("未设置");
                        target_progress.setText("100");
                        progress = (ProgressSeek) findViewById(R.id.progress2);
                        progress.init(100);
                    }
                    //计算总公里数
                    //totalKmTv.setText(countTotalKM(steps));
                }

                break;
        }
        return false;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        //记得解绑Service，不然多次绑定Service会异常
        if (isBind) this.unbindService(conn);
    }

    public void menu(){
        linearLayout_target.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final EditText name = new EditText(MainActivity.this);
                new AlertDialog.Builder(MainActivity.this).setTitle("设置目标")
                        .setIcon(null)
                        .setNegativeButton("取消", null)
                        .setView(name).setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String target=name.getText().toString();
                        //获取当前时间
                        String date = TimeUtil.getCurrentDate();
                        TargetEntity targetEntity0=new TargetEntity();
                        targetEntity0.setCurDate(date);
                        targetEntity0.setTargetSteps(target);
                        TargetEntity targetEntity=null;//stepDataDao.getCurTargetByDate(date);
                        if (targetEntity!=null){
                            stepDataDao.updateCurTarget(targetEntity0);
                        }else {
                            stepDataDao.addNewTarget(targetEntity0);
                        }
                        Log.e("set",target+"");
                        step_target_num.setText(target);
                    }
                }).show();

            }
        });
    }

}
