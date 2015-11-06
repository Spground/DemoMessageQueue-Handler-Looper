package cn.edu.dlut.demomessagequeue_handler_looper;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.MessageQueue;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;

import java.lang.reflect.Field;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        init();
        Log.v("===TAG===","UI ThreadID is " + Thread.currentThread().getId());
        /**start up thread**/
        MyThread myThread = new MyThread();
        new Thread(myThread).start();

        //因为new Thread(myThread).start()是异步方法
        //避免start返回的时候run方法没有实例化Handler
        while(myThread.handler == null);

        Log.v("===TAG===", "handler is ready");
        /**send msg to target thread**/
        for(int i = 0 ; i < 10 ; i++){
            Message message = new Message();
            message.what = i;
            myThread.handler.sendMessage(message);
            try {
                Thread.currentThread().sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

    }

    /**init everything here**/
    private void init(){
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });
    }
    /**
     * MessageQueue空闲时候的回调
     */
    class MyIdleHandler implements MessageQueue.IdleHandler{
        private int count = 0;
        @Override
        public boolean queueIdle() {
            Log.v("===TAG===","count is " + (count++) + " at threadId is" + Thread.currentThread().getId());
            return true;
        }
    }

    /**
     * custom thread
     */
    class MyThread implements Runnable{
        public Handler handler;
        public MyThread(){
        }
        @Override
        public void run() {
            Log.v("===TAG===", "new threadID is " + Thread.currentThread().getId());

            Looper.prepare();
            //add IdleHandler
            Looper.myQueue().addIdleHandler(new MyIdleHandler());

            /**反射得到MessageQueue的mPtr的值**/
            MessageQueue queue = Looper.myQueue();
            Class clazz = queue.getClass();
            Field field = null;
            try {
                field = clazz.getDeclaredField("mPtr");
            } catch (NoSuchFieldException e) {
                e.printStackTrace();
            }
            if(field != null){
                field.setAccessible(true);
                try {
                    Log.v("===TAG==","mPtr is " + field.get(queue));
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
            }

            handler = new Handler(){
                @Override
                public void handleMessage(Message msg) {
                    Log.v("===TAG===","msg:" + msg.what + " at threadId is" + Thread.currentThread().getId());
                }

            };
            Looper.loop();
        }
    }
}
