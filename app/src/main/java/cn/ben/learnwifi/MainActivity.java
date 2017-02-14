package cn.ben.learnwifi;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import java.util.List;

@SuppressWarnings({"unused", "FieldCanBeLocal"})
public class MainActivity extends AppCompatActivity {

    private static final String TAG = "BenYang";
    private List<ScanResult> mScanResultList;
    private List<WifiConfiguration> mWifiConfigurations;
    private final BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            mScanResultList = mWifiManager.getScanResults();
            for (ScanResult scanResult : mScanResultList) {
                Log.d(TAG, scanResult.SSID);
            }

            mWifiConfigurations = mWifiManager.getConfiguredNetworks();
            for (WifiConfiguration wifiConfiguration : mWifiConfigurations) {
                Log.d(TAG, wifiConfiguration.SSID);
            }
        }
    };
    private WifiManager mWifiManager;
    private boolean alreadyStartedScanned = false;
    private int linkingID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mWifiManager = (WifiManager) getSystemService(WIFI_SERVICE);
        Log.d(TAG, "mWifiManager.isWifiEnabled() " + String.valueOf(mWifiManager.isWifiEnabled()));
        boolean setRes = mWifiManager.setWifiEnabled(true);
        // not immediately
        Log.d(TAG, "set result " + String.valueOf(setRes));

        WifiInfo wifiInfo = mWifiManager.getConnectionInfo();
        Log.d(TAG, "current connection: " + wifiInfo.getSSID());
    }

    @Override
    protected void onResume() {
        super.onResume();
        registerReceiver(mBroadcastReceiver, new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));
        if (!alreadyStartedScanned) {
            boolean initRes = mWifiManager.startScan();
            Log.d(TAG, "startScan " + initRes);
            alreadyStartedScanned = true;
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(mBroadcastReceiver);
    }

    public WifiConfiguration CreateWifiConfiguration(String SS_ID, String Password, int Type) {
        WifiConfiguration config = new WifiConfiguration();
        config.allowedAuthAlgorithms.clear();
        config.allowedGroupCiphers.clear();
        config.allowedKeyManagement.clear();
        config.allowedPairwiseCiphers.clear();
        config.allowedProtocols.clear();

        config.SSID = "\"" + SS_ID + "\"";
        WifiConfiguration tempConfig = IsExists(SS_ID);
        if (tempConfig != null) {
            // Remove the specified network from the list of configured networks.
            mWifiManager.removeNetwork(tempConfig.networkId);
        }

        if (Type == 1) //WIFI CIPHER_NO_PASS
        {
          /*  config.wepKeys[0] = "";//连接无密码热点时加上这两句会出错
            config.wepTxKeyIndex = 0;*/
            config.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);
        }
        if (Type == 2) //WIFI CIPHER_WEP
        {
            config.hiddenSSID = true;
            config.wepKeys[0] = "\"" + Password + "\"";
            config.allowedAuthAlgorithms.set(WifiConfiguration.AuthAlgorithm.SHARED);
            config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.CCMP);
            config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.TKIP);
            config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.WEP40);
            config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.WEP104);
            config.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);
            config.wepTxKeyIndex = 0;
        }
        if (Type == 3) //WIFI_CIPHER_WPA
        {
            config.preSharedKey = "\"" + Password + "\"";
            config.hiddenSSID = true;
            config.allowedAuthAlgorithms.set(WifiConfiguration.AuthAlgorithm.OPEN);
            config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.TKIP);
            config.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.WPA_PSK);
            config.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.TKIP);
            //config.allowedProtocols.set(WifiConfiguration.Protocol.WPA);
            config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.CCMP);
            config.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.CCMP);
            config.status = WifiConfiguration.Status.ENABLED;
        }
        return config;
    }

    private WifiConfiguration IsExists(String ss_id) {
        for (WifiConfiguration wifiConfiguration : mWifiConfigurations) {
            if (wifiConfiguration.SSID.equals(ss_id)) return wifiConfiguration;
        }
        return null;
    }

    // 添加一个网络并连接
    public boolean addNetwork(WifiConfiguration wcg) {
        int wcgID = mWifiManager.addNetwork(wcg);
        boolean b = mWifiManager.enableNetwork(wcgID, true);
        if (b) {
            linkingID = wcgID;
        }
        return b;
    }

    public void disconnectWifi() {
        if(0 == linkingID ){
            return;
        }
        mWifiManager.disableNetwork(linkingID);
        mWifiManager.disconnect();
    }
}
