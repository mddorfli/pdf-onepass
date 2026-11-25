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

import java.util.Objects;

import org.apache.pdfbox.pdmodel.font.Standard14Fonts.FontName;

/**
 * A font family: the fonts for normal, bold, italic and bold italic text. {@link PdfGenerator} writes the footer
 * with it; the lines name their own fonts.
 * <p>
 * The constants are the standard PDF fonts, which need no font files but only cover Western European characters
 * (others print as {@value PdfGenerator#MISSING_GLYPH}). For other languages, make a font set of TrueType fonts:
 * <pre>
 * FontSet sans = new FontSet(
 *         PdfFont.trueType(MyFonts.class, "fonts/MySans-Regular.ttf"),
 *         PdfFont.trueType(MyFonts.class, "fonts/MySans-Bold.ttf"),
 *         PdfFont.trueType(MyFonts.class, "fonts/MySans-Italic.ttf"),
 *         PdfFont.trueType(MyFonts.class, "fonts/MySans-BoldItalic.ttf"));
 * </pre>
 *
 * @author Marco Dorfliger
 */
public record FontSet(PdfFont normal, PdfFont bold, PdfFont italic, PdfFont boldItalic) {

    /** Times (standard PDF font) */
    public static final FontSet SERIF = new FontSet(PdfFont.standard(FontName.TIMES_ROMAN),
            PdfFont.standard(FontName.TIMES_BOLD), PdfFont.standard(FontName.TIMES_ITALIC),
            PdfFont.standard(FontName.TIMES_BOLD_ITALIC));

    /** Helvetica (standard PDF font) */
    public static final FontSet SANS_SERIF = new FontSet(PdfFont.standard(FontName.HELVETICA),
            PdfFont.standard(FontName.HELVETICA_BOLD), PdfFont.standard(FontName.HELVETICA_OBLIQUE),
            PdfFont.standard(FontName.HELVETICA_BOLD_OBLIQUE));

    /** Courier (standard PDF font) */
    public static final FontSet MONOSPACED = new FontSet(PdfFont.standard(FontName.COURIER),
            PdfFont.standard(FontName.COURIER_BOLD), PdfFont.standard(FontName.COURIER_OBLIQUE),
            PdfFont.standard(FontName.COURIER_BOLD_OBLIQUE));

    public FontSet {
        Objects.requireNonNull(normal, "normal");
        Objects.requireNonNull(bold, "bold");
        Objects.requireNonNull(italic, "italic");
        Objects.requireNonNull(boldItalic, "boldItalic");
    }
}
