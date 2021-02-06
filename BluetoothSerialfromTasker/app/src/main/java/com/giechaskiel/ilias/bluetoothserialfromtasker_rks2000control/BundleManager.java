/*
 * Author: Ilias Giechaskiel
 * Website: https://ilias.giechaskiel.com
 * Description: Class responsible for creating and validating bundles.
 *
 */


package com.giechaskiel.ilias.bluetoothserialfromtasker_rks2000control;

import android.app.Notification;
import android.app.NotificationManager;
import android.content.Context;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import java.util.regex.Pattern;



public class BundleManager {
    // For logging
    private final static String TAG = "BundleManager";

    public final static String PACKAGE_NAME = "com.giechaskiel.ilias.bluetoothserialfromtasker_rks2000control";

    // Keys for bundle
    public final static String BUNDLE_STRING_MAC = PACKAGE_NAME + ".STRING_MAC";
    public final static String BUNDLE_STRING_MSG = PACKAGE_NAME + ".STRING_MSG";

    // only accept valid MAC addresses of form 00:11:22:AA:BB:CC, where colons can be dashes
    private static boolean isMacValid(String mac) {
        if (mac == null) {
            return false;
        }

        // We allow variable MACs
        if (mac.startsWith("%")) {
            return true;
        }

        return Pattern.matches("([0-9a-fA-F]{2}[:-]){5}[0-9a-fA-F]{2}", mac);
    }

    // Whether the bundle is valid. Strings must be non-null, and either variables
    // or valid format (correctly-formatted MAC, non-empty, proper hex if binary, etc.)
    public static boolean isBundleValid(final Bundle bundle) {
        if (bundle == null) {
            Log.w(TAG, "Null bundle");
            return false;
        }

        String[] keys = { BUNDLE_STRING_MAC, BUNDLE_STRING_MSG};
        for (String key: keys) {
            if (!bundle.containsKey(key)) {
                Log.w(TAG, "Bundle missing key " + key);
            }
        }

        String mac = getMac(bundle);
        if (!isMacValid(mac)) {
            Log.w(TAG, "Invalid MAC");
            return false;
        }

        String msg = getMsg(bundle);
        if (msg == null) {
            Log.w(TAG, "Null message");
            return false;
        }

        if (msg.isEmpty()) {
            Log.w(TAG, "Empty message");
            return false;
        }

        return true;
    }

    // method to get error message for the given values, or null if no error exists
    public static String getErrorMessage(Context context, final String mac, final String msg) {
        if (!isMacValid(mac)) {
            return "Invalid Mac";
        }
        if (msg == null || msg.isEmpty()) {
            return "Message not selected";
        }
        return null;
    }

    // Method to create bundle from the individual values
    public static Bundle generateBundle(final String mac, final String msg) {
        if (mac == null || msg == null) {
            return null;
        }

        final Bundle bundle = new Bundle();
        bundle.putString(BUNDLE_STRING_MAC, mac);
        bundle.putString(BUNDLE_STRING_MSG, msg);

        if (!isBundleValid(bundle)) {
            return null;
        } else {
            return bundle;
        }
    }

    // Method for getting short String description of bundle
    public static String getBundleBlurb(final Bundle bundle) {
        if (!isBundleValid(bundle)) {
            return null;
        }

        final String mac = getMac(bundle);
        final String msg = getMsg(bundle);

        final String nr_string = "\r";

        final int max_len = 60;
        final int nr_len = nr_string.length();
        final String ellipses = "...";

        StringBuilder builder = new StringBuilder();
        builder.append(mac);
        builder.append(" <- ");
        builder.append(msg);

        int length = builder.length() + nr_len;

        if (length > max_len) {
            builder.delete(max_len - nr_len - ellipses.length(), length);
            builder.append(ellipses);
        }

        builder.append(nr_string);

        return builder.toString();
    }

    // Method to get MAC address of bundle
    public static String getMac(final Bundle bundle) {
        return bundle.getString(BUNDLE_STRING_MAC, null);
    }

    // Method to get message part of bundle
    public static String getMsg(final Bundle bundle) {
        return bundle.getString(BUNDLE_STRING_MSG, null);
    }

    // method to get the message bytes for the given bundle, or null if the bundle is invalid
    public static byte[] getMsgBytes(final Bundle bundle) {
        if (!isBundleValid(bundle)) {
            return null;
        }
        String msg = getMsg(bundle) + "\r";

        byte[] msg_bytes = msg.getBytes();

        return msg_bytes;
    }
}
