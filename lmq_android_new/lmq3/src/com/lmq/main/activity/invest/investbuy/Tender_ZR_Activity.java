package com.lmq.main.activity.invest.investbuy;

import java.util.ArrayList;

import org.apache.http.Header;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;
import android.widget.TextView;

import com.lmq.R;
import com.lmq.http.BaseHttpClient;
import com.lmq.http.JsonHttpResponseHandler;
import com.lmq.main.api.BaseActivity;
import com.lmq.main.api.JsonBuilder;
import com.lmq.main.api.MyLog;
import com.lmq.main.api.SystenmApi;
import com.lmq.main.item.TB_YHQ_Item;
import com.lmq.main.util.Default;
import com.lmq.ybpay.YBPayActivity;

public class Tender_ZR_Activity extends BaseActivity implements
		android.view.View.OnClickListener {

	/**支付密码*/
	private EditText mEditPassword;
	/**投资金额*/
	private EditText mEditNum;
	
	/**账户余额*/
	private TextView tv_account_money;
	/**预计收益*/
	private TextView tv_yjsy;
	/**起投金额*/
	private TextView tv_borrow_min;
	/**限投金额*/
	private TextView tv_borrow_max;
	/**无限制"元"*/
	private TextView xz_yuan;
	/**可减金额*/
	private TextView tv_money;

	private String id;
	private int type;
	private ArrayList<TB_YHQ_Item> data = new ArrayList<TB_YHQ_Item>();

	
	/**预计收益*/
	private String amount;
	/**详情页债券id，作为最终投标使用**/
	private String invest_id;
	/**起投金额**/
	private String qitou;


	protected void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		setContentView(R.layout.tender_zr_layout);

		if(Default.IS_YB){
			findViewById(R.id.zfmm).setVisibility(View.GONE);
		}

		Intent intent = getIntent();
		if (intent.hasExtra("id")) {
			id = intent.getStringExtra("id");
		}
		if (intent.hasExtra("invest_id")) {
			invest_id = intent.getStringExtra("invest_id");
		}
		if (intent.hasExtra("qitou")) {
			qitou = intent.getStringExtra("qitou");
		}

		initView();
		mEditNum.addTextChangedListener(textWatcher);
	}
	
	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		doHttpBuy();
		tv_borrow_min.setText(qitou);
	}

	public void initView() {
		TextView title = (TextView) findViewById(R.id.title);
		title.setText("立即投标");
		
		findViewById(R.id.back).setOnClickListener(this);
		findViewById(R.id.enter_money).setOnClickListener(this);

		tv_account_money = (TextView) findViewById(R.id.tv_account_money);
		tv_yjsy = (TextView) findViewById(R.id.tv_yjsy);
		tv_borrow_min = (TextView) findViewById(R.id.tv_borrow_min);
		tv_borrow_max = (TextView) findViewById(R.id.tv_borrow_max);
		tv_money = (TextView) findViewById(R.id.tv_money);
		xz_yuan = (TextView) findViewById(R.id.xz_yuan);

		mEditNum = (EditText) findViewById(R.id.ed_money);
		mEditPassword = (EditText) findViewById(R.id.ed_pin);
	}

	private TextWatcher textWatcher = new TextWatcher() {
		@Override
		public void onTextChanged(CharSequence s, int start, int before,
								  int count) {

		}

		@Override
		public void beforeTextChanged(CharSequence s, int start, int count,
									  int after) {

		}

		@Override
		public void afterTextChanged(Editable s) {
			String temp =mEditNum.getText().toString();
			if(SystenmApi.isNullOrBlank(temp)){
				temp = "0";
			}
			double lv = Double.parseDouble(temp);
			quickcountrate();
		}
	};

	/**
	 * 计算金额
	 * **/
	public void quickcountrate() {

		JsonBuilder builder = new JsonBuilder();
		builder.put("id", id);
		builder.put("amount", mEditNum.getText().toString());

		BaseHttpClient.post(getBaseContext(), Default.quickcountrate, builder,
				new JsonHttpResponseHandler() {

					@Override
					public void onStart() {
						// TODO Auto-generated method stub
						super.onStart();
					}

					@Override
					public void onSuccess(int statusCode, Header[] headers,
										  JSONObject json) {
						// TODO Auto-generated method stub
						super.onSuccess(statusCode, headers, json);
						try {

							if (statusCode == 200) {
								if (json.getInt("status") == 1) {
									if (json.has("amount")) {
										amount=json.optString("amount", "0");
									}
									tv_yjsy.setText(amount);
								} else {
									String message = json.getString("message");
									SystenmApi.showCommonErrorDialog(Tender_ZR_Activity.this, json.getInt("status"),message);
								}
							} else {
								showCustomToast(R.string.toast1);
							}
						} catch (Exception e) {
							e.printStackTrace();
						}
						dismissLoadingDialog();
					}

					@Override
					public void onFailure(int statusCode, Header[] headers,
										  String responseString, Throwable throwable) {
						// TODO Auto-generated method stub
						super.onFailure(statusCode, headers, responseString,
								throwable);
						dismissLoadingDialog();
						showCustomToast(responseString);
					}
				});

	}
	
	public void updateInfo(JSONObject json) {

			if (json.has("user_money")) {
				tv_account_money.setText(json.optString("user_money", "0"));
			}
			if (json.has("invest_id")) {
				invest_id=json.optString("invest_id", "0");
			}
	}

	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.back:
			finish();
			break;
		case R.id.enter_money:

			if (SystenmApi.isNullOrBlank(mEditNum.getText().toString())) {
				showCustomToast("请输入投资金额");
				return ;
			}

			if(Default.IS_YB){

				if (SystenmApi.isNullOrBlank(mEditPassword.getText().toString())) {
					showCustomToast("请输入支付密码");
					return ;
				}
			}

			if(Default.IS_YB){
				Intent intent = new Intent(Tender_ZR_Activity.this, YBPayActivity.class);
				intent.putExtra("YB_TYPE", 6);
				intent.putExtra("id", invest_id);
				intent.putExtra("money", mEditNum.getText().toString());
				startActivity(intent);
				finish();
			}else{
				doHttpMoney();
			}
			break;
		}
	}
	

	public void doHttpBuy() {
		JsonBuilder builder = new JsonBuilder();
		builder.put("id", id);

		BaseHttpClient.post(getBaseContext(), Default.debt_ajax_invest, builder,
				new JsonHttpResponseHandler() {

					@Override
					public void onStart() {
						// TODO Auto-generated method stub
						super.onStart();
						showLoadingDialogNoCancle(getResources().getString(
								R.string.toast2));
					}

					@Override
					public void onSuccess(int statusCode, Header[] headers,
							JSONObject json) {
						// TODO Auto-generated method stub
						super.onSuccess(statusCode, headers, json);

						try {
							if (statusCode == 200) {
								if (json.getInt("status") == 1) {
									dismissLoadingDialog();
									updateInfo(json);
								} else {
									String message = json.getString("message");
									SystenmApi.showCommonErrorDialog(Tender_ZR_Activity.this, json.getInt("status"),message);
								}
							} else {
								showCustomToast(R.string.toast1);
								finish();
							}
						} catch (Exception e) {
							e.printStackTrace();
						}
						dismissLoadingDialog();
					}

					@Override
					public void onFailure(int statusCode, Header[] headers,
							String responseString, Throwable throwable) {
						// TODO Auto-generated method stub
						super.onFailure(statusCode, headers, responseString,
								throwable);
						dismissLoadingDialog();
						showCustomToast(responseString);
					}
				});
	}
	 /**最终购买*/
	private void doHttpMoney() {
		JsonBuilder builder = new JsonBuilder();
		builder.put("id", invest_id);
		builder.put("pin", mEditPassword.getText().toString());
		builder.put("money", mEditNum.getText().toString());

		BaseHttpClient.post(getBaseContext(), Default.debt_investmoney, builder,
                        new JsonHttpResponseHandler() {
					@Override
					public void onStart() {
						// TODO Auto-generated method stub
						super.onStart();
						showLoadingDialogNoCancle(getResources().getString(
								R.string.toast2));
					}

					@Override
					public void onSuccess(int statusCode, Header[] headers,
							JSONObject json) {
						// TODO Auto-generated method stub
						super.onSuccess(statusCode, headers, json);
						try {
							if (statusCode == 200) {
								if (json.getInt("status") == 1) {
									showCustomToast(json.getString("message"));
									finish();
								} else {
									showCustomToast(json.getString("message"));
								}
							} else {
								showCustomToast(R.string.toast1);
							}
						} catch (Exception e) {
							e.printStackTrace();
						}
						dismissLoadingDialog();
					}

					@Override
					public void onFailure(int statusCode, Header[] headers,
							String responseString, Throwable throwable) {
						// TODO Auto-generated method stub
						super.onFailure(statusCode, headers, responseString,
								throwable);
						dismissLoadingDialog();
						showCustomToast(responseString);
					}
				});
	}
}
