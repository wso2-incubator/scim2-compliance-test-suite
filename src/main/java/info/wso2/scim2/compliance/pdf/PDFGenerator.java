package info.wso2.scim2.compliance.pdf;

import info.wso2.scim2.compliance.entities.Result;
import info.wso2.scim2.compliance.entities.TestResult;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.encoding.WinAnsiEncoding;


import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;

public class PDFGenerator {

    private static PDDocument document = null;

    private static void init(Result finalResults){
        document = new PDDocument();
        if (finalResults.getErrorMessage() != null){
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
    public static void GeneratePDFResults(Result finalResults) throws IOException {
        init(finalResults);
        int pageNo = 0;
        for (TestResult testResult : finalResults.getResults()){
            //Retrieving the pages of the document
            PDPage page = document.getPage(pageNo);
            PDPageContentStream contentStream = new PDPageContentStream(document, page);

            //Begin the Content stream
            contentStream.beginText();
            //Setting the font to the Content stream
            contentStream.setFont(PDType1Font.TIMES_ROMAN, 14);
            // Move to the start of the next line, offset from the start of the
            // current line by (150, 700).
            contentStream.newLineAtOffset(150, 700);
            // Shows the given text at the location specified by the current
            // text matrix.
            contentStream.showText("SCIM 2.0 Compliance Test Suit Results");
            //Setting the font to the Content stream
            contentStream.setFont(PDType1Font.TIMES_ROMAN, 10);
            contentStream.newLine();
            contentStream.newLine();
            //Ending the content stream
            contentStream.endText();


            //Setting the non stroking color
            contentStream.setNonStrokingColor(Color.DARK_GRAY);
            //Drawing a rectangle
            contentStream.addRect(25, 800, 200, 1);
            //Drawing a rectangle
            contentStream.fill();

            //Begin the Content stream
            contentStream.beginText();
            //Setting the leading
            contentStream.setLeading(14.5f);
            //Setting the position for the line
            contentStream.newLineAtOffset(25, 1000);

            String text = testResult.getWire().getToServer();
            ArrayList<String> textToBeShown = removeUnsupportedCharacters(text);
            for (String textItem : textToBeShown) {
                //Adding text in the form of string
                contentStream.showText(textItem);
                contentStream.newLine();
            }
            //Ending the content stream
            contentStream.endText();

            //Closing the content stream
            contentStream.close();
            pageNo ++;
        }
        //save the document
        Calendar cal = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat("HHmmss");
        document.save(new File( "C:\\Users\\User\\Desktop\\SCIM-2.0-Complience-Test-Suite\\results\\"+
                sdf.format(cal.getTime())+ ".pdf" ));

        //Closing the document
        document.close();

    }

    private static ArrayList<String> removeUnsupportedCharacters(String test) throws IOException {
        ArrayList<String> textToBeShown= new ArrayList<>();
        StringBuilder b = new StringBuilder();
        for (int i = 0; i < test.length(); i++) {
            if (WinAnsiEncoding.INSTANCE.contains(test.charAt(i)) ) {
                b.append(test.charAt(i));
               // float width = PDType1Font.TIMES_ROMAN.getStringWidth(b.toString()) / 1000 * 10;
            } else {
                textToBeShown.add(b.toString());
                b = new StringBuilder();
            }
        }
        textToBeShown.add(b.toString());
        return textToBeShown;
    }


}