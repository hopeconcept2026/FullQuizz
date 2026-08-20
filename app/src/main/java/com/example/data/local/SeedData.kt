package com.example.data.local

import com.example.data.local.entity.AchievementEntity
import com.example.data.local.entity.CategoryEntity
import com.example.data.local.entity.QuestionEntity

object SeedData {

    val categories = listOf(
        CategoryEntity(
            id = "culture_generale",
            name = "Culture Générale",
            slug = "culture-generale",
            description = "Savoirs universels, inventions, découvertes, records du monde et société",
            iconName = "lightbulb",
            colorHex = "#3949AB", // Indigo
            isFeatured = true,
            displayOrder = 1
        ),
        CategoryEntity(
            id = "histoire",
            name = "Histoire & Civilisations",
            slug = "histoire",
            description = "Antiquité, grands empires, révolutions, traités, guerres et décolonisation",
            iconName = "account_balance",
            colorHex = "#D84315", // Deep Orange
            isFeatured = true,
            displayOrder = 2
        ),
        CategoryEntity(
            id = "geographie",
            name = "Géographie & Monde",
            slug = "geographie",
            description = "Capitales, fleuves, détroits, reliefs, îles et géopolitique mondiale",
            iconName = "explore",
            colorHex = "#2E7D32", // Forest Green
            isFeatured = true,
            displayOrder = 3
        ),
        CategoryEntity(
            id = "science",
            name = "Sciences, Nature & Espace",
            slug = "science",
            description = "Physique, astronomie, biologie cellulaire, chimie, médecine et écologie",
            iconName = "science",
            colorHex = "#00897B", // Teal
            isFeatured = true,
            displayOrder = 4
        ),
        CategoryEntity(
            id = "litterature",
            name = "Littérature & Philosophie",
            slug = "litterature",
            description = "Grands penseurs, classiques mondiaux, poésie, figures de style et étymologie",
            iconName = "auto_stories",
            colorHex = "#6D4C41", // Brown / Bronze
            isFeatured = true,
            displayOrder = 5
        ),
        CategoryEntity(
            id = "afrique",
            name = "Afrique & Panafricanisme",
            slug = "afrique",
            description = "Histoire des grands empires, héros panafricains, traditions et géopolitique",
            iconName = "public",
            colorHex = "#FF8F00", // Amber / Gold
            isFeatured = true,
            displayOrder = 6
        ),
        CategoryEntity(
            id = "rdc",
            name = "RDC & Patrimoine",
            slug = "rdc",
            description = "26 provinces, fleuve Congo, minerais, histoire, personnalités et biodiversité",
            iconName = "flag",
            colorHex = "#0288D1", // Light Blue
            isFeatured = false,
            displayOrder = 7
        ),
        CategoryEntity(
            id = "musique",
            name = "Arts, Musique & Cinéma",
            slug = "musique",
            description = "Rumba congolaise, peinture des maîtres, cinéma mondial, opéra et musique",
            iconName = "palette",
            colorHex = "#F4511E", // Coral
            isFeatured = false,
            displayOrder = 8
        ),
        CategoryEntity(
            id = "bible",
            name = "Bible & Textes Sacrés",
            slug = "bible",
            description = "Ancien & Nouveau Testament, prophètes, théologie, paraboles et histoire biblique",
            iconName = "menu_book",
            colorHex = "#8E24AA", // Purple
            isFeatured = false,
            displayOrder = 9
        ),
        CategoryEntity(
            id = "technologie",
            name = "Technologies & IA",
            slug = "technologie",
            description = "Informatique, algorithmes, pionniers du web, télécoms, IA et robotique",
            iconName = "memory",
            colorHex = "#5E35B1", // Deep Purple
            isFeatured = false,
            displayOrder = 10
        ),
        CategoryEntity(
            id = "sport",
            name = "Sports & Jeux Olympiques",
            slug = "sport",
            description = "Football mondial, CAN, JO, athlétisme, basketball, tennis et records",
            iconName = "sports_soccer",
            colorHex = "#E53935", // Red
            isFeatured = false,
            displayOrder = 11
        ),
        CategoryEntity(
            id = "logique",
            name = "Logique, Maths & Énigmes",
            slug = "logique",
            description = "Mathématiques, paradoxes, probabilités, suites logiques et déduction",
            iconName = "psychology",
            colorHex = "#C2185B", // Rose
            isFeatured = false,
            displayOrder = 12
        )
    )

    val achievements = listOf(
        AchievementEntity("first_quiz", "Première Victoire", "Compléter votre toute première partie", "GENERAL", "emoji_events", 1, 0, 50, 20),
        AchievementEntity("quiz_5", "Apprenti Curieux", "Jouer 5 parties de quiz", "GENERAL", "school", 5, 0, 100, 30),
        AchievementEntity("quiz_25", "Vétéran du Savoir", "Compléter 25 parties de quiz", "GENERAL", "workspace_premium", 25, 0, 250, 75),
        AchievementEntity("quiz_100", "Légende Vivante", "Compléter 100 parties de quiz", "GENERAL", "military_tech", 100, 0, 1000, 300),

        AchievementEntity("correct_10", "Démarrage Éclair", "Répondre correctement à 10 questions", "GENERAL", "check_circle", 10, 0, 50, 15),
        AchievementEntity("correct_50", "Cerveau Agile", "Répondre correctement à 50 questions", "GENERAL", "psychology", 50, 0, 150, 40),
        AchievementEntity("correct_200", "Encyclopédie Humaine", "Répondre correctement à 200 questions", "GENERAL", "auto_stories", 200, 0, 500, 150),
        AchievementEntity("correct_500", "Grand Érudit", "Répondre correctement à 500 questions", "GENERAL", "diamond", 500, 0, 1200, 400),

        AchievementEntity("combo_3", "En Forme", "Réaliser une série de 3 bonnes réponses (x2)", "STREAK", "bolt", 3, 0, 40, 15),
        AchievementEntity("combo_5", "En Feu !", "Réaliser une série de 5 bonnes réponses (x3)", "STREAK", "local_fire_department", 5, 0, 80, 25),
        AchievementEntity("combo_10", "Parfait Invincible", "Réaliser un sans-faute de 10 bonnes réponses (x5)", "STREAK", "whatshot", 10, 0, 200, 60),

        AchievementEntity("streak_3", "Habitude Gagnante", "Maintenir un streak de 3 jours consécutifs", "STREAK", "date_range", 3, 0, 100, 30),
        AchievementEntity("streak_7", "Fidélité de Fer", "Maintenir un streak de 7 jours consécutifs", "STREAK", "calendar_month", 7, 0, 250, 80),
        AchievementEntity("streak_14", "Dévouement Total", "Maintenir un streak de 14 jours consécutifs", "STREAK", "verified", 14, 0, 500, 150),
        AchievementEntity("streak_30", "Maître de la Discipline", "Maintenir un streak de 30 jours consécutifs", "STREAK", "star", 30, 0, 1200, 350),

        AchievementEntity("daily_1", "Défi Relevé", "Compléter votre premier défi quotidien", "GENERAL", "today", 1, 0, 60, 20),
        AchievementEntity("daily_5", "Chasseur de Défis", "Compléter 5 défis quotidiens", "GENERAL", "fact_check", 5, 0, 200, 60),
        AchievementEntity("daily_20", "Champion Quotidien", "Compléter 20 défis quotidiens", "GENERAL", "military_tech", 20, 0, 600, 180),

        AchievementEntity("expert_culture", "Savoir Universel", "Répondre à 30 questions de Culture Générale", "MASTERY", "lightbulb", 30, 0, 200, 50),
        AchievementEntity("expert_bible", "Expert Biblique", "Répondre à 30 questions de la catégorie Bible", "BIBLE", "menu_book", 30, 0, 200, 50),
        AchievementEntity("maitre_bible", "Maître des Écritures", "Répondre à 100 questions de la catégorie Bible", "BIBLE", "auto_awesome", 100, 0, 500, 150),

        AchievementEntity("expert_afrique", "Fils de l'Afrique", "Répondre à 30 questions de la catégorie Afrique", "AFRIQUE", "public", 30, 0, 200, 50),
        AchievementEntity("maitre_afrique", "Panafricaniste", "Répondre à 100 questions de la catégorie Afrique", "AFRIQUE", "stars", 100, 0, 500, 150),

        AchievementEntity("expert_rdc", "Fierté Congolaise", "Répondre à 30 questions de la catégorie RDC", "RDC", "flag", 30, 0, 200, 50),
        AchievementEntity("maitre_rdc", "Léopard Invincible", "Répondre à 100 questions de la catégorie RDC", "RDC", "shield", 100, 0, 500, 150),

        AchievementEntity("expert_science", "Savant Fou", "Répondre à 30 questions de Sciences", "MASTERY", "science", 30, 0, 200, 50),
        AchievementEntity("expert_histoire", "Historien Émérite", "Répondre à 30 questions d'Histoire", "MASTERY", "account_balance", 30, 0, 200, 50),
        AchievementEntity("expert_sport", "Athlète d'Or", "Répondre à 30 questions de Sports", "MASTERY", "sports_soccer", 30, 0, 200, 50),

        AchievementEntity("level_5", "Curieux Né", "Atteindre le niveau 5", "GENERAL", "trending_up", 5, 0, 150, 40),
        AchievementEntity("level_10", "Connaisseur Revanche", "Atteindre le niveau 10", "GENERAL", "military_tech", 10, 0, 350, 100),
        AchievementEntity("level_20", "Expert Couronné", "Atteindre le niveau 20", "GENERAL", "workspace_premium", 20, 0, 800, 250),
        AchievementEntity("level_50", "Maître du Panthéon", "Atteindre le niveau 50", "GENERAL", "diamond", 50, 0, 2000, 600)
    )

    val questions = listOf(
        // ==========================================
        // === 1. CULTURE GÉNÉRALE & SAVOIRS UNIVERS ===
        // ==========================================
        QuestionEntity(
            categoryId = "culture_generale",
            subcategory = "Inventions & Découvertes",
            question = "Qui a découvert le premier antibiotique naturel, la pénicilline, en 1928 ?",
            optionA = "Louis Pasteur",
            optionB = "Alexander Fleming",
            optionC = "Robert Koch",
            optionD = "Edward Jenner",
            correctAnswer = "B",
            explanation = "Alexander Fleming a découvert par sérendipité les propriétés antibactériennes du champignon Penicillium notatum.",
            difficulty = "easy",
            reference = "Histoire de la médecine moderne"
        ),
        QuestionEntity(
            categoryId = "culture_generale",
            subcategory = "Prix Nobel & Institutions",
            question = "Quelle scientifique est la seule personne dans l'histoire à avoir reçu deux prix Nobel dans deux disciplines scientifiques distinctes (Physique et Chimie) ?",
            optionA = "Marie Curie",
            optionB = "Rosalind Franklin",
            optionC = "Lise Meitner",
            optionD = "Ada Lovelace",
            correctAnswer = "A",
            explanation = "Marie Curie a obtenu le prix Nobel de physique en 1903 (avec Pierre Curie et Henri Becquerel) et celui de chimie en 1911 pour ses travaux sur le radium et le polonium.",
            difficulty = "easy",
            reference = "Fondation Nobel"
        ),
        QuestionEntity(
            categoryId = "culture_generale",
            subcategory = "Société & Droit",
            question = "En quelle année et dans quelle ville la Déclaration Universelle des Droits de l'Homme a-t-elle été adoptée par l'ONU ?",
            optionA = "1945 à San Francisco",
            optionB = "1948 à Paris (Palais de Chaillot)",
            optionC = "1950 à Genève",
            optionD = "1944 à Bretton Woods",
            correctAnswer = "B",
            explanation = "La DUDH a été adoptée le 10 décembre 1948 au Palais de Chaillot à Paris par l'Assemblée générale des Nations unies.",
            difficulty = "medium",
            reference = "Résolution 217 A (III) de l'ONU"
        ),
        QuestionEntity(
            categoryId = "culture_generale",
            subcategory = "Records & Monde",
            question = "Quel est le plus grand État souverain sans littoral (enclavé) au monde par sa superficie ?",
            optionA = "La Mongolie",
            optionB = "Le Tchad",
            optionC = "Le Kazakhstan",
            optionD = "La Bolivie",
            correctAnswer = "C",
            explanation = "Le Kazakhstan s'étend sur plus de 2,7 millions de km², ce qui en fait le plus vaste pays enclavé de la planète.",
            difficulty = "medium",
            reference = "Géographie politique internationale"
        ),
        QuestionEntity(
            categoryId = "culture_generale",
            subcategory = "Monuments & Merveilles",
            question = "Laquelle des sept merveilles du monde antique est la seule qui subsiste encore de nos jours ?",
            optionA = "Le Phare d'Alexandrie",
            optionB = "Le Colosse de Rhodes",
            optionC = "La Grande Pyramide de Khéops (Gizeh)",
            optionD = "Les Jardins suspendus de Babylone",
            correctAnswer = "C",
            explanation = "Érigée vers 2560 av. J.-C., la pyramide de Khéops en Égypte est l'unique merveille antique encore debout.",
            difficulty = "easy",
            reference = "Archéologie antique"
        ),
        QuestionEntity(
            categoryId = "culture_generale",
            subcategory = "Symboles & Savoirs",
            question = "Quel gaz compose à environ 78 % l'atmosphère terrestre que nous respirons ?",
            optionA = "L'oxygène (O2)",
            optionB = "Le dioxyde de carbone (CO2)",
            optionC = "Le diazote (N2)",
            optionD = "L'argon (Ar)",
            correctAnswer = "C",
            explanation = "L'air sec est constitué d'environ 78 % de diazote (azote), 21 % d'oxygène et 1 % d'autres gaz (argon, CO2).",
            difficulty = "easy",
            reference = "Sciences de l'atmosphère"
        ),
        QuestionEntity(
            categoryId = "culture_generale",
            subcategory = "Citations Célèbres",
            question = "À quel savant de la Renaissance attribue-t-on la célèbre phrase murmurée « Et pourtant, elle tourne ! » (Eppur si muove) ?",
            optionA = "Nicolas Copernic",
            optionB = "Galilée (Galileo Galilei)",
            optionC = "Johannes Kepler",
            optionD = "Giordano Bruno",
            correctAnswer = "B",
            explanation = "Galilée l'aurait prononcée après son procès en 1633 où il dut abjurer sa défense de l'héliocentrisme copernicien.",
            difficulty = "medium",
            reference = "Histoire des sciences"
        ),

        // ==========================================
        // === 2. HISTOIRE & GRANDES CIVILISATIONS ==
        // ==========================================
        QuestionEntity(
            categoryId = "histoire",
            subcategory = "Antiquité",
            question = "Quel roi babylonien du XVIIIe siècle av. J.-C. est célèbre pour avoir fait graver l'un des plus anciens codes juridiques complets de l'Histoire ?",
            optionA = "Nabuchodonosor II",
            optionB = "Hammourabi",
            optionC = "Sargon d'Akkad",
            optionD = "Assurbanipal",
            correctAnswer = "B",
            explanation = "Le Code d'Hammourabi, stèle de basalte conservée au musée du Louvre, comprend 282 articles de lois mésopotamiennes régies par la loi du talion.",
            difficulty = "medium",
            reference = "Stèle du Code d'Hammourabi (Louvre)"
        ),
        QuestionEntity(
            categoryId = "histoire",
            subcategory = "Histoire Contemporaine",
            question = "En quelle année le Mur de Berlin, symbole de la guerre froide et du 'Rideau de fer', est-il tombé ?",
            optionA = "1985",
            optionB = "1989",
            optionC = "1991",
            optionD = "1993",
            correctAnswer = "B",
            explanation = "Le mur est tombé dans la nuit du 9 au 10 novembre 1989, ouvrant la voie à la réunification allemande en 1990.",
            difficulty = "easy",
            reference = "Histoire de l'Europe contemporaine"
        ),
        QuestionEntity(
            categoryId = "histoire",
            subcategory = "Révolution & Empires",
            question = "Quelle bataille majeure de 1815 a marqué la défaite définitive de Napoléon Ier face aux armées coalisées de Wellington et Blücher ?",
            optionA = "Austerlitz",
            optionB = "Waterloo",
            optionC = "Iéna",
            optionD = "Trafalgar",
            correctAnswer = "B",
            explanation = "La bataille de Waterloo s'est déroulée le 18 juin 1815 dans l'actuelle Belgique.",
            difficulty = "easy",
            reference = "Guerres napoléoniennes"
        ),
        QuestionEntity(
            categoryId = "histoire",
            subcategory = "Grandes Découvertes",
            question = "Quel navigateur portugais a été le premier Européen à atteindre l'Inde par voie maritime en contournant le Cap de Bonne-Espérance en 1498 ?",
            optionA = "Christophe Colomb",
            optionB = "Fernand de Magellan",
            optionC = "Vasco de Gama",
            optionD = "Bartolomeu Dias",
            correctAnswer = "C",
            explanation = "Vasco de Gama a accosté à Calicut (Kozhikode) en Inde le 20 mai 1498, ouvrant la route des épices.",
            difficulty = "medium",
            reference = "Histoire des Grandes Découvertes"
        ),
        QuestionEntity(
            categoryId = "histoire",
            subcategory = "Antiquité Romaine",
            question = "Qui est devenu le premier empereur de Rome en 27 av. J.-C. sous le titre de 'Princeps' et 'Auguste' ?",
            optionA = "Jules César",
            optionB = "Octave (Auguste)",
            optionC = "Néron",
            optionD = "Marc Antoine",
            correctAnswer = "B",
            explanation = "Octave, petit-neveu et héritier de Jules César, fonda l'Empire romain et reçut le titre honorifique d'Auguste.",
            difficulty = "medium",
            reference = "Res Gestae Divi Augusti"
        ),
        QuestionEntity(
            categoryId = "histoire",
            subcategory = "Décolonisation & Tiers-Monde",
            question = "Quelle conférence historique d'avril 1955 en Indonésie a réuni 29 pays afro-asiatiques et a marqué la naissance du Mouvement des non-alignés ?",
            optionA = "Conférence de Casablanca",
            optionB = "Conférence de Bandung",
            optionC = "Conférence d'Accra",
            optionD = "Conférence du Caire",
            correctAnswer = "B",
            explanation = "La conférence de Bandung a condamné le colonialisme et affirmé la voix des nations émergentes du tiers-monde.",
            difficulty = "medium",
            reference = "Histoire de la décolonisation"
        ),

        // ==========================================
        // === 3. GÉOGRAPHIE & MONDE ================
        // ==========================================
        QuestionEntity(
            categoryId = "geographie",
            subcategory = "Hydrographie & Fleuves",
            question = "Quel est le fleuve le plus puissant au monde par son débit d'eau moyen (plus de 200 000 m³/s) ?",
            optionA = "Le Nil",
            optionB = "L'Amazone",
            optionC = "Le Fleuve Congo",
            optionD = "Le Yangtsé (Fleuve Bleu)",
            correctAnswer = "B",
            explanation = "L'Amazone en Amérique du Sud possède le plus grand débit mondial, suivi en deuxième position par le fleuve Congo (environ 41 000 m³/s).",
            difficulty = "easy",
            reference = "Hydrologie mondiale"
        ),
        QuestionEntity(
            categoryId = "geographie",
            subcategory = "Reliefs & Montagnes",
            question = "Dans quelle chaîne de montagnes se trouve le sommet le plus élevé de la planète, le mont Everest (8 848 m) ?",
            optionA = "Les Andes",
            optionB = "L'Himalaya",
            optionC = "Le Caucase",
            optionD = "Les Alpes",
            correctAnswer = "B",
            explanation = "L'Everest (Sagarmatha en népalais, Chomolungma en tibétain) se situe à la frontière entre le Népal et la Chine dans l'Himalaya.",
            difficulty = "easy",
            reference = "Orographie mondiale"
        ),
        QuestionEntity(
            categoryId = "geographie",
            subcategory = "Détroits Stratégiques",
            question = "Quel détroit maritime sépare la péninsule Ibérique (Europe) de la côte marocaine (Afrique) et relie la mer Méditerranée à l'océan Atlantique ?",
            optionA = "Le détroit du Bosphore",
            optionB = "Le détroit de Bab-el-Mandeb",
            optionC = "Le détroit de Gibraltar",
            optionD = "Le détroit de Malacca",
            correctAnswer = "C",
            explanation = "Le détroit de Gibraltar fait seulement environ 14 km de large à son point le plus resserré.",
            difficulty = "easy",
            reference = "Géographie maritime"
        ),
        QuestionEntity(
            categoryId = "geographie",
            subcategory = "Lacs & Records",
            question = "Quel est le lac le plus profond et le plus ancien du monde, contenant à lui seul environ 20 % de l'eau douce de surface non gelée de la Terre ?",
            optionA = "Le lac Supérieur",
            optionB = "Le lac Tanganyika",
            optionC = "Le lac Baïkal",
            optionD = "Le lac Victoria",
            correctAnswer = "C",
            explanation = "Le lac Baïkal en Sibérie (Russie) atteint 1 642 mètres de profondeur.",
            difficulty = "medium",
            reference = "Limnologie internationale"
        ),
        QuestionEntity(
            categoryId = "geographie",
            subcategory = "Capitales du Monde",
            question = "Quelle est la capitale politique de l'Australie (souvent confondue avec Sydney ou Melbourne) ?",
            optionA = "Brisbane",
            optionB = "Canberra",
            optionC = "Adélaïde",
            optionD = "Perth",
            correctAnswer = "B",
            explanation = "Canberra a été choisie comme capitale de compromis en 1908 entre Sydney et Melbourne.",
            difficulty = "medium",
            reference = "Géographie politique"
        ),
        QuestionEntity(
            categoryId = "geographie",
            subcategory = "Déserts",
            question = "Quel désert d'Amérique du Sud, situé au nord du Chili, est considéré comme le plus aride de la planète ?",
            optionA = "Le désert du Namib",
            optionB = "Le désert d'Atacama",
            optionC = "Le désert du Kalahari",
            optionD = "Le désert de Gobi",
            correctAnswer = "B",
            explanation = "Le désert d'Atacama enregistre certaines des plus faibles précipitations au monde, certaines zones n'ayant reçu aucune pluie pendant des décennies.",
            difficulty = "medium",
            reference = "Climatologie planétaire"
        ),

        // ==========================================
        // === 4. SCIENCES, NATURE & ESPACE =========
        // ==========================================
        QuestionEntity(
            categoryId = "science",
            subcategory = "Physique Théorique",
            question = "Dans la célèbre équation d'Albert Einstein E = mc², que représente la lettre 'c' ?",
            optionA = "La constante de gravitation",
            optionB = "La vitesse de la lumière dans le vide",
            optionC = "La capacité thermique",
            optionD = "La charge électrique de l'électron",
            correctAnswer = "B",
            explanation = "c représente la célérité (vitesse) de la lumière dans le vide, soit environ 299 792 458 m/s.",
            difficulty = "easy",
            reference = "Relativité restreinte (1905)"
        ),
        QuestionEntity(
            categoryId = "science",
            subcategory = "Biologie Moléculaire",
            question = "Quelles sont les 4 bases azotées constituant la double hélice de la molécule d'ADN ?",
            optionA = "Adénine, Thymine, Cytosine, Guanine",
            optionB = "Adénine, Uracile, Cytosine, Guanine",
            optionC = "Alanine, Tyrosine, Cystéine, Glycine",
            optionD = "Acétyle, Thymine, Choline, Glutamine",
            correctAnswer = "A",
            explanation = "L'ADN utilise l'Adénine (A), la Thymine (T), la Cytosine (C) et la Guanine (G). L'Uracile remplace la Thymine dans l'ARN.",
            difficulty = "medium",
            reference = "Génétique moderne"
        ),
        QuestionEntity(
            categoryId = "science",
            subcategory = "Astronomie",
            question = "Quelle planète du Système Solaire est surnommée la 'Planète Rouge' en raison de l'oxyde de fer (rouille) recouvrant sa surface ?",
            optionA = "Vénus",
            optionB = "Mars",
            optionC = "Mercure",
            optionD = "Jupiter",
            correctAnswer = "B",
            explanation = "Mars doit sa couleur rougeoyante caractéristique à l'abondance de régolithe riche en oxyde de fer.",
            difficulty = "easy",
            reference = "Exploration spatiale (NASA / ESA)"
        ),
        QuestionEntity(
            categoryId = "science",
            subcategory = "Chimie Fondamentale",
            question = "Quel chimiste russe a publié en 1869 la première version du Tableau périodique des éléments classés selon leur masse atomique ?",
            optionA = "Dmitri Mendeleïev",
            optionB = "Antoine Lavoisier",
            optionC = "Niels Bohr",
            optionD = "John Dalton",
            correctAnswer = "A",
            explanation = "Mendeleïev organisa les éléments connus et prédit avec exactitude l'existence et les propriétés d'éléments encore non découverts comme le gallium et le germanium.",
            difficulty = "medium",
            reference = "Tableau périodique des éléments"
        ),
        QuestionEntity(
            categoryId = "science",
            subcategory = "Physique Quantique",
            question = "Quel physicien a énoncé le fameux principe selon lequel on ne peut mesurer simultanément avec une précision infinie la position et la vitesse d'une particule ?",
            optionA = "Max Planck",
            optionB = "Werner Heisenberg (Principe d'indétermination)",
            optionC = "Erwin Schrödinger",
            optionD = "Paul Dirac",
            correctAnswer = "B",
            explanation = "Le principe d'incertitude ou d'indétermination de Heisenberg (1927) est un pilier de la mécanique quantique.",
            difficulty = "hard",
            reference = "Mécanique quantique"
        ),
        QuestionEntity(
            categoryId = "science",
            subcategory = "Médecine & Physiologie",
            question = "Quel groupe sanguin est considéré comme le 'donneur universel' de globules rouges pour les transfusions sanguines d'urgence ?",
            optionA = "Groupe AB positif (AB+)",
            optionB = "Groupe A négatif (A-)",
            optionC = "Groupe O négatif (O-)",
            optionD = "Groupe B positif (B+)",
            correctAnswer = "C",
            explanation = "Le sang O négatif (O-) ne possède ni antigènes A, B, ni facteur Rhésus à la surface des globules rouges, évitant le rejet immunitaire immédiat.",
            difficulty = "easy",
            reference = "Hématologie et transfusion"
        ),

        // ==========================================
        // === 5. LITTÉRATURE & PHILOSOPHIE =========
        // ==========================================
        QuestionEntity(
            categoryId = "litterature",
            subcategory = "Philosophie Antique",
            question = "Quel philosophe grec de l'Antiquité a formulé l'allégorie de la Caverne dans son ouvrage 'La République' ?",
            optionA = "Socrate",
            optionB = "Platon",
            optionC = "Aristote",
            optionD = "Épicure",
            correctAnswer = "B",
            explanation = "Platon utilise l'allégorie de la caverne (Livre VII de La République) pour illustrer la transition du monde des apparences sensibles vers le monde des Idées intelligibles.",
            difficulty = "medium",
            reference = "Platon, La République (Livre VII)"
        ),
        QuestionEntity(
            categoryId = "litterature",
            subcategory = "Classiques Mondiaux",
            question = "Quel roman fleuve de Victor Hugo met en scène Jean Valjean, Cosette, Fantine et l'inspecteur Javert ?",
            optionA = "Notre-Dame de Paris",
            optionB = "Les Misérables",
            optionC = "Quatrevingt-Treize",
            optionD = "Les Travailleurs de la mer",
            correctAnswer = "B",
            explanation = "Publié en 1862, Les Misérables est un chef-d'œuvre de la littérature humaniste et sociale du XIXe siècle.",
            difficulty = "easy",
            reference = "Victor Hugo, Les Misérables"
        ),
        QuestionEntity(
            categoryId = "litterature",
            subcategory = "Figures de Style",
            question = "Quelle figure de style consiste à juxtaposer deux termes de sens opposés dans un même syntagme (ex: « une obscure clarté ») ?",
            optionA = "Une métonymie",
            optionB = "Un oxymore",
            optionC = "Une anaphore",
            optionD = "Une hyperbole",
            correctAnswer = "B",
            explanation = "L'oxymore réunit deux mots contradictoires pour créer une image poétique frappante (Corneille dans Le Cid : « Cette obscure clarté qui tombe des étoiles »).",
            difficulty = "easy",
            reference = "Rhétorique française"
        ),
        QuestionEntity(
            categoryId = "litterature",
            subcategory = "Littérature Africaine & Négritude",
            question = "Quel poète martiniquais a forgé le concept de 'Négritude' aux côtés de Léopold Sédar Senghor et Léon-Gontran Damas dans son 'Cahier d'un retour au pays natal' ?",
            optionA = "Frantz Fanon",
            optionB = "Aimé Césaire",
            optionC = "Édouard Glissant",
            optionD = "Cheikh Anta Diop",
            correctAnswer = "B",
            explanation = "Aimé Césaire a proclamé la Négritude comme l'affirmation et la fierté de l'identité noire face à l'aliénation coloniale.",
            difficulty = "medium",
            reference = "Aimé Césaire, Cahier d'un retour au pays natal (1939)"
        ),
        QuestionEntity(
            categoryId = "litterature",
            subcategory = "Philosophie Moderne",
            question = "À quel philosophe français doit-on la maxime fondamentale « Je pense, donc je suis » (Cogito, ergo sum) ?",
            optionA = "Jean-Jacques Rousseau",
            optionB = "René Descartes",
            optionC = "Voltaire",
            optionD = "Michel de Montaigne",
            correctAnswer = "B",
            explanation = "Descartes pose le doute méthodique et le 'Cogito' comme première certitude inébranlable dans le Discours de la méthode (1637).",
            difficulty = "easy",
            reference = "Descartes, Discours de la méthode"
        ),
        QuestionEntity(
            categoryId = "litterature",
            subcategory = "Mythologie & Épopées",
            question = "Quel est le plus ancien texte littéraire et poétique connu de l'humanité, rédigé en cunéiforme sur des tablettes d'argile en Mésopotamie ?",
            optionA = "L'Iliade d'Homère",
            optionB = "Le Mahabharata",
            optionC = "L'Épopée de Gilgamesh",
            optionD = "Le Livre des Morts",
            correctAnswer = "C",
            explanation = "L'Épopée de Gilgamesh raconte la quête d'immortalité du roi d'Uruk et date du troisième millénaire av. J.-C.",
            difficulty = "medium",
            reference = "Tablettes mésopotamiennes cunéiformes"
        ),

        // ==========================================
        // === 6. AFRIQUE & PANAFRICANISME ==========
        // ==========================================
        QuestionEntity(
            categoryId = "afrique",
            subcategory = "Empires & Souverains",
            question = "Quel empereur du Mali du XIVe siècle est réputé pour son légendaire pèlerinage à La Mecque en 1324 au cours duquel il distribua tant d'or qu'il dévalua le métal précieux au Caire ?",
            optionA = "Soundiata Keïta",
            optionB = "Mansa Moussa (Kankou Moussa)",
            optionC = "Askia Mohammed",
            optionD = "Samory Touré",
            correctAnswer = "B",
            explanation = "Mansa Moussa est souvent considéré comme l'homme le plus fortuné de l'histoire universelle grâce aux mines d'or de Bambouk et Bouré.",
            difficulty = "easy",
            reference = "Chroniques d'Ibn Battuta et Al-Umari"
        ),
        QuestionEntity(
            categoryId = "afrique",
            subcategory = "Héros & Penseurs",
            question = "Quel président visionnaire du Burkina Faso a changé le nom de la 'Haute-Volta' en 'Pays des hommes intègres' et a mené une politique d'autosuffisance alimentaire ?",
            optionA = "Thomas Sankara",
            optionB = "Kwame Nkrumah",
            optionC = "Amílcar Cabral",
            optionD = "Modibo Keïta",
            correctAnswer = "A",
            explanation = "Thomas Sankara a dirigé le Burkina Faso de 1983 à 1987, devenant une icône du panafricanisme et de la souveraineté populaire.",
            difficulty = "easy",
            reference = "Histoire politique africaine"
        ),
        QuestionEntity(
            categoryId = "afrique",
            subcategory = "Institutions & Panafricanisme",
            question = "En quelle année et dans quelle capitale l'Organisation de l'Unité Africaine (OUA, devenue Union Africaine en 2002) a-t-elle été fondée ?",
            optionA = "1960 à Dakar",
            optionB = "1963 à Addis-Abeba",
            optionC = "1958 à Conakry",
            optionD = "1970 à Lagos",
            correctAnswer = "B",
            explanation = "Le 25 mai 1963, 32 chefs d'État africains ont signé la charte de l'OUA à Addis-Abeba (Éthiopie), date célébrée comme la Journée de l'Afrique.",
            difficulty = "medium",
            reference = "Charte de l'OUA (1963)"
        ),
        QuestionEntity(
            categoryId = "afrique",
            subcategory = "Histoire & Savoir",
            question = "Quelle célèbre ville du Mali abritait au XVe siècle la prestigieuse université de Sankoré et des milliers de précieux manuscrits anciens ?",
            optionA = "Gao",
            optionB = "Tombouctou",
            optionC = "Djenné",
            optionD = "Ségou",
            correctAnswer = "B",
            explanation = "Tombouctou était un carrefour transsaharien majeur d'échanges intellectuels, scientifiques, théologiques et commerciaux.",
            difficulty = "easy",
            reference = "Patrimoine mondial UNESCO"
        ),
        QuestionEntity(
            categoryId = "afrique",
            subcategory = "Lutte Anti-Apartheid",
            question = "Combien d'années Nelson Mandela a-t-il passées en prison (dont 18 ans à Robben Island) avant d'être libéré en 1990 et élu président d'Afrique du Sud en 1994 ?",
            optionA = "10 ans",
            optionB = "18 ans",
            optionC = "27 ans",
            optionD = "35 ans",
            correctAnswer = "C",
            explanation = "Nelson Mandela a été incarcéré de 1962 à 1990 pour sa lutte contre le régime d'apartheid.",
            difficulty = "easy",
            reference = "Nelson Mandela, Un long chemin vers la liberté"
        ),
        QuestionEntity(
            categoryId = "afrique",
            subcategory = "Égypte & Nubie",
            question = "Quelle dynastie de pharaons noirs originaires du royaume de Koush (Nubie / actuel Soudan) a régné sur toute l'Égypte antique au VIIIe siècle av. J.-C. ?",
            optionA = "La XVIIIe dynastie (Thoutmôsis)",
            optionB = "La XIXe dynastie (Ramsès)",
            optionC = "La XXVe dynastie (pharaons koushites / nubiens)",
            optionD = "La dynastie ptolémaïque",
            correctAnswer = "C",
            explanation = "Les pharaons koushites comme Piye, Shabaka et Taharqa ont unifié et gouverné l'Égypte pendant la 25e dynastie.",
            difficulty = "hard",
            reference = "Égyptologie et histoire nubienne"
        ),

        // ==========================================
        // === 7. RDC & PATRIMOINE CONGOLAIS ========
        // ==========================================
        QuestionEntity(
            categoryId = "rdc",
            subcategory = "Histoire & Indépendance",
            question = "Qui a prononcé le discours mémorable du 30 juin 1960 dénonçant le régime colonial lors de la proclamation de l'indépendance de la RDC ?",
            optionA = "Joseph Kasa-Vubu",
            optionB = "Patrice Émery Lumumba",
            optionC = "Moïse Tshombé",
            optionD = "Albert Kalonji",
            correctAnswer = "B",
            explanation = "Le Premier ministre Patrice Lumumba a prononcé un discours historique rétablissant la vérité sur la lutte du peuple congolais pour la liberté et la dignité.",
            difficulty = "easy",
            reference = "Discours du 30 juin 1960 (Kinshasa)"
        ),
        QuestionEntity(
            categoryId = "rdc",
            subcategory = "Géographie & Territoire",
            question = "Depuis le découpage territorial issu de la Constitution de 2006, en combien de provinces la République Démocratique du Congo est-elle subdivisée ?",
            optionA = "11 provinces",
            optionB = "18 provinces",
            optionC = "26 provinces (y compris la ville-province de Kinshasa)",
            optionD = "32 provinces",
            correctAnswer = "C",
            explanation = "La RDC compte 25 provinces et la ville-province de Kinshasa, soit 26 provinces au total.",
            difficulty = "easy",
            reference = "Constitution de la RDC (Article 2)"
        ),
        QuestionEntity(
            categoryId = "rdc",
            subcategory = "Biodiversité & Nature",
            question = "Quel mammifère rare et menacé, ressemblant à un croisement entre un zèbre et une girafe, est strictement endémique des forêts de la RDC (notamment en Ituri) ?",
            optionA = "Le Bongo",
            optionB = "L'Okapi",
            optionC = "Le Bonobo",
            optionD = "Le Chevrotain aquatique",
            correctAnswer = "B",
            explanation = "L'Okapi (Okapia johnstoni) est l'animal symbole national de la RDC et ne vit à l'état sauvage nulle part ailleurs sur Terre.",
            difficulty = "easy",
            reference = "Réserve de faune à okapis (UNESCO)"
        ),
        QuestionEntity(
            categoryId = "rdc",
            subcategory = "Parcs Nationaux",
            question = "Créé en 1925 (anciennement Parc Albert), quel est le plus ancien parc national d'Afrique, abritant les gorilles de montagne au Nord-Kivu ?",
            optionA = "Parc national de la Garamba",
            optionB = "Parc national des Virunga",
            optionC = "Parc national de Kahuzi-Biega",
            optionD = "Parc national de la Salonga",
            correctAnswer = "B",
            explanation = "Le parc national des Virunga a été fondé en 1925 et possède une biodiversité exceptionnelle autour des volcans Nyiragongo et Nyamuragira.",
            difficulty = "medium",
            reference = "Institut Congolais pour la Conservation de la Nature (ICCN)"
        ),
        QuestionEntity(
            categoryId = "rdc",
            subcategory = "Énergie & Économie",
            question = "Sur quel fleuve sont construits les puissants barrages hydroélectriques d'Inga I et Inga II dans la province du Kongo-Central ?",
            optionA = "La rivière Kasaï",
            optionB = "Le fleuve Congo",
            optionC = "La rivière Ubangi",
            optionD = "La rivière Lualaba",
            correctAnswer = "B",
            explanation = "Le site des chutes d'Inga sur le fleuve Congo représente l'un des plus grands potentiels hydroélectriques au monde (Grand Inga : plus de 40 000 MW).",
            difficulty = "easy",
            reference = "SNEL / Géographie économique de la RDC"
        ),
        QuestionEntity(
            categoryId = "rdc",
            subcategory = "Royaumes Anciens",
            question = "Quelle était la capitale historique du grand Royaume Kongo avant et pendant les premiers contacts avec les explorateurs portugais au XVe siècle ?",
            optionA = "Mbanza Kongo (renommée plus tard São Salvador)",
            optionB = "Boma",
            optionC = "Mpumbu",
            optionD = "Musumba",
            correctAnswer = "A",
            explanation = "Mbanza Kongo, située au cœur du royaume, était le centre politique et spirituel gouverné par le Manikongo.",
            difficulty = "medium",
            reference = "Histoire du Royaume Kongo (UNESCO)"
        ),

        // ==========================================
        // === 8. ARTS, MUSIQUE & CINÉMA ============
        // ==========================================
        QuestionEntity(
            categoryId = "musique",
            subcategory = "Musique Congolaise & Rumba",
            question = "Quel hymne musical emblématique composé par Grand Kallé et l'African Jazz a célébré la table ronde de Bruxelles et l'indépendance en 1960 ?",
            optionA = "Mario",
            optionB = "Indépendance Cha Cha",
            optionC = "Para Fifi",
            optionD = "Délivrance",
            correctAnswer = "B",
            explanation = "Indépendance Cha Cha (1960) est considérée comme le premier grand tube panafricain célébrant les émancipations africaines.",
            difficulty = "easy",
            reference = "Histoire de la rumba congolaise (UNESCO 2021)"
        ),
        QuestionEntity(
            categoryId = "musique",
            subcategory = "Arts Plastiques & Peinture",
            question = "Quel génie de la Renaissance italienne a peint la célèbre 'Joconde' (Mona Lisa) et la fresque de 'La Cène' à Milan ?",
            optionA = "Michel-Ange",
            optionB = "Léonard de Vinci",
            optionC = "Raphaël",
            optionD = "Botticelli",
            correctAnswer = "B",
            explanation = "Léonard de Vinci a peint le portrait de Mona Lisa au début du XVIe siècle, conservé au musée du Louvre.",
            difficulty = "easy",
            reference = "Musée du Louvre (Paris)"
        ),
        QuestionEntity(
            categoryId = "musique",
            subcategory = "Musique Classique",
            question = "Quel compositeur allemand, devenu complètement sourd vers la fin de sa vie, a composé la majestueuse Neuvième Symphonie avec son 'Hymne à la Joie' ?",
            optionA = "Wolfgang Amadeus Mozart",
            optionB = "Johann Sebastian Bach",
            optionC = "Ludwig van Beethoven",
            optionD = "Johannes Brahms",
            correctAnswer = "C",
            explanation = "Beethoven créa sa Neuvième Symphonie en 1824 à Vienne alors qu'il n'entendait plus rien, intégrant le poème de Schiller.",
            difficulty = "easy",
            reference = "Histoire de la musique classique"
        ),
        QuestionEntity(
            categoryId = "musique",
            subcategory = "Cinéma Mondial",
            question = "Quel film de science-fiction réalisé par James Cameron en 2009 sur la planète Pandora est devenu le plus grand succès au box-office mondial de tous les temps ?",
            optionA = "Titanic",
            optionB = "Avatar",
            optionC = "Avengers: Endgame",
            optionD = "Star Wars : Le Réveil de la Force",
            correctAnswer = "B",
            explanation = "Avatar a rapporté près de 2,9 milliards de dollars au box-office mondial.",
            difficulty = "easy",
            reference = "Box-office mondial"
        ),
        QuestionEntity(
            categoryId = "musique",
            subcategory = "Mouvements Artistiques",
            question = "Quel peintre espagnol est le cofondateur du mouvement Cubiste avec Georges Braque et l'auteur du tableau antifasciste 'Guernica' (1937) ?",
            optionA = "Salvador Dalí",
            optionB = "Pablo Picasso",
            optionC = "Joan Miró",
            optionD = "Francisco de Goya",
            correctAnswer = "B",
            explanation = "Pablo Picasso a peint Guernica en réaction au bombardement de la ville basque par l'aviation nazie en 1937.",
            difficulty = "medium",
            reference = "Musée Reina Sofía (Madrid)"
        ),
        QuestionEntity(
            categoryId = "musique",
            subcategory = "Légendes Musicales",
            question = "Quel monument de la musique congolaise, surnommé 'Le Grand Maître' et leader du Tout Puissant OK Jazz, a dominé la scène africaine pendant quatre décennies ?",
            optionA = "Tabu Ley Rochereau",
            optionB = "Franco Luambo Makiadi",
            optionC = "Papa Wemba",
            optionD = "Kabasele Yampanya (Pépé Kallé)",
            correctAnswer = "B",
            explanation = "Franco Luambo Makiadi (1938-1989) est l'un des guitaristes et compositeurs les plus influents de l'histoire du continent africain.",
            difficulty = "medium",
            reference = "Anthologie TP OK Jazz"
        ),

        // ==========================================
        // === 9. BIBLE & TEXTES SACRÉS =============
        // ==========================================
        QuestionEntity(
            categoryId = "bible",
            subcategory = "Langues & Manuscrits",
            question = "Dans quelles langues originelles les textes de l'Ancien Testament et du Nouveau Testament ont-ils été principalement rédigés ?",
            optionA = "Latin pour l'Ancien Testament, Arabe pour le Nouveau",
            optionB = "Hébreu (et quelques passages en araméen) pour l'Ancien, Grec koinè pour le Nouveau",
            optionC = "Syriaque pour l'Ancien, Copte pour le Nouveau",
            optionD = "Grec ancien pour les deux testaments",
            correctAnswer = "B",
            explanation = "L'Ancien Testament a été rédigé en hébreu biblique (avec quelques portions en araméen chez Daniel et Esdras), et le Nouveau Testament en grec populaire (koinè).",
            difficulty = "medium",
            reference = "Sciences bibliques et philologie"
        ),
        QuestionEntity(
            categoryId = "bible",
            subcategory = "Archéologie Biblique",
            question = "Quels célèbres manuscrits bibliques antiques, découverts en 1947 dans des grottes près de la mer Morte, ont confirmé l'authenticité textuelle des Écritures ?",
            optionA = "Les manuscrits de Tombouctou",
            optionB = "Les manuscrits de Qumrân (rouleaux de la mer Morte)",
            optionC = "Le Codex Sinaiticus",
            optionD = "Le Papyrus d'Éléphantine",
            correctAnswer = "B",
            explanation = "Les rouleaux de Qumrân contenaient des copies de presque tous les livres de l'Ancien Testament remontant au IIIe siècle av. J.-C.",
            difficulty = "medium",
            reference = "Archéologie de Qumrân"
        ),
        QuestionEntity(
            categoryId = "bible",
            subcategory = "Ancien Testament",
            question = "Quel prophète biblique a confronté les 450 prophètes de Baal sur le mont Carmel et a été enlevé au ciel dans un char de feu ?",
            optionA = "Élisée",
            optionB = "Élie le Thishbite",
            optionC = "Ésaïe",
            optionD = "Jérémie",
            correctAnswer = "B",
            explanation = "Élie a démontré la souveraineté de l'Éternel sur le mont Carmel (1 Rois 18) et fut enlevé dans un tourbillon (2 Rois 2).",
            difficulty = "easy",
            reference = "1 Rois 18 / 2 Rois 2"
        ),
        QuestionEntity(
            categoryId = "bible",
            subcategory = "Nouveau Testament",
            question = "Sur la route de quelle ville Saül de Tarse a-t-il été terrassé par une lumière éclatante du Christ avant de devenir l'apôtre Paul ?",
            optionA = "Antioche",
            optionB = "Damas",
            optionC = "Rome",
            optionD = "Éphèse",
            correctAnswer = "B",
            explanation = "Actes 9 relate la conversion fulgurante de Saül sur le chemin de Damas en Syrie.",
            difficulty = "easy",
            reference = "Actes des Apôtres 9:1-19"
        ),
        QuestionEntity(
            categoryId = "bible",
            subcategory = "Sagesse & Rois",
            question = "Quel roi d'Israël, fils de David, a demandé à Dieu la sagesse et le discernement plutôt que la richesse ou la mort de ses ennemis ?",
            optionA = "Roboam",
            optionB = "Salomon",
            optionC = "Josias",
            optionD = "Ézéchias",
            correctAnswer = "B",
            explanation = "Dieu lui accorda une sagesse incomparable ainsi que la gloire et les richesses (1 Rois 3:5-14).",
            difficulty = "easy",
            reference = "1 Rois 3:9-12"
        ),
        QuestionEntity(
            categoryId = "bible",
            subcategory = "Théologie & Évangiles",
            question = "Dans quel Évangile trouve-t-on le célèbre prologue théologique : « Au commencement était la Parole, et la Parole était avec Dieu, et la Parole était Dieu » ?",
            optionA = "Évangile selon Matthieu",
            optionB = "Évangile selon Marc",
            optionC = "Évangile selon Luc",
            optionD = "Évangile selon Jean",
            correctAnswer = "D",
            explanation = "Le prologue de Jean (Jean 1:1-14) présente Jésus-Christ comme le Logos éternel incarné parmi les hommes.",
            difficulty = "medium",
            reference = "Jean 1:1"
        ),
        QuestionEntity(
            categoryId = "bible",
            subcategory = "Prophéties & Symboles",
            question = "Quel prophète captif à Babylone a eu la vision saisissante de la vallée des ossements desséchés reprenant vie par l'Esprit ?",
            optionA = "Ézéchiel",
            optionB = "Daniel",
            optionC = "Osée",
            optionD = "Amos",
            correctAnswer = "A",
            explanation = "Ézéchiel 37 décrit la résurrection symbolique et la restauration du peuple d'Israël par le souffle de l'Esprit divin.",
            difficulty = "hard",
            reference = "Ézéchiel 37:1-14"
        ),

        // ==========================================
        // === 10. TECHNOLOGIES & IA ================
        // ==========================================
        QuestionEntity(
            categoryId = "technologie",
            subcategory = "Pionniers de l'Informatique",
            question = "Quel mathématicien britannique a brisé la machine de chiffrement nazie Enigma à Bletchley Park et posé les bases théoriques de l'informatique ?",
            optionA = "Charles Babbage",
            optionB = "Alan Turing",
            optionC = "John von Neumann",
            optionD = "Claude Shannon",
            correctAnswer = "B",
            explanation = "Alan Turing (1912-1954) a conçu la 'Bombe' pour déchiffrer Enigma et formulé le modèle de la 'Machine de Turing' et le Test de Turing en IA.",
            difficulty = "medium",
            reference = "Histoire de l'informatique moderne"
        ),
        QuestionEntity(
            categoryId = "technologie",
            subcategory = "Première Programmeuse",
            question = "Quelle mathématicienne du XIXe siècle est reconnue comme la première programmeuse de l'histoire pour avoir écrit le premier algorithme destiné à la Machine analytique de Babbage ?",
            optionA = "Grace Hopper",
            optionB = "Ada Lovelace",
            optionC = "Margaret Hamilton",
            optionD = "Katherine Johnson",
            correctAnswer = "B",
            explanation = "Ada Lovelace a rédigé en 1843 des notes détaillant le calcul des nombres de Bernoulli sur une machine mécanique.",
            difficulty = "medium",
            reference = "Notes sur la machine analytique (1843)"
        ),
        QuestionEntity(
            categoryId = "technologie",
            subcategory = "Intelligence Artificielle",
            question = "Que signifie l'acronyme 'LLM' désignant les modèles d'IA générative modernes tels que Gemini et GPT ?",
            optionA = "Logical Learning Machine",
            optionB = "Large Language Model (Grand Modèle de Langage)",
            optionC = "Linear Link Matrix",
            optionD = "Low Latency Memory",
            correctAnswer = "B",
            explanation = "Un Large Language Model est un réseau de neurones profond entraîné sur de vastes corpus textuels pour comprendre et générer du langage naturel.",
            difficulty = "easy",
            reference = "Deep Learning & NLP"
        ),
        QuestionEntity(
            categoryId = "technologie",
            subcategory = "Réseaux & Internet",
            question = "Quel réseau informatique militaire et universitaire américain créé en 1969 est considéré comme l'ancêtre direct d'Internet ?",
            optionA = "ARPANET",
            optionB = "Ethernet",
            optionC = "Usenet",
            optionD = "BITNET",
            correctAnswer = "A",
            explanation = "ARPANET a inauguré la transmission de données par commutation de paquets et le protocole TCP/IP.",
            difficulty = "medium",
            reference = "Histoire des télécommunications"
        ),
        QuestionEntity(
            categoryId = "technologie",
            subcategory = "Composants Électroniques",
            question = "Quel composant semi-conducteur miniature, inventé en 1947 aux laboratoires Bell, a remplacé les tubes à vide et permis la révolution électronique ?",
            optionA = "La résistance",
            optionB = "Le transistor",
            optionC = "Le condensateur",
            optionD = "La diode laser",
            correctAnswer = "B",
            explanation = "Inventé par Bardeen, Brattain et Shockley (prix Nobel 1956), le transistor est la brique élémentaire de tous les microprocesseurs modernes.",
            difficulty = "medium",
            reference = "Prix Nobel de Physique 1956"
        ),

        // ==========================================
        // === 11. SPORTS & JEUX OLYMPIQUES =========
        // ==========================================
        QuestionEntity(
            categoryId = "sport",
            subcategory = "Athlétisme & Records",
            question = "Quel sprinteur jamaïcain détient les records du monde du 100 m (9,58 s) et du 200 m (19,19 s) établis à Berlin en 2009 ?",
            optionA = "Carl Lewis",
            optionB = "Usain Bolt",
            optionC = "Asafa Powell",
            optionD = "Tyson Gay",
            correctAnswer = "B",
            explanation = "Usain Bolt, surnommé 'Lightning Bolt', est octuple champion olympique et légende planétaire de l'athlétisme.",
            difficulty = "easy",
            reference = "Fédération Internationale d'Athlétisme (World Athletics)"
        ),
        QuestionEntity(
            categoryId = "sport",
            subcategory = "Coupe du Monde de Football",
            question = "Quel pays a remporté la Coupe du Monde de football de la FIFA le plus grand nombre de fois (5 titres : 1958, 1962, 1970, 1994, 2002) ?",
            optionA = "L'Allemagne",
            optionB = "L'Italie",
            optionC = "Le Brésil",
            optionD = "L'Argentine",
            correctAnswer = "C",
            explanation = "Le Brésil (la Seleção) est le seul pays à avoir décroché 5 étoiles en Coupe du monde masculine.",
            difficulty = "easy",
            reference = "FIFA World Cup Archives"
        ),
        QuestionEntity(
            categoryId = "sport",
            subcategory = "Jeux Olympiques Antiques & Modernes",
            question = "Dans quel pays se sont tenus les premiers Jeux Olympiques de l'ère moderne en 1896 à l'initiative du baron Pierre de Coubertin ?",
            optionA = "En France (Paris)",
            optionB = "En Grèce (Athènes)",
            optionC = "En Angleterre (Londres)",
            optionD = "En Suisse (Lausanne)",
            correctAnswer = "B",
            explanation = "Les Jeux de 1896 ont eu lieu dans le stade panathénaïque d'Athènes pour faire le lien avec les Jeux antiques d'Olympie.",
            difficulty = "easy",
            reference = "Comité International Olympique (CIO)"
        ),
        QuestionEntity(
            categoryId = "sport",
            subcategory = "Légendes du Sport Africain",
            question = "Quel footballeur libérien est l'unique joueur africain de l'histoire à avoir remporté le prestigieux Ballon d'Or en 1995 avant de devenir président de son pays ?",
            optionA = "Samuel Eto'o",
            optionB = "Didier Drogba",
            optionC = "George Weah",
            optionD = "Roger Milla",
            correctAnswer = "C",
            explanation = "George Weah a remporté le Ballon d'Or France Football et le prix du Joueur mondial de la FIFA en 1995 sous les couleurs du Milan AC et du PSG.",
            difficulty = "easy",
            reference = "France Football / Palmarès Ballon d'Or"
        ),
        QuestionEntity(
            categoryId = "sport",
            subcategory = "Combats Légendaires",
            question = "Dans quelle ville s'est déroulé le mythique match de boxe 'Rumble in the Jungle' le 30 octobre 1974 opposant Mohamed Ali à George Foreman ?",
            optionA = "Dakar (Sénégal)",
            optionB = "Kinshasa (Zaïre / RDC) au Stade du 20 Mai",
            optionC = "Abidjan (Côte d'Ivoire)",
            optionD = "Lagos (Nigeria)",
            correctAnswer = "B",
            explanation = "Mohamed Ali a reconquis le titre mondial des poids lourds en battant George Foreman par KO au 8e round à Kinshasa.",
            difficulty = "easy",
            reference = "Histoire mondiale de la boxe"
        ),

        // ==========================================
        // === 12. LOGIQUE, MATHS & ÉNIGMES =========
        // ==========================================
        QuestionEntity(
            categoryId = "logique",
            subcategory = "Suites Mathématiques",
            question = "Dans la célèbre suite de Fibonacci (0, 1, 1, 2, 3, 5, 8, 13, ...), quel est le nombre suivant ?",
            optionA = "18",
            optionB = "20",
            optionC = "21",
            optionD = "26",
            correctAnswer = "C",
            explanation = "Dans la suite de Fibonacci, chaque terme est la somme des deux précédents : 8 + 13 = 21.",
            difficulty = "easy",
            reference = "Théorie des nombres"
        ),
        QuestionEntity(
            categoryId = "logique",
            subcategory = "Géométrie & Théorèmes",
            question = "Quel théorème mathématique fondamental stipule que dans tout triangle rectangle, le carré de l'hypoténuse est égal à la somme des carrés des deux autres côtés (a² + b² = c²) ?",
            optionA = "Théorème de Thalès",
            optionB = "Théorème de Pythagore",
            optionC = "Théorème d'Euclide",
            optionD = "Théorème de Fermat",
            correctAnswer = "B",
            explanation = "Le théorème de Pythagore est un résultat géométrique majeur connu depuis l'Antiquité grecque et babylonienne.",
            difficulty = "easy",
            reference = "Géométrie euclidienne"
        ),
        QuestionEntity(
            categoryId = "logique",
            subcategory = "Probabilités & Paradoxes",
            question = "Dans le célèbre problème de probabilités de 'Monty Hall' (avec 3 portes, dont une seule cache une voiture), le candidat augmente-t-il ses chances de gagner en changeant son choix initial après l'ouverture d'une porte vide ?",
            optionA = "Non, les chances restent strictement de 50/50",
            optionB = "Oui, changer fait passer la probabilité de gain de 1/3 à 2/3",
            optionC = "Non, les chances diminuent à 1/4",
            optionD = "Cela ne change rien car le hasard est pur",
            correctAnswer = "B",
            explanation = "En changeant systématiquement, la probabilité de gagner double mathématiquement pour atteindre 2/3 (66,7 %).",
            difficulty = "hard",
            reference = "Théorie des probabilités conditionnelles"
        ),
        QuestionEntity(
            categoryId = "logique",
            subcategory = "Énigmes Déductives",
            question = "Un père et son fils ont 36 ans à eux deux. Le père a 30 ans de plus que son fils. Quel âge a le fils ?",
            optionA = "6 ans",
            optionB = "3 ans",
            optionC = "4 ans",
            optionD = "2 ans",
            correctAnswer = "B",
            explanation = "Soit x l'âge du fils. Père = x + 30. Total : x + (x + 30) = 36 => 2x = 6 => x = 3 ans. (Le père a 33 ans, 33 + 3 = 36).",
            difficulty = "medium",
            reference = "Calcul algébrique récréatif"
        ),
        QuestionEntity(
            categoryId = "logique",
            subcategory = "Physique & Paradoxe",
            question = "Quel célèbre paradoxe d'astronomie et de cosmologie formulé par Enrico Fermi s'interroge : « Si des civilisations extraterrestres avancées existent dans l'Univers, où sont-elles donc ? »",
            optionA = "Le paradoxe d'Olbers",
            optionB = "Le paradoxe de Fermi",
            optionC = "Le paradoxe des jumeaux de Langevin",
            optionD = "Le paradoxe du chat de Schrödinger",
            correctAnswer = "B",
            explanation = "Le paradoxe de Fermi souligne la contradiction apparente entre l'estimation élevée de la probabilité de vie extraterrestre et l'absence totale de preuves directes.",
            difficulty = "medium",
            reference = "Astrophysique et équation de Drake"
        )
    )
}
