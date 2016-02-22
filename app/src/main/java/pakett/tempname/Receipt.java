package pakett.tempname;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

import pakett.tempname.Models.ReceiptContent;

/**
 * Created by Mart on 20/02/2016.
 */
public class Receipt {
    private int id;
    private boolean useful;
    private String companyName;
    private double price;
    private Date date;

    public ArrayList<ReceiptContent> getContentList() {
        return contentList;
    }

    public void setContentList(ArrayList<ReceiptContent> contentList) {
        this.contentList = contentList;
    }

    private ArrayList<ReceiptContent> contentList;
    public Receipt() {
    }


    public Receipt(String companyName, double price, Date date) {
        this.companyName = companyName;
        this.price = price;
        this.date = date;
        this.contentList  = new ArrayList<ReceiptContent>();
        for (int i = 0; i < 6; ++i) {
            ReceiptContent receiptContent = new ReceiptContent("Yummy icecream", "6");
            this.contentList.add(receiptContent);
        }
        if("2".equals(companyName) | "3".equals(companyName)){
            for (int i = 0; i < 3; ++i) {
                ReceiptContent receiptContent = new ReceiptContent("Moooaaar icecream", "6");
                this.contentList.add(receiptContent);
            }
        }
        if("3".equals(companyName)){
            for (int i = 0; i < 12; ++i) {
                ReceiptContent receiptContent = new ReceiptContent("Icecreaaam", "6");
                this.contentList.add(receiptContent);
            }
        }
    }

    public boolean isUseful() {
        return useful;
    }

    public void setUseful(boolean useful) {
        this.useful = useful;
    }

    public String getCompanyName() {
        return companyName;
    }

    public void setCompanyName(String companyName) {
        this.companyName = companyName;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(int price) {
        this.price = price;
    }


    public Date getDate() {
        return this.date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public int getId(){return this.id;}

    public void setId(int id) {this.id = id;}

    public static Receipt stringToReceipt(String string, String stringDate) {
        String[] lines = string.split("\\r\\n");
        String paidSumLine = lines[3];
        String[] paidSumLines = paidSumLine.split(" ");
        double paidSum = Double.parseDouble(paidSumLines[1].replace(" EUR", "").replace("-",""));

        String paidToLine = lines[5];
        String paidToSplitAtExpression = paidToLine.split(">")[0];
        String[] paidToFullExpression = paidToSplitAtExpression.split(" ");
        int paidToWordsCount = paidToFullExpression.length;

        String companyName = "";

        for (int i = 2; i < paidToWordsCount; i++) {
            companyName += paidToFullExpression[i];
        }

        return new Receipt(companyName, paidSum, parseDateString(stringDate));
    }

    public static Date parseDateString(String stringDate){
        DateFormat formatter = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss ZZZ", Locale.ENGLISH);

        try {
            return formatter.parse(stringDate);
        } catch (ParseException e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public String toString() {
        return "Receipt{" +
                "useful=" + useful +
                ", companyName='" + companyName + '\'' +
                ", price=" + price +
                ", date='" + date + '\'' +
                '}';
    }
}