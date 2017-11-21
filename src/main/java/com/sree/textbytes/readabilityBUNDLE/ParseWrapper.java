package com.sree.textbytes.readabilityBUNDLE;

import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.text.SimpleDateFormat;
import java.util.Calendar;

/**
 * Original code from Project Goose
 *
 * @user : Sreejith.S
 */

@Slf4j
public class ParseWrapper {
    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    public String status = "notStarted";
    public String startTime;


    public Document parse(String html) {
        this.status = "Started";
        this.startTime = now();
        Document doc;
        try {
            doc = Jsoup.parse(html);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        this.status = "Done";
        log.debug("Html parsed :  Time : {} Status : {}", startTime, status);
        return doc;
    }

    public static String now() {
        Calendar cal = Calendar.getInstance();
        return DATE_FORMAT.format(cal.getTime());
    }
}