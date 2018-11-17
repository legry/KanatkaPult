package com.example.kanatkapult.WebSocketWorker;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;

import java.util.TimerTask;

class TimerNetworkInfo extends BroadcastReceiver {

    private Intent intent;
    private Context context;
    private TimeChecker timeChecker;
    private NetworkInfoListener infoListener;

    TimerNetworkInfo(Context context, NetworkInfoListener infoListener) {
        this.context = context;
        this.infoListener = infoListener;
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(WifiManager.NETWORK_STATE_CHANGED_ACTION);
        this.context.registerReceiver(this, intentFilter);
        timeChecker = new TimeChecker(new MyTask(this.infoListener));
    }

    public void unreg() {
        context.unregisterReceiver(this);
    }

    public NetworkInfo.State getState() {
        NetworkInfo info = intent.getParcelableExtra(WifiManager.EXTRA_NETWORK_INFO);
        return info.getState();
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        this.intent = intent;
        timeChecker.restartMyTimer(new MyTask(infoListener));
    }

    class MyTask extends TimerTask {

        private NetworkInfoListener infoListener;

        MyTask(NetworkInfoListener infoListener) {
            this.infoListener = infoListener;
        }

        @Override
        public void run() {
            infoListener.niListener(getState());
        }
    }
}
