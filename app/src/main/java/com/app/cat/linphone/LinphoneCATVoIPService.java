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

package com.app.cat.linphone;

import android.os.Handler;

import com.app.cat.client.CATClient;
import com.app.cat.service.VoIPService;

/**
 * Class to create an background service on an android device to update VoIPService calls.
 *
 * @author Andreas Sekulski
 */
public class LinphoneCATVoIPService implements Runnable, VoIPService {

    /**
     * Constant interval to call updates from an SIP server in ms.
     */
    private static final long NOTIFY_INTERVAL = 50;

    /**
     * Handler to call this runnable periodically;
     */
    private Handler handler;

    /**
     * Corresponding client object to handle Client Server communication.
     */
    private CATClient client;

    /**
     * Boolean indicator to loop.
     */
    private boolean isRunning;

    /**
     * Returns <code>true</code> if the registration was successful.
     */
    private boolean isRegistrationSuccessful;

    /**
     * Creates an Voice over IP event handler to update periodically an SIP server status.
     */
    public LinphoneCATVoIPService(CATClient client) {
        super();
        this.handler = new Handler();
        this.client = client;
        this.isRunning = false;
    }

    @Override
    public void run() {
        if(isRunning) {
            client.updateServerInformation();
            // Call runnable again after a NOTIFY_INTERVAL
            handler.postDelayed(this, NOTIFY_INTERVAL);
        }
    }

    @Override
    public void stop() {
        logout();
        isRunning = false;
    }

    @Override
    public void start() {
        if(!isRunning()) {
            isRunning = true;
            login();
            if (isRegistrationSuccessful) {
                run();
            }
        }
    }

    @Override
    public boolean isRunning() {
        return isRunning;
    }

    /**
     * Login user to sip server.
     */
    private void login() {
        client.unregister();
        isRegistrationSuccessful = client.register();
        if (isRegistrationSuccessful) {
            client.enablePresenceStatus();
        }
    }

    /**
     * Logout from sip server.
     */
    private void logout() {
        client.disablePresenceStatus();
        // ToDo := Unregister should wait until presence is done !!!
        client.unregister();
    }
}