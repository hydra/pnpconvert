package com.seriouslypro.googlesheets

import com.google.api.client.auth.oauth2.Credential
import com.google.api.client.http.javanet.NetHttpTransport
import com.google.api.client.json.JsonFactory
import com.google.api.client.json.gson.GsonFactory
import com.google.api.services.sheets.v4.Sheets

class SheetsBuilder {
    private static final JsonFactory JSON_FACTORY = new GsonFactory()
    private static final String APPLICATION_NAME = "DPV to Google Sheets Updater";

    Sheets build(NetHttpTransport transport, Credential credentials) {
        Sheets service = new Sheets.Builder(transport, JSON_FACTORY, credentials)
            .setApplicationName(APPLICATION_NAME)
            .build();

        return service
    }
}
