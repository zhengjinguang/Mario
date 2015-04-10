package com.lemi.controller.lemigameassistance.view;

import android.annotation.TargetApi;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.usb.UsbManager;
import android.net.ConnectivityManager;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.util.AttributeSet;
import android.view.InputDevice;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.lemi.controller.lemigameassistance.R;
import com.lemi.controller.lemigameassistance.utils.NetworkCheckUtil;
import com.lemi.mario.externalmanager.manager.ExternalStorageManager;

/**
 * Created by shining on 11/21/14.
 */
public class TitleView extends RelativeLayout {
  private static final long GAMEPAD_SCHEDULE_TIME = 2000l;
  private ImageView mUsbView;
  private ImageView mNetworkView;
  private ImageView mSdcadView;
  private ImageView mGamepadView;

  public TitleView(Context context, AttributeSet attrs) {
    super(context, attrs);
  }

  @Override
  protected void onFinishInflate() {
    super.onFinishInflate();
    mUsbView = (ImageView) findViewById(R.id.title_usb);
    mNetworkView = (ImageView) findViewById(R.id.title_network);
    mSdcadView = (ImageView) findViewById(R.id.title_sdcard);
    mGamepadView = (ImageView) findViewById(R.id.title_gamepad);
    setNetworkIcon();
    setUsbIcon();
    setSdcardIcon();
  }

  @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
  @Override
  protected void onAttachedToWindow() {
    super.onAttachedToWindow();
    IntentFilter networkFilter = new IntentFilter();
    networkFilter.addAction(WifiManager.NETWORK_STATE_CHANGED_ACTION);
    networkFilter.addAction(WifiManager.WIFI_STATE_CHANGED_ACTION);
    networkFilter.addAction(WifiManager.RSSI_CHANGED_ACTION);
    networkFilter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);

    getContext().registerReceiver(mNetworkReceiver, networkFilter);
    IntentFilter usbFilter = new IntentFilter();
    usbFilter.addAction(Intent.ACTION_MEDIA_MOUNTED);
    usbFilter.addAction(Intent.ACTION_MEDIA_CHECKING);
    usbFilter.addAction(Intent.ACTION_MEDIA_EJECT);
    usbFilter.addAction(Intent.ACTION_MEDIA_REMOVED);
    usbFilter.addAction(Intent.ACTION_MEDIA_UNMOUNTED);
    usbFilter.addAction(Intent.ACTION_MEDIA_BAD_REMOVAL);
    usbFilter.addDataScheme("file");
    getContext().registerReceiver(mUsbReceiver, usbFilter);


    IntentFilter uFilter = new IntentFilter();
    uFilter.addAction(UsbManager.ACTION_USB_DEVICE_ATTACHED);
    uFilter.addAction(UsbManager.ACTION_USB_DEVICE_DETACHED);
    getContext().registerReceiver(mUsbReceiver, uFilter);
    setUsbIcon();
    setSdcardIcon();
    setNetworkIcon();
    setGamepadIcon();
    // getHandler().post(mGamepadChecker);
  }

  @Override
  protected void onDetachedFromWindow() {
    super.onDetachedFromWindow();
    getContext().unregisterReceiver(mUsbReceiver);
    getContext().unregisterReceiver(mNetworkReceiver);
    getHandler().removeCallbacks(mGamepadChecker);
  }

  private void setNetworkIcon() {
    NetworkCheckUtil.setNetworkIcon(getContext(), mNetworkView);
  }

  private BroadcastReceiver mUsbReceiver = new BroadcastReceiver() {

    @Override
    public void onReceive(Context context, Intent intent) {
      TitleView.this.getHandler().postDelayed(new Runnable() {
        @Override
        public void run() {
          setUsbIcon();
          setSdcardIcon();
          setGamepadIcon();
        }
      }, 1000l);
    }
  };

  private void setUsbIcon() {
    boolean hasUsb = ExternalStorageManager.getInstance().hasOtherExternalStorage();
    if (hasUsb) {
      mUsbView.setVisibility(View.VISIBLE);
    } else {
      mUsbView.setVisibility(View.GONE);
    }
  }

  private void setSdcardIcon() {
    boolean hasSdcard = ExternalStorageManager.getInstance().hasSdcard();
    if (hasSdcard) {
      mSdcadView.setVisibility(View.VISIBLE);
    } else {
      mSdcadView.setVisibility(View.GONE);
    }
  }

  private void setGamepadIcon() {
    int[] deviceIds = InputDevice.getDeviceIds();
    if (deviceIds == null || deviceIds.length == 0) {
      mGamepadView.setVisibility(INVISIBLE);
      return;
    }
    for (int id = 0; id < deviceIds.length; id++) {
      InputDevice device = InputDevice.getDevice(deviceIds[id]);
      if (device == null) {
        continue;
      }
      if (supportSource(device, InputDevice.SOURCE_JOYSTICK)
          || supportSource(device, InputDevice.SOURCE_GAMEPAD)) {
        mGamepadView.setVisibility(VISIBLE);
        return;
      }
    }
    mGamepadView.setVisibility(INVISIBLE);
  }

  private boolean supportSource(InputDevice device, int source) {
    return (device.getSources() & source) == source;
  }

  private BroadcastReceiver mNetworkReceiver = new BroadcastReceiver() {
    public void onReceive(Context context, Intent intent) {
      if (ConnectivityManager.CONNECTIVITY_ACTION.equals(intent.getAction())
          || WifiManager.WIFI_STATE_CHANGED_ACTION.equals(intent.getAction())
          || WifiManager.RSSI_CHANGED_ACTION.equals(intent.getAction())
          || WifiManager.NETWORK_STATE_CHANGED_ACTION.equals(intent.getAction())) {
        setNetworkIcon();
      }
    }
  };

  private final Runnable mGamepadChecker = new Runnable() {
    public void run() {
      setGamepadIcon();
      getHandler().postDelayed(mGamepadChecker, GAMEPAD_SCHEDULE_TIME);
    }
  };

}
