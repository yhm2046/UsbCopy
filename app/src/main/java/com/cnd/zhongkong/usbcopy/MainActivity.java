package com.cnd.zhongkong.usbcopy;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbManager;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;

import com.cnd.zhongkong.usbcopy.databinding.ActivityMainBinding;
import com.github.mjdev.libaums.UsbMassStorageDevice;
import com.github.mjdev.libaums.fs.FileSystem;
import com.github.mjdev.libaums.fs.UsbFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private String TAG="wxl";
    private static final String ACTION_USB_PERMISSION = "com.android.example.USB_PERMISSION";
    private Context mContext;
    private UsbMassStorageDevice[] storageDevices;
    private List<UsbFile> usbFiles = new ArrayList<>();
    private ActivityMainBinding activityMainBinding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        activityMainBinding=ActivityMainBinding.inflate(LayoutInflater.from(this));
        setContentView(activityMainBinding.getRoot());
        activityMainBinding.btnWrite.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.i(TAG,"onclick");
                if(redUDiskDevsList())
                    Log.i(TAG,"u盘可用");
                else
                    Log.i(TAG,"u盘不可用");
            }
        });
        Log.i(TAG,"oncreate");
        IntentFilter usbDeviceStateFilter = new IntentFilter();
        usbDeviceStateFilter.addAction(UsbManager.ACTION_USB_DEVICE_ATTACHED);  //usb插入
        usbDeviceStateFilter.addAction(UsbManager.ACTION_USB_DEVICE_DETACHED);  //usb拔出
        registerReceiver(mUsbReceiver, usbDeviceStateFilter);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.i(TAG,"ondestroy..");
        unregisterReceiver(mUsbReceiver);
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }
//动态广播监听拔插U盘
    BroadcastReceiver mUsbReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            Log.i(TAG,"onReceive");
            String action = intent.getAction();
            if (UsbManager.ACTION_USB_DEVICE_DETACHED.equals(action)) {
                Log.i(TAG,"拔出usb了");

            }else if(UsbManager.ACTION_USB_DEVICE_ATTACHED.equals(action)){
                Log.i(TAG,"插入usb了");
                UsbDevice device = (UsbDevice)intent.getParcelableExtra(UsbManager.EXTRA_DEVICE);
                if (device != null) {
                    Log.i(TAG,"设备的ProductId值为："+device.getProductId());
                    Log.i(TAG,"设备的VendorId值为："+device.getVendorId());
                }
//                redUDiskDevsList();
            }
        }
    };

    /**
     * 读取usb设备列表,可能不止一个usb设备
     * @return
     */
    private boolean redUDiskDevsList() {
        boolean mU_disk_ok=false;
        //设备管理器
        UsbManager usbManager = (UsbManager) getSystemService(Context.USB_SERVICE);
        //获取U盘存储设备
        storageDevices = UsbMassStorageDevice.getMassStorageDevices(this);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(this, 0, new Intent(ACTION_USB_PERMISSION), 0);
        //一般手机只有1个OTG插口
        for (UsbMassStorageDevice device : storageDevices) {
            //读取设备是否有权限
            if (usbManager.hasPermission(device.getUsbDevice())) {
                readDevice();
                mU_disk_ok = true;
                Log.i(TAG, "获取到权限: "+mU_disk_ok);
            } else {
                Log.i(TAG,"没有权限，进行申请");
                usbManager.requestPermission(device.getUsbDevice(), pendingIntent);
            }
        }
        return mU_disk_ok;
    }

    /**
     * 读取usb设备信息
     */
    private void readDevice() {
        try {
            UsbMassStorageDevice[] devices = UsbMassStorageDevice.getMassStorageDevices(this /* Context or Activity */);
            for(UsbMassStorageDevice device: devices) {
                device.init();
                FileSystem currentFs = device.getPartitions().get(0).getFileSystem();
                Log.i(TAG, "Capacity: " + currentFs.getCapacity());
                Log.i(TAG, "Occupied Space: " + currentFs.getOccupiedSpace());
                Log.i(TAG, "Free Space: " + currentFs.getFreeSpace());
                Log.i(TAG, "Chunk size: " + currentFs.getChunkSize());
            }
            Log.i(TAG,"readDevice success!");
        } catch (IOException e) {
            e.printStackTrace();
            Log.i(TAG,"readDevice error:"+e.toString());
        }

    }

}