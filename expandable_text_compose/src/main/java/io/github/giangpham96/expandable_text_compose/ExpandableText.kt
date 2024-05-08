package io.github.giangpham96.expandable_text_compose

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationSpec
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.LocalContentColor
import androidx.compose.material.LocalTextStyle
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.ExperimentalTextApi
import androidx.compose.ui.text.Paragraph
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.ResolvedTextDirection.Ltr
import androidx.compose.ui.text.style.ResolvedTextDirection.Rtl
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.LayoutDirection.Rtl as RTL
import androidx.compose.ui.unit.LayoutDirection.Ltr as LTR
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp

/**
 * @param originalText The text that might be truncated/collapsed if too long
 * @param expandAction The text that is appended at the end of the collapsed text
 * @param collapseAction The text that is appended at the end of the expanded text
 * @param expand Whether the text should be expanded or not. Default to false
 * @param actionColor The color of [expandAction] and [collapseAction]
 * @param actionStyle The style of [expandAction] and [collapseAction]
 * @param actionDecoration The decoration of [expandAction] and [collapseAction]
 * @param limitedMaxLines The number of lines displayed when the text collapses
 * @param modifier [Modifier] to apply to this layout node.
 * @param color [Color] to apply to the text. If [Color.Unspecified], and [style] has no color set,
 * this will be [LocalContentColor].
 * @param fontSize The size of glyphs to use when painting the text. See [TextStyle.fontSize].
 * @param fontStyle The typeface variant to use when drawing the letters (e.g., italic).
 * See [TextStyle.fontStyle].
 * @param fontWeight The typeface thickness to use when painting the text (e.g., [FontWeight.Bold]).
 * @param fontFamily The font family to be used when rendering the text. See [TextStyle.fontFamily].
 * @param letterSpacing The amount of space to add between each letter.
 * See [TextStyle.letterSpacing].
 * @param textDecoration The decorations to paint on the text (e.g., an underline).
 * See [TextStyle.textDecoration].
 * @param lineHeight Line height for the [Paragraph] in [TextUnit] unit, e.g. SP or EM.
 * See [TextStyle.lineHeight].
 * @param softWrap Whether the text should break at soft line breaks. If false, the glyphs in the
 * text will be positioned as if there was unlimited horizontal space. If [softWrap] is false,
 * @param style Style configuration for the text such as color, font, line height etc.
 */
@OptIn(ExperimentalTextApi::class)
@Composable
fun ExpandableText(
    originalText: String,
    expandAction: String,
    modifier: Modifier = Modifier,
    collapseAction: String? = null,
    expand: Boolean = false,
    actionColor: Color = Color.Unspecified,
    actionStyle: TextStyle = TextStyle.Default,
    actionDecoration: TextDecoration = TextDecoration.None,
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
    animationSpec: AnimationSpec<Float> = spring(),
) {
    val textMeasurer = rememberTextMeasurer()
    BoxWithConstraints(modifier) {
        val mergedStyle = style.merge(
            TextStyle(
                color = color,
                fontSize = fontSize,
                fontWeight = fontWeight,
                lineHeight = lineHeight,
                fontFamily = fontFamily,
                textDecoration = textDecoration,
                fontStyle = fontStyle,
                letterSpacing = letterSpacing
            )
        )
        val expandableTextData = constraints.rememberExpandableTextData(
            originalText = originalText,
            expandAction = expandAction,
            collapseAction = collapseAction,
            expand = expand,
            actionColor = actionColor,
            actionStyle = actionStyle,
            actionDecoration = actionDecoration,
            limitedMaxLines = limitedMaxLines,
            softWrap = softWrap,
            textStyle = mergedStyle,
            animationSpec = animationSpec,
            textMeasurer = textMeasurer,
        )
        Text(
            text = expandableTextData.text,
            modifier = Modifier.height(
                with(LocalDensity.current) { expandableTextData.height.toDp() }
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
            maxLines = expandableTextData.lineCount,
        )
    }
}

@Composable
fun Constraints.rememberExpandableTextData(
    originalText: String,
    expandAction: String,
    collapseAction: String?,
    expand: Boolean,
    actionColor: Color,
    actionStyle: TextStyle,
    actionDecoration: TextDecoration,
    limitedMaxLines: Int,
    softWrap: Boolean,
    textStyle: TextStyle,
    animationSpec: AnimationSpec<Float>,
    textMeasurer: TextMeasurer,
): ExpandableTextData {
    // internalExpand == expand means it's the first composition, thus, no animation needed
    var internalExpand by remember { mutableStateOf(expand) }
    val actionSpanStyle = actionStyle.toSpanStyle().copy(
        color = actionColor,
        textDecoration = actionDecoration
    )
    val expandActionWidth = textMeasurer.rememberExpandActionLayoutResult(
        expandAction = expandAction,
        textStyle = textStyle,
        softWrap = softWrap,
    ).size.width
    val collapsedLayoutResult = textMeasurer.rememberCollapsedTextLayoutResult(
        originalText = originalText,
        textStyle = textStyle,
        softWrap = softWrap,
        limitedMaxLines = limitedMaxLines,
        constraints = this
    )
    val expandedLayoutResult = textMeasurer.rememberExpandedTextLayoutResult(
        originalText = originalText,
        textStyle = textStyle,
        softWrap = softWrap,
        constraints = this,
    )
    val collapsedHeight = collapsedLayoutResult.size.height
    val expandedHeight = expandedLayoutResult.size.height
    val collapsedText = collapsedLayoutResult.rememberCollapsedText(
        originalText = originalText,
        expandAction = expandAction,
        expandActionWidth = expandActionWidth,
        actionSpanStyle = actionSpanStyle,
    )

    // Makes sure that we're not showing the show less option when the text can't even expand
    val canShowCollapseAction = collapsedText.text != originalText
    val expandedText = if (collapseAction != null && canShowCollapseAction) {
        val collapseActionWidth = textMeasurer.rememberCollapseActionLayoutResult(
            collapseAction = collapseAction,
            textStyle = textStyle,
            softWrap = softWrap,
        ).size.width
        expandedLayoutResult.rememberExpandedText(
            originalText = originalText,
            collapseAction = collapseAction,
            collapseActionWidth = collapseActionWidth,
            actionSpanStyle = actionSpanStyle,
        )
    } else {
        AnnotatedString(originalText)
    }

    var displayedText by remember {
        mutableStateOf(if (expand) expandedText else collapsedText)
    }
    var displayedLines by remember {
        mutableIntStateOf(if (expand) Int.MAX_VALUE else limitedMaxLines)
    }
    val animatableHeight = remember {
        Animatable((if (expand) expandedHeight else collapsedHeight).toFloat())
    }
    LaunchedEffect(expand, collapsedHeight, expandedHeight, collapsedText) {
        if (internalExpand != expand) {
            displayedText = AnnotatedString(originalText)
            displayedLines = Int.MAX_VALUE
            animatableHeight.animateTo(
                targetValue = (if (expand) expandedHeight else collapsedHeight).toFloat(),
                animationSpec = animationSpec
            )
            internalExpand = expand
        } else {
            animatableHeight.snapTo(
                targetValue = (if (expand) expandedHeight else collapsedHeight).toFloat(),
            )
        }
        displayedText = if (expand) expandedText else collapsedText
        displayedLines = if (expand) Int.MAX_VALUE else limitedMaxLines
    }
    return ExpandableTextData(
        text = displayedText,
        lineCount = displayedLines,
        height = animatableHeight.value
    )
}

@Composable
private fun TextLayoutResult.rememberCollapsedText(
    originalText: String,
    expandAction: String,
    expandActionWidth: Int,
    actionSpanStyle: SpanStyle,
): AnnotatedString {
    val lastLine = lineCount - 1
    val lastCharacterIndex = getLineEnd(lastLine)
    return remember(originalText, expandAction, expandActionWidth, actionSpanStyle) {
        if (lastCharacterIndex == originalText.length) {
            AnnotatedString(originalText)
        } else {
            var lastCharIndex = getLineEnd(lineIndex = lastLine, visibleEnd = true) + 1
            var charRect: Rect
            when (getParagraphDirection(lastCharIndex - 1)) {
                Ltr -> {
                    do {
                        lastCharIndex -= 1
                        charRect = getCursorRect(lastCharIndex)
                    } while (charRect.right > (size.width - expandActionWidth).coerceAtLeast(0))
                }

                Rtl -> {
                    do {
                        lastCharIndex -= 1
                        charRect = getCursorRect(lastCharIndex)
                    } while (charRect.left < expandActionWidth.coerceAtMost(size.width))
                }
            }
            val cutText = originalText
                .substring(startIndex = 0, endIndex = lastCharIndex)
                .dropLastWhile { it.isWhitespace() }
            buildAnnotatedString {
                append(cutText)
                append('…')
                append(' ')
                withStyle(actionSpanStyle) {
                    append(expandAction)
                }
            }
        }
    }
}

@Composable
private fun TextLayoutResult.rememberExpandedText(
    originalText: String,
    collapseAction: String,
    collapseActionWidth: Int,
    actionSpanStyle: SpanStyle,
): AnnotatedString {
    val lastLine = lineCount - 1
    return remember(originalText, collapseAction, collapseActionWidth, actionSpanStyle) {
        var lastCharIndex = getLineEnd(lineIndex = lastLine, visibleEnd = true) + 1
        var charRect: Rect
        when (getParagraphDirection(lastCharIndex - 1)) {
            Ltr -> {
                do {
                    lastCharIndex -= 1
                    charRect = getCursorRect(lastCharIndex)
                } while (charRect.right > (size.width - collapseActionWidth).coerceAtLeast(0))
            }

            Rtl -> {
                do {
                    lastCharIndex -= 1
                    charRect = getCursorRect(lastCharIndex)
                } while (charRect.left < collapseActionWidth.coerceAtMost(size.width))
            }
        }
        val cutText = originalText
            .substring(startIndex = 0, endIndex = lastCharIndex)
            .dropLastWhile { it.isWhitespace() }
        buildAnnotatedString {
            append(cutText)
            append(' ')
            withStyle(actionSpanStyle) {
                append(collapseAction)
            }
        }
    }
}

@Composable
private fun TextMeasurer.rememberExpandActionLayoutResult(
    expandAction: String,
    textStyle: TextStyle,
    softWrap: Boolean,
): TextLayoutResult {
    return remember(expandAction, textStyle, softWrap) {
        measure(
            text = AnnotatedString("… $expandAction"),
            style = textStyle,
            softWrap = softWrap,
            maxLines = 1,
        )
    }
}

@Composable
private fun TextMeasurer.rememberCollapseActionLayoutResult(
    collapseAction: String,
    textStyle: TextStyle,
    softWrap: Boolean,
): TextLayoutResult {
    return remember(collapseAction, textStyle, softWrap) {
        measure(
            text = AnnotatedString(" $collapseAction"),
            style = textStyle,
            softWrap = softWrap,
            maxLines = 1,
        )
    }
}

@Composable
private fun TextMeasurer.rememberCollapsedTextLayoutResult(
    originalText: String,
    textStyle: TextStyle,
    softWrap: Boolean,
    limitedMaxLines: Int,
    constraints: Constraints,
): TextLayoutResult {
    return remember(originalText, textStyle, softWrap, limitedMaxLines, constraints) {
        measure(
            text = AnnotatedString(originalText),
            style = textStyle,
            constraints = constraints,
            softWrap = softWrap,
            maxLines = limitedMaxLines,
        )
    }
}

@Composable
private fun TextMeasurer.rememberExpandedTextLayoutResult(
    originalText: String,
    textStyle: TextStyle,
    softWrap: Boolean,
    constraints: Constraints,
): TextLayoutResult {
    return remember(originalText, textStyle, softWrap, constraints) {
        measure(
            text = AnnotatedString(originalText),
            style = textStyle,
            softWrap = softWrap,
            constraints = constraints,
        )
    }
}

data class ExpandableTextData(
    val text: AnnotatedString,
    val lineCount: Int,
    val height: Float,
)

data class PreviewData(
    val text: String,
    val expandAction: String,
    val collapseAction: String? = null,
    val actionColor: Color = Color.Blue,
    val rtl: Boolean = false,
)

@Preview(showBackground = true, heightDp = 700, backgroundColor = 0xffffff)
@Composable
private fun Preview(
    @PreviewParameter(provider = ExpandableTextPreviewParameterProvider::class) data: PreviewData
) {
    with(data) {
        val direction = if (rtl) RTL else LTR
        CompositionLocalProvider(LocalLayoutDirection provides direction) {
            Box {
                var expand by remember { mutableStateOf(false) }
                ExpandableText(
                    originalText = text,
                    expandAction = expandAction,
                    collapseAction = collapseAction,
                    modifier = Modifier
                        .clickable(
                            indication = null,
                            interactionSource = remember { MutableInteractionSource() }
                        ) { expand = !expand }
                        .background(Color.Gray)
                        .padding(16.dp),
                    expand = expand,
                    actionColor = actionColor,
                )
            }
        }
    }
}
