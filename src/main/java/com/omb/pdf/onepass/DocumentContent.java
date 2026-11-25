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
import java.util.Date;
import java.util.List;

/**
 * One part of a PDF (e.g. one chapter or record) that starts on a new page. The title, author and creation date of the first
 * part become the PDF document information.
 *
 * @author Marco Dorfliger
 */
public class DocumentContent {

    private String title;
    private String author;
    private Date creationDate;
    private List<PageContent> pages = new ArrayList<>();

    public void addPage(PageContent page) {
        pages.add(page);
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public Date getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(Date creationDate) {
        this.creationDate = creationDate;
    }

    public List<PageContent> getPages() {
        return pages;
    }

    public void setPages(List<PageContent> pages) {
        this.pages = pages;
    }
}
