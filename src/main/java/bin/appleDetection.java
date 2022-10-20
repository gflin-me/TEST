package bin;

import org.opencv.core.*;
import org.opencv.highgui.HighGui;
import org.opencv.imgproc.Imgproc;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static org.opencv.imgcodecs.Imgcodecs.imread;

/**
 * @className: appleDetection
 * @description: TODO
 * @author: fxh
 * @date: 2022/01/05 17:30
 * @version: 1.0
 **/
public class appleDetection {
    static {
        // 动态链接opencv
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
    }
    public static void main(String[] args) throws IOException {
        //读取图像
        Mat applePic = imread("F:\\stereoCamera\\test\\apple4.png");
        Mat appleF = new Mat();
        //双边滤波
        Imgproc.bilateralFilter( applePic , appleF , 30 , 110 , 15 );
        //提取R、G通道
        List<Mat> matList = new ArrayList<>();
        Core.split( appleF , matList );
        Mat appleF_R = matList.get( 2 );
        Mat appleF_G = matList.get( 1 );
        //阈值二值化
        Mat binary_R = new Mat();
        Mat binary_G = new Mat();
        Imgproc.threshold(appleF_R,binary_R,100,255,Imgproc.THRESH_BINARY);
        Imgproc.threshold(appleF_G,binary_G,100,255,Imgproc.THRESH_BINARY);
        //R、G通道按位取异或运算
        Mat dst = new Mat();
        Core.bitwise_xor(binary_R,binary_G,dst);
        //otsu
//        Mat otsu = new Mat();
//        Imgproc.threshold(dst,otsu,111,255,Imgproc.THRESH_OTSU);
        //形态学_腐蚀
        int cols_pic = applePic.cols();
        int rows_pic = applePic.rows();
        int element_size = cols_pic/25;
        Mat element = Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new Size(element_size , element_size ), new Point(-1, -1));//3
        Imgproc.morphologyEx(dst, dst, Imgproc.MORPH_ERODE, element);
        //轮廓识别
        List<MatOfPoint> contours = new ArrayList<MatOfPoint>();//轮廓列表
        Mat hierarchy = new Mat();//轮廓图
        Imgproc.findContours(dst, contours, hierarchy, Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_SIMPLE, new Point(0, 0));
        //绘制外接矩形框
        int areaThreshold = cols_pic*rows_pic/1000;
        for (MatOfPoint contour : contours) {
            double area = Imgproc.contourArea(contour);//获取轮廓的面积
            if(area>areaThreshold) {
                //绘制轮廓外接矩形
                Rect rect = Imgproc.boundingRect(contour);
                Imgproc.rectangle(applePic, rect.tl(), rect.br(), new Scalar(0, 255, 0), 1);
            }
        }
        //显示图像
        HighGui.imshow("图像", dst );//depthImg   binaryPicNew
        HighGui.waitKey(0);
    }


}
