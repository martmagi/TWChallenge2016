package pakett.tempname;

import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import java.util.Date;

import pakett.tempname.Models.Receipt;

/**
 * Created by Risto on 2/22/2016.
 */
public class NotificationReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        //Get the extras
        Bundle bundle = intent.getExtras();
        int id = bundle.getInt("notificationId");
        String companyName = bundle.getString("company");
        double price = bundle.getDouble("price");
        Date date = (Date) intent.getSerializableExtra("date");

        //Create the receipt
        Receipt receipt = new Receipt(companyName, price, date);

        //Set the receipt's usefulness according to notification's button pressed
        if (intent.getAction().equals("pakett.tempname.decline")) {
            receipt.setUseful(false);
        } else if (intent.getAction().equals("pakett.tempname.accept")) {
            receipt.setUseful(true);
        }

        //Insert the receipt into DB
        DBHelper dbHelper = new DBHelper(context);
        dbHelper.insertIntoDB(receipt);

        //Close the notification
        NotificationManager manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        manager.cancel(id);
    }
}
