package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.viewmodel.GameViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsCheatDialog(
    viewModel: GameViewModel,
    onDismiss: () -> Unit
) {
    val cheat10x by viewModel.cheat10xScore.collectAsState()
    val cheat3x3 by viewModel.cheat3x3Only.collectAsState()

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .border(
                    BorderStroke(1.5.dp, Color(0xFF49454F)),
                    shape = RoundedCornerShape(24.dp)
                ),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF2B2930))
        ) {
            Column(
                modifier = Modifier
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                Color(0xFF2B2930),
                                Color(0xFF1C1B1F)
                            )
                        )
                    )
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Title header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "⚙️ SETTINGS & CHEATS",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Black,
                        color = Color.White
                    )

                    IconButton(
                        onClick = onDismiss,
                        colors = IconButtonDefaults.iconButtonColors(
                            containerColor = Color(0x1AFFFFFF),
                            contentColor = Color.White
                        ),
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close settings dialog",
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // How-To/About Box
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF1C1B1F)),
                    border = BorderStroke(1.dp, Color(0xFF49454F))
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = "Info icon",
                            tint = Color(0xFFD0BCFF),
                            modifier = Modifier.size(20.dp)
                        )
                        Column {
                            Text(
                                text = "Blast Combo Rules",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Text(
                                text = "Clear columns & rows at the same time to release particle chain explosions and trigger deep screenshakes! Consecutive line clears multiply your streak score.",
                                fontSize = 10.sp,
                                color = Color(0xFFB0B0C4),
                                lineHeight = 14.sp
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                Divider(color = Color(0xFF49454F))

                Spacer(modifier = Modifier.height(16.dp))

                // Development Cheat Title
                Text(
                    text = "ARCADE INTEGRATIONS & CHEATS",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Black,
                    color = Color(0xFFEFB8C8),
                    letterSpacing = 1.sp,
                    modifier = Modifier.align(Alignment.Start)
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Cheat Item 1: 10x Score multiplier
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color(0xFF1C1B1F))
                        .border(
                            BorderStroke(1.dp, if (cheat10x) Color(0xFFD0BCFF) else Color(0xFF49454F).copy(alpha = 0.5f)),
                            RoundedCornerShape(16.dp)
                        )
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "10x Score Multiplier",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (cheat10x) Color(0xFFD0BCFF) else Color(0xFFE6E1E5)
                        )
                        Text(
                            text = "Massive scoring for every combo placement and clearing.",
                            fontSize = 11.sp,
                            color = Color(0xFFCCC2DC),
                            lineHeight = 14.sp
                        )
                    }

                    Switch(
                        checked = cheat10x,
                        onCheckedChange = { viewModel.toggleCheat10xScore() },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color(0xFF381E72),
                            checkedTrackColor = Color(0xFFD0BCFF),
                            uncheckedThumbColor = Color(0xFF938F99),
                            uncheckedTrackColor = Color(0xFF49454F)
                        )
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Cheat Item 2: 3x3 solid blocks generation only
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color(0xFF1C1B1F))
                        .border(
                            BorderStroke(1.dp, if (cheat3x3) Color(0xFFEFB8C8) else Color(0xFF49454F).copy(alpha = 0.5f)),
                            RoundedCornerShape(16.dp)
                        )
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "3x3 Blocks Only",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (cheat3x3) Color(0xFFEFB8C8) else Color(0xFFE6E1E5)
                        )
                        Text(
                            text = "Spawn easiest pieces exclusively under generators.",
                            fontSize = 11.sp,
                            color = Color(0xFFCCC2DC),
                            lineHeight = 14.sp
                        )
                    }

                    Switch(
                        checked = cheat3x3,
                        onCheckedChange = { viewModel.toggleCheat3x3Only() },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color(0xFF381E72),
                            checkedTrackColor = Color(0xFFD0BCFF),
                            uncheckedThumbColor = Color(0xFF938F99),
                            uncheckedTrackColor = Color(0xFF49454F)
                        )
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Dismiss Okay Button
                Button(
                    onClick = onDismiss,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD0BCFF)),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    contentPadding = PaddingValues(12.dp)
                ) {
                    Text(
                        text = "BACK TO ACTION 💥",
                        color = Color(0xFF381E72),
                        fontWeight = FontWeight.Black,
                        fontSize = 14.sp
                    )
                }
            }
        }
    }
}
