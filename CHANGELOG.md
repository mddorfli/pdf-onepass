# Changelog

## 1.0.0 (unreleased)

First public version, extracted from the PDF code of reciperocity.com.

- Layout engine (`PdfGenerator`) with text, image, filled-bar and space lines, word wrapping, page breaks and a
  page footer.
- Fonts: the standard PDF fonts (`FontSet.SERIF`, `SANS_SERIF`, `MONOSPACED`), or TrueType fonts the caller
  supplies (`PdfFont.trueType`: file, class path resource or any source), read once and subset-embedded. No fonts
  are bundled.
- Text is drawn as far as the font allows: composed to NFC, any line break starts a paragraph, tabs become spaces,
  other control characters are dropped, characters without a glyph print as `?` instead of failing the document.
- Apache License 2.0.
