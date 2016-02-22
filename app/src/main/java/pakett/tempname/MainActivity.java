package pakett.tempname;

import android.accounts.AccountManager;
import android.app.Dialog;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.LinearLayout;
import android.widget.ListView;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.googleapis.extensions.android.gms.auth.GooglePlayServicesAvailabilityIOException;
import com.google.api.client.googleapis.extensions.android.gms.auth.UserRecoverableAuthIOException;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.Base64;
import com.google.api.client.util.ExponentialBackOff;
import com.google.api.services.gmail.GmailScopes;
import com.google.api.services.gmail.model.ListMessagesResponse;
import com.google.api.services.gmail.model.Message;
import com.google.api.services.gmail.model.MessagePartHeader;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import pakett.tempname.Adapters.ReceiptAdapter;


public class MainActivity extends AppCompatActivity {
    GoogleAccountCredential mCredential;
    //ProgressDialog mProgress;
    DBHelper db;

    static final int REQUEST_ACCOUNT_PICKER = 1000;
    static final int REQUEST_AUTHORIZATION = 1001;
    static final int REQUEST_GOOGLE_PLAY_SERVICES = 1002;
    private static final String PREF_ACCOUNT_NAME = "";
    private static final String[] SCOPES = {GmailScopes.GMAIL_LABELS, GmailScopes.GMAIL_READONLY, GmailScopes.MAIL_GOOGLE_COM};

    /**
     * Create the main activity.
     *
     * @param savedInstanceState previously saved instance data.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        LinearLayout activityLayout = new LinearLayout(this);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT);
        activityLayout.setLayoutParams(lp);
        activityLayout.setOrientation(LinearLayout.VERTICAL);
        activityLayout.setPadding(16, 16, 16, 16);
        setContentView(R.layout.activity_main);

        ListView recieptView = (ListView) findViewById(R.id.receipt_list);

        final ArrayList<Receipt> list = new ArrayList<>();

        Receipt receipt1 = new Receipt("1", 36, Receipt.parseDateString("Sat, 20 Feb 2016 16:28:28 +0200"));
        Receipt receipt2 = new Receipt("2", 54, Receipt.parseDateString("Sat, 20 Feb 2016 16:28:28 +0200"));
        Receipt receipt3 = new Receipt("3", 126, Receipt.parseDateString("Sat, 20 Feb 2016 16:28:28 +0200"));

        list.add(receipt1);
        list.add(receipt2);
        list.add(receipt3);
        ReceiptAdapter itemsAdapter =
                new ReceiptAdapter(this, 0, list);
        recieptView.setAdapter(itemsAdapter);
        Thread thread = new Thread() {
            @Override
            public void run() {
                forGoogle();
            }
        };
        thread.run();
    }

    private void forGoogle() {
        db = new DBHelper(this);
        db.readFromDB();
        // db.insertIntoDB(new Receipt("Peeter", 900, db.calcDate(0)));

        // The number in the argument defines the length of the period in stages
        // 1-current day, 2-last week, 3-last month

        db.readSpecificFromDB(1);
        db.readSpecificFromDB(2);
        db.readSpecificFromDB(3);
        db.closeDB();

        // Initialize credentials and service object.
        SharedPreferences settings = getPreferences(Context.MODE_PRIVATE);
        mCredential = GoogleAccountCredential.usingOAuth2(
                getApplicationContext(), Arrays.asList(SCOPES))
                .setBackOff(new ExponentialBackOff())
                .setSelectedAccountName(settings.getString(PREF_ACCOUNT_NAME, null));
    }

    /**
     * Called whenever this activity is pushed to the foreground, such as after
     * a call to onCreate().
     */
    @Override
    protected void onResume() {
        super.onResume();
        if (isGooglePlayServicesAvailable()) {
            refreshResults();
        }
    }

    /**
     * Called when an activity launched here (specifically, AccountPicker
     * and authorization) exits, giving you the requestCode you started it with,
     * the resultCode it returned, and any additional data from it.
     *
     * @param requestCode code indicating which activity result is incoming.
     * @param resultCode  code indicating the result of the incoming
     *                    activity result.
     * @param data        Intent (containing result data) returned by incoming
     *                    activity result.
     */
    @Override
    protected void onActivityResult(
            int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case REQUEST_GOOGLE_PLAY_SERVICES:
                if (resultCode != RESULT_OK) {
                    isGooglePlayServicesAvailable();
                }
                break;
            case REQUEST_ACCOUNT_PICKER:
                if (resultCode == RESULT_OK && data != null &&
                        data.getExtras() != null) {
                    String accountName =
                            data.getStringExtra(AccountManager.KEY_ACCOUNT_NAME);
                    if (accountName != null) {
                        mCredential.setSelectedAccountName(accountName);
                        SharedPreferences settings =
                                getPreferences(Context.MODE_PRIVATE);
                        SharedPreferences.Editor editor = settings.edit();
                        editor.putString(PREF_ACCOUNT_NAME, accountName);
                        editor.apply();
                    }
                } else if (resultCode == RESULT_CANCELED) {
                    Log.d("Google", "Account unspecified");
                }
                break;
            case REQUEST_AUTHORIZATION:
                if (resultCode != RESULT_OK) {
                    chooseAccount();
                }
                break;
        }

        super.onActivityResult(requestCode, resultCode, data);
    }

    /**
     * Attempt to get a set of data from the Gmail API to display. If the
     * email address isn't known yet, then call chooseAccount() method so the
     * user can pick an account.
     */
    private void refreshResults() {
        if (mCredential.getSelectedAccountName() == null) {
            chooseAccount();
        } else {
            if (isDeviceOnline()) {
                new MakeRequestTask(mCredential).execute();
            } else {
                Log.d("Google", "No network connection available.");
            }
        }
    }

    /**
     * Starts an activity in Google Play Services so the user can pick an
     * account.
     */
    private void chooseAccount() {
        startActivityForResult(
                mCredential.newChooseAccountIntent(), REQUEST_ACCOUNT_PICKER);
    }

    /**
     * Checks whether the device currently has a network connection.
     *
     * @return true if the device has a network connection, false otherwise.
     */
    private boolean isDeviceOnline() {
        ConnectivityManager connMgr =
                (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        return (networkInfo != null && networkInfo.isConnected());
    }

    /**
     * Check that Google Play services APK is installed and up to date. Will
     * launch an error dialog for the user to update Google Play Services if
     * possible.
     *
     * @return true if Google Play Services is available and up to
     * date on this device; false otherwise.
     */
    private boolean isGooglePlayServicesAvailable() {
        final int connectionStatusCode =
                GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);
        if (GooglePlayServicesUtil.isUserRecoverableError(connectionStatusCode)) {
            showGooglePlayServicesAvailabilityErrorDialog(connectionStatusCode);
            return false;
        } else if (connectionStatusCode != ConnectionResult.SUCCESS) {
            return false;
        }
        return true;
    }

    /**
     * Display an error dialog showing that Google Play Services is missing
     * or out of date.
     *
     * @param connectionStatusCode code describing the presence (or lack of)
     *                             Google Play Services on this device.
     */
    void showGooglePlayServicesAvailabilityErrorDialog(
            final int connectionStatusCode) {
        Dialog dialog = GooglePlayServicesUtil.getErrorDialog(
                connectionStatusCode,
                MainActivity.this,
                REQUEST_GOOGLE_PLAY_SERVICES);
        dialog.show();
    }

    /**
     * An asynchronous task that handles the Gmail API call.
     * Placing the API calls in their own task ensures the UI stays responsive.
     */
    private class MakeRequestTask extends AsyncTask<Void, Void, List<String>> {
        private com.google.api.services.gmail.Gmail mService = null;
        private Exception mLastError = null;

        public MakeRequestTask(GoogleAccountCredential credential) {
            HttpTransport transport = AndroidHttp.newCompatibleTransport();
            JsonFactory jsonFactory = JacksonFactory.getDefaultInstance();
            mService = new com.google.api.services.gmail.Gmail.Builder(
                    transport, jsonFactory, credential)
                    .setApplicationName("Gmail API Android Quickstart")
                    .build();
        }

        /**
         * Background task to call Gmail API.
         *
         * @param params no parameters needed for this task.
         */
        @Override
        protected List<String> doInBackground(Void... params) {
            try {
                return getDataFromApi();
            } catch (Exception e) {
                mLastError = e;
                cancel(true);
                return null;
            }
        }

        /**
         * Fetch a list of Gmail message that came from seb.
         *
         * @return List of Strings labels.
         * @throws IOException
         */
        private List<String> getDataFromApi() throws IOException {
            String user = "me";
            ListMessagesResponse messages = mService.users().messages().list(user).execute();
            List<Receipt> found = new ArrayList<>();
            List<String> metaHeaders = new ArrayList<>();
            metaHeaders.add("Return-Path");
            metaHeaders.add("Date");
            int counter = 0;
            for (Message message : messages.getMessages()) {
                List<MessagePartHeader> headers = mService.users().messages().get(user, message.getId()).setMetadataHeaders(metaHeaders).execute().getPayload().getHeaders();
                String content = "";
                boolean foundEntry = false;
                for (MessagePartHeader header : headers) {
                    if (header.getName().equals("Return-Path") && header.getValue().equals("<automailer@seb.ee>")) {
                        foundEntry = true;
                        content = new String(Base64.decodeBase64(mService.users().messages().get(user, message.getId()).execute().getPayload().getParts().get(0).getBody().getData()), "UTF-8");
                    }
                    if (foundEntry && header.getName().equals("Date")) {
                        String dateString = header.getValue();
                        found.add(Receipt.stringToReceipt(content, dateString));
                        break;
                    }
                }
                counter++;
                if (counter > 5) {
                    break;
                }
            }
            DBHelper dbHelper = new DBHelper(MainActivity.this);
            dbHelper.truncateDB();
            for (Receipt receipt : found) {
                callNotification(receipt);
            }
            dbHelper.readFromDB();
            return new ArrayList<>();
        }

        @Override
        protected void onCancelled() {
            if (mLastError != null) {
                if (mLastError instanceof GooglePlayServicesAvailabilityIOException) {
                    showGooglePlayServicesAvailabilityErrorDialog(
                            ((GooglePlayServicesAvailabilityIOException) mLastError)
                                    .getConnectionStatusCode());
                } else if (mLastError instanceof UserRecoverableAuthIOException) {
                    startActivityForResult(
                            ((UserRecoverableAuthIOException) mLastError).getIntent(),
                            MainActivity.REQUEST_AUTHORIZATION);
                } else {
                    Log.d("Google", "Following error occured: " + mLastError.getMessage());
                }
            }
        }
    }

    public void callNotification(Receipt receipt) {

        //Create const ID for possible multiple notifications
        int notificationId = 0;

        //Create Intents for the BroadcastReceiver
        //Decline button intent
        Intent declineIntentBase = new Intent("pakett.tempname.decline");
        declineIntentBase.putExtra("notificationId", notificationId);
        declineIntentBase.putExtra("company", receipt.getCompanyName());
        declineIntentBase.putExtra("price", receipt.getPrice());
        declineIntentBase.putExtra("date", receipt.getDate());

        //Accept button intent
        Intent acceptIntentBase = new Intent("pakett.tempname.accept");
        acceptIntentBase.putExtra("notificationId", notificationId);
        acceptIntentBase.putExtra("company", receipt.getCompanyName());
        acceptIntentBase.putExtra("price", receipt.getPrice());
        acceptIntentBase.putExtra("date", receipt.getDate());

        //Create the PendingIntents
        PendingIntent declineIntent = PendingIntent.getBroadcast(MainActivity.this, 0, declineIntentBase, 0);
        PendingIntent acceptIntent = PendingIntent.getBroadcast(MainActivity.this, 0, acceptIntentBase, 0);

        //Create the notification
        NotificationCompat.Builder builder = new NotificationCompat.Builder(getBaseContext());
        builder.setPriority(NotificationCompat.PRIORITY_MAX)
                .setDefaults(Notification.DEFAULT_ALL)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle("You spent " + receipt.getPrice() + " at " + receipt.getCompanyName() + " on " + receipt.getDate())
                .setContentText("Was it really necessary?")
                .setAutoCancel(true)
                .addAction(R.mipmap.ic_thumb_down_black_24dp, "Not really..", declineIntent)
                .addAction(R.mipmap.ic_thumb_up_black_24dp, "Of course!", acceptIntent);

        // Gets an instance of the NotificationManager service
        NotificationManager notifyMgr = (NotificationManager) this.getSystemService(Context.NOTIFICATION_SERVICE);

        // Builds the notification and issues it.
        notifyMgr.notify(notificationId, builder.build());

    }
}
