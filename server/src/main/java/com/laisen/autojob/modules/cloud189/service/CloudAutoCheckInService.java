
package com.laisen.autojob.modules.cloud189.service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.laisen.autojob.core.constants.Constants;
import com.laisen.autojob.core.entity.EventLog;
import com.laisen.autojob.core.repository.EventLogRepository;
import com.laisen.autojob.core.service.MessageService;
import com.laisen.autojob.core.utils.LogUtils;
import com.laisen.autojob.modules.cloud189.entity.CloudAccount;
import com.laisen.autojob.modules.cloud189.repository.CloudAccountRepository;
import com.laisen.autojob.modules.cloud189.util.AESUtil;
import io.vavr.control.Try;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;
import org.apache.commons.codec.binary.Base64;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.crypto.Cipher;
import java.security.KeyFactory;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.X509EncodedKeySpec;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author lise
 * @version CloudAutoCheckInService.java, v 0.1 2020年11月27日 13:43 lise
 */
@Service
@Slf4j
public class CloudAutoCheckInService {
    private static String loginPageUrl = "https://cloud.189.cn/udb/udb_login.jsp?pageId=1&redirectURL=/main.action";
    private static Pattern returnUrlPattern = Pattern.compile("returnUrl = '(.*)'");
    private static Pattern paramIdPattern = Pattern.compile("paramId = \"(.*)\"");
    private static Pattern ltPattern = Pattern.compile("lt = \"(.*)\"");
    private String returnUrl = "";
    private String paramId = "";
    private String lt = "";
    private static String loginUrl = "https://open.e.189.cn/api/logbox/oauth2/loginSubmit.do";
    private final List<Cookie> cookieStore = new ArrayList<>();

    String url = "https://m.cloud.189.cn/v2/drawPrizeMarketDetails.action?taskId=TASK_SIGNIN&activityId=ACT_SIGNIN";
    String url2 = "https://m.cloud.189.cn/v2/drawPrizeMarketDetails.action?taskId=TASK_SIGNIN_PHOTOS&activityId=ACT_SIGNIN";
    private OkHttpClient client;
    private List<String> headers = new ArrayList<>();
    @Autowired
    private CloudAccountRepository cloudAccountRepository;
    @Autowired
    private EventLogRepository eventLogRepository;
    @Autowired
    private MessageService messageService;

    public void autoCheckin(String userId) throws Exception {
        client = new OkHttpClient.Builder().cookieJar(new CookieJar() {
            @Override
            public void saveFromResponse(HttpUrl httpUrl, List<Cookie> list) {
                cookieStore.addAll(list);
            }

            @Override
            public List<Cookie> loadForRequest(HttpUrl httpUrl) {
                return cookieStore;
            }
        }).build();
        EventLog eventLog = new EventLog();
        eventLog.setUserId(userId);
        eventLog.setType(Constants.LOG_TYPE_CLOUD189);
        CloudAccount account = cloudAccountRepository.findByUserId(userId);
        if (Objects.isNull(account)) {
            throw new RuntimeException("用户未配置");
        }
        String loginResult = login(account.getAccount(), AESUtil.decrypt(account.getPassword()));
        LogUtils.info(log, userId, Constants.LOG_MODULES_CLOUD189, Constants.LOG_OPERATE_LOGIN, loginResult);

        String checkInResult = checkIn();
        LogUtils.info(log, userId, Constants.LOG_MODULES_CLOUD189, Constants.LOG_OPERATE_CHECKIN, checkInResult);

        String lottery = lottery(url);
        LogUtils.info(log, userId, Constants.LOG_MODULES_CLOUD189, Constants.LOG_OPERATE_LOTTERY, lottery);

        String lottery1 = lottery(url2);
        LogUtils.info(log, userId, Constants.LOG_MODULES_CLOUD189, Constants.LOG_OPERATE_LOTTERY, lottery1);

        String detail = checkInResult + ";" + lottery + ";" + lottery1;
        eventLog.setDetail(detail);
        eventLogRepository.save(eventLog);
        //log.info("天翼网盘签到:{}", detail);
        LogUtils.info(log, userId, Constants.LOG_MODULES_CLOUD189, Constants.LOG_OPERATE_LOTTERY, detail);

        messageService.sendMessage(userId, "天翼网盘签到", detail);

    }

    private String login(String username, String password) throws Exception {
        return Try.of(() -> {

            Document doc = Jsoup.connect(loginPageUrl).get();
            Element captchaToken = doc.body().selectFirst("input[name='captchaToken']");
            Element rsaKey = doc.getElementById("j_rsaKey");
            log.debug("captchaToken is:{}", captchaToken.val());
            log.debug("rsaKey is:{}", rsaKey.val());
            Elements scriptList = doc.getElementsByTag("script");
            scriptList.forEach(e -> {
                String html = e.html();
                Matcher matcher = returnUrlPattern.matcher(html);
                Matcher matcher2 = paramIdPattern.matcher(html);
                Matcher matcher3 = ltPattern.matcher(html);
                if (matcher.find()) {
                    returnUrl = matcher.group(1);
                    log.debug("returnUrl is:{}", returnUrl);
                }
                if (matcher2.find()) {
                    paramId = matcher2.group(1);
                    log.debug("paramId is:{}", paramId);
                }
                if (matcher3.find()) {
                    lt = matcher3.group(1);
                    log.debug("lt is:{}", lt);
                }
            });
            String encry_username = encrypt(username, rsaKey.val());
            String encry_password = encrypt(password, rsaKey.val());

            RequestBody body = new FormBody.Builder()
                    .add("appKey", "cloud")
                    .add("accountType", "01")
                    .add("userName", "{RSA}" + encry_username + "")
                    .add("password", "{RSA}" + encry_password + "")
                    .add("validateCode", "")
                    .add("captchaToken", captchaToken.val())
                    .add("returnUrl", returnUrl)
                    .add("mailSuffix", "@189.cn")
                    .add("paramId", paramId)
                    .build();
            Request request = new Request.Builder()
                    .url(loginUrl)
                    .post(body)
                    .header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:74.0) Gecko/20100101 Firefox/76.0")
                    .header("Referer", "https://open.e.189.cn/")
                    .header("lt", lt)
                    .build();
            try (Response response = client.newCall(request).execute()) {
                String s = response.body().string();
                //{
                //	"result":0,
                //	"msg":"登录成功",
                //	"toUrl":"https://cloud.189.cn/callbackUnify.action?redirectURL=/main
                //	.action&isShowTree=null&fileId=null&verifyCode=null&accessCode=null&marketingId=null&inviteUserId=null&pageId=1&appKey
                //	=null&display=null&responseType=null&appSignature=null&state=null&nonce=edb74a82-764e-4504-b27e-d959aa25102b&timestamp
                //	=null&callbackUrl=null&appId=cloud&paras
                //	=557EFC734706182C09DF54FB2A8374CED412E0F0C684C6E464851E0668F737FB06728F03162CA375CB95EA1BC5E02F6586CAA5CDD791C9F3ED55D0AA2ECB0EBAD10419339926324265695CA86B5D14B92BB9E625AB9433E1ECC7EEB89701B3DAB8AE3EAF8BE5973DFE899AAE7AAA077868E3451C9DD11BCFB180B83961C8558EE183A57B022E0B256C1C3AD086CF9C2EAC20739FFC687FAF407237A4F67D88EF52B16A30294CB2028C0613644945B4CDEE5AF070E13519A084E92D9DCD26657124BD04CB663A9F009F0F51E8A157A913817049C6B5E6F2FEB0241BCC0B65A9B2CAE427B9F3F1AB3B121F8E88DE3B1D68195D2FF5DC97CFE347D99EB29B22D616C0A40975E0EB7E0A43524F0BFDE2D2439CE2D646DAA9B690FA9B4643BA98D3ACEF0717AEE283F8434A26AF686772A8AEC16E6F2E6080F3D4D0C6480B69B81CE8268B0AE95ED716E39D7C7321A7611A7DDCDF7166280C2B69B6EC1AEE58FF44AB07767433ACA008AABE35C64842688ADE00B3F533FD9636889C6C56FD70DB12022A6F7582118AA57A40CE4E8570CDA9EC8E94A9AE46B20F60D299142E6A273F650562FF296EC9BB4B3F145EDE04BBC24F928674F4D1CD088A8759C751174B4BF027B2496437A6F2FDBC770FABA373E9A017AFB5ACBDBEB5870EF150F5&sign=B3D50C0BC3E047321C64D93AE9A563823FDCDE9F"
                //}
                JSONObject jsonObject = JSON.parseObject(s);
                if (jsonObject.containsKey("result") && jsonObject.getInteger("result").equals(0)) {
                    String toUrl = jsonObject.getString("toUrl");
                    Request request2 = new Request.Builder()
                            .url(toUrl).build();
                    Response response2 = client.newCall(request2).execute();
//                headers.addAll(response2.headers("set-cookie"));
//                String string = response2.body().string();
                    return jsonObject.getString("msg");
                } else {
                    throw new RuntimeException("登录失败");
                }
            }
        }).getOrElse("登录失败");

    }

    public static String encrypt(String str, String publicKey) throws Exception {
        //base64编码的公钥
        byte[] decoded = Base64.decodeBase64(publicKey);
        RSAPublicKey pubKey = (RSAPublicKey) KeyFactory.getInstance("RSA").generatePublic(new X509EncodedKeySpec(decoded));
        //RSA加密
        Cipher cipher = Cipher.getInstance("RSA");
        cipher.init(Cipher.ENCRYPT_MODE, pubKey);
        String outStr = base642hex(Base64.encodeBase64String(cipher.doFinal(str.getBytes("UTF-8"))));
        return outStr;
    }

    static String base642hex(String str) {
        String b64map = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/";

        String d = "";
        int e = 0, c = 0;
        for (int i = 0; i < str.length(); i++) {
            if (str.charAt(i) != '=') {
                int v = b64map.indexOf(str.charAt(i));
                if (e == 0) {
                    e = 1;
                    d += int2char(v >> 2);
                    c = 3 & v;
                } else if (e == 1) {
                    e = 2;
                    d += int2char(c << 2 | v >> 4);
                    c = 15 & v;
                } else if (e == 2) {
                    e = 3;
                    d += int2char(c);
                    d += int2char(v >> 2);
                    c = 3 & v;
                } else {
                    e = 0;
                    d += int2char(c << 2 | v >> 4);
                    d += int2char(15 & v);
                }
            }
        }
        if (e == 1) {
            d += int2char(c << 2);
        }
        return d;
    }

    private static String int2char(int i) {
        return String.valueOf("0123456789abcdefghijklmnopqrstuvwxyz".charAt(i));
    }

    private String checkIn() {
        return Try.of(() -> {
            String result = "";
            String surl = "https://api.cloud.189.cn/mkt/userSign.action?rand=" + System.currentTimeMillis()
                    + "&clientType=TELEANDROID&version=8.6.3&model=SM-G930K";

            Request request = new Request.Builder()
                    .url(surl)
                    .header("User-Agent",
                            "Mozilla/5.0 (Linux; Android 5.1.1; SM-G930K Build/NRD90M; wv) AppleWebKit/537.36 (KHTML, like Gecko) Version/4.0"
                                    + " Chrome/74.0.3729.136 Mobile Safari/537.36 Ecloud/8.6.3 Android/22 clientId/355325117317828 "
                                    + "clientModel/SM-G930K imsi/460071114317824 clientChannelId/qq proVersion/1.0.6")
                    .header("Referer", "https://m.cloud.189.cn/zhuanti/2016/sign/index.jsp?albumBackupOpened=1")
                    .header("Host", "m.cloud.189.cn")
                    .header("Accept-Encoding", "gzip, deflate")
                    .build();
            Response response2 = client.newCall(request).execute();
            String signInResult = response2.body().string();
            JSONObject jsonObject = JSON.parseObject(signInResult);
            if (jsonObject.getString("isSign").equals("false")) {
                result = "签到获得" + jsonObject.getString("netdiskBonus") + "MB";
                log.info(result);
            } else {
                result = "已签到，获得" + jsonObject.getString("netdiskBonus") + "MB";
                log.info(result);
            }
            return result;
        }).getOrElse("签到失败");
    }

    private String lottery(String url) {
        return Try.of(() -> {
            String result = "";
            Request request = new Request.Builder()
                    .url(url)
                    .header("User-Agent",
                            "Mozilla/5.0 (Linux; Android 5.1.1; SM-G930K Build/NRD90M; wv) AppleWebKit/537.36 (KHTML, like Gecko) Version/4.0"
                                    + " Chrome/74.0.3729.136 Mobile Safari/537.36 Ecloud/8.6.3 Android/22 clientId/355325117317828 "
                                    + "clientModel/SM-G930K imsi/460071114317824 clientChannelId/qq proVersion/1.0.6")
                    .header("Referer", "https://m.cloud.189.cn/zhuanti/2016/sign/index.jsp?albumBackupOpened=1")
                    .header("Host", "m.cloud.189.cn")
                    .header("Accept-Encoding", "gzip, deflate")
                    .build();

            Response response = client.newCall(request).execute();
            String responseText = response.body().string();
            JSONObject jsonObject = JSON.parseObject(responseText);
            if (jsonObject.containsKey("errorCode")) {
                if (jsonObject.getString("errorCode").equals("User_Not_Chance")) {
                    result = "抽奖次数不足";
                } else {
                    result = "抽奖出错";
                }
            } else if (jsonObject.containsKey("description")) {
                result = "获得" + jsonObject.getString("description") + "MB";
            }
            return result;
        }).getOrElse("抽奖失败");

    }
}