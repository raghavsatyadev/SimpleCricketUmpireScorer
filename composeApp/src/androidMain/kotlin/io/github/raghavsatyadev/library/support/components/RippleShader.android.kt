package io.github.raghavsatyadev.library.support.components

import android.graphics.RuntimeShader
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.ui.graphics.RenderEffect
import androidx.compose.ui.graphics.asComposeRenderEffect
import androidx.compose.ui.graphics.toArgb

/** AGSL on Android 13+; no ripple before that. */
internal actual class RippleShader actual constructor(private val source: String) {
  private val shader: Any? by lazy {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) RuntimeShader(source) else null
  }

  actual fun effect(uniforms: RippleUniforms): RenderEffect? {
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) return null
    return effect33(shader as RuntimeShader, uniforms)
  }

  @RequiresApi(Build.VERSION_CODES.TIRAMISU)
  private fun effect33(shader: RuntimeShader, u: RippleUniforms): RenderEffect {
    shader.setFloatUniform("size", u.width, u.height)
    shader.setFloatUniform("origin", u.originX, u.originY)
    shader.setFloatUniform("progress", u.progress)
    shader.setFloatUniform("maxRadius", u.maxRadius)
    shader.setFloatUniform("amplitude", u.amplitude)
    shader.setFloatUniform("wavelength", u.wavelength)
    shader.setColorUniform("highlight", u.highlight.toArgb())
    shader.setFloatUniform("highlightStrength", u.highlightStrength)
    return android.graphics.RenderEffect.createRuntimeShaderEffect(shader, "content")
      .asComposeRenderEffect()
  }
}
