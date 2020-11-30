
package com.laisen.autojob.core.service;

import com.alibaba.fastjson.JSON;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.laisen.autojob.core.service.dto.DataDetail;
import com.laisen.autojob.core.service.dto.Message;
import com.laisen.autojob.core.service.dto.ValueDetail;
import io.vavr.control.Try;
import lombok.extern.slf4j.Slf4j;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;

/**
 * @author lise
 * @version MessageService.java, v 0.1 2020年11月26日 14:11 lise
 */

@Service
@Slf4j
public class MessageService {
    public String getToken() {
        String secret = System.getenv("wx127525214d4abbe0_secret");

        String url = "https://api.weixin.qq.com/cgi-bin/token?grant_type=client_credential&appid=wx127525214d4abbe0&secret="
                + secret;
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder()
                .url(url)
                .build();
        return Try.of(() -> {
            Response response = client.newCall(request).execute();
            String responseText = response.body().string();
            ObjectMapper mapper = new ObjectMapper();
            Map map = mapper.readValue(responseText, Map.class);
            String openid = map.get("access_token").toString();

            return openid;
        }).getOrElse("");
    }

    public void sendMessage(String userId, String type, String detail) {
        Message message = new Message();
        message.setTemplate_id("j5OIz1iUpiBpx_80xtO0fmmc92gL0MFqU81GH2mTe_Y");
        message.setTouser(userId);
        DataDetail da = new DataDetail();
        da.setThing1(new ValueDetail(type));
        da.setDate2(new ValueDetail(LocalDateTime.now().format(DateTimeFormatter.ISO_DATE_TIME)));
        da.setThing3(new ValueDetail(detail));
        da.setThing4(new ValueDetail("签到结果"));
        message.setData(da);

        String token = getToken();
        //String url="http://localhost:8080/logs/send";
        String url = "https://api.weixin.qq.com/cgi-bin/message/custom/send?access_token=" + token;
        OkHttpClient client = new OkHttpClient();
        RequestBody body = RequestBody.create(MediaType.parse("application/json; charset=utf-8"), JSON.toJSONString(message));
        Request request = new Request.Builder()
                .url(url)
                .post(body)
                .build();
        Try.of(() -> client.newCall(request).execute());
    }
}