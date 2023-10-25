package io.github.giangpham96.app

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.content.res.AppCompatResources
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.constraintlayout.widget.ConstraintSet
import io.github.giangpham96.expandable_text_compose.ExpandableText
import io.github.giangpham96.app.databinding.ActivityMainBinding
import java.util.Timer
import java.util.TimerTask
import kotlin.random.Random

class MainActivity : AppCompatActivity() {

    private var _binding: ActivityMainBinding? = null
    private val binding get() = _binding!!
    private val t = Timer()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _binding = ActivityMainBinding.inflate(LayoutInflater.from(this))
        setContentView(binding.root)
        setupViews()
    }

    private fun setupViews() = with(binding) {
        btnWidth.setOnClickListener {
            val layoutParams = tvDynamic.layoutParams
            layoutParams.width = if (layoutParams.width == MATCH_PARENT) 800 else MATCH_PARENT
            tvDynamic.layoutParams = layoutParams
        }
        btnPadding.setOnClickListener {
            val paddingSet = setOf(16, 24, 32, 48)
            val l = paddingSet.random()
            val t = paddingSet.random()
            val r = paddingSet.random()
            val b = paddingSet.random()
            tvDynamic.setPadding(l, t, r, b)
        }
        btnLimitedMaxLines.setOnClickListener {
            tvDynamic.limitedMaxLines = Random.nextInt(3, 6)
        }
        btnExpandCta.setOnClickListener {
            val ctaSet = setOf("Read more", "More", "Expand", "View more", "More detail?")
            tvDynamic.expandAction = ctaSet.random()
        }
        btnExpandCtaColor.setOnClickListener {
            val ctaColorSet =
                setOf(Color.WHITE, Color.BLACK, Color.BLUE, Color.CYAN, Color.MAGENTA, Color.RED, Color.DKGRAY)
            tvDynamic.actionColor = ctaColorSet.random()
        }
        btnMaxLines.setOnClickListener {
            tvDynamic.maxLines = Random.nextInt(7, 20)
        }
        btnText.setOnClickListener {
            val textSet = setOf(
                getString(R.string.arabic_text),
                getString(R.string.georgian_text),
                getString(R.string.long_text),
                getString(R.string.hebrew_text),
                "Short text that doesn't need expand"
            )
            tvDynamic.originalText = textSet.random()
        }
        btnDrawable.setOnClickListener { 
            val drawableSet = setOf(
                AppCompatResources.getDrawable(this@MainActivity, android.R.drawable.ic_delete),
                AppCompatResources.getDrawable(this@MainActivity, android.R.drawable.ic_btn_speak_now),
                AppCompatResources.getDrawable(this@MainActivity, android.R.drawable.ic_dialog_dialer),
                AppCompatResources.getDrawable(this@MainActivity, android.R.drawable.ic_dialog_alert),
                AppCompatResources.getDrawable(this@MainActivity, android.R.drawable.ic_input_get),
                AppCompatResources.getDrawable(this@MainActivity, android.R.drawable.ic_input_add),
                AppCompatResources.getDrawable(this@MainActivity, android.R.drawable.ic_input_delete),
                AppCompatResources.getDrawable(this@MainActivity, android.R.drawable.ic_lock_idle_lock),
                null
            )
            val s = drawableSet.random()
            val t = drawableSet.random()
            val e = drawableSet.random()
            val b = drawableSet.random()
            tvDynamic.setCompoundDrawablesRelativeWithIntrinsicBounds(s, t, e, b)
        }
        t.schedule(object: TimerTask() {
            override fun run() {
                runOnUiThread {
                    val layoutParams = flDynamicSize.layoutParams
                    layoutParams.width = if (layoutParams.width == MATCH_PARENT) 800 else MATCH_PARENT
                    flDynamicSize.layoutParams = layoutParams
                    val layoutParams1 = v.layoutParams
                    layoutParams1.width = if (layoutParams1.width == 0) 200 else 0
                    v.layoutParams = layoutParams1
                    val constraintSet = ConstraintSet()
                    constraintSet.clone(clDynamicConstraint)
                    constraintSet.constrainPercentWidth(R.id.tvDynamicConstraint, Random.nextDouble(0.5, 1.0).toFloat().coerceIn(0.5f, 1f))
                    constraintSet.applyTo(clDynamicConstraint)
                }
            }
        }, 2000L, 5000L)
        composeView.setContent {
            var expand by remember { mutableStateOf(false) }
            ExpandableText(
                modifier = Modifier
                    .clickable { expand = !expand }
                    .background(colorResource(id = R.color.purple_100))
                    .padding(16.dp),
                originalText = stringResource(id = R.string.long_text),
                expandAction = "See more",
                expand = expand,
                actionColor = androidx.compose.ui.graphics.Color.Blue,
            )
        }
    }

    override fun onDestroy() {
        _binding = null
        t.cancel()
        super.onDestroy()
    }
}
