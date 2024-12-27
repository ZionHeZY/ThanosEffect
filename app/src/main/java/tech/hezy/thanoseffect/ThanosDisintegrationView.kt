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
import androidx.core.animation.doOnEnd
import kotlin.math.cos
import kotlin.math.roundToInt
import kotlin.math.sin
import kotlin.random.Random

class ThanosDisintegrationView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : ViewGroup(context, attrs, defStyleAttr) {

    companion object {
        private const val DEFAULT_GRID_ROWS = 10
        private const val DEFAULT_GRID_COLS = 20
        private const val DEFAULT_DURATION = 3000L
        private const val DEFAULT_GAP = 3
        private const val MIN_PARTICLE_SIZE = 10
    }

    private var gridRows: Int = DEFAULT_GRID_ROWS
    private var gridCols: Int = DEFAULT_GRID_COLS
    private var durationMillis: Long = DEFAULT_DURATION
    private var gapPx: Int = DEFAULT_GAP
    private var baseSpeedMin = 10f
    private var baseSpeedMax = 50f
    private var angleStart = -60.0
    private var angleEnd = -30.0

    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val random = Random(System.currentTimeMillis())
    private val particles = mutableListOf<Particle>()
    private var contentBitmap: Bitmap? = null
    private var animator: ValueAnimator? = null
    private var isAnimating = false

    init {
        context.obtainStyledAttributes(attrs, R.styleable.ThanosDisintegrationView).apply {
            try {
                gridRows = getInteger(R.styleable.ThanosDisintegrationView_gridRows, DEFAULT_GRID_ROWS)
                gridCols = getInteger(R.styleable.ThanosDisintegrationView_gridCols, DEFAULT_GRID_COLS)
                durationMillis = getInteger(R.styleable.ThanosDisintegrationView_durationMillis, DEFAULT_DURATION.toInt()).toLong()
                gapPx = getDimensionPixelSize(R.styleable.ThanosDisintegrationView_gapPx, DEFAULT_GAP)
            } finally {
                recycle()
            }
        }
        setWillNotDraw(false)
    }

    override fun onLayout(changed: Boolean, l: Int, t: Int, r: Int, b: Int) {
        getChildAt(0)?.let { child ->
            val centerX = (r - l - child.measuredWidth) / 2
            val centerY = (b - t - child.measuredHeight) / 2
            child.layout(
                centerX,
                centerY,
                centerX + child.measuredWidth,
                centerY + child.measuredHeight
            )
        }
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        getChildAt(0)?.let { child ->
            measureChild(child, widthMeasureSpec, heightMeasureSpec)
        }
        setMeasuredDimension(
            resolveSize(suggestedMinimumWidth, widthMeasureSpec),
            resolveSize(suggestedMinimumHeight, heightMeasureSpec)
        )
    }

    fun startDisintegration() {
        if (isAnimating || childCount == 0) return
        isAnimating = true
        
        getChildAt(0)?.let { child ->
            child.visibility = View.INVISIBLE
            contentBitmap = child.drawToBitmap()
            initParticles()
            startAnimation()
        }
    }

    fun reset() {
        animator?.cancel()
        isAnimating = false
        particles.clear()
        contentBitmap?.recycle()
        contentBitmap = null
        getChildAt(0)?.visibility = View.VISIBLE
        invalidate()
    }

    private fun initParticles() {
        particles.clear()
        val child = getChildAt(0) ?: return
        val bitmap = contentBitmap ?: return

        val viewWidth = child.width
        val viewHeight = child.height
        val startX = child.left
        val startY = child.top

        val baseParticleWidth = (viewWidth / gridCols.toFloat()).roundToInt()
        val baseParticleHeight = (viewHeight / gridRows.toFloat()).roundToInt()

        if (baseParticleWidth < MIN_PARTICLE_SIZE || baseParticleHeight < MIN_PARTICLE_SIZE) {
            val newCols = viewWidth / MIN_PARTICLE_SIZE
            val newRows = viewHeight / MIN_PARTICLE_SIZE
            gridCols = newCols.coerceAtMost(DEFAULT_GRID_COLS)
            gridRows = newRows.coerceAtMost(DEFAULT_GRID_ROWS)
        }

        for (row in 0 until gridRows) {
            for (col in 0 until gridCols) {
                val randomWidthFactor = 0.7f + random.nextFloat() * 0.6f
                val randomHeightFactor = 0.7f + random.nextFloat() * 0.6f
                
                val particleWidth = (baseParticleWidth * randomWidthFactor).roundToInt()
                val particleHeight = (baseParticleHeight * randomHeightFactor).roundToInt()

                val offsetX = random.nextFloat() * baseParticleWidth * 0.4f
                val offsetY = random.nextFloat() * baseParticleHeight * 0.2f
                
                val x = startX + col * baseParticleWidth + offsetX
                val y = startY + row * baseParticleHeight + offsetY
                
                if (x + particleWidth <= startX + viewWidth && 
                    y + particleHeight <= startY + viewHeight) {
                    particles.add(
                        Particle(
                            x = x,
                            y = y,
                            width = particleWidth - gapPx,
                            height = particleHeight - gapPx,
                            bitmap = bitmap,
                            speed = random.nextFloat() * (baseSpeedMax - baseSpeedMin) + baseSpeedMin,
                            angle = random.nextDouble() * (angleEnd - angleStart) + angleStart,
                            srcX = x.roundToInt() - startX,
                            srcY = y.roundToInt() - startY
                        )
                    )
                }
            }
        }
    }

    private fun startAnimation() {
        animator = ValueAnimator.ofFloat(0f, 1f).apply {
            duration = durationMillis
            interpolator = LinearInterpolator()
            
            addUpdateListener { animation ->
                particles.forEach { particle ->
                    particle.update(animation.animatedFraction)
                }
                invalidate()
            }
            
            doOnEnd {
                isAnimating = false
                particles.clear()
                contentBitmap?.recycle()
                contentBitmap = null
            }
            
            start()
        }
    }

    override fun onDraw(canvas: Canvas) {
        if (!isAnimating) {
            super.onDraw(canvas)
            return
        }

        particles.forEach { particle ->
            particle.draw(canvas, paint)
        }
    }

    private data class Particle(
        var x: Float,
        var y: Float,
        val width: Int,
        val height: Int,
        val bitmap: Bitmap,
        val speed: Float,
        val angle: Double,
        val srcX: Int,
        val srcY: Int,
        var alpha: Int = 255
    ) {
        fun update(progress: Float) {
            val moveProgress = progress * progress
            val distance = speed * moveProgress
            x += (distance * cos(Math.toRadians(angle))).toFloat()
            y += (distance * sin(Math.toRadians(angle))).toFloat()
            
            alpha = ((1f - progress) * 255).roundToInt().coerceIn(0, 255)
        }

        fun draw(canvas: Canvas, paint: Paint) {
            paint.alpha = alpha
            canvas.drawBitmap(
                bitmap,
                android.graphics.Rect(srcX, srcY, srcX + width, srcY + height),
                android.graphics.RectF(x, y, x + width, y + height),
                paint
            )
        }
    }

    private fun View.drawToBitmap(): Bitmap {
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        draw(canvas)
        return bitmap
    }
}



