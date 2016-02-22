package pakett.tempname;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;

import pakett.tempname.Models.ReceiptContent;

/**
 * Created by Mart on 20/02/2016.
 */
public class Receipt {
    private int id;
    private boolean useful;
    private String companyName;
    private int price;
    private int date;

    public ArrayList<ReceiptContent> getContentList() {
        return contentList;
    }

    public void setContentList(ArrayList<ReceiptContent> contentList) {
        this.contentList = contentList;
    }

    private ArrayList<ReceiptContent> contentList;
    public Receipt() {
    }

    public Receipt(String companyName, int price, int date) {
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

    public int getDate() {
        return this.date;
    }

    public void setDate(int date) {this.date = date;}

    public int getId(){return this.id;}

    public void setId(int id) {this.id = id;}

    public static Receipt stringToReceipt(String string, int date) {
        String[] lines = string.split("\\r\\n");
        String paidSumLine = lines[4];
        String[] paidSumLines = paidSumLine.split(" ");
        int paidSum = Integer.parseInt(paidSumLines[0].replace(" ",""));

        String paidToLine = lines[5];
        String paidToSplitAtExpression = paidToLine.split(">")[0];
        String[] paidToFullExpression = paidToSplitAtExpression.split(" ");
        int paidToWordsCount = paidToFullExpression.length;

        String companyName = "";

        for (int i = 2; i < paidToWordsCount; i++) {
            companyName += paidToFullExpression[i];
        }

        Receipt receipt = new Receipt(companyName, paidSum, date);
        return receipt;
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