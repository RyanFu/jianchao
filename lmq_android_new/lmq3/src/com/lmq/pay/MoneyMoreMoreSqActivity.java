package com.lmq.pay;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import org.apache.http.Header;
import org.json.JSONObject;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TextView;

import com.lmq.R;
import com.lmq.http.BaseHttpClient;
import com.lmq.main.api.BaseActivity;
import com.lmq.main.api.MyLog;
import com.lmq.main.api.SystenmApi;
import com.lmq.main.util.Default;
import com.lmq.http.JsonHttpResponseHandler;
import com.money.more.activity.ControllerActivity;
import com.money.more.bean.ParamMap;
import com.money.more.utils.RSAUtil;

public class MoneyMoreMoreSqActivity extends BaseActivity implements
		OnClickListener {

	private int mmm_status;
	private int sq1_status;
	private int sq2_status;

	private TextView mInfo[];

	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.moneymoremore_kh_sq);
		Intent intent = getIntent();
		if (intent != null) {
			mmm_status = intent.getIntExtra("mmm", 0);
			sq1_status = intent.getIntExtra("sq1", 0);
			sq2_status = intent.getIntExtra("sq2", 0);
		}
		mInfo = new TextView[3];
		mInfo[0] = (TextView) findViewById(R.id.tv_mmm);
		mInfo[1] = (TextView) findViewById(R.id.tv_mmmsq);
		mInfo[2] = (TextView) findViewById(R.id.tv_mmmsq2);

		mInfo[0].setText(getStyle(mmm_status));
		mInfo[1].setText(getStyle(sq1_status));
		mInfo[2].setText(getStyle(sq2_status));

		findViewById(R.id.rl_mmm).setOnClickListener(this);
		findViewById(R.id.rl_mmmsq).setOnClickListener(this);
		findViewById(R.id.rl_mmmsq2).setOnClickListener(this);
		findViewById(R.id.back).setOnClickListener(this);
	}

	public String getStyle(int type) {
		switch (type) {
		case 0:
			return "未授权";
		case 1:
		default:
			return "已授权";

		}
	}

	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.back:
			finish();
			break;
		case R.id.rl_mmm:
			if (mmm_status == 1) {
				showCustomToast("您已成功绑定托管信息！");
				return;
			}
			doHttpMmm();
			break;
		case R.id.rl_mmmsq:
			if (sq1_status == 1) {
				showCustomToast("您已开启投标授权！");
				return;
			}
			doHttpsq(1, Default.MoneyMmsq1);
			break;
		case R.id.rl_mmmsq2:
			if (sq2_status == 1) {
				showCustomToast("您已开启还款授权！");
				return;
			}
			doHttpsq(2, Default.MoneyMmsq2);
			break;

		}
	}

	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		// TODO Auto-generated method stub
		super.onActivityResult(requestCode, resultCode, data);
		StringBuilder sb = new StringBuilder();
		if (resultCode == 100 || resultCode == 200) {// 注册返回
			sb.append("注册返回数据:");
			int code = data.getIntExtra("code", -1);
			String message = data.getStringExtra("message");
			sb.append("code:").append(code).append(",message:").append(message);
			if (code != 88) {
				String AccountNumber = data.getStringExtra("AccountNumber");
				sb.append(",AccountNumber:").append(AccountNumber);
				showCustomToast(sb.toString());
			} else {
				showCustomToast("您已成功绑定托管信息！");
				mmm_status = 1;
				mInfo[0].setText(getStyle(mmm_status));
			}
		} else if (resultCode == 600) {// 授权接口返回
			sb.append("提现数据返回:");
			int code = data.getIntExtra("code", -1);
			String message = data.getStringExtra("message");
			sb.append("code:").append(code).append(",message").append(message);
			if (code != 88) {
				String authorizeType = data.getStringExtra("AuthorizeType");
				sb.append(",authorizeType").append(authorizeType);
				showCustomToast(sb.toString());
			} else {
				showCustomToast("您已授权成功！");
				if (requestCode == 100) {
					sq1_status = 1;
					mInfo[1].setText(getStyle(sq1_status));
				} else if (requestCode == 200) {
					sq2_status = 1;
					mInfo[2].setText(getStyle(sq2_status));

				}
			}
		}
	}

	/**
	 * 绑定钱多多 信息 开户
	 * */
	public void doHttpMmm() {

		BaseHttpClient.post(getBaseContext(), Default.MoneyMm, null,
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
									getMoneyMm(json);
								} else {
									String message = json.getString("message");
									SystenmApi.showCommonErrorDialog(MoneyMoreMoreSqActivity.this, json.getInt("status"), message);
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

	public void getMoneyMm(JSONObject json) {
		try {
			String info[] = new String[10];
			info[0] = json.getString("phone");
			info[1] = json.getString("email");
			info[2] = json.getString("real_name");
			info[3] = json.getString("idcard");
			info[4] = json.getString("user_name");
			info[5] = json.getString("plat_form_money_moremore");
			info[6] = json.getString("register_type");
			info[7] = json.getString("account_type");
			info[8] = json.getString("notifyURL");
			info[9] = json.getString("Remark1");

			register(info);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * 绑定/注册
	 */
	private void register(String info[]) {
		Intent intent = new Intent(MoneyMoreMoreSqActivity.this,
				ControllerActivity.class);
		Map<String, String> params = new LinkedHashMap<String, String>();
		
		params.put("RegisterType", info[6]);
		params.put("AccountType", info[7]);
		params.put("Mobile", info[0]);
		params.put("Email", info[1]);
		params.put("RealName", info[2]);
		params.put("IdentificationNo", info[3]);
		params.put("LoanPlatformAccount", info[4]);
		params.put("PlatformMoneymoremore", info[5]);
		params.put("Remark1", info[9]);
		params.put("NotifyURL", info[8]);
		
		String info2 = info[6] + info[7]+info[0]+info[1]+info[2]+info[3]+info[4]+info[5]+info[9]+info[8];
		String sign = RSAUtil.getInstance().signData(info2, Default.privateKey);
		params.put("SignInfo", sign);// 签名信息
		 
		
		ParamMap map = new ParamMap();
		map.setMap(params);
		//添加请求参数
		intent.putExtra("params", map);
		//添加请求类型，1：开户
		intent.putExtra("type", 1);
		startActivityForResult(intent, 100);
	}

	

	/**
	 * 绑定钱多多 信息 开户
	 * */
	public void doHttpsq(final int type, String url) {

		BaseHttpClient.post(getBaseContext(), url, null,
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
									getMoneyMmsq(type, json);
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

	public void getMoneyMmsq(int sqtype, JSONObject json) {
		try {
			String info[] = new String[4];
			info[0] = json.getString("TypeOpen");
			info[1] = json.getString("NotifyURL");
			info[2] = json.getString("Platform");
			info[3] = json.getString("MoneyId");

			authorization(sqtype, info);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * 调用授权接口
	 */
	private void authorization(int type, String info[]) {
		Intent intent = new Intent(this, ControllerActivity.class);
		Map<String, String> map = new LinkedHashMap<String, String>();
		// 根据文档添加对应的参数
		 
		map.put("MoneymoremoreId", info[3]);
		map.put("PlatformMoneymoremore", info[2]);
		map.put("AuthorizeTypeOpen", info[0]);
		map.put("NotifyURL", info[1]);
		// 对参数进行加密
		String info2 = info[3] + info[2]+info[0]+info[1];
		String sign = RSAUtil.getInstance().signData(info2, Default.privateKey);
		map.put("SignInfo", sign);// 签名信息
		MyLog.e("123", map.toString());
		//设置参数和访问的类型
		ParamMap params = new ParamMap();
		params.setMap(map);
		intent.putExtra("type", 5);
		intent.putExtra("params", params);
		startActivityForResult(intent, 100* type);
	}
}
