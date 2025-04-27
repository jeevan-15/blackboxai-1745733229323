

package com.example.win

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import com.google.android.material.card.MaterialCardView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions

class ChatActivity : AppCompatActivity() {

    private lateinit var questionEditText: EditText
    private lateinit var sendButton: Button
    private lateinit var scanImageButton: ImageButton
    private lateinit var chatLayout: LinearLayout
    private lateinit var scrollView: ScrollView

    private val IMAGE_REQUEST_CODE = 1001

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat)

        questionEditText = findViewById(R.id.editTextQuestion)
        sendButton = findViewById(R.id.buttonSend)
        scanImageButton = findViewById(R.id.buttonScanImage)
        chatLayout = findViewById(R.id.chatLayout)
        scrollView = findViewById(R.id.scrollViewChat)

        sendButton.setOnClickListener {
            val question = questionEditText.text.toString().trim()
            if (question.isNotEmpty()) {
                addUserMessage(question)
                questionEditText.text.clear()
                fetchAnswer(question)
            }
        }

        scanImageButton.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            startActivityForResult(intent, IMAGE_REQUEST_CODE)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == IMAGE_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            val imageUri: Uri? = data?.data
            if (imageUri != null) {
                processImage(imageUri)
            } else {
                Toast.makeText(this, "Failed to get image", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun addUserMessage(message: String) {
        val textView = TextView(this)
        textView.text = "You: $message"
        textView.setPadding(16, 16, 16, 16)
        chatLayout.addView(textView)
        scrollToBottom()
    }

    private fun addBotMessage(message: String) {
        val textView = TextView(this)
        textView.text = "WIN: $message"
        textView.setPadding(16, 16, 16, 16)
        chatLayout.addView(textView)
        scrollToBottom()
    }

    private fun addYouTubeVideo(videoUrl: String) {
        val webView = android.webkit.WebView(this)
        webView.layoutParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            600
        )
        webView.loadData(
            "<iframe width=\"100%\" height=\"100%\" src=\"$videoUrl\" frameborder=\"0\" allowfullscreen></iframe>",
            "text/html",
            "utf-8"
        )
        chatLayout.addView(webView)
        scrollToBottom()
    }

    private fun scrollToBottom() {
        scrollView.post {
            scrollView.fullScroll(ScrollView.FOCUS_DOWN)
        }
    }

    private fun fetchAnswer(question: String) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = AIChatApi.getAnswer(question)
                withContext(Dispatchers.Main) {
                    addBotMessage(response.answer)
                    response.youtubeVideos.forEach { videoUrl ->
                        addYouTubeVideo(videoUrl)
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    addBotMessage("Failed to get answer: ${e.message}")
                }
            }
        }
    }

    private fun processImage(imageUri: Uri) {
        try {
            val image = InputImage.fromFilePath(this, imageUri)
            val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
            recognizer.process(image)
                .addOnSuccessListener { visionText ->
                    val extractedText = visionText.text
                    if (extractedText.isNotEmpty()) {
                        addUserMessage("Scanned question: $extractedText")
                        fetchAnswer(extractedText)
                    } else {
                        addBotMessage("No text found in the image.")
                    }
                }
                .addOnFailureListener { e ->
                    addBotMessage("Failed to process image: ${e.message}")
                }
        } catch (e: Exception) {
            addBotMessage("Failed to process image: ${e.message}")
        }
    }
}
