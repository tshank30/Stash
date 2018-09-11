package com.fst.apps.ftelematics.fragments;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.app.Fragment;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.fst.apps.ftelematics.BuildConfig;
import com.fst.apps.ftelematics.R;

import java.util.List;

/**
 * Created by welcome on 4/23/2016.
 */
public class SupportFragment extends Fragment {

    Button submit;
    EditText query, mobile, email, name;

    LinearLayout callOption;
    TextView mob1, mob2;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_support, container, false);

        submit = (Button) v.findViewById(R.id.submit);
        query = (EditText) v.findViewById(R.id.query);
        mobile = (EditText) v.findViewById(R.id.mobile);
        email = (EditText) v.findViewById(R.id.email);
        name = (EditText) v.findViewById(R.id.name);
        callOption = (LinearLayout) v.findViewById(R.id.call_option);
        mob1 = (TextView) v.findViewById(R.id.mob1);
        mob2 = (TextView) v.findViewById(R.id.mob2);


        submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                try {
                    Intent intent = new Intent(android.content.Intent.ACTION_SEND);
                    intent.setType("text/html");

                    final PackageManager pm = getActivity().getPackageManager();
                    final List<ResolveInfo> matches = pm.queryIntentActivities(intent, 0);
                    ResolveInfo best = null;
                    for (final ResolveInfo info : matches) {
                        if (info.activityInfo.packageName.endsWith(".gm") || info.activityInfo.name.toLowerCase().contains("gmail")) {
                            best = info;
                            break;
                        }
                    }
                    if (best != null) {
                        intent.setClassName(best.activityInfo.packageName, best.activityInfo.name);
                    }
                    if (BuildConfig.FLAVOR.equals("brandwatch"))
                        intent.putExtra(android.content.Intent.EXTRA_EMAIL, new String[]{"brandwatchgps@gmail.com"});


                    intent.putExtra(android.content.Intent.EXTRA_SUBJECT, "Feedback");
                    intent.putExtra(android.content.Intent.EXTRA_TEXT, "Name : " + name.getText() +
                            "\n Mobile : " + mobile.getText() +
                            "\n Email : " + email.getText() +
                            "\n Query : " + query.getText());

                    startActivity(intent);
                } catch (ActivityNotFoundException e) {
                    Intent emailIntent = new Intent(android.content.Intent.ACTION_SEND);
                    emailIntent.putExtra(android.content.Intent.EXTRA_EMAIL, new String[]{"brandwatchgps@gmail.com"});
                    emailIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, "Feedback");
                    emailIntent.putExtra(Intent.EXTRA_TEXT, "Name : " + name.getText() +
                            "\n Mobile : " + mobile.getText() +
                            "\n Email : " + email.getText() +
                            "\n Query : " + query.getText());

                    emailIntent.setType("text/plain");

                    startActivity(Intent.createChooser(emailIntent, "Send mail..."));
                }

            }
        });


        if (BuildConfig.FLAVOR.equals("brandwatch")) {
            callOption.setVisibility(View.VISIBLE);

            mob1.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    Intent callIntent = new Intent(Intent.ACTION_DIAL);
                    callIntent.setData(Uri.parse("tel:7835034014"));
                    startActivity(callIntent);

                }
            });

            mob2.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent callIntent = new Intent(Intent.ACTION_DIAL);
                    callIntent.setData(Uri.parse("tel:9911620054"));
                    startActivity(callIntent);

                }
            });
        } else {
            callOption.setVisibility(View.GONE);
        }


        return v;
    }
}
