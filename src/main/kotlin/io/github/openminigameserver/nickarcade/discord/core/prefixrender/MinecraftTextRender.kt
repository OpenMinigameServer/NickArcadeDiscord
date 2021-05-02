package io.github.openminigameserver.nickarcade.discord.core.prefixrender

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.TextComponent
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.TextColor
import net.kyori.adventure.text.format.TextDecoration
import net.kyori.adventure.text.serializer.plain.PlainComponentSerializer
import net.kyori.adventure.util.HSVLike
import org.bukkit.map.MinecraftFont
import java.awt.Color
import java.awt.Graphics2D
import java.awt.image.BufferedImage
import java.io.ByteArrayOutputStream
import javax.imageio.ImageIO

object MinecraftTextRender {

    fun renderComponentToImage(text: Component, scale: Int = 7, padding: Int = 0): ByteArray {
        val font = EmoteableMinecraftFont
        val plainText = PlainComponentSerializer.plain().serialize(text)

        val maxWidth = (measureTextWidth(font, text) * scale) + (padding * 2 * scale)
        val maxHeight = (((font.height) * scale) * (plainText.count { it == '\n' } + 1)) + (padding * 2 * scale)

        BufferedImage(maxWidth, maxHeight, BufferedImage.TYPE_INT_ARGB).apply {
            val g = this.createGraphics()
            drawText(padding, padding, text, g, font, scale)

            return ByteArrayOutputStream().use {
                ImageIO.write(this, "png", it)
                it.toByteArray()
            }
        }
    }

    private fun measureTextWidth(
        font: MinecraftFont,
        text: Component,
    ): Int {
        val components = text.flatten
        var x = 0
        val xList = mutableListOf<Int>()
        components.forEach { textComponent ->
            if (textComponent is TextComponent) {
                textComponent.content().forEachIndexed { index, char ->
                    if (char == '\n') {
                        xList += x
                        x = 0
                        return@forEachIndexed
                    }
                    val isBoldChar = textComponent.style().hasDecoration(TextDecoration.BOLD)
                    val sprite = font.getChar(char) ?: font.getChar(' ')!!
                    if (isBoldChar)
                        x += 1

                    x += sprite.width + 1
                }
            }
        }
        xList += x
        return xList.maxOrNull()!!
    }

    private fun drawText(startX: Int, startY: Int, text: Component, g: Graphics2D, font: MinecraftFont, scale: Int) {
        val components = text.flatten
        var x = startX
        var y = startY
        components.forEach { textComponent ->
            if (textComponent is TextComponent) {
                textComponent.content().forEachIndexed { index, char ->
                    if (char == '\n') {
                        x = startX
                        y += font.height + 1
                        return@forEachIndexed
                    }
                    val isBoldChar = textComponent.style().hasDecoration(TextDecoration.BOLD)
                    val sprite = font.getChar(char) ?: font.getChar(' ')!!
                    for (pxlY in 0 until font.height) {
                        for (pxlX in 0 until sprite.width) {
                            if (sprite[pxlY, pxlX]) {
                                renderComponentPixel(g, scale, x, y, pxlX, pxlY, textComponent)
                                if (isBoldChar) {
                                    renderComponentPixel(g, scale, x + 1, y, pxlX, pxlY, textComponent)
                                }
                            }
                        }
                    }
                    if (isBoldChar)
                        x += 1

                    x += sprite.width + 1
                }
            }
        }
    }

    private fun renderComponentPixel(
        g: Graphics2D,
        scale: Int,
        x: Int,
        y: Int,
        pxlX: Int,
        pxlY: Int,
        textComponent: Component
    ) {
        g.setPixel(
            scale,
            x + pxlX + 1,
            y + pxlY + 1,
            (textComponent.color() ?: NamedTextColor.WHITE).asHSV()
                .let { TextColor.color(HSVLike.of(it.h(), it.s(), 0.25f)) }.value()
        )
        g.setPixel(scale, x + pxlX, y + pxlY, (textComponent.color() ?: NamedTextColor.WHITE).value())
    }

    private fun Graphics2D.setPixel(scale: Int, x: Int, y: Int, color: Int) {
        setColor(Color(color))
        fillRect(x * scale, y * scale, scale, scale)
    }

    private val Component.flatten: List<Component>
        get() = listOf(this, *children().flatMap { it.flatten }.toTypedArray())
}
