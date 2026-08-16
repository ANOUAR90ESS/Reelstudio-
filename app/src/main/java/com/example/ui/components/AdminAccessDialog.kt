package com.example.ui.components

import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.ui.theme.ReelGoldPrimary
import com.example.ui.theme.ReelRedPrimary
import com.example.ui.theme.ReelSurfaceDark
import com.example.ui.theme.ReelTextSecondary
import com.example.ui.theme.ReelTextTertiary

/**
 * Gate in front of the admin console.
 *
 * A signed-in admin never sees the passcode field — they are already identified. It only appears
 * for the offline path, where Firebase is not configured and there is no account to check.
 */
@Composable
fun AdminAccessDialog(
    isOpen: Boolean,
    isSignedInAdmin: Boolean,
    onDismiss: () -> Unit,
    onEnterConsole: () -> Unit,
    onSubmitPasscode: (String) -> Unit,
    errorMessage: String? = null
) {
    if (!isOpen) return

    var passcode by remember { mutableStateOf("") }

    Dialog(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(20.dp))
                .background(ReelSurfaceDark)
                .padding(20.dp)
                .testTag("admin_access_dialog")
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(ReelGoldPrimary.copy(alpha = 0.18f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Shield,
                        contentDescription = null,
                        tint = ReelGoldPrimary,
                        modifier = Modifier.size(22.dp)
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = "Admin Console",
                        style = MaterialTheme.typography.titleMedium.copy(
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                    )
                    Text(
                        text = "Create and publish films and episodes",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = ReelTextTertiary,
                            fontSize = 11.sp
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (isSignedInAdmin) {
                Text(
                    text = "Your account has the admin role. Everything you publish here becomes " +
                            "visible to every viewer of the app.",
                    style = MaterialTheme.typography.bodySmall.copy(color = ReelTextSecondary)
                )
            } else {
                Text(
                    text = "This account is not an admin. If you own this project, sign in with an " +
                            "admin account — or enter the local console passcode to author content " +
                            "on this device only.",
                    style = MaterialTheme.typography.bodySmall.copy(color = ReelTextSecondary)
                )

                Spacer(modifier = Modifier.height(12.dp))

                AdminTextField(
                    value = passcode,
                    onValueChange = { passcode = it },
                    label = "Console passcode",
                    isPassword = true,
                    imeAction = ImeAction.Done,
                    testTag = "admin_passcode_field"
                )

                if (errorMessage != null) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = errorMessage,
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = MaterialTheme.colorScheme.error
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                TextButton(
                    onClick = onDismiss,
                    modifier = Modifier.weight(1f)
                ) {
                    Text(text = "Cancel", color = ReelTextSecondary)
                }

                Button(
                    onClick = {
                        if (isSignedInAdmin) onEnterConsole() else onSubmitPasscode(passcode)
                    },
                    modifier = Modifier
                        .weight(2f)
                        .testTag("admin_access_confirm_button"),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = ReelRedPrimary,
                        contentColor = Color.White
                    )
                ) {
                    Text(
                        text = if (isSignedInAdmin) "Open Console" else "Unlock",
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}
