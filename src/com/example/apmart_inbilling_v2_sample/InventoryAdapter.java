package com.example.apmart_inbilling_v2_sample;

import jp.app_mart.billing.v2.AppmartIabHelper;
import jp.app_mart.billing.v2.AppmartIabHelper.OnConsumeFinishedListener;
import jp.app_mart.billing.v2.AppmartIabHelper.OnIabPurchaseFinishedListener;
import jp.app_mart.billing.v2.Inventory;
import jp.app_mart.billing.v2.Purchase;
import jp.app_mart.billing.v2.SkuDetails;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.TextView;

public class InventoryAdapter extends BaseAdapter {

	private Activity mActivity;
	private Context mContext;
	private Inventory mInventory;
	private AppmartIabHelper mHelper;
	private LayoutInflater mLayoutInflater = null;  
	
	// callback
	OnIabPurchaseFinishedListener mPurchaseFinishedListener; 
	OnConsumeFinishedListener mConsumeFinishedListener;

	public final String TAG = this.getClass().getSimpleName();

	public InventoryAdapter(Activity activity, Inventory inventory,
			AppmartIabHelper helper, OnIabPurchaseFinishedListener pfl, OnConsumeFinishedListener cfl) {
		this.mActivity = activity;
		this.mContext = activity.getApplicationContext();
		this.mInventory = inventory;
		this.mHelper = helper;	
		this.mLayoutInflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);  
		this.mPurchaseFinishedListener = pfl ;
		this.mConsumeFinishedListener = cfl;
	}

	@Override
	public int getCount() {
		return mInventory.getAllSkuDetails().size();
	}

	@Override
	public Object getItem(int position) {
		return mInventory.getAllSkuDetails().get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@SuppressLint("ViewHolder")
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		
		View v = convertView;  
		CompleteListViewHolder viewHolder;  
		
		if (convertView == null) {  
			v = mLayoutInflater.inflate(R.layout.inventory_object, null);
			viewHolder = new CompleteListViewHolder(v);
			v.setTag(viewHolder);
		}else{
			viewHolder = (CompleteListViewHolder) v.getTag();
		}
		
		// 情報設定
		final SkuDetails entry = mInventory.getAllSkuDetails().get(position);

		// サービス名
		viewHolder.itemName.setText((entry.getSku().length()>15) ? entry.getSku().substring(0, 14): entry.getSku());
		
		// 消費ボタン
		viewHolder.consume.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if(!mHelper.isAsyncInProgress()){
					if(mInventory.hasPurchase(entry.getSku())){
						Purchase p = mInventory.getPurchase(entry.getSku());
						mHelper.consumeAsync(p, mConsumeFinishedListener);
					}else{
						Log.d(mActivity.getClass().getSimpleName(), "未消費の情報がございません。");
					}
				}
			}
		});
			
		// 購入ボタン
		viewHolder.purchase.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if(!mHelper.isAsyncInProgress()){
					if(!mInventory.hasPurchase(entry.getSku())){
						mHelper.launchPurchaseFlow(mActivity,
							entry.getSku(), 10001,mPurchaseFinishedListener,"payload test string");
					}else{
						Log.d(mActivity.getClass().getSimpleName(), "このサービスは既に購入済みになっております。消費してから、もう一度購入してください。");
					}
				}
			}
		});

		return v;
	}
	
	class CompleteListViewHolder {
	      public TextView itemName;
	      public Button consume;
	      public Button purchase;	
	      public CompleteListViewHolder(View base) {  
	           	itemName = (TextView) base.findViewById(R.id.item_name);
	   			consume = (Button) base.findViewById(R.id.consume);
	   			purchase = (Button) base.findViewById(R.id.purchase);
	      }  
	 }  

}
