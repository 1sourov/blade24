package com.example.ui.components

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.ClickableText
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.core.i18n.bladeStrings
import com.example.ui.theme.BladeCyan
import com.example.ui.theme.TextWhite

@Composable
fun ExpandableVideoCaptionSection(
    videoId: String,
    title: String,
    caption: String,
    textShadow: Shadow,
    onHashtagClick: ((String) -> Unit)? = null,
    testTagPrefix: String = "feed",
    modifier: Modifier = Modifier
) {
    val cleanTitle = title.trim()
    val cleanCaption = caption.trim()
    if (cleanTitle.isBlank() && cleanCaption.isBlank()) return

    val strings = bladeStrings()
    var expanded by rememberSaveable(videoId) { mutableStateOf(false) }
    var titleOverflow by remember(videoId, cleanTitle) { mutableStateOf(false) }
    var captionOverflow by remember(videoId, cleanCaption) { mutableStateOf(false) }

    val canExpand = titleOverflow ||
        captionOverflow ||
        cleanTitle.length > 42 ||
        cleanTitle.contains('\n') ||
        cleanCaption.length > 78 ||
        cleanCaption.contains('\n')

    val containerModifier = if (expanded) {
        modifier
            .fillMaxWidth()
            .animateContentSize()
            .clip(RoundedCornerShape(12.dp))
            .background(Color.Black.copy(alpha = 0.48f))
            .padding(horizontal = 10.dp, vertical = 8.dp)
    } else {
        modifier
            .fillMaxWidth()
            .animateContentSize()
    }

    Column(
        modifier = containerModifier,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        val textContentModifier = if (expanded) {
            Modifier
                .fillMaxWidth()
                .heightIn(max = 200.dp)
                .verticalScroll(rememberScrollState())
        } else {
            Modifier.fillMaxWidth()
        }

        Column(
            modifier = textContentModifier,
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            if (cleanTitle.isNotBlank()) {
                Text(
                    text = cleanTitle,
                    color = TextWhite,
                    style = MaterialTheme.typography.titleSmall.copy(
                        fontSize = 14.5.sp,
                        lineHeight = 19.sp,
                        fontWeight = FontWeight.ExtraBold,
                        shadow = textShadow
                    ),
                    maxLines = if (expanded) Int.MAX_VALUE else if (cleanCaption.isNotBlank()) 1 else 2,
                    overflow = TextOverflow.Ellipsis,
                    onTextLayout = { layoutResult ->
                        if (!expanded && layoutResult.hasVisualOverflow) {
                            titleOverflow = true
                        }
                    }
                )
            }

            if (cleanCaption.isNotBlank()) {
                if (onHashtagClick != null) {
                    val annotatedCaption = remember(cleanCaption, textShadow) {
                        buildAnnotatedString {
                            val words = cleanCaption.split(" ")
                            words.forEachIndexed { idx, word ->
                                if (word.startsWith("#") && word.length > 1) {
                                    pushStringAnnotation(tag = "HASHTAG", annotation = word)
                                    pushStyle(
                                        SpanStyle(
                                            color = BladeCyan,
                                            fontWeight = FontWeight.ExtraBold,
                                            shadow = textShadow
                                        )
                                    )
                                    append(word)
                                    pop()
                                    pop()
                                } else {
                                    pushStyle(
                                        SpanStyle(
                                            color = TextWhite,
                                            fontWeight = FontWeight.Bold,
                                            shadow = textShadow
                                        )
                                    )
                                    append(word)
                                    pop()
                                }
                                if (idx < words.lastIndex) append(" ")
                            }
                        }
                    }
                    @Suppress("DEPRECATION")
                    ClickableText(
                        text = annotatedCaption,
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontSize = 13.5.sp,
                            fontWeight = FontWeight.Bold,
                            lineHeight = 18.sp
                        ),
                        maxLines = if (expanded) Int.MAX_VALUE else 2,
                        overflow = TextOverflow.Ellipsis,
                        onTextLayout = { layoutResult ->
                            if (!expanded && layoutResult.hasVisualOverflow) {
                                captionOverflow = true
                            }
                        },
                        onClick = { offset ->
                            val tag = annotatedCaption
                                .getStringAnnotations(tag = "HASHTAG", start = offset, end = offset)
                                .firstOrNull()
                            if (tag != null) {
                                onHashtagClick(tag.item)
                            } else if (canExpand) {
                                expanded = !expanded
                            }
                        }
                    )
                } else {
                    Text(
                        text = cleanCaption,
                        color = TextWhite,
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontSize = 13.5.sp,
                            fontWeight = FontWeight.Bold,
                            lineHeight = 18.sp,
                            shadow = textShadow
                        ),
                        maxLines = if (expanded) Int.MAX_VALUE else 2,
                        overflow = TextOverflow.Ellipsis,
                        onTextLayout = { layoutResult ->
                            if (!expanded && layoutResult.hasVisualOverflow) {
                                captionOverflow = true
                            }
                        },
                        modifier = Modifier.clickable(enabled = canExpand) {
                            expanded = !expanded
                        }
                    )
                }
            }
        }

        if (canExpand || expanded) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Start,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (expanded) strings.hide else strings.seeMore,
                    color = TextWhite.copy(alpha = 0.92f),
                    style = MaterialTheme.typography.labelMedium.copy(
                        fontWeight = FontWeight.Bold,
                        shadow = textShadow
                    ),
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .clickable { expanded = !expanded }
                        .padding(horizontal = 4.dp, vertical = 4.dp)
                        .defaultMinSize(minHeight = 24.dp)
                        .testTag(
                            if (expanded) "${testTagPrefix}_hide_$videoId"
                            else "${testTagPrefix}_see_more_$videoId"
                        )
                )
            }
        }
    }
}
