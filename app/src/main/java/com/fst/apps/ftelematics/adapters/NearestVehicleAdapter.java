package com.fst.apps.ftelematics.adapters;


import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.ShapeDrawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.fst.apps.ftelematics.R;
import com.fst.apps.ftelematics.entities.LastLocation;
import com.fst.apps.ftelematics.utils.AppUtils;
import com.google.android.gms.maps.model.LatLng;

import java.util.List;

public class NearestVehicleAdapter extends RecyclerView.Adapter<NearestVehicleAdapter.NearestVehicleHolder> {


    private List<LastLocation> filteredList;
    private LatLng fromLatlong;
    private Context context;


    public NearestVehicleAdapter(List<LastLocation> lastLocationList, Context context, LatLng fromLatLong) {
        this.fromLatlong = fromLatLong;
        this.filteredList = lastLocationList;
        this.context = context;
    }

    @Override
    public NearestVehicleHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.nearest_vehicle_item, parent, false);
        return new NearestVehicleHolder(v);
    }

    @Override
    public void onBindViewHolder(final NearestVehicleHolder holder, final int position) {
        final LastLocation lastLocation = filteredList.get(position);
        holder.firstLine.setText(lastLocation.getDisplayName());
        holder.distance.setText(lastLocation.getDistanceText()+" away");
        holder.duration.setText(lastLocation.getDurationText());

        String address = lastLocation.getAddress();
        if (TextUtils.isEmpty(address) || address.equalsIgnoreCase("NF") || address.equalsIgnoreCase("timeout")) {
            new AddresTask(holder.address, lastLocation).execute();
        } else {
            holder.address.setText(address);
        }
        holder.address.setSelected(true);

        if (lastLocation.getStatusCode().equalsIgnoreCase("61714")) {
            holder.thumbText.setText("M");
            setShapeBackground(holder.thumbnail.getBackground(), context.getResources().getColor(R.color.parrot));

        } else if (lastLocation.getStatusCode().equalsIgnoreCase("61715")) {
            holder.thumbText.setText("S");
            setShapeBackground(holder.thumbnail.getBackground(), Color.RED);


        } else if (lastLocation.getStatusCode().equalsIgnoreCase("61716")) {
            holder.thumbText.setText("D");
            setShapeBackground(holder.thumbnail.getBackground(), context.getResources().getColor(R.color.orange));

        } else if (lastLocation.getStatusCode().equalsIgnoreCase("0")) {
            holder.thumbText.setText("NW");
            setShapeBackground(holder.thumbnail.getBackground(), Color.BLUE);

        }


    }

    @Override
    public int getItemCount() {
        return filteredList == null ? 0 : filteredList.size();
    }

    public void setShapeBackground(Drawable background, int color) {
        if (background instanceof ShapeDrawable) {
            // cast to 'ShapeDrawable'
            ShapeDrawable shapeDrawable = (ShapeDrawable) background;
            shapeDrawable.getPaint().setColor(color);
        } else if (background instanceof GradientDrawable) {
            // cast to 'GradientDrawable'
            GradientDrawable gradientDrawable = (GradientDrawable) background;
            gradientDrawable.setColor(color);
        }
    }


    public class NearestVehicleHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        public TextView firstLine, thumbText, distance,duration,address;
        public RelativeLayout thumbnail;
        public ImageView navigate;

        public NearestVehicleHolder(View itemView) {
            super(itemView);
            firstLine = itemView.findViewById(R.id.display_name);
            thumbText = itemView.findViewById(R.id.thumb_text);
            thumbnail = itemView.findViewById(R.id.thumbnail);
            distance = itemView.findViewById(R.id.distance);
            duration = itemView.findViewById(R.id.duration);
            navigate = itemView.findViewById(R.id.navigation);
            address = itemView.findViewById(R.id.address);
            navigate.setOnClickListener(this);
        }


        @Override
        public void onClick(View v) {
            //Uri gmmIntentUri = Uri.parse("geo:"+filteredList.get(getAdapterPosition()).getLatitude()+","+filteredList.get(getAdapterPosition()).getLongitude());
            /*Uri gmmIntentUri = Uri.parse("google.navigation:q=" + filteredList.get(getAdapterPosition()).getLatitude() + "," + filteredList.get(getAdapterPosition()).getLongitude());
            Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
            mapIntent.setPackage("com.google.android.apps.maps")*/;

            Intent intent = new Intent(android.content.Intent.ACTION_VIEW,
                    Uri.parse("http://maps.google.com/maps?saddr="+fromLatlong.latitude+","+fromLatlong.longitude+"&daddr="+filteredList.get(getAdapterPosition()).getLatitude()+","+filteredList.get(getAdapterPosition()).getLongitude()));


            if (intent.resolveActivity(context.getPackageManager()) != null) {
                context.startActivity(intent);
            }
        }
    }

    class AddresTask extends AsyncTask {

        private TextView targetView;
        private LastLocation lastLocation;
        private String address;

        AddresTask(TextView targetView, LastLocation lastLocation) {
            this.targetView = targetView;
            this.lastLocation = lastLocation;
        }

        @Override
        protected Object doInBackground(Object[] params) {
            String latitude = lastLocation.getLatitude();
            String longitude = lastLocation.getLongitude();
            if (!TextUtils.isEmpty(latitude) && !TextUtils.isEmpty(longitude)) {
                address = AppUtils.reverseGeocode(context, Double.parseDouble(lastLocation.getLatitude()), Double.parseDouble(lastLocation.getLongitude()));
            }
            return address;
        }

        @Override
        protected void onPostExecute(Object o) {
            super.onPostExecute(o);
            targetView.setText((String) o);
        }
    }


}
