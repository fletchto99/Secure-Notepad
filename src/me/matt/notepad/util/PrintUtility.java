package me.matt.notepad.util;

import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.print.PageFormat;
import java.awt.print.Printable;
import java.awt.print.PrinterException;
import java.awt.print.PrinterJob;

import javax.swing.JOptionPane;
import javax.swing.JTextArea;

public class PrintUtility implements Printable {

    public static void print(final JTextArea jta) {
        try {
            new PrintUtility(jta.getFont(), jta.getText().trim()).execute();
        } catch (final PrinterException e) {
            JOptionPane.showMessageDialog(null,
                    "There was an error while printing");
        }
    }

    private final Font typeFont;
    private String[] body;
    private String text;

    private int[] pageBreaks;

    public PrintUtility(final Font font, final String text) {
        typeFont = font;
        this.text = text;
    }

    private void drawtabString(final Graphics g, final String text, int x,
            final int y) {
        for (final String line : text.split("\t")) {
            g.drawString(line, x += 36, y);
        }
    }

    public boolean execute() throws PrinterException {
        final PrinterJob job = PrinterJob.getPrinterJob();
        job.setPrintable(this);
        if (!job.printDialog()) {
            return false;
        }
        job.print();
        return true;
    }

    @Override
    public int print(final Graphics graphics, final PageFormat pageFormat,
            final int pageIndex) throws PrinterException {
        graphics.setFont(typeFont);
        final FontMetrics fm = graphics.getFontMetrics(typeFont);
        final int lineHeight = fm.getHeight();
        int charwidth = 0;

        for (int i = 0; i < text.length(); i++) {
            if (text.charAt(i) == '\n') {
                charwidth = 0;
            } else if (text.charAt(i) == '\t') {
                if (charwidth > (pageFormat.getImageableWidth() - 108)) {
                    text = text.substring(0, i) + "\r\n"
                            + text.substring(i, text.length());
                    charwidth = 0;
                } else {
                    charwidth += 36;
                }
            } else {
                if (charwidth > (pageFormat.getImageableWidth() - 108)) {
                    text = text.substring(0, i) + "\r\n"
                            + text.substring(i, text.length());
                    charwidth = 0;
                } else {
                    charwidth += fm.charWidth(text.charAt(i));
                }
            }
        }
        body = text.split("\\r?\\n");
        if (pageBreaks == null) {
            final int linesPerPage = (int) ((pageFormat.getImageableHeight() - 144) / lineHeight);
            final int numBreaks = (body.length - 1) / linesPerPage;
            pageBreaks = new int[numBreaks];
            for (int b = 0; b < numBreaks; b++) {
                pageBreaks[b] = (b + 1) * linesPerPage;
            }
        }

        if (pageIndex > pageBreaks.length) {
            return Printable.NO_SUCH_PAGE;
        }

        final Graphics2D g2d = (Graphics2D) graphics;
        g2d.translate(pageFormat.getImageableX(), pageFormat.getImageableY());

        int y = 72;
        final int start = (pageIndex == 0) ? 0 : pageBreaks[pageIndex - 1];
        final int end = (pageIndex == pageBreaks.length) ? body.length
                : pageBreaks[pageIndex];
        for (int line = start; line < end; line++) {
            y += lineHeight;
            this.drawtabString(graphics, body[line], 54, y);
        }
        return Printable.PAGE_EXISTS;
    }
}