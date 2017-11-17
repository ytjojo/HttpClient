package com.ytjojo.http.coverter;

import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.annotation.Nullable;

import okhttp3.Cookie;
import okhttp3.HttpUrl;
import okhttp3.internal.http.HttpDate;
import okhttp3.internal.publicsuffix.PublicSuffixDatabase;

import static okhttp3.internal.Util.UTC;
import static okhttp3.internal.Util.delimiterOffset;
import static okhttp3.internal.Util.indexOfControlOrNonAscii;
import static okhttp3.internal.Util.trimSubstring;
import static okhttp3.internal.Util.verifyAsIpAddress;

/**
 * Created by Administrator on 2017/11/12 0012.
 */

public class GMTDateUtils {

    private static final Pattern YEAR_PATTERN
            = Pattern.compile("(\\d{2,4})[^\\d]*");
    private static final Pattern MONTH_PATTERN
            = Pattern.compile("(?i)(jan|feb|mar|apr|may|jun|jul|aug|sep|oct|nov|dec).*");
    private static final Pattern DAY_OF_MONTH_PATTERN
            = Pattern.compile("(\\d{1,2})[^\\d]*");
    private static final Pattern TIME_PATTERN
            = Pattern.compile("(\\d{1,2}):(\\d{1,2}):(\\d{1,2})[^\\d]*");
    private static final Pattern GMT_PATTERN
            = Pattern.compile(".*GMT\\+{0,1}(\\d{2,2}):{0,1}\\d{0,2}.*");

    /** Parse a date as specified in RFC 6265, section 5.1.1. */
    public static long parseDate(String s, int pos, int limit) {
        pos = dateCharacterOffset(s, pos, limit, false);
        int timeZoneOffet=0;
        if(s.contains("GMT")){
            Matcher matcher = GMT_PATTERN.matcher(s);
            if(matcher.matches()){
                timeZoneOffet = Integer.parseInt( matcher.group(1));
            }else{

            }
        }

        int hour = -1;
        int minute = -1;
        int second = -1;
        int dayOfMonth = -1;
        int month = -1;
        int year = -1;
        Matcher matcher = TIME_PATTERN.matcher(s);

        while (pos < limit) {
            int end = dateCharacterOffset(s, pos + 1, limit, true);
            matcher.region(pos, end);

            if (hour == -1 && matcher.usePattern(TIME_PATTERN).matches()) {
                hour = Integer.parseInt(matcher.group(1));
                minute = Integer.parseInt(matcher.group(2));
                second = Integer.parseInt(matcher.group(3));
            } else if (dayOfMonth == -1 && matcher.usePattern(DAY_OF_MONTH_PATTERN).matches()) {
                dayOfMonth = Integer.parseInt(matcher.group(1));
            } else if (month == -1 && matcher.usePattern(MONTH_PATTERN).matches()) {
                String monthString = matcher.group(1).toLowerCase(Locale.US);
                month = MONTH_PATTERN.pattern().indexOf(monthString) / 4; // Sneaky! jan=1, dec=12.
            } else if (year == -1 && matcher.usePattern(YEAR_PATTERN).matches()) {
                year = Integer.parseInt(matcher.group(1));
            }

            pos = dateCharacterOffset(s, end + 1, limit, false);
        }

        // Convert two-digit years into four-digit years. 99 becomes 1999, 15 becomes 2015.
        if (year >= 70 && year <= 99) year += 1900;
        if (year >= 0 && year <= 69) year += 2000;

        // If any partial is omitted or out of range, return -1. The date is impossible. Note that leap
        // seconds are not supported by this syntax.
        if (year < 1601) throw new IllegalArgumentException();
        if (month == -1) throw new IllegalArgumentException();
        if (dayOfMonth < 1 || dayOfMonth > 31) throw new IllegalArgumentException();
        if (hour < 0 || hour > 23) throw new IllegalArgumentException();
        if (minute < 0 || minute > 59) throw new IllegalArgumentException();
        if (second < 0 || second > 59) throw new IllegalArgumentException();

        Calendar calendar = new GregorianCalendar(UTC);
        calendar.setLenient(false);
        calendar.set(Calendar.YEAR, year);
        calendar.set(Calendar.MONTH, month - 1);
        calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
        calendar.set(Calendar.HOUR_OF_DAY, hour);
        calendar.set(Calendar.MINUTE, minute);
        calendar.set(Calendar.SECOND, second);
        calendar.set(Calendar.MILLISECOND, 0);
        if(timeZoneOffet>0){
            calendar.add(Calendar.HOUR_OF_DAY,-timeZoneOffet);
        }
        return calendar.getTimeInMillis();
    }

    /**
     * Returns the index of the next date character in {@code input}, or if {@code invert} the index
     * of the next non-date character in {@code input}.
     */
    private static int dateCharacterOffset(String input, int pos, int limit, boolean invert) {
        for (int i = pos; i < limit; i++) {
            int c = input.charAt(i);
            boolean dateCharacter = (c < ' ' && c != '\t') || (c >= '\u007f')
                    || (c >= '0' && c <= '9')
                    || (c >= 'a' && c <= 'z')
                    || (c >= 'A' && c <= 'Z')
                    || (c == ':');
            if (dateCharacter == !invert) return i;
        }
        return limit;
    }

}
