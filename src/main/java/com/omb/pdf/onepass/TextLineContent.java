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

/**
 * A text, wrapped to the page width unless {@code wrap} is false; "\n" starts a new paragraph.
 *
 * @author Marco Dorfliger
 */
public class TextLineContent extends LineContent {

    public enum Alignment {
        LEFT, CENTER, RIGHT
    }

    private final Float xOffset;
    private final String text;
    private final PdfFont font;
    private final int fontSize;
    private final Alignment alignment;
    private final boolean wrap;

    public TextLineContent(final String text, final PdfFont font, final int fontSize, final Alignment alignment) {
        this(text, font, fontSize, alignment, null, true, false, true);
    }

    /**
     * @param font    the font, e.g. from a {@link FontSet}
     * @param xOffset anchor of the alignment (left edge, right edge or centre); null = page margin or centre
     */
    public TextLineContent(final String text, final PdfFont font, final int fontSize, final Alignment alignment, Float xOffset, boolean wrap, boolean doNotScroll, boolean bottomPadded) {
        super(doNotScroll, fontSize, bottomPadded);
        this.xOffset = xOffset;
        this.text = text;
        this.font = font;
        this.fontSize = fontSize;
        this.alignment = alignment;
        this.wrap = wrap;
    }

    public Float getXOffset() {
        return xOffset;
    }

    public String getText() {
        return text;
    }

    public PdfFont getFont() {
        return font;
    }

    public int getFontSize() {
        return fontSize;
    }

    public Alignment getAlignment() {
        return alignment;
    }

    public boolean isWrap() {
        return wrap;
    }
}
