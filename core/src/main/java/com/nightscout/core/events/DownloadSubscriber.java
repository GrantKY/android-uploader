package com.nightscout.core.events;

import com.nightscout.core.download.G4Download;
import com.squareup.otto.Subscribe;

public abstract class DownloadSubscriber {

  protected abstract void doHandle(G4Download download);

  @Subscribe
  public final void handleDownload(G4Download download) {
    doHandle(download);
  }
}
