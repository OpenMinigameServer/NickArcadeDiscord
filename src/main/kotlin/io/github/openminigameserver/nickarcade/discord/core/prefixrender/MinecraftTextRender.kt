package io.github.openminigameserver.nickarcade.discord.core.prefixrender

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.TextComponent
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.TextColor
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer
import net.kyori.adventure.text.serializer.plain.PlainComponentSerializer
import net.kyori.adventure.util.HSVLike
import org.bukkit.map.MinecraftFont
import java.awt.Color
import java.awt.Graphics2D
import java.awt.image.BufferedImage
import java.io.ByteArrayOutputStream
import javax.imageio.ImageIO
import kotlin.math.ceil

object MinecraftTextRender {

    fun renderTextToImage(text: String): Array<ByteArray> {
        val textToRender =
            LegacyComponentSerializer.legacySection().deserialize(text)
        val font = MinecraftFont.Font
        val scale = 7
        val widthTile = 64
        var maxWidth = (font.getWidth(PlainComponentSerializer.plain().serialize(textToRender)) + 1) * scale
        if (maxWidth.rem(widthTile) != 0) {
            maxWidth = ceil(maxWidth / widthTile.toFloat()).toInt() * widthTile
        }
        val maxHeight = (font.height + 1) * scale

        val output = mutableListOf<ByteArray>()
        BufferedImage(maxWidth, maxHeight, BufferedImage.TYPE_INT_ARGB).apply {
            val g = this.createGraphics()

            drawText(0, 0, textToRender, g, font, scale)

            repeat(ceil(maxWidth / widthTile.toFloat()).toInt()) { tileId ->
                val x = widthTile * tileId
                var finalTileWidth = widthTile
                if (x + finalTileWidth > maxWidth)
                    finalTileWidth = maxWidth - x

                output += ByteArrayOutputStream().use {
                    ImageIO.write(getSubimage(x, 0, finalTileWidth, maxHeight), "png", it)
                    it.toByteArray()
                }
            }
        }
        return output.toTypedArray()
    }

    private fun drawText(startX: Int, startY: Int, text: TextComponent, g: Graphics2D, font: MinecraftFont, scale: Int) {
        val components = text.flatten
        var x = startX
        val y = startY
        components.forEach { textComponent ->
            if (textComponent is TextComponent) {
                textComponent.content().forEachIndexed { index, char ->
                    val sprite = font.getChar(char) ?: font.getChar(' ')!!
                    for (r in 0 until font.height) {
                        for (c in 0 until sprite.width) {
                            if (sprite[r, c]) {
                                g.setPixel(scale, x + c, y + r, (textComponent.color() ?: NamedTextColor.WHITE).value())
                                g.setPixel(
                                    scale,
                                    x + c + 1,
                                    y + r + 1,
                                    (textComponent.color() ?: NamedTextColor.WHITE).asHSV()
                                        .let { TextColor.color(HSVLike.of(it.h(), it.s(), 0.25f)) }.value()
                                )
                            }
                        }
                    }

                    x += sprite.
                    width + 1
                }
            }
        }
    }

    private fun Graphics2D.setPixel(scale: Int, x: Int, y: Int, color: Int) {
        setColor(Color(color))
        fillRect(x * scale, y * scale, scale, scale)
    }

    private val Component.flatten: List<Component>
        get() = listOf(this, *children().flatMap { it.flatten }.toTypedArray())
}
