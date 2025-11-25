package org.me.gcu.kooner_jagpal_s2221326;

/**
 * Simple model class representing one currency rate from the GBP RSS feed.
 */
public class CurrencyRate {

    // Base currency is always GBP in this feed
    private static final String BASE_CODE = "GBP";

    private String targetCode;       // e.g. "AED"
    private String currencyName;     // e.g. "United Arab Emirates Dirham"
    private double rate;             // e.g. 4.8074
    private String pubDate;          // e.g. "Wed Aug 27 2025 2:00:45 UTC"
    private String rawTitle;         // full <title> text
    private String rawDescription;   // full <description> text

    public CurrencyRate() {
    }

    public String getBaseCode() {
        return BASE_CODE;
    }

    public String getTargetCode() {
        return targetCode;
    }

    public void setTargetCode(String targetCode) {
        this.targetCode = targetCode;
    }

    public String getCurrencyName() {
        return currencyName;
    }

    public void setCurrencyName(String currencyName) {
        this.currencyName = currencyName;
    }

    public double getRate() {
        return rate;
    }

    public void setRate(double rate) {
        this.rate = rate;
    }

    public String getPubDate() {
        return pubDate;
    }

    public void setPubDate(String pubDate) {
        this.pubDate = pubDate;
    }

    public String getRawTitle() {
        return rawTitle;
    }

    public void setRawTitle(String rawTitle) {
        this.rawTitle = rawTitle;
    }

    public String getRawDescription() {
        return rawDescription;
    }

    public void setRawDescription(String rawDescription) {
        this.rawDescription = rawDescription;
    }

    @Override
    public String toString() {
        // Example: "AED : 4.8074 (United Arab Emirates Dirham)"
        return targetCode + " : " + String.format("%.4f", rate) +
                " (" + currencyName + ")";
    }
}