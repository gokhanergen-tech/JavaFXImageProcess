package sample.Controller;

import com.github.sarxos.webcam.Webcam;
import com.sun.imageio.plugins.bmp.BMPImageReader;
import com.sun.imageio.plugins.common.InputStreamAdapter;
import javafx.animation.FadeTransition;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.embed.swing.SwingFXUtils;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Slider;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.media.MediaView;
import javafx.scene.media.VideoTrack;
import javafx.scene.shape.Line;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.util.Duration;
import org.opencv.core.Mat;
import org.opencv.core.Size;
import org.opencv.highgui.HighGui;
import org.opencv.videoio.VideoCapture;
import org.opencv.videoio.VideoWriter;
import sample.ImageBackground;
import sample.ImageProcess.AppOpenCV;
import sample.Main;
import sample.Record.StartRecord;
import sample.Threads.RecordClass;
import sample.Threads.ViewImage;
import sun.audio.AudioDevice;
import sun.audio.AudioStream;
import sun.java2d.pipe.BufferedRenderPipe;

import javax.imageio.ImageIO;
import javax.imageio.spi.ImageReaderSpi;
import javax.imageio.stream.ImageInputStream;
import javax.sound.sampled.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.*;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;


public class Controller {
    private AppOpenCV appOpenCV;
    private VideoWriter videoWriter=new VideoWriter();
    private StartRecord startRecord=null;
    private Process process=null;
    private   BufferedImage  bufferedImage1;
    private  TargetDataLine targetDataLine;
    @FXML
    private Pane pane;
    @FXML
    private CheckBox checkbox,removeBackground,removeInverseBackground;
    @FXML
    private Slider slider;
    @FXML
    private MediaView mediaView;
    @FXML
    private ImageView image;
    @FXML
    private Button button;
    @FXML
    private Button save;

    @FXML
    private Line lineVert;
    private ScrollPane scrollPane;
    @FXML
    private Button camOn;
    @FXML
    private Button camOff;
    @FXML
    private Button photo;
    @FXML
    private Button objectDetect;
    private AudioInputStream audioInputStream=null;
    VideoCapture videoCapture=new VideoCapture();


    private double fps=0;


    public Stage stage,backStage=null;
    @FXML
    private VBox vbox;
    private List<ImageBackground> images;
    public Controller(){
      images=new ArrayList<>();
    }
     public boolean startVideoAndSound(){
        ScheduledThreadPoolExecutor schedule= new ScheduledThreadPoolExecutor(1);



            String webcam=Webcam.getDefault().getName();
            String[] splitewebcam=webcam.split(" ");
            String nameofWebcam=splitewebcam[0]+" "+splitewebcam[1]+" "+splitewebcam[2];


                String microfone=AudioSystem.getMixerInfo()[4].getName();
                if(!microfone.contains("Definition")){
                    microfone+="on Audio)";
                }
                Mat m=new Mat();







               //final Process process = Runtime.getRuntime().exec(new File(Main.class.getResource("./ff/bin/ffmpeg.exe").toURI()).getAbsolutePath()+" -listen 1 -i http://localhost:8004  -c copy vv.avi ");










                             Runnable runnable=new Runnable() {
                                 @Override
                                 public void run() {
                                     try {
                                         process = Runtime.getRuntime().exec(new File(Main.class.getResource("./ff/bin/ffmpeg.exe").toURI()).getAbsolutePath()+" -i udp://192.168.1.3:8004 -f mjpeg pipe:1");
                                     } catch (IOException e) {
                                         e.printStackTrace();
                                     } catch (URISyntaxException e) {
                                         e.printStackTrace();
                                     }
                                     byte[] bytes=new byte[1024*100];

                                     DatagramSocket socket= null;
                                     try {
                                         socket = new DatagramSocket();
                                         InetAddress inetAddress=InetAddress.getByName("192.168.1.3");
                                         socket.connect(inetAddress,8004);
                                     } catch (SocketException | UnknownHostException e) {
                                         e.printStackTrace();
                                     }
                                     InputStream inputStream=process.getInputStream();

                                     while (process.isAlive()){
                                         try {



                                          inputStream=process.getInputStream();

                                             ByteArrayInputStream byteArrayInputStream=new ByteArrayInputStream(bytes);
                                             bufferedImage1=ImageIO.read(inputStream);


                                             Platform.runLater(new Runnable() {
                                                 @Override
                                                 public void run() {


                                                     javafx.scene.image.Image im= SwingFXUtils.toFXImage(bufferedImage1,null);

                                                     image.setImage(im);

                                                 }
                                             });










                                         }catch (Exception e){
e.printStackTrace();
                                         }
                                     }


                                 }
                             };
                             Thread thread=new Thread(runnable);
                             thread.start();
                             //schedule.scheduleAtFixedRate(runnable,0,1,TimeUnit.MILLISECONDS);


















        return  true;
    }
    private AudioFormat getAudioFormat(){
        return new AudioFormat(16000,16,2,true,true);
    }
    private boolean startRecording(){

       if(new File("record.avi").exists()){
           new File("record.avi").delete();
       }
       Thread thread= new Thread(new Task<Runnable>() {
            @Override
            protected Runnable call() throws Exception {
                fps=0;
                double start=System.currentTimeMillis();
                while(videoCapture.read(new Mat())){

                    fps++;
                    if(fps==150) {
                        double stop=System.currentTimeMillis();
                        double result=stop-start;
                        fps=1000*150/result;
                        System.out.println(fps);
                        videoWriter.open("a.mp4",VideoWriter.fourcc('L','A','G','S'),fps,new Size(640,480),true);

                        break;
                    }

                }

                AudioFormat audioFormat=getAudioFormat();
                DataLine.Info info=new DataLine.Info(TargetDataLine.class,audioFormat);
                if(!AudioSystem.isLineSupported(info)){
                    System.out.println("System is not supported!");
                    System.exit(0);
                }

                try {
                    targetDataLine=(TargetDataLine)AudioSystem.getLine(info);


                    targetDataLine.open(audioFormat);
                } catch (LineUnavailableException e) {
                    e.printStackTrace();

                }

                targetDataLine.start();


                audioInputStream=new AudioInputStream(targetDataLine);


                    if(startRecord==null){
                        startRecord=new StartRecord(new ViewImage(videoCapture,audioInputStream,videoWriter),new RecordClass(videoCapture,appOpenCV,audioInputStream,videoWriter,vbox,checkbox,removeBackground,removeInverseBackground,image,slider,button));
                    }
                    startRecord.shutDown();
                    startRecord.newSchedule();



                return null;
            }
        });

       thread.start();

        return true;


    }
    public void addImage(){
        ImageBackground imageBackground=new ImageBackground();
        vbox.getScene().getWindow().setOpacity(0);
        imageBackground.takeScreen();
        vbox.getScene().getWindow().setOpacity(1);
        ImageView imageView=new ImageView();
        imageView.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                image.setImage(imageBackground.resize(vbox.getScene().getWidth()-vbox.getPrefWidth()-button.getWidth(),vbox.getScene().getHeight()));
            }
        });

        save.setOnAction(save->{
            if(image.getImage()!=null){
                FileChooser chooser=new FileChooser();
                FileChooser.ExtensionFilter jpg = new FileChooser.ExtensionFilter("(*.jpg)", "*.jpg");
                FileChooser.ExtensionFilter png = new FileChooser.ExtensionFilter("(*.png)", "*.png");
                chooser.getExtensionFilters().addAll(jpg,png);

                File file = chooser.showSaveDialog(vbox.getScene().getWindow());

                if(file!=null)
                    imageBackground.save(file.getPath());


            }
        });

        imageView.setImage(imageBackground.resize(200,200));

        vbox.getChildren().add(imageView);
        images.add(imageBackground);
    }
    public BufferedImage resize(int w,int h){
        BufferedImage takedPhoto=SwingFXUtils.fromFXImage(image.getImage(),null);
        Image image=takedPhoto.getScaledInstance(w,h,Image.SCALE_SMOOTH);
        BufferedImage bufferedImage=new BufferedImage(w,h,BufferedImage.TYPE_INT_RGB);
        Graphics2D graphics2D=bufferedImage.createGraphics();
        graphics2D.drawImage(image,0,0,null);
        graphics2D.dispose();
        return bufferedImage;
    }
    public void onCheck(){
        checkbox.setDisable(false);
        removeBackground.setDisable(false);
        removeInverseBackground.setDisable(false);
    }
    public void offCheck(){
        checkbox.setDisable(true);
        removeBackground.setDisable(true);
        removeInverseBackground.setDisable(true);

    }

    @FXML
    public void initialize(){
        appOpenCV=AppOpenCV.getAppOpenCV();
       startVideoAndSound();

        objectDetect.setOnAction(event->{



            if(videoCapture.isOpened()){
                videoCapture.release();
                if(videoWriter.isOpened()){
                    videoWriter.release();
                }
                startRecord.shutDown();
            }

            Platform.runLater(()->{

                try {
                   InputStream in= Main.class.getResourceAsStream("View/object_detection.fxml");
                   FXMLLoader fxmlLoader=new FXMLLoader();

                   fxmlLoader.setLocation(Main.class.getResource("View/object_detection.fxml"));
                   fxmlLoader.load(in);
                   Parent parent=fxmlLoader.getRoot();



                    vbox.getScene().setRoot(parent);
                } catch (IOException e) {
                    e.printStackTrace();
                }

            });
        });
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                vbox.prefHeightProperty().bind(vbox.getScene().heightProperty());
                vbox.getScene().setOnKeyPressed(e->{
                    if(e.getCode()== KeyCode.PRINTSCREEN){
                        addImage();
                    }
                });
                lineVert.endYProperty().bind(vbox.getScene().heightProperty());
                vbox.getScene().getWindow().setOnCloseRequest(e->{
                    System.out.println("Kapatma isteği...");
                    if(videoCapture!=null&&videoCapture.isOpened()){
                        videoCapture.release();
                        try {
                            if(audioInputStream!=null){
                                audioInputStream.close();
                                if(videoWriter!=null&&videoWriter.isOpened())
                                    videoWriter.release();
                            }


                            doSomething();
                            if(startRecord!=null){
                                if(  startRecord.shutDown()){
                                    System.out.println("Kayıt Durduruldu...");
                                }

                            }


                        } catch (IOException ioException) {

                            ioException.printStackTrace();
                        }
                    }
                    System.gc();
                    System.exit(0);
                });
            }
        });

    button.setOnAction(e->{
          addImage();

    });


    camOn.setOnAction(e->{

        onCheck();
        videoCapture.open(0);

        if(videoCapture.isOpened()) {
            startRecording();
        }

   camOff.setOnAction(event->{


       videoCapture.release();
       new Thread(new Runnable() {
           @Override
           public void run() {
               try {
                   audioInputStream.close();
                   targetDataLine.close();
                   videoWriter.release();
                   File file=new File("audio.wav");
                   if(file.exists()){
                       file.delete();
                   }
                   while (!file.exists()){
                       try {
                           Thread.sleep(200);
                       } catch (InterruptedException interruptedException) {
                           interruptedException.printStackTrace();
                       }
                   }
                   doSomething();
                   startRecord.shutDown();
               } catch (IOException ioException) {
                   ioException.printStackTrace();
               }


           }
       }).start();

       offCheck();

   });

    });

    photo.setOnAction(e->{
        if(image.getImage()!=null){
            FileChooser chooser=new FileChooser();
            chooser.getExtensionFilters().addAll(new FileChooser.ExtensionFilter("png","*.png"),new FileChooser.ExtensionFilter("jpg","*.jpg"));
          File file=  chooser.showSaveDialog(vbox.getScene().getWindow());
          if(file!=null){
              System.out.println(file.getPath().substring(file.getPath().length()-3));
              try {
                  BufferedImage bufferedImage=SwingFXUtils.fromFXImage(image.getImage(),null);
                  ImageIO.write(resize(bufferedImage.getWidth(),bufferedImage.getHeight()),file.getPath().substring(file.getPath().length()-3),file);
              } catch (IOException ioException) {
                  ioException.printStackTrace();
              }

          }


        }

    });
    new Thread(new Task<Runnable>() {
        @Override
        protected Runnable call() throws Exception {
          
            Platform.runLater(new Runnable() {
                @Override
                public void run() {
                    FadeTransition fadeTransition=new FadeTransition();
                    fadeTransition.setDuration(Duration.millis(1000));
                    fadeTransition.setFromValue(10);
                    fadeTransition.setToValue(0.1);
                    fadeTransition.setCycleCount(1);
                    fadeTransition.setNode(pane.getChildren().get(1));
                    fadeTransition.setOnFinished(e->{

                            pane.getChildren().remove(1);

                    });
                    fadeTransition.play();

                }
            });
            return null;
        }
    }).start();


    }

    public boolean doSomething() {

        Runtime r = Runtime.getRuntime();
        try {



            r.exec( new File(Main.class.getResource("./ff/bin/ffmpeg.exe").toURI()).getAbsolutePath()+" "+ "-i audio.wav -i a.mp4 -acodec copy -vcodec copy record.avi");

            return true;
        } catch (IOException | URISyntaxException e) {
            System.out.println(7);
           e.printStackTrace();
        }
       return false;
    } //End doSomething Function


}

