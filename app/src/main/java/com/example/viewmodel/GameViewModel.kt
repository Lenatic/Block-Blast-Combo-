package com.example.viewmodel

import android.app.Application
import android.content.Context
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.model.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

class GameViewModel(application: Application) : AndroidViewModel(application) {

    private val prefs = application.getSharedPreferences("block_blast_prefs", Context.MODE_PRIVATE)

    // Current Grid (8x8) representing placed blocks
    private val _board = MutableStateFlow<List<List<BlockColor?>>>(List(8) { List(8) { null } })
    val board: StateFlow<List<List<BlockColor?>>> = _board.asStateFlow()

    // The 3 piece choices at the bottom. Indices 0, 1, 2. Can be null when placed.
    private val _activePieces = MutableStateFlow<List<BlockPiece?>>(listOf(null, null, null))
    val activePieces: StateFlow<List<BlockPiece?>> = _activePieces.asStateFlow()

    // Current gameplay scores
    private val _score = MutableStateFlow(0)
    val score: StateFlow<Int> = _score.asStateFlow()

    private val _highScore = MutableStateFlow(0)
    val highScore: StateFlow<Int> = _highScore.asStateFlow()

    // Selected piece index for tap-to-place flow (0, 1, or 2)
    private val _selectedPieceIndex = MutableStateFlow<Int?>(null)
    val selectedPieceIndex: StateFlow<Int?> = _selectedPieceIndex.asStateFlow()

    private val _isGameOver = MutableStateFlow(false)
    val isGameOver: StateFlow<Boolean> = _isGameOver.asStateFlow()

    // Screen shake multiplier (decays down to 0 over tick loops)
    private val _shakeIntensity = MutableStateFlow(0f)
    val shakeIntensity: StateFlow<Float> = _shakeIntensity.asStateFlow()

    // Neon particles for explosions
    private val _particles = MutableStateFlow<List<GameParticle>>(emptyList())
    val particles: StateFlow<List<GameParticle>> = _particles.asStateFlow()

    // Floating text banners for score milestones and high-combo blasts
    private val _comboBanners = MutableStateFlow<List<ComboBanner>>(emptyList())
    val comboBanners: StateFlow<List<ComboBanner>> = _comboBanners.asStateFlow()

    // Cheats configuration (On/Off states inside settings)
    private val _cheat10xScore = MutableStateFlow(false)
    val cheat10xScore: StateFlow<Boolean> = _cheat10xScore.asStateFlow()

    private val _cheat3x3Only = MutableStateFlow(false)
    val cheat3x3Only: StateFlow<Boolean> = _cheat3x3Only.asStateFlow()

    // Clearing streak (clearing lines in consecutive placements)
    private val _comboStreak = MutableStateFlow(0)
    val comboStreak: StateFlow<Int> = _comboStreak.asStateFlow()

    private var particleIdCounter = 0L
    private var bannerIdCounter = 0L

    init {
        _highScore.value = prefs.getInt("high_score", 0)
        resetGame()

        // Core animation ticking loop (combos, particles, screen shake decay) at 60 FPS
        viewModelScope.launch {
            while (isActive) {
                delay(16)
                updateVisualEffects()
            }
        }
    }

    // Toggle 10x score multiplier
    fun toggleCheat10xScore() {
        _cheat10xScore.update { !it }
    }

    // Toggle 3x3 Block only generation
    fun toggleCheat3x3Only() {
        _cheat3x3Only.update { enabled ->
            val next = !enabled
            // If turned on, instantly convert existing vacant choices to 3x3 blocks for convenience
            if (next) {
                _activePieces.update { current ->
                    current.map { piece ->
                        if (piece != null) BlockPresets.Piece3x3.copy(id = "cheat_3x3_${System.nanoTime()}") else null
                    }
                }
            }
            next
        }
    }

    // Full restart of gameplay state
    fun resetGame() {
        _board.value = List(8) { List(8) { null } }
        _score.value = 0
        _comboStreak.value = 0
        _isGameOver.value = false
        _selectedPieceIndex.value = null
        _particles.value = emptyList()
        _comboBanners.value = emptyList()
        _shakeIntensity.value = 0f
        generateNewPieces()
    }

    // Fills up empty slots if all 3 blocks have been successfully placed
    private fun generateNewPieces() {
        val force3x3 = _cheat3x3Only.value
        _activePieces.value = listOf(
            BlockPresets.getRandomPiece(force3x3),
            BlockPresets.getRandomPiece(force3x3),
            BlockPresets.getRandomPiece(force3x3)
        )
        // Just in case, confirm if the generated set has valid placements left
        checkGameOverTrigger()
    }

    // Select piece index for tap-highlight placements placement
    fun selectPieceIndex(index: Int?) {
        if (index != null && _activePieces.value.getOrNull(index) == null) {
            _selectedPieceIndex.value = null
            return
        }
        _selectedPieceIndex.value = index
    }

    // Validates if piece can be placed at (startRow, startCol) on board
    fun canPlacePiece(piece: BlockPiece, startRow: Int, startCol: Int): Boolean {
        val boardVal = _board.value
        val h = piece.height
        val w = piece.width

        // Boundaries check
        if (startRow < 0 || startRow + h > 8) return false
        if (startCol < 0 || startCol + w > 8) return false

        // Occupied overlay collision checking
        for (r in 0 until h) {
            for (c in 0 until w) {
                if (piece.shape[r][c]) {
                    val targetR = startRow + r
                    val targetC = startCol + c
                    if (boardVal[targetR][targetC] != null) {
                        return false
                    }
                }
            }
        }
        return true
    }

    // Places a piece from specific index at position (row, col)
    fun placePiece(pieceIndex: Int, row: Int, col: Int): Boolean {
        val piece = _activePieces.value.getOrNull(pieceIndex) ?: return false

        if (!canPlacePiece(piece, row, col)) {
            // Shake slightly to indicate bad move placement
            _shakeIntensity.update { maxOf(it, 0.4f) }
            return false
        }

        // Apply to board
        val updatedBoard = _board.value.map { it.toMutableList() }
        for (r in 0 until piece.height) {
            for (c in 0 until piece.width) {
                if (piece.shape[r][c]) {
                    updatedBoard[row + r][col + c] = piece.color
                }
            }
        }
        _board.value = updatedBoard

        // Calculate placement points
        val placedCellsCount = piece.shape.sumOf { r -> r.count { it } }
        val rawBasePoints = placedCellsCount * 10
        val finalBasePoints = if (_cheat10xScore.value) rawBasePoints * 10 else rawBasePoints
        _score.update { it + finalBasePoints }

        // Remove the piece from slot list
        _activePieces.update { current ->
            current.mapIndexed { i, p -> if (i == pieceIndex) null else p }
        }
        _selectedPieceIndex.value = null

        // Check for completed columns & rows and execute clearance animations
        val clearCount = checkForLineClearances(row, col, piece.color.toColor())
        
        if (clearCount == 0) {
            _comboStreak.value = 0 // Break streak
        }

        // Check if all three elements are placed. If yes, generate 3 more!
        if (_activePieces.value.all { it == null }) {
            generateNewPieces()
        } else {
            checkGameOverTrigger()
        }

        // Persist score
        updateHighScore()
        return true
    }

    // Scan for complete rows/columns and trigger blasts
    private fun checkForLineClearances(placedRow: Int, placedCol: Int, sampleColor: Color): Int {
        val boardVal = _board.value
        val fullRows = mutableListOf<Int>()
        val fullCols = mutableListOf<Int>()

        // Analyze rows
        for (r in 0 until 8) {
            if (boardVal[r].all { it != null }) {
                fullRows.add(r)
            }
        }

        // Analyze cols
        for (c in 0 until 8) {
            var full = true
            for (r in 0 until 8) {
                if (boardVal[r][c] == null) {
                    full = false
                    break
                }
            }
            if (full) {
                fullCols.add(c)
            }
        }

        val totalLines = fullRows.size + fullCols.size
        if (totalLines > 0) {
            // Visual coordinates of columns/rows cleared to display floating bubble combo banner
            val centerRow = if (fullRows.isNotEmpty()) fullRows.average().toFloat() else placedRow.toFloat()
            val centerCol = if (fullCols.isNotEmpty()) fullCols.average().toFloat() else placedCol.toFloat()

            // Update Combo Streak State
            val nextStreak = _comboStreak.value + 1
            _comboStreak.value = nextStreak

            // Compute massive scores
            // Single: 100, Double: 300, Triple: 600, Quadruple+: 1000 + 400 * extra lines
            val rawClearPoints = when (totalLines) {
                1 -> 150
                2 -> 400
                3 -> 900
                4 -> 1600
                else -> 2500
            }

            // Streak addition points bonus
            val streakBonusPoints = (nextStreak - 1) * 100
            val totalRawWithBonus = rawClearPoints + streakBonusPoints

            val scoreWithCheatMultiplier = if (_cheat10xScore.value) totalRawWithBonus * 10 else totalRawWithBonus
            _score.update { it + scoreWithCheatMultiplier }

            // Trigger Screen Shake (up to 3.0f maximum amplitude)
            val baseShake = 0.8f + (0.5f * totalLines)
            val streakShakeBonus = 0.15f * nextStreak
            _shakeIntensity.update { minOf(3.2f, it + baseShake + streakShakeBonus) }

            // Clear occupied spots on grid and trigger particle explosions
            val updatedBoard = _board.value.map { it.toMutableList() }
            val cellsToClear = mutableListOf<Pair<Int, Int>>()

            for (r in fullRows) {
                for (c in 0 until 8) {
                    cellsToClear.add(Pair(r, c))
                }
            }
            for (c in fullCols) {
                for (r in 0 until 8) {
                    cellsToClear.add(Pair(r, c))
                }
            }

            // De-duplicate cells that are at the intersection of a full row and column
            val uniqueCells = cellsToClear.distinct()
            uniqueCells.forEach { (r, c) ->
                val originBlockColor = boardVal[r][c]?.toColor() ?: sampleColor
                spawnCellParticles(r, c, originBlockColor)
                updatedBoard[r][c] = null
            }
            _board.value = updatedBoard

            // Launch colorful banner displays
            val bannerPhrase = when {
                totalLines == 1 -> "NICE BLAST!"
                totalLines == 2 -> "GREAT DOUBLE!"
                totalLines == 3 -> "MEGA SPLASH!"
                totalLines >= 4 -> "GODLY COMBO BLAST!"
                else -> "COMBO!"
            }

            val finalBannerText = if (nextStreak > 1) "$bannerPhrase (Streak $nextStreak!)" else bannerPhrase
            val finalPointsText = "+$scoreWithCheatMultiplier pts"

            spawnComboBanner(
                text = finalBannerText,
                pointsText = finalPointsText,
                cellR = centerRow,
                cellC = centerCol
            )
        }

        return totalLines
    }

    // Explode sparks from center of cleared grid item
    private fun spawnCellParticles(gridRow: Int, gridCol: Int, color: Color) {
        // Spawn 14 glowing particles for each cell that burst outwards
        val burstCount = 13
        val particleList = mutableListOf<GameParticle>()

        // Distribute offsets randomly centered at block coordinates
        for (i in 0 until burstCount) {
            val angle = Random.nextFloat() * 2f * Math.PI.toFloat()
            val speed = 2f + Random.nextFloat() * 7f
            val vx = cos(angle) * speed * 0.05f
            val vy = sin(angle) * speed * 0.05f

            particleList.add(
                GameParticle(
                    id = particleIdCounter++,
                    x = gridCol.toFloat() + 0.5f, // center of cell
                    y = gridRow.toFloat() + 0.5f,
                    vx = vx,
                    vy = vy,
                    color = color,
                    size = 14f + Random.nextFloat() * 14f,
                    alpha = 1.0f,
                    life = 1.0f,
                    decayRate = 0.02f + Random.nextFloat() * 0.03f,
                    rotation = Random.nextFloat() * 360f,
                    rotSpeed = (Random.nextFloat() * 2f - 1f) * 8f
                )
            )
        }
        _particles.update { current -> current + particleList }
    }

    // Emit beautiful floating milestone phrases
    private fun spawnComboBanner(text: String, pointsText: String, cellR: Float, cellC: Float) {
        val banner = ComboBanner(
            id = bannerIdCounter++,
            text = text,
            pointsText = pointsText,
            x = cellC + 0.5f,
            y = cellR + 0.5f,
            scale = 0.4f,
            alpha = 1.0f,
            life = 1.0f
        )
        // Ensure not too many overlapping banners
        _comboBanners.update { current -> (current + banner).takeLast(4) }
    }

    // Main animation loop logic
    private fun updateVisualEffects() {
        // Update particles (lifespan reduction, drift velocity, gravity)
        _particles.update { current ->
            current.mapNotNull { p ->
                val nextLife = p.life - p.decayRate
                if (nextLife <= 0) null
                else {
                    p.copy(
                        x = p.x + p.vx,
                        y = p.y + p.vy,
                        vy = p.vy + 0.003f, // minimal gravitational drop
                        life = nextLife,
                        alpha = nextLife,
                        rotation = (p.rotation + p.rotSpeed) % 360f
                    )
                }
            }
        }

        // Update active banners (rise upwards, scale up, fade out)
        _comboBanners.update { current ->
            current.mapNotNull { b ->
                val nextLife = b.life - 0.022f
                if (nextLife <= 0) null
                else {
                    b.copy(
                        y = b.y - 0.04f, // Float upwards
                        scale = if (b.life > 0.85f) b.scale + 0.08f else b.scale, // pop in
                        alpha = if (nextLife < 0.3f) nextLife / 0.3f else 1.0f,
                        life = nextLife
                    )
                }
            }
        }

        // Subside screen vibration intensity over time
        if (_shakeIntensity.value > 0f) {
            _shakeIntensity.update { maxOf(0f, it - 0.09f) }
        }
    }

    // Examines active board layout against remaining pieces to trigger game over
    private fun checkGameOverTrigger() {
        val boardVal = _board.value
        val pieces = _activePieces.value
        val activePieces = pieces.filterNotNull()

        if (activePieces.isEmpty()) {
            _isGameOver.value = false
            return
        }

        // Game over if absolutely no active pieces have any valid cell locations to root into
        var canPlaceAny = false
        for (piece in activePieces) {
            val ph = piece.height
            val pw = piece.width
            for (r in 0..(8 - ph)) {
                for (c in 0..(8 - pw)) {
                    if (canPlacePiece(piece, r, c)) {
                        canPlaceAny = true
                        break
                    }
                }
                if (canPlaceAny) break
            }
            if (canPlaceAny) break
        }

        if (!canPlaceAny) {
            _isGameOver.value = true
        }
    }

    // Persist scores
    private fun updateHighScore() {
        val s = _score.value
        if (s > _highScore.value) {
            _highScore.value = s
            prefs.edit().putInt("high_score", s).apply()
        }
    }
}
