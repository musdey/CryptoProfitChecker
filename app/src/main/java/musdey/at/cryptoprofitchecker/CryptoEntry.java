package musdey.at.cryptoprofitchecker;

import java.text.DecimalFormat;

/**
 * Created by Musdey on 23.09.2017.
 */

public class CryptoEntry {

    long entryId;

    double actualCourse;
    double profit;
    double boughtAmount;
    double priceWhenBought;

    public double getBoughtAmount() {
        return boughtAmount;
    }

    public void calculateProfit(){
        double totaloldprice = boughtAmount * priceWhenBought;
        double totalnewprice = boughtAmount * actualCourse;
        profit = totalnewprice - totaloldprice;
    }

    public void setBoughtAmount(double boughtAmount) {
        this.boughtAmount = boughtAmount;
    }

    public double getPriceWhenBought() {
        return priceWhenBought;
    }

    public void setPriceWhenBought(double priceWhenBought) {
        this.priceWhenBought = priceWhenBought;
    }

    public long getEntryId() {
        return entryId;
    }

    public void setEntryId(long entryId) {
        this.entryId = entryId;
    }

    public double getActualCourse() {
        return actualCourse;
    }

    public void setActualCourse(double actualCourse) {
        this.actualCourse = actualCourse;
    }

    public double getProfit() {
        return profit;
    }

    public void setProfit(double profit) {
        this.profit = profit;
    }

}
