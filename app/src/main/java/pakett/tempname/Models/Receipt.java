package pakett.tempname.Models;

import java.util.ArrayList;

/**
 * Created by Mart on 20/02/2016.
 */
public class Receipt {
    private boolean useful;
    private String companyName;
    private double price;

    public ArrayList<ReceiptContent> getContentList() {
        return contentList;
    }

    public void setContentList(ArrayList<ReceiptContent> contentList) {
        this.contentList = contentList;
    }

    private ArrayList<ReceiptContent> contentList;

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    private String date;

    public Receipt(String companyName, double price, String date) {
        this.companyName = companyName;
        this.price = price;
        this.contentList  = new ArrayList<ReceiptContent>();
        for (int i = 0; i < 6; ++i) {
            ReceiptContent receiptContent = new ReceiptContent("Yummy icecream", "6.35 €");
            this.contentList.add(receiptContent);
        }
        System.out.println(this.contentList.size());
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

    public void setPrice(double price) {
        this.price = price;
    }
}
