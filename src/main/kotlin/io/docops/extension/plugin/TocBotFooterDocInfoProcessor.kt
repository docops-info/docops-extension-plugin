package io.docops.extension.plugin



import org.asciidoctor.ast.Document
import org.asciidoctor.extension.DocinfoProcessor
import org.asciidoctor.extension.Location
import org.asciidoctor.extension.LocationType

@Location(LocationType.FOOTER)
class TocBotFooterDocInfoProcessor: DocinfoProcessor() {
    override fun process(document: Document): String {
        val attributes = document.attributes
        val tb = attributes.getOrDefault("tocbot", "NO") as String
        if (!document.isBasebackend("html") || "NO".equals(tb,true)) {
            return ""
        }
        /* Tocbot dynamic TOC, works with tocbot 3.0.2 */
        // language=HTML
        return """
            <script>!function(e){function t(o){if(n[o])return n[o].exports;var i=n[o]={i:o,l:!1,exports:{}};return e[o].call(i.exports,i,i.exports,t),i.l=!0,i.exports}var n={};t.m=e,t.c=n,t.i=function(e){return e},t.d=function(e,n,o){t.o(e,n)||Object.defineProperty(e,n,{configurable:!1,enumerable:!0,get:o})},t.n=function(e){var n=e&&e.__esModule?function(){return e.default}:function(){return e};return t.d(n,"a",n),n},t.o=function(e,t){return Object.prototype.hasOwnProperty.call(e,t)},t.p="",t(t.s=5)}([function(e,t){var n;n=function(){return this}();try{n=n||Function("return this")()||(0,eval)("this")}catch(e){"object"==typeof window&&(n=window)}e.exports=n},function(e,t,n){var o,i,r;!function(n,l){i=[],o=l(),void 0!==(r="function"==typeof o?o.apply(t,i):o)&&(e.exports=r)}(0,function(){"use strict";var e=function(e){return"getComputedStyle"in window&&"smooth"===window.getComputedStyle(e)["scroll-behavior"]};if("undefined"==typeof window||!("document"in window))return{};var t=function(t,n,o){n=n||999,o||0===o||(o=9);var i,r=function(e){i=e},l=function(){clearTimeout(i),r(0)},s=function(e){return Math.max(0,t.getTopOf(e)-o)},c=function(o,i,s){if(l(),0===i||i&&i<0||e(t.body))t.toY(o),s&&s();else{var c=t.getY(),a=Math.max(0,o)-c,u=(new Date).getTime();i=i||Math.min(Math.abs(a),n),function e(){r(setTimeout(function(){var n=Math.min(1,((new Date).getTime()-u)/i),o=Math.max(0,Math.floor(c+a*(n<.5?2*n*n:n*(4-2*n)-1)));t.toY(o),n<1&&t.getHeight()+o<t.body.scrollHeight?e():(setTimeout(l,99),s&&s())},9))}()}},a=function(e,t,n){c(s(e),t,n)},u=function(e,n,i){var r=e.getBoundingClientRect().height,l=t.getTopOf(e)+r,u=t.getHeight(),d=t.getY(),f=d+u;s(e)<d||r+o>u?a(e,n,i):l+o>f?c(l-u+o,n,i):i&&i()},d=function(e,n,o,i){c(Math.max(0,t.getTopOf(e)-t.getHeight()/2+(o||e.getBoundingClientRect().height/2)),n,i)};return{setup:function(e,t){return(0===e||e)&&(n=e),(0===t||t)&&(o=t),{defaultDuration:n,edgeOffset:o}},to:a,toY:c,intoView:u,center:d,stop:l,moving:function(){return!!i},getY:t.getY,getTopOf:t.getTopOf}},n=document.documentElement,o=function(){return window.scrollY||n.scrollTop},i=t({body:document.scrollingElement||document.body,toY:function(e){window.scrollTo(0,e)},getY:o,getHeight:function(){return window.innerHeight||n.clientHeight},getTopOf:function(e){return e.getBoundingClientRect().top+o()-n.offsetTop}});if(i.createScroller=function(e,o,i){return t({body:e,toY:function(t){e.scrollTop=t},getY:function(){return e.scrollTop},getHeight:function(){return Math.min(e.clientHeight,window.innerHeight||n.clientHeight)},getTopOf:function(e){return e.offsetTop}},o,i)},"addEventListener"in window&&!window.noZensmooth&&!e(document.body)){var r="scrollRestoration"in history;r&&(history.scrollRestoration="auto"),window.addEventListener("load",function(){r&&(setTimeout(function(){history.scrollRestoration="manual"},9),window.addEventListener("popstate",function(e){e.state&&"zenscrollY"in e.state&&i.toY(e.state.zenscrollY)},!1)),window.location.hash&&setTimeout(function(){var e=i.setup().edgeOffset;if(e){var t=document.getElementById(window.location.href.split("#")[1]);if(t){var n=Math.max(0,i.getTopOf(t)-e),o=i.getY()-n;0<=o&&o<9&&window.scrollTo(0,n)}}},9)},!1);var l=new RegExp("(^|\\s)noZensmooth(\\s|${'$'})");window.addEventListener("click",function(e){for(var t=e.target;t&&"A"!==t.tagName;)t=t.parentNode;if(!(!t||1!==e.which||e.shiftKey||e.metaKey||e.ctrlKey||e.altKey)){if(r)try{history.replaceState({zenscrollY:i.getY()},"")}catch(e){}var n=t.getAttribute("href")||"";if(0===n.indexOf("#")&&!l.test(t.className)){var o=0,s=document.getElementById(n.substring(1));if("#"!==n){if(!s)return;o=i.getTopOf(s)}e.preventDefault();var c=function(){window.location=n},a=i.setup().edgeOffset;a&&(o=Math.max(0,o-a),c=function(){history.pushState(null,"",n)}),i.toY(o,null,c)}}},!1)}return i})},function(e,t){e.exports=function(e){function t(e,n){var r=n.appendChild(o(e));if(e.children.length){var l=i(e.isCollapsed);e.children.forEach(function(e){t(e,l)}),r.appendChild(l)}}function n(e,n){var o=i(!1);n.forEach(function(e){t(e,o)});var r=document.querySelector(e);if(null!==r)return r.firstChild&&r.removeChild(r.firstChild),r.appendChild(o)}function o(t){var n=document.createElement("li"),o=document.createElement("a");return e.listItemClass&&n.setAttribute("class",e.listItemClass),e.includeHtml&&t.childNodes.length?u.call(t.childNodes,function(e){o.appendChild(e.cloneNode(!0))}):o.textContent=t.textContent,o.setAttribute("href","#"+t.id),o.setAttribute("class",e.linkClass+p+"node-name--"+t.nodeName+p+e.extraLinkClasses),n.appendChild(o),n}function i(t){var n=document.createElement("ul"),o=e.listClass+p+e.extraListClasses;return t&&(o+=p+e.collapsibleClass,o+=p+e.isCollapsedClass),n.setAttribute("class",o),n}function r(){var t=document.documentElement.scrollTop||f.scrollTop,n=document.querySelector(e.positionFixedSelector);"auto"===e.fixedSidebarOffset&&(e.fixedSidebarOffset=document.querySelector(e.tocSelector).offsetTop),t>e.fixedSidebarOffset?-1===n.className.indexOf(e.positionFixedClass)&&(n.className+=p+e.positionFixedClass):n.className=n.className.split(p+e.positionFixedClass).join("")}function l(t){var n=document.documentElement.scrollTop||f.scrollTop;e.positionFixedSelector&&r();var o,i=t;if(m&&null!==document.querySelector(e.tocSelector)&&i.length>0){d.call(i,function(t,r){if(t.offsetTop>n+e.headingsOffset+10){return o=i[0===r?r:r-1],!0}if(r===i.length-1)return o=i[i.length-1],!0});var l=document.querySelector(e.tocSelector).querySelectorAll("."+e.linkClass);u.call(l,function(t){t.className=t.className.split(p+e.activeLinkClass).join("")});var c=document.querySelector(e.tocSelector).querySelector("."+e.linkClass+".node-name--"+o.nodeName+'[href="#'+o.id+'"]');c.className+=p+e.activeLinkClass;var a=document.querySelector(e.tocSelector).querySelectorAll("."+e.listClass+"."+e.collapsibleClass);u.call(a,function(t){var n=p+e.isCollapsedClass;-1===t.className.indexOf(n)&&(t.className+=p+e.isCollapsedClass)}),c.nextSibling&&(c.nextSibling.className=c.nextSibling.className.split(p+e.isCollapsedClass).join("")),s(c.parentNode.parentNode)}}function s(t){return-1!==t.className.indexOf(e.collapsibleClass)?(t.className=t.className.split(p+e.isCollapsedClass).join(""),s(t.parentNode.parentNode)):t}function c(t){var n=t.target||t.srcElement;"string"==typeof n.className&&-1!==n.className.indexOf(e.linkClass)&&(m=!1)}function a(){m=!0}var u=[].forEach,d=[].some,f=document.body,m=!0,p=" ";return{enableTocAnimation:a,disableTocAnimation:c,render:n,updateToc:l}}},function(e,t){e.exports={tocSelector:".js-toc",contentSelector:".js-toc-content",headingSelector:"h1, h2, h3",ignoreSelector:".js-toc-ignore",linkClass:"toc-link",extraLinkClasses:"",activeLinkClass:"is-active-link",listClass:"toc-list",extraListClasses:"",isCollapsedClass:"is-collapsed",collapsibleClass:"is-collapsible",listItemClass:"toc-list-item",collapseDepth:0,smoothScroll:!0,smoothScrollDuration:420,scrollEndCallback:function(e){},headingsOffset:0,throttleTimeout:50,positionFixedSelector:null,positionFixedClass:"is-position-fixed",fixedSidebarOffset:"auto",includeHtml:!1}},function(e,t){e.exports=function(e){function t(e){return e[e.length-1]}function n(e){return+e.nodeName.split("H").join("")}function o(t){var o={id:t.id,children:[],nodeName:t.nodeName,headingLevel:n(t),textContent:t.textContent.trim()};return e.includeHtml&&(o.childNodes=t.childNodes),o}function i(i,r){for(var l=o(i),s=n(i),c=r,a=t(c),u=a?a.headingLevel:0,d=s-u;d>0;)a=t(c),a&&void 0!==a.children&&(c=a.children),d--;return s>=e.collapseDepth&&(l.isCollapsed=!0),c.push(l),c}function r(t,n){var o=n;e.ignoreSelector&&(o=n.split(",").map(function(t){return t.trim()+":not("+e.ignoreSelector+")"}));try{return document.querySelector(t).querySelectorAll(o)}catch(e){return console.warn("Element not found: "+t),null}}function l(e){return s.call(e,function(e,t){return i(o(t),e.nest),e},{nest:[]})}var s=[].reduce;return{nestHeadingsArray:l,selectHeadings:r}}},function(e,t,n){(function(o){var i,r,l;!function(n,o){r=[],i=o(n),void 0!==(l="function"==typeof i?i.apply(t,r):i)&&(e.exports=l)}(void 0!==o?o:this.window||this.global,function(e){"use strict";function t(){for(var e={},t=0;t<arguments.length;t++){var n=arguments[t];for(var o in n)m.call(n,o)&&(e[o]=n[o])}return e}function o(e,t,n){t||(t=250);var o,i;return function(){var r=n||this,l=+new Date,s=arguments;o&&l<o+t?(clearTimeout(i),i=setTimeout(function(){o=l,e.apply(r,s)},t)):(o=l,e.apply(r,s))}}var i,r,l=n(3),s={},c={},a=n(2),u=n(4);if("undefined"!=typeof window){var d,f=!!e.document.querySelector&&!!e.addEventListener,m=Object.prototype.hasOwnProperty;return c.destroy=function(){try{document.querySelector(s.tocSelector).innerHTML=""}catch(e){console.warn("Element not found: "+s.tocSelector)}document.removeEventListener("scroll",this._scrollListener,!1),document.removeEventListener("resize",this._scrollListener,!1),i&&document.removeEventListener("click",this._clickListener,!1)},c.init=function(e){if(f&&(s=t(l,e||{}),this.options=s,this.state={},s.smoothScroll&&(c.zenscroll=n(1),c.zenscroll.setup(s.smoothScrollDuration)),i=a(s),r=u(s),this._buildHtml=i,this._parseContent=r,c.destroy(),null!==(d=r.selectHeadings(s.contentSelector,s.headingSelector)))){var m=r.nestHeadingsArray(d),p=m.nest;return i.render(s.tocSelector,p),this._scrollListener=o(function(e){i.updateToc(d);var t=e&&e.target&&e.target.scrollingElement&&0===e.target.scrollingElement.scrollTop;(e&&0===e.eventPhase||t)&&(i.enableTocAnimation(),i.updateToc(d),s.scrollEndCallback&&s.scrollEndCallback(e))},s.throttleTimeout),this._scrollListener(),document.addEventListener("scroll",this._scrollListener,!1),document.addEventListener("resize",this._scrollListener,!1),this._clickListener=o(function(e){s.smoothScroll&&i.disableTocAnimation(e),i.updateToc(d)},s.throttleTimeout),document.addEventListener("click",this._clickListener,!1),this}},c.refresh=function(e){c.destroy(),c.init(e||this.options)},e.tocbot=c,c}})}).call(t,n(0))}]);</script>
            <style>
            /* Enhanced TOC Styling */
            .js-toc {
              font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif;
              line-height: 1.6;
              max-width: 300px;
              background: #f8f9fa;
              padding: 1.5rem;
              border-radius: 8px;
              box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
            }
            
            .toc-list {
              list-style: none;
              padding: 0;
              margin: 0;
              border-left: 2px solid #e9ecef;
              padding-left: 1rem;
            }
            
            .toc-list-item {
              margin: 0.25rem 0;
              position: relative;
            }
            
            .toc-link {
              display: block;
              color: #495057;
              text-decoration: none;
              padding: 0.5rem 0.75rem;
              border-radius: 4px;
              transition: all 0.2s ease;
              font-size: 0.9rem;
              position: relative;
            }
            
            .toc-link:hover {
              background-color: #e9ecef;
              color: #212529;
              transform: translateX(2px);
            }
            
            .toc-link.is-active-link {
              background-color: #007bff;
              color: white;
              font-weight: 600;
              box-shadow: 0 2px 4px rgba(0, 123, 255, 0.3);
            }
            
            .toc-list.is-collapsible {
              position: relative;
            }
            
            .toc-list.is-collapsible::before {
              content: '▼';
              position: absolute;
              left: -1.5rem;
              top: 0.5rem;
              font-size: 0.8rem;
              color: #6c757d;
              cursor: pointer;
              transition: transform 0.2s ease;
              z-index: 1;
            }
            
            .toc-list.is-collapsible.is-collapsed::before {
              transform: rotate(-90deg);
            }
            
            .toc-list.is-collapsed {
              max-height: 0;
              overflow: hidden;
              transition: max-height 0.3s ease;
            }
            
            .toc-list:not(.is-collapsed) {
              max-height: 1000px;
              transition: max-height 0.3s ease;
            }
            
            .toc-list .toc-list {
              margin-top: 0.5rem;
              border-left-color: #dee2e6;
              padding-left: 0.75rem;
            }
            
            .toc-list .toc-list .toc-link {
              font-size: 0.85rem;
              color: #6c757d;
            }
            
            @media (max-width: 768px) {
              .js-toc {
                max-width: 100%;
                margin: 1rem 0;
              }
              
              .toc-list {
                padding-left: 0.5rem;
              }
            }
            </style>
            <script>
                /* Enhanced Tocbot configuration */
                var oldtoc = document.getElementById('toctitle').nextElementSibling;
                var newtoc = document.createElement('div');
                newtoc.setAttribute('id', 'tocbot');
                newtoc.setAttribute('class', 'js-toc');
                oldtoc.parentNode.replaceChild(newtoc, oldtoc);
                
                // Enhanced configuration with better collapsing
                var tocConfig = {
                    contentSelector: '#content',
                    headingSelector: 'h1, h2, h3, h4, h5',
                    collapseDepth: 2,
                    smoothScroll: true,
                    smoothScrollDuration: 300,
                    headingsOffset: 80,
                    throttleTimeout: 50,
                    activeLinkClass: 'is-active-link',
                    listClass: 'toc-list',
                    linkClass: 'toc-link',
                    collapsibleClass: 'is-collapsible',
                    isCollapsedClass: 'is-collapsed'
                };
                
                tocbot.init(tocConfig);
                
                // Enhanced click handler for expand/collapse arrows
                document.addEventListener('click', function(e) {
                    var target = e.target;
                    if (target.matches('.toc-list.is-collapsible::before') || 
                        (target.closest('.toc-list.is-collapsible') && 
                         e.offsetX < 0)) {
                        var list = target.closest('.toc-list.is-collapsible') || target;
                        list.classList.toggle('is-collapsed');
                        e.preventDefault();
                    }
                });
                
                // Responsive behavior
                var handleTocOnResize = function() {
                    var width = window.innerWidth || document.documentElement.clientWidth || document.body.clientWidth;
                    
                    if (width < 768) {
                        tocbot.refresh({
                            ...tocConfig,
                            collapseDepth: 1,
                            activeLinkClass: 'ignoreactive',
                            throttleTimeout: 1000
                        });
                    } else {
                        tocbot.refresh(tocConfig);
                    }
                };
                
                window.addEventListener('resize', handleTocOnResize);
                handleTocOnResize();
                
                // Keyboard navigation support
                document.addEventListener('keydown', function(e) {
                    if (e.key === 'Enter' || e.key === ' ') {
                        var focused = document.activeElement;
                        if (focused.matches('.toc-list.is-collapsible::before')) {
                            focused.closest('.toc-list').classList.toggle('is-collapsed');
                            e.preventDefault();
                        }
                    }
                });
            </script>
        """.trimIndent()
    }
}