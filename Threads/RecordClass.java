package sample.Threads;

import javafx.application.Platform;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Slider;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import org.opencv.core.Mat;
import org.opencv.highgui.HighGui;
import org.opencv.videoio.VideoCapture;
import org.opencv.videoio.VideoWriter;
import sample.ImageProcess.AppOpenCV;

import javax.sound.sampled.AudioInputStream;
import java.awt.*;
import java.awt.image.BufferedImage;

public class RecordClass implements Runnable {
    private final VideoCapture videoCapture;
    private final AppOpenCV appOpenCV;
    private AudioInputStream audioInputStream=null;
    private final VideoWriter videoWriter;
    private final VBox vbox;
    private final CheckBox checkbox,removeBackground,removeInverseBackground;
    private final ImageView image;
    private final Slider slider;
    private final Button button;

    public RecordClass(VideoCapture videoCapture, AppOpenCV appOpenCV, AudioInputStream audioInputStream, VideoWriter videoWriter, VBox vbox, CheckBox checkbox, CheckBox removeBackground, CheckBox removeInverseBackground, ImageView image, Slider slider, Button button) {
        this.videoCapture = videoCapture;
        this.appOpenCV = appOpenCV;
        this.audioInputStream = audioInputStream;
        this.videoWriter = videoWriter;
        this.vbox = vbox;
        this.checkbox = checkbox;
        this.removeBackground = removeBackground;
        this.removeInverseBackground = removeInverseBackground;
        this.image = image;
        this.slider = slider;
        this.button = button;
    }


    @Override
    public void run() {
        Mat m=new Mat();

        while (videoCapture.isOpened()){

            if(audioInputStream!=null) {

                videoCapture.grab();
                videoCapture.retrieve(m, 0);

                BufferedImage bufferedImage = (BufferedImage) HighGui.toBufferedImage(m);
                BufferedImage bufferedImage1 = new BufferedImage((int) (vbox.getScene().getWidth() - vbox.getPrefWidth() - button.getWidth()), (int) vbox.getScene().getHeight(), BufferedImage.TYPE_INT_ARGB);
                Graphics2D graphics2D = bufferedImage1.createGraphics();
                graphics2D.drawImage(bufferedImage.getScaledInstance((int) (vbox.getScene().getWidth() - vbox.getPrefWidth() - button.getWidth()), (int) vbox.getScene().getHeight(), Image.SCALE_SMOOTH), 0, 0, null);
                graphics2D.dispose();

                if (checkbox.isSelected()) {
                    appOpenCV.doCanny(m, slider.getValue());
                    removeBackground.setDisable(true);
                    removeInverseBackground.setDisable(true);
                } else if (!checkbox.isSelected()) {

                    if (removeBackground.isSelected() || removeInverseBackground.isSelected()) {
                        appOpenCV.removeBackground(m, removeBackground.isSelected(), slider.getValue());
                        checkbox.setDisable(true);
                        if (removeBackground.isSelected())
                            removeInverseBackground.setDisable(true);
                        else
                            removeBackground.setDisable(true);
                    } else {
                        removeBackground.setDisable(false);
                        removeInverseBackground.setDisable(false);
                        checkbox.setDisable(false);
                    }
                }
                Platform.runLater(() -> {

                    image.setImage(SwingFXUtils.toFXImage((BufferedImage) HighGui.toBufferedImage(m), null));

                });
            }
        }

    }
}
