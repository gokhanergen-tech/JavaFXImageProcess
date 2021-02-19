package sample;

import javafx.embed.swing.SwingFXUtils;

import javafx.scene.image.Image;

import javax.imageio.ImageIO;

import java.awt.*;

import java.awt.image.BufferedImage;
import java.io.File;

import java.io.IOException;


public class ImageBackground {
    private Robot robot;
    private Image image=null;
    private BufferedImage bufferedImage=null;
    private double w,h;


    public ImageBackground(){
        try {
            this.robot=new Robot();
        } catch (AWTException e) {
            e.printStackTrace();
        }

    }
    public BufferedImage getBufferedImage(){
        return bufferedImage;
    }
    public boolean save(String path) {
        if(image!=null){
               File file=new File(path);
                try {
                    ImageIO.write(getBufferedImage(),"jpg",file);
                } catch (IOException e) {
                   return false;
                }
        }else{
            return false;
        }
        return true;

    }
    public Image resize(double w,double h){
        if(image!=null){
            this.h=h;
            this.w=w;
            BufferedImage bufferedImage= getBufferedImage();
            java.awt.Image image=bufferedImage.getScaledInstance((int)w,(int)h, java.awt.Image.SCALE_SMOOTH);
            BufferedImage bufferedImage1=new BufferedImage((int)w,(int)h,BufferedImage.TYPE_INT_RGB);
            Graphics2D graphics2D=bufferedImage1.createGraphics();
            graphics2D.drawImage(image,0,0,null);
            graphics2D.dispose();
            return SwingFXUtils.toFXImage(bufferedImage1,null);


        }
        throw new NullPointerException("Image object is null!");

    }
    public void takeScreen(){

            Rectangle rec=new Rectangle();
            Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
            double widthA = screenSize.getWidth();
            double heightA = screenSize.getHeight();
            rec.setRect(0,0,widthA,heightA);

            BufferedImage bufferedImage=robot.createScreenCapture(rec);
            this.bufferedImage=bufferedImage;
            image= SwingFXUtils.toFXImage(bufferedImage,null);



    }
    public Image getImage(){
        if(image!=null)
          return image;
        return null;
    }
}
