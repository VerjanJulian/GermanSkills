package com.detector.skills.viewmodel

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.ViewModel
import com.detector.skills.classifier.Classifier
import com.ibm.watson.natural_language_understanding.v1.NaturalLanguageUnderstanding
import com.ibm.watson.natural_language_understanding.v1.model.*
import com.detector.skills.classifier.Result
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.Dispatchers
import kotlin.coroutines.CoroutineContext

class SkillsViewModel() : ViewModel(), CoroutineScope {

    private var job = Job()
    private val result = MutableLiveData<Result>()

    fun getResults(): LiveData<Result> {
        return result
    }

    override val coroutineContext: CoroutineContext
        get() = job

    private val entityOptions = EntitiesOptions.Builder()
        .emotion(true)
        .sentiment(true)
        .build()

    private val sentimentOptions = SentimentOptions.Builder()
        .document(true)
        .build()

    private val features = Features.Builder()
        .entities(entityOptions)
        .sentiment(sentimentOptions)
        .build()

    fun analyzeText(text: String, analyzer: NaturalLanguageUnderstanding) {
        val analyzerOptions = AnalyzeOptions.Builder()
            .text(text)
            .features(features)
            .build()
        launch {
            suspend {
                val response = analyzer.analyze(analyzerOptions).execute()
                withContext(Dispatchers.Main) {
                    val analyzeResult = Result(
                        response.result.sentiment.document.label,
                        response.result.sentiment.document.score
                    )
                    result.postValue(analyzeResult)
                }
            }.invoke()
        }
    }

    fun analyzeTextLocally(text: String, classifier: Classifier) {
        val analyzeResult = classifier.analyzeText(text)
        result.postValue(analyzeResult)
    }

    override fun onCleared() {
        super.onCleared()
        job.cancel()
    }
}