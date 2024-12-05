package app.coreply.coreplyapp.utils

import android.util.Log

class ChatContents {
    // Contains a list of ChatMessage objects
    var chatContents: MutableList<ChatMessage> = mutableListOf()

    // Add a ChatMessage object to the list
    fun addChatMessage(chatMessage: ChatMessage) {
        chatContents.add(chatMessage)
    }

    // compare the chatContents list with another ChatContents list and combine them if they have ChatMessage objects in common
    // Returns a boolean, true if needs new suggestions
    fun combineChatContents(other: MutableList<ChatMessage>): Boolean {
        if (chatContents.size < 1 || other.size < 1) {
            chatContents = other
            return true
        } else{
            // Append new messages to the chatContents list
            if (other[0] in chatContents){
                var newEnd = false
                for (i in other){
                    if (i !in chatContents){
                        chatContents.add(i)
                        newEnd = true
                    }
                }
                return newEnd
            } else if (chatContents[0] in other){
                // Insert new messages to the top of chatContents list
                for (i in chatContents){
                    if (i !in other){
                        other.add(i)
                    }
                }
                chatContents = other
                return false
            } else {
                chatContents = other
                return true
            }
        }
    }
    fun getOpenAIFormat(): MutableList<com.aallam.openai.api.chat.ChatMessage> {
        Log.v("ChatContents", chatContents.toString())
        return chatContents.map { com.aallam.openai.api.chat.ChatMessage(
            role = it.getRole(),
            content = it.message
        ) }.toMutableList()
    }

    override fun toString(): String {
        var str = ""
        for (i in chatContents){
            str += i.toString() + "\n"
        }
        return str
    }

}