package com.example.kanatkapult.WebSocketWorker;

public interface ChangeListener {
    void OnChangeListener(boolean isConnect);
    void OnDataReadListener(String data);
}
