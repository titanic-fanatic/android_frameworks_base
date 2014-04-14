/*
 * Copyright (C) 2012 The CyanogenMod Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.android.systemui.usb;

import android.os.SystemProperties;
import android.util.Slog;

import java.io.DataOutputStream;
import java.io.IOException;
import java.lang.InterruptedException;
import java.lang.Process;

public class Utils {

    private static final String TAG = "USB_UTILS";
    private static final String INTERNAL_LUN_FILE = "/sys/devices/virtual/android_usb/android0/f_mass_storage/lun/file";
    private static final String EXTERNAL_LUN_FILE = "/sys/devices/virtual/android_usb/android0/f_mass_storage/lun_ex/file";
    private static final String INTERNAL_VOLD_PATH = "/dev/block/vold/179:28";
    private static final String EXTERNAL_VOLD_PATH = "/dev/block/vold/179:97";
    private static final String PERSIST_USB_PROP = "persist.sys.usb.config";
    private static final String USB_PROP = "sys.usb.config";
    
    /**
     * Enable external sdcard mass storage
     */
    public static boolean enableExternalUsbMassStorage() {
        String persistValue = SystemProperties.get(PERSIST_USB_PROP);
        
        if (!persistValue.equals("") && persistValue.contains("adb")) {
            SystemProperties.set(PERSIST_USB_PROP, "mass_storage,adb");
            SystemProperties.set(USB_PROP, "mass_storage,adb");
        }
        else {
            SystemProperties.set(PERSIST_USB_PROP, "mass_storage");
            SystemProperties.set(USB_PROP, "mass_storage");
        }
        
        try {
            Process process = Runtime.getRuntime().exec("su");
            DataOutputStream out = new DataOutputStream(process.getOutputStream());
            out.writeBytes("echo \"" + INTERNAL_VOLD_PATH + "\" > " + INTERNAL_LUN_FILE + "\n");
            out.writeBytes("echo \"" + EXTERNAL_VOLD_PATH + "\" > " + EXTERNAL_LUN_FILE + "\n");
            out.writeBytes("exit\n");  
            out.flush();
            process.waitFor();
        }
        catch (IOException e) {
            Slog.e(TAG, "Failed to write values");
            e.printStackTrace();
        }
        catch (InterruptedException e) {
            Slog.e(TAG, "Shell process interrupted.");
            e.printStackTrace();
        }
        
        return true;
    }
    
    /**
     * Disable external sdcard mass storage
     */
    public static boolean disableExternalUsbMassStorage() {
        String persistValue = SystemProperties.get(PERSIST_USB_PROP);
        
        if (!persistValue.equals("") && persistValue.contains("adb")) {
            SystemProperties.set(PERSIST_USB_PROP, "mtp,adb");
            SystemProperties.set(USB_PROP, "mtp,adb");
        }
        else {
            SystemProperties.set(PERSIST_USB_PROP, "mtp");
            SystemProperties.set(USB_PROP, "mtp");
        }
        
        try {
            Process process = Runtime.getRuntime().exec("su");
            DataOutputStream out = new DataOutputStream(process.getOutputStream());
            out.writeBytes("echo \"\" > " + INTERNAL_LUN_FILE + "\n");
            out.writeBytes("echo \"\" > " + EXTERNAL_LUN_FILE + "\n");
            out.writeBytes("exit\n");  
            out.flush();
            process.waitFor();
        }
        catch (IOException e) {
            Slog.e(TAG, "Failed to clear values");
            e.printStackTrace();
        }
        catch (InterruptedException e) {
            Slog.e(TAG, "Shell process interrupted.");
            e.printStackTrace();
        }
        
        return true;
    }
}
