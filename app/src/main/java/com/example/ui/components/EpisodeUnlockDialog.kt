package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.MonetizationOn
import androidx.compose.material.icons.filled.OndemandVideo
import androidx.compose.material.icons.filled.WorkspacePremium
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.model.Drama
import com.example.data.model.Episode
import com.example.ui.theme.ReelBackgroundDark
import com.example.ui.theme.ReelGoldDark
import com.example.ui.theme.ReelGoldLight
import com.example.ui.theme.ReelGoldPrimary
import com.example.ui.theme.ReelRedDark
import com.example.ui.theme.ReelRedPrimary
import com.example.ui.theme.ReelSurfaceDark
import com.example.ui.theme.ReelSurfaceVariantDark
import com.example.ui.theme.ReelTextSecondary

@Composable
fun EpisodeUnlockDialog(
    drama: Drama,
    episode: Episode,
    currentCoins: Int,
    onUnlockWithCoins: () -> Unit,
    onUnlockWithAd: () -> Unit,
    onNavigateToVip: () -> Unit,
    onDismiss: () -> Unit
) {
    val hasEnoughCoins = currentCoins >= episode.coinCost

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .testTag("episode_unlock_dialog"),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = ReelSurfaceDark)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Header with close button
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(Color(0x33FFB800)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Lock,
                            contentDescription = "Locked",
                            tint = ReelGoldPrimary,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = ReelTextSecondary
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = "Unlock Episode ${episode.episodeNumber}",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        fontSize = 18.sp
                    )
                )

                Text(
                    text = drama.title,
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = ReelTextSecondary,
                        fontSize = 12.sp
                    ),
                    textAlign = TextAlign.Center,
                    maxLines = 1
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Coin Balance Card
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(ReelSurfaceVariantDark)
                        .padding(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Your Coin Balance:",
                            style = MaterialTheme.typography.bodySmall.copy(color = ReelTextSecondary)
                        )
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.MonetizationOn,
                                contentDescription = "Coins",
                                tint = ReelGoldPrimary,
                                modifier = Modifier.size(18.dp)
                            )
                            Text(
                                text = "$currentCoins",
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = ReelGoldLight
                                )
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Action 1: Unlock with Coins
                Button(
                    onClick = onUnlockWithCoins,
                    enabled = hasEnoughCoins,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("unlock_coins_button"),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = ReelRedPrimary,
                        disabledContainerColor = Color(0xFF3B2025)
                    )
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.MonetizationOn,
                            contentDescription = null,
                            tint = if (hasEnoughCoins) ReelGoldLight else Color.Gray,
                            modifier = Modifier.size(18.dp)
                        )
                        Text(
                            text = if (hasEnoughCoins) "Unlock for ${episode.coinCost} Coins" else "Need ${episode.coinCost} Coins (Insufficient)",
                            style = MaterialTheme.typography.labelLarge.copy(
                                fontWeight = FontWeight.Bold,
                                color = if (hasEnoughCoins) Color.White else Color.Gray
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Action 2: Watch Ad for Free Unlock
                OutlinedButton(
                    onClick = onUnlockWithAd,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(46.dp)
                        .testTag("unlock_ad_button"),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.OndemandVideo,
                            contentDescription = null,
                            tint = ReelGoldPrimary,
                            modifier = Modifier.size(18.dp)
                        )
                        Text(
                            text = "Watch 1 Ad to Unlock Free",
                            style = MaterialTheme.typography.labelLarge.copy(
                                fontWeight = FontWeight.SemiBold,
                                color = Color.White
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // VIP Pass Banner
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(
                            brush = Brush.horizontalGradient(
                                listOf(Color(0xFF3D2700), Color(0xFF6B1D0E))
                            )
                        )
                        .clickable(onClick = onNavigateToVip)
                        .padding(10.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.WorkspacePremium,
                                contentDescription = "VIP",
                                tint = ReelGoldPrimary,
                                modifier = Modifier.size(22.dp)
                            )
                            Column {
                                Text(
                                    text = "ReelShort VIP Pass",
                                    style = MaterialTheme.typography.labelMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = ReelGoldLight
                                    )
                                )
                                Text(
                                    text = "Unlock ALL series unlimited",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        color = Color(0xCCFFFFFF),
                                        fontSize = 10.sp
                                    )
                                )
                            }
                        }

                        Text(
                            text = "JOIN >",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Black,
                                color = ReelGoldPrimary
                            )
                        )
                    }
                }
            }
        }
    }
}
