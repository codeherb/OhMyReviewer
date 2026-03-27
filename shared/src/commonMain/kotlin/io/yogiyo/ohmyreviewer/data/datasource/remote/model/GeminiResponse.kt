package io.yogiyo.ohmyreviewer.data.datasource.remote.model

import kotlinx.serialization.Serializable

@Serializable
data class GeminiResponse(
    val candidates: List<Candidate>? = null,
    val error: GeminiError? = null
)

@Serializable
data class Candidate(
    val content: CandidateContent? = null
)

@Serializable
data class CandidateContent(
    val parts: List<CandidatePart>? = null
)

@Serializable
data class CandidatePart(
    val text: String? = null
)

@Serializable
data class GeminiError(
    val code: Int? = null,
    val message: String? = null
)
