package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.entity.CategoryEntity
import com.example.ui.theme.CleanMinBlueBg
import com.example.ui.theme.CleanMinOutline
import com.example.ui.theme.CleanMinPinkBg
import com.example.ui.theme.CleanMinPurpleBg
import com.example.ui.theme.CleanMinSecondaryContainer

@Composable
fun CategoryCard(
    category: CategoryEntity,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val (emoji, bgTint) = when (category.id) {
        "bible" -> "📖" to CleanMinPurpleBg
        "afrique" -> "🌍" to CleanMinPinkBg
        "rdc" -> "🇨🇩" to CleanMinBlueBg
        "culture_generale" -> "💡" to CleanMinSecondaryContainer
        "science" -> "🔬" to CleanMinSecondaryContainer
        "histoire" -> "🏛️" to CleanMinPinkBg
        "geographie" -> "🧭" to CleanMinBlueBg
        "technologie" -> "💻" to CleanMinPurpleBg
        "sport" -> "⚽" to CleanMinPinkBg
        "musique" -> "🎵" to CleanMinPurpleBg
        "litterature" -> "📚" to CleanMinSecondaryContainer
        "logique" -> "🧩" to CleanMinPinkBg
        else -> "✨" to CleanMinSecondaryContainer
    }

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        modifier = modifier
            .fillMaxWidth()
            .border(1.dp, CleanMinOutline, RoundedCornerShape(16.dp))
            .clip(RoundedCornerShape(16.dp))
            .clickable { onClick() }
            .testTag("category_card_${category.id}")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(bgTint),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = emoji,
                    fontSize = 20.sp
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = category.name,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                ),
                maxLines = 1
            )

            Spacer(modifier = Modifier.height(2.dp))

            Text(
                text = if (category.questionCount > 0) "${category.questionCount} Questions" else "Questions variées",
                style = MaterialTheme.typography.bodySmall.copy(
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 12.sp
                ),
                maxLines = 1
            )
        }
    }
}
