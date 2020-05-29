package com.detector.skills.classifier.tensorflow

import com.detector.skills.classifier.Classifier
import com.detector.skills.classifier.Result
import com.detector.skills.classifier.GRAPH_INPUT_NAME
import com.detector.skills.classifier.GRAPH_OUTPUT_NAME
import com.detector.skills.classifier.COLOR_CHANNELS
import org.tensorflow.contrib.android.TensorFlowInferenceInterface
import java.util.PriorityQueue

private const val ENABLE_LOG_STATS = false

class WordsClassifier (
        private val textSize: Long,
        private val labels: List<String>,
        private val textSizeArray: IntArray,
        private val textSizeNormalized: FloatArray,
        private val results: FloatArray,
        private val tensorFlowInference: TensorFlowInferenceInterface
) : Classifier {

    override fun analyzeText(text: String): Result {
        preprocessTextToNormalizedFloats()
        classifyTextToOutputs()
        val outputQueue = getResults()
        return outputQueue.poll()
    }

    private fun preprocessTextToNormalizedFloats() {
        // Preprocess the image data from 0-255 int to normalized float based
        // on the provided parameters.
        val textMean = 128
        val textStd = 128.0f
        for (i in textSizeArray.indices) {
            val `val` = textSizeArray[i]
            textSizeNormalized[i * 3 + 0] = ((`val` shr 16 and 0xFF) - textMean) / textStd
            textSizeNormalized[i * 3 + 1] = ((`val` shr 8 and 0xFF) - textMean) / textStd
            textSizeNormalized[i * 3 + 2] = ((`val` and 0xFF) - textMean) / textStd
        }
    }

    private fun classifyTextToOutputs() {
        tensorFlowInference.feed(GRAPH_INPUT_NAME, textSizeNormalized,
                1L, textSize, textSize, COLOR_CHANNELS.toLong())
        tensorFlowInference.run(arrayOf(GRAPH_OUTPUT_NAME), ENABLE_LOG_STATS)
        tensorFlowInference.fetch(GRAPH_OUTPUT_NAME, results)
    }

    private fun getResults(): PriorityQueue<Result> {
        val outputQueue = createOutputQueue()
        results.indices.mapTo(outputQueue) { Result(labels[it], results[it].toDouble()) }
        return outputQueue
    }

    private fun createOutputQueue(): PriorityQueue<Result> {
        return PriorityQueue(
                labels.size,
                Comparator { (_, rConfidence), (_, lConfidence) ->
                    lConfidence.compareTo(rConfidence)
                })
    }
}