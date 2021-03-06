/*
 * This program is an Voice over IP client for Android devices.
 * Copyright (C) 2016 Andreas Sekulski, Dimitri Kotlovsky
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.app.cat.ui;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.LayoutRes;
import android.support.v4.content.LocalBroadcastManager;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.app.cat.R;
import com.app.cat.client.CATClient;
import com.app.cat.linphone.LinphoneCATClient;
import com.app.cat.model.CATFriend;
import com.app.cat.model.CATUser;
import com.app.cat.service.CATService;
import com.app.cat.ui.adapter.TelephoneBookAdapter;
import com.app.cat.util.ApplicationContext;
import com.app.cat.util.HashGenerator;
import com.app.cat.util.PermissionManager;
import com.app.cat.util.PropertiesLoader;

import org.linphone.core.LinphoneCoreException;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * The Main Activity represents the main user interface.
 *
 * @author Andreas Sekulski, Dimitri Kotlovsky
 */
public class PhoneBookActivity extends AppCompatActivity
        implements ActivityCompat.OnRequestPermissionsResultCallback {

    /**
     * List view ui element for an telephone book.
     */
    @Bind(R.id.listViewTBook)
    public ListView listTBook;

    /**
     * Information from user which is used.
     */
    @Bind(R.id.testViewInfo)
    public TextView info;

    /**
     * Status bar view to show error messages.
     */
    @Bind(R.id.statusBar)
    public LinearLayout statusBarLayout;

    /**
     * Error text view.
     */
    @Bind(R.id.textViewErrorMessage)
    public TextView statusBarText;

    /**
     * Adapter which handles telephone book ui with corresponding user models.
     */
    private TelephoneBookAdapter telephoneBookAdapter;

    /**
     * CATUser model from an SIP-User.
     */
    private CATUser catUser;

    /**
     * Mockup cat friend.
     */
    private CATFriend catFriend;

    /**
     * Configuration file to mockup user data from assets file.
     */
    private Map<String, String> configuration;

    /**
     * Service implementation which runs in background.
     */
    private Intent service;

    /**
     * Cat client instance.
     */
    private CATClient client;

    /**
     * Broadcast receiver to handle incoming intent calls.
     */
    private BroadcastReceiver receiver;

    @Override
    public void setContentView(@LayoutRes int layoutResID) {
        super.setContentView(layoutResID);
        ButterKnife.bind(this);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Set UI application context
        ApplicationContext.setActivity(this);

        // Mockup telephone book ui data.
        List<CATFriend> catAccounts = new ArrayList<CATFriend>();
        telephoneBookAdapter = new TelephoneBookAdapter(this, catAccounts);
        listTBook.setAdapter(telephoneBookAdapter);

        receiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if(intent.hasExtra(ApplicationContext.KEY_SHOW_ERROR_MESSAGE)) {
                    statusBarText.setText(intent.getStringExtra(ApplicationContext.KEY_SHOW_ERROR_MESSAGE));
                    statusBarLayout.setVisibility(View.VISIBLE);
                    listTBook.setVisibility(View.GONE);
                } else if(intent.hasExtra(ApplicationContext.KEY_HIDE_ERROR_MESSAGE)) {
                    statusBarText.setText("");
                    statusBarLayout.setVisibility(View.GONE);
                    listTBook.setVisibility(View.VISIBLE);
                }
            }
        };

        try {
            // ToDo: Check transport choosing in linphone implementation: {@link LinphonePreferenes}
            int udp = 0;
            int tcp = 5060;
            int tls = 0;

            // Get singleton object.
            client = LinphoneCATClient.getInstance();
            client.setTransportType(udp, tcp, tls);

            configuration = PropertiesLoader.loadProperty(
                    this.getAssets().open("config.properties"),
                    Arrays.asList("username", "password", "domain", "friendUsername"));

            catUser = new CATUser(
                    configuration.get("username"),
                    configuration.get("password"),
                    configuration.get("domain"));

            Log.v("Password_HA1", HashGenerator.ha1(catUser.getUsername(), catUser.getDomain(), configuration.get("password")));
            Log.v("Password_HA1B", HashGenerator.ha1b(catUser.getUsername(), catUser.getDomain(), configuration.get("password")));

            info.setText("User = " + configuration.get("username"));

            catFriend = new CATFriend(configuration.get("friendUsername"), configuration.get("domain"));
            catAccounts.add(catFriend);

            client.addCATFriend(catFriend);
            client.register(catUser);

            // Starts an service in background
            service = new Intent(PhoneBookActivity.this, CATService.class);
            startService(service);
        } catch (LinphoneCoreException | NoSuchAlgorithmException e) {
            ApplicationContext.showToast(
                    ApplicationContext.getStringFromRessources(R.string.unknown_error_message),
                    Toast.LENGTH_SHORT);
            e.printStackTrace();
        } catch (IOException e) {
            info.setText("No mockup data set");
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        LocalBroadcastManager.getInstance(this).registerReceiver((receiver),
                new IntentFilter(ApplicationContext.MAIN_ACTIVITY_CLASS)
        );
    }

    @Override
    protected void onStop() {
        super.onStop();
        LocalBroadcastManager.getInstance(this).unregisterReceiver(receiver);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        // ToDo: Productive this service don't die! FOREVER YOUNG!
        // Activity stops... kill background server
        // In productive this service runs all the time as an sub process.
        if(service != null) {
            stopService(service);
            service = null;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        // Process request results
        switch (requestCode) {
            case PermissionManager.REQUEST_PERMISSIONS_OUTGOING_CALL: {

                // Check granted permissions
                boolean permissionGranted = (grantResults.length > 0);
                for (int i = 0; (i < grantResults.length) && permissionGranted; i++) {
                    permissionGranted = (grantResults[i] == PackageManager.PERMISSION_GRANTED);
                }

                // Open Call Activity and call friend
                if (permissionGranted) {
                    Bundle bundle = new Bundle();
                    bundle.putInt(CallActivity.KEY_FRAGMENT_ID, CallActivity.FRAGMENT_OUTGOING_CALL);
                    ApplicationContext.runIntentWithParams(ApplicationContext.ACTIVITY_CALL, bundle);
                    client.callFriend(telephoneBookAdapter.isVideoCall(), telephoneBookAdapter.getCatFriend());
                } else {
                    PermissionManager.firstPermissionRequest = false;
                    ApplicationContext.showToast(ApplicationContext.getStringFromRessources(
                            R.string.permission_denied),
                            Toast.LENGTH_LONG);
                }

                return;
            }
        }
    }
}