package com.sam.geminicompose

import android.graphics.Bitmap
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.content
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.update

class MainViewModel : ViewModel() {

    data class UIState(
        var bitmapList: List<Bitmap> = listOf(),
        var content: String = "",
        var generatedContent: String = "",
        var isLoading: Boolean = false
    )

    private val textModel by lazy {
        GenerativeModel(
            modelName = "gemini-pro",
            apiKey = BuildConfig.API_KEY
        )
    }

    private val visionModel by lazy {
        GenerativeModel(
            modelName = "gemini-pro-vision",
            apiKey = BuildConfig.API_KEY
        )
    }

    var state = MutableStateFlow(UIState())
        private set

    fun onContentChanged(text: String) {
        state.update {
            it.copy(
                content = text
            )
        }
    }

    fun clearData() {
        state.update {
            it.copy(
                bitmapList = listOf(),
                content = "",
                generatedContent = "",
            )
        }
    }

    fun addBitmaps(bitmaps: List<Bitmap>) {
        state.update {
            it.copy(
                bitmapList = bitmaps
            )
        }
    }

    suspend fun generate() {

        val bitmaps = state.value.bitmapList
        val geminiModel = if (bitmaps.isEmpty())
            textModel else visionModel


        val inputContent = content {
            bitmaps.onEach {
                image(it)
            }

            text(state.value.content)
        }

        showLoading(true)
        try {
            geminiModel.generateContentStream(inputContent).collect {
                showLoading(false)
                state.value.generatedContent += it.text
            }
        } catch (e: Exception) {
            showLoading(false)
            state.value.generatedContent += e.localizedMessage
        }

    }

    private fun showLoading(isLoading: Boolean) {
        state.update {
            it.copy(
                isLoading = isLoading
            )
        }
    }
}