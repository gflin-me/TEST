package unit;

import org.opencv.core.*;
import org.opencv.highgui.HighGui;
import org.opencv.imgproc.Imgproc;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static org.opencv.core.CvType.CV_32F;
import static org.opencv.imgcodecs.Imgcodecs.IMREAD_GRAYSCALE;
import static org.opencv.imgcodecs.Imgcodecs.imread;
import static org.opencv.imgproc.Imgproc.DIST_L2;
import static unit.featureExtractionFunction.*;
import static unit.pcProcess.PointCloud2Binary;

/**
 * @className: sobel
 * @description: TODO
 * @author: fxh
 * @date: 2021/10/12 21:28
 * @version: 1.0
 **/
public class sobel {

    static {
        // 动态链接opencv
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
    }
    private static int mCutL = 10;
    private static int mCutR = 10;
    private static final StereoBMUtil stereoBMUtil = new StereoBMUtil(1 ,1 , mCutL ,mCutR );
    public static void main(String[] args) throws IOException {

        String savePath_US = "F:\\stereoCamera\\PCread\\picwithIMU\\databaseAll\\picture\\DR\\20220620_1655708272863_-36.92505_0.32958984_-176.34705.png";

        String fileName = "1214_17.12.49.995_-49.608765_-2.3565674_121.102295.png";
//        String fileName = "1105091411_-47.43347_-1.0272217_92.51587.png";
        double[] range = { -200 , 200   , 200 , 1750 , -2400 , 800 };
        //读取双目图像
        Mat binaryPic = imread( savePath_US );
        long Time1 = System.currentTimeMillis();
        //双目匹配算法
        Mat depthImg = stereoBMUtil.SGBMCompute(binaryPic);
        HighGui.imshow("二值图", depthImg );//depthImg   binaryPicNew
        HighGui.waitKey(0);

        Mat mat3d = stereoBMUtil.get3Dmat();                //获取点云图
        long Time2 = System.currentTimeMillis();
        //滤波1：双边滤波
//        Mat mat3dNew = new Mat();
//        Imgproc.bilateralFilter(mat3d , mat3dNew , 10 , 10 , 15 );//高斯双边滤波

        //滤波2：对z方向图双边滤波
//        List<Mat> matList = new ArrayList<>();
//        Core.split(mat3d,matList);
//        Mat matZ = new Mat();
//        Imgproc.bilateralFilter(matList.get(2) , matZ , 10 , 10 , 15 );
//        matList.remove(2);
//        matList.add(2,matZ);
//        Mat mat3dNew = new Mat();
//        Core.merge(matList,mat3dNew);

        //滤波3：快速引导滤波
//        Mat mat3dG = stereoBMUtil.guidedFilter( mat3d , 60 , 0.000001);

        //滤波4：双边滤波
        List<Mat> matList = new ArrayList<>();
        Core.split(mat3d,matList);
        Mat matZ = matList.get(2);
        matZ = stereoBMUtil.guidedFilter1( matZ , matZ , 60 , 0.000001 );
        matList.remove(2);
        matList.add(2,matZ);
        Mat mat3dNew = new Mat();
        Core.merge(matList,mat3dNew);

        long Time3 = System.currentTimeMillis();
        //获取欧拉角
        double[] Euler = pcProcess.findEuler(fileName);
        //投影二值图

        Mat binaryPicNew = PointCloud2Binary( mat3dNew , Euler, 4 , range);
        long Time4 = System.currentTimeMillis();
        long usedTime1 = Time2-Time1;
        long usedTime2 = Time3-Time2;
        long usedTime3 = Time4-Time3;
        long usedTimeAll= Time4-Time2;
        System.out.println("至投影匹配耗时"+usedTime1+"ms"+usedTime2+"ms"+usedTime3+"ms,总"+usedTimeAll+"ms");
        binaryMorphologyEx( binaryPicNew ,1,5,12);
        List<MatOfPoint> contours = new ArrayList<>();//轮廓列表
        Mat hierarchy = new Mat();//轮廓图
        Mat hierarchyPic = new Mat();//轮廓图
        Mat horizontalPic = new Mat();//水平线轮廓图
        //轮廓识别
        Imgproc.findContours(binaryPic, contours, hierarchy, Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_SIMPLE, new Point(0, 0));
        double numPixelAll = Core.countNonZero(binaryPic);
        hierarchyPic.create(binaryPic.size(),binaryPic.type());
        for(int i = 0;i <contours.size(); i++){
            Imgproc.drawContours(hierarchyPic,contours,i,new Scalar(255,255,255),1);
        }
//        Mat element = Imgproc.getStructuringElement(Imgproc.MORPH_CROSS, new Size(81, 81), new Point(-1, -1));
        Mat element = getStairElement(21,11);
        //先腐蚀再膨胀
        Imgproc.morphologyEx(binaryPic, horizontalPic, Imgproc.MORPH_ERODE, element);
        Imgproc.morphologyEx(horizontalPic, horizontalPic, Imgproc.MORPH_DILATE, element);
//        Imgproc.morphologyEx(binaryPic, horizontalPic, Imgproc.MORPH_OPEN, element);
//        double numPixelHorizontal = Core.countNonZero(horizontalPic);
        Core.normalize(element, element, 0, 255, Core.NORM_MINMAX, CvType.CV_8U);
        HighGui.imshow("深度图", depthImg );//depthImg   binaryPicNew
        HighGui.imshow("二值图", binaryPicNew );//depthImg   binaryPicNew
        HighGui.waitKey(0);

    }

    private static void harrisCornerDemo(Mat binaryPic, Mat dst){
        Mat cornerMat = new Mat();
        int numCoener = 0;
        Imgproc.cornerHarris(binaryPic,cornerMat,2,3,0.04);
        Core.normalize(cornerMat,cornerMat,0,255,Core.NORM_MINMAX , CV_32F);

        dst.create(binaryPic.size(),binaryPic.type());
        binaryPic.copyTo(dst);
        float[] data = new float[1];
        for(int j=0;j<cornerMat.rows();j++){
            for(int i=0;i<cornerMat.cols();i++){
                cornerMat.get(j,i,data);
                if((int)data[0]>100){
                    Imgproc.circle(dst,new Point(i,j),5,new Scalar(255,0,0),1,8,0);
                    numCoener++;
                }
            }
        }
        System.out.println(numCoener+"个角点");

    }
    private static double shiTomasCornerDemo(Mat binaryPic, Mat dst){
        //角点检测
        int numCoener = 0;
        MatOfPoint corners = new MatOfPoint();
        Imgproc.goodFeaturesToTrack(binaryPic,corners,100,0.01,22,new Mat(),3,false,0.04);
        //绘制角点
        dst.create(binaryPic.size(),binaryPic.type());
        binaryPic.copyTo(dst);
        Point[] points = corners.toArray();//角点点集
        for(int i=0;i<points.length;i++){
            Imgproc.circle( dst , points[i] ,1,new Scalar(255,255,255),5);
            numCoener++;
        }
        //角点直线拟合
        MatOfPoint pointMat = new MatOfPoint();
        pointMat.fromArray(points);
        Mat line = new Mat();
        Imgproc.fitLine(pointMat, line, DIST_L2 , 0 ,0.01,0.01);//拟合直线
        //直线参数获取
        float[] lineParams = new float[4];
        for(int i=0;i<4;i++){
            float[] a = new float[1];
            line.get(i,0,a);
            lineParams[i] = a[0];
        }
        //直线绘制
        double kline = lineParams[1]/lineParams[0];
        Point lineP1 = new Point(0, kline*(0-lineParams[2])+lineParams[3]);
        Point lineP2 = new Point(dst.width()-1 , kline*(dst.width()-1-lineParams[2])+lineParams[3]);
        Imgproc.line(dst,lineP1,lineP2,new Scalar(255,255,255));
        //计算距离
        double[] distance = new double[points.length];
        for( int j=0; j<points.length;j++){
            distance[j] = Distance(lineP1,lineP2,points[j]);
        }
        double distanceAverag = 0;
        for (double value : distance) {
            distanceAverag += value;
        }
        distanceAverag=distanceAverag/points.length;
        System.out.println(corners.rows()+"个角点"+numCoener+"角点偏移距离平均值"+distanceAverag);

        return distanceAverag;

    }

    /**
     * 根据输入图像大小切割图像
     * @param srt 原图像
     * @param cutLeft 图像宽度
     * @return 切割后的图像
     */
    public static Mat ImgCut( Mat srt, int cutLeft , int cutRight ){
        int widthOriginal = srt.width();
        int heightOriginal = srt.height();

        int imgWidth = widthOriginal-cutLeft-cutRight;  //新图像宽度
        Rect rect = new Rect( cutLeft , 0 , imgWidth ,  heightOriginal);
        return new Mat(srt , rect);
    }



}
