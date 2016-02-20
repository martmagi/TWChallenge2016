package pakett.tempname;

/**
 * Created by Mart on 20/02/2016.
 */
public class Receipt {
    private boolean useful;
    private String companyName;
    private double price;

    public Receipt(String companyName, double price) {
        this.companyName = companyName;
        this.price = price;
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

    public Receipt stringToReceipt(String string) {
        String[] lines = string.split("\\n");

        String paidSumLine = lines[4];
        String[] paidSumLines = paidSumLine.split(" ");
        double paidSum = Integer.parseInt(paidSumLines[1]);

        String paidToLine = lines[5];
        String paidToSplitAtExpression = paidToLine.split(">")[0];
        String[] paidToFullExpression = paidToSplitAtExpression.split(" ");
        int paidToWordsCount = paidToFullExpression.length;

        String companyName = "";

        for (int i = 2; i < paidToWordsCount; i++) {
            companyName += paidToFullExpression[i];
        }

        Receipt receipt = new Receipt(companyName, paidSum);
        return receipt;
    }
}
