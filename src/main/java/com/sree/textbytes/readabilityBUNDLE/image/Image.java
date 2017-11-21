package com.sree.textbytes.readabilityBUNDLE.image;

import lombok.Data;
import org.jsoup.nodes.Element;

/**
 * Original code from Project Goose
 * <p>
 * modified author :  sree
 */
@Data
public class Image {

    /**
     * holds the Element node of the image we think is top dog
     */
    private Element topImageNode;

    /**
     * holds the src of the image
     */
    private String imageSrc;

    /**
     * how confident are we in this image extraction? the most images generally
     * the less confident
     */
    private double confidenceScore = 0.0;

    /**
     * what kind of image extraction was used for this? bestGuess, linkTag,
     * openGraph tags?
     */
    private String imageExtractionType = "";

    /**
     * stores how many bytes this image is.
     */
    private int bytes;
}