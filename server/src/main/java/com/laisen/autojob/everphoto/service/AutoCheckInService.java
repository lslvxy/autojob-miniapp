package com.laisen.autojob.everphoto.service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.laisen.autojob.core.constants.Constants;
import com.laisen.autojob.core.entity.EventLog;
import com.laisen.autojob.core.repository.EventLogRepository;
import com.laisen.autojob.core.service.MessageService;
import com.laisen.autojob.core.service.dto.DataDetail;
import com.laisen.autojob.core.service.dto.Message;
import com.laisen.autojob.core.service.dto.ValueDetail;
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
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.zip.GZIPInputStream;

@Service
@Slf4j
public class AutoCheckInService {
    @Autowired
    EverPhotoAccountRepository everPhotoAccountRepository;
    @Autowired
    EventLogRepository         eventLogRepository;
    @Autowired
    private MessageService messageService;

    static String url      = "https://api.everphoto.cn/users/self/checkin/v2";
    static String urllogin = "https://web.everphoto.cn/api/auth";

    public Result autoCheckin(String id) throws Exception {

        EventLog l = new EventLog();
        final EverPhotoAccount everPhotoAccount = everPhotoAccountRepository.findByUserId(id);
        if (Objects.isNull(everPhotoAccount)) {
            throw new RuntimeException("用户未配置");
        }
        String token = "";
        String login = login(everPhotoAccount.getAccount(), everPhotoAccount.getPassword());
        final JSONObject loginResult = JSON.parseObject(login);
        if (Objects.isNull(loginResult) || !loginResult.getInteger("code").equals(0) || !loginResult.containsKey("data")) {
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
        l.setUserId(id);
        List<String> detail = new LinkedList<>();
        detail.add("签到结果:" + (result.getCheckin_result().equals("true") ? "成功" : "失败"));
        detail.add("累计签到:" + (result.getContinuity()) + "天");
        detail.add("总容量:" + (result.getTotal_reward() / 1024 / 1024) + "MB");
        detail.add("明日可得:" + (result.getTomorrow_reward() / 1024 / 1024) + "MB");
        String detail1 = detail.stream().collect(Collectors.joining("；"));
        l.setDetail(detail1);
        l.setType(Constants.LOG_EVERPHOTO);
        eventLogRepository.save(l);

        Message message = new Message();
        message.setTemplate_id("j5OIz1iUpiBpx_80xtO0fmmc92gL0MFqU81GH2mTe_Y");
        message.setTouser(id);
        DataDetail da = new DataDetail();
        da.setThing1(new ValueDetail("时光相册签到"));
        da.setDate2(new ValueDetail(LocalDateTime.now().format(DateTimeFormatter.ISO_DATE_TIME)));
        da.setThing3(new ValueDetail(detail1));
        da.setThing3(new ValueDetail("签到结果"));
        message.setData(da);
        messageService.sendMessage(JSON.toJSONString(message));
        return result;

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
