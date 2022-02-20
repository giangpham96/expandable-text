package la.me.leo.expandabletextview

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ValueAnimator
import android.content.Context
import android.os.Build
import android.text.DynamicLayout
import android.text.Layout.Alignment.ALIGN_NORMAL
import android.text.Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
import android.text.SpannableString
import android.text.SpannableStringBuilder
import android.text.StaticLayout
import android.text.TextUtils
import android.text.TextUtils.TruncateAt.END
import android.text.style.ForegroundColorSpan
import android.util.AttributeSet
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import androidx.annotation.ColorInt
import androidx.appcompat.widget.AppCompatTextView
import androidx.core.content.ContextCompat
import androidx.interpolator.view.animation.FastOutSlowInInterpolator
import kotlin.math.abs

class ExpandableTextView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
) : AppCompatTextView(context, attrs, defStyleAttr) {

    var expandableText: String = ""
        set(value) {
            field = value
            val textWidth = measuredWidth - compoundPaddingRight - compoundPaddingLeft
            updateLayout(collapsed = true, expanded = true, cta = false, textWidth)
            updateText(textWidth)
        }
    var expandCta: String = ""
        set(value) {
            field = value
            val ellipsis = Typography.ellipsis
            val start = ellipsis.toString().length
            expandCtaSpannable = SpannableString("$ellipsis $value")
            expandCtaSpannable.setSpan(
                ForegroundColorSpan(expandCtaColor),
                start,
                expandCtaSpannable.length,
                SPAN_EXCLUSIVE_EXCLUSIVE
            )
            val textWidth = measuredWidth - compoundPaddingRight - compoundPaddingLeft
            updateLayout(collapsed = false, expanded = false, cta = true, textWidth)
            updateText(textWidth)
        }
    var collapsedMaxLines: Int = 3
        set(value) {
            check(maxLines == -1 || value <= maxLines) {
                """
                    maxLines ($maxLines) must be greater than or equal to collapsedMaxLines ($value). 
                    maxLines can be -1 if there is no upper limit for lineCount.
                """.trimIndent()
            }
            field = value
            val textWidth = measuredWidth - compoundPaddingRight - compoundPaddingLeft
            updateLayout(collapsed = true, expanded = false, cta = false, textWidth)
            if (collapsed) {
                updateText(textWidth)
            }
        }

    @ColorInt
    var expandCtaColor: Int = ContextCompat.getColor(context, android.R.color.holo_purple)
        set(value) {
            field = value
            val colorSpan = ForegroundColorSpan(value)
            val ellipsis = Typography.ellipsis
            val start = ellipsis.toString().length
            expandCtaSpannable.setSpan(colorSpan, start, expandCtaSpannable.length, SPAN_EXCLUSIVE_EXCLUSIVE)
            updateText(measuredWidth - compoundPaddingRight - compoundPaddingLeft)
        }

    private var oldTextWidth = 0
    private var animator: Animator? = null
    private var expandCtaSpannable = SpannableString("")
    private var collapsed = true
    private var expandedStaticLayout: StaticLayout? = null
    private var collapsedStaticLayout: StaticLayout? = null
    private var expandCtaStaticLayout: StaticLayout? = null

    init {
        ellipsize = END
        val a = context.obtainStyledAttributes(attrs, R.styleable.ExpandableTextView)
        expandCta = a.getString(R.styleable.ExpandableTextView_expandCta) ?: expandCta
        expandCtaColor = a.getColor(R.styleable.ExpandableTextView_expandCtaColor, expandCtaColor)
        expandableText = a.getString(R.styleable.ExpandableTextView_expandableText) ?: expandableText
        collapsedMaxLines = a.getInt(R.styleable.ExpandableTextView_collapsedMaxLines, collapsedMaxLines)
        check(maxLines == -1 || collapsedMaxLines <= maxLines) {
            """
                maxLines ($maxLines) must be greater than or equal to collapsedMaxLines ($collapsedMaxLines). 
                maxLines can be -1 if there is no upper limit for lineCount.
            """.trimIndent()
        }
        a.recycle()
        setOnClickListener(null)
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val givenWidth = MeasureSpec.getSize(widthMeasureSpec)
        val textWidth = givenWidth - compoundPaddingRight - compoundPaddingLeft
        if (textWidth == oldTextWidth || animator?.isRunning == true) {
            super.onMeasure(widthMeasureSpec, heightMeasureSpec)
            return
        }
        oldTextWidth = textWidth
        updateLayout(collapsed = true, expanded = true, cta = true, textWidth)
        updateText(textWidth)
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
    }

    override fun setMaxLines(maxLines: Int) {
        check(maxLines == -1 || collapsedMaxLines <= maxLines) {
            """
                maxLines ($maxLines) must be greater than or equal to collapsedMaxLines ($collapsedMaxLines). 
                maxLines can be -1 if there is no upper limit for lineCount.
            """.trimIndent()
        }
        super.setMaxLines(maxLines)
        val textWidth = measuredWidth - compoundPaddingRight - compoundPaddingLeft
        updateLayout(collapsed = false, expanded = true, cta = true, textWidth)
        if (!collapsed) updateText(textWidth)
    }

    override fun setOnClickListener(l: OnClickListener?) {
        super.setOnClickListener {
            toggle()
            l?.onClick(it)
        }
    }

    override fun onDetachedFromWindow() {
        animator?.cancel()
        super.onDetachedFromWindow()
    }

    override fun setEllipsize(where: TextUtils.TruncateAt?) {
        /**
         * Due to this issue https://stackoverflow.com/questions/63939222/constraintlayout-ellipsize-start-not-working,
         * this view only supports TextUtils.TruncateAt.END
         */
        super.setEllipsize(END)
    }

    fun toggle() {
        if (expandedStaticLayout?.height == collapsedStaticLayout?.height) return
        val staticLayout = if (collapsed) expandedStaticLayout else collapsedStaticLayout
        val height0 = height
        val height1 = staticLayout!!.height + compoundPaddingBottom + compoundPaddingTop
        animator?.cancel()
        val dur = (abs(height1 - height0) * 2L).coerceAtMost(300L)
        animator = ValueAnimator.ofInt(height0, height1)
            .apply {
                interpolator = FastOutSlowInInterpolator()
                duration = dur
                addUpdateListener { value ->
                    val params = layoutParams
                    layoutParams.height = value.animatedValue as Int
                    layoutParams = params
                }
                addListener(object : AnimatorListenerAdapter() {
                    override fun onAnimationStart(animation: Animator?) {
                        super.onAnimationStart(animation)
                        collapsed = !collapsed
                        text = expandableText
                    }

                    override fun onAnimationEnd(animation: Animator?) {
                        super.onAnimationEnd(animation)
                        updateText(measuredWidth - compoundPaddingRight - compoundPaddingLeft)
                        val params = layoutParams
                        layoutParams.height = WRAP_CONTENT
                        layoutParams = params
                    }
                })
                start()
            }
    }

    private fun resolveDisplayedText(staticLayout: StaticLayout): CharSequence? {
        val truncatedTextWithoutCta = staticLayout.text
        if (truncatedTextWithoutCta.toString() != expandableText) {
            if (!collapsed) return truncatedTextWithoutCta
            val totalTextWidthWithoutCta =
                (0 until staticLayout.lineCount).sumOf { staticLayout.getLineWidth(it).toInt() }
            val totalTextWidthWithCta = totalTextWidthWithoutCta - expandCtaStaticLayout!!.getLineWidth(0)
            val textWithoutCta = TextUtils.ellipsize(expandableText, paint, totalTextWidthWithCta, END)
            val defaultEllipsisStart = textWithoutCta.indexOf(Typography.ellipsis)
            // in case the size only fits cta, shows cta only
            if (textWithoutCta == "") return SpannableStringBuilder(expandCtaStaticLayout!!.text)
            // on some devices Typography.ellipsis can't be found,
            // in that case don't replace ellipsis sign with ellipsizedText
            // users are still able to expand ellipsized text
            if (defaultEllipsisStart == -1) {
                return truncatedTextWithoutCta
            }
            val defaultEllipsisEnd = defaultEllipsisStart + 1
            val span = SpannableStringBuilder()
                .append(textWithoutCta)
                .replace(defaultEllipsisStart, defaultEllipsisEnd, expandCtaStaticLayout!!.text)
            return maybeRemoveEndingCharacters(span)
        } else {
            return expandableText
        }
    }

    // sanity check before applying the text. Most of the time, the loop doesn't happen
    private fun maybeRemoveEndingCharacters(span: SpannableStringBuilder): SpannableStringBuilder {
        val textWidth = collapsedStaticLayout!!.width
        val dynamicLayout = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            DynamicLayout.Builder.obtain(span, paint, textWidth)
                .setAlignment(ALIGN_NORMAL)
                .setIncludePad(false)
                .setLineSpacing(lineSpacingExtra, lineSpacingMultiplier)
                .build()
        } else {
            @Suppress("DEPRECATION")
            DynamicLayout(span, span, paint, textWidth, ALIGN_NORMAL, lineSpacingMultiplier, lineSpacingExtra, false)
        }

        val ctaIndex = span.indexOf(expandCtaStaticLayout!!.text.toString())
        var removingCharIndex = ctaIndex - 1
        while (removingCharIndex >= 0 && dynamicLayout.lineCount > collapsedMaxLines) {
            span.delete(removingCharIndex, removingCharIndex + 1)
            removingCharIndex--
        }
        return span
    }

    private fun updateLayout(collapsed: Boolean, expanded: Boolean, cta: Boolean, textWidth: Int) {
        if (textWidth <= 0) return
        if (collapsed)
            collapsedStaticLayout = getStaticLayout(collapsedMaxLines, expandableText, textWidth)
        if (expanded)
            expandedStaticLayout = getStaticLayout(maxLines, expandableText, textWidth)
        if (cta)
            expandCtaStaticLayout = getStaticLayout(1, expandCtaSpannable, textWidth)
    }

    private fun updateText(textWidth: Int) {
        if (textWidth <= 0) return
        text = resolveDisplayedText(if (collapsed) collapsedStaticLayout!! else expandedStaticLayout!!)
    }

    private fun getStaticLayout(targetMaxLines: Int, text: CharSequence, textWidth: Int): StaticLayout {
        val maximumLineWidth = textWidth.coerceAtLeast(0)
        return StaticLayout.Builder
            .obtain(text, 0, text.length, paint, maximumLineWidth)
            .setIncludePad(false)
            .setEllipsize(END)
            .setMaxLines(targetMaxLines)
            .setLineSpacing(lineSpacingExtra, lineSpacingMultiplier)
            .run {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                    setUseLineSpacingFromFallbacks(true)
                } else {
                    this
                }
            }
            .build()
    }

}
