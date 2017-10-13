package com.sree.textbytes.readability4j;


import okhttp3.Call;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.junit.Before;
import org.junit.Test;

public class SampleUsage {
    private OkHttpClient mHttpClient;

    @Before
    public void setup() {
        mHttpClient = new OkHttpClient.Builder().build();
    }

    private String fetchHtml(String url) throws Exception {
        Request request = new Request.Builder().url(url).get().build();
        Call call = mHttpClient.newCall(request);
        Response response = call.execute();
        return response.body().string();
    }

    @Test
    public void usage() throws Exception {
        ContentExtractor ce = new ContentExtractor();
        String url =
                "http://www.firstpost.com/tech/htc-eyes-15-indian-smartphone-market-to-open-7000-outlets-1164045.html";
        String html = fetchHtml(url);
        Article article = ce.extractContent(html);
        System.out.println(article.getTitle());
        System.out.println(article.getCleanedArticleText());
    }
}
