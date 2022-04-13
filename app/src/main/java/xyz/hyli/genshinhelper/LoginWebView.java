package xyz.hyli.genshinhelper;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.net.http.SslError;
import android.os.Bundle;
import android.webkit.CookieManager;
import android.webkit.SslErrorHandler;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import java.util.List;


public class LoginWebView extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_loginwebview);
        WebView webView = this.findViewById(R.id.webview);
        webView.getSettings().setJavaScriptEnabled(true);
        webView.getSettings().setUserAgentString(MihoyoAPIs.UA);
        CookieManager.getInstance().removeAllCookie();
        webView.getSettings().setCacheMode(WebSettings.LOAD_NO_CACHE);
        webView.getSettings().setAllowUniversalAccessFromFileURLs(true);
        webView.getSettings().setDomStorageEnabled(true);
        webView.setWebChromeClient(new WebChromeClient());
        webView.setWebViewClient(new WebViewClient(){
            @Override
            public void onReceivedSslError(WebView view, SslErrorHandler handler, SslError error) {
                handler.proceed();
            }
            @Override
            public void onPageFinished(WebView view, String url) {
                if(url != null && url.contains("https://user.mihoyo.com/#/login")) {
                    String fun = "javascript:function getClass(parent,sClass) { var aEle=parent.getElementsByTagName('div'); var aResult=[]; var i=0; for(i<0;i<aEle.length;i++) { if(aEle[i].className==sClass) { aResult.push(aEle[i]); } }; return aResult; } ";
                    view.loadUrl(fun);
                    String fun2 = "javascript:function hideOther() {getClass(document,'mhy-footer')[0].style.display='none';getClass(document,'register-bar')[0].style.display='none';}";
                    view.loadUrl(fun2);
                    view.loadUrl("javascript:hideOther();");
                } else if(url != null && url.contains("https://user.mihoyo.com/#/account/home")){
                    CookieManager cookieManager = CookieManager.getInstance();
                    String Cookie = cookieManager.getCookie(url);
                    if (Cookie.contains("login_ticket")) {
                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                boolean success_flag = false;
                                try {
                                    // 得到login_ticket
                                    String login_ticket;
                                    if (Cookie.indexOf(";",Cookie.indexOf("login_ticket=")) == -1) {
                                        login_ticket = Cookie.substring(Cookie.indexOf("login_ticket=")+13);
                                    } else {
                                        login_ticket = Cookie.substring(Cookie.indexOf("login_ticket=")+13,Cookie.indexOf(";",Cookie.indexOf("login_ticket=")));
                                    }
                                    // 将信息写入数据库
                                    new Thread(new Runnable() {
                                        @Override
                                        public void run() {
                                            JSONObject cookie_info = MihoyoAPIs.getCookieAccountInfoByLoginTicket(login_ticket);
                                            String account_id = cookie_info.getString("account_id");
                                            String SToken = MihoyoAPIs.getMultiTokenByLoginTicket(login_ticket,account_id);
                                            String cookie_token = MihoyoAPIs.getCookieAccountInfoBySToken(SToken, account_id);
                                            String Cookie = "account_id=" + account_id + ";cookie_token=" + cookie_token;
                                            // 得到绑定角色信息
                                            List<JSONObject> UserGameRoles = MihoyoAPIs.getUserGameRolesByCookie(Cookie);
                                            for (JSONObject role : UserGameRoles) {
                                                new Thread(new Runnable() {
                                                    @Override
                                                    public void run() {
                                                        SharedPreferences sharedPreferences = getSharedPreferences("data", Context.MODE_PRIVATE);
                                                        SharedPreferences.Editor editor = sharedPreferences.edit();
                                                        JSONArray array = JSON.parseArray(sharedPreferences.getString("uid_list", "[]"));
                                                        if (array.contains(role.getString("game_uid"))) {
                                                            array.add(role.getString("game_uid"));
                                                            editor.putString("uid_list",JSON.toJSONString(array));
                                                        }
                                                        if (role.getBoolean("is_chosen")) {
                                                            editor.putString("default_uid", role.getString("game_uid"));
                                                            editor.putString("default_nickname", role.getString("nickname"));
                                                            editor.putString("default_level", role.getString("level"));
                                                            editor.putString("default_region_name", role.getString("region_name"));
                                                            editor.putString("default_region", role.getString("region"));
                                                            editor.putString("default_cookie", Cookie);
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
                                                runOnUiThread(new Runnable() {
                                                    @Override
                                                    public void run() {
                                                        Toast.makeText(LoginWebView.this,"登录成功，已自动获取Cookie",Toast.LENGTH_LONG).show();
                                                    }
                                                });
                                            }
                                        }
                                    }).start();

                                } catch (Exception e) {
                                    runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            Toast.makeText(LoginWebView.this,"登录异常，未获取到Cookie",Toast.LENGTH_LONG).show();
                                            Toast.makeText(LoginWebView.this,e.toString(),Toast.LENGTH_LONG).show();
                                        }
                                    });
                                }
                            }
                        }).start();
                    } else {
                        Toast.makeText(LoginWebView.this,"登录失败",Toast.LENGTH_LONG).show();
                    }
                    finish();
                }
            }
            });
        webView.loadUrl("https://user.mihoyo.com/#/login/captcha");
//        Toast.makeText(MainActivity.this,webView.getSettings().getUserAgentString(),Toast.LENGTH_LONG).show();

    }
}
