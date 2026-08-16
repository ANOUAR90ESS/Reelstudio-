package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.EventAvailable
import androidx.compose.material.icons.filled.MonetizationOn
import androidx.compose.material.icons.filled.Redeem
import androidx.compose.material.icons.filled.Stars
import androidx.compose.material.icons.filled.WorkspacePremium
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
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
import com.example.data.local.UserAccountEntity
import com.example.data.model.CoinPackage
import com.example.data.model.DailyTask
import com.example.data.model.SampleDramas
import com.example.ui.theme.ReelBackgroundDark
import com.example.ui.theme.ReelGoldDark
import com.example.ui.theme.ReelGoldLight
import com.example.ui.theme.ReelGoldPrimary
import com.example.ui.theme.ReelRedDark
import com.example.ui.theme.ReelRedPrimary
import com.example.ui.theme.ReelSurfaceDark
import com.example.ui.theme.ReelSurfaceVariantDark
import com.example.ui.theme.ReelTextSecondary
import com.example.ui.theme.ReelTextTertiary
import java.util.concurrent.TimeUnit

@Composable
fun RewardsScreen(
    userAccount: UserAccountEntity,
    dailyTasks: List<DailyTask>,
    onCheckIn: () -> Unit,
    onClaimTask: (String) -> Unit,
    onPurchasePack: (CoinPackage) -> Unit,
    onSubscribeVip: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isCheckedInToday = userAccount.lastCheckinDateEpochDay == TimeUnit.MILLISECONDS.toDays(System.currentTimeMillis())

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("rewards_screen")
            .background(ReelBackgroundDark),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp)
    ) {
        // --- 1. User Balance & VIP Pass Card ---
        item {
            BalanceAndVipCard(
                userAccount = userAccount,
                onSubscribeVip = onSubscribeVip
            )
        }

        // --- 2. 7-Day Check-in Streak Section ---
        item {
            DailyCheckInCalendar(
                streak = userAccount.checkinStreak,
                isCheckedInToday = isCheckedInToday,
                onCheckIn = onCheckIn
            )
        }

        // --- 3. Daily Task Center ---
        item {
            DailyTaskCenter(
                tasks = dailyTasks,
                onClaimTask = onClaimTask
            )
        }

        // --- 4. Coin Store Bundles ---
        item {
            CoinStoreSection(
                packages = SampleDramas.coinStorePackages,
                onPurchasePack = onPurchasePack
            )
        }
    }
}

@Composable
private fun BalanceAndVipCard(
    userAccount: UserAccountEntity,
    onSubscribeVip: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("rewards_balance_card"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = ReelSurfaceDark)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Total Coin Balance",
                        style = MaterialTheme.typography.bodySmall.copy(color = ReelTextSecondary)
                    )
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.MonetizationOn,
                            contentDescription = "Coins",
                            tint = ReelGoldPrimary,
                            modifier = Modifier.size(28.dp)
                        )
                        Text(
                            text = "${userAccount.coins}",
                            style = MaterialTheme.typography.headlineMedium.copy(
                                fontWeight = FontWeight.Black,
                                color = Color.White
                            )
                        )
                    }
                }

                if (userAccount.isVip) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .background(
                                Brush.horizontalGradient(
                                    listOf(ReelGoldPrimary, ReelGoldDark)
                                )
                            )
                            .padding(horizontal = 14.dp, vertical = 6.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.WorkspacePremium,
                                contentDescription = "VIP",
                                tint = Color.Black,
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = "VIP ACTIVE",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Black,
                                    color = Color.Black
                                )
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // VIP Upsell Banner
            if (!userAccount.isVip) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(
                            Brush.horizontalGradient(
                                listOf(Color(0xFF382305), Color(0xFF6B1D0E))
                            )
                        )
                        .clickable(onClick = onSubscribeVip)
                        .padding(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.WorkspacePremium,
                                contentDescription = "VIP",
                                tint = ReelGoldPrimary,
                                modifier = Modifier.size(28.dp)
                            )
                            Column {
                                Text(
                                    text = "ReelShort VIP Pass",
                                    style = MaterialTheme.typography.titleSmall.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = ReelGoldLight
                                    )
                                )
                                Text(
                                    text = "Unlimited episodes • Ad-free 1080p",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        color = Color(0xDDFFFFFF),
                                        fontSize = 11.sp
                                    )
                                )
                            }
                        }

                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(16.dp))
                                .background(ReelGoldPrimary)
                                .padding(horizontal = 10.dp, vertical = 5.dp)
                        ) {
                            Text(
                                text = "UPGRADE",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Black,
                                    color = Color.Black
                                )
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun DailyCheckInCalendar(
    streak: Int,
    isCheckedInToday: Boolean,
    onCheckIn: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("rewards_checkin_card"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = ReelSurfaceDark)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.EventAvailable,
                        contentDescription = null,
                        tint = ReelRedPrimary
                    )
                    Text(
                        text = "7-Day Check-in Streak",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    )
                }

                Text(
                    text = "Day $streak/7",
                    style = MaterialTheme.typography.labelMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = ReelGoldPrimary
                    )
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // 7 Days Grid
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                val dayBonuses = listOf(50, 60, 80, 100, 120, 150, 300)
                (1..7).forEach { dayNum ->
                    val isPast = dayNum < streak || (dayNum == streak && isCheckedInToday)
                    val isToday = dayNum == streak && !isCheckedInToday

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(8.dp))
                            .background(
                                when {
                                    isPast -> Color(0xFF1E3A24)
                                    isToday -> ReelRedDark
                                    else -> ReelSurfaceVariantDark
                                }
                            )
                            .then(
                                if (isToday) Modifier.border(1.dp, ReelRedPrimary, RoundedCornerShape(8.dp))
                                else Modifier
                            )
                            .padding(vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "Day $dayNum",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontSize = 9.sp,
                                    color = if (isPast || isToday) Color.White else ReelTextTertiary
                                )
                            )
                            Spacer(modifier = Modifier.height(3.dp))
                            if (isPast) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = "Done",
                                    tint = Color(0xFF4CAF50),
                                    modifier = Modifier.size(16.dp)
                                )
                            } else {
                                Icon(
                                    imageVector = if (dayNum == 7) Icons.Default.Redeem else Icons.Default.MonetizationOn,
                                    contentDescription = "Coins",
                                    tint = ReelGoldPrimary,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "+${dayBonuses[dayNum - 1]}",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 10.sp,
                                    color = if (isPast) Color(0xFF81C784) else ReelGoldLight
                                )
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            Button(
                onClick = onCheckIn,
                enabled = !isCheckedInToday,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(44.dp)
                    .testTag("checkin_button"),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = ReelRedPrimary,
                    disabledContainerColor = Color(0xFF2C2D3A)
                )
            ) {
                Text(
                    text = if (isCheckedInToday) "Checked in Today! Come back tomorrow" else "Claim Today's Bonus Coins",
                    style = MaterialTheme.typography.labelLarge.copy(
                        fontWeight = FontWeight.Bold,
                        color = if (isCheckedInToday) ReelTextTertiary else Color.White
                    )
                )
            }
        }
    }
}

@Composable
private fun DailyTaskCenter(
    tasks: List<DailyTask>,
    onClaimTask: (String) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("rewards_tasks_card"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = ReelSurfaceDark)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Daily Rewards Tasks",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            )

            Spacer(modifier = Modifier.height(12.dp))

            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                tasks.forEach { task ->
                    DailyTaskRow(task = task, onClaim = { onClaimTask(task.id) })
                }
            }
        }
    }
}

@Composable
private fun DailyTaskRow(
    task: DailyTask,
    onClaim: () -> Unit
) {
    val isCompleted = task.currentCount >= task.targetCount

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(
                    text = task.title,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                )
                Text(
                    text = "+${task.rewardCoins} Coins",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.Bold,
                        color = ReelGoldPrimary
                    )
                )
            }

            Text(
                text = task.description,
                style = MaterialTheme.typography.bodySmall.copy(
                    color = ReelTextSecondary,
                    fontSize = 11.sp
                )
            )

            Spacer(modifier = Modifier.height(4.dp))

            LinearProgressIndicator(
                progress = { (task.currentCount.toFloat() / task.targetCount.toFloat()).coerceIn(0f, 1f) },
                modifier = Modifier
                    .fillMaxWidth(0.8f)
                    .height(4.dp)
                    .clip(RoundedCornerShape(2.dp)),
                color = ReelRedPrimary,
                trackColor = ReelSurfaceVariantDark
            )
        }

        Button(
            onClick = onClaim,
            enabled = isCompleted && !task.isClaimed,
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = ReelRedPrimary,
                disabledContainerColor = if (task.isClaimed) Color(0xFF1E3A24) else Color(0xFF2C2D3A)
            ),
            contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp)
        ) {
            Text(
                text = when {
                    task.isClaimed -> "Claimed"
                    isCompleted -> "Claim"
                    else -> "${task.currentCount}/${task.targetCount}"
                },
                style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.Bold,
                    color = if (task.isClaimed) Color(0xFF81C784) else if (isCompleted) Color.White else ReelTextTertiary
                )
            )
        }
    }
}

@Composable
private fun CoinStoreSection(
    packages: List<CoinPackage>,
    onPurchasePack: (CoinPackage) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("rewards_store_card"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = ReelSurfaceDark)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Recharge Coins Store",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            )
            Text(
                text = "Instant coins for continuous binge-watching",
                style = MaterialTheme.typography.bodySmall.copy(
                    color = ReelTextSecondary,
                    fontSize = 11.sp
                )
            )

            Spacer(modifier = Modifier.height(12.dp))

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                packages.forEach { pack ->
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(ReelSurfaceVariantDark)
                            .then(
                                if (pack.isPopular) Modifier.border(1.5.dp, ReelGoldPrimary, RoundedCornerShape(12.dp))
                                else Modifier
                            )
                            .clickable { onPurchasePack(pack) }
                            .padding(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.MonetizationOn,
                                    contentDescription = "Coins",
                                    tint = ReelGoldPrimary,
                                    modifier = Modifier.size(24.dp)
                                )
                                Column {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        Text(
                                            text = "${pack.coins} Coins",
                                            style = MaterialTheme.typography.bodyMedium.copy(
                                                fontWeight = FontWeight.Bold,
                                                color = Color.White
                                            )
                                        )
                                        if (pack.bonusCoins > 0) {
                                            Text(
                                                text = "+${pack.bonusCoins} Bonus",
                                                style = MaterialTheme.typography.labelSmall.copy(
                                                    fontWeight = FontWeight.Bold,
                                                    color = ReelGoldLight
                                                )
                                            )
                                        }
                                    }

                                    if (pack.tag != null) {
                                        Text(
                                            text = pack.tag,
                                            style = MaterialTheme.typography.labelSmall.copy(
                                                color = ReelRedPrimary,
                                                fontWeight = FontWeight.Black,
                                                fontSize = 10.sp
                                            )
                                        )
                                    }
                                }
                            }

                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(if (pack.isPopular) ReelGoldPrimary else ReelRedPrimary)
                                    .padding(horizontal = 14.dp, vertical = 6.dp)
                            ) {
                                Text(
                                    text = pack.priceUsd,
                                    style = MaterialTheme.typography.labelMedium.copy(
                                        fontWeight = FontWeight.Black,
                                        color = if (pack.isPopular) Color.Black else Color.White
                                    )
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
