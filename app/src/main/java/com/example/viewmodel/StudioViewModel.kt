package com.example.viewmodel

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import android.util.Log
import androidx.annotation.OptIn
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import androidx.media3.common.Effect
import androidx.media3.common.util.UnstableApi
import androidx.media3.transformer.Transformer
import com.example.core.UiState
import com.example.data.model.ActiveEditProjectState
import com.example.data.model.AiEditPlan
import com.example.data.model.AspectRatioOption
import com.example.data.model.BeatSyncCue
import com.example.data.model.CuratedTemplate
import com.example.data.model.DeviceVideoCapabilities
import com.example.data.model.EditProject
import com.example.data.model.EditTemplate
import com.example.data.model.ExportUiState
import com.example.data.model.FootballStyle
import com.example.data.model.FootballSubjectPreset
import com.example.data.model.QualityPreset
import com.example.data.model.StyleRecreationProfile
import com.example.data.model.SupportTicket
import com.example.data.model.WatermarkConfig
import com.example.data.model.WatermarkMode
import com.example.data.model.WatermarkPosition
import com.example.data.repository.ChoszenAiRepository
import com.example.data.repository.StudioFirestoreRepository
import com.example.media.MediaProcessingEngine
import com.google.firebase.firestore.FirebaseFirestoreException
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

private const val TAG = "StudioViewModel"

class StudioViewModel(
    private val firestoreRepository: StudioFirestoreRepository,
    private val aiRepository: ChoszenAiRepository,
    private val mediaEngine: MediaProcessingEngine,
    private val currentUserId: String
) : ViewModel() {

    val deviceCapabilities: DeviceVideoCapabilities = mediaEngine.queryDeviceVideoCapabilities()

    private val _projectState = MutableStateFlow(ActiveEditProjectState())
    val projectState: StateFlow<ActiveEditProjectState> = _projectState.asStateFlow()

    private val _aiPlanState = MutableStateFlow<UiState<AiEditPlan>>(UiState.Idle)
    val aiPlanState: StateFlow<UiState<AiEditPlan>> = _aiPlanState.asStateFlow()

    private val _styleRecreationState = MutableStateFlow<UiState<StyleRecreationProfile>>(UiState.Idle)
    val styleRecreationState: StateFlow<UiState<StyleRecreationProfile>> = _styleRecreationState.asStateFlow()

    private val _beatSyncState = MutableStateFlow<UiState<List<BeatSyncCue>>>(UiState.Idle)
    val beatSyncState: StateFlow<UiState<List<BeatSyncCue>>> = _beatSyncState.asStateFlow()

    private val _coverGenState = MutableStateFlow<UiState<Bitmap>>(UiState.Idle)
    val coverGenState: StateFlow<UiState<Bitmap>> = _coverGenState.asStateFlow()

    private val _exportState = MutableStateFlow<ExportUiState>(ExportUiState.Idle)
    val exportState: StateFlow<ExportUiState> = _exportState.asStateFlow()

    private val _actionFeedback = MutableStateFlow<String?>(null)
    val actionFeedback: StateFlow<String?> = _actionFeedback.asStateFlow()

    private var activeTransformer: Transformer? = null

    val userTemplates: StateFlow<UiState<List<EditTemplate>>> =
        firestoreRepository.observeTemplates(currentUserId)
            .map<List<EditTemplate>, UiState<List<EditTemplate>>> { UiState.Success(it) }
            .catch { error ->
                val fsErr = generateSequence(error) { it.cause }
                    .filterIsInstance<FirebaseFirestoreException>()
                    .firstOrNull()
                if (fsErr == null && error is CancellationException) throw error
                val root = fsErr ?: error
                Log.w(TAG, "Error observing templates", root)
                emit(UiState.Error(root.message ?: "Falha ao carregar seus modelos."))
            }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000L),
                initialValue = UiState.Loading
            )

    val savedProjects: StateFlow<UiState<List<EditProject>>> =
        firestoreRepository.observeProjects(currentUserId)
            .map<List<EditProject>, UiState<List<EditProject>>> { UiState.Success(it) }
            .catch { error ->
                val fsErr = generateSequence(error) { it.cause }
                    .filterIsInstance<FirebaseFirestoreException>()
                    .firstOrNull()
                if (fsErr == null && error is CancellationException) throw error
                val root = fsErr ?: error
                Log.w(TAG, "Error observing projects", root)
                emit(UiState.Error(root.message ?: "Falha ao carregar projetos salvos."))
            }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000L),
                initialValue = UiState.Loading
            )

    val supportTickets: StateFlow<UiState<List<SupportTicket>>> =
        firestoreRepository.observeTickets(currentUserId)
            .map<List<SupportTicket>, UiState<List<SupportTicket>>> { UiState.Success(it) }
            .catch { error ->
                val fsErr = generateSequence(error) { it.cause }
                    .filterIsInstance<FirebaseFirestoreException>()
                    .firstOrNull()
                if (fsErr == null && error is CancellationException) throw error
                val root = fsErr ?: error
                Log.w(TAG, "Error observing tickets", root)
                emit(UiState.Error(root.message ?: "Falha ao carregar tickets de suporte."))
            }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000L),
                initialValue = UiState.Loading
            )

    fun clearFeedback() {
        _actionFeedback.value = null
    }

    fun addSelectedMedia(uris: List<Uri>) {
        if (uris.isEmpty()) return
        viewModelScope.launch {
            val inspected = uris.map { mediaEngine.inspectMediaUri(it) }
            _projectState.update { current ->
                val merged = (current.mediaItems + inspected).distinctBy { it.uri }
                current.copy(mediaItems = merged)
            }
            _actionFeedback.value = "${inspected.size} mídia(s) adicionada(s) ao estúdio."
        }
    }

    fun removeMediaItem(uri: Uri) {
        _projectState.update { current ->
            current.copy(mediaItems = current.mediaItems.filterNot { it.uri == uri })
        }
    }

    fun updateProjectTitle(title: String) {
        _projectState.update { it.copy(projectTitle = title) }
    }

    fun updatePrompt(prompt: String) {
        _projectState.update { it.copy(prompt = prompt) }
    }

    fun selectSubject(preset: FootballSubjectPreset, customName: String = _projectState.value.customSubjectText) {
        _projectState.update {
            it.copy(
                subjectPreset = preset,
                customSubjectText = customName
            )
        }
    }

    fun updateCustomSubject(name: String) {
        _projectState.update { it.copy(customSubjectText = name) }
    }

    fun selectFootballStyle(style: FootballStyle) {
        _projectState.update {
            it.copy(
                selectedStyle = style,
                bpm = style.defaultBpm,
                musicTitle = style.suggestedMusic,
                contrast = style.contrast,
                saturation = style.saturation,
                brightness = style.brightness,
                speedMultiplier = style.speedMultiplier,
                glowIntensity = style.glowIntensity,
                shakeIntensity = style.shakeIntensity,
                zoomIntensity = style.zoomIntensity
            )
        }
    }

    fun updateEffects(
        contrast: Float? = null,
        saturation: Float? = null,
        brightness: Float? = null,
        glow: Float? = null,
        shake: Float? = null,
        zoom: Float? = null,
        speed: Float? = null,
        caption: String? = null
    ) {
        _projectState.update { state ->
            state.copy(
                contrast = contrast ?: state.contrast,
                saturation = saturation ?: state.saturation,
                brightness = brightness ?: state.brightness,
                glowIntensity = glow ?: state.glowIntensity,
                shakeIntensity = shake ?: state.shakeIntensity,
                zoomIntensity = zoom ?: state.zoomIntensity,
                speedMultiplier = speed ?: state.speedMultiplier,
                captionText = caption ?: state.captionText
            )
        }
    }

    fun selectMusicTrack(uri: Uri?, title: String, bpm: Int) {
        _projectState.update {
            it.copy(
                musicUri = uri,
                musicTitle = title,
                bpm = bpm.coerceIn(60, 200)
            )
        }
    }

    fun updateBpm(bpm: Int) {
        _projectState.update { it.copy(bpm = bpm.coerceIn(60, 200)) }
    }

    fun updateAspectRatio(option: AspectRatioOption) {
        _projectState.update { it.copy(aspectRatio = option) }
    }

    fun updateQualityPreset(preset: QualityPreset) {
        _projectState.update { it.copy(qualityPreset = preset) }
    }

    fun updateQualityToggles(
        lighting: Boolean? = null,
        noiseReduction: Boolean? = null,
        sharpness: Boolean? = null,
        colorAdjust: Boolean? = null,
        stabilization: Boolean? = null
    ) {
        _projectState.update {
            it.copy(
                lightingEnhance = lighting ?: it.lightingEnhance,
                noiseReduction = noiseReduction ?: it.noiseReduction,
                sharpnessBoost = sharpness ?: it.sharpnessBoost,
                colorAdjustment = colorAdjust ?: it.colorAdjustment,
                stabilizationEnabled = stabilization ?: it.stabilizationEnabled
            )
        }
    }

    fun updateWatermark(transform: (WatermarkConfig) -> WatermarkConfig) {
        _projectState.update { it.copy(watermarkConfig = transform(it.watermarkConfig)) }
    }

    fun applyDefaultChoszenWatermark() {
        _projectState.update {
            it.copy(
                watermarkConfig = it.watermarkConfig.copy(
                    enabled = true,
                    mode = WatermarkMode.TEXT,
                    text = "CHOSZEN™©",
                    shadowEnabled = true,
                    outlineEnabled = true,
                    position = WatermarkPosition.BOTTOM_RIGHT
                )
            )
        }
        _actionFeedback.value = "Marca-d'água padrão CHOSZEN™© aplicada."
    }

    fun generateAutoEditPlan() {
        val state = _projectState.value
        viewModelScope.launch {
            _aiPlanState.value = UiState.Loading
            val frames = state.mediaItems.firstOrNull()?.let { first ->
                mediaEngine.extractSampleFrames(first.uri, maxFrames = 4)
            }.orEmpty()
            val clipSummary = if (state.mediaItems.isEmpty()) {
                "Nenhum clipe anexado ainda; criar roteiro estruturado para o usuario."
            } else {
                state.mediaItems.joinToString("; ") { item ->
                    "${item.displayLabel} (${item.width}x${item.height}, ${item.durationMs / 1000}s)"
                }
            }
            val result = aiRepository.generateAutoEditPlan(
                prompt = state.prompt,
                subject = state.activeSubjectName,
                style = state.selectedStyle,
                musicTitle = state.musicTitle,
                bpm = state.bpm,
                aspectRatio = state.aspectRatio,
                sampleFrames = frames,
                clipSummary = clipSummary
            )
            frames.forEach { if (!it.isRecycled) it.recycle() }
            result.fold(
                onSuccess = { plan ->
                    _aiPlanState.value = UiState.Success(plan)
                    _projectState.update {
                        it.copy(
                            projectTitle = plan.title.take(100),
                            aiEditPlan = plan,
                            contrast = plan.recommendedContrast,
                            saturation = plan.recommendedSaturation,
                            brightness = plan.recommendedBrightness,
                            speedMultiplier = plan.recommendedSpeed,
                            captionText = plan.captions.firstOrNull().orEmpty()
                        )
                    }
                    _actionFeedback.value = "Plano de edição da IA aplicado ao estúdio!"
                },
                onFailure = { err ->
                    _aiPlanState.value = UiState.Error(err.message ?: "Erro ao gerar edit com IA.")
                }
            )
        }
    }

    fun analyzeReferenceStyle(referenceUri: Uri, note: String) {
        val authorizedWatermark = _projectState.value.watermarkConfig.text.ifBlank { "CHOSZEN™©" }
        viewModelScope.launch {
            _styleRecreationState.value = UiState.Loading
            val frames = mediaEngine.extractSampleFrames(referenceUri, maxFrames = 4)
            if (frames.isEmpty()) {
                _styleRecreationState.value = UiState.Error("Não foi possível ler frames da mídia de referência.")
                return@launch
            }
            val result = aiRepository.analyzeReferenceStyle(
                referenceFrames = frames,
                userNote = note,
                authorizedWatermark = authorizedWatermark
            )
            frames.forEach { if (!it.isRecycled) it.recycle() }
            result.fold(
                onSuccess = { profile ->
                    _styleRecreationState.value = UiState.Success(profile)
                    _projectState.update { it.copy(styleRecreation = profile) }
                },
                onFailure = { err ->
                    _styleRecreationState.value = UiState.Error(err.message ?: "Falha na análise de estilo.")
                }
            )
        }
    }

    fun applyRecreatedStyleToProject(profile: StyleRecreationProfile) {
        val ratio = AspectRatioOption.entries.firstOrNull { it.code == profile.recommendedAspectRatio }
            ?: AspectRatioOption.VERTICAL_9_16
        _projectState.update {
            it.copy(
                contrast = profile.contrast,
                saturation = profile.saturation,
                brightness = profile.brightness,
                speedMultiplier = profile.speedMultiplier,
                bpm = profile.recommendedBpm,
                aspectRatio = ratio,
                styleRecreation = profile
            )
        }
        _actionFeedback.value = "Estilo '${profile.styleName}' aplicado (apenas sua marca autorizada)."
    }

    fun generateBeatCues() {
        val state = _projectState.value
        val durationSec = ((state.mediaItems.sumOf { it.durationMs } / 1000L).toInt()).coerceIn(10, 60)
        viewModelScope.launch {
            _beatSyncState.value = UiState.Loading
            val result = aiRepository.generateBeatSyncCues(
                musicTitle = state.musicTitle,
                bpm = state.bpm,
                durationSec = durationSec,
                style = state.selectedStyle
            )
            result.fold(
                onSuccess = { cues ->
                    _beatSyncState.value = UiState.Success(cues)
                    _projectState.update { it.copy(beatSyncCues = cues) }
                },
                onFailure = { err ->
                    _beatSyncState.value = UiState.Error(err.message ?: "Falha ao sincronizar batidas.")
                }
            )
        }
    }

    fun generateCover(customPrompt: String, aspectRatio: AspectRatioOption) {
        val state = _projectState.value
        viewModelScope.launch {
            _coverGenState.value = UiState.Loading
            val result = aiRepository.generateCoverImage(
                subject = state.activeSubjectName,
                style = state.selectedStyle,
                customPrompt = customPrompt,
                aspectRatio = aspectRatio
            )
            result.fold(
                onSuccess = { bmp ->
                    _coverGenState.value = UiState.Success(bmp)
                },
                onFailure = { err ->
                    _coverGenState.value = UiState.Error(err.message ?: "Erro ao gerar capa.")
                }
            )
        }
    }

    fun saveGeneratedCoverToGallery(bitmap: Bitmap) {
        viewModelScope.launch {
            val result = mediaEngine.saveBitmapToGallery(bitmap, "choszen_capa")
            result.fold(
                onSuccess = {
                    _actionFeedback.value = "Capa salva na Galeria (Pictures/ChoszenEditAI)."
                },
                onFailure = { err ->
                    _actionFeedback.value = err.message ?: "Erro ao salvar capa."
                }
            )
        }
    }

    @OptIn(UnstableApi::class)
    fun buildPreviewEffectsForPlayer(): List<Effect> {
        val state = _projectState.value
        val (dims, _) = mediaEngine.computeTargetDimensions(
            aspectRatio = state.aspectRatio,
            requestedQuality = QualityPreset.HD,
            sourceInfo = state.mediaItems.firstOrNull(),
            capabilities = deviceCapabilities
        )
        return mediaEngine.buildPreviewVideoEffects(state, dims.first, dims.second)
    }

    fun exportCurrentProject() {
        val state = _projectState.value
        activeTransformer?.cancel()
        _exportState.value = ExportUiState.Exporting(0, "Iniciando renderização...")
        activeTransformer = mediaEngine.startExport(
            state = state,
            capabilities = deviceCapabilities,
            onProgress = { pct, label ->
                _exportState.value = ExportUiState.Exporting(pct, label)
            },
            onCompleted = { file, uri, sizeBytes, resolutionLabel, warning ->
                _exportState.value = ExportUiState.Completed(
                    outputFilePath = file.absolutePath,
                    outputUri = uri,
                    fileSizeBytes = sizeBytes,
                    resolutionLabel = resolutionLabel,
                    aspectRatioLabel = state.aspectRatio.label,
                    qualityWarning = warning
                )
            },
            onError = { msg ->
                _exportState.value = ExportUiState.Failed(msg)
            }
        )
    }

    fun cancelExport() {
        activeTransformer?.cancel()
        activeTransformer = null
        _exportState.value = ExportUiState.Idle
    }

    fun resetExportState() {
        _exportState.value = ExportUiState.Idle
    }

    fun useCuratedTemplate(template: CuratedTemplate) {
        _projectState.update {
            it.copy(
                projectTitle = template.name,
                selectedStyle = template.style,
                musicTitle = template.suggestedMusic,
                bpm = template.style.defaultBpm,
                aspectRatio = template.aspectRatio,
                contrast = template.style.contrast,
                saturation = template.style.saturation,
                brightness = template.style.brightness,
                speedMultiplier = template.style.speedMultiplier,
                glowIntensity = template.style.glowIntensity,
                shakeIntensity = template.style.shakeIntensity,
                zoomIntensity = template.style.zoomIntensity
            )
        }
        _actionFeedback.value = "Modelo '${template.name}' aplicado! Abra o Estúdio para pré-visualizar."
    }

    fun useSavedTemplate(template: EditTemplate) {
        val matchedStyle = FootballStyle.entries.firstOrNull { it.displayName == template.style }
            ?: FootballStyle.PHONK_EDIT
        val matchedRatio = AspectRatioOption.entries.firstOrNull { it.code == template.aspectRatio }
            ?: AspectRatioOption.VERTICAL_9_16
        _projectState.update {
            it.copy(
                projectTitle = template.name,
                selectedStyle = matchedStyle,
                musicTitle = template.suggestedMusic,
                aspectRatio = matchedRatio,
                contrast = template.contrast.toFloat(),
                saturation = template.saturation.toFloat(),
                brightness = template.brightness.toFloat(),
                speedMultiplier = template.speedMultiplier.toFloat(),
                watermarkConfig = it.watermarkConfig.copy(
                    text = template.watermarkText.ifBlank { "CHOSZEN™©" }
                )
            )
        }
        _actionFeedback.value = "Seu modelo '${template.name}' foi carregado!"
    }

    fun saveCurrentAsTemplate(
        name: String,
        category: String,
        durationSec: Int
    ) {
        val state = _projectState.value
        viewModelScope.launch {
            val tpl = EditTemplate(
                userId = currentUserId,
                name = name.trim(),
                style = state.selectedStyle.displayName,
                category = category.trim().ifBlank { "Futebol" },
                durationSec = durationSec.toLong().coerceIn(1L, 600L),
                suggestedMusic = state.musicTitle,
                transitions = state.selectedStyle.transitions,
                effects = state.selectedStyle.effects,
                aspectRatio = state.aspectRatio.code,
                contrast = state.contrast.toDouble(),
                saturation = state.saturation.toDouble(),
                brightness = state.brightness.toDouble(),
                speedMultiplier = state.speedMultiplier.toDouble(),
                watermarkText = state.watermarkConfig.text.ifBlank { "CHOSZEN™©" }
            )
            val result = firestoreRepository.saveTemplate(tpl)
            result.fold(
                onSuccess = {
                    _actionFeedback.value = "Modelo salvo na nuvem com sucesso!"
                },
                onFailure = { err ->
                    _actionFeedback.value = err.message ?: "Erro ao salvar modelo."
                }
            )
        }
    }

    fun deleteTemplate(templateId: String) {
        viewModelScope.launch {
            val result = firestoreRepository.deleteTemplate(templateId)
            result.onFailure { err ->
                _actionFeedback.value = err.message ?: "Erro ao excluir modelo."
            }
        }
    }

    fun saveProjectSnapshotToCloud() {
        val state = _projectState.value
        viewModelScope.launch {
            val project = EditProject(
                userId = currentUserId,
                title = state.projectTitle.ifBlank { "Edit ${state.activeSubjectName}" },
                subject = state.activeSubjectName,
                style = state.selectedStyle.displayName,
                prompt = state.prompt,
                aspectRatio = state.aspectRatio.code,
                qualityPreset = state.qualityPreset.label,
                aiSummary = state.aiEditPlan?.summary
                    ?: "Estilo ${state.selectedStyle.displayName} • ${state.musicTitle}",
                watermarkText = state.watermarkConfig.text.ifBlank { "CHOSZEN™©" }
            )
            val result = firestoreRepository.saveProject(project)
            result.fold(
                onSuccess = {
                    _actionFeedback.value = "Projeto salvo na sua conta!"
                },
                onFailure = { err ->
                    _actionFeedback.value = err.message ?: "Erro ao salvar projeto."
                }
            )
        }
    }

    fun createSupportTicket(
        subject: String,
        category: String,
        priority: String,
        description: String
    ) {
        viewModelScope.launch {
            val result = firestoreRepository.createTicket(subject, category, priority, description)
            result.fold(
                onSuccess = {
                    _actionFeedback.value = "Ticket de suporte enviado!"
                },
                onFailure = { err ->
                    _actionFeedback.value = err.message ?: "Erro ao abrir ticket."
                }
            )
        }
    }

    fun resolveTicket(ticketId: String) {
        viewModelScope.launch {
            val result = firestoreRepository.markTicketResolved(ticketId)
            result.fold(
                onSuccess = {
                    _actionFeedback.value = "Ticket marcado como resolvido."
                },
                onFailure = { err ->
                    _actionFeedback.value = err.message ?: "Erro ao atualizar ticket."
                }
            )
        }
    }

    override fun onCleared() {
        super.onCleared()
        activeTransformer?.cancel()
    }

    companion object {
        fun provideFactory(
            context: Context,
            currentUserId: String
        ): ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val appContext = context.applicationContext
                StudioViewModel(
                    firestoreRepository = StudioFirestoreRepository(appContext),
                    aiRepository = ChoszenAiRepository(),
                    mediaEngine = MediaProcessingEngine(appContext),
                    currentUserId = currentUserId
                )
            }
        }
    }
}
