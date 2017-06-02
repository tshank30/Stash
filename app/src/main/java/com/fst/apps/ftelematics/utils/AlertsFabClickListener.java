package com.fst.apps.ftelematics.utils;

import java.util.HashMap;

import com.fst.apps.ftelematics.R;

import android.content.Context;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class AlertsFabClickListener implements OnClickListener{

	private int alertType;
	private ListView list;
	private View noDataFoundView;
	private DatabaseHelper db;
	private Context context;
	private AlertDialogManager dialog=new AlertDialogManager();
	private String dialogTitle,dialogMessage,userId;

	public AlertsFabClickListener(HashMap<String,Object> objMap,Context context){
		alertType=(Integer)objMap.get("alertType");
		list=(ListView) objMap.get("listView");
		noDataFoundView=(TextView) objMap.get("noDataFoundView");
		db=(DatabaseHelper) objMap.get("database");
		userId=(String) objMap.get("userId");
		this.context=context;
		dialogTitle=context.getResources().getString(R.string.deleteSuccess);
		dialogMessage=context.getResources().getString(R.string.alertsDeleteSuccessMsg);
	}

	@Override
	public void onClick(View v) {
		int count=0;
		if(db!=null){
			count=db.deleteAlertsByType(alertType,userId);
		}

		if(count>0){
			dialogMessage=AppUtils.getAlertNameByType(alertType)+" "+dialogMessage;
			dialog.showAlertDialog(context,dialogTitle, dialogMessage, true);
			if(list!=null){
				list.setVisibility(View.GONE);
			}
			if(noDataFoundView!=null){
				noDataFoundView.setVisibility(View.VISIBLE);
			}
		}else{
			Toast.makeText(context, "No More Alerts :-)", Toast.LENGTH_SHORT).show();
		}
	}

}
