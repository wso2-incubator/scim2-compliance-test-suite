/*
 * Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.scim2.compliance.pdf;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.font.PDType0Font;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.encoding.WinAnsiEncoding;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.wso2.scim2.compliance.entities.Result;
import org.wso2.scim2.compliance.entities.TestResult;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

/**
 * This class handle the PDF report generation for the test suite.
 */
public class PDFGenerator {

    private static PDDocument document = null;

    /**
     * Init report generation.
     *
     * @param finalResults
     */
    private static void init(Result finalResults) {

        document = new PDDocument();
        if (finalResults.getErrorMessage() != "") {
            // Creating a blank page.
            PDPage blankPage = new PDPage();
            // Adding the blank page to the document.
            document.addPage(blankPage);

        } else {
            for (int i = 0; i < finalResults.getResults().size() * 2; i++) {
                // Creating a blank page.
                PDPage blankPage = new PDPage();
                // Adding the blank page to the document.
                document.addPage(blankPage);
            }
        }
    }

    /**
     * Method to generate the report in PDF format.
     *
     * @param finalResults
     * @param fullPath
     * @return
     * @throws IOException
     */
    public static String generatePdfResults(Result finalResults, String fullPath) throws IOException {

        init(finalResults);
        // Loading a ttf file containing text style.
        PDType0Font font = PDType0Font.load(document, new File("/home/anjanap/tr.ttf"));
        int pageNo = 0;
        for (TestResult testResult : finalResults.getResults()) {
            //Retrieving the pages of the document
            PDPage page = document.getPage(pageNo);
            PDPageContentStream contentStream = new PDPageContentStream(document, page);

            PDFont pdfFont = PDType1Font.HELVETICA;
            float typeWriterFont = 30;
            float titleFont = 10;
            float fontSize = 8;
            float smallFontSize = 7;
            float leading = 1.5f * fontSize;

            PDRectangle mediabox = page.getMediaBox();
            float margin = 42;
            float width = mediabox.getWidth() - 2 * margin;
            float startX = mediabox.getLowerLeftX() + margin;
            float startY = mediabox.getUpperRightY() - margin;

            ArrayList<String> toServer = new ArrayList<>();
            ArrayList<String> fromServer = new ArrayList<>();
            ArrayList<String> subTests = new ArrayList<>();
            if (testResult.getWire() != null) {
                toServer = removeUnsupportedCharacters(testResult.getWire().getToServer());
                fromServer = removeUnsupportedCharacters(testResult.getWire().getFromServer());
                subTests = removeUnsupportedCharacters(testResult.getWire().getTests());
            }

            List testName = getLines(testResult.getName(), fontSize, pdfFont, width);
            List testMessage = getLines(testResult.getMessage(), fontSize, pdfFont, width);
            List testLabel = getLines(testResult.getStatusText(), fontSize, pdfFont, width);
            List responseBody = new ArrayList();
            List requestBody = new ArrayList();
            if (!toServer.isEmpty()) {
                requestBody = getLines(toServer.get(toServer.size() - 1), fontSize, pdfFont, width);
                toServer.remove(toServer.size() - 1);
            }
            if (!fromServer.isEmpty()) {
                responseBody = getLines(fromServer.get(fromServer.size() - 1), fontSize, pdfFont, width);
                fromServer.remove(fromServer.size() - 1);
            }
            List emptyLine = new ArrayList();
            emptyLine.add(" ");

            //Drawing a rectangle
            contentStream.addRect(startX, startY - 5, width, 1);

            //Begin text printing
            contentStream.fill();
            contentStream.beginText();
            contentStream.setFont(PDType1Font.TIMES_ITALIC, fontSize);
            contentStream.newLineAtOffset(startX, startY);

            contentStream.showText("SCIM 2.0 Compliance Test Suite - Auto Generated Test Report");
            printResult(contentStream, fontSize, pdfFont, leading, startX, startY, emptyLine);
            printResult(contentStream, fontSize, pdfFont, leading, startX, startY, emptyLine);
            Color titleColor = new Color(102, 0, 153);
            contentStream.setFont(PDType1Font.COURIER_BOLD, fontSize);
            contentStream.setNonStrokingColor(Color.BLACK);
            contentStream.showText("Test Case Name : ");
            contentStream.setFont(PDType1Font.COURIER_BOLD, titleFont);
            contentStream.setNonStrokingColor(titleColor);
            printResult(contentStream, fontSize, pdfFont, leading, startX, startY, emptyLine);
            printResult(contentStream, fontSize, pdfFont, leading, startX, startY, testName);
            printResult(contentStream, fontSize, pdfFont, leading, startX, startY, emptyLine);

            Color timeColor = new Color(149, 69, 19);
            contentStream.setFont(PDType1Font.COURIER_BOLD, fontSize);
            contentStream.setNonStrokingColor(Color.BLACK);
            contentStream.showText("Test Case Elapsed Time(ms) : ");
            contentStream.setFont(PDType1Font.COURIER_BOLD, fontSize);
            contentStream.setNonStrokingColor(timeColor);
            printResult(contentStream, fontSize, pdfFont, leading, startX, startY, emptyLine);
            contentStream.showText(Long.toString(testResult.getElapsedTime()));
            printResult(contentStream, fontSize, pdfFont, leading, startX, startY, emptyLine);
            printResult(contentStream, fontSize, pdfFont, leading, startX, startY, emptyLine);

            if (!testMessage.isEmpty()) {
                contentStream.setFont(PDType1Font.COURIER_BOLD, fontSize);
                contentStream.setNonStrokingColor(Color.BLACK);
                contentStream.showText("Test Case Errors : ");
                contentStream.setFont(PDType1Font.COURIER_BOLD, fontSize);
                contentStream.setNonStrokingColor(Color.RED);
                printResult(contentStream, fontSize, pdfFont, leading, startX, startY, emptyLine);
                printResult(contentStream, fontSize, pdfFont, leading, startX, startY, testMessage);
                printResult(contentStream, fontSize, pdfFont, leading, startX, startY, emptyLine);
            }
            Color statusColor = new Color(0, 102, 0);
            Color statusColor2 = new Color(204, 204, 0);
            contentStream.setFont(PDType1Font.COURIER_BOLD, fontSize);
            contentStream.setNonStrokingColor(Color.BLACK);
            contentStream.showText("Test Case Status : ");
            contentStream.setFont(PDType1Font.COURIER_BOLD, fontSize);
            if (testResult.getStatusText() == "Success") {
                contentStream.setNonStrokingColor(statusColor);
            } else if (testResult.getStatusText() == "Failed") {
                contentStream.setNonStrokingColor(Color.red);
            } else {
                contentStream.setNonStrokingColor(statusColor2);
            }
            printResult(contentStream, fontSize, pdfFont, leading, startX, startY, emptyLine);
            printResult(contentStream, fontSize, pdfFont, leading, startX, startY, testLabel);
            printResult(contentStream, fontSize, pdfFont, leading, startX, startY, emptyLine);

            contentStream.setFont(PDType1Font.COURIER_BOLD, fontSize);
            contentStream.setNonStrokingColor(Color.BLACK);
            contentStream.showText(("Request : "));
            contentStream.setFont(PDType1Font.COURIER, smallFontSize);
            contentStream.setNonStrokingColor(Color.BLUE);
            printResult(contentStream, fontSize, pdfFont, leading, startX, startY, emptyLine);
            printResult(contentStream, smallFontSize, pdfFont, leading, startX, startY, toServer);
            printResult(contentStream, fontSize, pdfFont, leading, startX, startY, requestBody);
            printResult(contentStream, fontSize, pdfFont, leading, startX, startY, emptyLine);

            JSONObject json = null; // Convert text to object
            String r = null;
            try {
                r = testResult.getWire().getResponseBody();
                json = new JSONObject(r);
            } catch (Exception e) {
                json = null;
            }

            contentStream.setFont(PDType1Font.COURIER_BOLD, fontSize);
            contentStream.setNonStrokingColor(Color.BLACK);
            contentStream.showText("Response : ");
            contentStream.setFont(PDType1Font.COURIER, smallFontSize);
            contentStream.setNonStrokingColor(Color.BLUE);
            printResult(contentStream, smallFontSize, pdfFont, leading, startX, startY, fromServer);
            //printResult(contentStream, fontSize, pdfFont, leading, startX, startY, responseBody);
            if (json != null) {
                try {
                    if (json.getInt("totalResults") > 10) {
//                        String r2 = json.toString(4);
//                        List<String> r3 = getLines(r, fontSize, pdfFont, width);
//                        printResult(contentStream, fontSize, pdfFont, leading, startX, startY, r3);
                        //printResult(contentStream, fontSize, pdfFont, leading, startX, startY, responseBody);
                        contentStream.showText("Response contains more than 10 results which is larger to show in one" +
                                " page.");
                    } else {
                        printResult(contentStream, fontSize, pdfFont, leading, startX, startY, responseBody);
                    }
                } catch (JSONException e) {
                    printResult(contentStream, fontSize, pdfFont, leading, startX, startY, responseBody);
                }
            }
            printResult(contentStream, fontSize, pdfFont, leading, startX, startY, emptyLine);
            //Ending the content stream
            contentStream.endText();
            contentStream.close();
            pageNo++;

            //Retrieving the pages of the document
            PDPage page2 = document.getPage(pageNo);
            PDPageContentStream contentStream2 = new PDPageContentStream(document, page2);
            //Drawing a rectangle
            contentStream2.addRect(startX, startY - 5, width, 1);
            //Begin text printing
            contentStream2.fill();
            contentStream2.beginText();
            contentStream2.setFont(PDType1Font.TIMES_ITALIC, fontSize);
            contentStream2.newLineAtOffset(startX, startY);
            contentStream2.showText("SCIM 2.0 Compliance Test Suite - Auto Generated Test Report");
            printResult(contentStream2, fontSize, pdfFont, leading, startX, startY, emptyLine);
            printResult(contentStream2, fontSize, pdfFont, leading, startX, startY, emptyLine);

            contentStream2.setFont(PDType1Font.COURIER_BOLD, fontSize);
            contentStream2.setNonStrokingColor(Color.BLACK);
            contentStream2.showText("Assertions : ");
            contentStream2.setFont(PDType1Font.COURIER, fontSize);
            contentStream2.setNonStrokingColor(Color.BLUE);
            printResult(contentStream2, fontSize, pdfFont, leading, startX, startY, emptyLine);
            printResult(contentStream2, fontSize, pdfFont, leading, startX, startY, subTests);
            printResult(contentStream2, fontSize, pdfFont, leading, startX, startY, emptyLine);

            //Ending the content stream
            contentStream2.endText();
            contentStream2.close();
            pageNo++;
        }
        //last page
        PDPage blankPage = new PDPage();
        // Adding the blank page to the document.
        document.addPage(blankPage);
        //Retrieving the pages of the document
        PDPage page = document.getPage(pageNo);
        PDPageContentStream contentStream = new PDPageContentStream(document, page);

        PDFont pdfFont = PDType1Font.HELVETICA;
        float typeWriterFont = 12;
        float titleFont = 10;
        float fontSize = 8;
        float smallFontSize = 7;
        float leading = 1.5f * fontSize;

        PDRectangle mediabox = page.getMediaBox();
        float margin = 42;
        float width = mediabox.getWidth() - 2 * margin;
        float startX = mediabox.getLowerLeftX() + margin;
        float startY = mediabox.getUpperRightY() - margin;
        List emptyLine = new ArrayList();
        emptyLine.add(" ");

        //Drawing a rectangle
        contentStream.addRect(startX, startY - 5, width, 1);

        //Begin text printing
        contentStream.fill();
        contentStream.beginText();
        contentStream.setFont(PDType1Font.TIMES_ITALIC, fontSize);
        contentStream.newLineAtOffset(startX, startY);

        contentStream.showText("SCIM 2.0 Compliance Test Suite - Auto Generated Test Report");
        printResult(contentStream, fontSize, pdfFont, leading, startX, startY, emptyLine);
        printResult(contentStream, fontSize, pdfFont, leading, startX, startY, emptyLine);

        Color titleColor = new Color(102, 0, 153);
        Color dataColor = new Color(149, 69, 19);
        Color success = new Color(0, 102, 0);
        Color skipped = new Color(204, 204, 0);
        contentStream.setFont(PDType1Font.COURIER_BOLD, typeWriterFont);
        contentStream.setNonStrokingColor(titleColor);
        contentStream.showText("Summary : ");
        contentStream.setFont(PDType1Font.COURIER_BOLD, titleFont);
        printResult(contentStream, fontSize, pdfFont, leading, startX, startY, emptyLine);
        printResult(contentStream, fontSize, pdfFont, leading, startX, startY, emptyLine);

        contentStream.setFont(PDType1Font.COURIER_BOLD, fontSize);
        contentStream.setNonStrokingColor(success);
        contentStream.showText("Success Test cases : ");
        contentStream.setFont(PDType1Font.COURIER_BOLD, titleFont);
        contentStream.setNonStrokingColor(dataColor);
        printResult(contentStream, fontSize, pdfFont, leading, startX, startY, emptyLine);
        contentStream.showText(Integer.toString(finalResults.getStatistics().getSuccess()));
        printResult(contentStream, fontSize, pdfFont, leading, startX, startY, emptyLine);

        contentStream.setFont(PDType1Font.COURIER_BOLD, fontSize);
        contentStream.setNonStrokingColor(Color.RED);
        contentStream.showText("Failed Test cases : ");
        contentStream.setFont(PDType1Font.COURIER_BOLD, titleFont);
        contentStream.setNonStrokingColor(dataColor);
        printResult(contentStream, fontSize, pdfFont, leading, startX, startY, emptyLine);
        contentStream.showText(Integer.toString(finalResults.getStatistics().getFailed()));
        printResult(contentStream, fontSize, pdfFont, leading, startX, startY, emptyLine);

        contentStream.setFont(PDType1Font.COURIER_BOLD, fontSize);
        contentStream.setNonStrokingColor(skipped);
        contentStream.showText("Skipped Test cases : ");
        contentStream.setFont(PDType1Font.COURIER_BOLD, titleFont);
        contentStream.setNonStrokingColor(dataColor);
        printResult(contentStream, fontSize, pdfFont, leading, startX, startY, emptyLine);
        contentStream.showText(Integer.toString(finalResults.getStatistics().getSkipped()));
        printResult(contentStream, fontSize, pdfFont, leading, startX, startY, emptyLine);

        contentStream.setFont(PDType1Font.COURIER_BOLD, fontSize);
        contentStream.setNonStrokingColor(Color.BLACK);
        contentStream.showText("Total Test cases : ");
        contentStream.setFont(PDType1Font.COURIER_BOLD, titleFont);
        contentStream.setNonStrokingColor(dataColor);
        printResult(contentStream, fontSize, pdfFont, leading, startX, startY, emptyLine);
        contentStream.showText(Integer.toString(finalResults.getStatistics().getTotal()));
        printResult(contentStream, fontSize, pdfFont, leading, startX, startY, emptyLine);

        contentStream.setFont(PDType1Font.COURIER_BOLD, fontSize);
        contentStream.setNonStrokingColor(Color.BLACK);
        contentStream.showText("Time elapsed to run all Test cases(ms) : ");
        contentStream.setFont(PDType1Font.COURIER_BOLD, titleFont);
        contentStream.setNonStrokingColor(dataColor);
        printResult(contentStream, fontSize, pdfFont, leading, startX, startY, emptyLine);
        contentStream.showText(Long.toString(finalResults.getStatistics().getTime()));
        printResult(contentStream, fontSize, pdfFont, leading, startX, startY, emptyLine);

        //Ending the content stream
        contentStream.endText();
        contentStream.close();

        //save the document
        Calendar cal = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat("HHmmss");
        String url = fullPath + "\\" + sdf.format(cal.getTime()) + ".pdf";
        document.save(new File(url));

        //Closing the document
        document.close();
        return url;

    }

    /**
     * Print the results to PDF.
     *
     * @param contentStream
     * @param fontSize
     * @param pdfFont
     * @param leading
     * @param startX
     * @param startY
     * @param lines
     * @throws IOException
     */
    public static void printResult(PDPageContentStream contentStream, float fontSize,
                                   PDFont pdfFont, float leading, float startX, float startY, List<String> lines)
            throws IOException {

        for (String line : lines) {
            contentStream.showText(line);
            contentStream.newLineAtOffset(0, -leading);
        }
    }

    /**
     * Method to separate lines of the PDF.
     *
     * @param text
     * @param fontSize
     * @param pdfFont
     * @param width
     * @return
     * @throws IOException
     */
    private static List<String> getLines(String text, float fontSize, PDFont pdfFont, float width)
            throws IOException {

        width = width - 150;
        List<String> lines = new ArrayList<String>();
        int lastSpace = -1;
        while (text.length() > 0) {
            int spaceIndex = text.indexOf(' ', lastSpace + 1);
            if (spaceIndex < 0) {
                spaceIndex = text.length();
            }
            String subString = text.substring(0, spaceIndex);
            float size = fontSize * pdfFont.getStringWidth(subString) / 1000;
            if (size > width) {
                float requiredSize = (width * 1000) / fontSize;
                int characterSize = getCharacterCount(requiredSize, subString, pdfFont);
                //if (lastSpace < 0)
                lastSpace = characterSize;
                subString = text.substring(0, lastSpace);
                lines.add(subString);
                text = text.substring(lastSpace).trim();
                lastSpace = -1;
            } else if (spaceIndex == text.length()) {
                lines.add(text);
                text = "";
            } else {
                lastSpace = spaceIndex;
            }
        }
        return lines;
    }

    /**
     * This return the character count of a given text.
     *
     * @param requiredSize
     * @param subString
     * @param pdfFont
     * @return
     * @throws IOException
     */
    private static int getCharacterCount(float requiredSize, String subString, PDFont pdfFont) throws IOException {

        double factor = 0.95;
        String string = subString;
        while (pdfFont.getStringWidth(string) > requiredSize) {
            string = string.substring(0, (int) Math.round(string.length() * factor));
        }
        return string.length();
    }

    /**
     * This removes the unsupported characters from the text.
     *
     * @param test
     * @return
     * @throws IOException
     */
    private static ArrayList<String> removeUnsupportedCharacters(String test) throws IOException {

        ArrayList<String> textToBeShown = new ArrayList<>();
        StringBuilder b = new StringBuilder();
        for (int i = 0; i < test.length(); i++) {
            if (WinAnsiEncoding.INSTANCE.contains(test.charAt(i))) {
                b.append(test.charAt(i));
            } else {
                textToBeShown.add(b.toString());
                b = new StringBuilder();
            }
        }
        textToBeShown.add(b.toString());
        return textToBeShown;
    }

}