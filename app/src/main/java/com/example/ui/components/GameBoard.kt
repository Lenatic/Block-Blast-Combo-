package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.LayoutCoordinates
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.BlockColor
import com.example.model.BlockPiece
import com.example.viewmodel.GameViewModel
import kotlin.math.roundToInt
import kotlin.random.Random

// Holds the coordinate values during dragging
class DragStateHolder {
    var rootLayoutCoordinates: LayoutCoordinates? = null
    var boardCoordinates: LayoutCoordinates? = null
    
    // Original positions in root coordinates
    val originalSlotCenters = rmutableStateMapOf<Int, Offset>()

    // Current drag offset from start
    var activeDragIndex by mutableStateOf<Int?>(null)
    var dragDelta by mutableStateOf(Offset.Zero)
}

// Custom mutable state helper for slots mapping
fun <K, V> rmutableStateMapOf() = mutableStateMapOf<K, V>()

@Composable
fun GameContainer(
    viewModel: GameViewModel,
    modifier: Modifier = Modifier,
    onOpenSettings: () -> Unit
) {
    val board by viewModel.board.collectAsState()
    val activePieces by viewModel.activePieces.collectAsState()
    val selectedIndex by viewModel.selectedPieceIndex.collectAsState()
    val score by viewModel.score.collectAsState()
    val highScore by viewModel.highScore.collectAsState()
    val particles by viewModel.particles.collectAsState()
    val banners by viewModel.comboBanners.collectAsState()
    val shake by viewModel.shakeIntensity.collectAsState()
    val isGameOver by viewModel.isGameOver.collectAsState()

    // Cheat values for dashboard indicators
    val cheat10x by viewModel.cheat10xScore.collectAsState()
    val cheat3x3 by viewModel.cheat3x3Only.collectAsState()

    val dragState = remember { DragStateHolder() }
    val density = LocalDensity.current

    // Screen Shake Offset Calculation
    val shakeOffset = remember(shake) {
        if (shake > 0f) {
            val amplitude = shake * 9f
            Offset(
                x = (Random.nextFloat() * 2f - 1f) * amplitude,
                y = (Random.nextFloat() * 2f - 1f) * amplitude
            )
        } else {
            Offset.Zero
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF1C1B1F),
                        Color(0xFF141316)
                    )
                )
            )
            .windowInsetsPadding(WindowInsets.safeDrawing)
            .onGloballyPositioned { coords ->
                dragState.rootLayoutCoordinates = coords
            }
    ) {
        // Floating Stars / Sophisticated silver & lavender background dust
        Canvas(modifier = Modifier.fillMaxSize()) {
            val random = java.util.Random(42L)
            for (i in 0 until 40) {
                val x = random.nextFloat() * size.width
                val y = random.nextFloat() * size.height
                val radius = 1.2f + random.nextFloat() * 2f
                val color = if (random.nextBoolean()) Color(0x3DDFD6FF) else Color(0x2CE6E1E5)
                drawCircle(color, radius, Offset(x, y))
            }
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.SpaceBetween,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Header Stats Area
            HeaderBar(
                score = score,
                highScore = highScore,
                cheat10x = cheat10x,
                cheat3x3 = cheat3x3,
                onOpenSettings = onOpenSettings,
                onResetClick = { viewModel.resetGame() }
            )

            // Playboard Container with shake modifiers
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(vertical = 12.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    // Display Active Cheat Indicators
                    if (cheat10x || cheat3x3) {
                        Row(
                            horizontalArrangement = Arrangement.Center,
                            modifier = Modifier.padding(bottom = 8.dp)
                        ) {
                            if (cheat10x) {
                                Card(
                                    colors = CardDefaults.cardColors(containerColor = Color(0xFFD0BCFF)),
                                    border = BorderStroke(1.dp, Color(0xFFD0BCFF)),
                                    modifier = Modifier.padding(horizontal = 4.dp)
                                ) {
                                    Text(
                                        text = "⚡ SCORE 10x ON",
                                        color = Color(0xFF381E72),
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Black,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                    )
                                }
                            }
                            if (cheat3x3) {
                                Card(
                                    colors = CardDefaults.cardColors(containerColor = Color(0xFF31111D)),
                                    border = BorderStroke(1.dp, Color(0xFFF2B8B5)),
                                    modifier = Modifier.padding(horizontal = 4.dp)
                                ) {
                                    Text(
                                        text = "🧱 3x3 ONLY ON",
                                        color = Color(0xFFF2B8B5),
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Black,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                    )
                                }
                            }
                        }
                    }

                    Box(
                        modifier = Modifier
                            .aspectRatio(1f)
                            .shadow(24.dp, shape = RoundedCornerShape(20.dp), spotColor = Color(0xFFD0BCFF))
                            .offset {
                                IntOffset(
                                    shakeOffset.x.dp.roundToPx(),
                                    shakeOffset.y.dp.roundToPx()
                                )
                            }
                            .clip(RoundedCornerShape(20.dp))
                            .background(Color(0xFF1C1B1F))
                            .border(BorderStroke(3.dp, Color(0xFF49454F)), RoundedCornerShape(20.dp))
                            .onGloballyPositioned { coords ->
                                dragState.boardCoordinates = coords
                            }
                    ) {
                        // Compute interactive hover shadows based on drag state
                        var activeGhostCell by remember { mutableStateOf<Pair<Int, Int>?>(null) }
                        
                        // Recalculating ghost previews over grid
                        val activeDrag = dragState.activeDragIndex
                        if (activeDrag != null && activePieces.getOrNull(activeDrag) != null) {
                            val piece = activePieces[activeDrag]!!
                            val rootCoords = dragState.rootLayoutCoordinates
                            val boardCoords = dragState.boardCoordinates
                            
                            if (rootCoords != null && boardCoords != null) {
                                val boardSize = boardCoords.size
                                val rootPos = rootCoords.positionInRoot()
                                val boardPos = boardCoords.positionInRoot()
                                
                                val startSlotOffset = dragState.originalSlotCenters[activeDrag] ?: Offset.Zero
                                val currentFingerRoot = startSlotOffset + dragState.dragDelta
                                val currentFingerBoard = currentFingerRoot - (boardPos - rootPos)
                                
                                val cellW = boardSize.width.toFloat() / 8f
                                val cellH = boardSize.height.toFloat() / 8f
                                
                                // Direct centering adjustment
                                val rawCol = ((currentFingerBoard.x - (piece.width * cellW) / 2f) / cellW).roundToInt()
                                val rawRow = ((currentFingerBoard.y - (piece.height * cellH) / 2f) / cellH).roundToInt()
                                
                                val targetCell = Pair(rawRow, rawCol)
                                activeGhostCell = if (viewModel.canPlacePiece(piece, rawRow, rawCol)) {
                                    targetCell
                                } else {
                                    null
                                }
                            }
                        } else if (selectedIndex != null && activePieces.getOrNull(selectedIndex!!) != null) {
                            // If Tap-to-Place highlights, we can hover based on tapped/selected shadows
                            activeGhostCell = null
                        } else {
                            activeGhostCell = null
                        }

                        // Background Grid Drawing
                        GameBoardGrid(
                            board = board,
                            selectedIndex = selectedIndex,
                            selectedPiece = selectedIndex?.let { activePieces.getOrNull(it) },
                            activeGhostCell = activeGhostCell,
                            onCellClick = { r, c ->
                                selectedIndex?.let { pieceIdx ->
                                    val piece = activePieces.getOrNull(pieceIdx)
                                    if (piece != null) {
                                        // Center alignment math
                                        val startRow = r - (piece.height - 1) / 2
                                        val startCol = c - (piece.width - 1) / 2
                                        if (viewModel.placePiece(pieceIdx, startRow, startCol)) {
                                            viewModel.selectPieceIndex(null)
                                        }
                                    }
                                }
                            }
                        )

                        // Particle Blast Overlay Layer
                        ParticlesRenderer(particles = particles)

                        // Floating juicy notifications layer
                        BannersRenderer(banners = banners)
                    }
                }
            }

            // Bottom docking action slots
            SlotsDock(
                activePieces = activePieces,
                selectedIndex = selectedIndex,
                dragState = dragState,
                onSelectPiece = { idx ->
                    if (selectedIndex == idx) {
                        viewModel.selectPieceIndex(null)
                    } else {
                        viewModel.selectPieceIndex(idx)
                    }
                },
                onReleaseDrag = { pieceIndex, finalFingerPos ->
                    val boardCoords = dragState.boardCoordinates
                    val rootCoords = dragState.rootLayoutCoordinates
                    if (boardCoords != null && rootCoords != null) {
                        val boardPos = boardCoords.positionInRoot()
                        val rootPos = rootCoords.positionInRoot()
                        val startSlotOffset = dragState.originalSlotCenters[pieceIndex] ?: Offset.Zero
                        val currentFingerRoot = startSlotOffset + finalFingerPos
                        val relativeFingerBoard = currentFingerRoot - (boardPos - rootPos)
                        
                        val piece = activePieces.getOrNull(pieceIndex)
                        if (piece != null) {
                            val cellW = boardCoords.size.width.toFloat() / 8f
                            val cellH = boardCoords.size.height.toFloat() / 8f
                            
                            val rawCol = ((relativeFingerBoard.x - (piece.width * cellW) / 2f) / cellW).roundToInt()
                            val rawRow = ((relativeFingerBoard.y - (piece.height * cellH) / 2f) / cellH).roundToInt()
                            
                            // Attempt placement drop
                            viewModel.placePiece(pieceIndex, rawRow, rawCol)
                        }
                    }
                }
            )
        }

        // Draggable floating overlay representing the piece during movement
        val activeDragIdxToDraw = dragState.activeDragIndex
        if (activeDragIdxToDraw != null && activePieces.getOrNull(activeDragIdxToDraw) != null) {
            val piece = activePieces[activeDragIdxToDraw]!!
            val initialCenter = dragState.originalSlotCenters[activeDragIdxToDraw] ?: Offset.Zero
            val renderPosition = initialCenter + dragState.dragDelta

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .pointerInput(Unit) {} // Block taps on raw backgrounds while dragging
            ) {
                Box(
                    modifier = Modifier
                        .offset {
                            IntOffset(
                                (renderPosition.x - (piece.width * 28f) / 1.5f).roundToInt(),
                                (renderPosition.y - (piece.height * 28f) / 1.5f).roundToInt()
                            )
                        }
                        .shadow(16.dp, RoundedCornerShape(8.dp), clip = false)
                ) {
                    // Draw magnified, slightly translucent version of the piece under the thumb
                    MiniPieceLayout(
                        piece = piece,
                        sizeFactor = 32.dp,
                        alpha = 0.85f
                    )
                }
            }
        }

        // Full Screen Game Over Sheet / Overlay
        AnimatedVisibility(
            visible = isGameOver,
            modifier = Modifier.fillMaxSize()
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0xE6131215)),
                contentAlignment = Alignment.Center
            ) {
                Card(
                    modifier = Modifier
                        .widthIn(max = 340.dp)
                        .padding(24.dp)
                        .shadow(24.dp, shape = RoundedCornerShape(24.dp), spotColor = Color(0xFFEFB8C8)),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF2B2930)),
                    border = BorderStroke(2.dp, Color(0xFFF2B8B5))
                ) {
                    Column(
                        modifier = Modifier.padding(28.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .size(64.dp)
                                .clip(RoundedCornerShape(16.dp))
                                .background(Color(0xFF31111D)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Warning,
                                contentDescription = "Game Over Arrow",
                                tint = Color(0xFFF2B8B5),
                                modifier = Modifier.size(36.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Text(
                            text = "NO MORE MOVES",
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Black,
                            color = Color.White,
                            textAlign = TextAlign.Center,
                            letterSpacing = 1.sp
                        )

                        Text(
                            text = "You are bricked out! Challenge yourself with game-changing combos and clear records.",
                            fontSize = 13.sp,
                            color = Color(0xFFCCC2DC),
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(vertical = 12.dp)
                        )

                        Card(
                            colors = CardDefaults.cardColors(containerColor = Color(0xFF1C1B1F)),
                            modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)
                        ) {
                            Column(
                                modifier = Modifier.fillMaxWidth().padding(16.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text("FINAL SCORE", color = Color(0xFFCCC2DC), fontSize = 11.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
                                Text("$score", color = Color(0xFFD0BCFF), fontSize = 32.sp, fontWeight = FontWeight.Black)
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Button(
                            onClick = { viewModel.resetGame() },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD0BCFF)),
                            modifier = Modifier.fillMaxWidth(),
                            contentPadding = PaddingValues(14.dp)
                        ) {
                            Text("EXPLODE AGAIN 💥", fontWeight = FontWeight.Black, color = Color(0xFF381E72))
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun HeaderBar(
    score: Int,
    highScore: Int,
    cheat10x: Boolean,
    cheat3x3: Boolean,
    onOpenSettings: () -> Unit,
    onResetClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(
                text = "BLOCK BLAST",
                fontSize = 20.sp,
                fontWeight = FontWeight.Black,
                color = Color.White,
                letterSpacing = 2.sp
            )
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "COMBO MEGAPACK",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFD0BCFF),
                    letterSpacing = 1.sp
                )
                if (cheat10x || cheat3x3) {
                    Text(
                        text = " (Cheats Active)",
                        fontSize = 9.sp,
                        color = Color(0xFFF2B8B5),
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        Row(verticalAlignment = Alignment.CenterVertically) {
            // Scores Panel cards using #2B2930 / #49454F themes
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFF2B2930))
                    .border(BorderStroke(1.dp, Color(0xFF49454F)), RoundedCornerShape(12.dp))
                    .padding(horizontal = 12.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("SCORE", fontSize = 10.sp, color = Color(0xFFD0BCFF), fontWeight = FontWeight.Bold, letterSpacing = 0.5.sp)
                    Text("$score", fontSize = 18.sp, color = Color.White, fontWeight = FontWeight.Black)
                }
                Box(
                    modifier = Modifier
                        .width(1.dp)
                        .height(28.dp)
                        .background(Color(0xFF49454F))
                )
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("CROWN", fontSize = 10.sp, color = Color(0xFFEFB8C8), fontWeight = FontWeight.Bold, letterSpacing = 0.5.sp)
                    Text("$highScore", fontSize = 18.sp, color = Color(0xFFEFB8C8), fontWeight = FontWeight.Black)
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            // Settings/Cheats Button with sophisticated glowing border
            IconButton(
                onClick = onOpenSettings,
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(Color(0xFF2B2930))
                    .border(BorderStroke(1.5.dp, Color(0xFFD0BCFF)), RoundedCornerShape(10.dp))
            ) {
                Text("⚙️", fontSize = 16.sp)
            }
        }
    }
}

// 8x8 Grid Builder
@Composable
fun GameBoardGrid(
    board: List<List<BlockColor?>>,
    selectedIndex: Int?,
    selectedPiece: BlockPiece?,
    activeGhostCell: Pair<Int, Int>?,
    onCellClick: (Int, Int) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(10.dp),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        for (r in 0 until 8) {
            Row(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                for (c in 0 until 8) {
                    val color = board[r][c]
                    
                    // Is this cell part of a placing shadow preview?
                    var isGhostShadow = false
                    var ghostColor: Color? = null
                    
                    if (activeGhostCell != null && selectedPiece != null) {
                        val gr = activeGhostCell.first
                        val gc = activeGhostCell.second
                        val shapeRow = r - gr
                        val shapeCol = c - gc
                        if (shapeRow in 0 until selectedPiece.height && shapeCol in 0 until selectedPiece.width) {
                            if (selectedPiece.shape[shapeRow][shapeCol]) {
                                isGhostShadow = true
                                ghostColor = selectedPiece.color.toColor()
                            }
                        }
                    } else if (selectedIndex != null && selectedPiece != null) {
                        // For select and placement highlights, we highlight cells that match piece if they hover or tap
                        // Fallback simple shadow visual preview is elegant
                    }

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .aspectRatio(1f)
                            .padding(2.dp)
                            .clip(RoundedCornerShape(5.dp))
                            .background(
                                when {
                                    color != null -> Color.Transparent // drawn custom block
                                    isGhostShadow -> ghostColor!!.copy(alpha = 0.38f) // Ghost transparent overlay
                                    else -> Color(0xFF49454F).copy(alpha = 0.3f)
                                }
                            )
                            .border(
                                width = 1.dp,
                                color = when {
                                    color != null -> Color.Transparent
                                    isGhostShadow -> ghostColor!!.copy(alpha = 0.8f)
                                    else -> Color(0xFF49454F).copy(alpha = 0.5f)
                                },
                                shape = RoundedCornerShape(5.dp)
                            )
                            .clickable(
                                enabled = selectedIndex != null,
                                indication = null,
                                interactionSource = remember { MutableInteractionSource() }
                            ) {
                                onCellClick(r, c)
                            }
                    ) {
                        if (color != null) {
                            TactileBlockRenderer(color = color.toColor())
                        }
                    }
                }
            }
        }
    }
}

// Highly stylized tactile glass cube block
@Composable
fun TactileBlockRenderer(color: Color) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .drawBehind {
                val bevelSize = size.width * 0.12f
                
                // Draw bottom right deep bevel shade
                val darkPath = Path().apply {
                    moveTo(0f, size.height)
                    lineTo(size.width, size.height)
                    lineTo(size.width, 0f)
                    lineTo(size.width - bevelSize, bevelSize)
                    lineTo(size.width - bevelSize, size.height - bevelSize)
                    lineTo(bevelSize, size.height - bevelSize)
                    close()
                }
                drawPath(darkPath, color = Color(0x3B000000))

                // Draw top left bright bevel tint
                val lightPath = Path().apply {
                    moveTo(0f, size.height)
                    lineTo(0f, 0f)
                    lineTo(size.width, 0f)
                    lineTo(size.width - bevelSize, bevelSize)
                    lineTo(bevelSize, bevelSize)
                    lineTo(bevelSize, size.height - bevelSize)
                    close()
                }
                drawPath(lightPath, color = Color(0x40FFFFFF))
            }
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        color.copy(alpha = 0.95f),
                        color.copy(alpha = 0.72f)
                    )
                ),
                shape = RoundedCornerShape(5.dp)
            )
            .border(1.dp, Color(0xFFFFFFFF).copy(alpha = 0.2f), RoundedCornerShape(5.dp))
    ) {
        // Inner reflective dot for high polish candy blast satisfaction
        Box(
            modifier = Modifier
                .padding(4.dp)
                .size(6.dp)
                .background(Color.White.copy(alpha = 0.45f), shape = RoundedCornerShape(2.dp))
                .align(Alignment.TopStart)
        )
    }
}

// 3 Slots dock at the bottom of the screen
@Composable
fun SlotsDock(
    activePieces: List<BlockPiece?>,
    selectedIndex: Int?,
    dragState: DragStateHolder,
    onSelectPiece: (Int) -> Unit,
    onReleaseDrag: (Int, Offset) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(Color(0xFF2B2930))
            .border(BorderStroke(1.5.dp, Color(0xFF49454F)), RoundedCornerShape(24.dp))
            .padding(vertical = 12.dp, horizontal = 4.dp),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically
    ) {
        val density = LocalDensity.current
        
        for (i in 0..2) {
            val piece = activePieces.getOrNull(i)

            Box(
                modifier = Modifier
                    .weight(1f)
                    .aspectRatio(1.1f)
                    .onGloballyPositioned { coords ->
                        // Cache initial center coordinates of slots for offset calculations
                        if (dragState.activeDragIndex == null) {
                            val winPos = coords.positionInRoot()
                            val center = Offset(
                                x = winPos.x + coords.size.width / 2f,
                                y = winPos.y + coords.size.height / 2f
                            )
                            dragState.originalSlotCenters[i] = center
                        }
                    }
                    .padding(4.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(
                        if (selectedIndex == i) Color(0x2BD0BCFF) else Color(0x0CFFFFFF)
                    )
                    .border(
                        BorderStroke(
                            width = if (selectedIndex == i) 2.dp else 1.dp,
                            color = if (selectedIndex == i) Color(0xFFD0BCFF) else Color(0xFF49454F)
                        ),
                        shape = RoundedCornerShape(16.dp)
                    )
                    .pointerInput(piece) {
                        if (piece == null) return@pointerInput
                        detectDragGestures(
                            onDragStart = { startOffset ->
                                dragState.activeDragIndex = i
                                dragState.dragDelta = Offset.Zero
                            },
                            onDrag = { change, dragAmount ->
                                change.consume()
                                dragState.dragDelta += dragAmount
                            },
                            onDragEnd = {
                                val finalDelta = dragState.dragDelta
                                dragState.activeDragIndex = null
                                dragState.dragDelta = Offset.Zero
                                onReleaseDrag(i, finalDelta)
                            },
                            onDragCancel = {
                                dragState.activeDragIndex = null
                                dragState.dragDelta = Offset.Zero
                            }
                        )
                    }
                    .clickable(enabled = piece != null) {
                        onSelectPiece(i)
                    },
                contentAlignment = Alignment.Center
            ) {
                if (piece != null) {
                    MiniPieceLayout(piece = piece, sizeFactor = 20.dp)
                } else {
                    Box(
                        modifier = Modifier
                            .size(24.dp)
                            .background(Color(0xFF1C1B1F), shape = RoundedCornerShape(12.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("✓", color = Color(0xFFD0BCFF).copy(alpha = 0.5f), fontWeight = FontWeight.Bold, fontSize = 11.sp)
                    }
                }
            }
        }
    }
}

// Magnifies or normalizes piece shapes beautifully
@Composable
fun MiniPieceLayout(
    piece: BlockPiece,
    sizeFactor: Dp,
    alpha: Float = 1.0f
) {
    val h = piece.height
    val w = piece.width

    Column(
        modifier = Modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        for (r in 0 until h) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                for (c in 0 until w) {
                    val isActive = piece.shape[r][c]
                    Box(
                        modifier = Modifier
                            .size(sizeFactor)
                            .padding(1.5.dp)
                    ) {
                        if (isActive) {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(
                                        piece.color.toColor().copy(alpha = alpha),
                                        shape = RoundedCornerShape(3.dp)
                                    )
                                    .border(0.5.dp, Color.White.copy(alpha = 0.2f), RoundedCornerShape(3.dp))
                            )
                        }
                    }
                }
            }
        }
    }
}

// Particle Canvas Drawer
@Composable
fun ParticlesRenderer(
    particles: List<com.example.model.GameParticle>
) {
    Canvas(modifier = Modifier.fillMaxSize()) {
        val cellW = size.width / 8f
        val cellH = size.height / 8f
        
        particles.forEach { p ->
            val screenX = p.x * cellW
            val screenY = p.y * cellH
            
            // Draw sparks (combining star vectors or radial rings)
            rotate(p.rotation, pivot = Offset(screenX, screenY)) {
                // Outer core neon aura glow
                drawCircle(
                    color = p.color.copy(alpha = p.alpha * 0.28f),
                    radius = p.size * 1.5f,
                    center = Offset(screenX, screenY)
                )
                // Solid bright particle core
                drawRect(
                    color = Color.White.copy(alpha = p.alpha),
                    topLeft = Offset(screenX - p.size / 2f, screenY - p.size / 2f),
                    size = Size(p.size, p.size)
                )
                // Diamond colored overlay outline
                drawRect(
                    color = p.color.copy(alpha = p.alpha),
                    topLeft = Offset(screenX - p.size / 2f, screenY - p.size / 2f),
                    size = Size(p.size, p.size),
                    style = Stroke(width = 2.dp.toPx())
                )
            }
        }
    }
}

// Displays glowing text balloons overlaying completed areas
@Composable
fun BannersRenderer(
    banners: List<com.example.model.ComboBanner>
) {
    Box(modifier = Modifier.fillMaxSize()) {
        banners.forEach { banner ->
            // Let's compute grid coordinates relative to local pixels
            // This displays as interactive floaters right where combos explode
            val animatableScale = remember(banner.id) { Animatable(0.4f) }
            val animatableY = remember(banner.id) { Animatable(0f) }

            LaunchedEffect(banner.id) {
                // Pop scaling spring curves
                animatableScale.animateTo(1.05f, animationSpec = tween(180))
                animatableScale.animateTo(1.0f, animationSpec = tween(80))
            }
            
            val density = LocalDensity.current

            Box(
                modifier = Modifier
                    .fillMaxWidth(0.9f)
                    .align(Alignment.Center)
                    .padding(bottom = (80 - banner.life * 100).dp) // lift upwards over life
                    .graphicsLayer {
                        scaleX = banner.scale
                        scaleY = banner.scale
                        alpha = banner.alpha
                    }
                    .background(
                        Brush.horizontalGradient(
                            colors = listOf(
                                Color(0xCC2E004B),
                                Color(0xCC005B64),
                                Color(0xCC2E004B)
                            )
                        ),
                        shape = RoundedCornerShape(12.dp)
                    )
                    .border(BorderStroke(1.5.dp, Color(0xFFFFD54F)), RoundedCornerShape(12.dp))
                    .padding(horizontal = 14.dp, vertical = 6.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = banner.text,
                        color = Color.White,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Black,
                        textAlign = TextAlign.Center,
                        letterSpacing = 0.5.sp
                    )
                    Text(
                        text = banner.pointsText,
                        color = Color(0xFFFFD54F),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}

