package com.fst.apps.ftelematics.adapters;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.fst.apps.ftelematics.R;

public class HomeViewHolder extends RecyclerView.ViewHolder {

    public TextView thumbText,firstLine,secondLine,speed,acStatus,statusSince,address,battery,signal,tts,driverName,distance;
    public RelativeLayout thumbnail;
    public RelativeLayout mapButton,historyButton,ignitionButton,callButton;
    public LinearLayout batterySignalLayout;

    public HomeViewHolder(View itemView) {
        super(itemView);

        thumbnail=(RelativeLayout) itemView.findViewById(R.id.thumbnail);
        thumbText=(TextView) itemView.findViewById(R.id.thumb_text);
        tts=(TextView) itemView.findViewById(R.id.tts);
        firstLine=(TextView) itemView.findViewById(R.id.first_line);
        speed=(TextView) itemView.findViewById(R.id.speed);
        acStatus=(TextView) itemView.findViewById(R.id.ac_status);
        address=(TextView) itemView.findViewById(R.id.address);
        secondLine=(TextView) itemView.findViewById(R.id.second_line);
        statusSince=(TextView) itemView.findViewById(R.id.status_since);
        mapButton=(RelativeLayout) itemView.findViewById(R.id.map_button);
        historyButton=(RelativeLayout) itemView.findViewById(R.id.history_button);
        ignitionButton=(RelativeLayout) itemView.findViewById(R.id.ignition_button);
        callButton=(RelativeLayout) itemView.findViewById(R.id.call_btn);
        battery=(TextView) itemView.findViewById(R.id.battery);
        signal=(TextView) itemView.findViewById(R.id.signal);
        batterySignalLayout=(LinearLayout) itemView.findViewById(R.id.battery_signal_layout);
        driverName=(TextView) itemView.findViewById(R.id.driver_name);
        distance=(TextView) itemView.findViewById(R.id.distance);
    }
}
