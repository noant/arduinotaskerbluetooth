/*
 * Author: Ilias Giechaskiel
 * Website: https://ilias.giechaskiel.com
 * Description: Class responsible for setting values to be used by action
 *
 */

package com.giechaskiel.ilias.bluetoothserialfromtasker_rks2000control;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListPopupWindow;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import com.twofortyfouram.locale.sdk.client.ui.activity.AbstractPluginActivity;

import net.dinglisch.android.tasker.TaskerPlugin;

import java.util.Objects;
import java.util.Set;

import static com.giechaskiel.ilias.bluetoothserialfromtasker_rks2000control.BundleManager.BUNDLE_STRING_MAC;
import static com.giechaskiel.ilias.bluetoothserialfromtasker_rks2000control.BundleManager.BUNDLE_STRING_MSG;


public final class PluginActivity extends AbstractPluginActivity {
    // Tag for logging
    private static final String TAG = "PluginActivity";
    // Constant for activity result
    public static final int REQUEST_ENABLE_BT = 134;

    // Variables necessary for querying and setting MAC addresses
    private BluetoothAdapter mBluetoothAdapter;
    private ListPopupWindow popupWindow;
    private String[] addresses = new String[]{};
    private String[] names = new String[]{};
    private EditText macText;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit);

        macText = (EditText) findViewById(R.id.mac);

        final Button button = (Button) findViewById(R.id.button);
        button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                // On clicking the button, ask user to enable bluetooth, and then show paired devices

                mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
                if (mBluetoothAdapter == null) {
                    Context context = getApplicationContext();
                    String msg = "Bluetooth does not supported on this device";
                    Log.w(TAG, msg);
                    Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_SHORT).show();
                    return;
                }

                if (!mBluetoothAdapter.isEnabled()) {
                    Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                    startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
                } else {
                    showPairedDevices(getBaseContext());
                }
            }
        });
    }

    // Method that popups a list of paired devices
    private void showPairedDevices(Context context) {
        if (mBluetoothAdapter == null) {
            return;
        }

        Set<BluetoothDevice> pairedDevices = mBluetoothAdapter.getBondedDevices();

        addresses = new String[pairedDevices.size()];
        names = new String[pairedDevices.size()];
        int i = 0;

        for (BluetoothDevice device : pairedDevices) {
            addresses[i] = device.getAddress();
            names[i] = device.getName() + " (" + addresses[i] + ")";
            ++i;
        }

        popupWindow = new ListPopupWindow(context);
        popupWindow.setAdapter(new ArrayAdapter(context, R.layout.list_item, names));
        popupWindow.setAnchorView(macText);

        popupWindow.setModal(true);
        popupWindow.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (position >= 0 && position < addresses.length) {
                    macText.setText(addresses[position]);
                }
                popupWindow.dismiss();
            }
        });
        popupWindow.show();
    }

    // Method that gets called once request to enable bluetooth completes
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_ENABLE_BT && resultCode == RESULT_OK) {
            showPairedDevices(getBaseContext());
        }
    }

    // Method that checks if bundle is valid
    @Override
    public boolean isBundleValid(Bundle bundle) {
        return BundleManager.isBundleValid(bundle);
    }

    // Method that uses previously saved bundle
    @Override
    public void onPostCreateWithPreviousResult(Bundle bundle, String s) {
        final String mac = BundleManager.getMac(bundle);
        final String command = BundleManager.getMsg(bundle);
        macText.setText(mac);

        RadioGroup rgCommands = ((RadioGroup) findViewById(R.id.rgCommands));

        for (int i = 0; i < rgCommands.getChildCount(); i++)
        {
            RadioButton rb = (RadioButton) rgCommands.getChildAt(i);
            if (Objects.equals(rb.getTag().toString(), command))
            {
                rb.setChecked(true);
                break;
            }
        }
    }

    // Method that returns the bundle to be saved
    @Override
    public Bundle getResultBundle() {
        String mac = macText.getText().toString();
        String msg = null;
        RadioGroup rgCommands = ((RadioGroup) findViewById(R.id.rgCommands));
        int rbCheckedId = rgCommands.getCheckedRadioButtonId();
        if (rbCheckedId > 0)
        {
            RadioButton rbChecked = ((RadioButton) findViewById(rbCheckedId));
            msg = rbChecked.getTag().toString();
        }

        Bundle bundle = BundleManager.generateBundle(mac, msg);

        if (bundle == null) {
            Context context = getApplicationContext();
            String error = BundleManager.getErrorMessage(context, mac, msg);
            if (error != null) {
                Toast.makeText(context, error, Toast.LENGTH_LONG).show();
            } else {
                Log.e(TAG, "Null bundle, but no error");
            }
            return null;
        }

        if (TaskerPlugin.Setting.hostSupportsOnFireVariableReplacement(this)) {
            TaskerPlugin.Setting.setVariableReplaceKeys(bundle, new String[]{
                    BUNDLE_STRING_MAC,
                    BUNDLE_STRING_MSG});
        }

        return bundle;
    }

    // Method that creates summary of bundle
    @Override
    public String getResultBlurb(Bundle bundle) {
        return BundleManager.getBundleBlurb(bundle);
    }
}
