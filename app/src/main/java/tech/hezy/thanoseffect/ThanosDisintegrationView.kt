package tech.hezy.thanoseffect

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.util.AttributeSet
import android.view.View
import android.view.ViewGroup
import android.view.animation.LinearInterpolator
import kotlin.math.cos
import kotlin.math.roundToInt
import kotlin.math.sin
import kotlin.random.Random

class ThanosDisintegrationView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : ViewGroup(context, attrs, defStyleAttr) {

    private var gridRows: Int = 10
    private var gridCols: Int = 20
    private var durationMillis: Long = 3000
    private var enableFadeOut: Boolean = true
    private var gapPx: Int = 2
    private var baseSpeedMin = 100f
    private var baseSpeedMax = 400f
    private var angleStart = -60.0
    private var angleEnd = -30.0


    data class Particle(
        val bitmap: Bitmap,
        val initX: Float,
        val initY: Float,
        val angleDeg: Double,
        val speed: Float,
        var x: Float = 0f,
        var y: Float = 0f,
        var alpha: Int = 255
    )

    private var sourceBitmap: Bitmap? = null
    private var animator: ValueAnimator? = null
    private var isDisintegrating = false
    private val particles = mutableListOf<Particle>()
    private val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        isFilterBitmap = true
    }

    init {
        setWillNotDraw(false)
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        measureChildren(widthMeasureSpec, heightMeasureSpec)

        var maxWidth = 0
        var maxHeight = 0

        for (i in 0 until childCount) {
            val child = getChildAt(i)
            if (child.visibility != View.GONE) {
                val cw = child.measuredWidth
                val ch = child.measuredHeight
                if (cw > maxWidth) maxWidth = cw
                if (ch > maxHeight) maxHeight = ch
            }
        }
        maxWidth += paddingLeft + paddingRight
        maxHeight += paddingTop + paddingBottom

        val finalWidth = resolveSize(maxWidth, widthMeasureSpec)
        val finalHeight = resolveSize(maxHeight, heightMeasureSpec)
        setMeasuredDimension(finalWidth, finalHeight)
    }

    override fun onLayout(changed: Boolean, l: Int, t: Int, r: Int, b: Int) {
        if (childCount == 0) return

        val parentW = measuredWidth
        val parentH = measuredHeight

        for (i in 0 until childCount) {
            val child = getChildAt(i)
            if (child.visibility == View.GONE) continue

            val cw = child.measuredWidth
            val ch = child.measuredHeight
            val cl = (parentW - cw) / 2
            val ct = (parentH - ch) / 2
            child.layout(cl, ct, cl + cw, ct + ch)
        }
    }


    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        if (!isDisintegrating) return
        for (p in particles) {
            paint.alpha = p.alpha
            canvas.drawBitmap(p.bitmap, p.x, p.y, paint)
        }
    }


    fun startDisintegration() {
        if (childCount == 0 || isDisintegrating) return

        val child = getChildAt(0) ?: return
        sourceBitmap = createBitmapFromView(child)

        setupParticles()

        child.visibility = View.INVISIBLE

        animator = ValueAnimator.ofFloat(0f, 1f).apply {
            duration = durationMillis
            interpolator = LinearInterpolator()
            addUpdateListener {
                val fraction = it.animatedFraction
                updateParticles(fraction)
                invalidate()
            }
            start()
        }
        isDisintegrating = true
    }

    fun reset() {
        animator?.cancel()
        animator = null
        isDisintegrating = false
        particles.clear()
        sourceBitmap?.recycle()
        sourceBitmap = null

        if (childCount > 0) {
            getChildAt(0)?.visibility = View.VISIBLE
        }
        invalidate()
    }

    private fun createBitmapFromView(view: View): Bitmap {
        val w = view.width
        val h = view.height
        val bmp = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
        val c = Canvas(bmp)
        view.draw(c)
        return bmp
    }

    private fun setupParticles() {
        if (sourceBitmap == null) return
        particles.clear()

        val child = getChildAt(0) ?: return
        val offsetX = child.left
        val offsetY = child.top

        val bmp = sourceBitmap!!
        val pieceW = bmp.width / gridCols
        val pieceH = bmp.height / gridRows

        for (row in 0 until gridRows) {
            for (col in 0 until gridCols) {
                val srcLeft = col * pieceW
                val srcTop = row * pieceH
                val actualW = if (col == gridCols - 1) bmp.width - srcLeft else pieceW
                val actualH = if (row == gridRows - 1) bmp.height - srcTop else pieceH

                val pieceBitmap = Bitmap.createBitmap(bmp, srcLeft, srcTop, actualW, actualH)

                val initX = offsetX + col * (pieceW + gapPx)
                val initY = offsetY + row * (pieceH + gapPx)

                val angleDeg = angleStart + (angleEnd - angleStart) * Random.nextDouble()
                val speed = Random.nextFloat() * (baseSpeedMax - baseSpeedMin) + baseSpeedMin

                val p = Particle(
                    bitmap = pieceBitmap,
                    initX = initX.toFloat(),
                    initY = initY.toFloat(),
                    angleDeg = angleDeg,
                    speed = speed,
                    x = initX.toFloat(),
                    y = initY.toFloat(),
                    alpha = 255
                )
                particles.add(p)
            }
        }
    }

    private fun updateParticles(fraction: Float) {
        for (p in particles) {
            val distance = p.speed * fraction

            val rad = Math.toRadians(p.angleDeg)
            val dx = distance * cos(rad)
            val dy = distance * sin(rad)

            p.x = p.initX + dx.toFloat()
            p.y = p.initY + dy.toFloat()

            if (enableFadeOut) {
                p.alpha = (255 * (1 - fraction)).roundToInt().coerceIn(0, 255)
            }
        }
    }
}



