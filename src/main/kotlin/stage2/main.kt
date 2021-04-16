package stage2

import java.awt.Color
import java.io.File
import javax.imageio.ImageIO

fun main(args: Array<String>) {
    val (input, output) = parseArgs(args)

    val image = ImageIO.read(File(input))

    for (w in 0 until image.width) {
        for (h in 0 until image.height) {
            val color = Color(image.getRGB(w, h))
            val invertedColor = Color(255 - color.red, 255 - color.green, 255 - color.blue)
            image.setRGB(w, h, invertedColor.rgb)
        }
    }

    ImageIO.write(image, "png", File(output))
}

fun parseArgs(args: Array<String>): Pair<String?, String?> {
    if (args.size !in listOf(4)) {
        return Pair(null, null)
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