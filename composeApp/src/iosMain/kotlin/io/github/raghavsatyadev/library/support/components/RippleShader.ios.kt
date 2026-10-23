package io.github.raghavsatyadev.library.support.components

import androidx.compose.ui.graphics.RenderEffect
import androidx.compose.ui.graphics.asComposeRenderEffect
import org.jetbrains.skia.ImageFilter
import org.jetbrains.skia.RuntimeEffect
import org.jetbrains.skia.RuntimeShaderBuilder

/** SkSL through Skia. */
internal actual class RippleShader actual constructor(source: String) {
  private val builder by lazy { RuntimeShaderBuilder(RuntimeEffect.makeForShader(source)) }

  actual fun effect(uniforms: RippleUniforms): RenderEffect? {
    val u = uniforms
    builder.uniform("size", u.width, u.height)
    builder.uniform("origin", u.originX, u.originY)
    builder.uniform("progress", u.progress)
    builder.uniform("maxRadius", u.maxRadius)
    builder.uniform("amplitude", u.amplitude)
    builder.uniform("wavelength", u.wavelength)
    builder.uniform(
      "highlight",
      u.highlight.red,
      u.highlight.green,
      u.highlight.blue,
      u.highlight.alpha,
    )
    builder.uniform("highlightStrength", u.highlightStrength)
    return ImageFilter.makeRuntimeShader(builder, "content", null).asComposeRenderEffect()
  }
}
