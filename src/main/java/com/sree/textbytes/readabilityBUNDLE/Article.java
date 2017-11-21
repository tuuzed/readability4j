package com.sree.textbytes.readabilityBUNDLE;

import com.sree.textbytes.readabilityBUNDLE.image.Image;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Original code from Project Goose
 * <p>
 * modified author : Sree
 * <p>
 * This class represents the extraction of an Article from a website
 * It also contains all the meta data's extracted from the web page.
 */
@Slf4j
@ToString
public class Article {
    /**
     * Cleaned document for extraction
     */
    @Getter
    @Setter
    private Document cleanedDocument;
    /**
     * Holds the title of the webpage
     */
    @Getter
    @Setter
    private String title;
    /**
     * publish date
     */
    @Getter
    @Setter
    private String publishDate;
    /**
     * If any top image from the web page
     */
    @Getter
    @Setter
    private Image topImage;
    /**
     * holds the metadescription meta tag in the html doc
     */
    @Getter
    @Setter
    private String metaDescription;
    /**
     * holds the clean text after we do strip out everything but the text and
     * wrap it up in a nice package this is the guy you probably want, just pure
     * text
     */
    @Getter
    @Setter
    private String cleanedArticleText;
    /**
     * holds the original unmodified HTML retrieved from the URL
     */
    @Getter
    @Setter
    private String rawHtml;
    /**
     * holds the meta keywords that would in the meta tag of the html doc
     */
    @Getter
    @Setter
    private String metaKeywords;
    /**
     * holds the meta data canonical link that may be place in the meta tags of
     * the html doc
     */
    @Getter
    @Setter
    private String canonicalLink;
    /**
     * this represents the jSoup element that we think is the big content dude
     * of this page we can use this node to start grabbing text, images, etc..
     * around the content
     */
    @Getter
    @Setter
    private Element topNode;
    /**
     * holds a list of tags extracted from the article
     */
    @Setter
    private Set<String> tags;

    /**
     * The unique set of tags that matched: "a[rel=tag], a[href*=/tag/]"
     *
     * @return the unique set of TAGs extracted from this {@link Article}
     */
    public Set<String> getTags() {
        if (tags == null) {
            tags = new HashSet<String>();
        }
        return tags;
    }

    @Setter
    private List<String> nextPageHtmlSources;

    public List<String> getNextPageHtmlSources() {
        if (nextPageHtmlSources == null) {
            return Collections.emptyList();
        }
        return nextPageHtmlSources;
    }

    /**
     * Its true of the document has next pages.
     */
    @Getter
    @Setter
    private boolean isMultiPageStatus = false;
}