package stage3

import java.awt.Color
import java.awt.image.BufferedImage
import java.io.File
import javax.imageio.ImageIO
import kotlin.math.sqrt

fun main(args: Array<String>) {
    val (input, output) = parseArgs(args)

    val image = ImageIO.read(File(input))

    val energies = Array(image.width) { Array(image.height) { 0.0 } }

    for (x in 0 until image.width) {
        for (y in 0 until image.height) {
            energies[x][y] = calculateEnergy(image, x, y)
        }
    }

    val maxEnergy = energies.map { it.maxOrNull()!! }.maxOrNull()!!

    for (w in 0 until image.width) {
        for (h in 0 until image.height) {
            val energy = energies[w][h]
            val intensity = (255.0 * energy / maxEnergy).toInt()
            val alpha = Color(image.getRGB(w, h)).alpha
            image.setRGB(w, h, Color(intensity, intensity, intensity, alpha).rgb)
        }
    }

    ImageIO.write(image, "png", File(output))
}

private fun calculateEnergy(image: BufferedImage, x: Int, y: Int): Double {
    val posX = when (x) {
        0 -> 1
        image.width - 1 -> image.width - 2
        else -> x
    }
    val posY = when (y) {
        0 -> 1
        image.height - 1 -> image.height - 2
        else -> y
    }

    val westPixel = Color(image.getRGB(posX - 1, y))
    val eastPixel = Color(image.getRGB(posX + 1, y))
    val xDiffSquared = diffSquared(westPixel, eastPixel)

    val northPixel = Color(image.getRGB(x, posY - 1))
    val southPixel = Color(image.getRGB(x, posY + 1))
    val yDiffSquared = diffSquared(northPixel, southPixel)

    return sqrt(xDiffSquared + yDiffSquared)
}

fun diffSquared(c1: Color, c2: Color): Double {
    val red = c1.red - c2.red
    val green = c1.green - c2.green
    val blue = c1.blue - c2.blue
    return (red * red + green * green + blue * blue).toDouble()
}

fun parseArgs(args: Array<String>): Pair<String?, String?> {
    if (args.size !in listOf(4)) {
        throw Exception("usage: -in <input image> -out <output image>")
    }

    var inputFilename = ""
    var outputFilename = ""

    for (arg in args.asList().chunked(2)) {
        val paramKey = arg[0]
        val paramValue = arg[1]

        when (paramKey) {
            "-in" -> inputFilename = paramValue
            "-out" -> outputFilename = paramValue
        }
    }

    return Pair(inputFilename, outputFilename)
}