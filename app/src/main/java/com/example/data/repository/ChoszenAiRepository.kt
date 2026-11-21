package com.example.data.repository

import android.graphics.Bitmap
import android.util.Log
import com.example.data.model.AiEditPlan
import com.example.data.model.AspectRatioOption
import com.example.data.model.BeatSyncCue
import com.example.data.model.FootballStyle
import com.example.data.model.StyleRecreationProfile
import com.google.firebase.Firebase
import com.google.firebase.ai.ai
import com.google.firebase.ai.type.AspectRatio
import com.google.firebase.ai.type.ImageConfig
import com.google.firebase.ai.type.ImagePart
import com.google.firebase.ai.type.ImageSize
import com.google.firebase.ai.type.ResponseModality
import com.google.firebase.ai.type.Schema
import com.google.firebase.ai.type.content
import com.google.firebase.ai.type.generationConfig
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.withTimeout
import org.json.JSONArray
import org.json.JSONObject

private const val TAG = "ChoszenAiRepo"

class ChoszenAiRepository {

    private val editPlanSchema = Schema.obj(
        properties = mapOf(
            "title" to Schema.string("Titulo curto e impactante do edit"),
            "summary" to Schema.string("Resumo executivo em portugues de como a edicao sera montada"),
            "importantMoments" to Schema.array(Schema.string(), "Momentos importantes identificados na midia"),
            "beatSyncedCuts" to Schema.array(Schema.string(), "Cortes sincronizados com a batida e timestamps"),
            "transitions" to Schema.array(Schema.string(), "Transicoes recomendadas entre os clipes"),
            "visualEffects" to Schema.array(Schema.string(), "Efeitos visuais e shaders recomendados"),
            "lightingAndContrast" to Schema.string("Ajuste de iluminacao, exposicao e contraste"),
            "cameraAndZoom" to Schema.string("Movimentos de camera, zoom dinamico e tracking"),
            "speedEffects" to Schema.string("Curvas de velocidade e velocity ramps"),
            "captions" to Schema.array(Schema.string(), "Textos curtos e legendas sugeridas para o edit"),
            "recommendedContrast" to Schema.double("Contraste entre -1.0 e 1.0"),
            "recommendedSaturation" to Schema.double("Saturacao entre -1.0 e 1.0"),
            "recommendedBrightness" to Schema.double("Brilho entre -0.5 e 0.5"),
            "recommendedSpeed" to Schema.double("Velocidade media sugerida entre 0.5 e 2.0")
        )
    )

    private val styleRecreationSchema = Schema.obj(
        properties = mapOf(
            "styleName" to Schema.string("Nome do estilo identificado na referencia"),
            "transitions" to Schema.string("Analise detalhada das transicoes usadas"),
            "visualEffects" to Schema.string("Efeitos visuais detectados"),
            "speedProfile" to Schema.string("Comportamento de velocidade, slow motion e velocity"),
            "synchronization" to Schema.string("Como os cortes e efeitos sincronizam com batidas"),
            "zoomBehavior" to Schema.string("Uso de zoom, punch-in e movimento de camera"),
            "colorPalette" to Schema.string("Paleta de cores, contraste e color grading"),
            "lighting" to Schema.string("Iluminacao, flashes, vinheta e brilho"),
            "typography" to Schema.string("Estilo de tipografia e legendas sem copiar creditos de terceiros"),
            "watermarkPlacement" to Schema.string("Posicionamento sugerido para a marca propria autorizada do usuario (sem copiar marcas de terceiros)"),
            "overallStyle" to Schema.string("Resumo geral da estetica da edicao"),
            "ethicalNotice" to Schema.string("Confirmacao de que nenhuma marca d'agua, logo protegido ou credito de terceiro foi copiado"),
            "contrast" to Schema.double("Contraste sugerido entre -1.0 e 1.0"),
            "saturation" to Schema.double("Saturacao sugerida entre -1.0 e 1.0"),
            "brightness" to Schema.double("Brilho sugerido entre -0.5 e 0.5"),
            "speedMultiplier" to Schema.double("Velocidade media entre 0.5 e 2.0"),
            "recommendedBpm" to Schema.integer("BPM estimado entre 80 e 180"),
            "recommendedAspectRatio" to Schema.enumeration(listOf("9:16", "1:1", "16:9"), "Proporcao recomendada")
        )
    )

    private val beatSyncSchema = Schema.obj(
        properties = mapOf(
            "cues" to Schema.array(
                Schema.obj(
                    properties = mapOf(
                        "timestampSec" to Schema.double("Segundo exato do marcador na musica"),
                        "action" to Schema.string("Acao de edicao no beat (ex: Flash Cut + Impact Shake)"),
                        "intensity" to Schema.enumeration(listOf("Leve", "Medio", "Drop Forte"))
                    )
                )
            )
        )
    )

    private val editPlanModel by lazy {
        Firebase.ai.generativeModel(
            modelName = "gemini-3.8-flash",
            generationConfig = generationConfig {
                responseMimeType = "application/json"
                responseSchema = editPlanSchema
            }
        )
    }

    private val styleModel by lazy {
        Firebase.ai.generativeModel(
            modelName = "gemini-3.8-flash",
            generationConfig = generationConfig {
                responseMimeType = "application/json"
                responseSchema = styleRecreationSchema
            }
        )
    }

    private val beatModel by lazy {
        Firebase.ai.generativeModel(
            modelName = "gemini-3.8-flash",
            generationConfig = generationConfig {
                responseMimeType = "application/json"
                responseSchema = beatSyncSchema
            }
        )
    }

    suspend fun generateAutoEditPlan(
        prompt: String,
        subject: String,
        style: FootballStyle,
        musicTitle: String,
        bpm: Int,
        aspectRatio: AspectRatioOption,
        sampleFrames: List<Bitmap>,
        clipSummary: String
    ): Result<AiEditPlan> {
        return try {
            val response = withTimeout(60_000L) {
                editPlanModel.generateContent(
                    content {
                        text(
                            """
                            Voce e o motor de inteligencia artificial CHOSZEN EDIT AI, especialista em edits virais de futebol (estilo editorial avancado para TikTok, Reels e Shorts) e edicao geral de video.
                            Responda estritamente em Portugues do Brasil seguindo o schema JSON.
                            
                            Contexto do projeto:
                            - Assunto / Jogador / Clube: $subject
                            - Estilo selecionado: ${style.displayName} (${style.tagline})
                            - Musica / Ritmo: $musicTitle ($bpm BPM)
                            - Formato alvo: ${aspectRatio.label} (${aspectRatio.platformHint})
                            - Midias selecionadas: $clipSummary
                            - Pedido do usuario: ${prompt.ifBlank { "Criar um edit dinamico e viral destacando os melhores lances com cortes no beat." }}
                            
                            Analise os frames enviados (se houver) e produza um plano completo com momentos importantes, cortes sincronizados com a batida, transicoes, efeitos visuais, iluminacao/contraste, camera/zoom, curvas de velocidade e legendas curtas.
                            """.trimIndent()
                        )
                        sampleFrames.take(4).forEach { frame ->
                            image(frame)
                        }
                    }
                )
            }
            val jsonText = response.text ?: throw IllegalStateException("A IA retornou uma resposta vazia.")
            val json = JSONObject(jsonText)
            val plan = AiEditPlan(
                title = json.optString("title", "Edit $subject • ${style.displayName}"),
                summary = json.optString("summary", ""),
                importantMoments = json.optJSONArray("importantMoments").toStringList(),
                beatSyncedCuts = json.optJSONArray("beatSyncedCuts").toStringList(),
                transitions = json.optJSONArray("transitions").toStringList(),
                visualEffects = json.optJSONArray("visualEffects").toStringList(),
                lightingAndContrast = json.optString("lightingAndContrast", ""),
                cameraAndZoom = json.optString("cameraAndZoom", ""),
                speedEffects = json.optString("speedEffects", ""),
                captions = json.optJSONArray("captions").toStringList(),
                recommendedContrast = json.optDouble("recommendedContrast", style.contrast.toDouble())
                    .toFloat().coerceIn(-1f, 1f),
                recommendedSaturation = json.optDouble("recommendedSaturation", style.saturation.toDouble())
                    .toFloat().coerceIn(-1f, 1f),
                recommendedBrightness = json.optDouble("recommendedBrightness", style.brightness.toDouble())
                    .toFloat().coerceIn(-0.5f, 0.5f),
                recommendedSpeed = json.optDouble("recommendedSpeed", style.speedMultiplier.toDouble())
                    .toFloat().coerceIn(0.5f, 2.0f)
            )
            Result.success(plan)
        } catch (e: TimeoutCancellationException) {
            Log.e(TAG, "Auto edit plan timed out", e)
            Result.failure(Exception("A análise da IA expirou após 60 segundos. Tente novamente."))
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Log.e(TAG, "Failed to generate auto edit plan", e)
            Result.failure(Exception(e.localizedMessage ?: "Falha ao gerar plano de edição com IA."))
        }
    }

    suspend fun analyzeReferenceStyle(
        referenceFrames: List<Bitmap>,
        userNote: String,
        authorizedWatermark: String
    ): Result<StyleRecreationProfile> {
        return try {
            val response = withTimeout(60_000L) {
                styleModel.generateContent(
                    content {
                        text(
                            """
                            Voce e a ferramenta RECRIAR ESTILO do CHOSZEN EDIT AI.
                            Analise os frames da edicao de referencia enviada pelo usuario e extraia exclusivamente as tecnicas de edicao e estetica visual:
                            1. Transicoes
                            2. Efeitos visuais
                            3. Velocidade
                            4. Sincronizacao
                            5. Zoom
                            6. Cores
                            7. Iluminacao
                            8. Tipografia
                            9. Posicionamento da marca-d'agua
                            10. Estilo geral da edicao
                            
                            REGRA DE SEGURANCA OBRIGATORIA:
                            - NUNCA copie marcas-d'agua de terceiros, logotipos protegidos, @handles ou creditos de outros editores visiveis na referencia.
                            - Descreva apenas a tecnica de posicionamento e oriente o uso exclusivo da marca autorizada do usuario ("$authorizedWatermark").
                            
                            Observacao adicional do usuario: ${userNote.ifBlank { "Extrair o estilo visual completo da referencia." }}
                            Responda em Portugues do Brasil no formato JSON definido.
                            """.trimIndent()
                        )
                        referenceFrames.take(4).forEach { frame ->
                            image(frame)
                        }
                    }
                )
            }
            val jsonText = response.text ?: throw IllegalStateException("Resposta vazia da IA.")
            val json = JSONObject(jsonText)
            val profile = StyleRecreationProfile(
                styleName = json.optString("styleName", "Estilo Recriado CHOSZEN"),
                transitions = json.optString("transitions", ""),
                visualEffects = json.optString("visualEffects", ""),
                speedProfile = json.optString("speedProfile", ""),
                synchronization = json.optString("synchronization", ""),
                zoomBehavior = json.optString("zoomBehavior", ""),
                colorPalette = json.optString("colorPalette", ""),
                lighting = json.optString("lighting", ""),
                typography = json.optString("typography", ""),
                watermarkPlacement = json.optString("watermarkPlacement", ""),
                overallStyle = json.optString("overallStyle", ""),
                ethicalNotice = json.optString(
                    "ethicalNotice",
                    "Apenas técnicas visuais analisadas. Nenhuma marca-d'água ou crédito de terceiros foi copiado."
                ),
                contrast = json.optDouble("contrast", 0.30).toFloat().coerceIn(-1f, 1f),
                saturation = json.optDouble("saturation", 0.15).toFloat().coerceIn(-1f, 1f),
                brightness = json.optDouble("brightness", 0.0).toFloat().coerceIn(-0.5f, 0.5f),
                speedMultiplier = json.optDouble("speedMultiplier", 1.15).toFloat().coerceIn(0.5f, 2.0f),
                recommendedBpm = json.optInt("recommendedBpm", 135).coerceIn(80, 180),
                recommendedAspectRatio = json.optString("recommendedAspectRatio", "9:16")
            )
            Result.success(profile)
        } catch (e: TimeoutCancellationException) {
            Log.e(TAG, "Style recreation timed out", e)
            Result.failure(Exception("A análise de estilo expirou após 60 segundos."))
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Log.e(TAG, "Failed to analyze reference style", e)
            Result.failure(Exception(e.localizedMessage ?: "Falha ao analisar estilo de referência."))
        }
    }

    suspend fun generateBeatSyncCues(
        musicTitle: String,
        bpm: Int,
        durationSec: Int,
        style: FootballStyle
    ): Result<List<BeatSyncCue>> {
        return try {
            val response = withTimeout(45_000L) {
                beatModel.generateContent(
                    """
                    Gere entre 6 e 10 marcadores de sincronizacao com batida (beat sync) para um edit de ${durationSec}s.
                    Musica: $musicTitle
                    BPM: $bpm
                    Estilo: ${style.displayName}
                    Use timestamps crescentes dentro de 0.0s a ${durationSec}.0s alinhados ao intervalo de batida (${String.format("%.2f", 60.0 / bpm.coerceAtLeast(60))}s por beat).
                    Descreva acoes curtas em Portugues do Brasil (ex: "Flash Cut + Zoom In", "Velocity 0.4x -> 1.8x", "Beat Shake").
                    """.trimIndent()
                )
            }
            val jsonText = response.text ?: throw IllegalStateException("Resposta vazia da IA.")
            val json = JSONObject(jsonText)
            val cuesArr = json.optJSONArray("cues") ?: JSONArray()
            val cues = (0 until cuesArr.length()).mapNotNull { idx ->
                val obj = cuesArr.optJSONObject(idx) ?: return@mapNotNull null
                BeatSyncCue(
                    timestampSec = obj.optDouble("timestampSec", idx * 2.0),
                    action = obj.optString("action", "Corte no Beat"),
                    intensity = obj.optString("intensity", "Medio")
                )
            }
            Result.success(cues)
        } catch (e: TimeoutCancellationException) {
            Log.e(TAG, "Beat sync timed out", e)
            Result.failure(Exception("A sincronização de batidas expirou."))
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Log.e(TAG, "Failed to generate beat cues", e)
            Result.failure(Exception(e.localizedMessage ?: "Falha ao gerar marcadores de batida."))
        }
    }

    suspend fun generateCoverImage(
        subject: String,
        style: FootballStyle,
        customPrompt: String,
        aspectRatio: AspectRatioOption
    ): Result<Bitmap> {
        return try {
            val aiAspectRatio = when (aspectRatio) {
                AspectRatioOption.VERTICAL_9_16 -> AspectRatio.PORTRAIT_9x16
                AspectRatioOption.SQUARE_1_1 -> AspectRatio.SQUARE_1x1
                AspectRatioOption.HORIZONTAL_16_9 -> AspectRatio.LANDSCAPE_16x9
            }
            val config = generationConfig {
                responseModalities = listOf(ResponseModality.IMAGE)
                imageConfig = ImageConfig.builder()
                    .setAspectRatio(aiAspectRatio)
                    .setImageSize(ImageSize.SIZE_1K)
                    .build()
            }
            val model = Firebase.ai.generativeModel(
                modelName = "gemini-nano-banana-2.1",
                generationConfig = config
            )
            val fullPrompt = buildString {
                append("High-impact viral sports video thumbnail cover art featuring football aesthetic for ")
                append(subject)
                append(", style: ")
                append(style.displayName)
                append(" (")
                append(style.tagline)
                append("). Dramatic stadium lighting, crisp depth of field, glowing energy trails, ultra clean composition with space for a title badge. ")
                if (customPrompt.isNotBlank()) {
                    append("Extra creative direction: ")
                    append(customPrompt)
                }
                append(" No random gibberish text or third-party watermarks.")
            }
            val bitmap = withTimeout(60_000L) {
                val response = model.generateContent(fullPrompt)
                val imagePart = response.candidates.firstOrNull()
                    ?.content?.parts?.filterIsInstance<ImagePart>()
                    ?.firstOrNull()
                imagePart?.image
            }
            if (bitmap != null) {
                Result.success(bitmap)
            } else {
                Result.failure(Exception("O modelo não retornou uma imagem de capa."))
            }
        } catch (e: TimeoutCancellationException) {
            Log.e(TAG, "Cover generation timed out", e)
            Result.failure(Exception("A geração da capa expirou após 60 segundos. Tente novamente."))
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Log.e(TAG, "Cover image generation failed", e)
            Result.failure(Exception(e.localizedMessage ?: "Falha ao gerar capa com IA."))
        }
    }

    private fun JSONArray?.toStringList(): List<String> {
        if (this == null) return emptyList()
        return (0 until length()).mapNotNull { optString(it).takeIf { s -> s.isNotBlank() } }
    }
}
