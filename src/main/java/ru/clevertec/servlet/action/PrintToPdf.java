package ru.clevertec.servlet.action;

import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfReader;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.borders.Border;
import com.itextpdf.layout.borders.SolidBorder;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;
import ru.clevertec.servlet.enities.Check;
import ru.clevertec.servlet.enities.Item;
import ru.clevertec.servlet.exception.ServerException;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.NumberFormat;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;

/**
 * Класс формирующий представление Check  в формате pdf
 * возможно наложение на заданную подложку (в pdf формате)
 */
public class PrintToPdf {
    private static NumberFormat numberFormat = NumberFormat.getInstance();
    private OutputStream outResource;
    private InputStream inResource;
    private String inFile;
    private float topMargin = 120;
    private int inputType = 0;

    public PrintToPdf(OutputStream outResource) {
        this.outResource = outResource;
    }

    public PrintToPdf(String inFile, OutputStream outResource) {
        this.outResource = outResource;
        this.inFile = inFile;
        inputType = 1;
    }

    public PrintToPdf(InputStream inResource, OutputStream outResource) {
        this.outResource = outResource;
        this.inResource = inResource;
        inputType = 2;
    }

    public void setTopMargin(float topMargin) {
        this.topMargin = topMargin;
    }

    /**
     * Метод формирующий check в pdf формате и выводящий его в заданный ресурс
     *
     * @param check - Check-объект для прдставления
     * @throws ServerException - в случае ошибки при чтении файла-подложки или при записи в заданный ресурс
     */
    public void createPdf(Check check) throws ServerException {
        PdfDocument pdfDoc;
        try {
            if (inputType == 0) {
                pdfDoc = new PdfDocument(new PdfWriter(outResource));
            } else if (inputType == 1) {
                pdfDoc = new PdfDocument(new PdfReader(inFile), new PdfWriter(outResource));
            } else {
                pdfDoc = new PdfDocument(new PdfReader(inResource), new PdfWriter(outResource));
            }
        } catch (IOException ex) {
            throw new ServerException("Can't get access to resources", ex);
        }
        Document doc = new Document(pdfDoc);
        doc.setTopMargin(topMargin);

        Paragraph p = new Paragraph(check.getName());
        doc.add(p);
        p = new Paragraph(check.getShop());
        doc.add(p);
        DateTimeFormatter formatter = DateTimeFormatter.ofLocalizedDateTime(FormatStyle.SHORT);
        p = new Paragraph(check.getTimeCreating().format(formatter)).setTextAlignment(TextAlignment.RIGHT);
        doc.add(p);

        Table table = new Table(UnitValue.createPercentArray(new float[]{40, 20, 40}));
        table.useAllAvailableWidth()
                .setBorderTop(new SolidBorder(1)).setBorderBottom(new SolidBorder(1))
                .setMarginTop(20);
        long totalDiscount = 0;
        long totalAmount = 0;
        for (Item item : check.getItems()) {
            int discount = item.getDiscount();
            int amount = item.getAmount();
            Cell cell = new Cell(1, 3)
                    .setTextAlignment(TextAlignment.LEFT)
                    .setBorder(Border.NO_BORDER)
                    .setMarginTop(10)
                    .add(new Paragraph(item.getProductName()));
            table.addCell(cell);
            table.addCell(getSimpleCell(convertMoneyToString(item.getPrice()), TextAlignment.RIGHT));
            table.addCell(getSimpleCell("x" + item.getQuantity(), TextAlignment.CENTER));
            table.addCell(getSimpleCell(convertMoneyToString(item.getAmount()), TextAlignment.RIGHT));
            totalAmount += amount;
            if (discount > 0) {
                cell = new Cell(1, 2)
                        .add(new Paragraph("Discount"))
                        .setBorder(Border.NO_BORDER)
                        .setTextAlignment(TextAlignment.CENTER);
                table.addCell(cell);
                table.addCell(getSimpleCell(convertMoneyToString(discount), TextAlignment.RIGHT));
                totalDiscount += discount;
            }
        }
        if (totalDiscount > 0) {
            Cell cell = new Cell(1, 2)
                    .setTextAlignment(TextAlignment.LEFT)
                    .add(new Paragraph("Total discount"))
                    .setMarginTop(20)
                    .setBorder(Border.NO_BORDER);
            table.addCell(cell);
            table.addCell(getSimpleCell(convertMoneyToString(totalDiscount), TextAlignment.RIGHT));
            totalAmount -= totalDiscount;
        }
        Cell cell = new Cell(1, 2)
                .setTextAlignment(TextAlignment.LEFT)
                .add(new Paragraph("Total amount"))
                .setBorder(Border.NO_BORDER);
        table.addCell(cell);
        table.addCell(getSimpleCell(convertMoneyToString(totalAmount), TextAlignment.RIGHT));
        doc.add(table);
        doc.close();
    }

    /**
     * Создать одиночную ячейку таблицы без внешней рамки
     *
     * @param text      - содержимое ячейки
     * @param alignment - выравнивание содержимого ячейки
     * @return - сформированная ячейка
     */
    private Cell getSimpleCell(Object text, TextAlignment alignment) {
        return new Cell(1, 1)
                .setTextAlignment(alignment)
                .add(new Paragraph(text.toString()))
                .setBorder(Border.NO_BORDER);
    }

    public static String convertMoneyToString(long val) {
        return numberFormat.format(val / 100.00);
    }
}
