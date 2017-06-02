package com.fst.apps.ftelematics.utils;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.android.gms.maps.GoogleMap.InfoWindowAdapter;
import com.google.android.gms.maps.GoogleMap.OnInfoWindowClickListener;
import com.google.android.gms.maps.model.Marker;

public class MapInfoWindow implements InfoWindowAdapter,OnInfoWindowClickListener{

	private Context mContext;
    private String fragmentType;

	public MapInfoWindow(Context mContext,String fragmentType){
		this.mContext=mContext;
        this.fragmentType=fragmentType;
	}

	@Override
	public View getInfoContents(Marker marker) {

		LinearLayout info = new LinearLayout(mContext);
		info.setOrientation(LinearLayout.VERTICAL);
		LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
				LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
		layoutParams.setMargins(20, 10, 20, 0);
		info.setLayoutParams(layoutParams);
        //info.setWeightSum(2);
        String snippetText=marker.getSnippet();

        if(fragmentType.equalsIgnoreCase("HISTORY")) {
            if (!TextUtils.isEmpty(snippetText)) {
                String parts[] = snippetText.split("\n");
                for (int i = 0; i < parts.length; i++) {
                    String infoItem = parts[i];
                    if (!TextUtils.isEmpty(infoItem)) {
                        String infoItemParts[] = infoItem.split("~");
                        if(infoItemParts.length>1) {
                            addInfoItem(infoItemParts[0], infoItemParts[1], info);
                        }else{
                            addInfoItem("Tracking", "Truck", info);
                        }
                    }
                }

            }
        }else {
            addInfoItem(marker.getTitle(), snippetText,info);
        }


		return info;

	}

	@Override
	public View getInfoWindow(Marker marker) {
		return null;
	}

	@Override
	public void onInfoWindowClick(Marker marker) {
		marker.hideInfoWindow();
	}

    public void addInfoItem(String label,String text,LinearLayout info){
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        TextView title = new TextView(mContext);
        title.setTextColor(Color.BLACK);
        title.setGravity(Gravity.CENTER);
        title.setTypeface(null, Typeface.BOLD);
        title.setText(label);
        title.setLayoutParams(layoutParams);

        TextView snippet = new TextView(mContext);
        snippet.setTextColor(Color.GRAY);
        snippet.setText(text);
        snippet.setLayoutParams(layoutParams);

        info.addView(title);
        info.addView(snippet);
    }
}
