package com.nightscout.android.debug;

import android.app.Application;

import com.nightscout.android.MainActivity;
import com.nightscout.android.exceptions.Reporter;
import com.nightscout.android.exceptions.StubbedReporter;
import com.nightscout.android.modules.NightscoutModule;
import com.nightscout.android.ui.AppContainer;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

@Module(
        addsTo = NightscoutModule.class,
        injects = {
                MainActivity.class,
                DebugAppContainer.class
        },
        overrides = true
)
public final class DebugNightscoutModule {
    @Provides @Singleton AppContainer provideAppContainer(DebugAppContainer debugAppContainer) {
        return debugAppContainer;
    }
    @Provides @Singleton Reporter providesReporter(Application app) {
        return new StubbedReporter(app);
    }
}
