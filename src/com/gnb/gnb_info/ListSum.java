package com.gnb.gnb_info;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Locale;

import com.gnb.gnb_info.Main.gnbListAdapter;
import android.os.Bundle;
import android.app.ListActivity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

public class ListSum extends ListActivity {

	private ArrayList<Product> products;
	private gnbListSumAdapter adapter = null;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		products = new ArrayList<Product>();
		products.add(new Product("TOTAL", Main.productSum.getSumRoudedAsBankers(), "EUR"));
		for(Product pd : Main.productSum.product)		
			products.add(pd);		
		adapter = new gnbListSumAdapter(ListSum.this, products);
		setListAdapter(adapter);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.list_sum, menu);		
		return true;
	}

	private static class ViewHolder {
		public TextView tv_sku;
		public TextView tv_amount;
		public TextView tv_currency;
	}

	public class gnbListSumAdapter extends ArrayAdapter<Product> {
		private final Context context;
		ArrayList<Product> Rows;
		ViewHolder holder;
		DecimalFormat twoDForm = new DecimalFormat("#.00");
		NumberFormat form = NumberFormat.getCurrencyInstance(Locale.getDefault());

		public gnbListSumAdapter(Context context, ArrayList<Product> Rows_) {
			super(context, R.layout.layo_list_sum_row, Rows_);
			this.context = context;
			Rows = Rows_;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			View rowView = convertView;
			if (rowView == null) {
				LayoutInflater inflater = (LayoutInflater) context
						.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
				rowView = inflater.inflate(R.layout.layo_list_sum_row, parent,
						false);
				holder = new ViewHolder();
				holder.tv_sku = (TextView) rowView
						.findViewById(R.id.layo_list_sum_row_sku);
				holder.tv_amount = (TextView) rowView
						.findViewById(R.id.layo_list_sum_row_amount);
				holder.tv_currency = (TextView) rowView
						.findViewById(R.id.layo_list_sum_row_currency);

				rowView.setTag(holder);
			}

			holder = (ViewHolder) rowView.getTag();
			
			holder.tv_sku.setText(String.valueOf(Rows.get(position).sku));
			if (Rows.get(position).sku.compareTo("TOTAL") == 0)
				holder.tv_amount.setText(twoDForm.format(Rows.get(position).total));
			else
				holder.tv_amount.setText(twoDForm.format(Rows.get(position).amount));
			holder.tv_currency.setText(Rows.get(position).currency);

			return rowView;
		}
	}
	
}
