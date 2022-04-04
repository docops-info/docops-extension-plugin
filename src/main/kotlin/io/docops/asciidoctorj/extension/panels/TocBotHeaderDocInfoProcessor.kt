package io.docops.asciidoctorj.extension.panels

import org.asciidoctor.ast.Document
import org.asciidoctor.extension.DocinfoProcessor
import org.asciidoctor.extension.Location
import org.asciidoctor.extension.LocationType

@Location(LocationType.HEADER)
class TocBotHeaderDocInfoProcessor: DocinfoProcessor() {
    override fun process(document: Document): String {
        val attributes = document.attributes
        val tb = attributes.getOrDefault("tocbot", "NO") as String
        if (!document.isBasebackend("html") || "NO".equals(tb,true)) {
            return ""
        }
        return """
            <style>
              /* Tocbot dynamic TOC, works with tocbot 3.0.2 */
              /* Source: https://github.com/asciidoctor/asciidoctor/issues/699#issuecomment-321066006 */
              #tocbot a.toc-link.node-name--H1{ font-style: italic }
              @media screen{
                #tocbot > ul.toc-list{ margin-bottom: 0.5em; margin-left: 0.125em }
                #tocbot ul.sectlevel0, #tocbot a.toc-link.node-name--H1 + ul{
                  padding-left: 0 }
                #tocbot a.toc-link{ height:100% }
                .is-collapsible{ max-height:3000px; overflow:hidden; }
                .is-collapsed{ max-height:0 }
                .is-active-link{ font-weight:700; color: #00fa00 }
              }
              @media print{
                #tocbot a.toc-link.node-name--H4{ display:none }
              }
              .toc{overflow-y:auto}.toc>.toc-list{overflow:hidden;position:relative}.toc>.toc-list li{list-style:none}.toc-list{margin:0;padding-left:10px}a.toc-link{color:currentColor;height:100%}.is-collapsible{max-height:1000px;overflow:hidden;transition:all 300ms ease-in-out}.is-collapsed{max-height:0}.is-position-fixed{position:fixed !important;top:0}.is-active-link{font-weight:700}.toc-link::before{background-color:#EEE;content:' ';display:inline-block;height:inherit;left:0;margin-top:-12px;position:absolute;width:2px}.is-active-link::before{background-color:#54BC4B}
              @media only screen and (min-width: 993px) {
                .sticky {
                      position: -webkit-sticky;
                      position: sticky;
                      top: 0;
                      align-self: flex-start;
                  }
              }
              @media only screen and (max-width: 992px) {
                .is-collapsed {
                  max-height: none;
                }
              }
              .admin-bar .sticky {
                  top: 32px;
              }
              .sticky:before,
              .sticky:after {
                  content: '';
                  display: table;
              }
              a.toc-link {
                  padding: 1px 0;
                  display: block;
                  line-height: 1.0;
              }
              a.toc-link:hover {
                  color: #54bc4b;
              }
              .is-collapsible a.toc-link {
                  padding: 5px 0;
              }
              a.toc-link {
                  line-height: 1.4;
              }
            </style>
        """.trimIndent()
    }
}