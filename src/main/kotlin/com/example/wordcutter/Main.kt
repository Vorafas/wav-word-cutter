package com.example.wordcutter

import com.example.wordcutter.model.Result
import com.example.wordcutter.model.WordInfo
import com.example.wordcutter.util.Constants
import com.example.wordcutter.util.Serialization
import com.example.wordcutter.util.WavFormatter
import com.example.wordcutter.wave.WavHeaderReader
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream
import java.util.UUID
import org.vosk.Model
import org.vosk.Recognizer

fun main() {
    File(Constants.DATASET_FOLDER).listFiles()?.forEach { file ->
        if (file.extension == Constants.SUPPORTED_AUDIO_FILE_FORMAT) {
            try {
                cutWord(file)
            } catch (exc: IOException) {
                println(exc.message)
            }
        }
    }
}

private fun cutWord(file: File) {
    FileInputStream(file).use { input ->
        if (isValidWavFile(input)) {
            val audioData = input.readBytes()

            val result = recognize(audioData)
            for (wordInfo in result.words) {
                if (wordInfo.word == Constants.KEYWORD) {
                    createWavFile(wordInfo, audioData)
                }
            }
        } else {
            throw IOException("The input file is not valid '${file.name}'")
        }
    }
}

private fun createWavFile(wordInfo: WordInfo, audioData: ByteArray) {
    val beginOffset = (wordInfo.startTime * Constants.SAMPLE_RATE_HERTZ).toInt() * 2
    val endOffset = (wordInfo.endTime * Constants.SAMPLE_RATE_HERTZ).toInt() * 2
    val outputFile = createFile("${Constants.OUTPUT_FOLDER}${UUID.randomUUID()}.${Constants.SUPPORTED_AUDIO_FILE_FORMAT}")

    FileOutputStream(outputFile).use { output ->
        val buffer = ByteArray(endOffset - beginOffset)
        System.arraycopy(audioData, beginOffset, buffer, 0, buffer.size)

        WavFormatter.writeHeader(output, Constants.NUM_CHANNELS, Constants.SAMPLE_RATE_HERTZ, Constants.BITS_PER_SAMPLE)
        output.write(buffer)
        WavFormatter.updateHeader(outputFile)
    }
}

private fun recognize(audioData: ByteArray): Result {
    Model(Constants.MODEL_FOLDER).use { model ->
        Recognizer(model, Constants.SAMPLE_RATE_HERTZ.toFloat()).use { recognizer ->
            recognizer.acceptWaveForm(audioData, audioData.size)

            val finalResult = recognizer.finalResult.replace("(\\d+)(,)(\\d+)".toRegex(), "$1.$3")
            return Serialization.fromJson(finalResult, Result::class.java)
        }
    }
}

private fun createFile(fileName: String) = File(fileName).apply {
    parentFile.mkdirs()
    createNewFile()
}

private fun isValidWavFile(input: InputStream): Boolean {
    val wavHeader = WavHeaderReader.read(input)
    return wavHeader.run {
        sampleRate == Constants.SAMPLE_RATE_HERTZ &&
            numChannels == Constants.NUM_CHANNELS &&
            bitsPerSample == Constants.BITS_PER_SAMPLE
    }
}