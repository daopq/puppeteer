package util;

import com.sun.imageio.plugins.jpeg.JPEGImageWriter;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.ImageTypeSpecifier;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.metadata.IIOInvalidTreeException;
import javax.imageio.metadata.IIOMetadata;
import javax.imageio.metadata.IIOMetadataFormatImpl;
import javax.imageio.metadata.IIOMetadataNode;
import javax.imageio.plugins.jpeg.JPEGImageWriteParam;
import javax.imageio.stream.ImageInputStream;
import javax.imageio.stream.ImageOutputStream;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Iterator;

public class ImageUtil {
    private static final String FILE_PATH300 = "C:\\Users\\daopq\\Downloads\\イメージファイル比較（インポートファイルと、xStraBPMから出力したイメージファイル（焼き込みあり）の比較）\\sample_form 300.jpg";
    private static final String FILE_PNG = "C:\\Users\\daopq\\Downloads\\イメージファイル比較（インポートファイルと、xStraBPMから出力したイメージファイル（焼き込みあり）の比較）\\78.png";
    private static final double PIXELPERMM = 25.4;
    private static final double INCH_2_CM = 2.54;
    private static final String OUT_PATH = "C:\\Users\\daopq\\Downloads\\イメージファイル比較（インポートファイルと、xStraBPMから出力したイメージファイル（焼き込みあり）の比較）\\";

    public static void main(String[] args) throws Exception {

        writeBufferedImage(FILE_PNG, -1, 144, OUT_PATH);
        File input = new File(OUT_PATH + "output.png");

        ImageInputStream stream = ImageIO.createImageInputStream(input);
        Iterator<ImageReader> readers = ImageIO.getImageReaders(stream);

        if (readers.hasNext()) {
            ImageReader reader = readers.next();
            reader.setInput(stream);

            IIOMetadata metadata = reader.getImageMetadata(0);
            IIOMetadataNode standardTree = (IIOMetadataNode) metadata.getAsTree(IIOMetadataFormatImpl.standardMetadataFormatName);
            IIOMetadataNode dimension = (IIOMetadataNode) standardTree.getElementsByTagName("Dimension").item(0);
            float horizontalPixelSizeMM = getPixelSizeMM(dimension, "HorizontalPixelSize");
            float verticalPixelSizeMM = getPixelSizeMM(dimension, "VerticalPixelSize");

            // TODO: Convert pixelsPerMM to DPI left as an exercise to the reader.. ;-)

            System.err.println("horizontalPixelSizeMM: " + horizontalPixelSizeMM);
            System.err.println("verticalPixelSizeMM: " + verticalPixelSizeMM);


            System.err.println("dpi: " + Math.round(PIXELPERMM / horizontalPixelSizeMM));
            System.err.println("dpi: " + Math.round(PIXELPERMM / verticalPixelSizeMM));
        } else {
            System.err.printf("Could not read %s\n", input);
        }
    }

    private static float getPixelSizeMM(final IIOMetadataNode dimension, final String elementName) {
        // NOTE: The standard metadata format has defined dimension to pixels per millimeters, not DPI...
        NodeList pixelSizes = dimension.getElementsByTagName(elementName);
        IIOMetadataNode pixelSize = pixelSizes.getLength() > 0 ? (IIOMetadataNode) pixelSizes.item(0) : null;
        return pixelSize != null ? Float.parseFloat(pixelSize.getAttribute("value")) : -1;
    }

    private static void setDPI(IIOMetadata metadata, long dpiWidth, long dpiHeigh) throws IIOInvalidTreeException {

        double horizontalPixelSizeMM = 1.0 * dpiWidth / 10 / INCH_2_CM;
        double verticalPixelSizeMM = 1.0 * dpiWidth / 10 / INCH_2_CM;

        IIOMetadataNode horiz = new IIOMetadataNode("HorizontalPixelSize");
        horiz.setAttribute("value", Double.toString(horizontalPixelSizeMM));

        IIOMetadataNode vert = new IIOMetadataNode("VerticalPixelSize");
        vert.setAttribute("value", Double.toString(verticalPixelSizeMM));

        IIOMetadataNode dim = new IIOMetadataNode("Dimension");
        dim.appendChild(horiz);
        dim.appendChild(vert);

        IIOMetadataNode root = new IIOMetadataNode("javax_imageio_1.0");
        root.appendChild(dim);

        metadata.mergeTree("javax_imageio_1.0", root);
    }

    private static void saveDpi2Png(BufferedImage gridImageInput, String output) throws IOException {
        final String formatName = "png";

        for (Iterator<ImageWriter> iw = ImageIO.getImageWritersByFormatName(formatName); iw.hasNext(); ) {
            ImageWriter writer = iw.next();
            ImageWriteParam writeParam = writer.getDefaultWriteParam();
            ImageTypeSpecifier typeSpecifier = ImageTypeSpecifier.createFromBufferedImageType(BufferedImage.TYPE_INT_RGB);
            IIOMetadata metadata = writer.getDefaultImageMetadata(typeSpecifier, writeParam);
            if (metadata.isReadOnly() || !metadata.isStandardMetadataFormatSupported()) {
                continue;
            }

            setDPI(metadata, 144, 144);

            final ImageOutputStream stream = ImageIO.createImageOutputStream(new File(output + "output.png"));
            try {
                writer.setOutput(stream);
                /*Image newResizedImage = gridImage
                    .getScaledInstance(gridImage.getWidth(), gridImage.getHeight(), Image.SCALE_SMOOTH);*/

                writer.write(metadata, new IIOImage(gridImageInput, null, metadata), writeParam);
            } finally {
                stream.close();
            }
            break;
        }
    }

    public static void writeBufferedImage(String originalFilePath, float quality, int dpi, String targetFile) throws Exception {
        File inputFile = new File(originalFilePath);
        String fileType = getImageType(inputFile);
        BufferedImage bufferedImage = ImageIO.read(new File(FILE_PATH300));
        if (IMAGETYPE.JPG.isJpeg(fileType)) {
            saveDpi2Jpeg(bufferedImage, quality, dpi, targetFile);
        } else {
            saveDpi2Png(bufferedImage, targetFile);
        }
    }


    private static void saveDpi2Jpeg(BufferedImage bufferedImage, float quality, int dpi, String targetPath) throws IOException {
        JPEGImageWriter imageWriter = (JPEGImageWriter) ImageIO.getImageWritersBySuffix(IMAGETYPE.JPEG.getValue()).next();
        ImageWriteParam writeParam = imageWriter.getDefaultWriteParam();
        ImageTypeSpecifier typeSpecifier = ImageTypeSpecifier.createFromBufferedImageType(BufferedImage.TYPE_INT_RGB);
        IIOMetadata metadata = imageWriter.getDefaultImageMetadata(typeSpecifier, writeParam);

        Element tree = (Element) metadata.getAsTree("javax_imageio_jpeg_image_1.0");
        Element jfif = (Element) tree.getElementsByTagName("app0JFIF").item(0);
        jfif.setAttribute("Xdensity", Integer.toString(dpi));
        jfif.setAttribute("Ydensity", Integer.toString(dpi));
        jfif.setAttribute("resUnits", "1");
        metadata.setFromTree("javax_imageio_jpeg_image_1.0", tree);

        if (quality >= 0 && quality <= 1f) {

            JPEGImageWriteParam jpegParams = (JPEGImageWriteParam) imageWriter.getDefaultWriteParam();
            jpegParams.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
            jpegParams.setCompressionQuality(quality);

        }

        FileOutputStream os = new FileOutputStream(new File(targetPath + "output.jpeg"));

        try (ImageOutputStream stream = ImageIO.createImageOutputStream(os)) {
            imageWriter.setOutput(stream);
            imageWriter.write(metadata, new IIOImage(bufferedImage, null, metadata), writeParam);
        }
    }

    private static String getImageType(File inputFile) throws IOException {
        ImageInputStream iis = ImageIO.createImageInputStream(inputFile);
        Iterator<ImageReader> imageReaders = ImageIO.getImageReaders(iis);
        if (imageReaders.hasNext()) {
            ImageReader reader = imageReaders.next();
            return reader != null ? reader.getFormatName() : null;
        }
        return null;
    }

    enum IMAGETYPE {
        JPEG("jpeg"),
        JPG("jpg"),
        PNG("png");

        private String value;

        IMAGETYPE(String value) {
            this.value = value;
        }

        public String getValue() {
            return value;
        }

        public boolean isJpeg(String value) {
            return IMAGETYPE.JPEG.getValue().equalsIgnoreCase(value)
                || IMAGETYPE.JPG.getValue().equalsIgnoreCase(value);
        }

        public boolean isPng(String value) {
            return IMAGETYPE.PNG.getValue().equalsIgnoreCase(value);
        }
    }
}
