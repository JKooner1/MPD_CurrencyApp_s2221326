/*  Mobile Platform Development Coursework
    MainActivity:
    - Uses RssFeedFetcher to download & parse RSS off the UI thread
    - Shows:
        * Main currencies (GBP -> USD, EUR, JPY)
        * All currencies (colour coded)
    - Supports search (code / name / country text)
    - Clicking any item opens ConverterActivity
    - Automatically refreshes rates every 5 minutes
*/

package org.me.gcu.kooner_jagpal_s2221326;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity implements OnClickListener {

    private static final String TAG = "MPD";
    private static final long AUTO_REFRESH_MS = 5 * 60 * 1000; // 5 minutes

    private TextView salutation;
    private TextView acknowledgement;
    private TextView mainCurrenciesLabel;
    private TextView allCurrenciesLabel;
    private Button startButton;
    private ListView listMainCurrencies;
    private ListView listAllCurrencies;
    private EditText editSearch;
    private Button btnSearch;

    private String urlSource = "https://www.fx-exchange.com/gbp/rss.xml";

    // Parsed currency data
    private ArrayList<CurrencyRate> currencyRates = new ArrayList<>();
    private ArrayList<CurrencyRate> mainCurrencies = new ArrayList<>();
    private ArrayList<CurrencyRate> filteredCurrencies = new ArrayList<>();

    // Custom adapters
    private CurrencyRateAdapter mainAdapter;
    private CurrencyRateAdapter allAdapter;

    // Auto-refresh handler
    private Handler autoRefreshHandler = new Handler(Looper.getMainLooper());
    private Runnable autoRefreshRunnable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Link UI components
        salutation = findViewById(R.id.salutation);
        acknowledgement = findViewById(R.id.acknowledgement);
        mainCurrenciesLabel = findViewById(R.id.mainCurrenciesLabel);
        allCurrenciesLabel = findViewById(R.id.allCurrenciesLabel);
        startButton = findViewById(R.id.startButton);
        listMainCurrencies = findViewById(R.id.listMainCurrencies);
        listAllCurrencies = findViewById(R.id.listAllCurrencies);
        editSearch = findViewById(R.id.editSearch);
        btnSearch = findViewById(R.id.btnSearch);

        startButton.setOnClickListener(this);
        btnSearch.setOnClickListener(this);

        // Adapters use the backing ArrayLists directly
        mainAdapter = new CurrencyRateAdapter(this, mainCurrencies);
        allAdapter = new CurrencyRateAdapter(this, filteredCurrencies);

        listMainCurrencies.setAdapter(mainAdapter);
        listAllCurrencies.setAdapter(allAdapter);

        // Click on any item -> open converter
        listMainCurrencies.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (position >= 0 && position < mainCurrencies.size()) {
                    CurrencyRate selected = mainCurrencies.get(position);
                    openConverter(selected);
                }
            }
        });

        listAllCurrencies.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (position >= 0 && position < filteredCurrencies.size()) {
                    CurrencyRate selected = filteredCurrencies.get(position);
                    openConverter(selected);
                }
            }
        });

        // Initial load + start auto-refresh
        startProgress();
        scheduleAutoRefresh();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Stop auto-refresh when activity is destroyed
        if (autoRefreshRunnable != null) {
            autoRefreshHandler.removeCallbacks(autoRefreshRunnable);
        }
    }

    @Override
    public void onClick(View view) {
        int id = view.getId();
        if (id == R.id.startButton) {
            // Manual refresh
            startProgress();
        } else if (id == R.id.btnSearch) {
            performSearch();
        }
    }

    private void performSearch() {
        String query = editSearch.getText().toString().trim().toLowerCase();

        if (currencyRates.isEmpty()) {
            Toast.makeText(this, "No data yet. Please wait for rates to download.", Toast.LENGTH_SHORT).show();
            return;
        }

        filteredCurrencies.clear();

        // If query empty -> show all
        if (query.isEmpty()) {
            filteredCurrencies.addAll(currencyRates);
        } else {
            for (CurrencyRate rate : currencyRates) {
                String code = (rate.getTargetCode() != null) ? rate.getTargetCode().toLowerCase() : "";
                String name = (rate.getCurrencyName() != null) ? rate.getCurrencyName().toLowerCase() : "";

                if (code.contains(query) || name.contains(query)) {
                    filteredCurrencies.add(rate);
                }
            }

            if (filteredCurrencies.isEmpty()) {
                Toast.makeText(this, "No currencies match that search.", Toast.LENGTH_SHORT).show();
            }
        }

        allAdapter.notifyDataSetChanged();
    }

    public void startProgress() {
        Toast.makeText(this, "Updating currency rates...", Toast.LENGTH_SHORT).show();

        RssFeedFetcher.OnRatesParsedListener listener = new RssFeedFetcher.OnRatesParsedListener() {
            @Override
            public void onRatesParsed(final ArrayList<CurrencyRate> rates) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        currencyRates.clear();
                        currencyRates.addAll(rates);

                        mainCurrencies.clear();
                        mainCurrencies.addAll(extractMainCurrencies(currencyRates));

                        filteredCurrencies.clear();
                        filteredCurrencies.addAll(currencyRates);

                        mainAdapter.notifyDataSetChanged();
                        allAdapter.notifyDataSetChanged();

                        Toast.makeText(MainActivity.this,
                                "Rates updated (" + currencyRates.size() + " currencies).",
                                Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onError(final String message) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Log.e(TAG, "Error from RssFeedFetcher: " + message);
                        Toast.makeText(MainActivity.this, message, Toast.LENGTH_LONG).show();
                    }
                });
            }
        };

        // Run fetcher on a background thread
        new Thread(new RssFeedFetcher(urlSource, listener)).start();
    }

    private void scheduleAutoRefresh() {
        if (autoRefreshRunnable == null) {
            autoRefreshRunnable = new Runnable() {
                @Override
                public void run() {
                    startProgress();
                    autoRefreshHandler.postDelayed(this, AUTO_REFRESH_MS);
                }
            };
        }
        // First auto-refresh will happen after the delay
        autoRefreshHandler.postDelayed(autoRefreshRunnable, AUTO_REFRESH_MS);
    }

    // Open ConverterActivity for the selected CurrencyRate
    private void openConverter(CurrencyRate rate) {
        if (rate == null || rate.getTargetCode() == null) {
            Toast.makeText(this, "No currency details available.", Toast.LENGTH_SHORT).show();
            return;
        }

        Intent intent = new Intent(MainActivity.this, ConverterActivity.class);
        intent.putExtra("targetCode", rate.getTargetCode());
        intent.putExtra("currencyName", rate.getCurrencyName());
        intent.putExtra("rate", rate.getRate());
        startActivity(intent);
    }

    /**
     * Extract just the main currencies: USD, EUR, JPY.
     */
    private ArrayList<CurrencyRate> extractMainCurrencies(ArrayList<CurrencyRate> all) {
        ArrayList<CurrencyRate> mains = new ArrayList<>();
        String[] codes = {"USD", "EUR", "JPY"};

        for (CurrencyRate rate : all) {
            String code = rate.getTargetCode();
            if (code == null) {
                continue;
            }
            for (String mainCode : codes) {
                if (mainCode.equalsIgnoreCase(code)) {
                    mains.add(rate);
                    break;
                }
            }
        }
        return mains;
    }
}