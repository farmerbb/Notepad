package com.farmerbb.notepad.utils

import com.farmerbb.notepad.model.Note

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.encodeToString
import java.text.SimpleDateFormat
import java.util.*


@Serializable
data class SerializableNote(val text: String, val title: String, val date: String)

fun serializeNotes(notes: List<Note>) : String {
    val serializableNotes = notes.map { note ->
        val date = SimpleDateFormat("yyyy-MM-dd-HH-mm", Locale.getDefault()).format(note.date) ?: ""
        SerializableNote(note.text, note.title, date) }
    return Json.encodeToString(serializableNotes)
}