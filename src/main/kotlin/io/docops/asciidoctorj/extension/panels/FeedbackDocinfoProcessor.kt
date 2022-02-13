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

@Location(LocationType.HEADER)
class FeedbackDocinfoProcessor : DocinfoProcessor() {

    override fun process(document: Document): String? {
        val attributes = document.attributes
        val fb = attributes.getOrDefault("feedback", "NO") as String
        if (document.isBasebackend("html") && !"NO".equals(fb,true)) {
            val style = """
       
       <style>
        #slider {
            width: 500px;
            top: 100px;
            position: absolute
        }

        #fbheader {
            width: 260px;
            height: 520px;
            position: absolute;
            right: 0;
            border: 1px solid #d8d8d8;
            margin-left: 40px;
            padding: 20px 40px;
            border-radius: 3px;
            box-shadow: 0 0 8px gray;
            background: darkgray;
        }

        #sidebar {
            position: absolute;
            top: 180px;
            left: 113px;
            box-shadow: 0 0 8px gray
        }

        #sidebar1 {
            position: absolute;
            top: 180px;
            left: 113px;
            box-shadow: 0 0 8px gray
        }


        #submitter {
            background-color: white;
            width: 200px;
        }


        textarea {
            padding: 6px;
            font-size: 15px;
            border-radius: 2px;
            margin-top: 10px;
            height: 80px;
            width: 100%;
            background: #ffffff;
        }
        .error-message {
            color:#CC1111;
        }
        .error {
            box-shadow: 0 0 2px #CC1111;
        }
    </style>""".trimIndent()
    val script = """
        <script src="https://unpkg.com/htmx.org@1.6.1" ></script>
        <script src="https://unpkg.com/htmx.org/dist/ext/json-enc.js"></script>
        <script>
        var FeedBack = (function (my) {
            my.slideIn = function () {
                const slidingDiv = document.getElementById("slider");
                const stopPosition = -342;
                if (parseInt(slidingDiv.style.right) > stopPosition) {
                    slidingDiv.style.right = parseInt(slidingDiv.style.right) - 2 + "px";
                    setTimeout(my.slideIn, 1);
                }
            };
            my.close_panel = function () {
                my.slideIn();
                let a = document.getElementById("sidebar1");
                a.setAttribute("id", "sidebar");
                a.setAttribute("onclick", "FeedBack.open_panel()");
            };
            my.slideIt = function () {
                const slidingDiv = document.getElementById("slider");
                const stopPosition = 0;
                if (parseInt(slidingDiv.style.right) < stopPosition) {
                    slidingDiv.style.right = parseInt(slidingDiv.style.right) + 2 + "px";
                    setTimeout(FeedBack.slideIt, 1);
                }
            };
             my.open_panel = function () {
                my.slideIt();
                let a = document.getElementById("sidebar");
                a.setAttribute("id", "sidebar1");
                a.setAttribute("onclick", "FeedBack.close_panel()");
            };
             
            return my;
        }(FeedBack || {}));

    </script>
            """.trimIndent()

            return style+script
        } else {
            return null
        }
    }
}