/*
 * Copyright 2024-2026 Marco Dorfliger
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.omb.pdf.onepass;

import org.apache.commons.text.WordUtils;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDDocumentInformation;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.Color;
import java.io.IOException;
import java.io.OutputStream;
import java.text.Normalizer;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/**
 * Lays out {@link DocumentContent}s on pages and writes the PDF. Usage:
 * <pre>
 * try (PdfGenerator generator = new PdfGenerator(contents, FontSet.SANS_SERIF, footer)) {
 *     generator.setPageInfo(PDRectangle.A4, PdfGenerator.MARGIN, PdfGenerator.FOOTER, PdfGenerator.LINE_PADDING);
 *     generator.calculatePlacementAndExpandLines();
 *     generator.generatePdfDocument(out);
 * }
 * </pre>
 *
 * @author Marco Dorfliger
 */
public class PdfGenerator implements AutoCloseable {

    private static final Logger LOG = LoggerFactory.getLogger(PdfGenerator.class);

    public static final float MARGIN = 30;
    public static final float FOOTER = 40;
    public static final float LINE_PADDING = 6f;

    private PDRectangle pageSize = PDRectangle.A5;
    private float margin = MARGIN;
    private float footerMargin = FOOTER;
    private float linePadding = LINE_PADDING;

    private final Map<PdfFont, PDFont> fonts = new HashMap<>();
    private final Map<PdfFont, Map<Integer, Boolean>> glyphsByFont = new HashMap<>();

    /** what a character without a glyph in the font is printed as */
    public static final String MISSING_GLYPH = "?";

    private final List<DocumentContent> contents;
    private final PDDocument document;
    private final FontSet fontSet;
    private final PdfFooter footer;

    private int pageIdx;
    private int lineIdx;

    /**
     * @param fontSet the fonts of the footer (normal; the brand in italic)
     * @param footer  the footer on every page, or null for none
     */
    public PdfGenerator(List<DocumentContent> contents, FontSet fontSet, PdfFooter footer) {
        this.contents = Objects.requireNonNull(contents, "contents");
        this.fontSet = Objects.requireNonNull(fontSet, "fontSet");
        this.footer = footer;
        document = new PDDocument();
    }

    public void setPageInfo(PDRectangle pageSize, float sideMargin, float footerMargin, float linePadding) {
        this.pageSize = Objects.requireNonNull(pageSize, "pageSize");
        this.margin = sideMargin;
        this.footerMargin = footerMargin;
        this.linePadding = linePadding;
    }

    public PDRectangle getPageSize() {
        return pageSize;
    }

    public void calculatePlacementAndExpandLines() throws IOException {
        for (DocumentContent content : contents) {
            for (pageIdx = 0; pageIdx < content.getPages().size(); pageIdx++) {
                float y = pageSize.getHeight() - margin;
                lineIdx = 0;
                while (lineIdx < content.getPages().get(pageIdx).getLines().size()) {
                    LineContent line = content.getPages().get(pageIdx).getLines().get(lineIdx);
                    line.setY(y);
                    if (line instanceof TextLineContent textContent) {
                        y = expandTextLines(textContent, content);
                    } else {
                        y = addContentToPage(line, line.isDoNotScroll() ? y : y - line.getHeight() - (line.isBottomPadded() ? linePadding : 0f), content, true);
                    }
                }
            }
        }
    }

    private float expandTextLines(TextLineContent content, DocumentContent documentContent) throws IOException {
        float y = content.getY();
        if (content.getText() == null || content.getText().isEmpty()) {
            y = addContentToPage(content, y - (content.isDoNotScroll() ? 0 : content.getHeight()), documentContent, true);
        } else {
            // any line break (\n, \r\n, \r, ...) starts a new paragraph
            String[] paragraphsTexts = content.getText().split("\\R");
            for (int i = 0; i < paragraphsTexts.length; i++) {
                String paragraphText = printable(content.getFont(), paragraphsTexts[i]);
                String[] lineTexts;
                if (content.isWrap()) {
                    float avgCharWidth = getFont(content.getFont()).getStringWidth(paragraphText) / paragraphText.length() / 1000 * content.getFontSize();
                    float lineWidth = calculateLineWidth(content);
                    int charsToEol = (int) (lineWidth / avgCharWidth);
                    lineTexts = WordUtils.wrap(paragraphText, charsToEol, "\n", true).split("\n");
                } else {
                    lineTexts = new String[]{paragraphText};
                }
                for (int j = 0; j < lineTexts.length; j++) {
                    TextLineContent tlc = new TextLineContent(lineTexts[j], content.getFont(), content.getFontSize(), content.getAlignment(), content.getXOffset(), false, content.isDoNotScroll(), content.isBottomPadded());
                    tlc.setX(Optional.ofNullable(content.getX()).orElse(calculateX(tlc, tlc.getText())));
                    tlc.setY(y);
                    y = addContentToPage(tlc, y - (tlc.isDoNotScroll() ? 0 : tlc.getHeight()), documentContent, (i | j) == 0);
                }
            }
        }
        return y - (content.isBottomPadded() ? linePadding : 0);
    }

    public float calculateLineWidth(TextLineContent content) {
        float xOffset = Optional.ofNullable(content.getXOffset())
                .orElse(getDefaultXOffset(content.getAlignment()));
        return switch (content.getAlignment()) {
            case LEFT -> pageSize.getWidth() - margin - xOffset;
            case RIGHT -> xOffset - margin;
            case CENTER -> Math.min(xOffset - margin, pageSize.getWidth() - margin - xOffset) * 2f;
        };
    }

    private float calculateX(TextLineContent content, String text) throws IOException {
        float xOffset = Optional.ofNullable(content.getXOffset())
                .orElse(getDefaultXOffset(content.getAlignment()));

        return switch (content.getAlignment()) {
            case LEFT -> xOffset;
            case RIGHT -> xOffset - getWidth(content.getFont(), text, content.getFontSize());
            case CENTER -> xOffset - getWidth(content.getFont(), text, content.getFontSize()) / 2;
        };
    }

    private Float getDefaultXOffset(TextLineContent.Alignment alignment) {
        return switch (alignment) {
            case LEFT -> margin;
            case RIGHT -> pageSize.getWidth() - margin;
            case CENTER -> pageSize.getWidth() / 2;
        };
    }

    private float addContentToPage(LineContent content, float newY, DocumentContent documentContent, boolean replace) {
        PageContent currentPage = documentContent.getPages().get(pageIdx);
        if (newY < footerMargin) {
            // start a new page
            PageContent nextPage = new PageContent();
            documentContent.getPages().add(nextPage);
            pageIdx++;

            // move all remaining lines from the current index to next page
            nextPage.getLines().addAll(currentPage.getLines().subList(lineIdx, currentPage.getLines().size()));
            currentPage.setLines(currentPage.getLines().subList(0, lineIdx));

            newY = pageSize.getHeight() - margin;
            content.setY(newY);
            if (!content.isDoNotScroll()) {
                newY -= content.getHeight();
            }
            lineIdx = 0;

            currentPage = nextPage;
        }

        if (replace) {
            currentPage.getLines().set(lineIdx, content);
        } else {
            if (lineIdx < currentPage.getLines().size()) {
                currentPage.getLines().add(lineIdx, content);
            } else {
                currentPage.getLines().add(content);
            }
        }

        lineIdx++;
        return newY;
    }

    public void generatePdfDocument(OutputStream outStream) throws IOException {
        boolean firstPage = true;
        for (DocumentContent content : contents) {
            for (PageContent page : content.getPages()) {
                PDPage pdfPage = new PDPage(pageSize);
                document.addPage(pdfPage);

                if (firstPage) {
                    PDDocumentInformation info = new PDDocumentInformation();
                    info.setTitle(content.getTitle());
                    info.setAuthor(content.getAuthor());
                    if (content.getCreationDate() != null) {
                        Calendar cal = Calendar.getInstance();
                        cal.setTime(content.getCreationDate());
                        info.setCreationDate(cal);
                    }
                    document.setDocumentInformation(info);
                    firstPage = false;
                }

                try (PDPageContentStream cs = new PDPageContentStream(document, pdfPage, PDPageContentStream.AppendMode.APPEND, true, true)) {
                    for (LineContent line : page.getLines()) {
                        if (line instanceof TextLineContent textContent && textContent.getText() != null && !textContent.getText().isBlank()) {
                            cs.beginText();
                            cs.setFont(getFont(textContent.getFont()), textContent.getFontSize());
                            cs.newLineAtOffset(textContent.getX(), textContent.getY() - textContent.getFontSize());
                            LOG.debug("showText \"{}\" at ({}, {})", textContent.getText(), textContent.getX(), textContent.getY() - textContent.getFontSize());
                            cs.showText(textContent.getText());
                            cs.endText();

                            if (LOG.isTraceEnabled()) {
                                cs.addRect(line.getX(), line.getY() - textContent.getFontSize(), getWidth(textContent.getFont(), textContent.getText(), textContent.getFontSize()), textContent.getFontSize());
                                cs.stroke();
                            }

                        } else if (line instanceof ImageContent image) {
                            PDImageXObject pdfImage = PDImageXObject.createFromByteArray(document, image.getData(), image.getName());
                            float scale = image.getHeight() / pdfImage.getHeight();
                            float imgWidth = pdfImage.getWidth() * scale;
                            float imgHeight = image.getHeight();
                            if (image.getMaxWidth() != null && imgWidth > image.getMaxWidth()) {
                                imgWidth = image.getMaxWidth();
                                scale = image.getMaxWidth() / pdfImage.getWidth();
                                imgHeight = pdfImage.getHeight() * scale;
                            }
                            float x = getDefaultXOffset(TextLineContent.Alignment.CENTER) - (imgWidth / 2f);
                            LOG.debug("drawImage {} at ({}, {}) size {} x {}", image.getName(), x, image.getY() - imgHeight, imgWidth, imgHeight);
                            cs.drawImage(pdfImage, x, image.getY() - imgHeight, imgWidth, imgHeight);

                            if (LOG.isTraceEnabled()) {
                                cs.addRect(x, line.getY() - imgHeight, imgWidth, imgHeight);
                                cs.stroke();
                            }

                        } else if (line instanceof FillRectangleContent rect) {
                            cs.setNonStrokingColor(rect.getColour());
                            float x = margin;
                            float y = rect.getY() - rect.getHeight() - rect.getHeight() / 4;
                            float width = pageSize.getWidth() - margin * 2;
                            float height = rect.getHeight();
                            cs.addRect(x, y, width, height);
                            cs.fill();
                            cs.setNonStrokingColor(Color.BLACK);

                            if (LOG.isTraceEnabled()) {
                                cs.addRect(x, y, width, height);
                                cs.stroke();
                            }
                        }
                    }
                    addPageFooter(cs);
                }
            }
        }

        document.save(outStream);
        LOG.debug("PDF created");
    }

    private void addPageFooter(PDPageContentStream contentStream) throws IOException {
        if (footer == null) {
            return;
        }
        float y = margin / 2f;

        if (footer.prefix() != null || footer.brand() != null) {
            contentStream.beginText();
            contentStream.newLineAtOffset(margin, y);
            if (footer.prefix() != null) {
                contentStream.setFont(getFont(fontSet.normal()), 8);
                contentStream.showText(printable(fontSet.normal(), footer.prefix()));
            }
            if (footer.brand() != null) {
                contentStream.setFont(getFont(fontSet.italic()), 8);
                contentStream.setNonStrokingColor(Optional.ofNullable(footer.brandColour()).orElse(Color.BLACK));
                contentStream.showText(printable(fontSet.italic(), footer.brand()));
            }
            contentStream.endText();
        }

        if (footer.rightText() != null) {
            String rightText = printable(fontSet.normal(), footer.rightText());
            contentStream.beginText();
            contentStream.newLineAtOffset(getDefaultXOffset(TextLineContent.Alignment.RIGHT) - getWidth(fontSet.normal(), rightText, 8), y);
            contentStream.setFont(getFont(fontSet.normal()), 8);
            contentStream.setNonStrokingColor(Color.BLACK);
            contentStream.showText(rightText);
            contentStream.endText();
        }
    }

    /**
     * {@code text} as it can be drawn on one line in the font: composed (Unicode NFC, so a letter followed by a
     * combining accent becomes the accented letter, which the fonts have), tabs become spaces, other control characters
     * (such as a stray carriage return) are dropped, and characters the font has no glyph for (an emoji, a script it
     * does not cover) become {@value #MISSING_GLYPH}, instead of PDFBox failing the whole document.
     */
    private String printable(PdfFont font, String text) {
        if (text == null) {
            return null;
        }
        String composed = Normalizer.normalize(text, Normalizer.Form.NFC);
        StringBuilder sb = new StringBuilder(composed.length());
        composed.codePoints().forEach(cp -> {
            if (cp == '\t') {
                sb.append(' ');
            } else if (Character.isISOControl(cp)) {
                // dropped
            } else if (hasGlyph(font, cp)) {
                sb.appendCodePoint(cp);
            } else {
                sb.append(MISSING_GLYPH);
            }
        });
        return sb.toString();
    }

    private boolean hasGlyph(PdfFont font, int codePoint) {
        return glyphsByFont.computeIfAbsent(font, f -> new HashMap<>()).computeIfAbsent(codePoint, cp -> {
            try {
                getFont(font).encode(new String(Character.toChars(cp)));
                return true;
            } catch (IllegalArgumentException | IOException e) {
                LOG.warn("no glyph for U+{} in {}, printed as \"{}\"", String.format("%04X", cp), font, MISSING_GLYPH);
                return false;
            }
        });
    }

    /** Width of {@code text} in points. */
    public float getWidth(PdfFont font, String text, int fontSize) throws IOException {
        return getFont(font).getStringWidth(text) / 1000f * (float) fontSize;
    }

    private PDFont getFont(PdfFont font) {
        return fonts.computeIfAbsent(font, f -> PdfFont.loadUnchecked(f, document));
    }

    @Override
    public void close() throws IOException {
        document.close();
    }
}
