package com.laisen.autojob.core.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.laisen.autojob.core.entity.User;
import com.laisen.autojob.core.repository.UserRepository;
import io.vavr.control.Try;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;
import java.util.Objects;

@RestController
@RequestMapping("user")
public class UserController {

    @Autowired
    UserRepository userRepository;

    @PostMapping("login")
    public ModelAndView login(String account, String password, ModelAndView modelAndView, HttpServletRequest request) {
        User user = userRepository.findByAccount(account);
        if (Objects.isNull(user)) {
            user = new User();
            user.setAccount(account);
            user.setPassword(password);
            user = userRepository.save(user);
        }
        if (!user.getPassword().equals(password)) {
            return new ModelAndView("errors");
        }
        request.getSession().setAttribute("userId", user.getId());

        return new ModelAndView("main");
    }

    @GetMapping("getopenid")
    public String getOpenId(String code) {
        String secret = System.getenv("wx127525214d4abbe0_secret");
        String url = "https://api.weixin.qq.com/sns/jscode2session?appid=wx127525214d4abbe0&secret=" + secret + "&js_code=" + code
                + "&grant_type=authorization_code";
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder()
                .url(url)
                .build();
        return Try.of(() -> {
            Response response = client.newCall(request).execute();
            String responseText = response.body().string();
            ObjectMapper mapper = new ObjectMapper();
            Map map = mapper.readValue(responseText, Map.class);
            String openid = map.get("openid").toString();

            return openid;
        }).getOrElse("");
    }

    @RequestMapping("update")
    public void update(String openId, String nickName, String avatarUrl) {
        User user = userRepository.findByAccount(openId);
        if (Objects.isNull(user)) {
            user = new User();
            user.setAccount(openId);
            user.setAvatarUrl(avatarUrl);
        }
        user.setNickName(nickName);
        userRepository.save(user);
    }
}
