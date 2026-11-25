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
 * The footer on every page: on the left {@code prefix} followed by {@code brand} (in the font set's italic font, in
 * {@code brandColour}), on the right {@code rightText}. Any part may be null.
 *
 * @author Marco Dorfliger
 */
public record PdfFooter(String prefix, String brand, Color brandColour, String rightText) {
}
