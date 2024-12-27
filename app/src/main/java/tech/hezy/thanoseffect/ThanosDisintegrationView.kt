package tech.hezy.thanoseffect

import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.PorterDuff
import android.graphics.PorterDuffXfermode
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
    private var baseSpeedMin = 5f
    private var baseSpeedMax = 25f
    private var angleStart = -60.0
    private var angleEnd = -30.0

    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val random = Random(System.currentTimeMillis())
    private val particles = mutableListOf<Particle>()
    private var contentBitmap: Bitmap? = null
    private var animator: ValueAnimator? = null
    private var isAnimating = false
    private val originalPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        xfermode = PorterDuffXfermode(PorterDuff.Mode.DST_IN)
    }
    private val clearPaint = Paint().apply {
        xfermode = PorterDuffXfermode(PorterDuff.Mode.CLEAR)
    }

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
        
        getChildAt(0)?.let { child ->
            child.visibility = View.INVISIBLE
            contentBitmap = child.drawToBitmap()
            initParticles()
            
            animator = ValueAnimator.ofFloat(0f, 1f).apply {
                duration = durationMillis
                interpolator = LinearInterpolator()
                
                addUpdateListener { animation ->
                    val progress = animation.animatedFraction
                    particles.forEach { particle ->
                        particle.update(progress)
                    }
                    invalidate()
                }
                
                doOnEnd {
                    isAnimating = false
                    particles.clear()
                    contentBitmap?.recycle()
                    contentBitmap = null
                }
            }
            
            isAnimating = true
            animator?.start()
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

    @SuppressLint("DrawAllocation")
    override fun onDraw(canvas: Canvas) {
        if (!isAnimating) {
            super.onDraw(canvas)
            return
        }

        val child = getChildAt(0) ?: return
        val bitmap = contentBitmap ?: return
        val progress = animator?.animatedFraction ?: 0f

        val layerBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val layerCanvas = Canvas(layerBitmap)

        layerCanvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), clearPaint)

        layerCanvas.drawBitmap(
            bitmap,
            child.left.toFloat(),
            child.top.toFloat(),
            Paint()
        )

        val maskBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val maskCanvas = Canvas(maskBitmap)

        val maskDelay = 0.15f
        val adjustedProgress = ((progress - maskDelay) / (1 - maskDelay)).coerceIn(0f, 1f)
        
        val fadeWidth = width * 0.3f
        val fadeStart = width * adjustedProgress - fadeWidth
        val paint = Paint().apply {
            shader = android.graphics.LinearGradient(
                fadeStart, 0f,
                fadeStart + fadeWidth, 0f,
                intArrayOf(
                    0x00FFFFFF,
                    0x66FFFFFF,
                    0xFFFFFFFF.toInt()
                ),
                floatArrayOf(0f, 0.4f, 1f),
                android.graphics.Shader.TileMode.CLAMP
            )
        }
        maskCanvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), paint)

        layerCanvas.drawBitmap(maskBitmap, 0f, 0f, originalPaint)
        maskBitmap.recycle()

        particles.forEach { particle ->
            particle.draw(layerCanvas, this.paint)
        }

        canvas.drawBitmap(layerBitmap, 0f, 0f, Paint())
        layerBitmap.recycle()
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
        var alpha: Int = 0
    ) {
        var isActivated = false
        private var activationTime = 0f
        private val activationThreshold = (srcX.toFloat() / bitmap.width.toFloat()) - 0.02f

        fun update(progress: Float) {
            val splitDelay = 0.15f
            
            if (!isActivated && progress >= activationThreshold) {
                isActivated = true
                activationTime = progress
            }

            if (!isActivated) {
                alpha = 0
                return
            }

            val particleProgress = ((progress - activationTime) / (1 - activationThreshold)).coerceIn(0f, 1f)

            if (particleProgress <= splitDelay) {
                alpha = ((particleProgress / splitDelay) * 255).roundToInt().coerceIn(0, 255)
                return
            }

            val moveProgress = ((particleProgress - splitDelay) / (1 - splitDelay)).coerceIn(0f, 1f)
            val cubicProgress = moveProgress * moveProgress * moveProgress

            val distance = speed * cubicProgress
            x += (distance * cos(Math.toRadians(angle))).toFloat()
            y += (distance * sin(Math.toRadians(angle))).toFloat()

            val fadeStartProgress = 0.5f
            if (moveProgress > fadeStartProgress) {
                val fadeProgress = ((moveProgress - fadeStartProgress) / (1 - fadeStartProgress))
                val smoothFadeProgress = kotlin.math.sqrt(fadeProgress.toDouble()).toFloat()
                alpha = ((1f - smoothFadeProgress) * 255).roundToInt().coerceIn(0, 255)
            }
        }

        fun draw(canvas: Canvas, paint: Paint) {
            if (alpha > 0) {
                paint.alpha = alpha
                canvas.drawBitmap(
                    bitmap,
                    android.graphics.Rect(srcX, srcY, srcX + width, srcY + height),
                    android.graphics.RectF(x, y, x + width, y + height),
                    paint
                )
            }
        }
    }

    private fun View.drawToBitmap(): Bitmap {
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        draw(canvas)
        return bitmap
    }
}



