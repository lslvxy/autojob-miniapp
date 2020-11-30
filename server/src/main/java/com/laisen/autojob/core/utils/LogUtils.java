
package com.laisen.autojob.core.utils;

import org.slf4j.Logger;

/**
 * @author lise
 * @version LogUtil.java, v 0.1 2020年11月30日 18:31 lise
 */
public class LogUtils {
    public static void info(Logger log, String userId, String module, String operate, String detail, Object... params) {
        log.info("[{}]-[{}]-{}", module, operate, detail, params);
    }

    public static void error(Logger log, String userId, String module, String operate, String detail, Object... params) {
        log.info("[{}]-[{}]-{}", module, operate, detail, params);
    }
}