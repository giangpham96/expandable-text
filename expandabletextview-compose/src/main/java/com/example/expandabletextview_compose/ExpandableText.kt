package com.example.expandabletextview_compose

import android.accessibilityservice.AccessibilityServiceInfo.FEEDBACK_SPOKEN
import android.content.Context
import android.view.accessibility.AccessibilityManager
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationSpec
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Indication
import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.LocalTextStyle
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.SubcomposeLayout
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp

@Composable
fun ExpandableText(
    originalText: String,
    expandAction: String,
    modifier: Modifier = Modifier,
    expand: Boolean = false,
    onClick: () -> Unit = {},
    expandActionColor: Color = Color.Unspecified,
    limitedMaxLines: Int = 3,
    color: Color = Color.Unspecified,
    fontSize: TextUnit = TextUnit.Unspecified,
    fontStyle: FontStyle? = null,
    fontWeight: FontWeight? = null,
    fontFamily: FontFamily? = null,
    letterSpacing: TextUnit = TextUnit.Unspecified,
    textDecoration: TextDecoration? = null,
    lineHeight: TextUnit = TextUnit.Unspecified,
    softWrap: Boolean = true,
    style: TextStyle = LocalTextStyle.current,
    indication: Indication = LocalIndication.current,
    interactionSource: MutableInteractionSource = remember { MutableInteractionSource() },
    animationSpec: AnimationSpec<Float> = spring(),
) {
    val talkbackEnabled by LocalContext.current.collectIsAccessibilityServiceEnabled()
    val animatableHeight = remember { Animatable(0f) }
    var collapsedText by remember { mutableStateOf(AnnotatedString("")) }
    var collapsedHeight by remember { mutableStateOf(0f) }
    var expandedHeight by remember { mutableStateOf(0f) }
    // internalExpand == expand means it's the first composition, thus, no animation needed
    var internalExpand by remember { mutableStateOf(expand) }
    var displayedText by remember { mutableStateOf(AnnotatedString(originalText)) }
    var displayedLines by remember { mutableStateOf(limitedMaxLines) }
    var invalidate by remember { mutableStateOf(true) }
    LaunchedEffect(expand, collapsedHeight, expandedHeight, collapsedText) {
        if (internalExpand != expand) {
            displayedText = AnnotatedString(originalText)
            displayedLines = Int.MAX_VALUE
            animatableHeight.animateTo(
                if (expand) expandedHeight else collapsedHeight,
                animationSpec = animationSpec
            )
            internalExpand = expand
        } else {
            animatableHeight.snapTo(if (expand) expandedHeight else collapsedHeight)
        }
        displayedText = if (expand) AnnotatedString(originalText) else collapsedText
        displayedLines = if (expand) Int.MAX_VALUE else limitedMaxLines
    }
    LaunchedEffect(
        originalText,
        expandAction,
        fontSize,
        fontStyle,
        fontFamily,
        letterSpacing,
        textDecoration,
        style,
        modifier,
        limitedMaxLines,
    ) {
        invalidate = true
    }

    ExpandableTextLayout(
        expandAction = {
            Text(
                text = "… $expandAction",
                color = color,
                fontSize = fontSize,
                fontStyle = fontStyle,
                fontWeight = fontWeight,
                fontFamily = fontFamily,
                letterSpacing = letterSpacing,
                textDecoration = textDecoration,
                lineHeight = lineHeight,
                maxLines = 1,
                softWrap = softWrap,
                style = style,
            )
        },
        expandedText = {
            Text(
                text = originalText,
                color = color,
                fontSize = fontSize,
                fontStyle = fontStyle,
                fontWeight = fontWeight,
                fontFamily = fontFamily,
                letterSpacing = letterSpacing,
                textDecoration = textDecoration,
                lineHeight = lineHeight,
                softWrap = softWrap,
                style = style,
            )
        },
        collapsedText = { expandActionWidth ->
            Text(
                text = originalText,
                color = color,
                fontSize = fontSize,
                fontStyle = fontStyle,
                fontWeight = fontWeight,
                fontFamily = fontFamily,
                letterSpacing = letterSpacing,
                textDecoration = textDecoration,
                lineHeight = lineHeight,
                maxLines = limitedMaxLines,
                softWrap = softWrap,
                style = style,
                onTextLayout = { result ->
                    val lastLine = result.lineCount - 1
                    val lastCharacterIndex = result.getLineEnd(lastLine)
                    if (lastCharacterIndex == originalText.length) {
                        collapsedText = AnnotatedString(originalText)
                    } else {
                        var lastCharIndex = result.getLineEnd(lastLine, true) + 1
                        var charRect: Rect
                        do {
                            lastCharIndex -= 1
                            charRect = result.getCursorRect(lastCharIndex)
                        } while (charRect.right > result.size.width - expandActionWidth)
                        val cutText = originalText
                            .substring(startIndex = 0, endIndex = lastCharIndex)
                            .dropLastWhile { it.isWhitespace() }
                        collapsedText = buildAnnotatedString {
                            append(cutText)
                            append('…')
                            append(' ')
                            withStyle(SpanStyle(color = expandActionColor)) {
                                append(expandAction)
                            }
                        }
                    }
                }
            )
        },
        displayedText = {
            Text(
                text = if (expand == internalExpand) {
                    if (expand) AnnotatedString(originalText) else collapsedText
                } else {
                    displayedText
                },
                modifier = Modifier.height(
                    with(LocalDensity.current) {
                        if (expand == internalExpand) {
                            if (expand) expandedHeight.toDp() else collapsedHeight.toDp()
                        } else {
                            animatableHeight.value.toDp()
                        }
                    }
                ),
                color = color,
                fontSize = fontSize,
                fontStyle = fontStyle,
                fontWeight = fontWeight,
                fontFamily = fontFamily,
                letterSpacing = letterSpacing,
                textDecoration = textDecoration,
                lineHeight = lineHeight,
                softWrap = softWrap,
                style = style,
                maxLines = if (expand == internalExpand) {
                    if (expand) Int.MAX_VALUE else limitedMaxLines
                } else {
                    displayedLines
                },
            )
        },
        invalidate = invalidate,
        onMeasured = { measurements ->
            collapsedHeight = measurements.collapsedHeight.toFloat()
            expandedHeight = measurements.expandedHeight.toFloat()
            invalidate = false
        },
        modifier = Modifier
            .clickable(
                enabled = !talkbackEnabled && collapsedText.text != originalText,
                onClick = onClick,
                indication = indication,
                interactionSource = interactionSource,
            )
            .then(modifier)
    )
}

@Composable
private fun ExpandableTextLayout(
    expandAction: @Composable () -> Unit,
    expandedText: @Composable () -> Unit,
    collapsedText: @Composable (Int) -> Unit,
    displayedText: @Composable () -> Unit,
    invalidate: Boolean,
    onMeasured: (ExpandableTextHeightMeasurements) -> Unit,
    modifier: Modifier = Modifier,
) {
    SubcomposeLayout(
        modifier = modifier
    ) { constraints ->
        if (invalidate) {
            val expandActionWidth =
                subcompose(slotId = "ExpandAction", content = expandAction)
                    .first()
                    .measure(constraints).width
            val measuredTexts = subcompose(slotId = "MeasuredTexts") {
                collapsedText(expandActionWidth)
                expandedText()
            }
            val collapsedHeight = measuredTexts
                .first()
                .measure(constraints).height
            val expandedHeight = measuredTexts
                .last()
                .measure(constraints).height
            onMeasured(
                ExpandableTextHeightMeasurements(
                    collapsedHeight = collapsedHeight,
                    expandedHeight = expandedHeight
                )
            )
        }
        val placeable = subcompose(slotId = "DisplayedText", content = displayedText)
            .first()
            .measure(constraints)

        layout(placeable.width, placeable.height) {
            placeable.place(0, 0)
        }
    }
}

private data class ExpandableTextHeightMeasurements(
    val collapsedHeight: Int,
    val expandedHeight: Int,
)

@Composable
internal fun Context.collectIsAccessibilityServiceEnabled(): State<Boolean> {
    val accessibilityManager =
        this.getSystemService(Context.ACCESSIBILITY_SERVICE) as? AccessibilityManager?

    fun isAccessibilityServiceEnabled(): Boolean {
        val accessibilityServiceInfoList =
            accessibilityManager?.getEnabledAccessibilityServiceList(FEEDBACK_SPOKEN)
        return accessibilityServiceInfoList.orEmpty().isNotEmpty()
    }

    val accessibilityServiceEnabled = remember { mutableStateOf(isAccessibilityServiceEnabled()) }
    val accessibilityManagerEnabled = accessibilityManager?.isEnabled ?: false
    var accessibilityEnabled by remember { mutableStateOf(accessibilityManagerEnabled) }

    accessibilityManager?.addAccessibilityStateChangeListener { accessibilityEnabled = it }

    LaunchedEffect(accessibilityEnabled) {
        accessibilityServiceEnabled.value = accessibilityEnabled && isAccessibilityServiceEnabled()
    }
    return accessibilityServiceEnabled
}

@Preview(showBackground = true, heightDp = 700, backgroundColor = 0xffffff)
@Composable
private fun Preview() = Box {
    var expand by remember { mutableStateOf(false) }
    ExpandableText(
        modifier = Modifier
            .background(Color.Gray)
            .padding(16.dp),
        originalText = "Lorem ipsum dolor sit amet, consectetur adipiscing elit. Cras fermentum lectus vitae massa venenatis commodo. Nam quis felis commodo mauris congue pretium sit amet non magna. Class aptent taciti sociosqu ad litora torquent per conubia nostra, per inceptos himenaeos. Maecenas sed neque augue. Sed placerat, quam sodales congue luctus, orci ipsum viverra nisl, ac aliquet urna ipsum sed dui. Sed rutrum sollicitudin lacinia. Sed bibendum velit id nisi vulputate egestas. Nunc molestie arcu ante, vel consectetur lorem pulvinar et. Ut non mauris orci. Orci varius natoque penatibus et magnis dis parturient montes, nascetur ridiculus mus. Morbi consectetur lobortis consequat. Sed semper vestibulum arcu ut sodales.",
        expandAction = "See more",
        expand = expand,
        expandActionColor = Color.Blue,
        onClick = {
            expand = !expand
        }
    )
}
