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

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.font.PDType0Font;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;

/**
 * A font to write text with: either one of the 14 standard PDF fonts, which every PDF viewer has (nothing is
 * embedded, but they only cover Western European characters), or a TrueType font the caller supplies (only the
 * glyphs a document uses are embedded in it). Instances are immutable and meant to be created once and reused for
 * every document; a TrueType font's file is read once, on first use.
 *
 * @author Marco Dorfliger
 */
public final class PdfFont {

    /** reads the TrueType font file */
    @FunctionalInterface
    public interface Source {
        InputStream open() throws IOException;
    }

    private final String name;
    private final Standard14Fonts.FontName standard;
    private final Source source;
    private volatile byte[] data;

    private PdfFont(String name, Standard14Fonts.FontName standard, Source source) {
        this.name = Objects.requireNonNull(name, "name");
        this.standard = standard;
        this.source = source;
    }

    /** One of the 14 standard PDF fonts (Times, Helvetica, Courier, Symbol, ZapfDingbats). */
    public static PdfFont standard(Standard14Fonts.FontName font) {
        Objects.requireNonNull(font, "font");
        return new PdfFont(font.getName(), font, null);
    }

    /** A TrueType font file. */
    public static PdfFont trueType(Path file) {
        Objects.requireNonNull(file, "file");
        return new PdfFont(file.getFileName().toString(), null, () -> Files.newInputStream(file));
    }

    /**
     * A TrueType font on the class path, e.g. {@code trueType(MyFonts.class, "fonts/MyFont-Regular.ttf")}.
     *
     * @param anchor   the class whose {@link Class#getResourceAsStream(String)} finds the resource
     * @param resource the resource name, relative to {@code anchor}'s package or absolute ("/...")
     */
    public static PdfFont trueType(Class<?> anchor, String resource) {
        Objects.requireNonNull(anchor, "anchor");
        Objects.requireNonNull(resource, "resource");
        return new PdfFont(resource.substring(resource.lastIndexOf('/') + 1), null, () -> {
            InputStream in = anchor.getResourceAsStream(resource);
            if (in == null) {
                throw new IOException("font resource not found: " + resource + " (relative to " + anchor.getName() + ")");
            }
            return in;
        });
    }

    /** A TrueType font from any source, e.g. a database; {@code name} is used in log messages. */
    public static PdfFont trueType(String name, Source source) {
        return new PdfFont(name, null, Objects.requireNonNull(source, "source"));
    }

    /** @return the font's name: the standard font's name, or the TrueType font's file name */
    public String getName() {
        return name;
    }

    /** @return true for one of the 14 standard PDF fonts */
    public boolean isStandard() {
        return standard != null;
    }

    /** the font for one document */
    PDFont load(PDDocument document) throws IOException {
        if (standard != null) {
            return new PDType1Font(standard);
        }
        return PDType0Font.load(document, new ByteArrayInputStream(data()));
    }

    private byte[] data() throws IOException {
        byte[] d = data;
        if (d == null) {
            synchronized (this) {
                d = data;
                if (d == null) {
                    try (InputStream in = source.open()) {
                        d = in.readAllBytes();
                    }
                    data = d;
                }
            }
        }
        return d;
    }

    @Override
    public String toString() {
        return name;
    }

    /** loads a font, for {@link java.util.Map#computeIfAbsent} */
    static PDFont loadUnchecked(PdfFont font, PDDocument document) {
        try {
            return font.load(document);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
