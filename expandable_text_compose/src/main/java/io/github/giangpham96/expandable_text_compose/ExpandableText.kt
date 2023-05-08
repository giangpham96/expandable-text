package io.github.giangpham96.expandable_text_compose

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationSpec
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp

/**
 * @param originalText The text that might be truncated/collapsed if too long
 * @param expandAction The text that is appended at the end of the collapsed text
 * @param expand Whether the text should be expanded or not. Default to false
 * @param expandActionColor The color of [expandAction]
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
    expand: Boolean = false,
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
    animationSpec: AnimationSpec<Float> = spring(),
) {
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
        val animatableHeight = remember { Animatable(0f) }
        // internalExpand == expand means it's the first composition, thus, no animation needed
        var internalExpand by remember { mutableStateOf(expand) }
        var displayedText by remember { mutableStateOf(AnnotatedString(originalText)) }
        var displayedLines by remember { mutableStateOf(limitedMaxLines) }
        val textMeasurer = rememberTextMeasurer()
        val expandActionWidth = remember(expandAction, mergedStyle, softWrap) {
            textMeasurer.measure(
                text = AnnotatedString("… $expandAction"),
                style = mergedStyle,
                softWrap = softWrap,
                maxLines = 1,
            ).size.width
        }
        val collapsedLayoutResult = remember(originalText, mergedStyle, softWrap) {
            textMeasurer.measure(
                text = AnnotatedString(originalText),
                style = mergedStyle,
                constraints = constraints,
                softWrap = softWrap,
                maxLines = limitedMaxLines,
            )
        }
        val collapsedHeight = collapsedLayoutResult.size.height
        val expandedHeight = remember(originalText, mergedStyle, softWrap) {
            textMeasurer.measure(
                text = AnnotatedString(originalText),
                style = mergedStyle,
                softWrap = softWrap,
                constraints = constraints,
            ).size.height
        }
        val collapsedText = remember(collapsedLayoutResult) {
            collapsedLayoutResult.resolveCollapsedText(
                originalText,
                expandAction,
                expandActionWidth,
                expandActionColor,
            )
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
            displayedText = if (expand) AnnotatedString(originalText) else collapsedText
            displayedLines = if (expand) Int.MAX_VALUE else limitedMaxLines
        }
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
    }
}

private fun TextLayoutResult.resolveCollapsedText(
    originalText: String,
    expandAction: String,
    expandActionWidth: Int,
    expandActionColor: Color,
): AnnotatedString {
    val lastLine = lineCount - 1
    val lastCharacterIndex = getLineEnd(lastLine)
    return if (lastCharacterIndex == originalText.length) {
        AnnotatedString(originalText)
    } else {
        var lastCharIndex = getLineEnd(lineIndex = lastLine, visibleEnd = true) + 1
        var charRect: Rect
        when (getParagraphDirection(lastCharIndex - 1)) {
            Ltr -> {
                do {
                    lastCharIndex -= 1
                    charRect = getCursorRect(lastCharIndex)
                } while (charRect.right > size.width - expandActionWidth)
            }

            Rtl -> {
                do {
                    lastCharIndex -= 1
                    charRect = getCursorRect(lastCharIndex)
                } while (charRect.left < expandActionWidth)
            }
        }
        val cutText = originalText
            .substring(startIndex = 0, endIndex = lastCharIndex)
            .dropLastWhile { it.isWhitespace() }
        buildAnnotatedString {
            append(cutText)
            append('…')
            append(' ')
            withStyle(SpanStyle(color = expandActionColor)) {
                append(expandAction)
            }
        }
    }
}

@Preview(showBackground = true, heightDp = 700, backgroundColor = 0xffffff)
@Composable
private fun PreviewRtl() =
    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
        Box {
            var expand by remember { mutableStateOf(false) }
            ExpandableText(
                originalText = "וְאָהַבְתָּ אֵת יְיָ | אֱלֹהֶיךָ, בְּכָל-לְבָֽבְךָ, וּבְכָל-נַפְשְׁךָ" +
                        ", וּבְכָל-מְאֹדֶֽךָ. וְהָיוּ הַדְּבָרִים הָאֵלֶּה, אֲשֶׁר | אָֽנֹכִי מְצַוְּךָ הַיּוֹם, עַל-לְבָבֶֽךָ: וְשִׁנַּנְתָּם לְבָנ" +
                        "ֶיךָ, וְדִבַּרְתָּ בָּם בְּשִׁבְתְּךָ בְּבֵיתֶךָ, וּבְלֶכְתְּךָ בַדֶּרֶךְ וּֽבְשָׁכְבְּךָ, וּבְקוּמֶֽךָ. וּקְשַׁרְתָּם לְאוֹת" +
                        " | עַל-יָדֶךָ, וְהָיוּ לְטֹטָפֹת בֵּין | עֵינֶֽיךָ, וּכְתַבְתָּם | עַל מְזֻזֹת בֵּיתֶךָ וּבִשְׁעָרֶֽיך:",
                expandAction = "See more",
                modifier = Modifier
                    .clickable { expand = !expand }
                    .background(Color.Gray)
                    .padding(16.dp),
                expand = expand,
                expandActionColor = Color.Blue
            )
        }
    }

@Preview(showBackground = true, heightDp = 700, backgroundColor = 0xffffff)
@Composable
private fun Preview() = Box {
    var expand by remember { mutableStateOf(false) }
    ExpandableText(
        originalText = "Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod " +
                "tempor incididunt ut labore et dolore magna aliqua. Ut enim ad minim veniam, " +
                "quis nostrud exercitation ullamco laboris nisi ut aliquip ex ea commodo " +
                "consequat. Duis aute irure dolor in reprehenderit in voluptate velit esse " +
                "cillum dolore eu fugiat nulla pariatur. Excepteur sint occaecat cupidatat non " +
                "proident, sunt in culpa qui officia deserunt mollit anim id est laborum.",
        expandAction = "See more",
        modifier = Modifier
            .clickable { expand = !expand }
            .background(Color.Gray)
            .padding(16.dp),
        expand = expand,
        expandActionColor = Color.Blue
    )
}
