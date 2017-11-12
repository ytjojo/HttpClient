package com.ytjojo.practice;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.ytjojo.http.coverter.DateTypeAdapter;
import com.ytjojo.http.coverter.GMTDateUtils;

import org.junit.Test;

import java.util.Date;
import java.util.TimeZone;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import okio.ByteString;

/**
 * Created by Administrator on 2017/7/29 0029.
 */

public class GsonTest {

    @Test
    public void jsonTest(){
        String json ="{\"name\":2222,\"age\":\"11111.00\",\"birthday\":\"2017-12-12\",\"day\":\"2013-06-17 07:01:29\"}";
        System.out.println(json);
        GsonBuilder builder = new GsonBuilder();
        builder.enableComplexMapKeySerialization()
                .serializeNulls();
        Gson gson =builder.create();
        gson = new Gson();
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
    @Test
    public void testKey(){
      final CharSequence delimiter = "\", \"";
      System.out.println(delimiter);
       String abc = key("abc");
       String abc123cd = key("abc","123","cd");
       String abc123 = key("abc","123",null);
        //System.out.println(  ByteString.decodeHex(abc123cd).utf8());
        System.out.println( abc.length());
        System.out.println( abc123.length());
        System.out.println( abc123cd.length());
        System.out.println(abc);
        System.out.println(abc123cd);
        System.out.println(abc123);
    }
    private static final String PREFIX_DYNAMIC_KEY = "$d$d$d$";
    private static final String PREFIX_DYNAMIC_KEY_GROUP = "$g$g$g$";
    public static String key(String url,String dynamicKey,String dynamicKeyGroup){
        return key(url+PREFIX_DYNAMIC_KEY+dynamicKey+PREFIX_DYNAMIC_KEY_GROUP+dynamicKeyGroup);
    }
    public static String key(String key){
      return ByteString.encodeUtf8(key).md5().utf8();
    }

    public static class DateModel{
        public Date birthday;
    }

    @Test
    public void testDate(){
        String json ="{\"birthday\":\"Thu Oct 16 07:13:48 GMT 2014\"}";
//        String json ="{\"birthday\":\"Thu Oct 16 07:13:48 GMT 2015\"}";
        System.out.println(json);
        GsonBuilder builder = new GsonBuilder();
        builder.enableComplexMapKeySerialization()
                .serializeNulls();
        builder.registerTypeAdapterFactory(DateTypeAdapter.FACTORY);
        Gson gson =builder.create();
        DateModel people = gson.fromJson(json, DateModel.class);
        System.out.println(people.birthday);
    }
    @Test
    public void dateTest(){
        String s = "2016-08-12T16:00Z";
//        Pattern GMT_PATTERN1
//                = Pattern.compile(".*GMT\\+{0,1}(\\d{2,2}):{0,1}\\d{0,2}.*");
//        Matcher matcher = GMT_PATTERN1.matcher(s);
//        System.out.println(matcher.matches());
//        System.out.println(matcher.group(1)+ "   ");

        Date date = new Date();
//        s = date.toString();
        System.out.println(s);
        long m = GMTDateUtils.parseExpires(s,0,s.length());
        date = new Date();
        date.setTime(m);
        System.out.println(date.toString());
    }
}
