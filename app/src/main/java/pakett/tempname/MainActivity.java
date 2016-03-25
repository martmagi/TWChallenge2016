package pakett.tempname;

import android.accounts.AccountManager;
import android.app.Dialog;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.NotificationCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.test.RenamingDelegatingContext;
import android.text.method.ScrollingMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.util.Log;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

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
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Random;
import java.util.zip.Inflater;

import pakett.tempname.Adapters.ReceiptAdapter;
import pakett.tempname.Models.Receipt;


public class MainActivity extends AppCompatActivity {
    GoogleAccountCredential mCredential;

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

        FloatingActionButton getNewReceipts = (FloatingActionButton) findViewById(R.id.fab);
        FloatingActionButton refreshReceipts = (FloatingActionButton) findViewById(R.id.refresh);

        getNewReceipts.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Thread thread = new Thread() {
                    @Override
                    public void run() {
                        refreshResults();
                    }
                };
                thread.run();
            }
        });

        refreshReceipts.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showReceipts();
            }
        });
        forGoogle();
        setCustomActionBar();
        showReceipts();
    }

    private void showReceipts() {
        DBHelper db = new DBHelper(MainActivity.this);
        //db.truncateDB();
        LinearLayout receiptDay = (LinearLayout) findViewById(R.id.cell1);
        LinearLayout receiptWeek = (LinearLayout) findViewById(R.id.cell2);
        LinearLayout receiptMonth = (LinearLayout) findViewById(R.id.cell3);


        showReceiptContent(receiptDay, 0, db.readFromDB());
        showReceiptContent(receiptWeek, 1, getDummys(8));
        showReceiptContent(receiptMonth, 2, getDummys(15));

    }

    private ArrayList<Receipt> getDummys(int len) {
        ArrayList<Receipt> receipts = new ArrayList<>();
        Random r = new Random();

        DecimalFormat df = new DecimalFormat("#.##");
        for (int i = len; i > 0; i--) {
            double randomValue = ((int) (2 + (150 - 2) * r.nextDouble() * 100)) / 100.0;
            double price = Double.valueOf(randomValue);

            Receipt receipt = new Receipt("U wot m8", price, new Date());
            if ((i & 1) == 0) {
                receipt.setUseful(true);
            } else {
                receipt.setUseful(false);
            }
            receipts.add(receipt);
        }
        return receipts;
    }

    private void showReceiptContent(LinearLayout view, int period, ArrayList<Receipt> receipts) {
        System.out.println(receipts.size());
        String date = "";
        double total = 0;
        double good = 0;
        double bad = 0;
        DateFormat dateFormat = new SimpleDateFormat("EEE, MMM dd", getResources().getConfiguration().locale);

        long DAY_IN_MS = 1000 * 60 * 60 * 24;
        if (period == 0) {
            Date dateNow = new Date();
            date = dateFormat.format(dateNow); //2014/08/06 15:59:48
        } else if (period == 1) {
            String end = dateFormat.format(new Date(System.currentTimeMillis() - (DAY_IN_MS)));
            String start = dateFormat.format(new Date(System.currentTimeMillis() - (8 * DAY_IN_MS)));
            date = start + " - " + end;
        } else if (period == 2) {
            String end = dateFormat.format(new Date(System.currentTimeMillis() - (DAY_IN_MS)));
            String start = dateFormat.format(new Date(System.currentTimeMillis() - (30 * DAY_IN_MS)));
            date = start + " - " + end;
        }

        for (Receipt receipt : receipts) {
            total += receipt.getPrice();
            if (receipt.isUseful()) {
                good += receipt.getPrice();
            } else {
                bad += receipt.getPrice();
            }
        }
        TextView receiptDate = (TextView) view.findViewById(R.id.receipt_date);
        receiptDate.setText(date);

        DecimalFormat df = new DecimalFormat("#.##");

        TextView receiptTotal = (TextView) view.findViewById(R.id.receipt_total);
        receiptTotal.setText(df.format(total) + " €");

        TextView receiptGood = (TextView) view.findViewById(R.id.good);
        TextView receiptBad = (TextView) view.findViewById(R.id.bad);

        receiptBad.setText(df.format(bad) + " €");
        receiptGood.setText(df.format(good) + " €");


        View goodLine = (View) view.findViewById(R.id.good_line);
        View badLine = (View) view.findViewById(R.id.bad_line);

        LinearLayout.LayoutParams paramGood = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                15, (float) bad);
        LinearLayout.LayoutParams paramBad = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                15, (float) good);

        goodLine.setLayoutParams(paramGood);
        badLine.setLayoutParams(paramBad);
        populateList((LinearLayout) view.findViewById(R.id.content_holder), receipts);
    }

    private void populateList(LinearLayout view, ArrayList<Receipt> receipts) {
        LayoutInflater inflater = LayoutInflater.from(this);
        boolean allUseful = true;
        int color = 0;
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            color = getColor(R.color.inactive);
        } else {
            color = getResources().getColor(R.color.inactive);
        }

        Collections.sort(receipts, new Comparator<Receipt>() {
            @Override
            public int compare(Receipt abc1, Receipt abc2) {
                Boolean a1 = abc1.isUseful();
                Boolean a2 = abc2.isUseful();
                return a2.compareTo(a1);
            }
        });
        view.removeAllViewsInLayout();
        for (Receipt receipt : receipts) {
            LinearLayout contentView = (LinearLayout) inflater.inflate(R.layout.receipt_content_item, null);
            TextView tv = (TextView) contentView.findViewById(R.id.receipt_element_company);
            TextView tv2 = (TextView) contentView.findViewById(R.id.receipt_element_price);
            tv.setText(receipt.getCompanyName());
            tv2.setText(receipt.getPrice() + " €");
            if (!receipt.isUseful()) {
                contentView.setBackgroundColor(color);
                allUseful = false;
            }
            view.addView(contentView);
        }
        if (allUseful) {
            //((ImageView) findViewById(R.id.zigzag_bottom)).setColorFilter(Color.argb(255, 255, 255, 255));
        }
    }

    private void setCustomActionBar() {
        if (getSupportActionBar() != null) {
            ActionBar actionBar = getSupportActionBar();
            actionBar.setDisplayShowHomeEnabled(false);
            actionBar.setDisplayShowCustomEnabled(true);
            actionBar.setDisplayShowTitleEnabled(false);
            View customView = getLayoutInflater().inflate(R.layout.custom_actionbar, null);
            actionBar.setCustomView(customView);
            Toolbar parent = (Toolbar) customView.getParent();
            parent.setContentInsetsAbsolute(0, 0);
            getSupportActionBar().setIcon(R.mipmap.ic_launcher);

        }
    }

    private void forGoogle() {
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
        showReceipts();
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
                getDataFromApi();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }

        /**
         * Fetch a list of Gmail message that came from seb.
         *
         * @return List of Strings labels.
         * @throws IOException
         */
        private void getDataFromApi() throws IOException {
            List<Receipt> newExpenses = new ArrayList<>(); // to store all new expenses
            String user = "me";

            // Create connection to get all message ids
            ListMessagesResponse messages = mService.users().messages().list(user).execute();

            // Creating specific list of metaHeaders so that the result is smaller
            List<String> metaHeaders = new ArrayList<>();
            metaHeaders.add("Return-Path");
            metaHeaders.add("Date");

            int counter = 0; // for testing
            for (Message message : messages.getMessages()) {

                // Read Date and Return-Path headers for the current message
                List<MessagePartHeader> headers = mService.users().messages().get(user, message.getId()).setMetadataHeaders(metaHeaders).setFormat("metadata").execute().getPayload().getHeaders();

                // If the returned headers lists size is 2, meaning it has both dare and return-path data
                if (headers.size() == 2 && headers.get(0).getValue().equals("<automailer@seb.ee>")) {
                    String encryptedContent = mService.users().messages().get(user, message.getId()).execute().getPayload().getParts().get(0).getBody().getData();
                    String decryptedContent = new String(Base64.decodeBase64(encryptedContent), "UTF-8");
                    Receipt newReceipt = Receipt.stringToReceipt(decryptedContent, headers.get(1).getValue());
                    if (newReceipt != null) { // If the mail is not for notifying expenses
                        newExpenses.add(Receipt.stringToReceipt(decryptedContent, headers.get(1).getValue()));
                    }
                }

                // For testing, will be replaced with date checking
                if (counter > 200) {
                    break;
                }
                counter++;
            }

            // Call notification for every new expense
            for (Receipt receipt : newExpenses) {
                callNotification(receipt);
            }
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
        int notificationId = new Random().nextInt();

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
        PendingIntent declineIntent = PendingIntent.getBroadcast(MainActivity.this, new Random().nextInt(), declineIntentBase, 0);
        PendingIntent acceptIntent = PendingIntent.getBroadcast(MainActivity.this, new Random().nextInt(), acceptIntentBase, 0);

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
