package com.example.kanatkapult.WebSocketWorker;

import java.util.Timer;
import java.util.TimerTask;

public class TimeChecker {

    private Timer timer;
    private TimerTask mytask;

    public TimeChecker(TimerTask mytask) {
        this.mytask = mytask;
        setMyTimer(this.mytask);
    }

    private void setMyTimer(TimerTask mytask) {
        timer = new Timer();
        timer.schedule(mytask, 500);
    }

    public void restartMyTimer(TimerTask mytask) {
        timer.cancel();
        this.mytask.cancel();
        this.mytask = mytask;
        setMyTimer(this.mytask);
    }


}
