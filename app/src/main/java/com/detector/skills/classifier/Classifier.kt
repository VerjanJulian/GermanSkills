package com.detector.skills.classifier

interface Classifier {
    fun analyzeText(text: String): Result
}