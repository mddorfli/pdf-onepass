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
 * An image (PNG, JPEG, ...), centred, scaled to {@code height} and at most {@code maxWidth} wide.
 *
 * @author Marco Dorfliger
 */
public class ImageContent extends LineContent {

    private final String name;
    private final byte[] data;
    private final Float maxWidth;

    public ImageContent(byte[] data, Float maxWidth, float height, String name, boolean doNotScroll, boolean bottomPadded) {
        super(doNotScroll, height, bottomPadded);
        this.data = data;
        this.maxWidth = maxWidth;
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public byte[] getData() {
        return data;
    }

    public Float getMaxWidth() {
        return maxWidth;
    }
}
