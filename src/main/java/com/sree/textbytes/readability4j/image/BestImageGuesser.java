package com.sree.textbytes.readability4j.image;

import com.sree.textbytes.StringHelpers.string;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Original code from Project Goose.
 * <p>
 * modified author  : sree
 * <p>
 * If the extracted content does not contain any images ,
 * check the possibility of missing images and try to find a best top image
 */

@Slf4j
public class BestImageGuesser {

    /**
     * this lists all the known bad button names that we have
     */
    private static final Matcher matchBadImageNames;
    private static Matcher knownJunkImageMatcher;
    private static final String NODE_ID_FORMAT = "tag: %s class: %s ID: %s";

    Pattern SPACE = Pattern.compile(" ");
    Document doc;
    Image image;

    /**
     * holds the result of our image extraction
     **/

    static {
        matchBadImageNames = Pattern.compile(".html|.ico|button|twitter.jpg|facebook.jpg|digg.jpg|digg.png|" +
                "delicious.png|facebook.pngreddit.jpg|doubleclick|diggthis|diggThis|adserver|/ads/|ec.atdmt.com|" +
                "mediaplex.com|adsatt|view.atdmt|reuters_fb_share.jpg")
                .matcher(string.empty);

        knownJunkImageMatcher = Pattern.compile("d-logo-blue-100x100.png|WSJ_profile_lg.gif|dealbook75.gif|" +
                "t_wb_75.gif|fivethirtyeight75.gif|current_issue.jpg|thecaucus75.gif")
                .matcher(string.empty);
    }

    public BestImageGuesser() {
        image = new Image();
    }


    public Element filterBadImages(Element topNode) {
        Elements topNodeImages = topNode.select("img");
        log.debug("Found " + topNodeImages.size() + " images in top node");
        if (topNodeImages.size() > 0) {
            for (Element imageElement : topNodeImages) {
                String imgSrc = imageElement.attr("src");
                if (string.isNullOrEmpty(imgSrc))
                    continue;
                matchBadImageNames.reset(imgSrc);
                if (matchBadImageNames.find()) {
                    log.debug("Found bad filename for image: " + imgSrc);
                    imageElement.parent().remove();
                }
            }

        }

        return topNode;
    }


    public Image getTopImage(Element topNode, Document doc) {
        this.doc = doc;

        // fall back to meta tags, these can sometimes be inconsistent which is
        // why we favor them less
        if (image.getImageSrc() == null)
            this.checkForMetaTag();

        return image;

    }

    /**
     * checks to see if we were able to find open graph tags on this page
     *
     * @return
     */

    private boolean checkForMetaTag() {
        if (this.checkMetaTagsForImage("meta[property~=og:image]", "content",
                "opengraph")) {
            return true;
        }
        if (this.checkMetaTagsForImage("link[rel~=image_src]", "href",
                "linktag")) {
            return true;
        }
        return false;
    }

    /**
     * checks to see if we were able to find open graph tags on this page
     *
     * @return
     */

    private boolean checkMetaTagsForImage(String metaRegex, String attr,
                                          String extractionType) {
        try {
            Elements metaElements = this.doc.select(metaRegex);
            for (Element metaItem : metaElements) {
                if (metaItem.attr(attr).length() < 1) {
                    log.debug("Meta " + attr + " link faling");
                    return false;
                }
                knownJunkImageMatcher.reset(metaItem.attr(attr));
                if (knownJunkImageMatcher.find()) {
                    log.debug("Known Junk image found in Meta , skipping " + metaItem.attr(attr));
                    return false;
                } else {
                    this.image.setImageSrc(metaItem.attr(attr));
                    this.image.setImageExtractionType(extractionType);
                    this.image.setConfidenceScore(100);
                    log.debug("Meta tag found and using : "
                            + this.image.getImageSrc());
                    return true;
                }
            }

            return false;
        } catch (Exception e) {
            log.error(e.toString(), e);
            return false;
        }

    }
}
