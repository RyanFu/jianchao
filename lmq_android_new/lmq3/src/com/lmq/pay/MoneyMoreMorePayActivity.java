package com.lmq.pay;

import java.util.LinkedHashMap;
import java.util.Map;

import org.apache.http.Header;
import org.json.JSONObject;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.lmq.R;
import com.lmq.http.BaseHttpClient;
import com.lmq.main.api.BaseActivity;
import com.lmq.main.api.JsonBuilder;
import com.lmq.main.api.MyLog;
import com.lmq.main.api.SystenmApi;
import com.lmq.main.util.Default;
import com.lmq.http.JsonHttpResponseHandler;
import com.money.more.activity.ControllerActivity;
import com.money.more.bean.ParamMap;
import com.money.more.utils.RSAUtil;

public class MoneyMoreMorePayActivity extends BaseActivity implements
		OnClickListener {

	Button mEnter_money;

	private EditText mEdit_money;
	private EditText mEdit_pass;
	private String mMoney, mPassword;

	private int mType; // 0 充值 1 提现

	private String RechargeMoneymoremore = "m6154";
	private String PlatformMoneymoremore = "p1";
	private String WithdrawMoneymoremore = "m6154";
	private String OrderNo;
	private String FeeType;
	private String Amount, FeePercent, FeeRate;
	private String Remark1;
	private String NotifyURL, ReturnURL;
	private String BranchBankName, CardType, BankCode, Province, City, CardNo;

	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.moneymoremore);

		mType = getIntent().getStringExtra("type").equals("cz") ? 0 : 1;

		findViewById(R.id.back).setOnClickListener(this);

		mEnter_money = (Button) findViewById(R.id.enter_money);
		mEnter_money.setOnClickListener(this);

		mEdit_money = (EditText) findViewById(R.id.edit_money);
		// mEdit_pass = (EditText) findViewById(R.id.edit_pass);

		TextView title = (TextView) findViewById(R.id.textView1);

		if (mType == 0)// 充值
		{
			mEdit_money.setHint("请输入充值金额");
			title.setText("我要充值");
			mEnter_money.setText("充    值");
		} else
		// 提现
		{
			mEdit_money.setHint("请输入提现金额");
			title.setText("我要提现");
			mEnter_money.setText("提    现");
		}

	}

	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.enter_money:

			mMoney = mEdit_money.getText().toString();
			// mPassword = mEdit_pass.getText().toString();
			if (mMoney.equals("")) {
				showCustomToast("请输入" + (mType == 0 ? "充值" : "提现") + "金额");
				return;
			}

			if (mType == 0) {
				doHttpCz();
			} else {
				doHttpTx();
			}
			break;
		case R.id.back:
			finish();
			break;
		}
	}

	public void clearEnterInfo() {
		mEdit_money.setText("");
		mEdit_pass.setText("");
	}

	// 充值
	private void loan() {
		Map<String, String> map = new LinkedHashMap<String, String>();
		//map.put("phone", "1");
		map.put("RechargeMoneymoremore", RechargeMoneymoremore);// 充值人乾多多标识
		map.put("PlatformMoneymoremore", PlatformMoneymoremore);// 平台乾多多标识
		map.put("OrderNo", OrderNo);// 平台的充值订单号
		map.put("Amount", Amount);// 金额
		map.put("RechargeType", "2");// 充值类型 空.网银充值 1.代扣充值 2.快捷支付 3.汇款充值
		map.put("FeeType", FeeType);// 手续费类型 1.充值成功时从充值人账户全额扣除
									// 2.充值成功时从平台自有账户全额扣除
		// 3.充值成功时从充值人账户扣除与提现手续费的差值 快捷支付必填，其他类型留空
		map.put("NotifyURL", NotifyURL);
		String info = RechargeMoneymoremore + PlatformMoneymoremore + OrderNo
				+ Amount + "2" + FeeType + NotifyURL;
		String singInfo = RSAUtil.getInstance().signData(info,
				Default.privateKey);
		map.put("SignInfo", singInfo);
//		ControllerActivity.setParams(map, 3);
//		Intent intent = new Intent(MoneyMoreMorePayActivity.this,
//				ControllerActivity.class);
//		startActivityForResult(intent, 300);
		
		Intent intent = new Intent(this,ControllerActivity.class);
		ParamMap params = new ParamMap();
		params.setMap(map);
		//添加请求参数
		intent.putExtra("params", params);
		//添加请求类型，1：开户
		intent.putExtra("type", 2);
		startActivityForResult(intent, 2);
	}

	// 提现
	private void getMoney() {
		Intent intent = new Intent(MoneyMoreMorePayActivity.this,
				ControllerActivity.class);
		Map<String, String> map = new LinkedHashMap<String, String>();
		//map.put("phone", "1");
		map.put("WithdrawMoneymoremore", WithdrawMoneymoremore);
		map.put("PlatformMoneymoremore", PlatformMoneymoremore);
		map.put("OrderNo", OrderNo);
		map.put("Amount", Amount);
		map.put("FeePercent", FeePercent);
		map.put("FeeRate", FeeRate);
		String cardNo = RSAUtil.getInstance().encryptData(CardNo,
				Default.publicKey);
		map.put("CardNo", cardNo);
		map.put("CardType", CardType);// 银行卡类型 0.借记卡 1.信用卡
		map.put("BankCode", BankCode);// 银行代码
		map.put("Province", Province);// 开户行省份
		map.put("City", City);// 开户行城市
		map.put("Remark1", Remark1);
		map.put("NotifyURL", NotifyURL);
		String info = WithdrawMoneymoremore + PlatformMoneymoremore + OrderNo
				+ Amount + FeePercent + FeeRate + CardNo + CardType + BankCode
				+ Province + City + Remark1 + NotifyURL;
		String signInfo = RSAUtil.getInstance().signData(info,
				Default.privateKey);
		map.put("SignInfo", signInfo);
		
		ParamMap params = new ParamMap();
		params.setMap(map);
		intent.putExtra("type", 3);
		intent.putExtra("params", params);
		startActivityForResult(intent, 400);  
	}

	private void doHttpTx() {

		JsonBuilder builder = new JsonBuilder();
		builder.put("money", mMoney);

		BaseHttpClient.post(getBaseContext(), Default.withdrawMoney, builder,
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
									Amount = json.getString("Amount");
									BranchBankName = json
											.getString("BranchBankName");
									FeePercent = json.getString("FeePercent");
									FeeRate = json.getString("FeeRate");
									Province = json.getString("Province");
									City = json.getString("City");
									Remark1 = json.getString("Remark1");
									CardNo = json.getString("CardNo");
									OrderNo = json.getString("OrderNo");// "6222031609000278619";
									WithdrawMoneymoremore = json
											.getString("WithdrawMoneymoremore");
									BankCode = json.getString("BankCode");
									NotifyURL = json.getString("NotifyURL");
									PlatformMoneymoremore = json
											.getString("PlatformMoneymoremore");
									CardType = json.getString("CardType");
									// ReturnURL = json.getString("ReturnURL");

									getMoney();
								} else {
									String message = json.getString("message");
									SystenmApi.showCommonErrorDialog(MoneyMoreMorePayActivity.this, json.getInt("status"), message);


								}
							} else {
								showCustomToast("提现失败");
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

	private void doHttpCz() {
		JsonBuilder builder = new JsonBuilder();
		builder.put("money", mMoney);

		BaseHttpClient.post(getBaseContext(), Default.chargeMoney, builder,
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
									RechargeMoneymoremore = json
											.getString("RechargeMoneymoremore");
									PlatformMoneymoremore = json
											.getString("PlatformMoneymoremore");
									NotifyURL = json.getString("NotifyURL");
									Amount = json.getString("Amount");
									OrderNo = json.getString("OrderNo");
									ReturnURL = json.getString("ReturnURL");
									FeeType = json.optString("FeeType", "2");
									loan();
								} else {
									String message = json.getString("message");
									SystenmApi.showCommonErrorDialog(MoneyMoreMorePayActivity.this, json.getInt("status"), message);
								}
							} else {
								showCustomToast("充值失败");
								MyLog.d("zzx", "充值失败");
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

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		int code = data.getIntExtra("code", -1);
		if (code == 88) {
			showCustomToast(mType == 0 ? "充值成功" : "提现成功");
		} else {
			String message = data.getStringExtra("message");
			Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
		}
	}

}
