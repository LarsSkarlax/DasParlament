package com.lrs.dasparlament

import android.content.Context
import android.graphics.Canvas
import android.graphics.Matrix
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.util.TypedValue
import androidx.appcompat.widget.AppCompatImageView

class TopOffsetCropImageView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyle: Int = 0
) : AppCompatImageView(context, attrs, defStyle) {

    var offsetDp: Float = 50f

    private var matrixNeedsUpdate: Boolean = true

    private fun updateMatrixIfPossible() {
        val drawable = drawable ?: return
        val viewWidth = width.toFloat()
        val viewHeight = height.toFloat()
        val drawableWidth = drawable.intrinsicWidth.toFloat()
        val drawableHeight = drawable.intrinsicHeight.toFloat()
        if (drawableWidth <= 0f || drawableHeight <= 0f || viewWidth <= 0f || viewHeight <= 0f) return

        val scale = viewWidth / drawableWidth
        val offsetPx = TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            offsetDp,
            resources.displayMetrics
        )
        val maxOffset = (drawableHeight * scale) - viewHeight
        val actualOffset = if (offsetPx < maxOffset) offsetPx else maxOffset.coerceAtLeast(0f)

        val matrix = Matrix()
        matrix.setScale(scale, scale)
        matrix.postTranslate(0f, -actualOffset)
        imageMatrix = matrix
        scaleType = ScaleType.MATRIX
        matrixNeedsUpdate = false
    }

    override fun setImageDrawable(drawable: Drawable?) {
        super.setImageDrawable(drawable)
        matrixNeedsUpdate = true
        invalidate()
    }

    override fun setImageBitmap(bm: android.graphics.Bitmap?) {
        super.setImageBitmap(bm)
        matrixNeedsUpdate = true
        invalidate()
    }

    override fun setImageResource(resId: Int) {
        super.setImageResource(resId)
        matrixNeedsUpdate = true
        invalidate()
    }

    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        super.onLayout(changed, left, top, right, bottom)
        matrixNeedsUpdate = true
    }

    override fun onDraw(canvas: Canvas) {
        if (matrixNeedsUpdate) {
            updateMatrixIfPossible()
        }
        super.onDraw(canvas)
    }

    /**
     * If you use an image loader like Glide, call this from the onResourceReady callback!
     * Example for Glide:
     * .into(object : CustomTarget<Drawable>() {
     *    override fun onResourceReady(resource: Drawable, ...) {
     *        imageView.setImageDrawable(resource)
     *        imageView.invalidateMatrix()
     *    }
     * })
     */
    fun invalidateMatrix() {
        matrixNeedsUpdate = true
        invalidate()
    }
}