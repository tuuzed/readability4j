package com.sree.textbytes.readability4j.extractor;

import com.sree.textbytes.readability4j.Article;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

public interface ReadabilityExtractor {
    Element grabArticle(Article article, ReadabilityExtractor readabilityExtractor);

    Element fetchArticleContent(Document nextPageDocument);
}
