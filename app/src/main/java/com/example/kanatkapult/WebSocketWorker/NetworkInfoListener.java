package com.example.kanatkapult.WebSocketWorker;

import android.net.NetworkInfo;

interface NetworkInfoListener {
    void niListener(NetworkInfo.State state);
}
