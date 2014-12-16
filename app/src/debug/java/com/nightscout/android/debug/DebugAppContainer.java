package com.nightscout.android.debug;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.support.v4.widget.DrawerLayout;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import com.nightscout.android.R;
import com.nightscout.android.ui.AppContainer;

import javax.inject.Inject;
import javax.inject.Singleton;

import butterknife.ButterKnife;
import butterknife.InjectView;

/**
 * An {@link AppContainer} for debug builds which wrap the content view with a sliding drawer on
 * the right that holds all of the debug information and settings.
 */
@Singleton
public class DebugAppContainer implements AppContainer {

  private final Application application;

  Activity activity;
  Context drawerContext;

  @Inject
  public DebugAppContainer(Application application) {
    this.application = application;
  }

  @InjectView(R.id.debug_drawer_layout) DrawerLayout drawerLayout;


  @Override
  public ViewGroup get(Activity activity) {
    this.activity = activity;
    drawerContext = activity;

    // Manually find the debug drawer and inflate the drawer layout inside of it.
    ViewGroup drawer = findById(activity, R.id.debug_drawer);
    LayoutInflater.from(drawerContext).inflate(R.layout.debug_drawer_content, drawer);

    // Inject after inflating the drawer layout so its views are available to inject.
    ButterKnife.inject(this, activity);


    return null;
  }
}
