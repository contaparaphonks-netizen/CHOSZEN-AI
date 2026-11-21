package com.example.data

import com.example.data.model.AspectRatioOption
import com.example.data.model.CuratedTemplate
import com.example.data.model.FootballStyle
import com.example.data.model.TutorialGuide

object StudioCatalog {

    val suggestedMusicTracks: List<Pair<String, Int>> = listOf(
        "Brazilian Phonk — Automotivo 142 BPM" to 142,
        "Aura Synth — Slowed Reverb 118 BPM" to 118,
        "Velocity Funk Rave — Drop Rápido 132 BPM" to 132,
        "UCL Stadium Anthem — Epic Strings 105 BPM" to 105,
        "Dark Memphis Drill — Sub Bass 128 BPM" to 128,
        "Hardstyle Impact — Kick Heavy 148 BPM" to 148
    )

    val curatedTemplates: List<CuratedTemplate> = listOf(
        CuratedTemplate(
            id = "tpl_neymar_phonk",
            name = "Neymar Magia • Phonk Viral",
            style = FootballStyle.PHONK_EDIT,
            category = "Futebol",
            durationSec = 16,
            suggestedMusic = "Brazilian Phonk — Automotivo 142 BPM",
            transitions = listOf("Flash Cut", "Whip Zoom", "Glitch Cut"),
            effects = listOf("Beat Shake", "Contraste Alto", "Bass Zoom"),
            aspectRatio = AspectRatioOption.VERTICAL_9_16
        ),
        CuratedTemplate(
            id = "tpl_cr7_aura",
            name = "CR7 Aura • Slowed Cinematic",
            style = FootballStyle.AURA_EDIT,
            category = "Futebol",
            durationSec = 22,
            suggestedMusic = "Aura Synth — Slowed Reverb 118 BPM",
            transitions = listOf("Dissolve Glow", "Smooth Zoom"),
            effects = listOf("Aura Halo", "Tons Frios Ciano", "Slow Motion 0.85x"),
            aspectRatio = AspectRatioOption.VERTICAL_9_16
        ),
        CuratedTemplate(
            id = "tpl_vini_velocity",
            name = "Vini Jr. • Velocity 132 BPM",
            style = FootballStyle.VELOCITY_EDIT,
            category = "Futebol",
            durationSec = 15,
            suggestedMusic = "Velocity Funk Rave — Drop Rápido 132 BPM",
            transitions = listOf("Speed Ramp", "Optical Pull-In"),
            effects = listOf("Curva 0.4x -> 2.0x", "Motion Blur", "Beat Flash"),
            aspectRatio = AspectRatioOption.VERTICAL_9_16
        ),
        CuratedTemplate(
            id = "tpl_ucl_cinematic",
            name = "Noite de Champions • 16:9",
            style = FootballStyle.FOOTBALL_CINEMATIC,
            category = "Clubes e Seleções",
            durationSec = 30,
            suggestedMusic = "UCL Stadium Anthem — Epic Strings 105 BPM",
            transitions = listOf("Luma Fade", "Cinematic Cut"),
            effects = listOf("Teal & Gold Grade", "Estabilização", "Nitidez 4K"),
            aspectRatio = AspectRatioOption.HORIZONTAL_16_9
        ),
        CuratedTemplate(
            id = "tpl_mbappe_shake",
            name = "Mbappé Turbo • Impact Shake",
            style = FootballStyle.SHAKE_EDIT,
            category = "Futebol",
            durationSec = 14,
            suggestedMusic = "Hardstyle Impact — Kick Heavy 148 BPM",
            transitions = listOf("Impact Shake", "RGB Split Cut"),
            effects = listOf("XY Shake", "Snap Zoom", "Chromatic Impact"),
            aspectRatio = AspectRatioOption.VERTICAL_9_16
        ),
        CuratedTemplate(
            id = "tpl_glow_square",
            name = "Glow Chuteira • Feed 1:1",
            style = FootballStyle.GLOW_EDIT,
            category = "Geral & Futebol",
            durationSec = 18,
            suggestedMusic = "Electro Phonk 136 BPM",
            transitions = listOf("Light Leak", "Glow Flash"),
            effects = listOf("Neon Bloom", "Saturação +32%", "Brilho Dinâmico"),
            aspectRatio = AspectRatioOption.SQUARE_1_1
        )
    )

    val tutorials: List<TutorialGuide> = listOf(
        TutorialGuide(
            id = "tut_1",
            title = "1. Criar Edit de Futebol Automático com IA",
            durationTag = "2 min",
            steps = listOf(
                "Na aba EDIT AI, toque em 'Criar Edit' ou 'Edit de Futebol'.",
                "Selecione seus vídeos ou imagens pelo seletor seguro do Android.",
                "Escolha o jogador (Neymar, Messi, CR7, Vini Jr., Endrick, Raphinha ou qualquer time/jogador) e o estilo (Phonk, Velocity, Aura, etc.).",
                "Descreva o ritmo desejado e toque em 'Gerar Plano com IA' para receber cortes no beat, efeitos e legendas.",
                "Vá para a aba Estúdio, pré-visualize o resultado e exporte em MP4 (9:16, 1:1 ou 16:9)."
            )
        ),
        TutorialGuide(
            id = "tut_2",
            title = "2. Usar RECRIAR ESTILO sem copiar marcas",
            durationTag = "2 min",
            steps = listOf(
                "Abra 'Aplicar Efeitos' no menu EDIT AI e entre na seção RECRIAR ESTILO.",
                "Envie um vídeo ou imagem de referência de edição.",
                "A IA analisa transições, velocidade, zoom, paleta de cores, iluminação e posicionamento.",
                "Por segurança e direitos autorais, marcas-d'água, logos e créditos de terceiros nunca são copiados.",
                "Toque em 'Aplicar Estilo ao Meu Vídeo' para transferir a estética usando apenas sua marca autorizada (ex: CHOSZEN™©)."
            )
        ),
        TutorialGuide(
            id = "tut_3",
            title = "3. Configurar Marca-d'água CHOSZEN™©",
            durationTag = "1 min",
            steps = listOf(
                "Toque em 'Marca-d'água' no menu EDIT AI.",
                "Use o botão rápido 'CHOSZEN™©', digite texto personalizado ou envie sua própria imagem/logo.",
                "Ajuste tamanho, opacidade, posição na grade 3x3, duração, sombra e contorno.",
                "A marca é renderizada em alta definição na prévia e no MP4 final exportado."
            )
        ),
        TutorialGuide(
            id = "tut_4",
            title = "4. Qualidade Real (HD, Full HD, 2K, 4K) e Exportação",
            durationTag = "2 min",
            steps = listOf(
                "Abra 'Melhorar Qualidade' para inspecionar os limites reais do hardware do seu aparelho.",
                "O app nunca inventa resolução ou qualidade inexistente: 2K e 4K aparecem liberados somente quando o encoder do dispositivo suporta.",
                "Ative Melhoria de Iluminação, Nitidez, Redução de Ruído, Ajuste de Cores e Estabilização.",
                "Na aba Estúdio, escolha 9:16 (TikTok/Reels/Shorts), 1:1 ou 16:9 e clique em 'Exportar MP4'."
            )
        ),
        TutorialGuide(
            id = "tut_5",
            title = "5. Salvar e Reutilizar Modelos Prontos",
            durationTag = "1 min",
            steps = listOf(
                "Explore a aba Modelos e toque em 'USAR MODELO' em qualquer template pronto.",
                "Personalize efeitos, BPM e marca-d'água no Estúdio.",
                "Toque em 'Criar Modelo' na aba Modelos para salvar sua própria receita na nuvem com segurança."
            )
        )
    )
}
