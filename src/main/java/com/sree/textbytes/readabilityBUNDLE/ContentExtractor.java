package com.sree.textbytes.readabilityBUNDLE;

import com.sree.textbytes.StringHelpers.StringSplitter;
import com.sree.textbytes.StringHelpers.string;
import com.sree.textbytes.readabilityBUNDLE.cleaner.DocumentCleaner;
import com.sree.textbytes.readabilityBUNDLE.extractor.ReadabilityCoreExtractor;
import com.sree.textbytes.readabilityBUNDLE.extractor.ReadabilityExtractor;
import com.sree.textbytes.readabilityBUNDLE.extractor.ReadabilityGooseExtractor;
import com.sree.textbytes.readabilityBUNDLE.extractor.ReadabilitySnackExtractor;
import com.sree.textbytes.readabilityBUNDLE.formatter.DocumentFormatter;
import com.sree.textbytes.readabilityBUNDLE.image.BestImageGuesser;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.jsoup.select.Selector;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Original code from Project Goose
 * <p>
 * modified author sree
 */

@Slf4j
public class ContentExtractor {

    public enum Algorithm {

        SNACK(ReadabilitySnackExtractor.getInstance()),
        CORE(ReadabilityCoreExtractor.getInstance()),
        GOOSE(ReadabilityGooseExtractor.getInstance());

        private ReadabilityExtractor extractor;

        Algorithm(ReadabilityExtractor extractor) {
            this.extractor = extractor;
        }

        public ReadabilityExtractor getExtractor() {
            return extractor;
        }
    }

    private ContentExtractor() {
    }

    private static class Holder {
        private static final ContentExtractor instance = new ContentExtractor();
    }


    public static Article extract(String rawHtml) {
        return Holder.instance.performExtraction(rawHtml, Algorithm.SNACK, null);
    }

    public static Article extract(String rawHtml, List<String> htmlSources) {
        return Holder.instance.performExtraction(rawHtml, Algorithm.SNACK, htmlSources);
    }

    public static Article extract(String rawHtml, Algorithm algorithm, List<String> htmlSources) {
        return Holder.instance.performExtraction(rawHtml, algorithm, htmlSources);
    }

    public static Article extract(String rawHtml, Algorithm algorithm) {
        return Holder.instance.performExtraction(rawHtml, algorithm, null);
    }

    private Article performExtraction(String rawHtml, Algorithm algorithm, List<String> htmlSources) {
        Article article = new Article();
        try {
            article.setRawHtml(rawHtml);
            Document document;
            ParseWrapper parseWrapper = new ParseWrapper();
            try {
                document = parseWrapper.parse(rawHtml);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
            try {
                article.setPublishDate(extractPublishedDate(document));
            } catch (Exception e) {
                log.warn("Publish Date extraction failed ");
            }
            try {
                article.setTags(extractTags(document));
            } catch (Exception e) {
                log.warn("Extract tags failed");
            }
            try {
                article.setTitle(getTitle(document));
            } catch (Exception e) {
                log.warn("Article Set title failed ");
            }
            try {
                article.setMetaDescription(getMetaDescription(document));
                article.setMetaKeywords(getMetaKeywords(document));
            } catch (Exception e) {
                log.warn("Meta Key & Des failed to set ");
            }
            /*
             * Find out the possibility of Next Page in the input,
             */
            if (htmlSources != null) {
                if (htmlSources.size() > 0) {
                    log.debug("There are next pages, true with size : {}", htmlSources.size());
                    article.setMultiPageStatus(true);
                    article.setNextPageHtmlSources(htmlSources);
                }
            }
            //now perform a nice deep cleansing
            DocumentCleaner documentCleaner = new DocumentCleaner();
            document = documentCleaner.clean(document);
            log.debug("Cleaned Document: {}", document.toString());
            article.setCleanedDocument(document);
            article.setTopNode(algorithm.getExtractor().grabArticle(article));

            if (article.getTopNode() != null) {
                log.debug("Extracted content Before CleanUP : {}", article.getTopNode());
                /*
                 * Check out another Image Extraction algorithm to find out the best image
                 */
                try {
                    BestImageGuesser bestImageGuesser = new BestImageGuesser();
                    bestImageGuesser.filterBadImages(article.getTopNode());

                    Elements imgElements = article.getTopNode()
                            .getElementsByTag("img");
                    ArrayList<String> imageCandidates = new ArrayList<String>();
                    for (Element imgElement : imgElements) {
                        imageCandidates.add(imgElement.attr("src"));

                    }
                    log.debug("Available size of images in top node : {}" + imageCandidates.size());

                    // Setting the best image , in case still you need the top image.
                    article.setTopImage(bestImageGuesser.getTopImage(article.getTopNode(), document));
                    log.debug("BestImage : {}" + article.getTopImage().getImageSrc());

                    String bestImage = article.getTopImage().getImageSrc();
                    if (!string.isNullOrEmpty(bestImage)) {
                        log.debug("Best image found : {}", bestImage);
                        if (!imageCandidates.contains(bestImage)) {
                            log.debug("Top node does not contain the same Best Image");
                            try {
                                log.debug("Child Node : {}", article.getTopNode().children().size());
                                if (article.getTopNode().children().size() > 0) {
                                    log.debug("Child Nodes greater than Zero " + article.getTopNode().children().size());
                                    article.getTopNode().child(0).before("<p><img src=" + bestImage + "></p>");
                                } else {
                                    log.debug("Top node has 0 childs appending after");
                                    article.getTopNode().append("<p><img src=" + bestImage + "></p>");
                                }
                            } catch (Exception e) {
                                log.error(e.toString(), e);
                            }
                        } else {
                            log.debug("Top node already has the Best image found");
                        }
                    }
                } catch (Exception e) {
                    log.warn("Best Image Guesser failed {}", e.toString());
                }

                /*
                 * So we have all of the content that we need. Now we clean it up for presentation.
                 */
                DocumentFormatter documentFormatter = new DocumentFormatter();
                Element node = documentFormatter.getFormattedElement(article.getTopNode());

                article.setCleanedArticleText(outputNormalization(node.toString()));

                /*
                 * check whether the extracted content lenght less than meta
                 * description
                 */
                log.debug("Meta des lenght : {} content lenght : {}", article.getMetaDescription().length(), article.getTopNode().text().length());
                if (article.getMetaDescription().trim().length() > article.getTopNode().text().length()) {
                    log.debug("Meta Description greater than extrcated content , swapping");
                    article.setCleanedArticleText("<div><p>" + article.getMetaDescription().trim() + "</p></div>");
                }
                log.debug("After clean up : {}", node);
            }

        } catch (Exception e) {
            log.error("General Exception occured  {}", e.toString(), e);
        }

        return article;
    }

    /**
     * Convert single Brs in to double brs
     *
     * @param text
     * @return
     */
    private String outputNormalization(String text) {
        return text.replaceAll("<br[^>]*>", "<br /><br />");
    }


    /**
     * if the article has meta keywords set in the source, use that
     */
    private String getMetaKeywords(Document doc) {
        return getMetaContent(doc, "meta[name=keywords]");
    }

    /**
     * if the article has meta description set in the source, use that
     */
    private String getMetaDescription(Document doc) {
        return getMetaContent(doc, "meta[name=description]");
    }

    private String getMetaContent(Document doc, String metaName) {
        Elements meta = doc.select(metaName);
        if (meta.size() > 0) {
            String content = meta.first().attr("content");
            return string.isNullOrEmpty(content) ? string.empty : content.trim();
        }
        return string.empty;
    }

    /**
     * attemps to grab titles from the html pages, lots of sites use different
     * delimiters for titles so we'll try and do our best guess.
     *
     * @param doc
     * @return
     */
    private String getTitle(Document doc) {
        String title = string.empty;

        try {
            Elements titleElem = doc.getElementsByTag("title");
            if (titleElem == null || titleElem.isEmpty())
                return string.empty;

            String titleText = titleElem.first().text();
            if (string.isNullOrEmpty(titleText))
                return string.empty;

            boolean usedDelimeter = false;

            if (titleText.contains("|")) {
                titleText = doTitleSplits(titleText, Patterns.PIPE_SPLITTER);
                usedDelimeter = true;
            }

            if (!usedDelimeter && titleText.contains("-")) {
                titleText = doTitleSplits(titleText, Patterns.DASH_SPLITTER);
                usedDelimeter = true;
            }
            if (!usedDelimeter && titleText.contains("»")) {
                titleText = doTitleSplits(titleText, Patterns.ARROWS_SPLITTER);
                usedDelimeter = true;
            }

            if (!usedDelimeter && titleText.contains(":")) {
                titleText = doTitleSplits(titleText, Patterns.COLON_SPLITTER);
            }
            title = titleText;
        } catch (NullPointerException e) {
            log.error(e.toString());
        }
        return title;
    }

    /**
     * based on a delimeter in the title take the longest piece or do some
     * custom logic based on the site
     *
     * @param title
     * @param splitter
     * @return
     */
    private String doTitleSplits(String title, StringSplitter splitter) {
        int largetTextLen = 0;
        int largeTextIndex = 0;

        String[] titlePieces = splitter.split(title);

        // take the largest split
        for (int i = 0; i < titlePieces.length; i++) {
            String current = titlePieces[i];
            if (current.length() > largetTextLen) {
                largetTextLen = current.length();
                largeTextIndex = i;
            }
        }

        return Patterns.TITLE_REPLACEMENTS.replaceAll(titlePieces[largeTextIndex])
                .trim();
    }

    private Set<String> extractTags(Element node) {
        if (node.children().size() == 0)
            return Patterns.NO_STRINGS;
        Elements elements = Selector.select(Patterns.A_REL_TAG_SELECTOR, node);
        if (elements.size() == 0)
            return Patterns.NO_STRINGS;
        Set<String> tags = new HashSet<String>(elements.size());
        for (Element el : elements) {
            String tag = el.text();
            if (!string.isNullOrEmpty(tag))
                tags.add(tag);
        }
        return tags;
    }

    private String extractPublishedDate(Document doc) {
        String pubDateRegex = "(DATE|date|pubdate|Date|REVISION_DATE)";
        return doc.select("meta[name~=" + pubDateRegex + "]").attr("content");
    }


    // used for gawker type ajax sites with pound sites
    private String getUrlToCrawl(String urlToCrawl) {
        String finalURL;
        if (urlToCrawl.contains("#!")) {
            finalURL = Patterns.ESCAPED_FRAGMENT_REPLACEMENT.replaceAll(urlToCrawl);
        } else {
            finalURL = urlToCrawl;
        }
        log.debug("Extraction: " + finalURL);
        return finalURL;
    }
}
