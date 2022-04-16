package xyz.hyli.genshinhelper;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.widget.ViewPager2;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Ajiu
 */
public class MainActivity extends FragmentActivity {
    private TextView tv_account_nickname,tv_account_uid;
    private String SToken;
    private String Uid,ServerID,Cookie;
    private SharedPreferences data;
    private JSONArray uid_list;
    private String default_Uid,default_Nickname,default_Level,default_region_name,default_region,default_Cookie;
    @SuppressLint("Range")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        data = getSharedPreferences("data", Context.MODE_PRIVATE);
        uid_list = JSON.parseArray(data.getString("uid_list", "[]"));
        default_Uid = data.getString("default_uid", "");
        default_Nickname = data.getString("default_nickname", "");
        default_Level = data.getString("default_level", "");
        default_region_name = data.getString("default_region_name", "");
        default_region = data.getString("default_region", "");
        default_Cookie = data.getString("default_cookie", "");
        // 首次启动
        if (data.getBoolean("first_launch",true)) {
            showFirstLaunchAlertDialog();
            SharedPreferences.Editor editor = data.edit();
            editor.putBoolean("first_launch", false);
            editor.apply();
        }
        initFragment();
        initInfo();

    }
    private void initInfo() {
        tv_account_nickname = findViewById(R.id.tv_account_nickname);
        tv_account_uid = findViewById(R.id.tv_account_uid);
        if (default_Uid != "") {
            tv_account_nickname.setText(default_Nickname+"   Lv."+default_Level);
            tv_account_uid.setText("Uid:"+default_Uid+"   "+default_region_name);
            tv_account_nickname.setTextColor(Color.DKGRAY);
            tv_account_uid.setTextColor(Color.LTGRAY);
        }
        new Thread(new Runnable() {
            @Override
            public void run() {
                Log.i("uid_list",uid_list.toString());
                List<String> ac_l = new ArrayList<>();
                for (Object uid : uid_list) {
                    SharedPreferences sharedPreferences = getSharedPreferences(uid.toString(), Context.MODE_PRIVATE);
                    String account_id = sharedPreferences.getString("account_id", null);
                    String SToken = sharedPreferences.getString("SToken", null);
                    if (ac_l.contains(account_id) == false) {
                        ac_l.add(account_id);
                        updateUserRoleInfo(SToken,account_id);
                    }

                }
            }
        }).start();
    }
    public void updateUserRoleInfo(String SToken,String account_id) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                String cookie_token = MihoyoAPIs.getCookieAccountInfoBySToken(SToken, account_id);
                String Cookie = "account_id=" + account_id + ";cookie_token=" + cookie_token;
                // 得到绑定角色信息
                List<JSONObject> UserGameRoles = MihoyoAPIs.getUserGameRolesByCookie(Cookie);
                for (JSONObject role : UserGameRoles) {
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            SharedPreferences.Editor editor = data.edit();
                            if (role.getString("game_uid").contains(default_Uid)) {
                                editor.putString("default_uid", role.getString("game_uid"));
                                editor.putString("default_nickname", role.getString("nickname"));
                                editor.putString("default_level", role.getString("level"));
                                editor.putString("default_region_name", role.getString("region_name"));
                                editor.putString("default_region", role.getString("region"));
                                editor.putString("default_cookie", Cookie);
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        tv_account_nickname.setText(role.getString("nickname")+"   Lv."+role.getString("level"));
                                    }
                                });
                            }
                            editor.commit();
                        }
                    }).start();
                    SharedPreferences sharedPreferences = getSharedPreferences(role.getString("game_uid"), Context.MODE_PRIVATE);
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.putString("Uid", role.getString("game_uid"));
                    editor.putString("level", role.getString("level"));
                    editor.putString("nickname", role.getString("nickname"));
                    editor.putString("region_name", role.getString("region_name"));
                    editor.putString("ServerID", role.getString("region"));
                    editor.putString("Cookie", Cookie);
                    editor.putString("account_id", account_id);
                    editor.putString("SToken", SToken);
                    editor.commit();
                }
            }
        }).start();
    }
    private void initFragment() {
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottomNavigation);
        Button bt_accountswitcher = findViewById(R.id.bt_AccountSwitcher);
        ViewPager2 viewPager = findViewById(R.id.MainActivityViewPager);
        List<Fragment> fragmentList = new ArrayList<>();
        fragmentList.add(mFragment.newInstance("概览"));
        fragmentList.add(mFragment.newInstance("工具"));
        fragmentList.add(mFragment.newInstance("图鉴"));
        fragmentList.add(mFragment.newInstance("设置"));
        mFragmentPageAdapter mFragmentPagerAdapter = new mFragmentPageAdapter(getSupportFragmentManager(),getLifecycle(),fragmentList);
        viewPager.setAdapter(mFragmentPagerAdapter);
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
    private void showView_Sign() {
        ImageView main_sign_award_img = findViewById(R.id.main_sign_award_img);
        TextView main_sign_award_txt = findViewById(R.id.main_sign_award_txt);
        main_sign_award_txt.setText("111");
        if (default_Uid != "") {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    JSONObject SignInfo = MihoyoAPIs.getSignInfo(default_Uid, default_region, default_Cookie);
                    List<JSONObject> AwardList = MihoyoAPIs.getSignAward();
                    JSONObject todayAward = MihoyoAPIs.getTodayAward(default_Uid, default_region, default_Cookie, SignInfo, AwardList);
                    URL myFileURL;
                    Bitmap bitmap = null;
                    try{
                        myFileURL = new URL(todayAward.getString("icon"));
                        //获得连接
                        HttpURLConnection conn=(HttpURLConnection)myFileURL.openConnection();
                        //设置超时时间为6000毫秒，conn.setConnectionTiem(0);表示没有时间限制
                        conn.setConnectTimeout(6000);
                        //连接设置获得数据流
                        conn.setDoInput(true);
                        //不使用缓存
                        conn.setUseCaches(true);
                        //得到数据流
                        InputStream is = conn.getInputStream();
                        //解析得到图片
                        bitmap = BitmapFactory.decodeStream(is);
                        //关闭数据流
                        is.close();
                    }catch(Exception e){
                        e.printStackTrace();
                    }
                    Bitmap finalBitmap = bitmap;
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            main_sign_award_txt.setText(todayAward.getString("name")+"×"+todayAward.getString("cnt"));
                            if (finalBitmap != null) {
                                main_sign_award_img.setImageBitmap(finalBitmap);
                            }
                        }
                    });
                }
            }).start();
        }
    }
    public void startLoginWebView(View view) {
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
        builder.setMessage("账号未登录, 登录后可使用米游社相关功能");
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
        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(Color.BLACK);
        dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(Color.DKGRAY);
        setScreenBgDarken();
    }
}
