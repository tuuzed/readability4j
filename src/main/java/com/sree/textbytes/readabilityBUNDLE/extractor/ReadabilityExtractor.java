package com.sree.textbytes.readabilityBUNDLE.extractor;

import com.sree.textbytes.readabilityBUNDLE.Article;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

public interface ReadabilityExtractor {
    Element grabArticle(Article article);

    Element fetchArticleContent(Document nextPageDocument);
}
