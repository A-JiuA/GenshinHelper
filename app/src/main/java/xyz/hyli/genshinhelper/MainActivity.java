package xyz.hyli.genshinhelper;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Ajiu
 */
public class MainActivity extends FragmentActivity {
    @SuppressLint("Range")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        SharedPreferences sharedPreferences= getSharedPreferences("data", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        // 首次启动
        if (sharedPreferences.getBoolean("first_launch",true)) {
            showFirstLaunchAlertDialog();
            editor.putBoolean("first_launch", false);
            editor.apply();
        }

        initFragment();

    }

    public void initFragment() {
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottomNavigation);

        ViewPager2 viewPager = findViewById(R.id.MainActivityViewPager);
        List<Fragment> fragmentList = new ArrayList<>();
        fragmentList.add(mFragment.newInstance("概览"));
        fragmentList.add(mFragment.newInstance("工具"));
        fragmentList.add(mFragment.newInstance("图鉴"));
        fragmentList.add(mFragment.newInstance("设置"));
        mFragmentPagerAdapter mFragmentPagerAdapter = new mFragmentPagerAdapter(getSupportFragmentManager(),getLifecycle(),fragmentList);
        viewPager.setAdapter(mFragmentPagerAdapter);
        viewPager.setCurrentItem(0);

        viewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                bottomNavigationView.getMenu().getItem(position).setChecked(true);
                }
        });


        bottomNavigationView.setItemIconTintList(null);
        bottomNavigationView.setOnItemSelectedListener(menuItem -> {
            switch (menuItem.getItemId()) {
                case R.id.tools:
                    viewPager.setCurrentItem(1);
                    break;
                case R.id.map:
                    viewPager.setCurrentItem(2);
                    break;
                case R.id.settings:
                    viewPager.setCurrentItem(3);
                    break;
                default:
                    viewPager.setCurrentItem(0);
            }
            return true;
        });


    }
    public void onPageSelected(int position) {
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottomNavigation);
        bottomNavigationView.getMenu().getItem(position).setChecked(true);
    }
    public void startLoginwebView(View view) {
        startActivity(new Intent(this, LoginWebView.class));
    }
    @SuppressLint("Range")
    // 设置屏幕背景变暗
    private void setScreenBgDarken() {
        WindowManager.LayoutParams lp = getWindow().getAttributes();
        lp.alpha = 0.5f;
        lp.dimAmount = 0.5f;
        getWindow().setAttributes(lp);
    }
    // 设置屏幕背景变亮
    private void setScreenBgLight() {
        WindowManager.LayoutParams lp = getWindow().getAttributes();
        lp.alpha = 1.0f;
        lp.dimAmount = 1.0f;
        getWindow().setAttributes(lp);
    }
    private void showFirstLaunchAlertDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("账号未登录");
        builder.setMessage("登陆后可使用米游社相关功能");
        builder.setPositiveButton("网页登录", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                setScreenBgLight();
                startActivity(new Intent(MainActivity.this, LoginWebView.class));
            }
        });
        builder.setNegativeButton("跳过", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                setScreenBgLight();
            }
        });
        AlertDialog dialog = builder
                .create();
        dialog.setCancelable(false);
        dialog.show();
        setScreenBgDarken();
    }
}
