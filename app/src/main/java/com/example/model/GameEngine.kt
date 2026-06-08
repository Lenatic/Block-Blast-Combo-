package com.example.model

import androidx.compose.ui.graphics.Color

// Rich custom block colors following the Sophisticated Dark theme scheme
enum class BlockColor(val hex: Long) {
    SOPH_GOLD(0xFFEAD295),    // Elegant golden peach
    SOPH_SALMON(0xFFEAA695),  // Sophisticated salmon-coral pink
    SOPH_APRICOT(0xFFF2B8B5), // Reddish peach apricot
    SOPH_SAGE(0xFFB5E3C4),    // Soft sage mint green
    SOPH_ICE(0xFFBAC7EB),     // Elegant light blue-grey
    SOPH_PURPLE(0xFFD0BCFF),  // Lavender accent color
    SOPH_VIOLET(0xFF7755D1),  // Deep royal indigo violet
    SOPH_PINK(0xFFEFB8C8);    // Warm blossom pink

    fun toColor() = Color(hex)
}

// BlockPiece definition
data class BlockPiece(
    val id: String,
    val shape: List<List<Boolean>>,
    val color: BlockColor,
    val name: String
) {
    val width: Int get() = shape.firstOrNull()?.size ?: 0
    val height: Int get() = shape.size
}

// An active particle during combos
data class GameParticle(
    val id: Long,
    val x: Float, // Grid coordinates (can be fractional)
    val y: Float,
    val vx: Float, // Velocity vector X
    val vy: Float, // Velocity vector Y
    val color: Color,
    val size: Float,
    val alpha: Float,
    val life: Float,        // 1.0 down to 0.0
    val decayRate: Float,   // speed of life decay
    val rotation: Float,
    val rotSpeed: Float
)

// A floating text indicator for juicy combo milestones
data class ComboBanner(
    val id: Long,
    val text: String,
    val pointsText: String,
    val x: Float, // Centered at cell coordinate
    val y: Float,
    val scale: Float,
    val alpha: Float,
    val life: Float // 1.0 down to 0.0
)

// Preset blocks definition
object BlockPresets {
    val Piece1x1 = BlockPiece(
        id = "1x1",
        shape = listOf(listOf(true)),
        color = BlockColor.SOPH_GOLD,
        name = "Dot"
    )

    val Piece2x2 = BlockPiece(
        id = "2x2",
        shape = listOf(
            listOf(true, true),
            listOf(true, true)
        ),
        color = BlockColor.SOPH_SALMON,
        name = "Square 2x2"
    )

    val Piece3x3 = BlockPiece(
        id = "3x3",
        shape = listOf(
            listOf(true, true, true),
            listOf(true, true, true),
            listOf(true, true, true)
        ),
        color = BlockColor.SOPH_APRICOT,
        name = "Block 3x3"
    )

    val LineH2 = BlockPiece(
        id = "line_h_2",
        shape = listOf(listOf(true, true)),
        color = BlockColor.SOPH_SAGE,
        name = "Bar H 2"
    )

    val LineH3 = BlockPiece(
        id = "line_h_3",
        shape = listOf(listOf(true, true, true)),
        color = BlockColor.SOPH_ICE,
        name = "Bar H 3"
    )

    val LineH4 = BlockPiece(
        id = "line_h_4",
        shape = listOf(listOf(true, true, true, true)),
        color = BlockColor.SOPH_PURPLE,
        name = "Bar H 4"
    )

    val LineV2 = BlockPiece(
        id = "line_v_2",
        shape = listOf(listOf(true), listOf(true)),
        color = BlockColor.SOPH_SAGE,
        name = "Bar V 2"
    )

    val LineV3 = BlockPiece(
        id = "line_v_3",
        shape = listOf(listOf(true), listOf(true), listOf(true)),
        color = BlockColor.SOPH_ICE,
        name = "Bar V 3"
    )

    val LineV4 = BlockPiece(
        id = "line_v_4",
        shape = listOf(listOf(true), listOf(true), listOf(true), listOf(true)),
        color = BlockColor.SOPH_PURPLE,
        name = "Bar V 4"
    )

    val CornerL = BlockPiece(
        id = "corner_l",
        shape = listOf(
            listOf(true, true),
            listOf(true, false)
        ),
        color = BlockColor.SOPH_VIOLET,
        name = "Corner L"
    )

    val CornerJ = BlockPiece(
        id = "corner_j",
        shape = listOf(
            listOf(true, true),
            listOf(false, true)
        ),
        color = BlockColor.SOPH_VIOLET,
        name = "Corner J"
    )

    val ShapeT = BlockPiece(
        id = "shape_t",
        shape = listOf(
            listOf(true, true, true),
            listOf(false, true, false)
        ),
        color = BlockColor.SOPH_PINK,
        name = "T shape"
    )

    val ShapeL = BlockPiece(
        id = "shape_l",
        shape = listOf(
            listOf(true, false),
            listOf(true, false),
            listOf(true, true)
        ),
        color = BlockColor.SOPH_SALMON,
        name = "L shape"
    )

    val ShapeInverseL = BlockPiece(
        id = "shape_inv_l",
        shape = listOf(
            listOf(false, true),
            listOf(false, true),
            listOf(true, true)
        ),
        color = BlockColor.SOPH_SALMON,
        name = "Inverse L"
    )

    val ShapeZ = BlockPiece(
        id = "shape_z",
        shape = listOf(
            listOf(true, true, false),
            listOf(false, true, true)
        ),
        color = BlockColor.SOPH_SAGE,
        name = "Z block"
    )

    val ShapeS = BlockPiece(
        id = "shape_s",
        shape = listOf(
            listOf(false, true, true),
            listOf(true, true, false)
        ),
        color = BlockColor.SOPH_SAGE,
        name = "S block"
    )

    val standardPool = listOf(
        Piece1x1, Piece2x2, Piece3x3,
        LineH2, LineH3, LineH4,
        LineV2, LineV3, LineV4,
        CornerL, CornerJ,
        ShapeT, ShapeL, ShapeInverseL, ShapeZ, ShapeS
    )

    // Generated on 10x score and 3x3 block only cheats request
    fun getRandomPiece(force3x3: Boolean): BlockPiece {
        if (force3x3) {
            return Piece3x3.copy(id = "cheat_3x3_${System.nanoTime()}")
        }
        val original = standardPool.random()
        return original.copy(id = "${original.id}_${System.nanoTime()}")
    }
}
