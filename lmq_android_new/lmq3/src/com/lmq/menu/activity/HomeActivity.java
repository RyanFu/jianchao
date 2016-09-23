package com.lmq.menu.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.ImageView;

import com.lmq.menu.MainTabNewActivity;
import com.lmq.R;
import com.lmq.main.activity.login.loginActivity;
import com.lmq.main.api.BaseFragmentActivity;
import com.lmq.main.api.MyLog;
import com.lmq.main.dialog.InvestPopList;
import com.lmq.main.util.Default;
import com.lmq.view.CustomViewPager;
import com.lmq.view.DrawableCenterButton;
import com.viewpagerindicator.TabPageIndicator;

public class HomeActivity extends BaseFragmentActivity {

    private static final String[] TITLE = new String[]{"直投", "理财"};
    public static InvestPopList popList;
    private int tipsType;

    private ImageView mtriangle;

    private TabPageIndicator indicator;
    private DrawableCenterButton titleView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_home_new);

        mtriangle = (ImageView) findViewById(R.id.triangle);


        FragmentPagerAdapter adapter = new TabPageIndicatorAdapter(getSupportFragmentManager());
        CustomViewPager pager = (CustomViewPager) findViewById(R.id.pager);
        pager.setScanScroll(true);
        pager.setAdapter(adapter);

        indicator = (TabPageIndicator) findViewById(R.id.indicator);
        indicator.setViewPager(pager);
        indicator.setVisibility(View.GONE);
        titleView = (DrawableCenterButton) findViewById(R.id.title);
        titleView.setText(TITLE[0]);
        titleView.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {

                Animation animation = AnimationUtils.loadAnimation(HomeActivity.this, R.anim.triangle_totate);
                mtriangle.clearAnimation();
                mtriangle.setImageResource(R.drawable.wite_arrow_down);
                mtriangle.startAnimation(animation);
                animation.setFillAfter(true);

                shwoPop(v);
            }
        });

        indicator.setOnPageChangeListener(new OnPageChangeListener() {

            @Override
            public void onPageSelected(int arg0) {

                titleView.setText(TITLE[arg0]);
                popList.setDefauleSelect(arg0);

                Animation animation = AnimationUtils.loadAnimation(HomeActivity.this, R.anim.triangle_totate2);
                titleView.clearAnimation();
                mtriangle.setImageResource(R.drawable.wite_arrow_up);
                mtriangle.startAnimation(animation);
                animation.setFillAfter(true);
                MyLog.e("currentPage---->", arg0 + "");

            }

            @Override
            public void onPageScrolled(int arg0, float arg1, int arg2) {

            }

            @Override
            public void onPageScrollStateChanged(int arg0) {

            }
        });

        popList = new InvestPopList(HomeActivity.this);
        popList.addItems(TITLE);

        popList.setOnItemClickLinstener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {


                if (position != popList.getDefauleSelect()) {
                    popList.setDefauleSelect(position);

                }

                titleView.setText(TITLE[position]);
                tipsType = position;
                popList.dissmiss();
change(position);




            }
        });

    }
    public void change ( int position)

    {

        if (position == 4) {
            if (Default.userId == 0) {

                Intent intent = new Intent();

                intent.setClass(HomeActivity.this, loginActivity.class);

                startActivityForResult(intent, 3000);

            } else {

                indicator.setCurrentItem(position);

            }
        } else {
            indicator.setCurrentItem(position);

        }

    }

    @Override
    protected void onResume() {
        super.onResume();
//               int positon  = MainTabNewActivity.mainTabNewActivity.getHomeType();
//                        change(positon);
    }

    public static void shwoPop(View v) {

        if (!popList.isShowing()) {
            popList.showPOpList(v);


        }


    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 3000) {

            if (Default.userId == 0) {
                indicator.setCurrentItem(0);
                popList.setDefauleSelect(0);
            } else {
                indicator.setCurrentItem(4);
                popList.setDefauleSelect(4);
            }

        }
    }

    class TabPageIndicatorAdapter extends FragmentPagerAdapter {
        public TabPageIndicatorAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            Fragment fragment;
            if (position == 4) {
                fragment = new LHBItemFragment();

            } else {
                fragment = new HomeItemFragment();
                Bundle args = new Bundle();
                args.putInt("switch_flag", position);
                fragment.setArguments(args);
            }

            return fragment;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return TITLE[position % TITLE.length];
        }

        @Override
        public int getCount() {
            return TITLE.length;
        }
    }


}
