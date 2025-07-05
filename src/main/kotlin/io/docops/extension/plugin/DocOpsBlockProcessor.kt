package io.docops.extension.plugin

import org.asciidoctor.ast.Block
import org.asciidoctor.ast.ContentModel
import org.asciidoctor.ast.StructuralNode
import org.asciidoctor.extension.*
import org.asciidoctor.log.LogRecord
import org.asciidoctor.log.Severity
import java.io.ByteArrayOutputStream
import java.net.URI
import java.net.URLEncoder
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.nio.charset.StandardCharsets
import java.time.Duration
import java.util.*
import java.util.zip.GZIPOutputStream

@Name("docops")
@PositionalAttributes(value = ["kind"])
@Contexts(Contexts.LISTING)
@ContentModel(ContentModel.COMPOUND)
class DocOpsBlockProcessor: BlockProcessor()  {
    protected var server = "http://localhost:8010"
    protected var webserver = "http://localhost:8010"
    protected var localDebug = false


    override fun process(parent: StructuralNode, reader: Reader, attributes: MutableMap<String, Any>): Any {
        val doc = parent.document

        val docname = doc.attributes["docname"] as String

        val id = generateId(attributes)
        val title = attributes["title"] as? String ?: "SVG Viewer"
        val showControls = (attributes["controls"] as? String)?.toBoolean() ?: false
        val allowCopy = (attributes["copy"] as? String)?.toBoolean() ?: true
        val allowZoom = (attributes["zoom"] as? String)?.toBoolean() ?: true
        val allowExpand = (attributes["expand"] as? String)?.toBoolean() ?: true
        val theme = attributes["theme"] as? String ?: "light"
        val role = attributes.getOrDefault("role", "center") as String
        debugOnOff(parent)
        val content = subContent(reader, parent, debug = localDebug)
        setupServers(parent)
        val block: Block = createBlock(parent, "open", null as String?)
        var image = ""

        if (serverAvailable(parent = parent, pb = this)) {
            val type = getType(parent = parent)
            val backend = parent.document.getAttribute("backend") as String
            val payload = getCompressedPayload(parent = parent, content = content)
            val opts = "format=svg,opts=inline,align='$role'"
            val kind = attributes.get("kind") as String
            if (kind.isEmpty()) {
                parseContent(
                    parent,
                    mutableListOf<String>("Parameter Error: Missing 'kind' block parameter, example -> [\"docops\", kind=\"buttons\"] 😵")
                )
            }
            val dark = attributes.getOrDefault("useDark", "false") as String
            val useDark: Boolean = "true".equals(dark, true)
            val scale = attributes.getOrDefault("scale", "1.0") as String
            val title = attributes.getOrDefault("title", "Title") as String
            val lines = mutableListOf<String>()
            // Check if base64 conversion is requested
            val useBase64 = (attributes["base64"] as? String)?.toBoolean() ?: true



            if ("PDF" == type) {

                val link = """$webserver/api/docops/svg?kind=$kind&payload=$payload&scale=$scale&title=${title.encodeUrl()}&type=SVG&useDark=$useDark&backend=$backend&docname=${docname.encodeUrl()}&filename=docops.svg"""
                val img = """image::$link[$opts,link=$link,window=_blank,opts=nofollow]"""
                if (localDebug) {
                    println(img)
                }
                lines.add(img)
                parseContent(block, lines)
            } else {

                val url = """$webserver/api/docops/svg?kind=$kind&payload=$payload&scale=$scale&type=$type&useDark=$useDark&title=${title.encodeUrl()}&backend=$backend&docname=${docname.encodeUrl()}&filename=ghi.svg"""
                if (localDebug) {
                    println(url)
                }
                image = getContentFromServer(url, parent, this, debug = localDebug)
                // Convert to base64 if requested and it's SVG content
                if (useBase64 && !isIdeaOn(parent)) {
                    image = convertSvgToBase64Image(image, attributes)
                }

                val html = if (showControls) {
                    generateSvgViewerHtml(
                        image, id, title,
                        showControls, allowCopy, allowZoom, allowExpand, theme, role  // Pass role
                    )
                } else {
                    // For non-controlled SVGs, still apply alignment
                    """<div style="${getAlignmentStyle(role)}"><div style="display: inline-block;">$image</div></div>"""
                }

                lines.add(html)
                return createBlock(block, "pass", html)
            }

        } else {
            parseContent(parent, mutableListOf<String>("DocOps Server Unavailable! 😵"))
        }
        return block
    }

    private fun generateId(attributes: Map<String, Any>): String {
        return attributes["id"] as? String ?: "svgviewer-${System.currentTimeMillis()}"
    }

    /**
     * Build HTML object tag for interactive SVG with proper attributes
     */
    private fun buildObjectTag(dataUrl: String, attributes: Map<String, Any>): String {
        return buildString {
            append("<object type=\"image/svg+xml\" data=\"$dataUrl\"")

            // Add standard attributes if provided
            attributes["width"]?.let { append(" width=\"${escapeHtml(it.toString())}\"") }
            attributes["height"]?.let { append(" height=\"${escapeHtml(it.toString())}\"") }
            attributes["class"]?.let { append(" class=\"${escapeHtml(it.toString())}\"") }
            attributes["id"]?.let { append(" id=\"${escapeHtml(it.toString())}\"") }
            attributes["style"]?.let { append(" style=\"${escapeHtml(it.toString())}\"") }

            // Add ARIA attributes for accessibility
            attributes["title"]?.let { append(" aria-label=\"${escapeHtml(it.toString())}\"") }
            attributes["alt"]?.let { append(" aria-describedby=\"${escapeHtml(it.toString())}\"") }

            // Close opening tag
            append(">")

           /* // Add fallback content for browsers that don't support object tag
            append("<p>")
            attributes["alt"]?.let {
                append("${escapeHtml(it.toString())}")
            } ?: append("Interactive SVG content - please use a modern browser to view.")
            append("</p>")*/

            // Close object tag
            append("</object>")
        }
    }

    /**
     * Check if the content is SVG by looking for SVG tags
     */
    private fun isSvgContent(content: String): Boolean {
        return content.trim().startsWith("<svg") && content.contains("</svg>")
    }

    /**
     * Convert SVG content to base64 data URL image tag
     */
    private fun convertSvgToBase64Image(svgContent: String, attributes: Map<String, Any>): String {
        return try {
            // Clean and optimize the SVG content
            val cleanSvg = optimizeSvgContent(svgContent)

            // Encode to base64
            val base64Content = Base64.getEncoder().encodeToString(
                cleanSvg.toByteArray(Charsets.UTF_8)
            )

            // Create data URL
            val dataUrl = "data:image/svg+xml;base64,$base64Content"

            // Build image tag with attributes
            buildObjectTag(dataUrl, attributes)

        } catch (e: Exception) {
            if (localDebug) {
                println("Warning: Failed to convert SVG to base64: ${e.message}")
            }
            // Return original SVG content if conversion fails
            svgContent
        }
    }

    /**
     * Optimize SVG content by removing unnecessary whitespace and comments
     */
    private fun optimizeSvgContent(svgContent: String): String {
        var optimized = svgContent.trim()

        // Remove XML declaration if present (not needed for data URLs)
        optimized = optimized.replace(Regex("""<\?xml[^>]*\?>"""), "")

        // Remove DOCTYPE if present
        optimized = optimized.replace(Regex("""<!DOCTYPE[^>]*>"""), "")

        // Remove comments
        optimized = optimized.replace(Regex("""<!--.*?-->""", RegexOption.DOT_MATCHES_ALL), "")

        // Normalize whitespace (but be careful with text content)
        optimized = optimized.replace(Regex("""\s+"""), " ")

        // Remove leading/trailing whitespace
        optimized = optimized.trim()

        // Ensure xmlns attribute is present for standalone SVG
        if (!optimized.contains("xmlns=")) {
            optimized = optimized.replace("<svg", """<svg xmlns="http://www.w3.org/2000/svg"""")
        }

        return optimized
    }


    /**
     * Escape HTML special characters in attribute values
     */
    private fun escapeHtml(text: String): String {
        return text.replace("&", "&amp;")
            .replace("<", "&lt;")
            .replace(">", "&gt;")
            .replace("\"", "&quot;")
            .replace("'", "&#39;")
    }

    private fun generateSvgViewerHtml(
        svgContent: String,
        id: String,
        title: String,
        showControls: Boolean,
        allowCopy: Boolean,
        allowZoom: Boolean,
        allowExpand: Boolean,
        theme: String,
        role: String = "center"
    ): String = buildString {

        // Get alignment styling based on role
        val alignmentStyle = getAlignmentStyle(role)

        // Outer container for alignment
        append("""<div class="svg-viewer-container" style="$alignmentStyle">""")

        // Inner container for SVG and controls together
        append("""<div class="svg-with-controls" id="$id" data-theme="$theme">""")

        // Insert the raw SVG content
        append(svgContent)

        // Add floating controls overlay if enabled
        if (showControls) {
            append(
                """
            <div class="svg-floating-controls">
                <button class="svg-controls-toggle" onclick="svgViewer.toggleControls('$id')" title="Controls">
                    ⚙️
                </button>
                <div class="svg-controls-panel" id="controls-panel-$id">
        """.trimIndent()
            )

            if (allowZoom) {
                append(buildZoomControls(id))
            }

            if (allowExpand) {
                append(buildExpandControl(id))
            }

            if (allowCopy) {
                append(buildCopyControl(id))
            }

            append(
                """
                </div>
            </div>
        """.trimIndent()
            )
        }

        append("</div>") // Close svg-with-controls
        append("</div>") // Close svg-viewer-container

        // Add minimal CSS and JavaScript
        append(getMinimalControlsAssets())
    }

    private fun getAlignmentStyle(role: String): String {
        return when (role.lowercase()) {
            "left" -> "display: block; text-align: left;"
            "right" -> "display: block; text-align: right;"
            "center" -> "display: block; text-align: center;"
            else -> "display: block; text-align: center;" // default to center
        }
    }

    //language=html
    private fun getMinimalControlsAssets(): String = """
<style>
/* Outer container handles alignment */
.svg-viewer-container {
    width: 100%;
    /* text-align will be set via inline style based on role */
}

/* Inner container groups SVG and controls together */
.svg-with-controls {
    position: relative;
    display: inline-block; /* This makes it respect text-align and stay together */
    max-width: 100%;
}

.svg-with-controls svg {
    max-width: 100%;
    height: auto;
    transition: transform 0.3s ease;
    transform-origin: center center;
    display: block;
}

.svg-floating-controls {
    position: absolute;
    top: 8px;
    right: 8px;
    z-index: 100;
}

.svg-controls-toggle {
    width: 32px;
    height: 32px;
    border: none;
    background: rgba(255, 255, 255, 0.9);
    border-radius: 50%;
    cursor: pointer;
    font-size: 14px;
    display: flex;
    align-items: center;
    justify-content: center;
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.15);
    transition: all 0.2s ease;
    backdrop-filter: blur(4px);
}

.svg-controls-toggle:hover {
    background: rgba(255, 255, 255, 1);
    transform: scale(1.1);
    box-shadow: 0 4px 12px rgba(0, 0, 0, 0.2);
}

.svg-controls-panel {
    position: absolute;
    top: 40px;
    right: 0;
    background: rgba(255, 255, 255, 0.95);
    backdrop-filter: blur(8px);
    border: 1px solid rgba(0, 0, 0, 0.1);
    border-radius: 8px;
    padding: 6px;
    display: none;
    flex-direction: column;
    gap: 4px;
    box-shadow: 0 8px 25px rgba(0, 0, 0, 0.15);
    min-width: 120px;
    animation: slideIn 0.2s ease-out;
}

.svg-controls-panel.show {
    display: flex;
}

@keyframes slideIn {
    from {
        opacity: 0;
        transform: translateY(-8px);
    }
    to {
        opacity: 1;
        transform: translateY(0);
    }
}

.svg-control-btn {
    padding: 6px 10px;
    border: none;
    background: rgba(255, 255, 255, 0.8);
    border-radius: 4px;
    cursor: pointer;
    font-size: 11px;
    transition: all 0.2s ease;
    white-space: nowrap;
    text-align: center;
    color: #333;
    border: 1px solid rgba(0, 0, 0, 0.1);
}

.svg-control-btn:hover {
    background: rgba(255, 255, 255, 1);
    transform: translateY(-1px);
    box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
}

/* Dark theme support */
[data-theme="dark"] .svg-controls-toggle,
[data-theme="dark"] .svg-controls-panel,
[data-theme="dark"] .svg-control-btn {
    background: rgba(30, 30, 30, 0.9);
    color: #f0f0f0;
    border-color: rgba(255, 255, 255, 0.2);
}

[data-theme="dark"] .svg-controls-toggle:hover,
[data-theme="dark"] .svg-control-btn:hover {
    background: rgba(50, 50, 50, 0.95);
}

/* Fullscreen mode */
.svg-fullscreen {
    position: fixed !important;
    top: 0 !important;
    left: 0 !important;
    width: 100vw !important;
    height: 100vh !important;
    z-index: 9999 !important;
    background: rgba(0, 0, 0, 0.95) !important;
    display: flex !important;
    align-items: center !important;
    justify-content: center !important;
}

.svg-fullscreen svg {
    max-width: 90vw !important;
    max-height: 90vh !important;
}

.svg-fullscreen .svg-floating-controls {
    top: 20px;
    right: 20px;
}

/* Copy success message */
.copy-message {
    position: absolute;
    top: 50%;
    left: 50%;
    transform: translate(-50%, -50%);
    background: rgba(34, 197, 94, 0.95);
    color: white;
    padding: 8px 16px;
    border-radius: 6px;
    font-size: 14px;
    pointer-events: none;
    opacity: 0;
    transition: opacity 0.3s ease;
    z-index: 101;
}

.copy-message.show {
    opacity: 1;
}

.copy-message.error {
    background: rgba(239, 68, 68, 0.95);
}
</style>

<script>
window.svgViewer = window.svgViewer || {
    toggleControls(id) {
        const panel = document.getElementById(`controls-panel-${'$'}{id}`);
        const isVisible = panel.classList.contains('show');
        
        // Hide all other panels
        document.querySelectorAll('.svg-controls-panel').forEach(p => {
            p.classList.remove('show');
        });
        
        // Toggle current panel
        if (!isVisible) {
            panel.classList.add('show');
        }
    },
    
    zoomIn(id) {
        const container = document.getElementById(id);
        const svg = container.querySelector('svg');
        if (!svg) return;
        
        let currentZoom = parseFloat(svg.dataset.zoom) || 1;
        currentZoom = Math.min(currentZoom * 1.3, 5);
        svg.dataset.zoom = currentZoom;
        svg.style.transform = `scale(${'$'}{currentZoom})`;
    },
    
    zoomOut(id) {
        const container = document.getElementById(id);
        const svg = container.querySelector('svg');
        if (!svg) return;
        
        let currentZoom = parseFloat(svg.dataset.zoom) || 1;
        currentZoom = Math.max(currentZoom / 1.3, 0.2);
        svg.dataset.zoom = currentZoom;
        svg.style.transform = `scale(${'$'}{currentZoom})`;
    },
    
    resetZoom(id) {
        const container = document.getElementById(id);
        const svg = container.querySelector('svg');
        if (!svg) return;
        
        svg.dataset.zoom = 1;
        svg.style.transform = 'scale(1)';
    },
    
    toggleFullscreen(id) {
        const container = document.getElementById(id);
        if (container.classList.contains('svg-fullscreen')) {
            container.classList.remove('svg-fullscreen');
            document.body.style.overflow = '';
        } else {
            container.classList.add('svg-fullscreen');
            document.body.style.overflow = 'hidden';
        }
    },
    
    async copyAsSvg(id) {
        const container = document.getElementById(id);
        const svg = container.querySelector('svg');
        
        if (!svg) {
            this.showMessage(container, 'No SVG found', 'error');
            return;
        }
        
        try {
            const svgData = new XMLSerializer().serializeToString(svg);
            await navigator.clipboard.writeText(svgData);
            this.showMessage(container, 'SVG copied as text! 📋');
        } catch (error) {
            console.error('SVG copy failed:', error);
            this.showMessage(container, 'SVG copy failed 😞', 'error');
        }
    },
    
    async copyAsPng(id) {
        const container = document.getElementById(id);
        const svg = container.querySelector('svg');
        
        if (!svg) {
            this.showMessage(container, 'No SVG found', 'error');
            return;
        }
        
        try {
            this.showMessage(container, 'Converting to PNG...', 'info');
            const pngBlob = await this.convertSvgToPng(svg);
            const item = new ClipboardItem({ 'image/png': pngBlob });
            await navigator.clipboard.write([item]);
            this.showMessage(container, 'PNG copied to clipboard! 🖼️');
        } catch (error) {
            console.error('PNG copy failed:', error);
            this.showMessage(container, 'PNG copy failed 😞', 'error');
        }
    },
    
    convertSvgToPng(svgElement) {
        return new Promise((resolve, reject) => {
            // Clone the SVG to avoid modifying the original
            const svgClone = svgElement.cloneNode(true);
            
            // Get SVG dimensions
            let svgWidth = parseInt(svgClone.getAttribute('width')) || svgElement.getBoundingClientRect().width || 800;
            let svgHeight = parseInt(svgClone.getAttribute('height')) || svgElement.getBoundingClientRect().height || 600;
            
            // Handle viewBox if no explicit width/height
            if (!svgClone.getAttribute('width') && !svgClone.getAttribute('height')) {
                const viewBox = svgClone.getAttribute('viewBox');
                if (viewBox) {
                    const [x, y, w, h] = viewBox.split(' ').map(Number);
                    svgWidth = w;
                    svgHeight = h;
                }
            }
            
            // Ensure the cloned SVG has explicit dimensions
            svgClone.setAttribute('width', svgWidth.toString());
            svgClone.setAttribute('height', svgHeight.toString());
            
            // Add XML namespace if missing
            if (!svgClone.getAttribute('xmlns')) {
                svgClone.setAttribute('xmlns', 'http://www.w3.org/2000/svg');
            }
            
            // Handle external stylesheets and fonts
            this.inlineStyles(svgClone);
            
            const svgData = new XMLSerializer().serializeToString(svgClone);
            const canvas = document.createElement('canvas');
            const ctx = canvas.getContext('2d');
            const img = new Image();
            
            // Set canvas size with scaling for better quality
            const scale = 2; // For higher resolution
            canvas.width = svgWidth * scale;
            canvas.height = svgHeight * scale;
            ctx.scale(scale, scale);
            
            // Create a blob URL for the SVG
            const svgBlob = new Blob([svgData], { type: 'image/svg+xml;charset=utf-8' });
            const url = URL.createObjectURL(svgBlob);
            
            img.onload = function() {
                // Set white background (optional - remove if you want transparency)
                ctx.fillStyle = 'white';
                ctx.fillRect(0, 0, svgWidth, svgHeight);
                
                // Draw the image
                ctx.drawImage(img, 0, 0, svgWidth, svgHeight);
                
                canvas.toBlob((blob) => {
                    if (blob) {
                        resolve(blob);
                    } else {
                        reject(new Error('Failed to create PNG blob'));
                    }
                }, 'image/png', 0.95);
                
                URL.revokeObjectURL(url);
            };
            
            img.onerror = (error) => {
                URL.revokeObjectURL(url);
                reject(new Error('Failed to load SVG as image: ' + error));
            };
            
            img.src = url;
        });
    },
    
    inlineStyles(svgElement) {
        // Get all stylesheets from the document
        const stylesheets = Array.from(document.styleSheets);
        let cssText = '';
        
        stylesheets.forEach(sheet => {
            try {
                const rules = Array.from(sheet.cssRules || sheet.rules || []);
                rules.forEach(rule => {
                    if (rule.type === CSSRule.STYLE_RULE) {
                        cssText += rule.cssText + ' ';
                    }
                });
            } catch (e) {
                // Cross-origin stylesheets might throw errors
                console.warn('Could not access stylesheet:', e);
            }
        });
        
        // Create a style element and add it to the SVG
        if (cssText) {
            const styleElement = document.createElementNS('http://www.w3.org/2000/svg', 'style');
            styleElement.textContent = cssText;
            svgElement.insertBefore(styleElement, svgElement.firstChild);
        }
    },
    
    // Legacy method for backward compatibility
    async copyToClipboard(id) {
        await this.copyAsSvg(id);
    },
    
    showMessage(container, message, type = 'success') {
        const existing = container.querySelector('.copy-message');
        if (existing) existing.remove();
        
        const messageEl = document.createElement('div');
        messageEl.className = `copy-message ${'$'}{type === 'error' ? 'error' : ''}`;
        
        // Different background colors for different message types
        if (type === 'info') {
            messageEl.style.background = 'rgba(59, 130, 246, 0.95)';
        }
        
        messageEl.textContent = message;
        container.appendChild(messageEl);
        
        setTimeout(() => messageEl.classList.add('show'), 10);
        setTimeout(() => {
            messageEl.classList.remove('show');
            setTimeout(() => messageEl.remove(), 300);
        }, type === 'info' ? 1500 : 2500);
    }
};

// Close controls when clicking outside
document.addEventListener('click', function(e) {
    if (!e.target.closest('.svg-floating-controls')) {
        document.querySelectorAll('.svg-controls-panel').forEach(panel => {
            panel.classList.remove('show');
        });
    }
});

// Escape key for fullscreen
document.addEventListener('keydown', function(e) {
    if (e.key === 'Escape') {
        document.querySelectorAll('.svg-fullscreen').forEach(container => {
            container.classList.remove('svg-fullscreen');
            document.body.style.overflow = '';
        });
    }
});
</script>
""".trimIndent()

    fun subContent(reader: Reader, parent: StructuralNode, debug: Boolean = false): String {
        val content = reader.read()
        return subs(content, parent, debug)
    }

    fun subs(content: String, parent: StructuralNode, debug: Boolean = false): String {
        val pattern = """#\[.*?]""".toRegex()
        val res = pattern.findAll(content)
        var localContent = content
        res.forEach {
            val subValue = parent.document.attributes[it.value.replace("#[", "").replace("]", "").lowercase()]
            val key = it.value
            if (debug) {
                println("Text Substitution for $key & value to replace $subValue")
            }
            if (subValue != null) {
                subValue as String
                localContent = localContent.replace(key, subValue)
                if (debug) {
                    println("content after substituting $key -> $localContent")
                }
            }
        }
        return localContent
    }

    fun debugOnOff(parent: StructuralNode) {
        val debug = parent.document.attributes["local-debug"]
        if (debug != null) {
            debug as String
            localDebug = debug.toBoolean()
        }
    }

    fun setupServers(parent: StructuralNode) {
        val remoteServer = parent.document.attributes["panel-server"]
        remoteServer?.let {
            server = remoteServer as String
        }
        val remoteWebserver = parent.document.attributes["panel-webserver"]
        remoteWebserver?.let {
            webserver = it as String
        }
    }

    fun getCompressedPayload(parent: StructuralNode, content: String): String {
        val payload: String = try {
            compressString(content)
        } catch (e: Exception) {
            log(LogRecord(Severity.ERROR, parent.sourceLocation, e.message))
            ""
        }
        return payload
    }

    fun isIdeaOn(parent: StructuralNode): Boolean {
        val idea = parent.document.getAttribute("env", "") as String
        return "idea".equals(idea, true)
    }

    fun getType(parent: StructuralNode): String {
        var type = "SVG"
        val backend = parent.document.getAttribute("backend") as String
        if("pdf" == backend) {
            type = "PDF"
        }
        return type
    }

    private fun createImageBlockFromString(
        parent: StructuralNode,
        svg: String,
        role: String,
        width: String = "970"
    ): Block {

        val align = mutableMapOf(
            "right" to "margin-left: auto; margin-right: 0;",
            "left" to "",
            "center" to "margin: auto;"
        )
        val center = align[role.lowercase()]
        val content: String = """
            <div class="openblock">
            <div class="content" style="width: $width;padding: 10px;$center">
            $svg
            </div>
            </div>
        """.trimIndent()
        return createBlock(parent, "pass", content)
    }

    fun serverAvailable(parent: StructuralNode, pb: BlockProcessor): Boolean {
        if (localDebug) {
            println("Checking if server is present ${server}/api/ping")
        }
        val client = HttpClient.newBuilder().version(HttpClient.Version.HTTP_1_1)
            .connectTimeout(Duration.ofSeconds(20))
            .build()
        val request = HttpRequest.newBuilder()
            .uri(URI.create("$server/api/ping"))
            .timeout(Duration.ofMinutes(1))
            .build()
        return try {
            val response = client.send(request, HttpResponse.BodyHandlers.ofString())
            (200 == response.statusCode())
        } catch (e: Exception) {
            pb.log(LogRecord(Severity.ERROR, parent.sourceLocation, e.message))
            e.printStackTrace()
            false
        }
    }

    protected fun String.encodeUrl(): String {
        return URLEncoder.encode(this, StandardCharsets.UTF_8.toString())
    }

    protected fun getCharLength(attributes: MutableMap<String, Any>, defaultLength: Int): String {
        return attributes.getOrDefault("numChars", "$defaultLength") as String
    }

    private fun buildZoomControls(id: String): String = """
        <button class="svg-control-btn zoom-in" onclick="svgViewer.zoomIn('$id')" title="Zoom In">🔍+</button>
        <button class="svg-control-btn zoom-out" onclick="svgViewer.zoomOut('$id')" title="Zoom Out">🔍-</button>
        <button class="svg-control-btn zoom-reset" onclick="svgViewer.resetZoom('$id')" title="Reset Zoom">⚪</button>
    """.trimIndent()

    private fun buildExpandControl(id: String): String = """
        <button class="svg-control-btn expand" onclick="svgViewer.toggleFullscreen('$id')" title="Toggle Fullscreen">⛶</button>
    """.trimIndent()

    private fun buildCopyControl(id: String): String = """
    <button class="svg-control-btn" onclick="svgViewer.copyAsSvg('$id')" title="Copy as SVG">📋 SVG</button>
    <button class="svg-control-btn" onclick="svgViewer.copyAsPng('$id')" title="Copy as PNG">📋 PNG</button>
""".trimIndent()
}

fun serverPresent(server: String, parent: StructuralNode, pb: BlockProcessor, debug: Boolean = false): Boolean {
    if (debug) {
        println("Checking if server is present ${server}/api/ping")
    }
    val client = HttpClient.newBuilder().version(HttpClient.Version.HTTP_1_1)
        .connectTimeout(Duration.ofSeconds(20))
        .build()
    val request = HttpRequest.newBuilder()
        .uri(URI.create("$server/api/ping"))
        .timeout(Duration.ofMinutes(1))
        .build()
    return try {
        val response = client.send(request, HttpResponse.BodyHandlers.ofString())
        (200 == response.statusCode())
    } catch (e: Exception) {
        pb.log(LogRecord(Severity.ERROR, parent.sourceLocation, e.message))
        e.printStackTrace()
        false
    }
}

fun compressString(body: String): String {
    val baos = ByteArrayOutputStream()
    val zos = GZIPOutputStream(baos)
    zos.use { z ->
        z.write(body.toByteArray())
    }
    val bytes = baos.toByteArray()
    return Base64.getUrlEncoder().encodeToString(bytes)
}

fun subContent(reader: Reader, parent: StructuralNode, debug: Boolean = false): String {
    val content = reader.read()
    return subs(content, parent, debug)
}

fun subs(content: String, parent: StructuralNode, debug: Boolean = false): String {
    val pattern = """#\[.*?]""".toRegex()
    val res = pattern.findAll(content)
    var localContent = content
    res.forEach {
        val subValue = parent.document.attributes[it.value.replace("#[", "").replace("]", "").lowercase()]
        val key = it.value
        if (debug) {
            println("Text Substitution for $key & value to replace $subValue")
        }
        if (subValue != null) {
            subValue as String
            localContent = localContent.replace(key, subValue)
            if (debug) {
                println("content after substituting $key -> $localContent")
            }
        }
    }
    return localContent
}
fun getContentFromServer(url: String, parent: StructuralNode, pb: BlockProcessor, debug: Boolean = false): String {
    if (debug) {
        println("getting image from url $url")
    }
    val client = HttpClient.newBuilder().version(HttpClient.Version.HTTP_1_1)
        .connectTimeout(Duration.ofSeconds(20))
        .build()
    val request = HttpRequest.newBuilder()
        .uri(URI.create(url))
        .timeout(Duration.ofMinutes(1))
        .build()
    return try {
        val response = client.send(request, HttpResponse.BodyHandlers.ofString())
        response.body()
    } catch (e: Exception) {
        pb.log(LogRecord(Severity.ERROR, parent.sourceLocation, e.message))
        e.printStackTrace()
        ""
    }
}

