/*
 * Copyright (c) 2021, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.scim2.testsuite.core.pdf;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.encoding.WinAnsiEncoding;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.wso2.scim2.testsuite.core.entities.Result;
import org.wso2.scim2.testsuite.core.entities.TestResult;

import java.awt.Color;
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
     * @param finalResults Array of test results.
     */
    private static void init(Result finalResults) {

        document = new PDDocument();
        if (!finalResults.getErrorMessage().equals("")) {
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
     * @param finalResults Array of test results.
     * @param fullPath     Path to save pdf.
     * @return url Location of saved document.
     * @throws IOException Exception is related to Input and Output operations.
     */
    public static String generatePdfResults(Result finalResults, String fullPath) throws IOException {

        init(finalResults);
        int pageNo = 0;
        for (TestResult testResult : finalResults.getResults()) {
            // Retrieving the pages of the document.
            PDPage page = document.getPage(pageNo);
            PDPageContentStream contentStream = new PDPageContentStream(document, page);

            PDFont pdfFont = PDType1Font.HELVETICA;
            float titleFont = 10;
            float fontSize = 8;
            float smallFontSize = 7;
            float leading = 1.5f * fontSize;

            PDRectangle mediaBox = page.getMediaBox();
            float margin = 42;
            float width = mediaBox.getWidth() - 2 * margin;
            float startX = mediaBox.getLowerLeftX() + margin;
            float startY = mediaBox.getUpperRightY() - margin;

            ArrayList<String> toServer = new ArrayList<>();
            ArrayList<String> fromServer = new ArrayList<>();
            ArrayList<String> subTests = new ArrayList<>();
            if (testResult.getWire() != null) {
                toServer = removeUnsupportedCharacters(testResult.getWire().getToServer());
                fromServer = removeUnsupportedCharacters(testResult.getWire().getFromServer());
                subTests = removeUnsupportedCharacters(testResult.getWire().getTests());
            }

            List<String> testName = getLines(testResult.getName(), fontSize, pdfFont, width);
            List<String> testMessage = getLines(testResult.getMessage(), fontSize, pdfFont, width);
            List<String> testLabel = getLines(testResult.getStatusText(), fontSize, pdfFont, width);
            List<String> responseBody = new ArrayList<>();
            List<String> requestBody = new ArrayList<>();
            if (!toServer.isEmpty()) {
                requestBody = getLines(toServer.get(toServer.size() - 1), fontSize, pdfFont, width);
                toServer.remove(toServer.size() - 1);
            }
            if (!fromServer.isEmpty()) {
                responseBody = getLines(fromServer.get(fromServer.size() - 1), fontSize, pdfFont, width);
                fromServer.remove(fromServer.size() - 1);
            }
            List<String> emptyLine = new ArrayList<>();
            emptyLine.add(" ");

            // Drawing a rectangle.
            contentStream.addRect(startX, startY - 5, width, 1);

            // Begin text printing.
            contentStream.fill();
            contentStream.beginText();
            contentStream.setFont(PDType1Font.TIMES_ITALIC, fontSize);
            contentStream.newLineAtOffset(startX, startY);

            contentStream.showText("SCIM 2.0 Compliance Test Suite - Auto Generated Test Report");
            printResult(contentStream, leading, emptyLine);
            printResult(contentStream, leading, emptyLine);
            Color titleColor = new Color(102, 0, 153);
            contentStream.setFont(PDType1Font.COURIER_BOLD, fontSize);
            contentStream.setNonStrokingColor(Color.BLACK);
            contentStream.showText("Test Case Name : ");
            contentStream.setFont(PDType1Font.COURIER_BOLD, titleFont);
            contentStream.setNonStrokingColor(titleColor);
            printResult(contentStream, leading, emptyLine);
            printResult(contentStream, leading, testName);
            printResult(contentStream, leading, emptyLine);

            Color timeColor = new Color(149, 69, 19);
            contentStream.setFont(PDType1Font.COURIER_BOLD, fontSize);
            contentStream.setNonStrokingColor(Color.BLACK);
            contentStream.showText("Test Case Elapsed Time(ms) : ");
            contentStream.setFont(PDType1Font.COURIER_BOLD, fontSize);
            contentStream.setNonStrokingColor(timeColor);
            printResult(contentStream, leading, emptyLine);
            contentStream.showText(Long.toString(testResult.getElapsedTime()));
            printResult(contentStream, leading, emptyLine);
            printResult(contentStream, leading, emptyLine);

            if (!testMessage.isEmpty()) {
                contentStream.setFont(PDType1Font.COURIER_BOLD, fontSize);
                contentStream.setNonStrokingColor(Color.BLACK);
                contentStream.showText("Test Case Errors : ");
                contentStream.setFont(PDType1Font.COURIER_BOLD, fontSize);
                contentStream.setNonStrokingColor(Color.RED);
                printResult(contentStream, leading, emptyLine);
                printResult(contentStream, leading, testMessage);
                printResult(contentStream, leading, emptyLine);
            }
            Color statusColor = new Color(0, 102, 0);
            Color statusColor2 = new Color(204, 204, 0);
            contentStream.setFont(PDType1Font.COURIER_BOLD, fontSize);
            contentStream.setNonStrokingColor(Color.BLACK);
            contentStream.showText("Test Case Status : ");
            contentStream.setFont(PDType1Font.COURIER_BOLD, fontSize);
            if (testResult.getStatusText().equals("Success")) {
                contentStream.setNonStrokingColor(statusColor);
            } else if (testResult.getStatusText().equals("Failed")) {
                contentStream.setNonStrokingColor(Color.red);
            } else {
                contentStream.setNonStrokingColor(statusColor2);
            }
            printResult(contentStream, leading, emptyLine);
            printResult(contentStream, leading, testLabel);
            printResult(contentStream, leading, emptyLine);

            contentStream.setFont(PDType1Font.COURIER_BOLD, fontSize);
            contentStream.setNonStrokingColor(Color.BLACK);
            contentStream.showText(("Request : "));
            contentStream.setFont(PDType1Font.COURIER, smallFontSize);
            contentStream.setNonStrokingColor(Color.BLUE);
            printResult(contentStream, leading, emptyLine);
            printResult(contentStream, leading, toServer);
            printResult(contentStream, leading, requestBody);
            printResult(contentStream, leading, emptyLine);
            // Convert text to object.
            JSONObject json;
            String r;
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
            printResult(contentStream, leading, fromServer);
            if (json != null) {
                try {
                    if (json.getInt("totalResults") > 10) {
                        contentStream.showText("Response contains more than 10 results which is larger to show in one" +
                                " page.");
                    } else {
                        printResult(contentStream, leading, responseBody);
                    }
                } catch (JSONException e) {
                    printResult(contentStream, leading, responseBody);
                }
            }
            printResult(contentStream, leading, emptyLine);
            // Ending the content stream.
            contentStream.endText();
            contentStream.close();
            pageNo++;

            // Retrieving the pages of the document.
            PDPage page2 = document.getPage(pageNo);
            PDPageContentStream contentStream2 = new PDPageContentStream(document, page2);
            // Drawing a rectangle.
            contentStream2.addRect(startX, startY - 5, width, 1);
            // Begin text printing.
            contentStream2.fill();
            contentStream2.beginText();
            contentStream2.setFont(PDType1Font.TIMES_ITALIC, fontSize);
            contentStream2.newLineAtOffset(startX, startY);
            contentStream2.showText("SCIM 2.0 Compliance Test Suite - Auto Generated Test Report");
            printResult(contentStream2, leading, emptyLine);
            printResult(contentStream2, leading, emptyLine);

            contentStream2.setFont(PDType1Font.COURIER_BOLD, fontSize);
            contentStream2.setNonStrokingColor(Color.BLACK);
            contentStream2.showText("Assertions : ");
            contentStream2.setFont(PDType1Font.COURIER, fontSize);
            contentStream2.setNonStrokingColor(Color.BLUE);
            printResult(contentStream2, leading, emptyLine);
            printResult(contentStream2, leading, subTests);
            printResult(contentStream2, leading, emptyLine);

            // Ending the content stream.
            contentStream2.endText();
            contentStream2.close();
            pageNo++;
        }
        // Last page.
        PDPage blankPage = new PDPage();
        // Adding the blank page to the document.
        document.addPage(blankPage);
        // Retrieving the pages of the document.
        PDPage page = document.getPage(pageNo);
        PDPageContentStream contentStream = new PDPageContentStream(document, page);

        float typeWriterFont = 12;
        float titleFont = 10;
        float fontSize = 8;
        float leading = 1.5f * fontSize;

        PDRectangle mediaBox = page.getMediaBox();
        float margin = 42;
        float width = mediaBox.getWidth() - 2 * margin;
        float startX = mediaBox.getLowerLeftX() + margin;
        float startY = mediaBox.getUpperRightY() - margin;
        List<String> emptyLine = new ArrayList<>();
        emptyLine.add(" ");

        // Drawing a rectangle.
        contentStream.addRect(startX, startY - 5, width, 1);

        // Begin text printing.
        contentStream.fill();
        contentStream.beginText();
        contentStream.setFont(PDType1Font.TIMES_ITALIC, fontSize);
        contentStream.newLineAtOffset(startX, startY);

        contentStream.showText("SCIM 2.0 Compliance Test Suite - Auto Generated Test Report");
        printResult(contentStream, leading, emptyLine);
        printResult(contentStream, leading, emptyLine);

        Color titleColor = new Color(102, 0, 153);
        Color dataColor = new Color(149, 69, 19);
        Color success = new Color(0, 102, 0);
        Color skipped = new Color(204, 204, 0);
        contentStream.setFont(PDType1Font.COURIER_BOLD, typeWriterFont);
        contentStream.setNonStrokingColor(titleColor);
        contentStream.showText("Summary : ");
        contentStream.setFont(PDType1Font.COURIER_BOLD, titleFont);
        printResult(contentStream, leading, emptyLine);
        printResult(contentStream, leading, emptyLine);

        contentStream.setFont(PDType1Font.COURIER_BOLD, fontSize);
        contentStream.setNonStrokingColor(success);
        contentStream.showText("Success Test cases : ");
        contentStream.setFont(PDType1Font.COURIER_BOLD, titleFont);
        contentStream.setNonStrokingColor(dataColor);
        printResult(contentStream, leading, emptyLine);
        contentStream.showText(Integer.toString(finalResults.getStatistics().getSuccess()));
        printResult(contentStream, leading, emptyLine);

        contentStream.setFont(PDType1Font.COURIER_BOLD, fontSize);
        contentStream.setNonStrokingColor(Color.RED);
        contentStream.showText("Failed Test cases : ");
        contentStream.setFont(PDType1Font.COURIER_BOLD, titleFont);
        contentStream.setNonStrokingColor(dataColor);
        printResult(contentStream, leading, emptyLine);
        contentStream.showText(Integer.toString(finalResults.getStatistics().getFailed()));
        printResult(contentStream, leading, emptyLine);

        contentStream.setFont(PDType1Font.COURIER_BOLD, fontSize);
        contentStream.setNonStrokingColor(skipped);
        contentStream.showText("Skipped Test cases : ");
        contentStream.setFont(PDType1Font.COURIER_BOLD, titleFont);
        contentStream.setNonStrokingColor(dataColor);
        printResult(contentStream, leading, emptyLine);
        contentStream.showText(Integer.toString(finalResults.getStatistics().getSkipped()));
        printResult(contentStream, leading, emptyLine);

        contentStream.setFont(PDType1Font.COURIER_BOLD, fontSize);
        contentStream.setNonStrokingColor(Color.BLACK);
        contentStream.showText("Total Test cases : ");
        contentStream.setFont(PDType1Font.COURIER_BOLD, titleFont);
        contentStream.setNonStrokingColor(dataColor);
        printResult(contentStream, leading, emptyLine);
        contentStream.showText(Integer.toString(finalResults.getStatistics().getTotal()));
        printResult(contentStream, leading, emptyLine);

        contentStream.setFont(PDType1Font.COURIER_BOLD, fontSize);
        contentStream.setNonStrokingColor(Color.BLACK);
        contentStream.showText("Time elapsed to run all Test cases(ms) : ");
        contentStream.setFont(PDType1Font.COURIER_BOLD, titleFont);
        contentStream.setNonStrokingColor(dataColor);
        printResult(contentStream, leading, emptyLine);
        contentStream.showText(Long.toString(finalResults.getStatistics().getTime()));
        printResult(contentStream, leading, emptyLine);

        // Ending the content stream.
        contentStream.endText();
        contentStream.close();

        // Save the document.
        Calendar cal = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat("HHmmss");
        String url = fullPath + "\\" + sdf.format(cal.getTime()) + ".pdf";
        document.save(new File(url));

        // Closing the document.
        document.close();
        return url;
    }

    /**
     * Print the results to PDF.
     *
     * @param contentStream Page contentStream.
     * @param leading       Leading in document.
     * @param lines         Empty line to add between text.
     * @throws IOException Exception is related to Input and Output operations.
     */
    public static void printResult(PDPageContentStream contentStream,
                                   float leading, List<String> lines)
            throws IOException {

        for (String line : lines) {
            contentStream.showText(line);
            contentStream.newLineAtOffset(0, -leading);
        }
    }

    /**
     * Method to separate lines of the PDF.
     *
     * @param text     Input stream of string.
     * @param fontSize General font size.
     * @param pdfFont  Font size to use in pdf.
     * @param width    Page width.
     * @return lines List of lines of text.
     * @throws IOException Exception is related to Input and Output operations.
     */
    private static List<String> getLines(String text, float fontSize, PDFont pdfFont, float width)
            throws IOException {

        width = width - 150;
        List<String> lines = new ArrayList<>();
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
                //if (lastSpace < 0)
                lastSpace = getCharacterCount(requiredSize, subString, pdfFont);
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
     * @param requiredSize Expected size.
     * @param subString    String to get character count.
     * @param pdfFont      Font use in pdf.
     * @return length Character count of string.
     * @throws IOException Exception is related to Input and Output operations.
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
     * @param test Stream of text.
     * @return textToBeShown Stream of text without unsupported characters.
     */
    private static ArrayList<String> removeUnsupportedCharacters(String test) {

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
