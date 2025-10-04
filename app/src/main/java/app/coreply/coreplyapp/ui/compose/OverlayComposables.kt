/**
 * coreply
 *
 * Copyright (C) 2024 coreply
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

package app.coreply.coreplyapp.ui.compose

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.Typography
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Main inline suggestion overlay that appears over the text input field
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun InlineSuggestionOverlay(
    text: String,
    textSize: Float,
    showBackground: Boolean,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .wrapContentWidth(if (showBackground) { Alignment.End } else { Alignment.Start })
            .wrapContentHeight(if (showBackground) { Alignment.CenterVertically } else { Alignment.Bottom })
            .combinedClickable(
                onClick = onClick,
                onLongClick = onLongClick,
                onClickLabel = "Insert first word",
                onLongClickLabel = "Insert full suggestion"
            )
            .then(
                if (showBackground) {
                    Modifier
                        .background(
                            color = MaterialTheme.colorScheme.secondaryContainer,
                            shape = RoundedCornerShape(50.dp)
                        )
                        .padding(horizontal = 8.dp)
                } else {
                    Modifier.background(Color.Transparent)
                }
            ),
        contentAlignment = if (showBackground) {Alignment.Center} else {Alignment.BottomStart}
    ) {
        Text(
            text = text,
            fontSize = textSize.sp,
            color = if (showBackground)
                MaterialTheme.colorScheme.onSecondaryContainer
            else
                Color(0xEE999999), // A color that fits both light and dark backgrounds
            style = Typography().bodyMedium,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,

        )

    }
}

/**
 * Trailing suggestion overlay that appears below the text input field
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun TrailingSuggestionOverlay(
    text: String,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    isExpanded: Boolean,
    onExpand: () -> Unit,
    modifier: Modifier = Modifier
) {
    val chevronRotation by animateFloatAsState(targetValue = if (isExpanded) 180f else 0f)

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp)
            .then(
                if (isExpanded) {
                    Modifier.wrapContentHeight(Alignment.Top)
                } else {
                    Modifier.height(20.dp)
                }
            )
            .combinedClickable(
                onClick = onClick,
                onLongClick = onLongClick,
                onClickLabel = "Insert first word",
                onLongClickLabel = "Insert full suggestion"
            ),
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.secondaryContainer,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding( horizontal = 10.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = text,
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSecondaryContainer,
                style = Typography().bodyMedium,
                textAlign = TextAlign.Start,
                maxLines = if (isExpanded) Int.MAX_VALUE else 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Icon(
                imageVector = Icons.Default.KeyboardArrowDown,
                contentDescription = "Expand",
                tint = MaterialTheme.colorScheme.onSecondaryContainer,
                modifier = Modifier
                    .rotate(chevronRotation)
                    .combinedClickable(
                        onClick = onExpand,
                        onLongClick = onExpand,
                        indication = null,
                        interactionSource = remember { MutableInteractionSource() },
                    )
            )
        }
    }
}
