package com.fst.apps.ftelematics;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.support.v7.app.AppCompatActivity;

import com.fst.apps.ftelematics.soapclient.IAppManager;
import com.fst.apps.ftelematics.soapclient.IMService;

public class BaseActivity extends AppCompatActivity {
	protected IAppManager imService;
	protected Context context;

	private ServiceConnection mConnection = new ServiceConnection() {

		public void onServiceConnected(ComponentName className, IBinder service) {
			// This is called when the connection with the service has been
			// established, giving us the service object we can use to
			// interact with the service. Because we have bound to a explicit
			// service that we know is running in our own process, we can
			// cast its IBinder to a concrete class and directly access it.
			imService = ((IMService.IMBinder) service).getService();
		}

		public void onServiceDisconnected(ComponentName className) {
			// This is called when the connection with the service has been
			// unexpectedly disconnected -- that is, its process crashed.
			// Because it is running in our same process, we should never
			// see this happen.
			imService = null;
		}
	};

	@Override
	protected void onResume() {
		bindService(new Intent(context, IMService.class), mConnection,Context.BIND_AUTO_CREATE);
		super.onResume();
	}

	@Override
	protected void onPause() {
		unbindService(mConnection);
		super.onPause();
	}
}
