package com.sree.textbytes.readabilityBUNDLE;


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
        String url = "http://geek.csdn.net/news/detail/239665";
        String html = fetchHtml(url);
        Article article = ContentExtractor.extract(html, ContentExtractor.Algorithm.SNACK);
        System.out.println(article.getTitle());
        System.out.println(article.getCleanedArticleText());
        System.out.println(article.getTags());
    }
}
