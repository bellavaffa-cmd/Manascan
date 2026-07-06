package com.example.manascan.camera

import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.Text
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import java.util.concurrent.atomic.AtomicBoolean

/**
 * Runs on-device ML Kit text recognition on each camera frame and surfaces the line
 * of text most likely to be the card's title (top-most legible line in the frame).
 *
 * MTG cards place the name in a bold title bar at the very top, so "topmost line
 * with enough letters" is a cheap and fairly reliable heuristic without needing a
 * trained card-layout model.
 */
class CardTextAnalyzer(
    private val onCandidateName: (String) -> Unit
) : ImageAnalysis.Analyzer {

    private val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
    private val isProcessing = AtomicBoolean(false)

    @ExperimentalGetImage
    override fun analyze(imageProxy: ImageProxy) {
        val mediaImage = imageProxy.image
        if (mediaImage == null || !isProcessing.compareAndSet(false, true)) {
            imageProxy.close()
            return
        }

        val image = InputImage.fromMediaImage(mediaImage, imageProxy.imageInfo.rotationDegrees)
        recognizer.process(image)
            .addOnSuccessListener { visionText -> extractCardName(visionText)?.let(onCandidateName) }
            .addOnCompleteListener {
                isProcessing.set(false)
                imageProxy.close()
            }
    }

    private fun extractCardName(visionText: Text): String? {
        val lines = visionText.textBlocks.flatMap { it.lines }
        if (lines.isEmpty()) return null

        return lines
            .sortedBy { it.boundingBox?.top ?: Int.MAX_VALUE }
            .map { it.text.trim() }
            .firstOrNull { line -> isPlausibleCardName(line) }
    }

    private fun isPlausibleCardName(line: String): Boolean {
        val letterCount = line.count { it.isLetter() }
        val looksLikeManaCostOrNumber = line.matches(Regex("^[0-9{}WUBRGXC/]+$"))
        return line.length in 3..40 && letterCount >= 2 && !looksLikeManaCostOrNumber
    }

    fun close() {
        recognizer.close()
    }
}
