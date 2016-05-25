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

package com.app.cat.ui;

import android.support.annotation.LayoutRes;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.app.cat.R;
import com.app.cat.ui.fragment.CallFragment;
import com.app.cat.ui.listener.CallFragmentListener;
import com.app.cat.ui.fragment.IncomingCallFragment;
import com.app.cat.ui.listener.IncomingCallFragmentListener;
import com.app.cat.util.ApplicationContext;

import butterknife.ButterKnife;

/**
 * The Call Activity represents the user interface for outgoing or incoming calls.
 *
 * @author Andreas Sekulski, Dimitry Kotlovsky
 */
public class CallActivity extends AppCompatActivity implements CallFragmentListener, IncomingCallFragmentListener {

    /**
     * The Fragment Transaction object supports the replacement of fragments.
     */
    FragmentTransaction fragmentTransaction;

    @Override
    public void setContentView(@LayoutRes int layoutResID) {
        super.setContentView(layoutResID);
        ButterKnife.bind(this);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_call);

        if (savedInstanceState == null) {
            fragmentTransaction = getSupportFragmentManager().beginTransaction().add(R.id.callUIContainer, new IncomingCallFragment());
            fragmentTransaction.commit();
        }

        // Set UI application context
        ApplicationContext.setContext(this);
    }

    @Override
    public void onAcceptCall() {
        fragmentTransaction = getSupportFragmentManager().beginTransaction().replace(R.id.callUIContainer, new CallFragment());
        fragmentTransaction.commit();
    }

    @Override
    public void onDeclineCall() {
        ApplicationContext.runIntent(ApplicationContext.ACTIVITY_MAIN);
    }

    @Override
    public void onHangUp() {
        ApplicationContext.runIntent(ApplicationContext.ACTIVITY_MAIN);
    }
}
