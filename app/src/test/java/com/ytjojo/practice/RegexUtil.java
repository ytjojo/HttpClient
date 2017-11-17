package com.ytjojo.practice;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by Administrator on 2017/11/17 0017.
 */

public class RegexUtil {

    public static boolean fullyMatch(String src,String regex){
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(src);
        return matcher.matches();
    }
    public static boolean contains(String src,String regex){
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(src);
        StringBuffer sb = new StringBuffer();
        while (matcher.find()){
           return true;
        }
        return false;
    }
    public static boolean isFirstHitEnd(String src,String regex){
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(src);
        StringBuffer sb = new StringBuffer();
        while (matcher.find()){
           return matcher.hitEnd();
        }
        return false;
    }
    public static String replcace(String src,String regex,String replacement){
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(src);
        StringBuffer sb = new StringBuffer();
        while (matcher.find()){
            matcher.appendReplacement(sb,replacement);
        }
        matcher.appendTail(sb);
        return sb.toString();
    }
    public static ArrayList<String> findAll (String src,String regex){
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(src);
        ArrayList<String> strings = new ArrayList<>();
        while (matcher.find()){
            strings.add(matcher.group(0));
        }
        return strings;
    }
    public static String findFirst(String src,String regex){
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(src);
        while (matcher.find()){
            return matcher.group(0);
        }
        return null;
    }
    public static int findFirstStart(String src,String regex){
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(src);
        while (matcher.find()){
            return matcher.start();
        }
        return -1;
    }
    public static int findFirstEnd(String src,String regex){
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(src);
        while (matcher.find()){
            return matcher.end();
        }
        return -1;
    }

    public static String[] split(String src,String regex){
        Pattern pattern = Pattern.compile(regex);
        return pattern.split(src);
    }
    public static boolean isStartWith(String src,String regex){
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(src);
        return matcher.lookingAt();
    }

    public static boolean isEndWith(String src,String regex){
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(src);
        int end = -1;
        while (matcher.find()){
            end = matcher.end();
        }
        return  end == src.length();
    }
    public static ArrayList<String> findWords(String src){
        String regex = "\\b\\w+\\b";
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(src);
        ArrayList<String> strings = new ArrayList<>();
        while (matcher.find()) {
            strings.add(matcher.group());
        }
        return strings;
    }
    public static Pattern quote(String regex){
        return Pattern.compile(Pattern.quote(regex));
    }
}
