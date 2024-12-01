package io.github.raghavsatyadev.support.extensions

import android.graphics.drawable.Drawable
import android.widget.ImageView
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import coil3.ImageLoader
import coil3.imageLoader
import coil3.request.CachePolicy
import coil3.request.ErrorResult
import coil3.request.ImageRequest
import coil3.request.SuccessResult
import coil3.request.crossfade
import coil3.request.fallback
import coil3.request.placeholder
import coil3.request.target
import io.github.raghavsatyadev.support.extensions.ResourceExtensions.getConDrawable

@Suppress("unused")
object ImageExtensions {
    fun ImageView.loadImage(
        url: String?,
        placeHolder: Drawable? = null,
        crossFade: Boolean = false,
        error: Drawable? = null,
        cacheAllowed: Boolean = true,
        listener: (() -> Unit)? = null,
        errorListener: ((Throwable) -> Unit)? = null,
    ) {
        val pair =
            getImageLoaderBuilder(
                url = url,
                placeHolderImage = placeHolder,
                errorImage = error,
                crossFade = crossFade,
                cacheAllowed = cacheAllowed,
                errorListener = errorListener,
                listener = listener
            )

        pair.first.enqueue(pair.second.build())
    }

    private fun ImageView.getImageLoaderBuilder(
        url: String?,
        placeHolderImage: Drawable?,
        errorImage: Drawable?,
        crossFade: Boolean,
        cacheAllowed: Boolean = true,
        errorListener: ((Throwable) -> Unit)?,
        listener: (() -> Unit)?,
    ): Pair<ImageLoader, ImageRequest.Builder> {
        this.let {
            val imageLoader = context.imageLoader

            val builder = with(ImageRequest.Builder(context)) {
                data(url)
                target(it)
                placeHolderImage?.let { placeholder(placeHolderImage).fallback(placeHolderImage) }
                errorImage?.let { error(errorImage) }
                if (!cacheAllowed) memoryCachePolicy(CachePolicy.DISABLED)
                listener(object : ImageRequest.Listener {
                    override fun onError(request: ImageRequest, result: ErrorResult) {
                        super.onError(request, result)
                        errorListener?.invoke(result.throwable)
                    }

                    override fun onSuccess(request: ImageRequest, result: SuccessResult) {
                        super.onSuccess(request, result)
                        listener?.invoke()
                    }
                })
                crossfade(crossFade)
            }

            return Pair(imageLoader, builder)
        }
    }

    fun ImageView.loadImage(
        url: String?,
        @DrawableRes
        placeHolderRes: Int? = null,
        @DrawableRes
        errorRes: Int? = null,
        listener: (() -> Unit)? = null,
        errorListener: ((Throwable) -> Unit)? = null,
    ) {
        loadImage(
            url = url,
            placeHolder = context.getConDrawable(placeHolderRes),
            error = context.getConDrawable(errorRes),
            listener = listener,
            errorListener = errorListener
        )
    }

    fun ImageView.loadImage(
        url: String?,
        placeHolder: Drawable? = null,
        @DrawableRes
        errorRes: Int? = null,
        listener: (() -> Unit)? = null,
        errorListener: ((Throwable) -> Unit)? = null,
    ) {
        loadImage(
            url = url,
            placeHolder = placeHolder,
            error = context.getConDrawable(errorRes),
            listener = listener,
            errorListener = errorListener
        )
    }

    fun ImageView.loadImage(
        url: String?,
        @DrawableRes
        placeHolderRes: Int? = null,
        error: Drawable? = null,
        listener: (() -> Unit)? = null,
        errorListener: ((Throwable) -> Unit)? = null,
    ) {
        loadImage(
            url = url,
            placeHolder = context.getConDrawable(placeHolderRes),
            error = error,
            listener = listener,
            errorListener = errorListener
        )
    }

    fun ImageView.loadImage(
        @StringRes
        urlRes: Int,
        placeHolder: Drawable? = null,
        error: Drawable? = null,
        listener: (() -> Unit)? = null,
        errorListener: ((Throwable) -> Unit)? = null,
    ) {
        loadImage(
            url = context.getString(urlRes),
            placeHolder = placeHolder,
            error = error,
            listener = listener,
            errorListener = errorListener,
        )
    }

    fun ImageView.loadImage(
        @StringRes
        urlRes: Int,
        @DrawableRes
        placeHolderRes: Int? = null,
        @DrawableRes
        errorRes: Int? = null,
        listener: (() -> Unit)? = null,
        errorListener: ((Throwable) -> Unit)? = null,
    ) {
        loadImage(
            urlRes = urlRes,
            placeHolder = context.getConDrawable(placeHolderRes),
            error = context.getConDrawable(errorRes),
            listener = listener, errorListener = errorListener
        )
    }

    fun ImageView.loadImage(
        @StringRes
        urlRes: Int,
        placeHolder: Drawable? = null,
        @DrawableRes
        errorRes: Int? = null,
        listener: (() -> Unit)? = null,
        errorListener: ((Throwable) -> Unit)? = null,
    ) {
        loadImage(
            urlRes = urlRes,
            placeHolder = placeHolder,
            error = context.getConDrawable(errorRes),
            listener = listener,
            errorListener = errorListener
        )
    }

    fun ImageView.loadImage(
        @StringRes
        urlRes: Int,
        @DrawableRes
        placeHolderRes: Int? = null,
        error: Drawable? = null,
        listener: (() -> Unit)? = null,
        errorListener: ((Throwable) -> Unit)? = null,
    ) {
        loadImage(
            urlRes = urlRes,
            placeHolder = context.getConDrawable(placeHolderRes),
            error = error,
            listener = listener,
            errorListener = errorListener
        )
    }
}