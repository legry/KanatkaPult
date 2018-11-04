package com.example.kanatkapult;

import android.graphics.drawable.Drawable;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;

import com.example.kanatkapult.WebSocketWorker.WebSocketClient;

class RecyclerAdapter extends RecyclerView.Adapter<RecyclerAdapter.MyHolder> {
    private Drawable[] drawables;
    private String[] cmnds;
    private WebSocketClient bluetoothConnect;
    private ImageButton[] imageButtons = new ImageButton[9];
    private int valan_start = 2;
    private int valan_left = 0;
    private int valan_right = 1;
    private int aut_mode = 3;
    private int hand_mode = 4;
    private int alarm_stop = 5;
    private int hod_back = 6;
    private int hod_not = 7;
    private int hod_forward = 8;

    ImageButton[] getImageButtons() {
        return imageButtons;
    }

    RecyclerAdapter(Drawable[] drawables, String[] cmnds, WebSocketClient bluetoothConnect) {
        this.drawables = drawables;
        this.cmnds = cmnds;
        this.bluetoothConnect = bluetoothConnect;
    }

    @Override
    public MyHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new MyHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.btns, parent, false));
    }

    @Override
    public void onBindViewHolder(final MyHolder holder, final int position) {
        holder.imBtn.setImageDrawable(drawables[position]);
        imageButtons[position] = holder.imBtn;
        imageButtons[position].setVisibility(View.INVISIBLE);
        holder.imBtn.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                if (motionEvent.getAction() == MotionEvent.ACTION_DOWN) {
                    if (position == valan_left) {
                        holder.imBtn.setSelected(true);
                        imageButtons[valan_right].setSelected(false);
                        imageButtons[valan_start].setSelected(false);
                        bluetoothConnect.WriteData("<" + cmnds[position] + ">");
                    } else if (position == valan_right) {
                        holder.imBtn.setSelected(true);
                        imageButtons[valan_left].setSelected(false);
                        imageButtons[valan_start].setSelected(false);
                        bluetoothConnect.WriteData("<" + cmnds[position] + ">");
                    } else if (position == valan_start) {
                        holder.imBtn.setSelected(true);
                        bluetoothConnect.WriteData("<" + cmnds[valan_start] + ">");
                    } else if (position == aut_mode) {
                        holder.imBtn.setSelected(true);
                        imageButtons[hand_mode].setSelected(false);
                        bluetoothConnect.WriteData("<" + cmnds[position] + ">");
                    } else if (position == hand_mode) {
                        holder.imBtn.setSelected(true);
                        imageButtons[aut_mode].setSelected(false);
                        bluetoothConnect.WriteData("<" + cmnds[position] + ">");
                    } else if (position == alarm_stop) {
                        holder.imBtn.setSelected(!holder.imBtn.isSelected());
                        if (holder.imBtn.isSelected()) {
                            bluetoothConnect.WriteData("<" + (cmnds[alarm_stop]) + "0>");
                        } else {
                            bluetoothConnect.WriteData("<" + (cmnds[alarm_stop]) + "1>");
                        }
                        for (int i = 0; i < 9; i++) {
                            if (i != position) {
                                if (!(imageButtons[hod_back].isSelected() && (i == hod_forward)) &&
                                        !(imageButtons[hod_forward].isSelected() && (i == hod_back)))
                                imageButtons[i].setEnabled(!view.isSelected());
                            }
                        }
                        imageButtons[valan_start].setSelected(false);
                    } else if (position == hod_back) {
                        holder.imBtn.setSelected(true);
                        imageButtons[hod_forward].setSelected(false);
                        imageButtons[hod_forward].setEnabled(false);
                        imageButtons[hod_not].setSelected(false);
                        bluetoothConnect.WriteData("<" + cmnds[position] + ">");
                    } else if (position == hod_not) {
                        holder.imBtn.setSelected(true);
                        imageButtons[hod_forward].setSelected(false);
                        imageButtons[hod_back].setSelected(false);
                        imageButtons[hod_forward].setEnabled(true);
                        imageButtons[hod_back].setEnabled(true);
                        bluetoothConnect.WriteData("<" + cmnds[position] + ">");
                    } else if (position == hod_forward) {
                        holder.imBtn.setSelected(true);
                        bluetoothConnect.WriteData("<" + cmnds[position] + ">");
                        imageButtons[hod_back].setSelected(false);
                        imageButtons[hod_back].setEnabled(false);
                        imageButtons[hod_not].setSelected(false);
                    }
                }
                return false;
            }
        });
    }

    @Override
    public int getItemCount() {
        return drawables.length;
    }

    class MyHolder extends RecyclerView.ViewHolder {
        ImageButton imBtn;

        MyHolder(View itemView) {
            super(itemView);
            imBtn = itemView.findViewById(R.id.imgbtn);
        }
    }
}
