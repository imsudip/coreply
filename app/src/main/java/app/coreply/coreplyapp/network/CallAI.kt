/**
 * coreply
 *
 * Copyright (C) 2024 coreply
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

package app.coreply.coreplyapp.network

import android.util.Log
import com.aallam.openai.api.chat.ChatCompletionRequest
import com.aallam.openai.api.chat.ChatMessage
import com.aallam.openai.api.chat.ChatRole
import com.aallam.openai.api.model.ModelId
import com.aallam.openai.client.OpenAI
import com.aallam.openai.client.OpenAIConfig
import com.aallam.openai.client.OpenAIHost
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.conflate
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.launch
import app.coreply.coreplyapp.utils.ChatContents
import app.coreply.coreplyapp.utils.PreferenceHelper
import app.coreply.coreplyapp.utils.SuggestionUpdateListener
import java.util.concurrent.ConcurrentHashMap


class SuggestionStorageClass(private var listener: SuggestionUpdateListener? = null) {
    private val _suggestionHistory = ConcurrentHashMap<String, String>()
    fun getSuggestion(toBeCompleted: String): String? {
        if (toBeCompleted.isBlank()) {
            if (_suggestionHistory.containsKey("")) {
                return _suggestionHistory[""]!!
            }
        }
        for (i in 0..toBeCompleted.length) {
            val target: String = toBeCompleted.substring(0, i)
            if (_suggestionHistory.containsKey(target)) {
                val starting = toBeCompleted.substring(i)
                val suggestion = _suggestionHistory[target]!!
                if (starting.isEmpty() || suggestion.startsWith(starting)) {
                    return suggestion.substring(starting.length)
                }
            }
        }
        return null;
    }

    fun clearSuggestion() {
        _suggestionHistory.clear()
    }

    fun setSuggestionUpdateListener(listener: SuggestionUpdateListener) {
        this.listener = listener
    }

    fun updateSuggestion(typingInfo: TypingInfo, newSuggestion: String) {
        _suggestionHistory[typingInfo.currentTypingTrimmed] = newSuggestion
        listener?.onSuggestionUpdated(typingInfo, newSuggestion)
    }
}

data class TypingInfo(val pastMessages: ChatContents, val currentTyping: String) {
    val currentTypingTrimmed = currentTyping.substring(0, currentTyping.length - currentTyping.split(" ").last().length).trimEnd()
}

class CallAI(val suggestionStorage: SuggestionStorageClass) {
    private val coroutineScope = CoroutineScope(Dispatchers.IO)

    // Flow to handle debouncing of user input
    private val userInputFlow = MutableSharedFlow<TypingInfo>(replay = 1)

    init {
        // Launch a coroutine to collect debounced user input and fetch suggestions
        coroutineScope.launch {
            userInputFlow // adjust debounce delay as needed
                .debounce(500).conflate()
                .collectLatest { typingInfo ->
                    fetchSuggestions(typingInfo)
                }
        }
    }

    fun onUserInputChanged(typingInfo: TypingInfo) {
        // Emit user input to the flow
        coroutineScope.launch {
            userInputFlow.emit(typingInfo)
        }
    }

    private suspend fun fetchSuggestions(typingInfo: TypingInfo) {
        try {
            var suggestions =
                requestSuggestionsFromServer(typingInfo)
            suggestions = suggestions.replace("\n", " ")
            if (suggestions.startsWith(" ")) {
                suggestions = " " + suggestions.trim()
            }
            suggestionStorage.updateSuggestion(typingInfo, suggestions.trimEnd())
        } catch (e: Exception) {
            // Handle exceptions such as network errors
            e.printStackTrace()
        }
    }

    private suspend fun requestSuggestionsFromServer(
        typingInfo: TypingInfo
    ): String {
        var baseUrl = PreferenceHelper["customApiUrl", "https://api.openai.com/v1/"]
        if (!baseUrl.endsWith("/")) {
            baseUrl += "/"
        }
        val host = OpenAIHost(
            baseUrl = baseUrl,
        )
        val config = OpenAIConfig(
            host = host,
            token = PreferenceHelper["customApiKey", ""],
        )

        val openAI = OpenAI(config)
        val request = ChatCompletionRequest(
            temperature = PreferenceHelper["temperature", 3] / 10.0,
            model = ModelId(PreferenceHelper["customModelName", ""]),
            topP = PreferenceHelper["topp", 5] / 10.0,
            messages = listOf(
                ChatMessage(
                    role = ChatRole.System,
                    content = PreferenceHelper["customSystemPrompt","You are now texting a user. The symbol '>>' Indicates the start of a message, or the end of the message turn.\n'//' indicates a comment line, which describes the message in the next line.\n\nFor example:\n>>Hello\n// Next line is a message starting with 'Fre':\n>>Free now?\n>>\n\nYour output should always adhere to the given format, and match the tone and style of the text."]
                ))+typingInfo.pastMessages.getCoreplyFormat(typingInfo),

            stop = listOf("\n", ">>","//",",",".","?","!"),
        )
        Log.v("OpenAI", request.messages.toString())
        val response = openAI.chatCompletion(request)
        Log.v("OpenAI", response.choices.first().message.content!!)
        return (if (typingInfo.currentTypingTrimmed.endsWith(" ")) (response.choices.first().message.content ?: "").trimEnd().trimEnd('>').trim() else (response.choices.first().message.content ?: "").trimEnd().trimEnd('>').trimEnd())
    }
}