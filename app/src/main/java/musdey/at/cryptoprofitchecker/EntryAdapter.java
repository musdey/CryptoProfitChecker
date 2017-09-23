package musdey.at.cryptoprofitchecker;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.text.DecimalFormat;
import java.util.ArrayList;

/**
 * Created by Musdey on 23.09.2017.
 */

public class EntryAdapter extends ArrayAdapter<CryptoEntry> {
    public EntryAdapter(Context context, ArrayList<CryptoEntry> entries) {
        super(context, 0, entries);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // Get the data item for this position
        CryptoEntry entry = getItem(position);
        // Check if an existing view is being reused, otherwise inflate the view
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.list_item, parent, false);
        }
        // Lookup view for data population
        TextView tvActual = (TextView) convertView.findViewById(R.id.investedView);
        TextView tvProfit = (TextView) convertView.findViewById(R.id.profitView);
        TextView tvAmountInvested = (TextView) convertView.findViewById(R.id.actualCourseView);


        DecimalFormat numberFormat = new DecimalFormat("#.00");

        tvActual.setText(entry.getBoughtAmount()+" ETH bought at: ");
        tvProfit.setText("€ " + numberFormat.format(entry.getProfit()));
        tvAmountInvested.setText("€ " +entry.getPriceWhenBought());
        // Return the completed view to render on screen
        return convertView;
    }
}
