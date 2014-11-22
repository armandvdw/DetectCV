package detectcv;
/*  
 * Captures the camera stream with OpenCV  
 * Search for the faces  
 * Display a circle around the faces using Java
 */

import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import javax.swing.*;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.MatOfRect;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.highgui.VideoCapture;
import org.opencv.imgproc.Imgproc;
import org.opencv.objdetect.CascadeClassifier;

class My_Panel extends JPanel {

    private static final long serialVersionUID = 1L;
    private BufferedImage image;

    // Create a constructor method  
    /**
     * Converts/writes a Mat into a BufferedImage.
     *
     * @param matrix Mat of type CV_8UC3 or CV_8UC1
     * @return BufferedImage of type TYPE_3BYTE_BGR or TYPE_BYTE_GRAY
     */
    public boolean MatToBufferedImage(Mat matBGR) {
        long startTime = System.nanoTime();
        int width = matBGR.width(), height = matBGR.height(), channels = matBGR.channels();
        byte[] sourcePixels = new byte[width * height * channels];
        matBGR.get(0, 0, sourcePixels);
        // create new image and get reference to backing data  
        image = new BufferedImage(width, height, BufferedImage.TYPE_3BYTE_BGR);
        final byte[] targetPixels = ((DataBufferByte) image.getRaster().getDataBuffer()).getData();
        System.arraycopy(sourcePixels, 0, targetPixels, 0, sourcePixels.length);
        long endTime = System.nanoTime();
        System.out.println(String.format("Elapsed time: %.2f ms", (float) (endTime - startTime) / 1000000));
        return true;
    }

    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (this.image == null) {
            return;
        }
        g.drawImage(this.image, 10, 10, this.image.getWidth(), this.image.getHeight(), null);
    }
}

class processor {

    private CascadeClassifier face_cascade;

    // Create a constructor method  
    public processor(String cascade) {
        face_cascade = new CascadeClassifier(cascade);
        if (face_cascade.empty()) {
            System.out.println("--(!)Error loading A\n");
            return;
        } else {
            System.out.println("Face classifier loooaaaaaded up");
        }
    }

    public Mat detect(Mat inputframe) {
        Mat mRgba = new Mat();
        Mat mGrey = new Mat();
        MatOfRect faces = new MatOfRect();
        inputframe.copyTo(mRgba);
        inputframe.copyTo(mGrey);
        Imgproc.cvtColor(mRgba, mGrey, Imgproc.COLOR_BGR2GRAY);
        Imgproc.equalizeHist(mGrey, mGrey);
        face_cascade.detectMultiScale(mGrey, faces);
        System.out.println(String.format("Detected %s faces", faces.toArray().length));
        for (Rect rect : faces.toArray()) {
            Point center = new Point(rect.x + rect.width * 0.5, rect.y + rect.height * 0.5);
            Core.ellipse(mRgba, center, new Size(rect.width * 0.5, rect.height * 0.5), 0, 0, 360, new Scalar(255, 0, 255), 4, 8, 0);
        }
        return mRgba;
    }
}

public class Window {

    public static void main(String arg[]) throws InterruptedException {
        // Load the native library.  
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
        Window w = new Window();
        String video = "c:\\vid1.mp4";
        String cascade = "E:\\Dropbox\\Train\\Train1\\Data\\cascade.xml";
        int camera = -1;
        w.runWindow(cascade, video, camera); 
    }

    public void runWindow(String cascade, String video, int camera) throws InterruptedException {
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
        String window_name = "Capture - Face detection";
        JFrame frame = new JFrame(window_name);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(400, 400);
        processor my_processor = new processor(cascade);
        My_Panel my_panel = new My_Panel();
        frame.setContentPane(my_panel);
        frame.setVisible(true);
        //-- 2. Read the video stream  
        Mat webcam_image = new Mat();
        VideoCapture capture;
        if (camera == 0) {
            capture = new VideoCapture(0);
        } else {
            capture = new VideoCapture(video);
        }
        if (capture.isOpened()) {
            while (true) {
                // Thread.sleep(200);
                capture.read(webcam_image);
                if (!webcam_image.empty()) {
                    frame.setSize(webcam_image.width() + 40, webcam_image.height() + 60);
                    //-- 3. Apply the classifier to the captured image  
                    webcam_image = my_processor.detect(webcam_image);
                    //-- 4. Display the image  
                    my_panel.MatToBufferedImage(webcam_image); // We could look at the error...  
                    my_panel.repaint();
                } else {
                    System.out.println(" --(!) No captured frame -- Break!");
                    break;
                }
            }
        }
    }
}
