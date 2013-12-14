package com.gnb.gnb_info;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Stack;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import android.os.AsyncTask;
import android.os.Bundle;
import android.app.ListActivity;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class Main extends ListActivity {

	private ArrayList<Product> products;
	public static ProductSum productSum;
	public ArrayList<Conversion> conversions;
	private gnbListAdapter adapter = null;
	private Stack<String> stack = new Stack<String>();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		products = new ArrayList<Product>();
		conversions = new ArrayList<Conversion>();
		LoadProducts();
		LoadConversions();
		adapter = new gnbListAdapter(Main.this, products);
		setListAdapter(adapter);
		Toast.makeText(Main.this, "Loading Products, Please wait...",
				Toast.LENGTH_SHORT).show();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	private void GetSumOfProduct(String sku, ProductSum ps) {
		for (Product p : products) {
			if (sku.compareTo(p.sku) == 0) {
				ps.sum +=  convToEUR(p.currency, p.amount);
				ps.product.add(p);
			}
		}
	}

	private Double convToEUR(String currency, Double amount) {
		if (currency.compareTo("EUR") == 0) return amount;
		stack.clear();
		stack.push(currency);
		
		ArrayList<Conversion> RatesToConversion = new ArrayList<Conversion>();
		GetRatesToConversion(listOfCurrFROM(currency), RatesToConversion);
		return toConv(amount, RatesToConversion);
	}

	private Double toConv(Double amount, ArrayList<Conversion> ratiosToAppli)
	{
		Double sum = amount;
		for (Conversion cc : ratiosToAppli)		
			sum *= cc.rate;
		
		return sum;
	}
	
	private void GetRatesToConversion(
			ArrayList<Conversion> occurences_list,
			ArrayList<Conversion> ratiosToAppli) {

		if (existCurrencyInTO("EUR", occurences_list)) {
			ratiosToAppli.add(searchByTO("EUR", occurences_list));
			return;
		} else {
			for (Conversion cc : occurences_list) {
				ratiosToAppli.add(cc);
				if (!stack.contains(cc.to)) {
					stack.push(cc.to);
					ArrayList<Conversion> listTemp = listOfCurrFROM(cc.to);
					if (existCurrencyInTO("EUR", listTemp)) {
						ratiosToAppli.add(searchByTO("EUR", listTemp));
						return;
					} else
						GetRatesToConversion(listTemp, ratiosToAppli);
				}
			}
		}

		return;
	}

	private boolean existCurrencyInTO(String currency,
			ArrayList<Conversion> l_conversions) {
		for (Conversion cc : l_conversions)
			if (cc.to.compareTo(currency) == 0)
				return true;

		return false;
	}

	private ArrayList<Conversion> listOfCurrFROM(String from) {
		ArrayList<Conversion> ret = new ArrayList<Conversion>();
		for (Conversion cc : conversions)
			if (cc.from.compareTo(from) == 0)
				ret.add(cc);

		return ret;
	}

	private Conversion searchByTO(String to, ArrayList<Conversion> l_convs) {
		for (Conversion cc : l_convs)
			if (cc.to.compareTo(to) == 0)
				return cc;

		return null;
	}

	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		Intent intent = new Intent(Main.this, ListSum.class);
		productSum = new ProductSum();
		GetSumOfProduct(products.get(position).sku, productSum);
		startActivity(intent);
	}

	private void LoadConversions() {
		AsyncTask<Void, Void, Void> task = new AsyncTask<Void, Void, Void>() {

			HttpClient client;
			HttpGet httpGet;
			StringBuilder builder;

			@Override
			protected void onPreExecute() {
				builder = new StringBuilder();
				client = new DefaultHttpClient();
				httpGet = new HttpGet(
						"http://quiet-stone-2094.herokuapp.com/rates");
				httpGet.setHeader("Content-Type", "application/json");
				httpGet.setHeader("Accept", "application/json");
			}

			@Override
			protected Void doInBackground(Void... arg0) {
				try {
					HttpResponse response = client.execute(httpGet);
					StatusLine statusLine = response.getStatusLine();
					int statusCode = statusLine.getStatusCode();
					if (statusCode == 200) {
						HttpEntity entity = response.getEntity();
						InputStream content = entity.getContent();
						BufferedReader reader = new BufferedReader(
								new InputStreamReader(content));
						String line;
						while ((line = reader.readLine()) != null) {
							builder.append(line);
						}
					} else {
						Log.d("ERROR", "Failed to download file");
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
				return null;
			}

			@Override
			protected void onPostExecute(Void result) {
				try {
					//builder.delete(0, builder.length());
					//builder.append("[{\"to\":\"USD\",\"rate\":\"1.06\",\"from\":\"CAD\"},{\"to\":\"CAD\",\"rate\":\"0.94\",\"from\":\"USD\"},{\"to\":\"EUR\",\"rate\":\"1.46\",\"from\":\"CAD\"},{\"to\":\"CAD\",\"rate\":\"0.68\",\"from\":\"EUR\"},{\"to\":\"AUD\",\"rate\":\"1.04\",\"from\":\"USD\"},{\"to\":\"USD\",\"rate\":\"0.96\",\"from\":\"AUD\"}]");
					JSONArray jsonArray = new JSONArray(builder.toString());
					for (int i = 0; i < jsonArray.length(); i++) {
						JSONObject jsonObject = jsonArray.getJSONObject(i);
						conversions.add(new Conversion(jsonObject
								.getString("from"), jsonObject.getString("to"),
								jsonObject.getDouble("rate")));

					}
				} catch (JSONException e) {
					e.printStackTrace();
				}
			}
		};
		task.execute((Void[]) null);
	}

	private void LoadProducts() {
		AsyncTask<Void, Void, Void> task = new AsyncTask<Void, Void, Void>() {

			HttpClient client;
			HttpGet httpGet;
			StringBuilder builder;

			@Override
			protected void onPreExecute() {
				builder = new StringBuilder();
				client = new DefaultHttpClient();				
				httpGet = new HttpGet(
						"http://quiet-stone-2094.herokuapp.com/transactions");
				httpGet.setHeader("Content-Type", "application/json");
				httpGet.setHeader("Accept", "application/json");
			}

			@Override
			protected Void doInBackground(Void... arg0) {
				try {
					HttpResponse response = client.execute(httpGet);
					StatusLine statusLine = response.getStatusLine();
					int statusCode = statusLine.getStatusCode();
					if (statusCode == 200) {
						HttpEntity entity = response.getEntity();
						InputStream content = entity.getContent();
						BufferedReader reader = new BufferedReader(
								new InputStreamReader(content));
						String line;
						while ((line = reader.readLine()) != null) {
							builder.append(line);
						}
					} else {
						Log.d("ERROR", "Failed to download file");
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
				return null;
			}

			@Override
			protected void onPostExecute(Void result) {
				try {
					JSONArray jsonArray = new JSONArray(builder.toString());
					for (int i = 0; i < jsonArray.length(); i++) {
						JSONObject jsonObject = jsonArray.getJSONObject(i);
						products.add(new Product(jsonObject.getString("sku"),
								jsonObject.getDouble("amount"), jsonObject
										.getString("currency")));
					}
					adapter.addAll(products);
					adapter.setNotifyOnChange(true);
				} catch (JSONException e) {
					e.printStackTrace();
				}
			}
		};
		task.execute((Void[]) null);
	}

	private static class ViewHolder {
		public TextView tv_sku;
		public TextView tv_amount;
		public TextView tv_currency;
	}

	public class gnbListAdapter extends ArrayAdapter<Product> {
		private final Context context;
		ArrayList<Product> Rows;
		ViewHolder holder;
		DecimalFormat twoDForm = new DecimalFormat("#.00");

		public gnbListAdapter(Context context, ArrayList<Product> Rows_) {
			super(context, R.layout.layo_main_row, Rows_);
			this.context = context;
			Rows = Rows_;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			View rowView = convertView;
			if (rowView == null) {
				LayoutInflater inflater = (LayoutInflater) context
						.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
				rowView = inflater.inflate(R.layout.layo_main_row, parent,
						false);
				holder = new ViewHolder();
				holder.tv_sku = (TextView) rowView
						.findViewById(R.id.layo_main_row_sku);
				holder.tv_amount = (TextView) rowView
						.findViewById(R.id.layo_main_row_amount);
				holder.tv_currency = (TextView) rowView
						.findViewById(R.id.layo_main_row_currency);

				rowView.setTag(holder);
			}

			holder = (ViewHolder) rowView.getTag();

			holder.tv_sku.setText(String.valueOf(Rows.get(position).sku));
			holder.tv_amount
					.setText(twoDForm.format(Rows.get(position).amount));
			holder.tv_currency.setText(Rows.get(position).currency);

			return rowView;
		}
	}

}
