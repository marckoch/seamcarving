package stage5

import java.awt.Color
import java.awt.image.BufferedImage
import java.io.File
import javax.imageio.ImageIO
import kotlin.math.sqrt

fun main(args: Array<String>) {
    val (input, output) = parseArgs(args)

    val image = ImageIO.read(File(input)).transpose()

    val seam = SeamCarving().findSeam(image)

    colorSeam(image, seam)

    ImageIO.write(image.transpose(), "png", File(output))
}

fun colorSeam(image: BufferedImage, seam: List<Pair<Int, Int>>) {
    for ((x, y) in seam) {
        image.setRGB(x, y, Color.RED.rgb)
    }
}

fun BufferedImage.transpose(): BufferedImage {
    val transposedImage = BufferedImage(this.height, this.width, this.type)
    for (x in 0 until this.width) {
        for (y in 0 until this.height) {
            transposedImage.setRGB(y, x, this.getRGB(x, y))
        }
    }
    return transposedImage
}

class SeamCarving {
    fun findSeam(image: BufferedImage): List<Pair<Int, Int>> {
        val energies = Array(image.width) { Array(image.height) { 0.0 } }

        // fill energy matrix
        for (w in 0 until image.width) {
            for (h in 0 until image.height) {
                energies[w][h] = calculateEnergy(image, w, h)
            }
        }

        val maxEnergy = energies.map { it.maxOrNull()!! }.maxOrNull()!!

        // replace each image pixel with normalized energy value (grey -> red, green, blue are all same value)
        for (w in 0 until image.width) {
            for (h in 0 until image.height) {
                val energy = energies[w][h]
                val intensity = (255.0 * energy / maxEnergy).toInt()
                val alpha = Color(image.getRGB(w, h)).alpha
                image.setRGB(w, h, Color(intensity, intensity, intensity, alpha).rgb)
            }
        }

        return findMinSumPathInEnergyMatrix(energies, image)
    }

    private fun findMinSumPathInEnergyMatrix(energies: Array<Array<Double>>, image: BufferedImage): List<Pair<Int, Int>> {
        val width = image.width
        val height = image.height

        val sumMatrix = Array(width) { Array(height) { 0.0 } }

        // fill sumMatrix from top to bottom
        // each cell is sum of its energy and minimum energy of three neighbors in row above (up left, left, up right)
        // https://en.m.wikipedia.org/wiki/Seam_carving#Dynamic_programming
        for (y in 0 until height) {
            for (x in 0 until width) {
                if (y == 0) {
                    // top row has no row above it, so will be initialized with energy data
                    sumMatrix[x][y] = energies[x][y]
                } else {
                    val neighbors = getUpstairNeighbors(sumMatrix, x, y)
                    sumMatrix[x][y] = energies[x][y] + minOf(Double.MAX_VALUE, *neighbors)
                }
            }
        }

        val seam = emptyList<Pair<Int, Int>>().toMutableList()

        // find minimum in last row and ...
        var minimumEnergy = Double.MAX_VALUE
        var xOfMinimum = 0
        for (x in 0 until width) {
            if (sumMatrix[x][height - 1] < minimumEnergy) {
                minimumEnergy = sumMatrix[x][height - 1]
                xOfMinimum = x
            }
        }
        seam.add(Pair(xOfMinimum, height - 1))

        // ... step back up each row, only up+left, up, up+right, always choose the minimum
        for (y in height - 1 downTo 0) {
            val neighbors = getUpstairNeighbors(sumMatrix, xOfMinimum, y + 1)

            val min = minOf(Double.MAX_VALUE, *neighbors)

            xOfMinimum += when (min) {
                neighbors[0] -> -1 // minimum was up left
                neighbors[2] -> +1 // minimum was up right
                else -> 0 // minimum was directly above
            }

            seam.add(Pair(xOfMinimum, y))
        }

        return seam
    }

    // get energy of three fields above field (w/h)
    // returns array with three values of top left, top, top right
    private fun getUpstairNeighbors(sumMatrix: Array<Array<Double>>, x: Int, y: Int): Array<Double> {
        val rightBorder = sumMatrix.size
        return arrayOf(
            if (x == 0) Double.MAX_VALUE else sumMatrix[x - 1][y - 1],
            sumMatrix[x][y - 1],
            if (x == rightBorder - 1) Double.MAX_VALUE else sumMatrix[x + 1][y - 1]
        )
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

    private fun diffSquared(c1: Color, c2: Color): Double {
        val red = c1.red - c2.red
        val green = c1.green - c2.green
        val blue = c1.blue - c2.blue
        return (red * red + green * green + blue * blue).toDouble()
    }
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