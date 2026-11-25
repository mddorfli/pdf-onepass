# pdf-onepass

A small layout library for [Apache PDFBox](https://pdfbox.apache.org/)™ 3, for documents that are built from top to
bottom in a single pass: you give it lines (text, images, filled bars, vertical space), it wraps the text to the
page width, breaks pages, places a footer on every page and writes the PDF. The jar is under 30 KB and bundles no
fonts: you use the standard PDF fonts, or pass your own TrueType fonts.

It has no tables, no flowing columns and no HTML/CSS. If you need those, look at
[ph-pdf-layout](https://github.com/phax/ph-pdf-layout) (fluid layout, tables) or
[easytable](https://github.com/vandeseer/easytable) (tables).

Java 21 or later. Dependencies: Apache PDFBox 3.0, Apache Commons Text (word wrapping), SLF4J API.

## Use

It is not on Maven Central. Build it with `mvn install` and depend on it:

```xml
<dependency>
  <groupId>io.github.mddorfli</groupId>
  <artifactId>pdf-onepass</artifactId>
  <version>1.0.0</version>
</dependency>
```

or let [JitPack](https://jitpack.io) build it from this repository (its coordinates are
`com.github.mddorfli:pdf-onepass:<tag>`).

```java
DocumentContent doc = new DocumentContent();          // one part; each part starts on a new page
doc.setTitle("Title");                                // the first part's title, author, date = PDF document info
PageContent page = new PageContent();
page.appendLine(new TextLineContent("Title", FontSet.SERIF.bold(), 18, TextLineContent.Alignment.CENTER));
page.appendLine(new FillRectangleContent(Color.GRAY, false, 2, true));
page.appendLine(new TextLineContent("A long paragraph ...", FontSet.SERIF.normal(), 10, TextLineContent.Alignment.LEFT));
doc.addPage(page);

try (PdfGenerator generator = new PdfGenerator(List.of(doc), FontSet.SERIF,
        new PdfFooter("Made with ", "onepass", Color.BLUE, "page footer"))) {
    generator.setPageInfo(PDRectangle.A4, PdfGenerator.MARGIN, PdfGenerator.FOOTER, PdfGenerator.LINE_PADDING);
    generator.calculatePlacementAndExpandLines();      // wraps lines and assigns positions and pages
    generator.generatePdfDocument(outputStream);
}
```

| Class | What |
| --- | --- |
| `PdfGenerator` | Lays out the contents and writes the PDF |
| `DocumentContent`, `PageContent` | A part of the document (starts on a new page) and its lines |
| `TextLineContent` | Text in a font, wrapped to the page width (unless `wrap` is false); a line break starts a new paragraph; left, centre or right aligned, optionally anchored at an x offset |
| `ImageContent` | An image (any format PDFBox reads), scaled to a height and an optional maximum width |
| `FillRectangleContent` | A filled bar across the page (rules, headers) |
| `VerticalSpaceLineContent` | Space |
| `PdfFooter` | The footer on every page: prefix, coloured brand (in italic), right-hand text |
| `PdfFont`, `FontSet` | A font, and a family of four (normal, bold, italic, bold italic) |

A line created with `doNotScroll` does not move down to a new line, so the element after it is drawn on the same
line (for example an amount at an x offset, followed by its description); `bottomPadded` adds the line padding
below a line.

## Fonts

`FontSet.SERIF`, `SANS_SERIF` and `MONOSPACED` are the standard PDF fonts Times, Helvetica and Courier. Every PDF
viewer has them, so nothing is embedded and no font file is needed; but they only cover Western European
characters. The first time a JVM uses them, PDFBox scans the system fonts once (seconds) and caches the result in
`~/.pdfbox.cache`.

For other languages, or another look, pass TrueType fonts:

```java
FontSet fonts = new FontSet(
        PdfFont.trueType(MyFonts.class, "fonts/MySans-Regular.ttf"),   // a class path resource
        PdfFont.trueType(MyFonts.class, "fonts/MySans-Bold.ttf"),
        PdfFont.trueType(Path.of("/usr/share/fonts/MySans-Italic.ttf")), // a file
        PdfFont.trueType("MySans-BoldItalic", () -> openFromDatabase()));  // anything else
```

Create them once and reuse them: a font file is read on first use and kept in memory, and each PDF embeds only the
glyphs it uses. Free fonts with wide coverage include the [Liberation fonts](https://github.com/liberationfonts)
(Latin, Greek, Cyrillic; SIL Open Font License) and [Noto](https://fonts.google.com/noto).

Text is drawn as far as the font allows: it is composed (Unicode NFC: a letter followed by a combining accent becomes
the accented letter), any line break (`\n`, `\r\n`, `\r`) starts a new paragraph, a tab becomes a space, other
control characters are dropped, and a character the font has no glyph for (an emoji, a script it does not cover)
is printed as `?` with a warning in the log, instead of failing the document. PDFBox does not shape text, so
scripts that need it (Arabic, Hebrew, Indic) do not print correctly even with a font that has their glyphs.

## Build

```
mvn install
```

## Licence

Copyright 2024-2026 Marco Dorfliger. Licensed under the [Apache License, Version 2.0](LICENSE); see also
[NOTICE](NOTICE).

Apache, Apache PDFBox and PDFBox are trademarks of The Apache Software Foundation. This project is not affiliated
with or endorsed by The Apache Software Foundation.

## History

Written in 2024 for the recipe PDFs of reciperocity.com and extracted from it as a library in 2026. See
[CHANGELOG.md](CHANGELOG.md).
