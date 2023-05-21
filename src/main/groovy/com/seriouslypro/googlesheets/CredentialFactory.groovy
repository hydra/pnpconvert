package com.seriouslypro.googlesheets

import com.google.api.client.auth.oauth2.Credential
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets
import com.google.api.client.http.javanet.NetHttpTransport
import com.google.api.client.json.JsonFactory
import com.google.api.client.json.gson.GsonFactory
import com.google.api.client.util.store.FileDataStoreFactory
import com.google.api.services.sheets.v4.SheetsScopes

/**
 * Get credentials from the Google API Console, see:
 * 'APIs and Services' ->
 * 'Credentials' ->
 * 'OAuth 2.0 Client IDs'.
 *
 * Then create one, and download the 'Client secrets' `.json` file and store it as-required,
 * e.g.
 * `credentials\test-credentials.json`
 * `credentials\secret-credentials.json`
 * etc.
 *
 * Reference 1: https://developers.google.com/api-client-library/java/google-api-java-client/oauth2
 * Reference 2: https://console.cloud.google.com/apis/credentials/oauthclient
 *
 * Example: https://console.cloud.google.com/welcome?pli=1&project=component-manager-319920
 */
class CredentialFactory {

    private static final JsonFactory JSON_FACTORY = new GsonFactory()
    private static final String TOKENS_DIRECTORY_PATH = "tokens"
    private static final List<String> SCOPES = Collections.singletonList(SheetsScopes.SPREADSHEETS);

    Credential getCredential(NetHttpTransport transport, String credentialsFileName) {
        // Load client secrets.

        InputStream input = new FileInputStream(credentialsFileName)
        if (input == null) {
            throw new FileNotFoundException("File not found: " + credentialsFileName);
        }
        GoogleClientSecrets clientSecrets = GoogleClientSecrets.load(JSON_FACTORY, new InputStreamReader(input));

        // Build flow and trigger user authorization request.
        GoogleAuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow.Builder(
            transport, JSON_FACTORY, clientSecrets, SCOPES)
            .setDataStoreFactory(new FileDataStoreFactory(new File(TOKENS_DIRECTORY_PATH)))
            .setAccessType("offline")
            .build();
        LocalServerReceiver receiver = new LocalServerReceiver.Builder().setPort(8888).build();
        return new AuthorizationCodeInstalledApp(flow, receiver).authorize("user");
    }
}
