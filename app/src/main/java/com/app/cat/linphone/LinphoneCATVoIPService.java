/*
 * Copyright (c) 2016.
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
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.app.cat.linphone;

import android.os.Handler;

import com.app.cat.service.VoIPService;

import org.linphone.core.LinphoneCore;

/**
 * Class to create an background service on an android device to update VoIPService calls.
 *
 * @author Andreas Sekulski
 */
public class LinphoneCATVoIPService implements Runnable, VoIPService {

    /**
     * Constant interval to call updates from an SIP server in ms.
     */
    private static final long NOTIFY_INTERVAL = 100;

    /**
     * Handler to call this runnable periodically;
     */
    private Handler handler = new Handler();

    /**
     * Corresponding core object to handle Client Server communication.
     */
    private LinphoneCore core;

    /**
     * Boolean indicator to loop.
     */
    private boolean isRunning;

    /**
     * Creates an Voice over IP event handler to update periodically an SIP server status.
     */
    public LinphoneCATVoIPService(LinphoneCore core) {
        super();
        this.core = core;
        isRunning = false;
    }

    @Override
    public void run() {
        core.iterate();
        if(isRunning) {
            // Call runnable again after an NOTIFY_INTERVAL
            handler.postDelayed(this, NOTIFY_INTERVAL);
        }
    }

    @Override
    public void stop() {
        isRunning = false;
    }

    @Override
    public void start() {
        if(!isRunning()) {
            isRunning = true;
            run();
        }
    }

    @Override
    public boolean isRunning() {
        return isRunning;
    }
}