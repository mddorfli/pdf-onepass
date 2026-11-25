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

import java.awt.Color;

/**
 * A filled bar across the page width (between the margins), e.g. to stripe table rows.
 *
 * @author Marco Dorfliger
 */
public class FillRectangleContent extends LineContent {

    private final Color colour;

    public FillRectangleContent(Color colour, boolean doNotScroll, float lineHeight, boolean bottomPadded) {
        super(doNotScroll, lineHeight, bottomPadded);
        this.colour = colour;
    }

    public Color getColour() {
        return colour;
    }
}
