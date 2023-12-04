import java.awt.image.BufferedImage;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

public class VectorQuantization {
    public static void Compress(BufferedImage image, int codeBookSize, int vectorHeight, int vectorWidth,
            File outputFile) {
        int[][] originalImage = RWCompression.ReadImage(image);
        int originalHeight = RWCompression.height;
        int originalWidth = RWCompression.width;
        int scaledHeight = originalHeight;
        int scaledWidth = originalWidth;
        boolean scaled = false;
        // padding
        if (originalHeight % vectorHeight != 0) {
            scaled = true;
            scaledHeight = ((originalHeight / vectorHeight) + 1) * vectorHeight;
        }
        if (originalWidth % vectorWidth != 0) {
            scaled = true;
            scaledWidth = ((originalWidth / vectorWidth) + 1) * vectorWidth;
        }
        int[][] scaledImage;
        // fill the padding
        if (scaled) {
            scaledImage = new int[scaledHeight][scaledWidth];
            for (int i = 0; i < scaledHeight; i++) {
                int x = i >= originalHeight ? originalHeight - 1 : i;
                for (int j = 0; j < scaledWidth; j++) {
                    int y = j >= originalWidth ? originalWidth - 1 : j;
                    scaledImage[i][j] = originalImage[x][y];
                }
            }
        } else {
            scaledImage = originalImage;
        }
        // vector of Books
        Vector<Vector<Integer>> vectorOfBooks = new Vector<>();
        for (int i = 0; i < scaledHeight; i += vectorHeight) {
            for (int j = 0; j < scaledWidth; j += vectorWidth) {
                Vector<Integer> vector = new Vector<>();
                for (int k = i; k < vectorHeight + i && k < scaledHeight; k++) {
                    for (int l = j; l < vectorWidth + j && l < scaledWidth; l++) {
                        vector.add(scaledImage[k][l]);
                    }
                }
                vectorOfBooks.add(vector);
            }
        }

        Vector<Vector<Integer>> codeBook = new Vector<>();
        Quantize(vectorOfBooks, codeBook, codeBookSize);

        // compress
        Vector<Integer> compressedVectors = SubstituteOriginal(codeBook, vectorOfBooks);

        // items to write in file
        // CompressedVectors,codebook,originalHeight,originalWidth,scaledHeight,ScaledWidth,VectorWidth,VectorHeight
        try (FileOutputStream fileOutputStream = new FileOutputStream(outputFile);
                DataOutputStream dataOutputStream = new DataOutputStream(fileOutputStream)) {

            dataOutputStream.writeInt(originalWidth);
            dataOutputStream.writeInt(originalHeight);
            dataOutputStream.writeInt(scaledWidth);
            dataOutputStream.writeInt(scaledHeight);
            dataOutputStream.writeInt(vectorWidth);
            dataOutputStream.writeInt(vectorHeight);

            dataOutputStream.writeInt(compressedVectors.size());
            for (int integer : compressedVectors) {
                dataOutputStream.writeInt(integer);
            }

            int codeBookheight = codeBook.size();
            int codeBookWidth = codeBook.get(0).size();
            dataOutputStream.writeInt(codeBookWidth);
            dataOutputStream.writeInt(codeBookheight);
            for (int i = 0; i < codeBookheight; i++) {
                for (int j = 0; j < codeBookWidth; j++) {
                    dataOutputStream.writeInt(codeBook.get(i).get(j));
                }
            }

            dataOutputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static Vector<Integer> SubstituteOriginal(Vector<Vector<Integer>> codeBook,
            Vector<Vector<Integer>> vectorOfBooks) {
        Vector<Integer> compressedVectors = new Vector<>();
        for (Vector<Integer> vector : vectorOfBooks) {
            int min = Integer.MAX_VALUE;
            int index = 0;
            for (int i = 0; i < codeBook.size(); i++) {
                int distance = EcludianDistance(vector, codeBook.get(i));
                if (distance < min) {
                    min = distance;
                    index = i;
                }
            }
            compressedVectors.add(index);
        }
        return compressedVectors;
    }

    private static void Quantize(Vector<Vector<Integer>> vectorOfBooks, Vector<Vector<Integer>> quantizedVector,
            int level) {
        if (level == 1 || vectorOfBooks.isEmpty()) {
            if (vectorOfBooks.size() > 0) {
                quantizedVector.add(AverageOfVectors(vectorOfBooks));
            }
            return;
        }

        Vector<Integer> average = AverageOfVectors(vectorOfBooks);
        Vector<Vector<Integer>> LeftVectors = new Vector<>();
        Vector<Vector<Integer>> RightVectors = new Vector<>();
        Vector<Integer> averagePlusOne = new Vector<>();
        Vector<Integer> averageMinusOne = new Vector<>();
        for (int i = 0; i < average.size(); i++) {
            averagePlusOne.add(average.get(i) + 1);
            averageMinusOne.add(average.get(i) - 1);
        }

        for (int i = 0; i < vectorOfBooks.size(); i++) {
            if (EcludianDistance(vectorOfBooks.get(i), averagePlusOne) < EcludianDistance(vectorOfBooks.get(i),
                    averageMinusOne)) {
                RightVectors.add(vectorOfBooks.get(i));
            } else {
                LeftVectors.add(vectorOfBooks.get(i));
            }
        }

        Quantize(LeftVectors, quantizedVector, level / 2);
        Quantize(RightVectors, quantizedVector, level / 2);
    }

    private static Vector<Integer> AverageOfVectors(Vector<Vector<Integer>> vectors) {
        int[] sum = new int[vectors.get(0).size()];
        for (int i = 0; i < sum.length; i++) {
            sum[i] = 0;
        }
        for (int i = 0; i < vectors.size(); i++) {
            for (int j = 0; j < vectors.get(i).size(); j++) {
                sum[j] += (vectors.get(i).get(j));
            }
        }

        Vector<Integer> averageVector = new Vector<>();
        for (int i = 0; i < sum.length; i++) {
            averageVector.add(sum[i] / vectors.size());
        }
        return averageVector;
    }

    private static int EcludianDistance(Vector<Integer> x, Vector<Integer> y) {
        int distance = 0;
        for (int i = 0; i < x.size(); i++) {
            distance += Math.pow(x.get(i) - y.get(i), 2);
        }
        distance = (int) Math.sqrt(distance);
        return distance;
    }

    public static BufferedImage Decompress(File file) {
        try (FileInputStream fileInputStream = new FileInputStream(file);
                DataInputStream dataInputStream = new DataInputStream(fileInputStream)) {

            int originalWidth = dataInputStream.readInt();
            int originalHeight = dataInputStream.readInt();
            int scaledWidth = dataInputStream.readInt();
            int scaledHeight = dataInputStream.readInt();
            int vectorWidth = dataInputStream.readInt();
            int vectorHeight = dataInputStream.readInt();

            int compressedVectorSize = dataInputStream.readInt();
            List<Integer> compressedVectors = new ArrayList<>();
            for (int i = 0; i < compressedVectorSize; i++) {
                int value = dataInputStream.readInt();
                compressedVectors.add(value);
            }

            int codeBookWidth = dataInputStream.readInt();
            int codeBookHeight = dataInputStream.readInt();
            List<List<Integer>> codeBook = new ArrayList<>();
            for (int i = 0; i < codeBookHeight; i++) {
                List<Integer> row = new ArrayList<>();
                for (int j = 0; j < codeBookWidth; j++) {
                    int value = dataInputStream.readInt();
                    row.add(value);
                }
                codeBook.add(row);
            }

            int[][] decompressedImage = new int[scaledHeight][scaledWidth];
            for (int i = 0; i < compressedVectorSize; i++) {
                int x = i / (scaledWidth / vectorWidth);
                int y = i % (scaledWidth / vectorWidth);
                x *= vectorHeight;
                y *= vectorWidth;
                int v = 0;
                for (int j = x; j < x + vectorHeight; j++) {
                    for (int k = y; k < y + vectorWidth; k++) {
                        decompressedImage[j][k] = codeBook.get(compressedVectors.get(i)).get(v++);
                    }
                }
            }

            BufferedImage image = new BufferedImage(originalWidth, originalHeight, BufferedImage.TYPE_INT_RGB);
            for (int i = 0; i < originalHeight; i++) {
                for (int j = 0; j < originalWidth; j++) {
                    int value = 0xff000000 | (decompressedImage[i][j] << 16) | (decompressedImage[i][j] << 8)
                            | (decompressedImage[i][j]);
                    image.setRGB(j, i, value);
                }
            }
            return image;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
}
