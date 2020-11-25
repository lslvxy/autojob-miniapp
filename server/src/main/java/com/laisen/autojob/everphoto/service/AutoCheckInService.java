package com.laisen.autojob.everphoto.service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.laisen.autojob.core.entity.EventLog;
import com.laisen.autojob.core.repository.EventLogRepository;
import com.laisen.autojob.everphoto.Result;
import com.laisen.autojob.everphoto.entity.EverPhotoAccount;
import com.laisen.autojob.everphoto.repository.EverPhotoAccountRepository;
import lombok.extern.slf4j.Slf4j;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Objects;
import java.util.zip.GZIPInputStream;

@Service
@Slf4j
public class AutoCheckInService {
    @Autowired
    EverPhotoAccountRepository everPhotoAccountRepository;
    @Autowired
    EventLogRepository         eventLogRepository;

    static String url      = "https://api.everphoto.cn/users/self/checkin/v2";
    static String urllogin = "https://web.everphoto.cn/api/auth";

    public Result autoCheckin(String id) {
        try {
            final EverPhotoAccount everPhotoAccount = everPhotoAccountRepository.findByUserId(id);
            if (Objects.isNull(everPhotoAccount)) {
                throw new RuntimeException("用户未配置");
            }
            String token = "";
            String login = login(everPhotoAccount.getAccount(), everPhotoAccount.getPassword());
            final JSONObject loginResult = JSON.parseObject(login);
            if (!loginResult.getInteger("code").equals(0) || !loginResult.containsKey("data")) {
                throw new RuntimeException("登录失败");
            }
            final JSONObject loginData = loginResult.getJSONObject("data");
            if (loginData.containsKey("token")) {
                token = loginData.getString("token");
                log.info("登录成功,token={}", token);
            }
            if (StringUtils.isEmpty(token)) {
                throw new RuntimeException("获取token失败");
            }
            final JSONObject checkinResponse = JSON.parseObject(checkin(token));
            if (!checkinResponse.getInteger("code").equals(0) || !checkinResponse.containsKey("data")) {
                throw new RuntimeException("签到失败");
            }
            final JSONObject checkinData = checkinResponse.getJSONObject("data");
            Result result = checkinData.toJavaObject(Result.class);
            EventLog l = new EventLog();
            l.setUserId(id);
            l.setDetail(JSON.toJSONString(result));
            eventLogRepository.save(l);
            return result;
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }

    private String checkin(String token) throws IOException {
        OkHttpClient client = new OkHttpClient();
        RequestBody body = new FormBody.Builder().build();
        Request request = new Request.Builder()
                .url(url)
                .post(body)
                .header("User-Agent", "EverPhoto/2.7.0 (Android;2702;ONEPLUS A6000;28;oppo")
                .header("x-device-mac", "02:00:00:00:00:00")
                .header("application", "tc.everphoto")
                .header("x-locked", "1")
                .header("content-length", "0")
                .header("authorization", "Bearer " + token)
                .build();
        try (Response response = client.newCall(request).execute()) {
            return (response.body().string());
        }
    }

    private String login(String account, String password) throws IOException {
        //        String md5 = DigestUtils.md5DigestAsHex(base.getBytes());

        OkHttpClient client = new OkHttpClient();
        RequestBody body = new FormBody.Builder().add("mobile", account).add("password", password).build();
        Request request = new Request.Builder()
                .url(urllogin)
                .post(body)
                .header("User-Agent", "EverPhoto/2.7.0 (Android;2702;ONEPLUS A6000;28;oppo")
                .header("x-device-mac", "02:00:00:00:00:00")
                .header("application", "tc.everphoto")
                .header("authorization", "Bearer 94P6RfZFfqvVQ2hH4jULaYGI")
                .header("x-locked", "1")
                .header("content-length", "0")
                .header("accept-encoding", "gzip")
                .build();
        try (Response response = client.newCall(request).execute()) {
            return uncompress(response.body().bytes());
        }
    }

    public static String uncompress(byte[] str) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try (GZIPInputStream gis = new GZIPInputStream(new ByteArrayInputStream(str))) {
            int b;
            while ((b = gis.read()) != -1) {
                baos.write((byte) b);
            }
        } catch (Exception e) {
            return "";
        }
        return new String(baos.toByteArray(), StandardCharsets.UTF_8);
    }
}
