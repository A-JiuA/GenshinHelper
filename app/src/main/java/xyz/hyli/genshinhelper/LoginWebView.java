package xyz.hyli.genshinhelper;

import android.app.Activity;
import android.content.ClipData;
import android.content.Context;
import android.graphics.Color;
import android.net.http.SslError;
import android.os.Bundle;
import android.view.View;
import android.webkit.CookieManager;
import android.webkit.SslErrorHandler;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;
import android.content.ClipboardManager;

import com.alibaba.fastjson.JSONObject;

import java.util.List;


public class LoginWebView extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState){
        getWindow().setStatusBarColor(Color.TRANSPARENT);
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_webview);
        WebView webView = this.findViewById(R.id.webview);
        webView.getSettings().setJavaScriptEnabled(true);
//        String ua = webView.getSettings().getUserAgentString();
//        webView.getSettings().setUserAgentString("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/99.0.4844.51 Safari/537.36 Edg/99.0.1150.39");
        CookieManager.getInstance().removeAllCookie();
//        WebView.setWebContentsDebuggingEnabled(true);
        webView.clearCache(true);
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
                                    // 得到Cookie
                                    JSONObject cookie_info = MihoyoAPIs.getCookieAccountInfoByLoginTicket(login_ticket);
                                    String account_id = cookie_info.getString("account_id");
                                    String cookie_token = cookie_info.getString("cookie_token");
                                    String Cookie = "account_id=" + account_id + ";cookie_token=" + cookie_token;
                                    String SToken = MihoyoAPIs.getMultiTokenByLoginTicket(login_ticket,account_id);
                                    // 得到绑定角色信息
                                    List<JSONObject> gameRoles = MihoyoAPIs.getUserGameRolesByCookie(Cookie);

                                    // 测试 获取SToken
//                                    runOnUiThread(new Runnable() {
//                                        @Override
//                                        public void run() {
//                                            Toast.makeText(webView.this,SToken,Toast.LENGTH_LONG).show();
//                                        }
//                                    });

                                    // 将信息写入数据库


                                    success_flag = true;
                                } catch (Exception e) {
                                    runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            Toast.makeText(LoginWebView.this,"登录异常，未获取到Cookie",Toast.LENGTH_LONG).show();
                                            Toast.makeText(LoginWebView.this,e.toString(),Toast.LENGTH_LONG).show();
                                        }
                                    });

                                } finally {
                                    if (success_flag){
                                        runOnUiThread(new Runnable() {
                                            @Override
                                            public void run() {
                                                ClipboardManager cm = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                                                ClipData mClipData = ClipData.newPlainText("Label", "" );
                                                cm.setPrimaryClip(mClipData);
                                                Toast.makeText(LoginWebView.this,"登录成功，已自动获取Cookie",Toast.LENGTH_LONG).show();
                                            }
                                        });
                                    }
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
