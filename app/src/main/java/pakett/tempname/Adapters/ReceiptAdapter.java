package pakett.tempname.Adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import org.w3c.dom.Text;

import java.util.ArrayList;

import pakett.tempname.Receipt;
import pakett.tempname.Models.ReceiptContent;
import pakett.tempname.R;

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
            v = inflater.inflate(R.layout.receipt_item, null);

        }

        LinearLayout contentHolder = (LinearLayout) v.findViewById(R.id.content_holder);
        contentHolder.removeAllViews();

        TextView date = (TextView) v.findViewById(R.id.receipt_date);
        TextView total = (TextView) v.findViewById(R.id.receipt_total);

        date.setText(receipt.getDate());
        total.setText(receipt.getPrice() + " €");
        System.out.println(receipt.getContentList().size());
        int i = receipt.getContentList().size()/3*2;
        int b = 1;
        for (ReceiptContent content:receipt.getContentList()
             ) {
            LinearLayout contentView = (LinearLayout) inflater.inflate(R.layout.receipt_content_item, null);
            TextView tv = (TextView) contentView.findViewById(R.id.receipt_element_name);
            TextView tv2 = (TextView) contentView.findViewById(R.id.receipt_element_price);
            tv.setText(content.getName());
            tv2.setText(content.getPrice() + " €");
            if( b > i){
contentView.setBackgroundColor(context.getResources().getColor(R.color.inactive));
            }
            TextView good = (TextView) v.findViewById(R.id.good);
            TextView bad = (TextView) v.findViewById(R.id.bad);
            good.setText((receipt.getPrice())/3*2 + " €");
            bad.setText((receipt.getPrice()) / 3  + " €");
            contentHolder.addView(contentView);
            b++;
        }
        return v;
    }
}
