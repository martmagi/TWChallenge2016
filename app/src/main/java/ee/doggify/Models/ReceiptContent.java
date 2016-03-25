package ee.doggify.Models;

/**
 * Created by AnnaMC on 20.02.2016.
 */
public class ReceiptContent {

    private String name;

    public ReceiptContent(String name, String price) {
        this.name = name;
        this.price = price;
    }

    private String price;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPrice() {
        return price;
    }

    public void setPrice(String price) {
        this.price = price;
    }
}
