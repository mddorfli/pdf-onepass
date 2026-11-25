/*
 * Copyright 2026 Marco Dorfliger
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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Date;
import java.util.List;

import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.text.PDFTextStripper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/**
 * Tests of {@link PdfGenerator}.
 *
 * @author Marco Dorfliger
 */
class PdfGeneratorTest {

    /** a TrueType font set (Liberation Sans, SIL Open Font License, test resource only) */
    static final PdfFont LIBERATION_SANS = PdfFont.trueType(PdfGeneratorTest.class, "LiberationSans-Regular.ttf");
    static final FontSet SANS = new FontSet(LIBERATION_SANS, LIBERATION_SANS, LIBERATION_SANS, LIBERATION_SANS);

    private static byte[] generate(List<DocumentContent> contents, FontSet font, PdfFooter footer) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try (PdfGenerator generator = new PdfGenerator(contents, font, footer)) {
            generator.setPageInfo(PDRectangle.A5, PdfGenerator.MARGIN, PdfGenerator.FOOTER, PdfGenerator.LINE_PADDING);
            generator.calculatePlacementAndExpandLines();
            generator.generatePdfDocument(out);
        }
        return out.toByteArray();
    }

    @Test
    void wrapsTextBreaksPagesAndWritesFooterAndDocumentInfo() throws IOException {
        DocumentContent doc = new DocumentContent();
        doc.setTitle("Test title");
        doc.setAuthor("Test author");
        doc.setCreationDate(new Date(0));
        PageContent page = new PageContent();
        page.appendLine(new TextLineContent("Heading", FontSet.SERIF.bold(), 18, TextLineContent.Alignment.CENTER));
        page.appendLine(new FillRectangleContent(Color.GRAY, false, 2, true));
        page.appendLine(new VerticalSpaceLineContent(10, false));
        // far more than one A5 page: forces wrapping and page breaks
        String paragraph = "The quick brown fox jumps over the lazy dog. ".repeat(40);
        for (int i = 0; i < 10; i++) {
            page.appendLine(new TextLineContent("Paragraph " + i + ": " + paragraph, FontSet.SERIF.normal(), 10, TextLineContent.Alignment.LEFT));
        }
        doc.addPage(page);

        byte[] pdf = generate(List.of(doc), FontSet.SERIF, new PdfFooter("Made with ", "onepass", Color.BLUE, "right"));

        try (PDDocument document = Loader.loadPDF(pdf)) {
            assertTrue(document.getNumberOfPages() > 1, "page breaks");
            assertEquals("Test title", document.getDocumentInformation().getTitle());
            assertEquals("Test author", document.getDocumentInformation().getAuthor());
            String text = new PDFTextStripper().getText(document);
            assertTrue(text.contains("Heading"));
            assertTrue(text.contains("Paragraph 9"));
            assertTrue(text.contains("onepass"), "footer brand");
            PDFTextStripper firstPage = new PDFTextStripper();
            firstPage.setStartPage(1);
            firstPage.setEndPage(1);
            String[] lines = firstPage.getText(document).split("\\R");
            for (String line : lines) {
                assertTrue(line.length() < 120, "wrapped to the page width: " + line);
            }
        }
    }

    @Test
    void anyLineBreakStartsAParagraphAndUnprintableCharactersDoNotFailTheDocument() throws IOException {
        DocumentContent doc = new DocumentContent();
        PageContent page = new PageContent();
        // Windows and old Mac line breaks, a tab, an emoji the font has no glyph for, and a decomposed "č" (c + combining caron)
        page.appendLine(new TextLineContent("first\r\nsecond\rthird\tcolumn 🍝 pasta\nčevapi", SANS.normal(), 10,
                TextLineContent.Alignment.LEFT));
        doc.addPage(page);

        byte[] pdf = generate(List.of(doc), SANS, new PdfFooter("by\r", "brand", null, "right\u0007"));

        try (PDDocument document = Loader.loadPDF(pdf)) {
            List<String> lines = List.of(new PDFTextStripper().getText(document).split("\\R"));
            assertTrue(lines.contains("first"), "paragraphs: " + lines);
            assertTrue(lines.contains("second"), "paragraphs: " + lines);
            assertTrue(lines.contains("third column " + PdfGenerator.MISSING_GLYPH + " pasta"), "tab and emoji: " + lines);
            assertTrue(lines.contains("čevapi"), "composed: " + lines);
        }
    }

    @Test
    void standardFontsNeedNoFontFilesButOnlyCoverWesternEuropeanCharacters() throws IOException {
        DocumentContent doc = new DocumentContent();
        PageContent page = new PageContent();
        page.appendLine(new TextLineContent("Grüße, café — čevapi", FontSet.SANS_SERIF.normal(), 10, TextLineContent.Alignment.LEFT));
        doc.addPage(page);

        byte[] pdf = generate(List.of(doc), FontSet.SANS_SERIF, null);

        try (PDDocument document = Loader.loadPDF(pdf)) {
            String text = new PDFTextStripper().getText(document);
            assertTrue(text.contains("Grüße, café — " + PdfGenerator.MISSING_GLYPH + "evapi"), text);
            assertTrue(document.getPage(0).getResources().getFont(document.getPage(0).getResources().getFontNames().iterator().next()).isStandard14(),
                    "nothing embedded");
        }
    }

    @Test
    void aTrueTypeFontIsReadOnceAndEmbedsOnlyTheUsedGlyphsInEachDocument(@TempDir Path dir) throws IOException {
        Path file = dir.resolve("LiberationSans-Regular.ttf");
        try (InputStream in = PdfGeneratorTest.class.getResourceAsStream("LiberationSans-Regular.ttf")) {
            Files.copy(in, file);
        }
        long fontSize = Files.size(file);
        PdfFont font = PdfFont.trueType(file);
        FontSet set = new FontSet(font, font, font, font);
        for (int i = 0; i < 2; i++) {
            DocumentContent doc = new DocumentContent();
            PageContent page = new PageContent();
            page.appendLine(new TextLineContent("Čokoladne palačinke " + i, font, 12, TextLineContent.Alignment.LEFT));
            doc.addPage(page);

            byte[] pdf = generate(List.of(doc), set, new PdfFooter(null, null, null, "page"));

            try (PDDocument document = Loader.loadPDF(pdf)) {
                assertTrue(new PDFTextStripper().getText(document).contains("Čokoladne palačinke " + i));
            }
            assertTrue(pdf.length < fontSize / 4, "subset embedded: " + pdf.length + " bytes");
            Files.deleteIfExists(file); // the second document must not need the file any more
        }
    }

    @Test
    void eachDocumentContentStartsOnANewPage() throws IOException {
        DocumentContent first = new DocumentContent();
        PageContent p1 = new PageContent();
        p1.appendLine(new TextLineContent("first", FontSet.SANS_SERIF.normal(), 12, TextLineContent.Alignment.LEFT));
        first.addPage(p1);
        DocumentContent second = new DocumentContent();
        PageContent p2 = new PageContent();
        p2.appendLine(new TextLineContent("second", FontSet.SANS_SERIF.normal(), 12, TextLineContent.Alignment.LEFT));
        second.addPage(p2);

        byte[] pdf = generate(List.of(first, second), FontSet.SANS_SERIF, null);

        try (PDDocument document = Loader.loadPDF(pdf)) {
            assertEquals(2, document.getNumberOfPages());
        }
    }
}
