package com.fst.apps.ftelematics.adapters;

import java.util.List;

import com.fst.apps.ftelematics.R;
import com.fst.apps.ftelematics.entities.Alerts;
import com.fst.apps.ftelematics.utils.AppUtils;

import android.app.Activity;
import android.content.Context;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class AlertsAdapter extends BaseAdapter {  
    private Activity activity;
    private LayoutInflater inflater;
    private List<Alerts> alerts;
 


    public AlertsAdapter(Activity activity, List<Alerts> alerts) {
        this.activity = activity;
        this.alerts = alerts;
    }
 
    @Override
    public int getCount() {
        return alerts.size();
    }
 
    @Override
    public Object getItem(int location) {
        return alerts.get(location);
    }
 
    @Override
    public long getItemId(int position) {
        return position;
    }
 
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
 
        if (inflater == null)
            inflater = (LayoutInflater) activity
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        if (convertView == null)
            convertView = inflater.inflate(R.layout.fragment_alert_item, null);
 
        
 
        TextView name = (TextView) convertView.findViewById(R.id.name);
        TextView timestamp = (TextView) convertView
                .findViewById(R.id.timestamp);
        TextView statusMsg = (TextView) convertView
                .findViewById(R.id.txtStatusMsg);
        TextView url = (TextView) convertView.findViewById(R.id.txtUrl);
        ImageView profilePic = (ImageView) convertView.findViewById(R.id.profilePic);
        ImageView feedImageView = (ImageView) convertView.findViewById(R.id.feedImage1);
 
        Alerts alert = alerts.get(position);
 
        name.setText(AppUtils.getAlertTypes(alert.getAlertType())+" ("+alert.getAlertSubType()+") ");
 
        
        timestamp.setText(alert.getCreatedAt());
 
        // Chcek for empty status message
        if (!TextUtils.isEmpty(alert.getAlertText())) {
            statusMsg.setText(alert.getAlertText());
            statusMsg.setVisibility(View.VISIBLE);
        }
        
        if (!TextUtils.isEmpty(alert.getAlertSubType())) {
            if(alert.getAlertSubType().equalsIgnoreCase("OFF")){
            	profilePic.setImageResource(R.drawable.ic_ignition_off);
            }else if(alert.getAlertSubType().equalsIgnoreCase("ON")){
            	profilePic.setImageResource(R.drawable.ic_ignition_on);
            }else if(alert.getAlertSubType().equalsIgnoreCase("OVERSPEED")){
            	profilePic.setImageResource(R.drawable.ic_overspeed);
            }else if(alert.getAlertSubType().equalsIgnoreCase("IN")){
            	profilePic.setImageResource(R.drawable.ic_geofence);
            }else if(alert.getAlertSubType().equalsIgnoreCase("OUT")){
            	profilePic.setImageResource(R.drawable.ic_geofence);
            }else{
            	profilePic.setImageResource(R.drawable.ic_overspeed);
            }
            
        }
       
       
 
        // Feed image
       /* if (item.getImge() != null) {
            feedImageView.setImageUrl(item.getImge(), imageLoader);
            feedImageView.setVisibility(View.VISIBLE);
            feedImageView
                    .setResponseObserver(new FeedImageView.ResponseObserver() {
                        @Override
                        public void onError() {
                        }
 
                        @Override
                        public void onSuccess() {
                        }
                    });
        } else {
            feedImageView.setVisibility(View.GONE);
        }
 */
        return convertView;
    }
 
}
