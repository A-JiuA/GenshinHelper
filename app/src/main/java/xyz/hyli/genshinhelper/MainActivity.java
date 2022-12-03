package xyz.hyli.genshinhelper;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Application;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.fragment.app.FragmentActivity;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

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
    public String default_Uid,default_Nickname,default_Level,default_region_name,default_region,default_Cookie;

    public Activity currentActivity;
    MyActivityLifecycleCallbacks myActivityLifecycleCallbacks = new MyActivityLifecycleCallbacks();

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
        initInfo();
        updateUserRoleInfo();
        registerActivityLifecycleCallbacks(myActivityLifecycleCallbacks);
    }
    private void initInfo() {
        tv_account_nickname = findViewById(R.id.tv_account_nickname);
        tv_account_uid = findViewById(R.id.tv_account_uid);
        if (default_Uid != "") {
            tv_account_nickname.setText(default_Nickname + "   Lv." + default_Level);
            tv_account_uid.setText("Uid:" + default_Uid + "   " + default_region_name);
            tv_account_nickname.setTextColor(Color.DKGRAY);
            tv_account_uid.setTextColor(Color.LTGRAY);
        }
    }
    private void updateUserRoleInfo() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                for (Object uid : uid_list) {
                    SharedPreferences sharedPreferences = getSharedPreferences(uid.toString(), Context.MODE_PRIVATE);
                    String SToken,account_id;
                    SToken = sharedPreferences.getString("SToken","");
                    account_id = sharedPreferences.getString("account_id","");

                    String cookie_token = MihoyoAPIs.getCookieAccountInfoBySToken(SToken, account_id);
                    String Cookie = "account_id=" + account_id + ";cookie_token=" + cookie_token;
                    // 得到绑定角色信息
                    List<JSONObject> UserGameRoles = MihoyoAPIs.getUserGameRolesByCookie(Cookie);
                    Log.i(Thread.currentThread().getStackTrace()[2].getMethodName(),UserGameRoles.toString());
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
                                    default_Uid = role.getString("game_uid");
                                    default_Nickname = role.getString("nickname");
                                    default_Level = role.getString("level");
                                    default_region_name = role.getString("region_name");
                                    default_region = role.getString("region");
                                    default_Cookie = Cookie;
                                    runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            tv_account_nickname.setText(role.getString("nickname")+"   Lv."+role.getString("level"));
                                            tv_account_uid.setText("Uid:" + role.getString("game_uid") + "   " + role.getString("region_name"));
                                        }
                                    });
                                }
                                editor.commit();
                            }
                        }).start();
                        sharedPreferences = getSharedPreferences(role.getString("game_uid"), Context.MODE_PRIVATE);
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
                        Log.i("info","Updated role info of "+role.getString("game_uid"));
                    }
                }
            }
        }).start();
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
                startActivityForResult(new Intent(MainActivity.this, LoginWebView.class),1);
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
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == 101) {
            updateUserRoleInfo();
        }
    }

    class MyActivityLifecycleCallbacks implements Application.ActivityLifecycleCallbacks {
        @Override
        public void onActivityCreated(Activity activity, Bundle savedInstanceState) {
        }
        @Override
        public void onActivityStarted(Activity activity) {
        }
        @Override
        public void onActivityResumed(Activity activity) {
            currentActivity = activity;
            Log.i("info", "Current Activity:" + currentActivity.getClass());
            if (currentActivity.getClass().toString().contains("MainActivity")) {

                updateUserRoleInfo();
            }
        }
        @Override
        public void onActivityPaused(Activity activity) {
        }
        @Override
        public void onActivityStopped(Activity activity) {
        }
        @Override
        public void onActivitySaveInstanceState(Activity activity, Bundle outState) {
        }
        @Override
        public void onActivityDestroyed(Activity activity) {
        }
    }
}
