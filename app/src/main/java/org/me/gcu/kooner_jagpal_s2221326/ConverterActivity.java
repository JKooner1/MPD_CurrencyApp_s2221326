package org.me.gcu.kooner_jagpal_s2221326;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.Locale;

/**
 * Activity to convert between GBP and a selected currency.
 */
public class ConverterActivity extends AppCompatActivity {

    private TextView txtHeader;
    private TextView txtRateInfo;
    private EditText editAmount;
    private RadioButton rbGbpToForeign;
    private RadioButton rbForeignToGbp;
    private Button btnConvert;
    private TextView txtResult;
    private Button btnBack;

    private String targetCode;
    private String currencyName;
    private double rate; // how many target units for 1 GBP

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_converter);

        txtHeader = (TextView) findViewById(R.id.txtHeader);
        txtRateInfo = (TextView) findViewById(R.id.txtRateInfo);
        editAmount = (EditText) findViewById(R.id.editAmount);
        rbGbpToForeign = (RadioButton) findViewById(R.id.rbGbpToForeign);
        rbForeignToGbp = (RadioButton) findViewById(R.id.rbForeignToGbp);
        btnConvert = (Button) findViewById(R.id.btnConvert);
        txtResult = (TextView) findViewById(R.id.txtResult);
        btnBack = (Button) findViewById(R.id.btnBack);

        // Get data from Intent
        targetCode = getIntent().getStringExtra("targetCode");
        currencyName = getIntent().getStringExtra("currencyName");
        rate = getIntent().getDoubleExtra("rate", 0.0);

        if (targetCode == null) {
            targetCode = "";
        }
        if (currencyName == null) {
            currencyName = "";
        }

        // Header text
        String header = "GBP ⇄ " + targetCode + " (" + currencyName + ")";
        txtHeader.setText(header);

        String rateInfo = String.format(Locale.UK,
                "1 GBP = %.4f %s", rate, targetCode);
        txtRateInfo.setText(rateInfo);

        // Default to GBP → foreign
        rbGbpToForeign.setChecked(true);

        btnConvert.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                doConversion();
            }
        });

        // Back button finishes this activity and returns to MainActivity
        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();   // closes ConverterActivity and goes back
            }
        });
    }

    private void doConversion() {
        String amtStr = editAmount.getText().toString().trim();

        if (TextUtils.isEmpty(amtStr)) {
            editAmount.setError("Enter an amount");
            return;
        }

        double amount;
        try {
            amount = Double.parseDouble(amtStr);
        } catch (NumberFormatException e) {
            editAmount.setError("Enter a valid number");
            return;
        }

        if (rate <= 0.0) {
            Toast.makeText(this, "Rate not available.", Toast.LENGTH_SHORT).show();
            return;
        }

        double resultValue;
        String directionText;

        if (rbGbpToForeign.isChecked()) {
            // GBP -> foreign
            resultValue = amount * rate;
            directionText = String.format(Locale.UK,
                    "GBP %.2f = %s %.2f",
                    amount, targetCode, resultValue);
        } else {
            // foreign -> GBP
            resultValue = amount / rate;
            directionText = String.format(Locale.UK,
                    "%s %.2f = GBP %.2f",
                    targetCode, amount, resultValue);
        }

        txtResult.setText(directionText);
    }
}