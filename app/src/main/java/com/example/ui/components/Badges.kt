package com.example.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MonetizationOn
import androidx.compose.material.icons.filled.Stars
import androidx.compose.material.icons.filled.WorkspacePremium
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.ReelGoldDark
import com.example.ui.theme.ReelGoldLight
import com.example.ui.theme.ReelGoldPrimary
import com.example.ui.theme.ReelRedPrimary

@Composable
fun CoinsBadge(
    coins: Int,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "coin_shimmer")
    val scale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.06f,
        animationSpec = infiniteRepeatable(
            animation = tween(1400, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "coin_scale"
    )

    Row(
        modifier = modifier
            .testTag("coins_badge_button")
            .clip(RoundedCornerShape(20.dp))
            .background(
                brush = Brush.horizontalGradient(
                    listOf(
                        Color(0xFF2E2405),
                        Color(0xFF4A3A0B)
                    )
                )
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 10.dp, vertical = 5.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(5.dp)
    ) {
        Icon(
            imageVector = Icons.Default.MonetizationOn,
            contentDescription = "Coins",
            tint = ReelGoldPrimary,
            modifier = Modifier
                .size(18.dp)
                .scale(scale)
        )
        Text(
            text = "$coins",
            style = MaterialTheme.typography.labelLarge.copy(
                fontWeight = FontWeight.Bold,
                color = ReelGoldLight,
                fontSize = 13.sp
            )
        )
        Box(
            modifier = Modifier
                .clip(CircleShape)
                .background(ReelGoldPrimary)
                .padding(horizontal = 4.dp, vertical = 1.dp)
        ) {
            Text(
                text = "+",
                style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.Black,
                    color = Color.Black,
                    fontSize = 10.sp
                )
            )
        }
    }
}

@Composable
fun VipBadge(
    isVip: Boolean = false,
    onClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .testTag("vip_badge")
            .clip(RoundedCornerShape(16.dp))
            .background(
                brush = if (isVip) {
                    Brush.horizontalGradient(listOf(Color(0xFFFFB300), Color(0xFFFF6F00)))
                } else {
                    Brush.horizontalGradient(listOf(Color(0xFF33202A), Color(0xFF571026)))
                }
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 8.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Icon(
            imageVector = Icons.Default.WorkspacePremium,
            contentDescription = "VIP Status",
            tint = if (isVip) Color.Black else ReelGoldLight,
            modifier = Modifier.size(15.dp)
        )
        Text(
            text = if (isVip) "VIP PRO" else "GET VIP",
            style = MaterialTheme.typography.labelSmall.copy(
                fontWeight = FontWeight.Black,
                color = if (isVip) Color.Black else Color.White,
                fontSize = 11.sp
            )
        )
    }
}

@Composable
fun RankNumberBadge(
    rank: Int,
    modifier: Modifier = Modifier
) {
    val (bgGradient, textColor) = when (rank) {
        1 -> Pair(listOf(Color(0xFFFFD700), Color(0xFFFF8F00)), Color.Black)
        2 -> Pair(listOf(Color(0xFFE0E0E0), Color(0xFF9E9E9E)), Color.Black)
        3 -> Pair(listOf(Color(0xFFCD7F32), Color(0xFF8D5524)), Color.White)
        else -> Pair(listOf(Color(0xFF2B2D42), Color(0xFF1E1F2E)), Color.White)
    }

    Box(
        modifier = modifier
            .size(24.dp)
            .clip(RoundedCornerShape(6.dp))
            .background(Brush.linearGradient(bgGradient)),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "$rank",
            style = MaterialTheme.typography.labelMedium.copy(
                fontWeight = FontWeight.Black,
                color = textColor,
                fontSize = 13.sp
            )
        )
    }
}
