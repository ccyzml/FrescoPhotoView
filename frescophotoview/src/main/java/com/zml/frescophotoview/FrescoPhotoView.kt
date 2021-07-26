package com.zml.frescophotoview

import android.content.Context
import android.graphics.Canvas
import android.graphics.RectF
import android.graphics.drawable.Animatable
import android.util.AttributeSet
import android.view.GestureDetector
import android.view.GestureDetector.SimpleOnGestureListener
import android.view.MotionEvent
import androidx.core.view.ScrollingView
import com.facebook.drawee.controller.AbstractDraweeController
import com.facebook.drawee.controller.BaseControllerListener
import com.facebook.drawee.controller.ControllerListener
import com.facebook.drawee.drawable.ScalingUtils
import com.facebook.drawee.generic.GenericDraweeHierarchyBuilder
import com.facebook.drawee.generic.GenericDraweeHierarchyInflater
import com.facebook.drawee.interfaces.DraweeController
import com.facebook.drawee.view.GenericDraweeView
import com.zml.frescophotoview.gestures.TransformGestureDetector

open class FrescoPhotoView @JvmOverloads constructor(
    context: Context?,
    attrs: AttributeSet? = null,
    defStyle: Int = 0
) : GenericDraweeView(context, attrs, defStyle), ScrollingView {
    private val imageBounds = RectF()
    private val viewBounds = RectF()
    private var zoomableController: ZoomableControllerImp
    private var gestureDetector: GestureDetector
    private var tapListenerWrapper: GestureListenerWrapper
    private var outerControllerListener: BaseControllerListener<Any>? = null
    private val controllerListener: ControllerListener<Any> =
        object : BaseControllerListener<Any>() {
            override fun onIntermediateImageSet(id: String, imageInfo: Any?) {
                super.onIntermediateImageSet(id, imageInfo)
                outerControllerListener?.onIntermediateImageSet(id, imageInfo)
            }

            override fun onFinalImageSet(
                id: String, imageInfo: Any?, animatable: Animatable?
            ) {
                this@FrescoPhotoView.onFinalImageSet()
                outerControllerListener?.onFinalImageSet(id, imageInfo, animatable)
            }

            override fun onRelease(id: String) {
                this@FrescoPhotoView.onRelease()
                outerControllerListener?.onRelease(id)
            }
        }
    private val mZoomableListener =
        ZoomableController.Listener { invalidate() }

    init {
        zoomableController = createController()
        zoomableController.listener = mZoomableListener
        tapListenerWrapper = GestureListenerWrapper(createGestureHandler())
        gestureDetector = GestureDetector(context, tapListenerWrapper)
        gestureDetector.setIsLongpressEnabled(true)
    }

    override fun inflateHierarchy(context: Context, attrs: AttributeSet?) {
        val resources = context.resources
        val builder = GenericDraweeHierarchyBuilder(resources)
            .setActualImageScaleType(ScalingUtils.ScaleType.FIT_CENTER)
        GenericDraweeHierarchyInflater.updateBuilder(builder, context, attrs)
        aspectRatio = builder.desiredAspectRatio
        hierarchy = builder.build()
    }


    protected open fun createController(): ZoomableControllerImp {
        return ZoomableControllerImp(TransformGestureDetector(), context)
    }

    protected open fun createGestureHandler(): DefaultGestureHandler {
        return DefaultGestureHandler(zoomableController)
    }

    /**
     * Gets the original image bounds, in view-absolute coordinates.
     *
     *
     * The original image bounds are those reported by the hierarchy. The hierarchy itself may
     * apply scaling on its own (e.g. due to scale type) so the reported bounds are not necessarily
     * the same as the actual bitmap dimensions. In other words, the original image bounds correspond
     * to the image bounds within this view when no zoomable transformation is applied, but including
     * the potential scaling of the hierarchy. Having the actual bitmap dimensions abstracted away
     * from this view greatly simplifies implementation because the actual bitmap may change (e.g.
     * when a high-res image arrives and replaces the previously set low-res image). With proper
     * hierarchy scaling (e.g. FIT_CENTER), this underlying change will not affect this view nor the
     * zoomable transformation in any way.
     */
    protected fun getImageBounds(outBounds: RectF?) {
        hierarchy.getActualImageBounds(outBounds)
    }

    /**
     * Gets the bounds used to limit the translation, in view-absolute coordinates.
     *
     *
     * These bounds are passed to the zoomable controller in order to limit the translation. The
     * image is attempted to be centered within the limit bounds if the transformed image is smaller.
     * There will be no empty spaces within the limit bounds if the transformed image is bigger. This
     * applies to each dimension (horizontal and vertical) independently.
     *
     *
     * Unless overridden by a subclass, these bounds are same as the view bounds.
     */
    protected fun getLimitBounds(outBounds: RectF) {
        outBounds[0f, 0f, width.toFloat()] = height.toFloat()
    }


    fun setHorizontalNestedScrollEnabled(enabled: Boolean) {
        zoomableController.isHorizontalNestedScrollEnabled = enabled
    }

    /**
     * Sets the tap listener.
     */
    fun setTapListener(tapListener: SimpleOnGestureListener?) {
        tapListenerWrapper.setListener(tapListener)
    }

    fun setZoomingEnabled(zoomingEnabled: Boolean) {
        zoomableController.isScaleEnabled = zoomingEnabled
    }

    fun setTranslationEnabled(enabled: Boolean) {
        zoomableController.isTranslationEnabled = enabled
    }

    /**
     * Sets the image controller.
     */
    override fun setController(controller: DraweeController?) {
        removeControllerListener(getController())
        addControllerListener(controller)
        super.setController(controller)
    }

    private fun removeControllerListener(controller: DraweeController?) {
        if (controller is AbstractDraweeController<*, *>) {
            controller.removeControllerListener(controllerListener)
        }
    }

    private fun addControllerListener(controller: DraweeController?) {
        if (controller is AbstractDraweeController<*, *>) {
            controller.addControllerListener(controllerListener)
        }
    }

    override fun onDraw(canvas: Canvas) {
        val saveCount = canvas.save()
        canvas.concat(zoomableController.activeTransform)
        super.onDraw(canvas)
        canvas.restoreToCount(saveCount)
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        super.onTouchEvent(event)
        //仅仅是想获取双击，Fling等回调，不关心返回值（默认都返回false)
        gestureDetector.onTouchEvent(event)
        if (zoomableController.onTouchEvent(event)) {
            parent.requestDisallowInterceptTouchEvent(true)
            return true
        } else {
            parent.requestDisallowInterceptTouchEvent(false)
        }
        // None of our components reported that they handled the touch event. Upon returning false
        // from this method, our parent won't send us any more events for this gesture. Unfortunately,
        // some components may have started a delayed action, such as a long-press timer, and since we
        // won't receive an ACTION_UP that would cancel that timer, a false event may be triggered.
        // To prevent that we explicitly send one last cancel event when returning false.
        val cancelEvent = MotionEvent.obtain(event)
        cancelEvent.action = MotionEvent.ACTION_CANCEL
        gestureDetector.onTouchEvent(cancelEvent)
        zoomableController.onTouchEvent(cancelEvent)
        cancelEvent.recycle()
        return false
    }

    override fun canScrollVertically(direction: Int): Boolean {
        return super.canScrollVertically(direction)
    }

    override fun computeHorizontalScrollRange(): Int {
        return zoomableController.computeHorizontalScrollRange()
    }

    override fun computeHorizontalScrollOffset(): Int {
        return zoomableController.computeHorizontalScrollOffset()
    }

    override fun computeHorizontalScrollExtent(): Int {
        return zoomableController.computeHorizontalScrollExtent()
    }

    override fun computeVerticalScrollRange(): Int {
        return zoomableController.computeVerticalScrollRange()
    }

    override fun computeVerticalScrollOffset(): Int {
        return zoomableController.computeVerticalScrollOffset()
    }

    override fun computeVerticalScrollExtent(): Int {
        return zoomableController.computeVerticalScrollExtent()
    }

    override fun computeScroll() {
        zoomableController.computeScroll()
    }

    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        super.onLayout(changed, left, top, right, bottom)
        updateZoomableControllerBounds()
    }

    private fun onFinalImageSet() {
        if (!zoomableController.isEnabled) {
            zoomableController.isEnabled = true
            updateZoomableControllerBounds()
        }
    }

    private fun onRelease() {
        zoomableController.isEnabled = false
    }

    protected fun updateZoomableControllerBounds() {
        getImageBounds(imageBounds)
        getLimitBounds(viewBounds)
        zoomableController.imageBounds = imageBounds
        zoomableController.viewBounds = viewBounds
    }

    fun setControllerListener(listener: BaseControllerListener<Any>?) {
        outerControllerListener = listener
    }

    fun setMaxScaleFactor(maxScale: Float) {
        zoomableController.maxScaleFactor = maxScale
    }

    fun setAnimationDuration(duration: Long) {
        zoomableController.animationDuration = duration
    }

    fun setScaleFactorRetriever(scaleFactorRetriever: ScaleFactorRetriever?) {
        zoomableController.scaleFactorFetcher = scaleFactorRetriever
    }
}