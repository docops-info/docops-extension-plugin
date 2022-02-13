/*
 * Copyright 2020 The DocOps Consortium
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

package io.docops.asciidoctorj.extension.panels

import org.asciidoctor.ast.Document
import org.asciidoctor.extension.DocinfoProcessor
import org.asciidoctor.extension.Location
import org.asciidoctor.extension.LocationType
import java.util.*



@Location(LocationType.FOOTER)
class FeedbackDivDocInfoProcessor : DocinfoProcessor() {

    companion object {
        private const val FEEDBACK = "feedback"
        private const val NO = "NO"

    }
    override fun process(document: Document): String? {
        val attributes = document.attributes
        val fb = attributes.getOrDefault(FEEDBACK, NO) as String
        if (document.isBasebackend("html") && !NO.equals(fb, true)) {
            val png = FeedbackDivDocInfoProcessor::class.java.classLoader.getResourceAsStream("feedback.png")
            png?.let {
                val enc = Base64.getEncoder().encodeToString(png.readAllBytes())
                return """
                <div id="slider" style="right:-342px;">
                    <div id="sidebar" onclick="FeedBack.open_panel()"><img src="data:image/png;base64,$enc" alt="feedback"></div>
                    <div id="fbheader">
                        <form data-hx-post="savefeedback"
                              method="post" data-hx-ext='json-enc'>
                            <h2>Contact Form</h2>
                            <p>Please provide feedback</p>
                            <label>
                                <input name="submitter" id="submitter" type="text" value="" placeholder="Your Name" required>
                            </label>
                            <h4>Document</h4>
                            <label>
                                <input id="docName" name="docName" type="text" value="${document.getAttribute("docfile")}" readonly>
                            </label>
                            <label>
                                <textarea id="message" name="message" rows="10" placeholder="Message" required></textarea>
                            </label>
                            <button>Submit</button>
                        </form>
                    </div>
                </div>
            """.trimIndent()
            }
            return null
        } else {
            return null
        }
    }
}