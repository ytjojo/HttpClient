package com.ytjojo.http.coverter;


import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.google.gson.TypeAdapter;
import com.google.gson.TypeAdapterFactory;
import com.google.gson.internal.bind.util.ISO8601Utils;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;

import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public final class DateTypeAdapter extends TypeAdapter<Date> {
  public static final TypeAdapterFactory FACTORY = new TypeAdapterFactory() {
    @SuppressWarnings("unchecked") // we use a runtime check to make sure the 'T's equal
    @Override public <T> TypeAdapter<T> create(Gson gson, TypeToken<T> typeToken) {
      return typeToken.getRawType() == Date.class ? (TypeAdapter<T>) new DateTypeAdapter() : null;
    }
  };

  private final DateFormat enUsFormat
      = DateFormat.getDateTimeInstance(DateFormat.DEFAULT, DateFormat.DEFAULT, Locale.US);
  private final DateFormat localFormat
      = DateFormat.getDateTimeInstance(DateFormat.DEFAULT, DateFormat.DEFAULT);

  private final static SimpleDateFormat FORMAT_YYYMMDDHHMM =new SimpleDateFormat("yyyy-MM-dd HH:mm");
  private final static SimpleDateFormat FORMAT_GMT =new SimpleDateFormat("EEE MMM ddHH:mm:ss 'GMT' yyyy",Locale.US);
  private final static SimpleDateFormat FORMAT_UTC =new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
  @SuppressWarnings("unchecked")
  @Override public Date read(JsonReader in) throws IOException {
    if (in.peek() == JsonToken.NULL) {
      in.nextNull();
      return null;
    }
    return deserializeToDate(in.nextString());
  }

  private synchronized Date deserializeToDate(String json) {
    try {
      return localFormat.parse(json);
    } catch (ParseException ignored) {
    }
    if(json.length() ==16){
      try {
        return FORMAT_YYYMMDDHHMM.parse(json);
      } catch (ParseException e) {
        throw new JsonSyntaxException(json, e);
      }
    }
    if(json.contains("GMT")){
      try {
        return FORMAT_GMT.parse(json);
      } catch (ParseException e) {

      }
    }
    try {
      return FORMAT_UTC.parse(json);
    } catch (ParseException e) {

    }
    try {
      return enUsFormat.parse(json);
    } catch (ParseException ignored) {
    }
    ParseException parseException;
    try {
        return ISO8601Utils.parse(json, new ParsePosition(0));
    } catch (ParseException e) {
      parseException = e;
    }
    Date result= DateUtils.parseDate(json);
    if(result == null){
      throw new JsonSyntaxException(json, parseException);
    }
    return result;
  }

  @Override public synchronized void write(JsonWriter out, Date value) throws IOException {
    if (value == null) {
      out.nullValue();
      return;
    }
    String dateFormatAsString = enUsFormat.format(value);
    out.value(dateFormatAsString);
  }
}
  