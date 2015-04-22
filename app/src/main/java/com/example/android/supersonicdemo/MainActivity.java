package com.example.android.supersonicdemo;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import com.google.android.gms.ads.identifier.AdvertisingIdClient;
import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.supersonicads.sdk.SSAFactory;
import com.supersonicads.sdk.SSAPublisher;
import com.supersonicads.sdk.data.AdUnitsReady;
import com.supersonicads.sdk.listeners.OnInterstitialListener;
import com.supersonicads.sdk.listeners.OnOfferWallListener;
import com.supersonicads.sdk.listeners.OnRewardedVideoListener;
import com.supersonicads.sdk.utils.SDKUtils;

import android.os.Bundle;
import android.app.Activity;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity implements
        OnClickListener,
        OnRewardedVideoListener,
        OnInterstitialListener,
        OnOfferWallListener{

    private boolean rewardedVideoInitSuccess = false;		//indicates if RewardedVideo was initiated successfully


    private boolean interstitialInitiated = false;			//indicates if Interstitial was initiated successfully
    private boolean isAvailable = false;

    //UI buttons
    private Button mRVButton;								//RewardedVideo button
    private Button mISButton;								//Interstitial button
    private Button mOWButton;								//Offerwall button

    private TextView mVersion;

    private ProgressBar mRVProgress;						//RewardedVideo button's progress bar
    private ProgressBar mISProgress;						//Interstitial button's progress bar

    // Supersonic Parameters.
    private  String userId="demo";
    private final String applicationKey = "392454e9";

    private SSAPublisher mSSAPublisher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initViews();

        mSSAPublisher = SSAFactory.getPublisherInstance(this);
    }

    //initiates UI views
    private void initViews() {
        mRVButton = (Button) findViewById(R.id.rv_button);
        mRVButton.setOnClickListener(this);

        mISButton = (Button) findViewById(R.id.is_button);
        mISButton.setOnClickListener(this);

        mOWButton = (Button) findViewById(R.id.ow_button);
        mOWButton.setOnClickListener(this);

        mVersion = (TextView) findViewById(R.id.version);



        PackageInfo pInfo;
        try {

            pInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
            mVersion.setText("SDK version: " + SDKUtils.getSDKVersion() + "");

        } catch (NameNotFoundException e) {}

        mRVProgress = (ProgressBar) findViewById(R.id.rv_pb);
        mISProgress = (ProgressBar) findViewById(R.id.is_pb);


//        try {
//            userId = AdvertisingIdClient.getAdvertisingIdInfo(MainActivity.this).getId();
//            Log.e("user id: ", userId+"");
//        } catch (IOException e) {
//            e.printStackTrace();
//        } catch (GooglePlayServicesNotAvailableException e) {
//            e.printStackTrace();
//        } catch (GooglePlayServicesRepairableException e) {
//            e.printStackTrace();
//        }
    }


    @Override
    protected void onResume() {
        super.onResume();
        mSSAPublisher.onResume(this);
    }


    @Override
    protected void onPause() {
        super.onPause();
        mSSAPublisher.onPause(this);
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        mSSAPublisher.release(this);
    }

    @Override
    public void onClick(View v) {

        int id = v.getId();

        switch (id) {

            case R.id.rv_button:

                //Call the relevant RewardedVideo method according to the status
                if(rewardedVideoInitSuccess){
                    mSSAPublisher.showRewardedVideo();
                }else{
                    mRVProgress.setVisibility(View.VISIBLE);
                    mSSAPublisher.initRewardedVideo(applicationKey, userId, null, this);
                }

                break;

            case R.id.is_button:

                //Call the relevant Interstitial method according to the status
                if(interstitialInitiated){
                    if(isAvailable)
                        mSSAPublisher.showInterstitial();
                }else{
                    mISProgress.setVisibility(View.VISIBLE);
                    mSSAPublisher.initInterstitial(applicationKey, userId, null, this);
                }

                break;

            case R.id.ow_button:
                Map<String, String> extraParams = new HashMap<String, String>();

                //before showing the Offerwall - you can customize it:
                //for example adding: extraParams.put("pageSize", "5"); will display maximum 5 offers per page in our Offerwall.
                mSSAPublisher.showOfferWall(applicationKey, userId, extraParams, this);
                break;

            default:
                break;
        }
    }

    @Override
    public void onRVInitSuccess(AdUnitsReady adUnitsReady) {
        rewardedVideoInitSuccess = true;
        mRVProgress.setVisibility(View.INVISIBLE);
        updateView();
    }

    @Override
    public void onRVInitFail(String errMsg) {
        rewardedVideoInitSuccess = false;
        mRVProgress.setVisibility(View.INVISIBLE);
        updateView();
    }

    @Override
    public void onRVNoMoreOffers() {
        rewardedVideoInitSuccess = false;
        mRVProgress.setVisibility(View.INVISIBLE);
        updateView();
    }

    @Override
    public void onRVAdCredited(int credits) {
        Toast.makeText(MainActivity.this,credits+"",Toast.LENGTH_LONG).show();
    }


    @Override
    public void onOWShowSuccess() {}

    @Override
    public void onOWShowFail(String errMsg) {}

    @Override
    public boolean onOWAdCredited(int credits, int totalCredits, boolean totalCreditsFlag) {
        return true;
    }

    @Override
    public void onOWGeneric(String arg0, String arg1) {
    }


    @Override
    public void onRVGeneric(String arg0, String arg1) {
    }

    @Override
    public void onGetOWCreditsFailed(String arg0) {
    }

    @Override
    public void onOWAdClosed() {
    }

    @Override
    public void onRVAdClosed() {
    }

    private void updateView(){
        MainActivity.this.runOnUiThread(new Runnable() {

            @Override
            public void run() {
                //updates Interstitial button
                if(interstitialInitiated){
                    if(isAvailable){
                        mISButton.setText(R.string.show_interstitial);
                        mISProgress.setVisibility(View.INVISIBLE);
                    }
                    else{
                        mISButton.setText(R.string.loading_interstitial);
                        mISProgress.setVisibility(View.VISIBLE);
                    }
                }else{
                    mISButton.setText(R.string.init_interstitial);
                    mISProgress.setVisibility(View.INVISIBLE);
                }

                //updates RewardedVideo button
                if(rewardedVideoInitSuccess){
                    mRVButton.setText(R.string.show_rewarded_video);
                    mRVProgress.setVisibility(View.INVISIBLE);
                }
                else{
                    mRVButton.setText(R.string.init_rewarded_video);
                }
            }
        });
    }







    //New Interstitial Api
    @Override
    public void onInterstitialAdClicked() {
        // TODO Auto-generated method stub

    }

    @Override
    public void onInterstitialAdClosed() {
        mISProgress.setVisibility(View.VISIBLE);
        updateView();
    }

    @Override
    public void onInterstitialAvailability(boolean available) {
        //Check if Interstitial ads are ready to display
        if(available){
            isAvailable = true;
            updateView();
        }else{
            isAvailable = false;
        }
    }

    @Override
    public void onInterstitialInitFail(String arg0) {
        interstitialInitiated = false;
        updateView();
    }

    @Override
    public void onInterstitialInitSuccess() {
        interstitialInitiated = true;
        updateView();
    }

    @Override
    public void onInterstitialShowFail(String arg0) {
        // TODO Auto-generated method stub

    }

    @Override
    public void onInterstitialShowSuccess() {
        // TODO Auto-generated method stub

    }
}
