package org.me.gcu.kooner_jagpal_s2221326;

import android.util.Log;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;

/**
 * Separate class responsible for downloading and parsing the RSS feed
 * off the UI thread. Results are returned via a callback.
 */
public class RssFeedFetcher implements Runnable {

    public interface OnRatesParsedListener {
        void onRatesParsed(ArrayList<CurrencyRate> rates);
        void onError(String message);
    }

    private static final String TAG = "RssFeedFetcher";

    private final String urlSource;
    private final OnRatesParsedListener listener;

    public RssFeedFetcher(String urlSource, OnRatesParsedListener listener) {
        this.urlSource = urlSource;
        this.listener = listener;
    }

    @Override
    public void run() {
        String result = "";
        URL aurl;
        URLConnection yc;
        BufferedReader in = null;
        String inputLine;

        // --- Network download ---
        try {
            aurl = new URL(urlSource);
            yc = aurl.openConnection();
            in = new BufferedReader(new InputStreamReader(yc.getInputStream()));
            while ((inputLine = in.readLine()) != null) {
                result = result + inputLine;
            }
            in.close();
        } catch (IOException ae) {
            Log.e(TAG, "Network IO exception", ae);
            if (listener != null) {
                listener.onError("Network error while downloading rates.");
            }
            return;
        }

        // Clean up any leading garbage characters
        int i = result.indexOf("<?"); // initial tag
        if (i >= 0) {
            result = result.substring(i);
        }

        // Clean up any trailing garbage at the end of the file
        i = result.indexOf("</rss>"); // final tag
        if (i >= 0) {
            result = result.substring(0, i + 6);
        }

        ArrayList<CurrencyRate> parsedList = new ArrayList<>();

        // --- XML parsing with PullParser ---
        try {
            XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
            factory.setNamespaceAware(true);
            XmlPullParser xpp = factory.newPullParser();
            xpp.setInput(new StringReader(result));

            int eventType = xpp.getEventType();
            CurrencyRate currentRate = null;
            String text = "";

            while (eventType != XmlPullParser.END_DOCUMENT) {
                String tagName = xpp.getName();

                switch (eventType) {
                    case XmlPullParser.START_TAG:
                        if ("item".equalsIgnoreCase(tagName)) {
                            currentRate = new CurrencyRate();
                        }
                        break;

                    case XmlPullParser.TEXT:
                        text = xpp.getText();
                        break;

                    case XmlPullParser.END_TAG:
                        if (currentRate != null) {
                            if ("item".equalsIgnoreCase(tagName)) {
                                parsedList.add(currentRate);
                                Log.d(TAG, "Parsed: " + currentRate.toString());
                                currentRate = null;
                            } else if ("title".equalsIgnoreCase(tagName)) {
                                currentRate.setRawTitle(text);
                                parseTitleIntoCurrency(currentRate, text);
                            } else if ("description".equalsIgnoreCase(tagName)) {
                                currentRate.setRawDescription(text);
                                parseDescriptionIntoCurrency(currentRate, text);
                            } else if ("pubDate".equalsIgnoreCase(tagName)) {
                                currentRate.setPubDate(text);
                            }
                        }
                        break;
                }

                eventType = xpp.next();
            }

        } catch (XmlPullParserException e) {
            Log.e(TAG, "Parsing EXCEPTION", e);
            if (listener != null) {
                listener.onError("Problem parsing the RSS feed.");
            }
            return;
        } catch (IOException e) {
            Log.e(TAG, "Parsing I/O EXCEPTION", e);
            if (listener != null) {
                listener.onError("I/O error while parsing the RSS feed.");
            }
            return;
        }

        if (listener != null) {
            listener.onRatesParsed(parsedList);
        }
    }

    // -------- Helper parsing methods (same logic as before) --------

    private void parseTitleIntoCurrency(CurrencyRate rate, String titleText) {
        if (titleText == null) {
            return;
        }

        int slashIndex = titleText.indexOf('/');
        if (slashIndex == -1 || slashIndex == titleText.length() - 1) {
            return;
        }

        String targetPart = titleText.substring(slashIndex + 1).trim();
        int openBracket = targetPart.lastIndexOf('(');
        int closeBracket = targetPart.lastIndexOf(')');

        if (openBracket != -1 && closeBracket != -1 && closeBracket > openBracket) {
            String code = targetPart.substring(openBracket + 1, closeBracket).trim();
            String name = targetPart.substring(0, openBracket).trim();

            rate.setTargetCode(code);
            rate.setCurrencyName(name);
        } else {
            rate.setCurrencyName(targetPart);
        }
    }

    private void parseDescriptionIntoCurrency(CurrencyRate rate, String descriptionText) {
        if (descriptionText == null) {
            return;
        }

        String[] parts = descriptionText.split(" ");
        for (int i = 0; i < parts.length; i++) {
            if ("=".equals(parts[i]) && i + 1 < parts.length) {
                String numberPart = parts[i + 1];
                try {
                    double value = Double.parseDouble(numberPart);
                    rate.setRate(value);
                } catch (NumberFormatException nfe) {
                    Log.e(TAG, "Could not parse rate from description: " + descriptionText, nfe);
                }
                break;
            }
        }
    }
}
