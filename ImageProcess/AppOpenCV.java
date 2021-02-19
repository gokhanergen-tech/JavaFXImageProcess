package sample.ImageProcess;

import sample.Interfaces.ObjDetector;
import org.opencv.core.*;
import org.opencv.imgproc.Imgproc;
import org.opencv.objdetect.CascadeClassifier;
import org.opencv.objdetect.Objdetect;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class AppOpenCV implements ObjDetector {
    private final CascadeClassifier cascadeClassifier;
    private static AppOpenCV appOpenCV;
    private  int absoluteFaceSize=0;

    static {appOpenCV=new AppOpenCV();}
    private AppOpenCV() {
        cascadeClassifier=new CascadeClassifier("../haarcascades/haarcascade_frontalface_alt.xml");
    }
    public static AppOpenCV getAppOpenCV(){
        return appOpenCV;
    }
    public Mat getHSV(Mat frame){
        List<Mat> images = new ArrayList<Mat>();
        Core.split(frame, images);
        MatOfInt channels = new MatOfInt(0);

        // set the ranges
        MatOfFloat histRange = new MatOfFloat(0, 256);

        Mat hist_b=new Mat();
        Mat hist_r=new Mat();

        Mat hist_g=new Mat();
        MatOfInt histSize = new MatOfInt(256);
        Imgproc.calcHist(images.subList(0, 1), channels, new Mat(), hist_b, histSize, histRange, false);

        // G and R components (if the image is not in gray scale)

        Imgproc.calcHist(images.subList(1, 2), channels, new Mat(), hist_g, histSize, histRange, false);
        Imgproc.calcHist(images.subList(2, 3), channels, new Mat(), hist_r, histSize, histRange, false);





        Mat hist=new Mat(200,200, CvType.CV_8UC3,new Scalar(0,0,0));



        int bin_w = (int) Math.round(200 / histSize.get(0, 0)[0]);
        Core.normalize(hist_b, hist_b, 0, hist.rows(), Core.NORM_MINMAX, -1, new Mat());

        Core.normalize(hist_g, hist_g, 0, hist.rows(), Core.NORM_MINMAX, -1, new Mat());
        Core.normalize(hist_r, hist_r, 0, hist.rows(), Core.NORM_MINMAX, -1, new Mat());

        for (int i = 1; i < frame.get(0, 0)[0]; i++){
            Imgproc.line(hist, new Point(bin_w * (i - 1), 200 - Math.round(hist_b.get(i - 1, 0)[0])), new Point(bin_w * (i), 200 - Math.round(hist_b.get(i, 0)[0])), new Scalar(255, 0, 0), 2, 8, 0);

            Imgproc.line(hist, new Point(bin_w * (i - 1), 200 - Math.round(hist_g.get(i - 1, 0)[0])),new Point(bin_w * (i), 200 - Math.round(hist_g.get(i, 0)[0])), new Scalar(0, 255, 0), 2, 8, 0);
            Imgproc.line(hist, new Point(bin_w * (i - 1), 200 - Math.round(hist_r.get(i - 1, 0)[0])),new Point(bin_w * (i), 200 - Math.round(hist_r.get(i, 0)[0])), new Scalar(0, 0, 255), 2, 8, 0);

        }

        return hist;
    }
    public void getTransformFourier(Mat frame){
        Mat tf=new Mat(frame.height(),frame.width(),CvType.CV_8UC3);
        int addPixelRows=Core.getOptimalDFTSize(frame.rows());
        int addPixelCols=Core.getOptimalDFTSize(frame.cols());
        Imgproc.cvtColor(frame,tf,Imgproc.COLOR_BGR2GRAY);
        Core.copyMakeBorder(tf,tf,0,addPixelRows-frame.rows(),0,addPixelCols-frame.cols(),Core.BORDER_CONSTANT,Scalar.all(0));
        tf.convertTo(tf,CvType.CV_32F);
        Core.merge(new ArrayList<Mat>(Arrays.asList(new Mat[]{tf,Mat.zeros(tf.size(),CvType.CV_32F)})),tf);
        Core.dft(tf,tf);

        List<Mat> newPlanets=new ArrayList<>();
        Core.split(tf,newPlanets);
        Core.magnitude(newPlanets.get(0),newPlanets.get(1),tf);
        Core.add(Mat.ones(tf.size(),CvType.CV_32F),tf,tf);
        Core.log(tf,tf);
        Core.normalize(tf,tf, 0, 255, Core.NORM_MINMAX);
        tf.convertTo(frame,CvType.CV_8UC3);


    }
    public void  getAntiTransform(Mat frame){
        Mat tf=new Mat();
        tf=frame.clone();
        int addPixelRows=Core.getOptimalDFTSize(tf.rows());
        int addPixelCols=Core.getOptimalDFTSize(tf.cols());
        Imgproc.cvtColor(tf,tf,Imgproc.COLOR_BGR2GRAY);
        Core.copyMakeBorder(tf,tf,0,addPixelRows-tf.rows(),0,addPixelCols-tf.cols(),Core.BORDER_CONSTANT,Scalar.all(0));
        tf.convertTo(tf,CvType.CV_32F);
        List<Mat> planes=Arrays.asList(new Mat[]{tf,Mat.zeros(tf.size(),CvType.CV_32F)});
        Core.merge(planes,tf);
        Core.idft(tf,tf);
        Mat restoredImage = new Mat();
        List<Mat> divided=new ArrayList<>();
        Core.split(tf, divided);
        Core.normalize(planes.get(0), restoredImage, 0, 255, Core.NORM_MINMAX);
        restoredImage.convertTo(frame,CvType.CV_8UC3);
    }
    public void faceDetect(Mat frame){
        Mat copiedFrame=frame.clone();
        Imgproc.cvtColor(copiedFrame,copiedFrame,Imgproc.COLOR_BGR2GRAY);
        Imgproc.equalizeHist(copiedFrame,copiedFrame);

        MatOfRect faces=new MatOfRect();
        if (absoluteFaceSize == 0)
        {
            int height = copiedFrame.rows();
            if (Math.round(height * 0.2f) > 0)
            {
                absoluteFaceSize = Math.round(height * 0.2f);
            }
        }

        try {
            cascadeClassifier.detectMultiScale(copiedFrame, faces,1.1,1, 0 | Objdetect.CASCADE_SCALE_IMAGE, new Size(this.absoluteFaceSize, this.absoluteFaceSize), new Size());


        }catch (Exception e){
            e.printStackTrace();
        }
        faces.toList().forEach(face->{
            Imgproc.rectangle(frame,face.tl(),face.br(),new Scalar(255,0,0),3);
        });



    }
    public void doCanny(Mat frame,Double sliderValue){
        Mat copiedFrame=frame.clone();
        Mat gray=new Mat();
        Imgproc.cvtColor(copiedFrame,gray,
                Imgproc.COLOR_BGR2GRAY);
        Mat blur=new Mat();
        Imgproc.blur(gray,blur,new Size(3,3));
        Imgproc.Canny(blur,frame,sliderValue,sliderValue*3,3,false);



    }
    public void removeBackground(Mat frame,Boolean removeBG,Double sliderValue){
        Mat original=frame.clone();
        Mat hsvImage=new Mat(frame.size(),CvType.CV_8U);

        Imgproc.cvtColor(frame,hsvImage,Imgproc.COLOR_BGR2HSV);
        List<Mat> mats=new ArrayList<>();
        Core.split(hsvImage,mats);
        Mat matThresh=new Mat();
        Imgproc.threshold(mats.get(0),matThresh,sliderValue,179,Imgproc.THRESH_BINARY);
        Imgproc.blur(matThresh,matThresh,new Size(5,5));
        Imgproc.dilate(matThresh,matThresh,new Mat(),new Point(-1,-1),1);
        Imgproc.erode(matThresh,matThresh,new Mat(),new Point(-1,-1),3);
        if(removeBG)
            Imgproc.threshold(matThresh, frame,sliderValue, 179.0, Imgproc.THRESH_BINARY);
        else{
            Imgproc.threshold(matThresh, frame, sliderValue, 255, Imgproc.THRESH_BINARY_INV);

            original.copyTo(frame,frame);


        }






    }
}