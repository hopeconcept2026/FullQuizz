package com.example.data.remote

import com.example.data.local.dao.CategoryDao
import com.example.data.local.dao.QuestionDao
import com.example.data.local.entity.CategoryEntity
import com.example.data.local.entity.QuestionEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import java.util.UUID

/**
 * QuizCloudDataSource - Manages cloud-hosted question packs, online multiplayer lobbies,
 * and remote data synchronization for FullQuizz.
 */
class QuizCloudDataSource(
    private val questionDao: QuestionDao,
    private val categoryDao: CategoryDao
) {
    private val _activeRoom = MutableStateFlow<OnlineMatchRoom?>(null)
    val activeRoom: StateFlow<OnlineMatchRoom?> = _activeRoom.asStateFlow()

    private val _isSyncingCloud = MutableStateFlow(false)
    val isSyncingCloud: StateFlow<Boolean> = _isSyncingCloud.asStateFlow()

    private val _syncMessage = MutableStateFlow<String?>(null)
    val syncMessage: StateFlow<String?> = _syncMessage.asStateFlow()

    /**
     * Remote catalog of available question packs in the Cloud.
     */
    fun getAvailableCloudPacks(): List<CloudQuestionPack> {
        return listOf(
            CloudQuestionPack(
                packId = "pack_rdc_grand_lacs_v2",
                title = "Expansion RDC & Grands Lacs - V2",
                categoryId = "rdc",
                description = "Plus de 20 questions approfondies sur les parcs nationaux, les fleuves, la musique rumba et l'histoire politique.",
                questionCount = 5,
                version = 2,
                questions = listOf(
                    QuestionEntity(
                        categoryId = "rdc",
                        subcategory = "Géographie & Nature",
                        question = "Quel est le plus ancien parc national de la RDC et d'Afrique, créé en 1925 ?",
                        optionA = "Parc National des Virunga",
                        optionB = "Parc National de la Garamba",
                        optionC = "Parc National de Kahuzi-Biega",
                        optionD = "Parc National de Salonga",
                        correctAnswer = "A",
                        explanation = "Le parc national des Virunga, créé en 1925 sous le nom de Parc Albert, est le premier parc national créé en Afrique.",
                        difficulty = "medium",
                        reference = "Patrimoine mondial UNESCO"
                    ),
                    QuestionEntity(
                        categoryId = "rdc",
                        subcategory = "Culture & Musique",
                        question = "En quelle année la Rumba congolaise a-t-elle été officiellement inscrite au patrimoine culturel immatériel de l'humanité par l'UNESCO ?",
                        optionA = "2018",
                        optionB = "2021",
                        optionC = "2015",
                        optionD = "2023",
                        correctAnswer = "B",
                        explanation = "Le 14 décembre 2021, l'UNESCO a inscrit la rumba congolaise sur sa liste représentative du patrimoine culturel immatériel de l'humanité.",
                        difficulty = "medium",
                        reference = "UNESCO Décembre 2021"
                    ),
                    QuestionEntity(
                        categoryId = "rdc",
                        subcategory = "Histoire",
                        question = "Quel souverain a dirigé le Royaume Kongo lors du premier contact avec les explorateurs portugais en 1482 ?",
                        optionA = "Nzinga Nkuwu (João Ier)",
                        optionB = "Afonso Ier",
                        optionC = "Donna Kimpa Vita",
                        optionD = "M'siri",
                        correctAnswer = "A",
                        explanation = "Le Manikongo Nzinga Nkuwu régnait sur le royaume du Kongo lorsque le navigateur Diogo Cão est arrivé à l'embouchure du fleuve en 1482.",
                        difficulty = "hard",
                        reference = "Histoire générale du Royaume Kongo"
                    ),
                    QuestionEntity(
                        categoryId = "rdc",
                        subcategory = "Économie & Mines",
                        question = "La RDC est le premier producteur mondial de quel minerai indispensable aux batteries modernes ?",
                        optionA = "Cobalt",
                        optionB = "Lithium",
                        optionC = "Titane",
                        optionD = "Bauxite",
                        correctAnswer = "A",
                        explanation = "La RDC fournit plus de 65% de la production mondiale de cobalt, métal stratégique pour la transition énergétique.",
                        difficulty = "easy",
                        reference = "Rapport USGS & Ministère des Mines"
                    ),
                    QuestionEntity(
                        categoryId = "rdc",
                        subcategory = "Hydrologie",
                        question = "Quel est le deuxième fleuve le plus puissant du monde par son débit après l'Amazone ?",
                        optionA = "Le Fleuve Congo",
                        optionB = "Le Nil",
                        optionC = "Le Mississippi",
                        optionD = "Le Yangtsé",
                        correctAnswer = "A",
                        explanation = "Le fleuve Congo a un débit moyen de 41 000 m³/s, ce qui en fait le deuxième plus puissant au monde.",
                        difficulty = "easy",
                        reference = "Hydrologie mondiale"
                    )
                )
            ),
            CloudQuestionPack(
                packId = "pack_bible_prophetes_v2",
                title = "Les Prophètes & Révélations Bibliques",
                categoryId = "bible",
                description = "Pack enrichi sur l'Ancien Testament, l'exil à Babylone et les visions de l'Apocalypse.",
                questionCount = 4,
                version = 2,
                questions = listOf(
                    QuestionEntity(
                        categoryId = "bible",
                        subcategory = "Ancien Testament",
                        question = "Quel prophète a eu la vision de la vallée des ossements desséchés qui reprenaient vie ?",
                        optionA = "Ézéchiel",
                        optionB = "Jérémie",
                        optionC = "Ésaïe",
                        optionD = "Daniel",
                        correctAnswer = "A",
                        explanation = "Ézéchiel au chapitre 37 décrit la vision de la vallée des ossements secs qui reprennent souffle par la parole de l'Éternel.",
                        difficulty = "medium",
                        reference = "Ézéchiel 37:1-14"
                    ),
                    QuestionEntity(
                        categoryId = "bible",
                        subcategory = "Ancien Testament",
                        question = "Combien de jours et de nuits Jonas est-il resté dans le ventre du grand poisson ?",
                        optionA = "3 jours et 3 nuits",
                        optionB = "7 jours et 7 nuits",
                        optionC = "40 jours et 40 nuits",
                        optionD = "1 jour et 1 nuit",
                        correctAnswer = "A",
                        explanation = "Jonas 2:1 indique que Jonas demeura dans les entrailles du poisson trois jours et trois nuits.",
                        difficulty = "easy",
                        reference = "Jonas 2:1"
                    ),
                    QuestionEntity(
                        categoryId = "bible",
                        subcategory = "Nouveau Testament",
                        question = "Sur quelle île l'apôtre Jean a-t-il reçu et rédigé les visions de l'Apocalypse ?",
                        optionA = "Patmos",
                        optionB = "Chypre",
                        optionC = "Crète",
                        optionD = "Malte",
                        correctAnswer = "A",
                        explanation = "Jean était sur l'île appelée Patmos à cause de la parole de Dieu et du témoignage de Jésus.",
                        difficulty = "easy",
                        reference = "Apocalypse 1:9"
                    ),
                    QuestionEntity(
                        categoryId = "bible",
                        subcategory = "Rois d'Israël",
                        question = "Quel roi sage a demandé à Dieu un cœur intelligent pour discerner le bien et le mal plutôt que la richesse ?",
                        optionA = "Salomon",
                        optionB = "David",
                        optionC = "Josias",
                        optionD = "Ézéchias",
                        correctAnswer = "A",
                        explanation = "Salomon a demandé la sagesse à Gabaon et Dieu lui accorda à la fois sagesse, gloire et richesses.",
                        difficulty = "easy",
                        reference = "1 Rois 3:9-12"
                    )
                )
            ),
            CloudQuestionPack(
                packId = "pack_afrique_histoire_v2",
                title = "Grands Empires & Héros d'Afrique",
                categoryId = "afrique",
                description = "Découvrez l'Empire du Mali, Soundiata Keïta, Thomas Sankara, Nelson Mandela et le panafricanisme.",
                questionCount = 4,
                version = 1,
                questions = listOf(
                    QuestionEntity(
                        categoryId = "afrique",
                        subcategory = "Histoire",
                        question = "Quel empereur du Mali du 14ème siècle est célèbre pour son légendaire pèlerinage à La Mecque en 1324 ?",
                        optionA = "Mansa Moussa",
                        optionB = "Soundiata Keïta",
                        optionC = "Sony Ali Ber",
                        optionD = "Askia Mohammed",
                        correctAnswer = "A",
                        explanation = "Mansa Moussa (Kankou Moussa) est souvent considéré comme l'homme le plus riche de l'histoire grâce aux réserves d'or de l'Empire du Mali.",
                        difficulty = "medium",
                        reference = "Chroniques d'Ibn Battuta & Al-Umari"
                    ),
                    QuestionEntity(
                        categoryId = "afrique",
                        subcategory = "Géographie & Sommets",
                        question = "Quel est le plus haut sommet du continent africain avec 5 895 mètres d'altitude ?",
                        optionA = "Le Mont Kilimandjaro",
                        optionB = "Le Mont Kenya",
                        optionC = "Le Mont Stanley (Rwenzori)",
                        optionD = "Le Mont Cameroun",
                        correctAnswer = "A",
                        explanation = "Le mont Kilimandjaro en Tanzanie culmine au pic Uhuru à 5 895 mètres.",
                        difficulty = "easy",
                        reference = "Géographie physique de l'Afrique"
                    ),
                    QuestionEntity(
                        categoryId = "afrique",
                        subcategory = "Figures Panafricaines",
                        question = "Quel dirigeant burkinabé est célèbre pour avoir renommé son pays 'la patrie des hommes intègres' en 1984 ?",
                        optionA = "Thomas Sankara",
                        optionB = "Kwame Nkrumah",
                        optionC = "Patrice Lumumba",
                        optionD = "Julius Nyerere",
                        correctAnswer = "A",
                        explanation = "Thomas Sankara a rebaptisé la Haute-Volta en 'Burkina Faso' (la patrie des hommes intègres).",
                        difficulty = "easy",
                        reference = "Histoire contemporaine africaine"
                    ),
                    QuestionEntity(
                        categoryId = "afrique",
                        subcategory = "Institutions",
                        question = "En quelle année l'Union Africaine (succédant à l'OUA) a-t-elle été officiellement lancée à Durban ?",
                        optionA = "2002",
                        optionB = "1994",
                        optionC = "1963",
                        optionD = "2010",
                        correctAnswer = "A",
                        explanation = "L'Union Africaine (UA) a été officiellement inaugurée en juillet 2002 à Durban en Afrique du Sud.",
                        difficulty = "medium",
                        reference = "Sommet de Durban 2002"
                    )
                )
            )
        )
    }

    /**
     * Downloads and installs a cloud question pack into the local SQLite/Room database.
     */
    suspend fun installCloudPack(pack: CloudQuestionPack): Boolean = withContext(Dispatchers.IO) {
        _isSyncingCloud.value = true
        _syncMessage.value = "Téléchargement du pack '${pack.title}'..."
        try {
            delay(500) // Simulate cloud fetch
            questionDao.insertAll(pack.questions)
            categoryDao.refreshQuestionCounts()
            _syncMessage.value = "${pack.questions.size} questions installées avec succès !"
            _isSyncingCloud.value = false
            true
        } catch (e: Exception) {
            _syncMessage.value = "Erreur de synchronisation : ${e.message}"
            _isSyncingCloud.value = false
            false
        }
    }

    /**
     * Adds a user-created or admin question into local DB and syncs category count.
     */
    suspend fun createCustomQuestion(question: QuestionEntity): Boolean = withContext(Dispatchers.IO) {
        try {
            questionDao.insertAll(listOf(question))
            categoryDao.refreshQuestionCounts()
            true
        } catch (e: Exception) {
            false
        }
    }

    /**
     * Online Multiplayer: Creates a new Match Room with a generated 4-digit code.
     */
    suspend fun createMultiplayerRoom(
        hostNickname: String,
        hostAvatar: String,
        hostLevel: Int,
        categoryId: String,
        categoryName: String
    ): OnlineMatchRoom = withContext(Dispatchers.IO) {
        val randomCode = "QZ-" + (1000..9999).random()
        val questions = questionDao.getRandomQuestionsByCategory(categoryId, 5).ifEmpty {
            questionDao.getRandomQuestionsAll(5)
        }
        val room = OnlineMatchRoom(
            roomId = randomCode,
            categoryId = categoryId,
            categoryName = categoryName,
            host = OnlinePlayer(
                id = UUID.randomUUID().toString(),
                nickname = hostNickname,
                avatarId = hostAvatar,
                level = hostLevel
            ),
            status = MatchStatus.WAITING_FOR_OPPONENT,
            questions = questions
        )
        _activeRoom.value = room
        room
    }

    /**
     * Online Multiplayer: Joins an existing Match Room by code or creates a real waiting room.
     */
    suspend fun joinMultiplayerRoom(
        roomCode: String,
        playerNickname: String,
        playerAvatar: String,
        playerLevel: Int
    ): OnlineMatchRoom = withContext(Dispatchers.IO) {
        _isSyncingCloud.value = true
        val targetCode = roomCode.ifBlank { "QZ-" + (1000..9999).random() }
        _syncMessage.value = "Création du salon $targetCode en attente d'un adversaire..."

        val current = _activeRoom.value
        val questions = if (current != null && current.questions.isNotEmpty()) {
            current.questions
        } else {
            questionDao.getRandomQuestionsAll(5)
        }

        val room = OnlineMatchRoom(
            roomId = targetCode,
            categoryId = "all",
            categoryName = "Duel Multijoueur Direct",
            host = OnlinePlayer(
                id = UUID.randomUUID().toString(),
                nickname = playerNickname,
                avatarId = playerAvatar,
                level = playerLevel
            ),
            guest = null,
            status = MatchStatus.WAITING_FOR_OPPONENT,
            questions = questions
        )
        _activeRoom.value = room
        _isSyncingCloud.value = false
        _syncMessage.value = "Salon $targetCode ouvert ! Partagez ce code à un ami pour jouer."
        room
    }

    /**
     * Resets the active online match room.
     */
    fun leaveRoom() {
        _activeRoom.value = null
        _syncMessage.value = null
    }
}
