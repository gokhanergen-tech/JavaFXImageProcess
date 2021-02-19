package sample.Threads;

import com.sun.media.sound.JDK13Services;
import org.opencv.core.Mat;
import org.opencv.videoio.VideoCapture;
import org.opencv.videoio.VideoWriter;
import org.opencv.videoio.Videoio;

import javax.sound.sampled.AudioFileFormat;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.spi.AudioFileWriter;
import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.DoubleStream;
import java.util.stream.Stream;

public  class ViewImage implements Runnable {
    private VideoCapture videoCapture;
    private AudioInputStream audioInputStream;
    private VideoWriter videoWriter;
    private File wavFile = new File("audio.wav");
    public ViewImage(VideoCapture videoCapture,AudioInputStream audioInputStream,VideoWriter videoWriter){
        this.audioInputStream=audioInputStream;
        this.videoCapture=videoCapture;
        this.videoWriter=videoWriter;

    }
    @Override
    public void run() {
        Mat m = new Mat();


            List<Byte> bytes=new ArrayList<>();

            while (videoCapture.isOpened()) {
                if(audioInputStream!=null){
                    videoCapture.grab();
                    videoCapture.retrieve(m, 0);


                    byte[] bytes1=new byte[16000/5];
                    try {
                        audioInputStream.read(bytes1);



                        for(int i=0;i<(16000/5);i++) bytes.add(bytes1[i]);

                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                    videoWriter.write(m);


                }

            }
             byte[] resultAudioArray=new byte[bytes.size()];
            for(int i=0;i<bytes.size();i++) resultAudioArray[i]=bytes.get(i);

            System.out.println(resultAudioArray.length);

            InputStream inputStream=new ByteArrayInputStream(resultAudioArray);
            AudioInputStream audioInputStreamBytes=new AudioInputStream(inputStream,audioInputStream.getFormat(),bytes.size());
        try {
            AudioSystem.write(audioInputStreamBytes, AudioFileFormat.Type.WAVE,wavFile);
        } catch (IOException e) {
            e.printStackTrace();
        }


    }
}
