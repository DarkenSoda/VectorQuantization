import java.awt.image.BufferedImage;

public class RWCompression {
    public static int height;
    public static int width;

    public static int[][] ReadImage(BufferedImage image) {
        height = image.getHeight();
        width = image.getWidth();
        int[][] imagePixels = new int[height][width];
        for (int i = 0; i < height; i++) {
            for (int j = 0; j < width; j++) {
                int pixel = image.getRGB(j, i);
                int red = (pixel & 0xff0000) >> 16;
                int green = (pixel & 0x00ff00) >> 8;
                int blue = (pixel & 0x0000ff);
                int grayScale = Math.max(green, Math.max(red, blue));
                imagePixels[i][j] = grayScale;
            }
        }
        return imagePixels;
    }
}