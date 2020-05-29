package com.detector.skills.utils

import android.content.res.AssetManager
import com.detector.skills.activiy.MainActivity
import com.detector.skills.classifier.ASSETS_PATH
import java.io.*
import java.util.*

fun getLabels(assetManager: AssetManager, labelFilePath: String): List<String> {
    val actualFilename = getLabelsFileName(labelFilePath)
    return getLabelsFromFile(assetManager, actualFilename)
}

private fun getLabelsFromFile(assetManager: AssetManager, actualFilename: String): ArrayList<String> {
    val labels = ArrayList<String>()
    BufferedReader(InputStreamReader(assetManager.open(actualFilename))).use {
        var line: String? = it.readLine()
        while (line != null) {
            labels.add(line)
            line = it.readLine()
        }
        it.close()
    }
    return labels
}

private fun getLabelsFileName(labelFilenamePath: String): String {
    return labelFilenamePath.split(ASSETS_PATH.toRegex())
            .dropLastWhile { it.isEmpty() }
            .toTypedArray()[1]
}

@Throws(IOException::class)
fun MainActivity.assetFilePath(
    assetName: String?
): String? {
    val file = File(this.filesDir, assetName)
    if (file.exists() && file.length() > 0) {
        return file.absolutePath
    }
    this.assets.open(assetName).use { `is` ->
        FileOutputStream(file).use { os ->
            val buffer = ByteArray(4 * 1024)
            var read: Int
            while (`is`.read(buffer).also { read = it } != -1) {
                os.write(buffer, 0, read)
            }
            os.flush()
        }
        return file.absolutePath
    }
}

fun topK(a: FloatArray, topk: Int): IntArray {
    val values = FloatArray(topk)
    Arrays.fill(values, -Float.MAX_VALUE)
    val ixs = IntArray(topk)
    Arrays.fill(ixs, -1)
    for (i in a.indices) {
        for (j in 0 until topk) {
            if (a[i] > values[j]) {
                for (k in topk - 1 downTo j + 1) {
                    values[k] = values[k - 1]
                    ixs[k] = ixs[k - 1]
                }
                values[j] = a[i]
                ixs[j] = i
                break
            }
        }
    }
    return ixs
}