import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import javax.imageio.ImageIO;
import javafx.embed.swing.SwingFXUtils;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.TextFormatter;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.FileChooser;
import javafx.util.converter.IntegerStringConverter;

public class VectorQuantizerController {
    // #region FXML Fields
    @FXML
    private TextField blockLengthText;

    @FXML
    private TextField blockWidthText;

    @FXML
    private TextField codeBookSizeText;

    @FXML
    private Button selectOriginalImageButton;

    @FXML
    private Button compressButton;

    @FXML
    private ImageView originalImage;

    @FXML
    private ImageView decompressedImage;

    @FXML
    private Button decompressButton;

    @FXML
    private Button saveDecompressedImage;

    @FXML
    private Button selectBinaryFileButton;

    @FXML
    private Label openedFileLabel;
    // #endregion

    private File fileToDecompress;

    // #region FXML Functions
    @FXML
    void onCompress(ActionEvent event) {
        int codeBookSize, blockLength, blockWidth;
        try {
            codeBookSize = Integer.parseInt(codeBookSizeText.getText());
            blockLength = Integer.parseInt(blockLengthText.getText());
            blockWidth = Integer.parseInt(blockWidthText.getText());
        } catch (Exception e) {
            System.out.println("Invalid input!");
            return;
        }

        // Compress originalImage and save as bin file
    }

    @FXML
    void onDecompress(ActionEvent event) {
        // Decompress fileToDecompress.bin
        // Show result in decompressedImage
    }

    @FXML
    void onSaveImage(ActionEvent event) {
        // Save decompressedImage.jpg
        if (decompressedImage == null)
            return;

        FileChooser fileChooser = new FileChooser();
        FileChooser.ExtensionFilter textFilter = new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jgp",
                "*.jpeg");
        fileChooser.getExtensionFilters().add(textFilter);
        fileChooser.setTitle("Save Image");

        File file = fileChooser.showSaveDialog(null);
        if (file == null)
            return;

        Image imageToBeSaved = decompressedImage.getImage();
        try {
            String extension = getFileExtension(file);
            ImageIO.write(SwingFXUtils.fromFXImage(imageToBeSaved, null), extension.isEmpty() ? ".jpg" : extension,
                    file);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    void onSelectBinFile(ActionEvent event) {
        FileChooser fileChooser = new FileChooser();
        FileChooser.ExtensionFilter textFilter = new FileChooser.ExtensionFilter("Binary Files", "*.bin");
        fileChooser.getExtensionFilters().add(textFilter);
        fileChooser.setTitle("Select Binary File to Decompress");

        File file = fileChooser.showOpenDialog(null);
        if (file == null)
            return;

        openedFileLabel.setText("Opened File: " + file.getName());
        fileToDecompress = file;
    }

    @FXML
    void onSelectOriginalImage(ActionEvent event) throws IOException {
        FileChooser file = new FileChooser();
        FileChooser.ExtensionFilter imagFilter = new FileChooser.ExtensionFilter("Image files", "*.png", "*.jpg",
                "*.jpeg");
        file.getExtensionFilters().add(imagFilter);
        file.setTitle("Select Image to Compress");

        File selectedImage = file.showOpenDialog(null);
        if (selectedImage == null)
            return;

        InputStream stream = new FileInputStream(selectedImage.getAbsolutePath());
        Image image = new Image(stream);
        originalImage.setImage(image);
    }

    @FXML
    void initialize() {
        blockWidthText.setTextFormatter(new TextFormatter<>(new IntegerStringConverter()));
        blockLengthText.setTextFormatter(new TextFormatter<>(new IntegerStringConverter()));
        codeBookSizeText.setTextFormatter(new TextFormatter<>(new IntegerStringConverter()));
    }
    // #endregion

    private String getFileExtension(File file) {
        String name = file.getName();
        int lastIndexOf = name.lastIndexOf(".");
        if (lastIndexOf == -1) {
            return ""; // empty extension
        }
        return name.substring(lastIndexOf);
    }
}