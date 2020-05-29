package com.detector.skills.classifier

class AnalysisResult(
    val topKClassNames: Array<String?>,
    val topKScores: FloatArray
)