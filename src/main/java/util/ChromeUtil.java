package util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Class utility for chrome
 *
 * @author daopq
 * @version 1.0
 */
public class ChromeUtil {

    private static final String CMD_CREATE_CHROME = "node %s " +
        " --url=%s " +
        " --viewportWidth=%s " +
        " --viewportHeight=%s " +
        " --format=%s " +
        " --imagePath=%s ";

    /**
     * Default constructor
     */
    private ChromeUtil() {
        // Avoid create ChromeUtil instance
    }

    /**
     * Get random number from lowerBound to highBound
     *
     * @param lowerBound Min value
     * @param highBound  Max value
     *
     * @return Number from lowerBound
     */
    public static int getRandomPortInRange(int lowerBound, int highBound) {
        return ThreadLocalRandom.current().nextInt(lowerBound, highBound + 1);
    }

    /**
     * Open and capture image from Chrome
     *
     * @param nodeJsPath path to nodejs module
     * @param svgPath    path to svg file
     * @param width      image width
     * @param height     image height
     * @param format     image type (png, jpeg)
     * @param outputFile Path to export image file
     */
    public static Process captureHtml(String nodeJsPath, String svgPath,
                                   String width, String height,
                                   String format, String outputFile) throws IOException, InterruptedException {
        Process process = execCommand(
            String.format(CMD_CREATE_CHROME, nodeJsPath, svgPath, width, height, format, outputFile));

        // Write log execute
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
            String line;
            while ((line = reader.readLine()) != null) {
                System.out.println(line);
            }
        }
        try (BufferedReader bre = new BufferedReader(new InputStreamReader(process.getErrorStream())) ) {
            String line;
            boolean hasError = false;
            while ((line = bre.readLine()) != null) {
                System.err.println(line);
                hasError = true;
            }
            if (hasError) {
                throw new InterruptedException();
            }
        }

        // Get return value
        int exitCode = process.waitFor();
        if (exitCode != 0) {
            throw new InterruptedException();
        }

        return process;
    }

    /**
     * Exec windows command
     *
     * @param command Command to execute
     *
     * @return Process object
     * @throws IOException Throw when IO error
     */
    private static Process execCommand(String command) throws IOException {
        ProcessBuilder processBuilder = new ProcessBuilder();
        processBuilder.command("cmd.exe", "/c", command);
        return processBuilder.start();
    }
}
