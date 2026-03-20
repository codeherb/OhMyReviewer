package io.yogiyo.ohmyreviewer.data.model

enum class GeminiModel(
    val modelId: String,
    val displayName: String,
    val tier: ModelTier,
) {
    // Gemini 2.5 (Stable)
    GEMINI_2_5_FLASH_LITE(
        modelId = "gemini-2.5-flash-lite",
        displayName = "Gemini 2.5 Flash Lite",
        tier = ModelTier.FREE,
    ),
    GEMINI_2_5_FLASH(
        modelId = "gemini-2.5-flash",
        displayName = "Gemini 2.5 Flash",
        tier = ModelTier.FREE,
    ),
    GEMINI_2_5_PRO(
        modelId = "gemini-2.5-pro",
        displayName = "Gemini 2.5 Pro",
        tier = ModelTier.FREE,
    ),

    // Gemini 3 (Preview)
    GEMINI_3_FLASH_PREVIEW(
        modelId = "gemini-3-flash-preview",
        displayName = "Gemini 3 Flash (Preview)",
        tier = ModelTier.FREE,
    ),
    GEMINI_3_1_FLASH_LITE_PREVIEW(
        modelId = "gemini-3.1-flash-lite-preview",
        displayName = "Gemini 3.1 Flash Lite (Preview)",
        tier = ModelTier.FREE,
    ),
    GEMINI_3_1_PRO_PREVIEW(
        modelId = "gemini-3.1-pro-preview",
        displayName = "Gemini 3.1 Pro (Preview)",
        tier = ModelTier.FREE,
    );

    companion object {
        val DEFAULT = GEMINI_2_5_FLASH_LITE
    }
}

enum class ModelTier {
    FREE,
    PAID,
}
