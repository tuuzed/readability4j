package com.sree.textbytes.readability4j.nextpage;

import com.sree.textbytes.readability4j.Article;
import com.sree.textbytes.readability4j.ParseWrapper;
import com.sree.textbytes.readability4j.cleaner.DocumentCleaner;
import com.sree.textbytes.readability4j.extractor.ReadabilityExtractor;
import com.sree.textbytes.readability4j.formatter.DocumentFormatter;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.ArrayList;
import java.util.List;

/**
 * Append next page extracted content and create a final consolidated
 *
 * @author sree
 */

@Slf4j
public class AppendNextPage {


    public List<Integer> contentHashes = new ArrayList<Integer>();

    /**
     * Append next page content
     *
     * @param article
     * @param firstPageContent
     * @param readabilityExtractor
     * @return
     */
    public Element appendNextPageContent(Article article, Element firstPageContent,
                                         ReadabilityExtractor readabilityExtractor) {
        int pageNumber = 1;
        DocumentFormatter documentFormatter = new DocumentFormatter();
        contentHashes.add(firstPageContent.text().hashCode());
        Document document = article.getCleanedDocument();
        document.body().empty();
        Element finalConsolidatedContent = document.createElement("div").attr("id", "ace-final-consolidated");
        Element articleContent = document.createElement("div").attr("algo-page-number", Integer.toString(pageNumber)).attr("class", "algo-page-class");
        articleContent.appendChild(documentFormatter.getFormattedElement(firstPageContent));

        finalConsolidatedContent.appendChild(articleContent);

        ParseWrapper parseWrapper = new ParseWrapper();
        DocumentCleaner documentClearner = new DocumentCleaner();

        if (article.isMultiPageStatus()) {
            List<String> nextPageHtmlSource;
            nextPageHtmlSource = article.getNextPageHtmlSources();
            log.debug("MultiPagesInfo size : " + nextPageHtmlSource.toString());

            for (String nextPageHtml : nextPageHtmlSource) {
                log.debug("Fetching article from next page : ");
                Element nextPageExtractedContent = null;
                Document nextPageDocument = null;
                try {
                    nextPageDocument = parseWrapper.parse(nextPageHtml);
                    nextPageDocument = documentClearner.clean(nextPageDocument);
                } catch (Exception e) {
                    log.warn("JSOUP PARSE EXCEPTION ", e);
                }

                nextPageExtractedContent = readabilityExtractor.fetchArticleContent(nextPageDocument);
                if (nextPageExtractedContent != null) {
                    if (checkDuplicateNextPage(nextPageExtractedContent.text().hashCode())) {
                        log.debug("Duplicate next page content found , skipping");
                    } else {

                        contentHashes.add(nextPageExtractedContent.text().hashCode());
                        Element nextPageContent = document.createElement("div").attr("algo-page-number", Integer.toString(pageNumber)).attr("class", "algo-page-class");
                        nextPageContent.appendChild(documentFormatter.getFormattedElement(nextPageExtractedContent));
                        log.debug("Next Page Content : " + nextPageExtractedContent);
                        if (!checkParagraphDeDupe(finalConsolidatedContent, nextPageContent)) {
                            finalConsolidatedContent.appendChild(nextPageContent);
                            pageNumber++;
                        }
                    }
                }
            }
        }

        return finalConsolidatedContent;
    }


    /**
     * Paragraph duplicate mechanism. Check whether next page extracted content is duplicate of existing.
     *
     * @param finalConsolidatedContent
     * @param nextPageContent
     * @return
     */

    private boolean checkParagraphDeDupe(Element finalConsolidatedContent, Element nextPageContent) {

        int pSize = totalTags(nextPageContent);
        if (pSize == 0) {
            return true;
        }
        int i = 0, finalPSize;
        Element firstPara = nextPageContent.getElementsByTag("p").get(i);
        if (firstPara.toString().length() < 100) {
            if (pSize > 1) {
                i = 1;
                firstPara = nextPageContent.getElementsByTag("p").get(i);
            }
        }
        Elements finalElements = finalConsolidatedContent
                .getElementsByAttribute("algo-page-number");
        for (Element elt : finalElements) {
            finalPSize = totalTags(elt);
            if (finalPSize > i) {
                Element firstPtag = elt.getElementsByTag("p").get(i);
                if (firstPara.toString().equals(firstPtag.toString())) {
                    return true;
                }
            }
        }
        return false;
    }

    private int totalTags(Element element) {
        return element.getElementsByTag("p").size();
    }

    /**
     * De dupe mechanism using content hash
     *
     * @param contentHash
     * @return
     */
    private boolean checkDuplicateNextPage(int contentHash) {
        return contentHashes.contains(contentHash);
    }
}
