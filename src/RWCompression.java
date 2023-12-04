import java.awt.image.BufferedImage;

public class RWCompression {
    public static int height;
    public static int width;

    public static int[][][] ReadImage(BufferedImage image) {
        height = image.getHeight();
        width = image.getWidth();
        int[][][] imagePixels = new int[height][width][3];
        for (int i = 0; i < height; i++) {
            for (int j = 0; j < width; j++) {
                int pixel = image.getRGB(j, i);
                int red = (pixel & 0xff0000) >> 16;
                int green = (pixel & 0x00ff00) >> 8;
                int blue = (pixel & 0x0000ff);
                imagePixels[i][j][0] = red;
                imagePixels[i][j][1] = green;
                imagePixels[i][j][2] = blue;
            }
        }
        return imagePixels;
    }
}