package com.anjlab.android.smsgateway.gcm;

import android.app.Application;

import org.acra.ACRA;
import org.acra.annotation.*;

@ReportsCrashes(formUri = "http://www.bugsense.com/api/acra?api_key=f502e669", formKey="")
public class GatewayApplication extends Application {
	
	@Override
	public void onCreate() {
		ACRA.init(this);
		
		super.onCreate();
	}

}
