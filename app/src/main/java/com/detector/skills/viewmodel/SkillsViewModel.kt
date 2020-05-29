package com.detector.skills.viewmodel

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.ViewModel
import com.detector.skills.classifier.AnalysisResult
import com.detector.skills.utils.topK
import kotlinx.coroutines.*
import org.pytorch.IValue
import org.pytorch.Module
import org.pytorch.Tensor
import java.nio.charset.Charset
import kotlin.coroutines.CoroutineContext

class SkillsViewModel() : ViewModel(), CoroutineScope {

    private var job = Job()
    private val result = MutableLiveData<AnalysisResult>()
    private lateinit var moduleClasses: Array<String?>
    val TOP_K = 3

    fun getResults(): LiveData<AnalysisResult> {
        return result
    }

    override val coroutineContext: CoroutineContext
        get() = job

    fun analyzeText(
        text: String,
        module: Module,
        getClassesOutput: IValue
    ) {

        val classesListIValue = getClassesOutput.toList()
        val moduleClassesTemp =
            arrayOfNulls<String>(classesListIValue.size)
        var i = 0
        for (iv in classesListIValue) {
            moduleClassesTemp[i++] = iv.toStr()
        }
        moduleClasses = moduleClassesTemp

        val bytes = text.toByteArray(Charset.forName("UTF-8"))
        val shape = longArrayOf(1, bytes.size.toLong())
        val inputTensor = Tensor.fromBlobUnsigned(bytes, shape)
        val outputTensor: Tensor = module.forward(IValue.from(inputTensor)).toTensor()
        val scores = outputTensor.dataAsFloatArray
        val ixs: IntArray =
            topK(scores, TOP_K)
        val topKClassNames =
            arrayOfNulls<String>(TOP_K)
        val topKScores =
            FloatArray(TOP_K)
        for (i in 0 until TOP_K) {
            val ix = ixs[i]
            topKClassNames[i] = moduleClasses[ix]
            topKScores[i] = scores[ix]
        }
        result.postValue( AnalysisResult(
            topKClassNames,
            topKScores
        ))
    }

    override fun onCleared() {
        super.onCleared()
        job.cancel()
    }
}