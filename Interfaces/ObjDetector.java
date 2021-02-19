package sample.Interfaces;

import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.image.ImageView;
import org.opencv.core.*;
import org.opencv.highgui.HighGui;
import org.opencv.imgproc.Imgproc;

import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

public interface ObjDetector {
    default void remove_noise(Mat frame){
        Mat blurredImage=new Mat();
        Mat hsv=new Mat();
        Imgproc.blur(frame,blurredImage,new Size(7,7));
        Imgproc.cvtColor(blurredImage,frame,Imgproc.COLOR_BGR2HSV);

    }
    default void values_of_hsv_image(Mat frame,double hueStart,double hueStop,double saturStart,double saturStop,double valueStart,double valueStop){
        Scalar minValues=new Scalar(hueStart,saturStart,valueStart);
        Scalar maxValues=new Scalar(hueStop,saturStop,valueStop);
        Core.inRange(frame,minValues,maxValues,frame);



    }
    default void morph_operator(Mat mask){
        Mat output=new Mat();
       Mat dilateElement=Imgproc.getStructuringElement(Imgproc.MORPH_RECT,new Size(24,24));
        Mat erodeElement=Imgproc.getStructuringElement(Imgproc.MORPH_RECT,new Size(12,12));
        Imgproc.erode(mask,output,erodeElement);
        Imgproc.dilate(mask,mask,dilateElement);


    }
    default void object_tracking(Mat frame, ImageView im1,ImageView im2, double hueStart, double hueStop, double saturStart, double saturStop, double valueStart, double valueStop){

        Mat copy=frame.clone();
        remove_noise(copy);
        values_of_hsv_image(copy,hueStart,hueStart,saturStart,saturStop,valueStart,valueStop);
        Platform.runLater(new Task<Runnable>() {
            @Override
            protected Runnable call() throws Exception {
                im1.setImage(SwingFXUtils.toFXImage((BufferedImage) HighGui.toBufferedImage(copy),null));

                return null;
            }
        });

        morph_operator(copy);
        Platform.runLater(new Task<Runnable>() {
            @Override
            protected Runnable call() throws Exception {
                im2.setImage(SwingFXUtils.toFXImage((BufferedImage) HighGui.toBufferedImage(copy),null));

                return null;
            }
        });



        List<MatOfPoint> points=new ArrayList<>();
        Mat hierarchy=new Mat();
        Imgproc.findContours(copy,points,hierarchy,Imgproc.RETR_EXTERNAL,Imgproc.CHAIN_APPROX_SIMPLE);
        if(hierarchy.size().height>0&&hierarchy.size().width>0){
            for(int idx=0;idx>=0;idx=(int)hierarchy.get(0,idx)[0]){
                Imgproc.drawContours(frame,points,idx,new Scalar(255,0,0));
            }
        }


    }
}
