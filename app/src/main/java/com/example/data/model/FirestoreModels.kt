package com.example.data.model

import androidx.annotation.Keep
import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.IgnoreExtraProperties
import com.google.firebase.firestore.ServerTimestamp

@Keep
@IgnoreExtraProperties
data class EditTemplate(
    @DocumentId val id: String = "",
    val userId: String = "",
    val name: String = "",
    val style: String = "Phonk Edit",
    val category: String = "Futebol",
    val durationSec: Long = 15L,
    val suggestedMusic: String = "Brazilian Phonk 140 BPM",
    val transitions: List<String> = emptyList(),
    val effects: List<String> = emptyList(),
    val aspectRatio: String = "9:16",
    val contrast: Double = 0.25,
    val saturation: Double = 0.15,
    val brightness: Double = 0.0,
    val speedMultiplier: Double = 1.0,
    val watermarkText: String = "CHOSZEN™©",
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
) {
    fun toCreateMap(uid: String): Map<String, Any> = mapOf(
        "userId" to uid,
        "name" to name.trim().take(80),
        "style" to style.trim().take(50),
        "category" to category.trim().take(40),
        "durationSec" to durationSec.coerceIn(1L, 600L),
        "suggestedMusic" to suggestedMusic.trim().take(100),
        "transitions" to transitions.take(10).map { it.take(60) },
        "effects" to effects.take(12).map { it.take(60) },
        "aspectRatio" to if (aspectRatio in listOf("9:16", "1:1", "16:9")) aspectRatio else "9:16",
        "contrast" to contrast.coerceIn(-1.0, 1.0),
        "saturation" to saturation.coerceIn(-1.0, 1.0),
        "brightness" to brightness.coerceIn(-1.0, 1.0),
        "speedMultiplier" to speedMultiplier.coerceIn(0.25, 4.0),
        "watermarkText" to watermarkText.trim().take(50),
        "createdAt" to FieldValue.serverTimestamp(),
        "updatedAt" to FieldValue.serverTimestamp()
    )
}

@Keep
@IgnoreExtraProperties
data class EditProject(
    @DocumentId val id: String = "",
    val userId: String = "",
    val title: String = "",
    val subject: String = "",
    val style: String = "Phonk Edit",
    val prompt: String = "",
    val aspectRatio: String = "9:16",
    val qualityPreset: String = "Full HD",
    val aiSummary: String = "",
    val watermarkText: String = "CHOSZEN™©",
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
) {
    fun toCreateMap(uid: String): Map<String, Any> = mapOf(
        "userId" to uid,
        "title" to title.trim().take(100),
        "subject" to subject.trim().take(80),
        "style" to style.trim().take(50),
        "prompt" to prompt.trim().take(1000),
        "aspectRatio" to if (aspectRatio in listOf("9:16", "1:1", "16:9")) aspectRatio else "9:16",
        "qualityPreset" to if (qualityPreset in listOf("HD", "Full HD", "2K", "4K")) qualityPreset else "Full HD",
        "aiSummary" to aiSummary.trim().take(3000),
        "watermarkText" to watermarkText.trim().take(50),
        "createdAt" to FieldValue.serverTimestamp(),
        "updatedAt" to FieldValue.serverTimestamp()
    )
}

@Keep
@IgnoreExtraProperties
data class SupportTicket(
    @DocumentId val id: String = "",
    val userId: String = "",
    val subject: String = "",
    val category: String = "Geral",
    val priority: String = "media",
    val description: String = "",
    val status: String = "aberto",
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
) {
    fun toCreateMap(uid: String): Map<String, Any> = mapOf(
        "userId" to uid,
        "subject" to subject.trim().take(120),
        "category" to category.trim().take(40),
        "priority" to if (priority in listOf("baixa", "media", "alta")) priority else "media",
        "description" to description.trim().take(2000),
        "status" to "aberto",
        "createdAt" to FieldValue.serverTimestamp(),
        "updatedAt" to FieldValue.serverTimestamp()
    )
}
