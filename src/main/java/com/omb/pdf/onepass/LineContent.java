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
 * One positioned element of a page. The generator assigns {@code y} (and for text {@code x}) while laying out the
 * pages; elements created with {@code doNotScroll} share the line of the element after them.
 *
 * @author Marco Dorfliger
 */
public abstract class LineContent {

    protected final float height;
    protected final boolean bottomPadded;
    protected final boolean doNotScroll;
    protected Float x, y;

    protected LineContent(boolean doNotScroll, float height, boolean bottomPadded) {
        this.doNotScroll = doNotScroll;
        this.height = height;
        this.bottomPadded = bottomPadded;
    }

    public float getHeight() {
        return height;
    }

    public boolean isBottomPadded() {
        return bottomPadded;
    }

    public boolean isDoNotScroll() {
        return doNotScroll;
    }

    public Float getX() {
        return x;
    }

    public void setX(Float x) {
        this.x = x;
    }

    public Float getY() {
        return y;
    }

    public void setY(Float y) {
        this.y = y;
    }
}
