import java.awt.image.BufferedImage;
import java.util.Vector;

public class VectorQuantization {
    public static void Compress(BufferedImage image,int codeBookSize,int vectorHeight,int vectorWidth){
        int[][] originalImage=RWCompression.ReadImage(image);
        int originalHeight=RWCompression.height;
        int originalWidth=RWCompression.width;
        int scaledHeight=originalHeight;
        int scaledWidth=originalWidth;
        boolean scaled=false;
        //padding
        if(originalHeight%vectorHeight!=0){
            scaled=true;
            scaledHeight=((originalHeight / vectorHeight) + 1) * vectorHeight;
        }
        if (originalWidth % vectorWidth == 0) {
            scaled = true;
            scaledWidth = ((originalWidth / vectorWidth) + 1) * vectorWidth;
        }
        int[][] scaledImage;
        // fill the padding
        if (scaled) {
            scaledImage = new int[scaledHeight][scaledWidth];
            for (int i = 0; i < scaledHeight; i++) {
                int x = i >= originalHeight ? originalHeight - 1 : originalHeight;
                for (int j = 0; j < scaledWidth; j++) {
                    int y = i >= originalWidth ? originalWidth - 1 : originalWidth;
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
                for (int k = i; k < vectorHeight + i; k++) {
                    for (int l = j; l < vectorWidth + j; l++) {
                        vector.add(scaledImage[k][l]);
                    }
                }
                vectorOfBooks.add(vector);

            }

        }
        Vector<Vector<Integer>>codeBook=new Vector<>();
        Quantize(vectorOfBooks,codeBook,codeBookSize);
        
        //compress
        Vector<Integer>compressedVectors=SubstituteOriginal(codeBook, vectorOfBooks);


        //items to write in file CompressedVectors,codebook,originalHeight,originalWidth,scaledHeight,ScaledWidth,VectorWidth,VectorHeight

    }
    private static Vector<Integer> SubstituteOriginal(Vector<Vector<Integer>>codeBook,Vector<Vector<Integer>>vectorOfBooks){
        Vector<Integer>compressedVectors=new Vector<>();
        for (Vector<Integer> vector : vectorOfBooks) {
            int min=Integer.MAX_VALUE;
            int index=0;
            for (int i=0;i<codeBook.size();i++) {
                int distance=EcludianDistance(vector, codeBook.get(i));
                if(distance<min){
                    min=distance;
                    index=i;
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
            averagePlusOne.add(average.get(i)+1);
            averageMinusOne.add(average.get(i)-1);
        }
       

        for (int i = 0; i < vectorOfBooks.size(); i++) {
            if (EcludianDistance(vectorOfBooks.get(i), averagePlusOne) < EcludianDistance(vectorOfBooks.get(i), averageMinusOne)) {
                RightVectors.add(vectorOfBooks.get(i));
            } else {
                LeftVectors.add(vectorOfBooks.get(i));
            }

        }
        Quantize(LeftVectors, quantizedVector, level / 2);
        Quantize(RightVectors, quantizedVector, level / 2);

    }

    // average of vector
    private static Vector<Integer> AverageOfVectors(Vector<Vector<Integer>> vectors) {
        if (vectors.isEmpty()) {
            return new Vector<>();
        }
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
}
