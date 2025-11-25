package org.me.gcu.kooner_jagpal_s2221326;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.List;
import java.util.Locale;

/**
 * Custom adapter to display CurrencyRate objects with colour coding and flags.
 */
public class CurrencyRateAdapter extends ArrayAdapter<CurrencyRate> {

    public CurrencyRateAdapter(@NonNull Context context, @NonNull List<CurrencyRate> objects) {
        super(context, 0, objects);
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        View rowView = convertView;
        if (rowView == null) {
            rowView = LayoutInflater.from(getContext())
                    .inflate(R.layout.row_currency, parent, false);
        }

        CurrencyRate rate = getItem(position);
        if (rate == null) {
            return rowView;
        }

        TextView txtCode = rowView.findViewById(R.id.txtCode);
        TextView txtName = rowView.findViewById(R.id.txtName);
        TextView txtValue = rowView.findViewById(R.id.txtValue);
        ImageView imgFlag = rowView.findViewById(R.id.imgFlag);

        // --- Text values ---
        txtCode.setText(rate.getTargetCode());
        txtName.setText(rate.getCurrencyName());
        txtValue.setText(String.format(Locale.UK,
                "Rate: %.4f per 1 GBP", rate.getRate()));

        // --- Flag loading based on currency code ---
        String code = rate.getTargetCode();
        if (code != null && !code.isEmpty()) {
            // We expect drawables named like: flag_usd.png, flag_eur.png, flag_jpy.png, ...
            String resName = "flag_" + code.toLowerCase(Locale.ROOT);
            int resId = getContext().getResources().getIdentifier(
                    resName,
                    "drawable",
                    getContext().getPackageName()
            );

            if (resId != 0) {
                imgFlag.setImageResource(resId);
                imgFlag.setVisibility(View.VISIBLE);
            } else {
                // No flag found for this code: hide the ImageView
                imgFlag.setImageDrawable(null);
                imgFlag.setVisibility(View.INVISIBLE);
            }
        } else {
            imgFlag.setImageDrawable(null);
            imgFlag.setVisibility(View.INVISIBLE);
        }

        // --- Colour coding based on strength vs GBP (4 ranges) ---
        double value = rate.getRate();
        int bgColor;

        if (value < 1.0) {
            bgColor = Color.parseColor("#C8E6C9"); // light green
        } else if (value < 5.0) {
            bgColor = Color.parseColor("#FFF9C4"); // light yellow
        } else if (value < 10.0) {
            bgColor = Color.parseColor("#FFE0B2"); // light orange
        } else {
            bgColor = Color.parseColor("#FFCDD2"); // light red
        }

        rowView.setBackgroundColor(bgColor);

        return rowView;
    }
}