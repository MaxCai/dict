package com.example.yecai.dict;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.regex.*;

/**
 * Created by ye.cai on 2014/10/8.
 */
public class TransLate {
    String input;
    String baseUrl;
    TransLate(String input)
    {
        baseUrl = "http://dict.youdao.com/search?doctype=xml&q=";
        this.input = input;
    }
    TransLate()
    {

    }

    public void setInput(String input)
    {
        this.input = input;
    }

    public String parseTranslateFromUrl()
    {
        String result = "";
        BufferedReader in = null;
        try
        {
            String get = baseUrl + URLEncoder.encode(input, "utf-8");
            URL realUrl = new URL(get);
            URLConnection conn = realUrl.openConnection();
            conn.setRequestProperty("accept", "*/*");
            conn.setRequestProperty("connection", "Keep-Alive");
            conn.setRequestProperty("user-agent", "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/35.0.1916.153 Safari/537.36");
            conn.connect();
            in = new BufferedReader(
                    new InputStreamReader(conn.getInputStream())
            );
            String line;
            String response = "";
            while((line = in.readLine()) != null)
            {
                response += "\n" + line;
            }
            Matcher matcher = Pattern.compile(
                    "<content>\\s*<!\\[CDATA\\[(.+?)\\]\\]>" +
                    "|<key>\\s*<!\\[CDATA\\[(.+?)\\]\\]>" +
                    "|<value>\\s*<!\\[CDATA\\[(.+?)\\]\\]>" +
                    "|<sentence>\\s*<!\\[CDATA\\[(.+?)\\]\\]>" +
                    "|<sentence-translation>\\s*<!\\[CDATA\\[(.+?)\\]\\]>").matcher(response);
            int groupCnt = matcher.groupCount();
            while(matcher.find())
            {
                String grp;
                for(int i = 1; i <= groupCnt; ++i)
                    if((grp = matcher.group(i)) != null)
                        result += "<br>" + grp;
            }
        }
       catch (Exception e)
       {
           System.out.println("翻译请求异常！ " + e);
           e.printStackTrace();
       }
        finally
        {
            try
            {
                if(in != null)
                    in.close();
            }
            catch(IOException ex)
            {
                ex.printStackTrace();
            }
        }
        return result;
    }
}
