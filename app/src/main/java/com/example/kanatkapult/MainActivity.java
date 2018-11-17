package com.example.kanatkapult;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.example.kanatkapult.WebSocketWorker.ChangeListener;
import com.example.kanatkapult.WebSocketWorker.WebSocketClient;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends AppCompatActivity implements View.OnTouchListener,
        ChangeListener, View.OnLongClickListener {

    private WebSocketClient blCn;
    private TextView amp, ustamp;
    private RecyclerAdapter recyclerAdapter;
    private int ust;
    private int j, amper = 0;
    private boolean regust = false, reg = false;
    private ImageButton minus, plus;
    private RecyclerView recyclerView;
    private Drawable[] drawables;
    private String[] cmnds;
    private int amps;
    private String data;
    private boolean isConnected;

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        amp = findViewById(R.id.amp);
        amp.setOnLongClickListener(this);
        ustamp = findViewById(R.id.ust);
        minus = findViewById(R.id.minus);
        plus = findViewById(R.id.plus);
        minus.setOnTouchListener(this);
        plus.setOnTouchListener(this);
        recyclerView = findViewById(R.id.recycler);
        recyclerView.setHasFixedSize(true);
        GridLayoutManager mLayoutManager = new GridLayoutManager(this, 3);
        recyclerView.setLayoutManager(mLayoutManager);
        cmnds = getResources().getStringArray(R.array.cmnds);
        drawables = new Drawable[9];
        drawables[0] = getResources().getDrawable(R.drawable.v_l_selector);
        drawables[1] = getResources().getDrawable(R.drawable.v_r_selector);
        drawables[2] = getResources().getDrawable(R.drawable.v_s_selector);
        drawables[3] = getResources().getDrawable(R.drawable.a_m_selector);
        drawables[4] = getResources().getDrawable(R.drawable.h_m_selector);
        drawables[5] = getResources().getDrawable(R.drawable.a_s_selector);
        drawables[6] = getResources().getDrawable(R.drawable.h_b_selector);
        drawables[7] = getResources().getDrawable(R.drawable.h_n_selector);
        drawables[8] = getResources().getDrawable(R.drawable.h_f_selector);

        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            String age = extras.getString("mytext");
            Toast.makeText(this, age, Toast.LENGTH_SHORT).show();
        }
        BluthCrt();
        Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (reg) {
                            if (regust) {
                                if (ust < 80) {
                                    ust += 1;
                                }
                            } else {
                                if (ust > 10) {
                                    ust -= 1;
                                }
                            }
                            blCn.WriteData("<ust" + ust + ">");
                            ustamp.setText(String.valueOf(ust));
                        }
                    }
                });
            }
        }, 300, 500);
    }

    @Override
    protected void onDestroy() {
        blCn.unreg();
        super.onDestroy();
    }

    @Override
    protected void onResume() {
        super.onResume();
        blCn.wsConnect();
        Log.i("myws", "resume");
    }

    @Override
    protected void onStop() {
        blCn.wsDisconnect();
        Log.i("myws", "stop");
        super.onStop();
    }

    void BluthCrt() {
        blCn = new WebSocketClient(MainActivity.this, this);
        recyclerAdapter = new RecyclerAdapter(drawables, cmnds, blCn, new AttachedDeatachedListener() {
            @Override
            public void onAttached() {
                if (isConnected) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            updateRecycler();
                        }
                    });
                }
            }

            @Override
            public void onDetached() {

            }
        });
        recyclerView.setAdapter(recyclerAdapter);
    }

    void dialogCreator() {
        final EditText edittext = new EditText(MainActivity.this);
        AlertDialog.Builder alert = new AlertDialog.Builder(MainActivity.this);
        alert.setMessage("Введите значение");
        alert.setTitle("Калибровка тока");

        alert.setView(edittext);

        alert.setPositiveButton("Скалибровать", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                int kal = Integer.parseInt(edittext.getText().toString());
                JSONArray jsonArray = new JSONArray();
                jsonArray.put(kal);
                jsonArray.put(amps);
                blCn.WriteData("json" + jsonArray.toString());

            }
        });

        alert.setNegativeButton("Отмена", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                // what ever you want to do with No option.
            }
        });
        alert.show();
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public boolean onTouch(View view, MotionEvent motionEvent) {
        if (MotionEvent.ACTION_DOWN == motionEvent.getActionMasked()) {
            switch (view.getId()) {
                case R.id.plus:
                    regust = true;
                    break;
                case R.id.minus:
                    regust = false;
                    break;
            }
            reg = true;
        }
        if (MotionEvent.ACTION_UP == motionEvent.getActionMasked()) {
            reg = false;
        }
        if (MotionEvent.ACTION_CANCEL == motionEvent.getActionMasked()) {
            reg = false;
        }
        return false;
    }

    @Override
    public void OnChangeListener(final boolean isConnect) {
        isConnected = isConnect;
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (isConnect) {
                    minus.setVisibility(View.VISIBLE);
                    plus.setVisibility(View.VISIBLE);
                    ustamp.setVisibility(View.VISIBLE);
                    amp.setVisibility(View.VISIBLE);
                } else {
                    for (int i = 0; i < 9; i++) {
                        recyclerAdapter.getImageButtons()[i].setVisibility(View.INVISIBLE);
                    }
                    minus.setVisibility(View.INVISIBLE);
                    plus.setVisibility(View.INVISIBLE);
                    ustamp.setVisibility(View.INVISIBLE);
                    amp.setVisibility(View.INVISIBLE);
                }
            }
        });
    }

    void updateRecycler() {
        try {
            JSONArray jsonArray = new JSONArray(data);
            boolean st;
            st = (boolean) jsonArray.get(0);
            if (st) {
                recyclerAdapter.getImageButtons()[0].setSelected(false);
                recyclerAdapter.getImageButtons()[1].setSelected(true);
            } else {
                recyclerAdapter.getImageButtons()[1].setSelected(false);
                recyclerAdapter.getImageButtons()[0].setSelected(true);
            }
            recyclerAdapter.getImageButtons()[0].setVisibility(View.VISIBLE);
            recyclerAdapter.getImageButtons()[1].setVisibility(View.VISIBLE);
            st = (boolean) jsonArray.get(1);
            recyclerAdapter.getImageButtons()[2].setSelected(st);
            recyclerAdapter.getImageButtons()[2].setVisibility(View.VISIBLE);
            st = (boolean) jsonArray.get(2);
            if (st) {
                recyclerAdapter.getImageButtons()[3].setSelected(false);
                recyclerAdapter.getImageButtons()[4].setSelected(true);
            } else {
                recyclerAdapter.getImageButtons()[4].setSelected(false);
                recyclerAdapter.getImageButtons()[3].setSelected(true);
            }
            recyclerAdapter.getImageButtons()[3].setVisibility(View.VISIBLE);
            recyclerAdapter.getImageButtons()[4].setVisibility(View.VISIBLE);
            st = (boolean) jsonArray.get(3);
            recyclerAdapter.getImageButtons()[5].setSelected(!st);
            for (int i = 0; i < 9; i++) {
                if (i != 5) {
                    recyclerAdapter.getImageButtons()[i].setEnabled(st);
                }
            }
            recyclerAdapter.getImageButtons()[5].setVisibility(View.VISIBLE);
            st = (boolean) jsonArray.get(4);
            if (st) {
                recyclerAdapter.getImageButtons()[6].setSelected(true);
                recyclerAdapter.getImageButtons()[8].setEnabled(false);
                recyclerAdapter.getImageButtons()[8].setSelected(false);
                recyclerAdapter.getImageButtons()[7].setSelected(false);
            } else {
                st = (boolean) jsonArray.get(5);
                if (st) {
                    recyclerAdapter.getImageButtons()[8].setSelected(true);
                    recyclerAdapter.getImageButtons()[6].setEnabled(false);
                    recyclerAdapter.getImageButtons()[6].setSelected(false);
                    recyclerAdapter.getImageButtons()[7].setSelected(false);
                } else {
                    recyclerAdapter.getImageButtons()[7].setSelected(true);
                    recyclerAdapter.getImageButtons()[6].setEnabled(true);
                    recyclerAdapter.getImageButtons()[8].setEnabled(true);
                    recyclerAdapter.getImageButtons()[6].setSelected(false);
                    recyclerAdapter.getImageButtons()[8].setSelected(false);
                }
            }
            recyclerAdapter.getImageButtons()[6].setVisibility(View.VISIBLE);
            recyclerAdapter.getImageButtons()[7].setVisibility(View.VISIBLE);
            recyclerAdapter.getImageButtons()[8].setVisibility(View.VISIBLE);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void OnDataReadListener(final String data) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (data.contains("[")) {
                    MainActivity.this.data = data;
                    updateRecycler();
                } else {
                    if (data.startsWith("<") && data.endsWith(">")) {
                        if (data.contains("ust")) {
                            String uststr = data.substring(4, data.length() - 1);
                            ustamp.setText(uststr);
                            ust = Integer.parseInt(uststr);
                        } else {
                            if (j < 5) {
                                j += 1;
                                amper += Integer.parseInt(data.substring(data.indexOf("<") + 1, data.lastIndexOf(">")));
                            }
                            if (j == 5) {
                                amps = Math.round(amper / 5);
                                amp.setText(String.valueOf(amps));
                                j = 0;
                                amper = 0;
                            }
                        }
                    }
                }
            }
        });
    }

    @Override
    public boolean onLongClick(View view) {
        dialogCreator();
        return true;
    }
}

