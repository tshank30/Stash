package com.fst.apps.ftelematics.fragments;

import com.fst.apps.ftelematics.R;
import com.fst.apps.ftelematics.utils.SharedPreferencesManager;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.Toast;

public class SettingsFragment extends Fragment{

	private SeekBar autoRefreshSeekBar;
	private CheckBox notificationsCB,speechNotificationsCB;
	private SharedPreferencesManager sharedPrefs;
	private final static int PUSH_NOTIFICATIONS=1;
	private final static int TTS_NOTIFICATIONS=2;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		sharedPrefs=new SharedPreferencesManager(getActivity());
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {

		View v=inflater.inflate(R.layout.fragment_settings, container,false);
		autoRefreshSeekBar=(SeekBar) v.findViewById(R.id.autoRefreshSeek);
		notificationsCB=(CheckBox) v.findViewById(R.id.notificationsCB);
		speechNotificationsCB=(CheckBox) v.findViewById(R.id.speech_notificationsCB);

		long seekBarPosition=sharedPrefs.getAutoRefresh();
		if(seekBarPosition>0){
			autoRefreshSeekBar.setProgress(((int)seekBarPosition)/1000);
		}

		boolean alertModeStatus=sharedPrefs.getAlertsMode();
		boolean ttsModeStatus=sharedPrefs.getSpeechMode();
		notificationsCB.setChecked(alertModeStatus);
		speechNotificationsCB.setChecked(ttsModeStatus);

		autoRefreshSeekBar.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {

			@Override
			public void onStopTrackingTouch(SeekBar seekBar) {
				sharedPrefs.setAutoRefresh(seekBar.getProgress());
				displayToast("Auto refresh set to " + seekBar.getProgress() + " seconds");
			}

			@Override
			public void onStartTrackingTouch(SeekBar seekBar) {

			}

			@Override
			public void onProgressChanged(SeekBar seekBar, int progress,
										  boolean fromUser) {
				//Log.v("Progress", progress+"");
				if (progress == 10) {
					autoRefreshSeekBar.setProgress(10);
				} else if (progress == 30) {
					autoRefreshSeekBar.setProgress(30);
				} else if (progress == 60) {
					autoRefreshSeekBar.setProgress(60);
				} else if (progress < 10) {
					autoRefreshSeekBar.setProgress(0);
				} else if (progress > 15) {
					autoRefreshSeekBar.setProgress(30);
				} else if (progress < 15) {
					autoRefreshSeekBar.setProgress(10);
				} else if (progress > 30) {
					autoRefreshSeekBar.setProgress(60);
				}
			}
		});


		notificationsCB.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				boolean isChecked = ((CheckBox) v).isChecked();
				handleNotificationSettings(v,isChecked,PUSH_NOTIFICATIONS);

			}
		});

		speechNotificationsCB.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				boolean isChecked=((CheckBox) v).isChecked();
				handleNotificationSettings(v,isChecked,TTS_NOTIFICATIONS);

			}
		});

		return v;
	}

	public void handleNotificationSettings(View v, boolean isChecked,int notificationType){
		CheckBox cb = (CheckBox) v;
		if(isChecked){
			cb.setChecked(true);
			if(notificationType==PUSH_NOTIFICATIONS) {
				sharedPrefs.setAlertsMode(true);
				displayToast("You will start receiving push notifications!");
			}else {
				sharedPrefs.setSpeechMode(true);
				displayToast("Text to speech activated!");
			}
		}else{
			cb.setChecked(false);
			if(notificationType==PUSH_NOTIFICATIONS) {
				sharedPrefs.setAlertsMode(false);
				displayToast("You will no longer receive push notifications!");
			}else{
				sharedPrefs.setSpeechMode(false);
				displayToast("Text to speech deactivated!");
			}

		}
	}


	public void displayToast(String message){
		Toast.makeText(getActivity(), message, Toast.LENGTH_SHORT).show();
	}
}
