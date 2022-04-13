package xyz.hyli.genshinhelper;

import android.util.Log;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import okhttp3.Call;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class MihoyoAPIs {
    public static final String UA = "Mozilla/5.0 (iPhone; CPU iPhone OS 15_4_1 like Mac OS X) AppleWebKit/605.1.15 (KHTML, like Gecko) miHoYoBBS/2.11.1";
    public static final String AppVersion = "2.11.1";

    // 通过login_ticket获取SToken
    public static String getMultiTokenByLoginTicket(String login_ticket, String accountId) {
        OkHttpClient okHttpClient = new OkHttpClient();
        Request request = new Request.Builder()
                .url("https://api-takumi.mihoyo.com/auth/api/getMultiTokenByLoginTicket?login_ticket=" + login_ticket
                        + "&token_types=3&uid=" + accountId)
                .header("User-Agent", UA)
                .build();
        Call call = okHttpClient.newCall(request);
        try {
            Response response = call.execute();
            JSONObject text = JSONObject.parseObject(Objects.requireNonNull(response.body()).string());
            JSONObject data = JSONObject.parseObject(text.getString("data"));
            List<JSONObject> list = JSON.parseArray(data.getJSONArray("list").toString(), JSONObject.class);
            String SToken = "";
            for (JSONObject tokens : list) {
                if (tokens.getString("name").contains("stoken")) {
                    SToken = tokens.getString("token");
                }
            }
            Log.i(Thread.currentThread().getStackTrace()[2].getMethodName(),text.toString());
            return SToken;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    // 通过SToken刷新cookie_token
    public static String getCookieAccountInfoBySToken(String SToken, String accountId) {
        OkHttpClient okHttpClient = new OkHttpClient();
        Request request = new Request.Builder()
                .url("https://api-takumi.mihoyo.com/auth/api/getCookieAccountInfoBySToken?stoken=" + SToken + "&uid="
                        + accountId)
                .header("User-Agent", UA)
                .build();
        Call call = okHttpClient.newCall(request);
        try {
            Response response = call.execute();
            JSONObject text = JSONObject.parseObject(Objects.requireNonNull(response.body()).string());
            JSONObject data = JSONObject.parseObject(text.getString("data"));
            String cookie_token = data.getString("cookie_token");
            Log.i(Thread.currentThread().getStackTrace()[2].getMethodName(),text.toString());
            return cookie_token;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    // 通过login_ticket获取cookie_token
    public static JSONObject getCookieAccountInfoByLoginTicket(String login_ticket) {
        OkHttpClient okHttpClient = new OkHttpClient();
        Request request = new Request.Builder()
                .url("https://webapi.account.mihoyo.com/Api/cookie_accountinfo_by_loginticket?login_ticket="
                        + login_ticket)
                .header("x-rpc-app_version", AppVersion)
                .header("User-Agent", UA)
                .header("x-rpc-client_type", "5")
                .build();
        Call call = okHttpClient.newCall(request);
        try {
            Response response = call.execute();
            JSONObject text = JSONObject.parseObject(Objects.requireNonNull(response.body()).string());
            JSONObject data = JSON.parseObject(text.getString("data"));
            JSONObject cookie_info = JSON.parseObject(data.getString("cookie_info"));
            // String account_id = cookie_info.getString("account_id");
            // String cookie_token = cookie_info.getString("cookie_token");
            Log.i(Thread.currentThread().getStackTrace()[2].getMethodName(),text.toString());
            return cookie_info;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    // 通过Cookie获取绑定角色信息
    public static List<JSONObject> getUserGameRolesByCookie(String Cookie) {
        OkHttpClient okHttpClient = new OkHttpClient();
        String q = "game_biz=hk4e_cn";
        String b = "";
        Request request = new Request.Builder()
                .url("https://api-takumi.mihoyo.com/binding/api/getUserGameRolesByCookie?" + q)
                .header("User-Agent", UA)
                .header("Cookie", Cookie)
                .header("x-rpc-device_id", API_tools.random_hex())
                .header("Origin", "https://webstatic.mihoyo.com/")
                .header("DS", API_tools.DSGet(q, b))
                .header("X_Requested_With", "com.mihoyo.hyperion")
                .header("x-rpc-app_version", AppVersion)
                .header("x-rpc-client_type", "5")
                .header("Referer",
                        "https://webstatic.mihoyo.com/bbs/event/signin-ys/index.html?bbs_auth_required=true&act_id=e202009291139501&utm_source=bbs&utm_medium=mys&utm_campaign=icon")
                .build();
        Call call = okHttpClient.newCall(request);
        try {
            Response response = call.execute();
            JSONObject text = JSONObject.parseObject(Objects.requireNonNull(response.body()).string());
            JSONObject data = JSONObject.parseObject(text.getString("data"));
            List<JSONObject> list = JSON.parseArray(data.getJSONArray("list").toJSONString(), JSONObject.class);
            Log.i(Thread.currentThread().getStackTrace()[2].getMethodName(),text.toString());
            return list;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    // 实时便笺
    public static JSONObject getDailyNote(String Uid, String ServerID, String Cookie) {
        OkHttpClient okHttpClient = new OkHttpClient();
        String q = "role_id=" + Uid + "&server=" + ServerID;
        String b = "";
        Request request = new Request.Builder()
                .url("https://api-takumi.mihoyo.com/game_record/app/genshin/api/dailyNote?" + q)
                .header("DS", API_tools.DSGet(q, b))
                .header("x-rpc-app_version", AppVersion)
                .header("User-Agent", UA)
                .header("x-rpc-client_type", "5")
                .header("Referer", "https://webstatic.mihoyo.com/")
                .header("Cookie", Cookie)
                .build();
        Call call = okHttpClient.newCall(request);
        try {
            Response response = call.execute();
            JSONObject text = JSONObject.parseObject(Objects.requireNonNull(response.body()).string());
            JSONObject data = JSONObject.parseObject(text.getString("data"));
            Log.i(Thread.currentThread().getStackTrace()[2].getMethodName(),text.toString());
            return data;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    // 签到 原神
    public static JSONObject GenshinSign(String Uid, String ServerID, String Cookie) {
        OkHttpClient okHttpClient = new OkHttpClient();
        String json = "{\"act_id\": \"e202009291139501\" ,\"uid\": \"" + Uid + "\" ,\"region\":\"" + ServerID + "\"}";
        RequestBody body = RequestBody.create(
                MediaType.parse("application/json"), json);
        Request request = new Request.Builder()
                .url("https://api-takumi.mihoyo.com/event/bbs_sign_reward/sign")
                .header("User-Agent",
                        "Mozilla/5.0 (iPhone; CPU iPhone OS 15_4_1 like Mac OS X) AppleWebKit/605.1.15 (KHTML, like Gecko) miHoYoBBS/2.3.0")
                .header("Cookie", Cookie)
                .header("x-rpc-device_id", API_tools.random_hex())
                .header("Origin", "https://webstatic.mihoyo.com/")
                .header("DS", API_tools.DSGet("", ""))
                .header("X_Requested_With", "com.mihoyo.hyperion")
                .header("x-rpc-app_version", "2.3.0")
                .header("x-rpc-client_type", "5")
                .header("Referer",
                        "https://webstatic.mihoyo.com/bbs/event/signin-ys/index.html?bbs_auth_required=true&act_id=e202009291139501&utm_source=bbs&utm_medium=mys&utm_campaign=icon")
                .post(body)
                .build();
        Call call = okHttpClient.newCall(request);
        try {
            Response response = call.execute();
            JSONObject text = JSONObject.parseObject(Objects.requireNonNull(response.body()).string());
            Log.i(Thread.currentThread().getStackTrace()[2].getMethodName(),text.toString());
            return text;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    // 获取签到奖励列表 原神
    public static List<JSONObject> getSignAward() {
        OkHttpClient okHttpClient = new OkHttpClient();
        Request request = new Request.Builder()
                .url("https://api-takumi.mihoyo.com/event/bbs_sign_reward/home?act_id=e202009291139501")
                .header("User-Agent", UA)
                .header("x-rpc-app_version", AppVersion)
                .header("x-rpc-client_type", "5")
                .header("Referer", "https://webstatic.mihoyo.com/")
                .build();
        Call call = okHttpClient.newCall(request);
        try {
            Response response = call.execute();
            JSONObject text = JSONObject.parseObject(Objects.requireNonNull(response.body()).string());
            JSONObject data = JSONObject.parseObject(text.getString("data"));
            List<JSONObject> awards = JSON.parseArray(data.getJSONArray("awards").toJSONString(), JSONObject.class);
            Log.i(Thread.currentThread().getStackTrace()[2].getMethodName(),text.toString());
            return awards;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    // 获取签到信息 原神
    public static JSONObject getSignInfo(String Uid, String ServerID, String Cookie) {
        OkHttpClient okHttpClient = new OkHttpClient();
        Request request = new Request.Builder()
                .url("https://api-takumi.mihoyo.com/event/bbs_sign_reward/info?act_id=e202009291139501&region="
                        + ServerID + "&uid=" + Uid)
                .header("User-Agent", UA)
                .header("Cookie", Cookie)
                .header("x-rpc-app_version", AppVersion)
                .header("x-rpc-client_type", "5")
                .header("Referer", "https://webstatic.mihoyo.com/")
                .build();
        Call call = okHttpClient.newCall(request);
        try {
            Response response = call.execute();
            JSONObject text = JSONObject.parseObject(Objects.requireNonNull(response.body()).string());
            JSONObject data = JSONObject.parseObject(text.getString("data"));
            Log.i(Thread.currentThread().getStackTrace()[2].getMethodName(),text.toString());
            return data;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    // 获取今日奖励
    public static JSONObject getTodayAward(String Uid, String ServerID, String Cookie, JSONObject SignInfo, List<JSONObject> AwardList) {
        JSONObject award = null;
        if (SignInfo.getBooleanValue("is_sign")) {
            award = AwardList.get(SignInfo.getIntValue("total_sign_day") - 1);
        } else {
            award = AwardList.get(SignInfo.getIntValue("total_sign_day"));
        }
        Log.i(Thread.currentThread().getStackTrace()[2].getMethodName(),award.toString());
        return award;
    }

    // 获取角色卡片信息 凭account_id
    // 需要Cookie!每个Cookie每日只能查30条信息
    public static JSONObject getGameRecordCard(String account_id, String Cookie) {
        OkHttpClient okHttpClient = new OkHttpClient();
        String q = "uid=" + account_id;
        String b = "";
        Request request = new Request.Builder()
                .url("https://api-takumi.mihoyo.com/game_record/card/wapi/getGameRecordCard?" + q)
                .header("DS", API_tools.DSGet(q,b))
                .header("User-Agent", UA)
                .header("Cookie", Cookie)
                .header("x-rpc-app_version", AppVersion)
                .header("x-rpc-client_type", "5")
                .header("Referer", "https://webstatic.mihoyo.com/")
                .build();
        Call call = okHttpClient.newCall(request);
        try {
            Response response = call.execute();
            JSONObject text = JSONObject.parseObject(Objects.requireNonNull(response.body()).string());
            JSONObject data = JSONObject.parseObject(text.getString("data"));
            List<JSONObject> list = JSON.parseArray(data.getJSONArray("list").toJSONString(), JSONObject.class);
            JSONObject gameRecordCard = null;
            for (JSONObject object : list) {
                if (object.getString("game_id").contains("2")) {
                    gameRecordCard = object;
                }
            }
            Log.i(Thread.currentThread().getStackTrace()[2].getMethodName(),text.toString());
            return gameRecordCard;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    // 获取角色卡片信息 凭本人Cookie
    public static JSONObject getGameRecordCard(String Uid, String ServerID, String Cookie) {
        OkHttpClient okHttpClient = new OkHttpClient();
        String q = "role_id=" + Uid + "&server=" + ServerID;
        String b = "";
        Request request = new Request.Builder()
                .url("https://api-takumi.mihoyo.com/game_record/app/genshin/api/index?" + q)
                .header("DS", API_tools.DSGet(q, b))
                .header("User-Agent", UA)
                .header("Cookie", Cookie)
                .header("x-rpc-app_version", AppVersion)
                .header("x-rpc-client_type", "5")
                .header("Referer", "https://webstatic.mihoyo.com/")
                .build();
        Call call = okHttpClient.newCall(request);
        try {
            Response response = call.execute();
            JSONObject text = JSONObject.parseObject(Objects.requireNonNull(response.body()).string());
            JSONObject data = JSONObject.parseObject(text.getString("data"));
            Log.i(Thread.currentThread().getStackTrace()[2].getMethodName(),text.toString());
            return data;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    //获取人物信息
    public static JSONObject getCharacterInfo(String Uid, String ServerID, String Cookie) {
        OkHttpClient okHttpClient = new OkHttpClient();
        String q = "";
        // 根据其它项目,此api需要传入character_ids并返回对应人物信息
        // 然而根据测试,无论是否传入,传入什么内容,都会返回所有人物的信息
        String b = "{\"character_ids\":"+new ArrayList<>()+",\"role_id\":\""+Uid+"\",\"server\":\""+ServerID+"\"}";
        RequestBody body = RequestBody.create(
                MediaType.parse("application/json"), b);
        Request request = new Request.Builder()
                .url("https://api-takumi.mihoyo.com/game_record/app/genshin/api/character")
                .header("DS", API_tools.DSGet(q, b))
                .header("User-Agent", UA)
                .header("Cookie", Cookie)
                .header("x-rpc-app_version", AppVersion)
                .header("x-rpc-client_type", "5")
                .header("Referer", "https://webstatic.mihoyo.com/")
                .post(body)
                .build();
        Call call = okHttpClient.newCall(request);
        try {
            Response response = call.execute();
            JSONObject text = JSONObject.parseObject(Objects.requireNonNull(response.body()).string());
            JSONObject data = JSONObject.parseObject(text.getString("data"));
            List<JSONObject> avatars = JSON.parseArray(data.getJSONArray("avatars").toJSONString(), JSONObject.class);
            JSONObject avatars1 = new JSONObject();
            for (JSONObject object: avatars) {
                avatars1.put(object.getString("id"),object);
            }
            Log.i(Thread.currentThread().getStackTrace()[2].getMethodName(),avatars1.toString());
            return avatars1;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    // 获取人物技能,武器,圣遗物等级
    public static JSONObject getCharacterDetail(String Uid, String ServerID, String Cookie, String avatar_id) {
        OkHttpClient okHttpClient = new OkHttpClient();
        String q = "uid=" + Uid + "&region=" + ServerID + "&avatar_id=" + avatar_id;
        String b = "";
        Request request = new Request.Builder()
                .url("https://api-takumi.mihoyo.com/event/e20200928calculate/v1/sync/avatar/detail?" + q)
                .header("DS", API_tools.DSGet(q, b))
                .header("User-Agent", UA)
                .header("Cookie", Cookie)
                .header("x-rpc-app_version", AppVersion)
                .header("x-rpc-client_type", "5")
                .header("Referer", "https://webstatic.mihoyo.com/")
                .build();
        Call call = okHttpClient.newCall(request);
        try {
            Response response = call.execute();
            JSONObject text = JSONObject.parseObject(Objects.requireNonNull(response.body()).string());
            JSONObject data = JSONObject.parseObject(text.getString("data"));
            Log.i(Thread.currentThread().getStackTrace()[2].getMethodName(),text.toString());
            return data;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    // 获取深渊信息
    public static JSONObject getSpiralAbyssInfo(String Uid, String ServerID, String Cookie, String schedule_type) {
        // schedule_type 深渊代码 1为本期 2为上期
        OkHttpClient okHttpClient = new OkHttpClient();
        String q = "role_id=" + Uid + "&schedule_type=" + schedule_type + "&server=" + ServerID;
        String b = "";
        Request request = new Request.Builder()
                .url("https://api-takumi.mihoyo.com/game_record/app/genshin/api/spiralAbyss?" + q)
                .header("DS", API_tools.DSGet(q, b))
                .header("User-Agent", UA)
                .header("Cookie", Cookie)
                .header("x-rpc-app_version", AppVersion)
                .header("x-rpc-client_type", "5")
                .header("Referer", "https://webstatic.mihoyo.com/")
                .build();
        Call call = okHttpClient.newCall(request);
        try {
            Response response = call.execute();
            JSONObject text = JSONObject.parseObject(Objects.requireNonNull(response.body()).string());
            JSONObject data = JSONObject.parseObject(text.getString("data"));
            Log.i(Thread.currentThread().getStackTrace()[2].getMethodName(),text.toString());
            return data;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    // 旅行者札记 概览
    public static JSONObject getMonthInfo(String Uid, String ServerID, String Cookie, String month) {
        // 传入字段 说明 可能的值
        // month 月份,仅支持查询近三个月 本月/上月/上上月
        // bind_uid 游戏UID 12345678
        // bind_region 所在服 cn_gf01或cn_qd01
        try {
            OkHttpClient okHttpClient = new OkHttpClient();
            String q = "month=" + month + "&bind_uid" + Uid + "&bind_region=" + ServerID;
            String b = "";
            Request request = new Request.Builder()
                    .url("https://hk4e-api.mihoyo.com/event/ys_ledger/monthInfo?" + q)
                    .header("DS", API_tools.DSGet(q, b))
                    .header("User-Agent", UA)
                    .header("Cookie", Cookie)
                    .header("x-rpc-app_version", AppVersion)
                    .header("x-rpc-client_type", "5")
                    .header("Referer", "https://webstatic.mihoyo.com/")
                    .build();
            Call call = okHttpClient.newCall(request);
            Response response = call.execute();
            JSONObject text = JSONObject.parseObject(Objects.requireNonNull(response.body()).string());
            JSONObject data = JSONObject.parseObject(text.getString("data"));
            Log.i(Thread.currentThread().getStackTrace()[2].getMethodName(),text.toString());
            return data;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }
    // 旅行者札记 明细
    public static List<JSONObject> getMonthDetail(String Uid, String ServerID, String Cookie, String month, String type) {
        // 传入字段 说明 可能的值
        // page 页数 1
        // month 月份,仅支持查询近三个月 本月/上月/上上月
        // limit 一次的返回信息数量 20(max=100)
        // type 原石/摩拉查询 1为原石,2为摩拉
        // bind_uid 游戏UID 12345678
        // bind_region 所在服 cn_gf01或cn_qd01
        // bbs_presentation_style 未知 fullscreen
        // bbs_auth_required 未知(可能能省略) true
        // utm_source 未知(可能能省略) bbs
        // utm_medium 未知(可能能省略) mys
        // utm_campaign 未知(可能能省略) icon
        try {
            List<JSONObject> list = new ArrayList<>();
            int page = 1;
            while (true) {
                OkHttpClient okHttpClient = new OkHttpClient();
                String q = "page=" + page + "&month=" + month + "&limit=20&type=" + type + "&bind_uid" + Uid
                        + "&bind_region=" + ServerID
                        + "&bbs_presentation_style=fullscreen&bbs_auth_required=true&utm_source=bbs&utm_medium=mys&utm_campaign=icon";
                String b = "";
                Request request = new Request.Builder()

                        .url("https://hk4e-api.mihoyo.com/event/ys_ledger/monthDetail?" + q)
                        .header("DS", API_tools.DSGet(q, b))
                        .header("User-Agent", UA)
                        .header("Cookie", Cookie)
                        .header("x-rpc-app_version", AppVersion)
                        .header("x-rpc-client_type", "5")
                        .header("Referer", "https://webstatic.mihoyo.com/")
                        .build();
                Call call = okHttpClient.newCall(request);
                Response response = call.execute();
                JSONObject text = JSONObject.parseObject(Objects.requireNonNull(response.body()).string());
                JSONObject data = JSONObject.parseObject(text.getString("data"));
                List<JSONObject> list1 = JSON.parseArray(data.getJSONArray("list").toJSONString(), JSONObject.class);
                if (list1.size()==0 || list1.size()<20) {
                    break;
                } else {
                    list.addAll(list1);
                    page += 1;
                }
            }
            Log.i(Thread.currentThread().getStackTrace()[2].getMethodName(),list.toString());
            return list;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }
}
