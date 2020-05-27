package com.wangzhen.util;

import lombok.extern.slf4j.Slf4j;

import javax.servlet.http.HttpSession;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * @Author wangzhen
 * @Description
 * @CreateDate 2020/1/8
 */
@Slf4j
public class Utils {
    public static String randomUuid(){
        return UUID.randomUUID().toString().replace("-","");
    }
    public static String randomString(String s){
        return UUID.randomUUID().toString().replace("-","")+s;
    }
    public static <T> T getUser(HttpSession session, Class<T> cla){
        return (T)session.getAttribute(cla.getSimpleName().toUpperCase());
    }

    public static <T> List<T> getArray(T...ts){
        List<T> list = new ArrayList<T>();
        for (T t : ts) {
            list.add(t);
        }
        return list;
    }
}
