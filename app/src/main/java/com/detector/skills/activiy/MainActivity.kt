package com.detector.skills.activiy

import android.arch.lifecycle.Observer
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.View.VISIBLE
import com.detector.skills.R
import com.detector.skills.classifier.*
import com.detector.skills.classifier.tensorflow.WordsClassifierFactory
import com.detector.skills.utils.assetFilePath
import com.detector.skills.viewmodel.SkillsViewModel
import kotlinx.android.synthetic.main.activity_main.*
import org.koin.android.viewmodel.ext.android.viewModel
import org.pytorch.IValue
import org.pytorch.Module
import java.util.*

class MainActivity : AppCompatActivity() {

    private val model: SkillsViewModel by viewModel()
    private lateinit var classifier: Classifier
    // loading serialized torchscript module from packaged into app android asset model.pt,
    // app/src/model/assets/model.pt
    private lateinit var module: Module
    private lateinit var getClassesOutput: IValue
    private val SCORES_FORMAT = "%.2f"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        module = Module.load(assetFilePath( "model-reddit16-f140225004_2.pt1"))
        setModelViewObserver()
        setListener()
        setUpPytorchModule()
    }

    private fun setUpPytorchModule(){
        getClassesOutput = module.runMethod("get_classes")
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
                imageResult.visibility = VISIBLE
                applyUIAnalysisResult(it)
            }
        })
    }

    private fun applyUIAnalysisResult(result: AnalysisResult) {
        for (i in 0 until model.TOP_K) {
            Log.e("ClassName..", result.topKClassNames[i])
            Log.e("RESULT....",String.format(
                Locale.US,
                SCORES_FORMAT,
                result.topKScores[i]
            ))
        }
        imageResult.visibility = VISIBLE
    }

    private fun setListener() {
        showDetail.setOnClickListener {
            model.analyzeText(writeHere.text.toString(), module, getClassesOutput)
        }
    }
}
