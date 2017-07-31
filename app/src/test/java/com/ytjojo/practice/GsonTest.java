package com.ytjojo.practice;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import org.junit.Test;

import java.util.Date;

/**
 * Created by Administrator on 2017/7/29 0029.
 */

public class GsonTest {

    @Test
    public void jsonTest(){
        String json ="{\"name\":2222,\"age\":\"11111.00\",\"birthday\":\"2017-12-12\",\"day\":\"Mon, 03 Jun 2013 07:01:29 GMT\"}";
        System.out.println(json);
        GsonBuilder builder = new GsonBuilder();
        builder.enableComplexMapKeySerialization()
                .serializeNulls();
        Gson gson =builder.create();
        People people = gson.fromJson(json, People.class);
        System.out.println(people.age+people.name+people.date+people.birthday+people.day);


    }

    public static class People{
        public String name;
        public int age;
        public Date birthday;
        public Date day;
        public Date date;
    }
}
