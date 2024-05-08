package io.github.giangpham96.expandable_text_compose

import androidx.compose.ui.tooling.preview.PreviewParameterProvider

internal class ExpandableTextPreviewParameterProvider : PreviewParameterProvider<PreviewData> {

    override val values: Sequence<PreviewData>
        get() = sequenceOf(
            rtlPreview(showCollapseAction = true),
            rtlPreview(showCollapseAction = false),
            ltrPreview(text = BIG_TEXT, showCollapseAction = true),
            ltrPreview(text = BIG_TEXT, showCollapseAction = false),
            ltrPreview(text = SMALL_TEXT, showCollapseAction = true),
            ltrPreview(text = SMALL_TEXT, showCollapseAction = false),
        )

    private companion object {
        const val RTL_TEXT =
            "וְאָהַבְתָּ אֵת יְיָ | אֱלֹהֶיךָ, בְּכָל-לְבָֽבְךָ, וּבְכָל-נַפְשְׁךָ" +
                    ", וּבְכָל-מְאֹדֶֽךָ. וְהָיוּ הַדְּבָרִים הָאֵלֶּה, אֲשֶׁר | אָֽנֹכִי מְצַוְּךָ הַיּוֹם, עַל-לְבָבֶֽךָ: וְשִׁנַּנְתָּם לְבָנ" +
                    "ֶיךָ, וְדִבַּרְתָּ בָּם בְּשִׁבְתְּךָ בְּבֵיתֶךָ, וּבְלֶכְתְּךָ בַדֶּרֶךְ וּֽבְשָׁכְבְּךָ, וּבְקוּמֶֽךָ. וּקְשַׁרְתָּם לְאוֹת" +
                    " | עַל-יָדֶךָ, וְהָיוּ לְטֹטָפֹת בֵּין | עֵינֶֽיךָ, וּכְתַבְתָּם | עַל מְזֻזֹת בֵּיתֶךָ וּבִשְׁעָרֶֽיך:"
        const val BIG_TEXT = "Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod " +
                    "tempor incididunt ut labore et dolore magna aliqua. Ut enim ad minim veniam, " +
                    "quis nostrud exercitation ullamco laboris nisi ut aliquip ex ea commodo " +
                    "consequat. Duis aute irure dolor in reprehenderit in voluptate velit esse " +
                    "cillum dolore eu fugiat nulla pariatur. Excepteur sint occaecat cupidatat non " +
                    "proident, sunt in culpa qui officia deserunt mollit anim id est laborum."
        const val SMALL_TEXT = "Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod " +
                    "tempor incididunt ut labore et dolore magna aliqua."
        const val EXPAND_ACTION = "See more"
        const val COLLAPSE_ACTION = "See less"

        fun rtlPreview(showCollapseAction: Boolean) = PreviewData(
            text = RTL_TEXT,
            expandAction = EXPAND_ACTION,
            collapseAction = COLLAPSE_ACTION.takeIf { showCollapseAction },
            rtl = true
        )

        fun ltrPreview(
            text: String,
            showCollapseAction: Boolean
        ) = PreviewData(
            text = text,
            expandAction = EXPAND_ACTION,
            collapseAction = COLLAPSE_ACTION.takeIf { showCollapseAction }
        )
    }
}