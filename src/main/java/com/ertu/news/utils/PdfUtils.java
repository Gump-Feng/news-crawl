package com.ertu.news.utils;

import com.itextpdf.text.pdf.PdfReader;
import org.apache.log4j.Logger;

/**
 * @author hxf
 * @date 2019/3/20 9:28
 */

public class PdfUtils {
    private static Logger logger = Logger.getLogger(PdfUtils.class);

    public static int getPdfPage(byte[] bytes, String fileUrl){
        int pages = 0;
        try {
            PdfReader reader = new PdfReader(bytes);
            pages = reader.getNumberOfPages();
        } catch (Exception e) {
            logger.info("the fileUrl is not the type that we expected: "+fileUrl);
        }
        return pages;
    }
    /*public static int getPdfPage(byte[] bytes){
        int pages = 0;
        try {
            PdfReader reader = new PdfReader(bytes);
            pages = reader.getNumberOfPages();
        } catch (Exception e) {
            e.getMessage();
        }
        return pages;
    }*/

}
