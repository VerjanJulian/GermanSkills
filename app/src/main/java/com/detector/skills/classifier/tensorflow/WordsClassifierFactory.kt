package com.detector.skills.classifier.tensorflow

import android.content.res.AssetManager
import com.detector.skills.classifier.COLOR_CHANNELS
import com.detector.skills.classifier.Classifier
import com.detector.skills.utils.getLabels
import org.tensorflow.contrib.android.TensorFlowInferenceInterface

object WordsClassifierFactory {

    fun create(
            assetManager: AssetManager,
            graphFilePath: String,
            labelsFilePath: String,
            textSize: Int
    ): Classifier {

        val labels = getLabels(assetManager, labelsFilePath)

        return WordsClassifier(
            textSize.toLong(),
                labels,
                IntArray(textSize * textSize),
                FloatArray(textSize * textSize * COLOR_CHANNELS),
                FloatArray(labels.size),
                TensorFlowInferenceInterface(assetManager, graphFilePath)
        )
    }
}