package com.detector.skills.activiy

import android.arch.lifecycle.Observer
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.View.VISIBLE
import com.detector.skills.R
import com.detector.skills.classifier.Classifier
import com.detector.skills.classifier.GRAPH_FILE_PATH
import com.detector.skills.classifier.LABELS_FILE_PATH
import com.detector.skills.classifier.TEXT_SIZE
import com.detector.skills.classifier.tensorflow.WordsClassifierFactory
import com.detector.skills.viewmodel.SkillsViewModel
import com.ibm.cloud.sdk.core.security.IamAuthenticator
import com.ibm.watson.natural_language_understanding.v1.NaturalLanguageUnderstanding
import kotlinx.android.synthetic.main.activity_main.imageResult
import kotlinx.android.synthetic.main.activity_main.writeHere
import kotlinx.android.synthetic.main.activity_main.showDetail
import org.koin.android.viewmodel.ext.android.viewModel

class MainActivity : AppCompatActivity() {

    private val model: SkillsViewModel by viewModel()
    private lateinit var analyzer: NaturalLanguageUnderstanding
    private lateinit var classifier: Classifier

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        createClassifier()
        setModelViewObserver()
        setListener()
        setNaturalLanguageUnderstanding()
    }

    private fun createClassifier() {
        classifier = WordsClassifierFactory.create(
            assets,
            GRAPH_FILE_PATH,
            LABELS_FILE_PATH,
            TEXT_SIZE
        )
    }

    private fun setModelViewObserver() {
        model.getResults().observe(this, Observer { overallSentiment ->
            overallSentiment?.let {
                val overallSentimentScore = it.confidence
                imageResult.setImageDrawable(
                    resources.getDrawable(
                        R.drawable.ic_sentiment_satisfied_black_24dp,
                        null
                    )
                )
                if (overallSentimentScore < 0.0)
                    imageResult.setImageDrawable(
                        resources.getDrawable(
                            R.drawable.ic_sentiment_very_dissatisfied_black_24dp,
                            null
                        )
                    )
                if (overallSentimentScore == 0.0)
                    imageResult.setImageDrawable(
                        resources.getDrawable(
                            R.drawable.ic_sentiment_very_dissatisfied_black_24dp,
                            null
                        )
                    )

                imageResult.visibility = VISIBLE
            }
        })
    }


    private fun setNaturalLanguageUnderstanding() {
        val authenticator = IamAuthenticator(
            resources.getString(
                R.string.natural_language_understanding_key
            )
        )
        analyzer = NaturalLanguageUnderstanding(
            "2019-07-12",
            authenticator
        )
        analyzer.serviceUrl = resources.getString(
            R.string.natural_language_understanding_url
        )
    }

    private fun setListener() {
        showDetail.setOnClickListener {
            model.analyzeText(writeHere.text.toString(), analyzer)
            //model.analyzeTextLocally(writeHere.text.toString(), classifier) For the moment we don use that because the model is not trained well
        }
    }
}
