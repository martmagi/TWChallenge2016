package pakett.tempname.Adapters;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Locale;

import pakett.tempname.Models.ReceiptContent;
import pakett.tempname.R;
import pakett.tempname.Models.Receipt;

/**
 * Created by AnnaMC on 20.02.2016.
 */
public class ReceiptAdapter extends ArrayAdapter<Receipt> {

    private final Context context;
    private final ArrayList<Receipt> receipts;
    private final LayoutInflater inflater;

    public ReceiptAdapter(Context context, int resource, ArrayList<Receipt> receipts) {
        super(context, resource, receipts);
        this.context = context;
        this.receipts = receipts;
        this.inflater = LayoutInflater.from(context);

    }

    public View getView(int position, View convertView, ViewGroup parent) {
        View v = convertView;
        Receipt receipt = receipts.get(position);
        if (v == null) {
            v = inflater.inflate(R.layout.receipt_content_item, null);
        }




        return v;
    }
}
