package com.ytjojo.practice;

import org.junit.Test;

import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by Administrator on 2017/11/16 0016.
 */

public class Regex {
    Pattern pattern = Pattern.compile("(\\d+)");
//Pattern pattern = Pattern.compile("([^\\d]*)(\\d+?)[^\\d]*");
//Pattern pattern = Pattern.compile("[\\u4E00-\\u9FA5]+");

    @Test
    public void testReg(){
        String s = "今天aaa222已5经收到111sdwsdw";
         Matcher matcher = pattern.matcher(s);
        boolean match = matcher.matches();
        System.out.println(match);
        if(match){
            int count=  matcher.groupCount()+1;
            for (int i = 0; i < count; i++) {
                try{
                    String result = matcher.group(i);
                    System.out.println(i+"matcher group  "+ result);
                }catch (Exception e){
                    e.printStackTrace();
                }

            }

        }

        matcher = pattern.matcher(s);
        int count = 0;
        StringBuffer sb = new StringBuffer();
        while(matcher.find()) {
            count++;
            System.out.println("Match number "+count);
            System.out.println("start(): "+matcher.start());
            System.out.println("end(): "+matcher.end());

            int groupCount=  matcher.groupCount()+1;
            for (int i = 0; i < groupCount; i++) {
                try{
                    String result = matcher.group(i);
                    System.out.println(i+"find matcher group  "+ result);
                }catch (Exception e){
                    e.printStackTrace();
                }

            }
            matcher.appendReplacement(sb,"---");

        }
        matcher.appendTail(sb);
        System.out.println(sb.toString());

    }
    @Test
    public void testUtil(){
        System.out.println(RegexUtil.findAll("送领是 导滴1叫欧文1322，是滴是滴123.","([^\\d]+)"));

    }
    @Test
    public void testSplit(){
        System.out.println(Arrays.asList(RegexUtil.split("送领导1叫欧文1322，是滴是滴123","(\\d+)")));

    }
    @Test
    public void testEnd(){
        System.out.println(RegexUtil.isEndWith("1送领导1叫欧文1322，是滴是滴","(是+)"));

    }
    @Test
    public void testHitEnd(){
        System.out.println(RegexUtil.isFirstHitEnd("11说打控卫111","(\\d+)"));

    }
    @Test
    public void testGroup(){
        Pattern pattern = Pattern.compile("\\b(\\w+)\\b\\s\\1\\b");
        Matcher matcher=  pattern.matcher(".zery zery sd zery zery");
//        Pattern pattern = Pattern.compile("\\b([a-z]+) \\1\\b");
//        Matcher matcher=  pattern.matcher("is is the cost of of gasoline going up up");
        int count = 0;
        while(matcher.find()) {
            count++;
            System.out.println("Match number "+count);
            System.out.println("start(): "+matcher.start());
            System.out.println("end(): "+matcher.end());

            int groupCount=  matcher.groupCount()+1;
            for (int i = 0; i < groupCount; i++) {
                try{
                    String result = matcher.group(i);
                    System.out.println(i+"find matcher group  "+ result);
                }catch (Exception e){
                    e.printStackTrace();
                }

            }

        }
    }

}
