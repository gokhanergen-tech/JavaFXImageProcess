package sample.Controller;

import sample.ImageProcess.AppOpenCV;
import javafx.application.Platform;
import javafx.embed.swing.SwingFXUtils;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Slider;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.control.Button;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Scalar;
import org.opencv.highgui.HighGui;
import org.opencv.videoio.VideoCapture;
import sample.Main;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class ObjectDetection {
    private VideoCapture videoCapture;
    private ScheduledExecutorService scheduledES;
    private AppOpenCV openCV;
    public ObjectDetection(){
        videoCapture=new VideoCapture();
        openCV=AppOpenCV.getAppOpenCV();


    }
    @FXML
    private BorderPane pane;
    @FXML
    private Button back;
    @FXML
    private ImageView image,image1,image2,min,max;
    @FXML
    public void onHover(){
        back.setStyle("-fx-background-color:black;-fx-cursor:hand");
    }
    public void mouseLeaveButton(){
        back.setStyle("-fx-background-color:blue;-fx-cursor:hand");
    }
    @FXML
    private Slider hueStart,hueStop,saturationStop,saturationStart,valueStart,valueStop;
    @FXML
    public void back_button(){
        try {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    scheduledES.shutdown();
                    try {
                        Thread.sleep(1000);
                        videoCapture.release();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }



                }
            }).start();


            Parent parent= FXMLLoader.load(Main.class.getResource("View/sample.fxml"));
            pane.getScene().setRoot(parent);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    @FXML
    public void initialize(){
        scheduledES= Executors.newSingleThreadScheduledExecutor();
        Runnable runnable=new Runnable() {
            @Override
            public void run() {
                Mat frame=new Mat();
                videoCapture.open(0);
                while (videoCapture.isOpened()){
                    Mat maxVal=new Mat(40,40, CvType.CV_8UC3,new Scalar(hueStart.getValue(),saturationStart.getValue(),valueStart.getValue()));
                    Mat minVal=new Mat(40,40, CvType.CV_8UC3,new Scalar(hueStop.getValue(),saturationStop.getValue(),valueStop.getValue()));



                    videoCapture.grab();
                    videoCapture.retrieve(frame,0);

                    openCV.object_tracking(frame,image1,image2,hueStart.getValue(),hueStop.getValue(),saturationStart.getValue(),saturationStop.getValue(),valueStart.getValue(),valueStop.getValue());
                    BufferedImage bufferedImage= (BufferedImage) HighGui.toBufferedImage(frame);
                    Platform.runLater(new Runnable() {
                        @Override
                        public void run() {
                            max.setImage(SwingFXUtils.toFXImage((BufferedImage)HighGui.toBufferedImage(maxVal),null));
                            min.setImage(SwingFXUtils.toFXImage((BufferedImage)HighGui.toBufferedImage(minVal),null));
                            image.setImage(SwingFXUtils.toFXImage(bufferedImage,null));
                        }
                    });

                }
            }
        };
        scheduledES.scheduleAtFixedRate(runnable,0,33, TimeUnit.MILLISECONDS);

    }

}
