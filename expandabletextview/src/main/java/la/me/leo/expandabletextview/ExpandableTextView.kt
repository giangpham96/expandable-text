package la.me.leo.expandabletextview

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ValueAnimator
import android.content.Context
import android.os.Build
import android.text.Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
import android.text.SpannableString
import android.text.SpannableStringBuilder
import android.text.StaticLayout
import android.text.TextUtils
import android.text.style.ForegroundColorSpan
import android.util.AttributeSet
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
            invalidate()
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
            invalidate()
        }
    var collapsedMaxLines: Int = 6
        set(value) {
            field = value
            invalidate()
        }
    var animator: Animator? = null

    @ColorInt
    var expandCtaColor: Int = ContextCompat.getColor(context, android.R.color.holo_purple)
        set(value) {
            field = value
            val colorSpan = ForegroundColorSpan(value)
            val ellipsis = Typography.ellipsis
            val start = ellipsis.toString().length
            expandCtaSpannable.setSpan(colorSpan, start, expandCtaSpannable.length, SPAN_EXCLUSIVE_EXCLUSIVE)
            invalidate()
        }

    private val spannableStringBuilder = SpannableStringBuilder()
    private var expandCtaSpannable = SpannableString("")
    private var collapsed = true

    init {
        val a = context.obtainStyledAttributes(attrs, R.styleable.ExpandableTextView)
        expandCta = a.getString(R.styleable.ExpandableTextView_expandCta) ?: expandCta
        expandCtaColor = a.getColor(R.styleable.ExpandableTextView_expandCtaColor, expandCtaColor)
        expandableText = a.getString(R.styleable.ExpandableTextView_expandableText) ?: expandableText
        collapsedMaxLines = a.getInt(R.styleable.ExpandableTextView_collapsedMaxLines, collapsedMaxLines)
        a.recycle()
        setOnClickListener(null)
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        if (!collapsed || animator?.isRunning == true) return
        val staticLayout = getStaticLayout(collapsedMaxLines)
        text = resolveDisplayedText(staticLayout)
    }

    private fun resolveDisplayedText(staticLayout: StaticLayout): CharSequence? {
        val truncatedTextWithoutCta = staticLayout.text
        if (truncatedTextWithoutCta.toString() != expandableText) {
            if (!collapsed) return truncatedTextWithoutCta
            val totalTextWidthWithoutCta =
                (0 until staticLayout.lineCount).sumOf { staticLayout.getLineWidth(it).toInt() }
            val totalTextWidthWithCta = totalTextWidthWithoutCta - paint.measureText("$expandCta ")
            val textWithoutCta = TextUtils.ellipsize(expandableText, paint, totalTextWidthWithCta, ellipsize)

            val defaultEllipsisStart = textWithoutCta.indexOf(Typography.ellipsis)
            // on some devices Typography.ellipsis can't be found,
            // in that case don't replace ellipsis sign with ellipsizedText
            // users are still able to expand ellipsized text
            if (defaultEllipsisStart == -1) {
                return truncatedTextWithoutCta
            }
            val defaultEllipsisEnd = defaultEllipsisStart + 1
            spannableStringBuilder.clear()
            return spannableStringBuilder
                .append(textWithoutCta)
                .replace(defaultEllipsisStart, defaultEllipsisEnd, expandCtaSpannable)
        } else {
            return expandableText
        }
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

    fun toggle() {
        val targetMaxLines = if (collapsed) maxLines else collapsedMaxLines
        val staticLayout = getStaticLayout(targetMaxLines)
        val height0 = height
        val height1 = staticLayout.height + compoundPaddingBottom + compoundPaddingTop
        animator?.cancel()
        val lineCount0 =
            staticLayout.lineCount / staticLayout.height * (height - compoundPaddingTop - compoundPaddingBottom)
        val lineCount1 = staticLayout.lineCount
        val dur = (abs(lineCount1 - lineCount0) * 20L).coerceAtMost(300L)
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
                        text = resolveDisplayedText(staticLayout)
                    }
                })
                start()
            }
    }

    private fun getStaticLayout(targetMaxLines: Int): StaticLayout {
        val maximumLineWidth = measuredWidth - compoundPaddingLeft - compoundPaddingRight
        return StaticLayout.Builder
            .obtain(expandableText, 0, expandableText.length, paint, maximumLineWidth)
            .setIncludePad(false)
            .setEllipsize(ellipsize)
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
