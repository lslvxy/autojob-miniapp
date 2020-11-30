package com.laisen.autojob.core.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class ViewController {

    @RequestMapping(value = "/ok")
    public String index() {
        return "ok";
    }

    //@RequestMapping(value = "/main")
    //public String main() {
    //    return "main";
    //}
    //
    //@RequestMapping(value = "/errors")
    //public String error() {
    //    return "error";
    //}
    //
    //@RequestMapping(value = "/logspage")
    //public String logspage() {
    //    return "logspage";
    //}


}
