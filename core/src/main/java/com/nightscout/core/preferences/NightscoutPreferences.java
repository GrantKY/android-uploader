package com.nightscout.core.preferences;

import java.util.List;

public interface NightscoutPreferences {
    boolean isRestApiEnabled();
    List<String> getRestApiBaseUris();
    boolean isCalibrationUploadEnabled();
    boolean isSensorUploadEnabled();
    boolean isMongoUploadEnabled();
    boolean isDataDonateEnabled();
    void setDataDonateEnabled(boolean toDonate);
    String getMongoClientUri();
    String getMongoCollection();
    String getMongoDeviceStatusCollection();
    boolean isMqttEnabled();
    String getMqttEndpoint();
    String getMqttUser();
    // TODO: (klee) look into how to securely store this information
    String getMqttPass();
}
