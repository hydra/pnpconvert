package com.seriouslypro.googlesheets

import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport
import com.google.api.client.http.javanet.NetHttpTransport

interface TransportFactory {
    NetHttpTransport build()
}

class GoogleSheetsTransportFactory implements TransportFactory{
    @Override
    NetHttpTransport build() {
        final NetHttpTransport HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();
        return HTTP_TRANSPORT
    }
}
