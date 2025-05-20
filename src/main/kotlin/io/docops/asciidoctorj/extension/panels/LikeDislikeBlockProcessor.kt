package io.docops.asciidoctorj.extension.panels

import org.asciidoctor.ast.StructuralNode
import org.asciidoctor.extension.BlockMacroProcessor
import org.asciidoctor.extension.Name

@Name("likeDislike")
class LikeDislikeBlockProcessor : BlockMacroProcessor() {

    override fun process(parent: StructuralNode, target: String, attributes: Map<String, Any>): StructuralNode {
        // Only support HTML backend
        val backend = parent.document.getAttribute("backend") as String
        if (backend != "html5") {
            return createBlock(parent, "paragraph", "")
        }

        // Get document information
        val documentName = parent.document.getAttribute("docname", "unknown") as String
        val documentAuthor = parent.document.getAttribute("author", "anonymous") as String

        // language=Html
        val html = """
            <div class="like-dislike-container" style="display: flex; align-items: center; margin: 20px 0;">
                <div class="thumbs-container" style="display: flex; gap: 10px;">
                    <button class="thumb-button thumb-up" 
                            style="background: none; border: none; cursor: pointer; font-size: 24px;" 
                            onclick="handleThumbClick('up', '$documentName', '$documentAuthor')">
                        👍
                    </button>
                    <button class="thumb-button thumb-down" 
                            style="background: none; border: none; cursor: pointer; font-size: 24px;" 
                            onclick="handleThumbClick('down', '$documentName', '$documentAuthor')">
                        👎
                    </button>
                </div>
                <div class="comment-bubble" 
                     style="margin-left: 15px; cursor: pointer; font-size: 24px;"
                     onclick="showCommentForm('$documentName', '$documentAuthor')">
                    💬
                </div>
            </div>

            <!-- Comment Form Modal -->
            <div id="commentFormModal" style="display: none; position: fixed; top: 0; left: 0; width: 100%; height: 100%; 
                 background-color: rgba(0,0,0,0.5); z-index: 1000; justify-content: center; align-items: center;">
                <div style="background-color: white; padding: 20px; border-radius: 5px; width: 80%; max-width: 500px;">
                    <h3>Leave a Comment</h3>
                    <input type="hidden" id="commentDocName" value="">
                    <input type="hidden" id="commentAuthor" value="">
                    <input type="hidden" id="commentThumbDirection" value="">
                    <textarea id="commentText" style="width: 100%; height: 100px; margin: 10px 0; padding: 5px;"></textarea>
                    <div style="display: flex; justify-content: flex-end; gap: 10px;">
                        <button onclick="closeCommentForm()" style="padding: 5px 10px;">Cancel</button>
                        <button onclick="submitComment()" style="padding: 5px 10px; background-color: #4CAF50; color: white; border: none;">Submit</button>
                    </div>
                </div>
            </div>

            <script>
                function handleThumbClick(direction, docName, author) {
                    // Highlight the selected thumb
                    const thumbsUp = document.querySelectorAll('.thumb-up');
                    const thumbsDown = document.querySelectorAll('.thumb-down');

                    if (direction === 'up') {
                        thumbsUp.forEach(btn => btn.style.opacity = '1');
                        thumbsDown.forEach(btn => btn.style.opacity = '0.5');
                    } else {
                        thumbsUp.forEach(btn => btn.style.opacity = '0.5');
                        thumbsDown.forEach(btn => btn.style.opacity = '1');
                    }

                    // Post the data
                    const data = {
                        documentName: docName,
                        author: author,
                        direction: direction
                    };

                    console.log('Like/Dislike data:', data);

                    // You can implement actual AJAX post here
                    // fetch('/api/feedback', {
                    //     method: 'POST',
                    //     headers: { 'Content-Type': 'application/json' },
                    //     body: JSON.stringify(data)
                    // });

                }

                function showCommentForm(docName, author, direction = null) {
                    document.getElementById('commentDocName').value = docName;
                    document.getElementById('commentAuthor').value = author;
                    if (direction) {
                        document.getElementById('commentThumbDirection').value = direction;
                    }

                    const modal = document.getElementById('commentFormModal');
                    modal.style.display = 'flex';
                }

                function closeCommentForm() {
                    const modal = document.getElementById('commentFormModal');
                    modal.style.display = 'none';
                    document.getElementById('commentText').value = '';
                }

                function submitComment() {
                    const docName = document.getElementById('commentDocName').value;
                    const author = document.getElementById('commentAuthor').value;
                    const comment = document.getElementById('commentText').value;

                    if (!comment.trim()) {
                        alert('Please enter a comment');
                        return;
                    }

                    const data = {
                        documentName: docName,
                        author: author,
                        comment: comment
                    };

                    console.log('Comment data:', data);

                    // You can implement actual AJAX post here
                    // fetch('/api/comment', {
                    //     method: 'POST',
                    //     headers: { 'Content-Type': 'application/json' },
                    //     body: JSON.stringify(data)
                    // });

                    closeCommentForm();
                    alert('Thank you for your feedback!');
                }
            </script>
        """.trimIndent()

        return createBlock(parent, "pass", html)
    }
}
