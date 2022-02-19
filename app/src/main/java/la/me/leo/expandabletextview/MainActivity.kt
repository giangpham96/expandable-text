package la.me.leo.expandabletextview

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.content.res.AppCompatResources
import la.me.leo.expandabletextview.databinding.ActivityMainBinding
import kotlin.random.Random

class MainActivity : AppCompatActivity() {

    private var _binding: ActivityMainBinding? = null
    private val binding get() = _binding!!

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _binding = ActivityMainBinding.inflate(LayoutInflater.from(this))
        setContentView(binding.root)
        setupViews()
    }

    private fun setupViews() = with(binding) {
        btnWidth.setOnClickListener {
            val layoutParams = tvDynamic.layoutParams
            layoutParams.width = if (layoutParams.width == MATCH_PARENT) 600 else MATCH_PARENT
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
        btnCollapsedMaxLines.setOnClickListener {
            tvDynamic.collapsedMaxLines = Random.nextInt(3, 6)
        }
        btnExpandCta.setOnClickListener {
            val ctaSet = setOf("Read more", "More", "Expand", "View more", "More detail?")
            tvDynamic.expandCta = ctaSet.random()
        }
        btnExpandCtaColor.setOnClickListener {
            val ctaColorSet =
                setOf(Color.WHITE, Color.BLACK, Color.BLUE, Color.CYAN, Color.MAGENTA, Color.RED, Color.DKGRAY)
            tvDynamic.expandCtaColor = ctaColorSet.random()
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
            tvDynamic.expandableText = textSet.random()
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
    }

    override fun onDestroy() {
        _binding = null
        super.onDestroy()
    }
}
