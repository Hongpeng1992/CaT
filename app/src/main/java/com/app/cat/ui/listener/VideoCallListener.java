package com.app.cat.ui.listener;

import android.os.Bundle;
import android.view.View;

import com.app.cat.client.CATClient;
import com.app.cat.model.CATFriend;
import com.app.cat.ui.CallActivity;
import com.app.cat.ui.adapter.TelephoneBookAdapter;
import com.app.cat.util.ApplicationContext;
import com.app.cat.util.PermissionManager;

/**
 * Video call listener implementation for an audio call click event.
 *
 * @author Andreas Sekulski, Dimitri Kotlovsky
 */
public class VideoCallListener implements View.OnClickListener {

    /**
     * Position which item was clicked.
     */
    private int position;

    /**
     * Model entity to get data.
     */
    private TelephoneBookAdapter adapter;

    /**
     * Cat client to communicate.
     */
    private CATClient client;

    /**
     * Audio call listener constructor to create an video call event.
     * @param position Position which model from adapter is clicked.
     * @param adapter Model adapter
     * @param client Client to start an video call.
     */
    public VideoCallListener(int position, TelephoneBookAdapter adapter, CATClient client) {
        this.position = position;
        this.adapter = adapter;
        this.client = client;
    }

    @Override
    public void onClick(View v) {

        CATFriend friend = adapter.getItem(position);

        // Try to call a friend
        if (!PermissionManager.havePermissions(PermissionManager.PERMISSIONS_AUDIO_CAMERA)) {
            PermissionManager.requestPermissions(
                    PermissionManager.PERMISSIONS_AUDIO_CAMERA,
                    PermissionManager.REQUEST_PERMISSIONS_OUTGOING_CALL);
            adapter.setCatFriend(friend);
            adapter.setVideoCall(true);
        } else {
            // Open Call Activity and call friend
            ApplicationContext.runIntent(ApplicationContext.ACTIVITY_VIDEOCALL);
            client.callFriend(true, friend);
        }
    }
}