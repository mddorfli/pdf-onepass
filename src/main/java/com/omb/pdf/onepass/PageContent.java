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

import java.util.ArrayList;
import java.util.List;

/**
 * The lines of one page; the generator moves the lines that do not fit to new pages.
 *
 * @author Marco Dorfliger
 */
public class PageContent {

    private List<LineContent> lines = new ArrayList<>();

    public void appendLine(LineContent line) {
        lines.add(line);
    }

    public List<LineContent> getLines() {
        return lines;
    }

    public void setLines(List<LineContent> lines) {
        this.lines = lines;
    }
}
