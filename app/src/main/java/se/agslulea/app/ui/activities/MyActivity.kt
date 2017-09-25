package se.agslulea.app.ui.activities

import android.graphics.drawable.ColorDrawable
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.widget.LinearLayout

import se.agslulea.app.R

class MyActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_my)

        val parent = findViewById(R.id.my_linear_layout) as LinearLayout

        val firstChild = LinearLayout(this)
        val firstParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT,
                3.toFloat())
        firstChild.layoutParams = firstParams
        firstChild.background = ColorDrawable(0xFFFF0000.toInt())
        parent.addView(firstChild)

        val secondChild = LinearLayout(this)
        val secondParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT,
                2.toFloat())
        secondChild.layoutParams = secondParams
        secondChild.background = ColorDrawable(0xFF00FF00.toInt())
        parent.addView(secondChild)

        val thirdChild = LinearLayout(this)
        val thirdParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT,
                4.toFloat())
        thirdChild.layoutParams = thirdParams
        thirdChild.background = ColorDrawable(0xFF0000FF.toInt())
        parent.addView(thirdChild)

    }
}
