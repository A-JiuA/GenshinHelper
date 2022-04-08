package xyz.hyli.genshinhelper;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;

public class MainActivity extends Activity {
    @SuppressLint("Range")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        getWindow().setStatusBarColor(Color.TRANSPARENT);
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION);
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
