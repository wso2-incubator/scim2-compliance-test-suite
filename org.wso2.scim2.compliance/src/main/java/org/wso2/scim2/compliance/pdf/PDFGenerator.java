/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.wso2.scim2.compliance.pdf;

import org.wso2.scim2.compliance.entities.Result;
import org.wso2.scim2.compliance.entities.TestResult;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.encoding.WinAnsiEncoding;

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
            //Creating a blank page
            PDPage blankPage = new PDPage();
            //Adding the blank page to the document
            document.addPage(blankPage);

        } else {
            for (int i = 0; i < finalResults.getResults().size(); i++) {
                //Creating a blank page
                PDPage blankPage = new PDPage();
                //Adding the blank page to the document
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
    public static String GeneratePDFResults(Result finalResults, String fullPath) throws IOException {

        init(finalResults);
        int pageNo = 0;
        for (TestResult testResult : finalResults.getResults()) {
            //Retrieving the pages of the document
            PDPage page = document.getPage(pageNo);
            PDPageContentStream contentStream = new PDPageContentStream(document, page);

            PDFont pdfFont = PDType1Font.HELVETICA;
            float fontSize = 10;
            float smallFontSize = 8;
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

            contentStream.setFont(PDType1Font.HELVETICA_BOLD_OBLIQUE, fontSize);
            contentStream.showText("Test Case Name : ");
            contentStream.setFont(PDType1Font.COURIER, fontSize);
            printResult(contentStream, fontSize, pdfFont, leading, startX, startY, emptyLine);
            printResult(contentStream, fontSize, pdfFont, leading, startX, startY, testName);

            contentStream.setFont(PDType1Font.HELVETICA_BOLD_OBLIQUE, fontSize);
            contentStream.showText("Test Case Errors : ");
            contentStream.setFont(PDType1Font.COURIER, fontSize);
            printResult(contentStream, fontSize, pdfFont, leading, startX, startY, emptyLine);
            printResult(contentStream, fontSize, pdfFont, leading, startX, startY, testMessage);

            contentStream.setFont(PDType1Font.HELVETICA_BOLD_OBLIQUE, fontSize);
            contentStream.showText("Test Case Status : ");
            contentStream.setFont(PDType1Font.COURIER, fontSize);
            printResult(contentStream, fontSize, pdfFont, leading, startX, startY, emptyLine);
            printResult(contentStream, fontSize, pdfFont, leading, startX, startY, testLabel);

            contentStream.setFont(PDType1Font.HELVETICA_BOLD_OBLIQUE, fontSize);
            contentStream.showText(("To Server : "));
            contentStream.setFont(PDType1Font.COURIER, fontSize);
            printResult(contentStream, fontSize, pdfFont, leading, startX, startY, emptyLine);
            printResult(contentStream, smallFontSize, pdfFont, leading, startX, startY, toServer);
            printResult(contentStream, fontSize, pdfFont, leading, startX, startY, requestBody);

            contentStream.setFont(PDType1Font.HELVETICA_BOLD_OBLIQUE, fontSize);
            contentStream.showText("From Server : ");
            contentStream.setFont(PDType1Font.COURIER, fontSize);
            printResult(contentStream, smallFontSize, pdfFont, leading, startX, startY, fromServer);
            printResult(contentStream, fontSize, pdfFont, leading, startX, startY, responseBody);

            contentStream.setFont(PDType1Font.HELVETICA_BOLD_OBLIQUE, fontSize);
            contentStream.showText("Sub Tests Performed : ");
            contentStream.setFont(PDType1Font.COURIER, fontSize);
            printResult(contentStream, fontSize, pdfFont, leading, startX, startY, emptyLine);
            printResult(contentStream, fontSize, pdfFont, leading, startX, startY, subTests);

            //Ending the content stream
            contentStream.endText();
            contentStream.close();
            pageNo++;
        }
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
            if (spaceIndex < 0)
                spaceIndex = text.length();
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