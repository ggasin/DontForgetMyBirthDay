package com.dfmbd.dontforgetbirthdayproject.apiClass;

import android.app.Activity;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.ProgressBar;

public class LoadingApiManager {
    public void showLoading( LinearLayout progressBarLy, ProgressBar loadingSpinner , Activity activity) {
        Log.d("showLoadMethod","");
        progressBarLy.setVisibility(View.VISIBLE);
        Log.d("showLoadMethod1","");
        loadingSpinner.setVisibility(View.VISIBLE);
        Log.d("showLoadMethod2","");
        activity.getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE, WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
        Log.d("showLoadMethod3","");
    }

    public void hideLoading(LinearLayout progressBarLy, ProgressBar loadingSpinner , Activity activity) {
        Log.d("hideLoadMethod","");
        progressBarLy.setVisibility(View.GONE);
        Log.d("hideLoadMethod1","");
        loadingSpinner.setVisibility(View.GONE);
        Log.d("hideLoadMethod2","");
        activity.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
        Log.d("hideLoadMethod3","");
    }
}
