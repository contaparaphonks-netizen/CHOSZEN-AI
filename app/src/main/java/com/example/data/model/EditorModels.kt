package com.example.data.model

import android.net.Uri

enum class EditToolModule(
    val title: String,
    val subtitle: String
) {
    CREATE_EDIT("Criar Edit", "IA analisa clipes e monta cortes no ritmo"),
    FOOTBALL_EDIT("Edit de Futebol", "Jogadores, seleções e 10 estilos pro"),
    APPLY_EFFECTS("Aplicar Efeitos", "Color grading, FX e Recriar Estilo"),
    SYNC_MUSIC("Sincronizar Música", "BPM, batidas e cortes sincronizados"),
    CREATE_COVER("Criar Capa", "Capas virais com IA generativa"),
    WATERMARK("Marca-d'água", "Editor completo e CHOSZEN™©"),
    ENHANCE_QUALITY("Melhorar Qualidade", "HD a 4K real, nitidez e denoise"),
    TEMPLATES("Modelos Prontos", "Biblioteca de estilos e seus modelos")
}

enum class FootballStyle(
    val displayName: String,
    val tagline: String,
    val defaultBpm: Int,
    val contrast: Float,
    val saturation: Float,
    val brightness: Float,
    val speedMultiplier: Float,
    val glowIntensity: Float,
    val shakeIntensity: Float,
    val zoomIntensity: Float,
    val suggestedMusic: String,
    val transitions: List<String>,
    val effects: List<String>
) {
    PHONK_EDIT(
        "Phonk Edit",
        "Batida agressiva, flash na caixa e contraste alto",
        142, 0.42f, 0.12f, -0.06f, 1.15f, 0.35f, 0.65f, 0.45f,
        "Brazilian Phonk 142 BPM",
        listOf("Flash Cut", "Whip Zoom", "Glitch Cut"),
        listOf("Beat Shake", "High Contrast", "Bass Pump Zoom")
    ),
    VELOCITY_EDIT(
        "Velocity Edit",
        "Curvas de velocidade 0.4x -> 2.0x no drop",
        132, 0.25f, 0.20f, 0.02f, 1.35f, 0.20f, 0.25f, 0.60f,
        "Speed Trap / Funk Rave 132 BPM",
        listOf("Speed Ramp", "Optical Flow Cut", "Pull-In"),
        listOf("Velocity Curve", "Motion Blur", "Beat Flash")
    ),
    AURA_EDIT(
        "Aura Edit",
        "Brilho etéreo, câmera lenta dramática e tons frios",
        118, 0.28f, -0.10f, -0.04f, 0.85f, 0.78f, 0.15f, 0.35f,
        "Slowed Phonk / Aura Synth 118 BPM",
        listOf("Dissolve Glow", "Smooth Zoom", "Cross Fade"),
        listOf("Aura Halo Glow", "Cold Cyan Tint", "Slow-Mo Impact")
    ),
    FOOTBALL_CINEMATIC(
        "Football Cinematic",
        "Look de transmissão UCL com profundidade dramática",
        105, 0.30f, 0.10f, 0.0f, 0.90f, 0.25f, 0.10f, 0.30f,
        "Orchestral Epic Trailer 105 BPM",
        listOf("Cinematic Cut", "Luma Fade", "Letterbox Reveal"),
        listOf("Film Grain", "Teal & Gold Grade", "Smooth Dolly Zoom")
    ),
    DARK_EDIT(
        "Dark Edit",
        "Sombras profundas, destaque neon e clima intenso",
        128, 0.50f, -0.25f, -0.14f, 1.0f, 0.40f, 0.35f, 0.40f,
        "Dark Drill / Memphis Bass 128 BPM",
        listOf("Blackout Cut", "Strobe Transition", "Whip Pan"),
        listOf("Dark Vignette", "Desaturated Shadows", "Edge Highlight")
    ),
    SMOOTH_EDIT(
        "Smooth Edit",
        "Transições fluidas e curvas suaves 60fps feel",
        124, 0.15f, 0.18f, 0.05f, 1.0f, 0.18f, 0.05f, 0.25f,
        "Melodic House / Chill Trap 124 BPM",
        listOf("Smooth Slide", "Zoom Blur", "Morph Cut"),
        listOf("Smooth Stabilization", "Clean Clarity", "Warm Highlights")
    ),
    GLOW_EDIT(
        "Glow Edit",
        "Realce luminoso nas chuteiras, bola e uniforme",
        136, 0.22f, 0.32f, 0.06f, 1.10f, 0.90f, 0.20f, 0.35f,
        "Hyperpop / Electro Phonk 136 BPM",
        listOf("Light Leak", "Glow Flash", "Zoom Out"),
        listOf("Bloom Neon Glow", "Vibrant Saturation", "Sparkle Highlights")
    ),
    SHAKE_EDIT(
        "Shake Edit",
        "Impacto sísmico em cada chute, drible e gol",
        148, 0.36f, 0.15f, 0.0f, 1.20f, 0.30f, 0.92f, 0.55f,
        "Hardstyle / Aggressive Funk 148 BPM",
        listOf("Impact Shake", "RGB Split Cut", "Hard Cut"),
        listOf("XY Camera Shake", "Chromatic Impact", "Snap Zoom")
    ),
    FOUR_K_FOOTBALL_EDIT(
        "4K Football Edit",
        "Ultra nitidez, cores fiéis e contraste de estádio",
        120, 0.24f, 0.22f, 0.04f, 1.0f, 0.15f, 0.10f, 0.20f,
        "Clean Stadium Anthem 120 BPM",
        listOf("Crisp Cut", "Whip Pan Clean", "Match Cut"),
        listOf("Sharpness Boost", "True Stadium Color", "Noise Reduction")
    ),
    HIGHLIGHT_EDIT(
        "Highlight Edit",
        "Ritmo ágil focado nos melhores lances e gols",
        130, 0.20f, 0.20f, 0.03f, 1.15f, 0.22f, 0.20f, 0.30f,
        "Upbeat Trap / Afro Beats 130 BPM",
        listOf("Beat Cut", "Slide Left", "Flash Cut"),
        listOf("Highlight Markers", "Dynamic Zoom", "Auto Captions")
    )
}

enum class FootballSubjectPreset(val label: String, val tag: String) {
    NEYMAR("Neymar Jr.", "Dribles & Magia"),
    MESSI("Lionel Messi", "Gênio & Gols"),
    CRISTIANO("Cristiano Ronaldo", "SIUU & Potência"),
    MBAPPE("Kylian Mbappé", "Velocidade Pura"),
    VINI_JR("Vini Jr.", "Baila & UCL"),
    ENDRICK("Endrick", "Nova Geração"),
    RAPHINHA("Raphinha", "Raça & Gols"),
    CLUBES_SELECOES("Clubes e Seleções", "Times & Copas"),
    PERSONALIZADO("Outro Jogador / Time", "Personalizado")
}

enum class AspectRatioOption(
    val label: String,
    val code: String,
    val ratioFloat: Float,
    val platformHint: String,
    val baseWidth: Int,
    val baseHeight: Int
) {
    VERTICAL_9_16("Vertical 9:16", "9:16", 9f / 16f, "TikTok • Reels • Shorts", 1080, 1920),
    SQUARE_1_1("Quadrado 1:1", "1:1", 1f, "Feed • Instagram", 1080, 1080),
    HORIZONTAL_16_9("Horizontal 16:9", "16:9", 16f / 9f, "YouTube • Widescreen", 1920, 1080)
}

enum class QualityPreset(
    val label: String,
    val shortSidePx: Int,
    val longSidePx: Int,
    val bitrateBps: Int
) {
    HD("HD", 720, 1280, 5_000_000),
    FULL_HD("Full HD", 1080, 1920, 10_000_000),
    QHD_2K("2K", 1440, 2560, 18_000_000),
    UHD_4K("4K", 2160, 3840, 32_000_000)
}

enum class WatermarkPosition(val label: String) {
    TOP_LEFT("Topo Esquerda"),
    TOP_CENTER("Topo Centro"),
    TOP_RIGHT("Topo Direita"),
    CENTER_LEFT("Centro Esquerda"),
    CENTER("Centro"),
    CENTER_RIGHT("Centro Direita"),
    BOTTOM_LEFT("Base Esquerda"),
    BOTTOM_CENTER("Base Centro"),
    BOTTOM_RIGHT("Base Direita")
}

enum class WatermarkMode(val label: String) {
    TEXT("Texto"),
    IMAGE("Imagem / Logo")
}

data class WatermarkConfig(
    val enabled: Boolean = true,
    val mode: WatermarkMode = WatermarkMode.TEXT,
    val text: String = "CHOSZEN™©",
    val imageUri: Uri? = null,
    val sizeScale: Float = 1.0f,
    val opacity: Float = 0.85f,
    val position: WatermarkPosition = WatermarkPosition.BOTTOM_RIGHT,
    val applyFullDuration: Boolean = true,
    val startSec: Float = 0f,
    val endSec: Float = 15f,
    val shadowEnabled: Boolean = true,
    val outlineEnabled: Boolean = true
)

data class DeviceVideoCapabilities(
    val maxSupportedWidth: Int = 1920,
    val maxSupportedHeight: Int = 1080,
    val supports2K: Boolean = false,
    val supports4K: Boolean = false,
    val hardwareEncoderName: String = "Padrão Android",
    val stabilizationAvailable: Boolean = true
)

data class SourceMediaInfo(
    val uri: Uri,
    val isImage: Boolean,
    val width: Int,
    val height: Int,
    val durationMs: Long,
    val displayLabel: String
)

data class AiEditPlan(
    val title: String,
    val summary: String,
    val importantMoments: List<String>,
    val beatSyncedCuts: List<String>,
    val transitions: List<String>,
    val visualEffects: List<String>,
    val lightingAndContrast: String,
    val cameraAndZoom: String,
    val speedEffects: String,
    val captions: List<String>,
    val recommendedContrast: Float,
    val recommendedSaturation: Float,
    val recommendedBrightness: Float,
    val recommendedSpeed: Float
)

data class StyleRecreationProfile(
    val styleName: String,
    val transitions: String,
    val visualEffects: String,
    val speedProfile: String,
    val synchronization: String,
    val zoomBehavior: String,
    val colorPalette: String,
    val lighting: String,
    val typography: String,
    val watermarkPlacement: String,
    val overallStyle: String,
    val ethicalNotice: String,
    val contrast: Float,
    val saturation: Float,
    val brightness: Float,
    val speedMultiplier: Float,
    val recommendedBpm: Int,
    val recommendedAspectRatio: String
)

data class BeatSyncCue(
    val timestampSec: Double,
    val action: String,
    val intensity: String
)

data class ActiveEditProjectState(
    val projectTitle: String = "Novo Edit CHOSZEN",
    val mediaItems: List<SourceMediaInfo> = emptyList(),
    val musicUri: Uri? = null,
    val musicTitle: String = "Brazilian Phonk 142 BPM (Sugerida)",
    val bpm: Int = 142,
    val subjectPreset: FootballSubjectPreset = FootballSubjectPreset.NEYMAR,
    val customSubjectText: String = "",
    val selectedStyle: FootballStyle = FootballStyle.PHONK_EDIT,
    val prompt: String = "",
    val aspectRatio: AspectRatioOption = AspectRatioOption.VERTICAL_9_16,
    val qualityPreset: QualityPreset = QualityPreset.FULL_HD,
    val lightingEnhance: Boolean = true,
    val noiseReduction: Boolean = true,
    val sharpnessBoost: Boolean = true,
    val colorAdjustment: Boolean = true,
    val stabilizationEnabled: Boolean = false,
    val contrast: Float = 0.32f,
    val saturation: Float = 0.15f,
    val brightness: Float = 0.0f,
    val glowIntensity: Float = 0.35f,
    val shakeIntensity: Float = 0.50f,
    val zoomIntensity: Float = 0.40f,
    val speedMultiplier: Float = 1.15f,
    val captionText: String = "",
    val watermarkConfig: WatermarkConfig = WatermarkConfig(),
    val aiEditPlan: AiEditPlan? = null,
    val styleRecreation: StyleRecreationProfile? = null,
    val beatSyncCues: List<BeatSyncCue> = emptyList()
) {
    val activeSubjectName: String
        get() = if (subjectPreset == FootballSubjectPreset.PERSONALIZADO ||
            subjectPreset == FootballSubjectPreset.CLUBES_SELECOES
        ) {
            customSubjectText.ifBlank { subjectPreset.label }
        } else {
            subjectPreset.label
        }
}

data class CuratedTemplate(
    val id: String,
    val name: String,
    val style: FootballStyle,
    val category: String,
    val durationSec: Int,
    val suggestedMusic: String,
    val transitions: List<String>,
    val effects: List<String>,
    val aspectRatio: AspectRatioOption
)

data class TutorialGuide(
    val id: String,
    val title: String,
    val durationTag: String,
    val steps: List<String>
)

sealed interface ExportUiState {
    data object Idle : ExportUiState
    data class Exporting(val progressPercent: Int, val stageLabel: String) : ExportUiState
    data class Completed(
        val outputFilePath: String,
        val outputUri: Uri,
        val fileSizeBytes: Long,
        val resolutionLabel: String,
        val aspectRatioLabel: String,
        val qualityWarning: String? = null
    ) : ExportUiState
    data class Failed(val message: String) : ExportUiState
}
