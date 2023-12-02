/**
 * RWCompression
 */
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;
public class RWCompression {
    public static int height;
    public static int width;
    public static int[][] ReadImage(String Path){
        BufferedImage image;
        try {
            image=ImageIO.read(new File(Path));
            height=image.getHeight();
            width=image.getWidth();
            System.out.println(height);
            System.out.println(width);
            int[][] imagePixels=new int[height][width];
            for (int i = 0; i < height; i++) {
                for (int j = 0; j < width; j++) {
                    int pixel=image.getRGB(j, i);
                    int red=(pixel & 0xff0000) >> 16;
                    int green=(pixel & 0x00ff00) >> 8;
                    int blue=(pixel & 0x0000ff);
                    int grayScale = Math.max(green,Math.max(red, blue));
                    imagePixels[i][j]=grayScale;
                }
            }
            return imagePixels;
        } catch (IOException e) {
           System.out.println(e.getMessage());
        }
        return null;
        
    }
}