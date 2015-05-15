package com.example.apmart_inbilling_v2_sample;

import java.util.ArrayList;
import java.util.List;

import jp.app_mart.billing.v2.AppmartIabHelper;
import jp.app_mart.billing.v2.IabResult;
import jp.app_mart.billing.v2.Inventory;
import jp.app_mart.billing.v2.Purchase;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

public class MainActivity extends ActionBarActivity {

	
	/* ######  編集部分 ##### */
	
	// ライセンスキー
	public static final String APPMART_LICENSE_KEY = "your_license_id";
	// アプリID
	public static final String APPMART_APP_ID = "your_app_id";
	// サービスID(複数)
	public static final String APPMART_SERVICE_ID1 = "your_service_id_1";
	public static final String APPMART_SERVICE_ID2= "your_service_id_2";
	
	/* ###  編集部分エンド　### */
	
	// TAG
	public final String TAG = this.getClass().getSimpleName();
	// debugモード
	private boolean isDebug = true;	
	// Inventory
	public Inventory mInventory = new Inventory();
	// Appmartヘルパー
	public AppmartIabHelper mHelper;
	//　ListView (サービス情報表示)
	public ListView listView;
	// ListviewのAdapter
	public InventoryAdapter mAdapter;


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		// ListViewインスタンス化+adapterセット
		mAdapter = new InventoryAdapter(MainActivity.this, mInventory, mHelper, mPurchaseFinishedListener, onConsumeFinishedListener);
		listView = (ListView) findViewById(R.id.listview01);
		listView.setAdapter(mAdapter);
		
		// ListView初期設定
		mHelper= new AppmartIabHelper(this, APPMART_APP_ID, APPMART_LICENSE_KEY);
		mHelper.startSetup(new AppmartIabHelper.OnIabSetupFinishedListener() {
			   public void onIabSetupFinished(IabResult result) {
			      if (!result.isSuccess()){
			    	  if(result.getResponse() == AppmartIabHelper.BILLING_RESPONSE_RESULT_BILLING_UNAVAILABLE)
			    		  debugMess("appmartを更新してください。");			    	  
			    	  Log.d(TAG, "appmartアプリ内課金：問題が発生しました。" + result);
			      }else{
			    	  Log.d(TAG, "appmartアプリ内課金：設定完了");
			      }
			   }
		});
				
		// Inventory更新ボタン
		((Button)findViewById(R.id.getskus)).setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View v) {
				updateListAdapter();				
			}			
		});		
	}
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
		//Helper停止
		if (mHelper != null) mHelper.dispose();
		   mHelper = null;
	}
	
	/** 必ず[handleActivityResult]を実装してください。
	 * セキュリティー向上のため、handleActivityResult(requestCode,resultCode,data,publicKey)
	 * を使うこともできます。publicKeyをStringとして保存しないでください。
	 */
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (!mHelper.handleActivityResult(requestCode, resultCode, data)) {
            super.onActivityResult(requestCode, resultCode, data);
        }else {
            Log.d(TAG, "onActivityResult handled by AppmartIabHelper.");
	    }	
	}
	
	// Inventoryを更新する処理
	private void updateListAdapter(){
		// 取得したいサービスのIDをリスト化しqueryInventoryAsyncに渡す
		List<String> additionalSkuList = new ArrayList<String>();
		additionalSkuList.add(APPMART_SERVICE_ID1);
		additionalSkuList.add(APPMART_SERVICE_ID2);
		additionalSkuList.add("kanri-suru");
		mHelper.queryInventoryAsync(additionalSkuList, mQueryFinishedListener);
	}
	
	/* ########################
	 * Callback 定義
	 * ######################## */
	
	// Inventory取得後 callback
	AppmartIabHelper.QueryInventoryFinishedListener 
	   mQueryFinishedListener = new AppmartIabHelper.QueryInventoryFinishedListener() {
		@Override
		public void onQueryInventoryFinished(IabResult result, Inventory inventory){
	      if (result.isFailure()) {
	    	  if(result.getResponse() == AppmartIabHelper.BILLING_RESPONSE_USER_NOT_LOG){
	    		  debugMess("appmartでログインしてください。");
	    	  }else{
	    		  debugMess(result.getMessage());
	    	  }
	         return;
	       }	      
	      // Adapterを更新 [TODO use notifyDataSetChanged	]  	
	      mAdapter = new InventoryAdapter(MainActivity.this, inventory, mHelper, mPurchaseFinishedListener, onConsumeFinishedListener);
	      listView.setAdapter(mAdapter);	       
	   }
	};
	
	// 決済完了後 callback
	AppmartIabHelper.OnIabPurchaseFinishedListener mPurchaseFinishedListener = new AppmartIabHelper.OnIabPurchaseFinishedListener() {
		@Override
		public void onIabPurchaseFinished(IabResult result, Purchase purchase){
		   if (result.isFailure()) {
	         Log.d(TAG, "購入エラー: " + result);
	         return;
	      }	      
	      debugMess("サービスが購入されました (" + purchase.getSku() + ")");
	      // ListView更新
	      updateListAdapter();
	   }
	};	
	
	//　サービス消費後 callback
	AppmartIabHelper.OnConsumeFinishedListener onConsumeFinishedListener = new AppmartIabHelper.OnConsumeFinishedListener(){
		@Override
		public void onConsumeFinished(Purchase purchase,IabResult result) {		
			if(result.isFailure()){
				debugMess("アイテムが消費されませんでした。");
			}else{
				debugMess("アイテムが消費されました。");
				//ListView更新
				updateListAdapter();
			}
		}
	};
	
	/* ########################
	 * その他 メッソド
	 * ######################## */
	
	// debug用
	private void debugMess(String mess) {
		if (isDebug) {
			Log.d("DEBUG", mess);
			Toast.makeText(getApplicationContext(), mess, Toast.LENGTH_SHORT).show();
		}
	}
	
}
