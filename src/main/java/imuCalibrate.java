import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static org.opencv.calib3d.Calib3d.findChessboardCorners;
import static org.opencv.imgcodecs.Imgcodecs.imread;

/**
 * @className: imuCalibrate
 * @description: TODO
 * @author: Lin Guifeng
 * @date: 2022/04/11 21:20
 * @version: 1.0
 **/
public class imuCalibrate {
    static {
        //在使用OpenCV前必须加载Core.NATIVE_LIBRARY_NAME类,否则会报错
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
    }
    public static void main(String[] args) throws IOException {
        List<Mat> matList = new ArrayList<>();
        List<Long> tsList = new ArrayList<>();

        //二值图地址
//        String defaultPath = "F:\\stereoCamera\\0bin\\imuCal3\\";
//        File file = new File(defaultPath);//获取文件夹对象
//        File[] fs = file.listFiles();    //获取文件夹下的文件列表，包括文件和子文件夹
//        for (File binaryPicFile : fs) {
//            //读取图像
//            String binaryPicPath = binaryPicFile.getAbsolutePath();   //获取路径
//            System.out.println("读取文件：" + binaryPicFile);
//            String tsStr = binaryPicPath.substring(29,42);
//            long ts = Long.parseLong(tsStr);
//            tsList.add(ts);
//            Mat binaryPic = imread(binaryPicPath);//读取二值图像
//            matList.add(binaryPic);
//        }
//        long out = IMUCalibrate(matList,tsList);//1649750209369
        long out = 1649750209369L ;
        long imu = TimeDataProcess.stringTimeToLong("1970-01-22 04:31:46.197"); //1970-01-22 04:31:46.197  2022-3-22 19:25:03.682
        String tsStl = TimeDataProcess.getStrTime(out-imu);
        System.out.println(tsStl);

    }

    public static long IMUCalibrate( List<Mat> frameList , List<Long> tsCameraSeq){

        //查找相机抖动点
        Size patternSize = new Size(11,9);
        MatOfPoint2f cornersLast = new MatOfPoint2f();
        Mat grayMat_0 = new Mat();
        Imgproc.cvtColor(frameList.get(0), grayMat_0, Imgproc.COLOR_RGB2GRAY);
        findChessboardCorners(grayMat_0 , patternSize , cornersLast);
        long tsCam = 0 ;
        outer:for (int i = 1; i<frameList.size();i++){
            MatOfPoint2f corners = new MatOfPoint2f();
            Mat grayMat = new Mat();
            Imgproc.cvtColor(frameList.get(i), grayMat, Imgproc.COLOR_RGB2GRAY);
            boolean patternWasFound = findChessboardCorners(grayMat,patternSize,corners);
//            drawChessboardCorners(chessPic,patternSize,corners,patternWasFound);
            if(patternWasFound){
                //计算当前棋盘内角点坐标与上一帧角点坐标差值
                double differ = 0 ;
                boolean orderIsSame = true;
                if( (corners.get(0, 0)[1]-corners.get(98, 0)[1])*(cornersLast.get(0, 0)[1]-cornersLast.get(98, 0)[1])<0){
                    orderIsSame = false;
                }
                for( int j = 0 ; j < corners.rows() ; j++ ){
                    double[] a = corners.get( j , 0);
                    double[] b = new double[2];
                    if(orderIsSame){
                        b = cornersLast.get( j , 0);
                    }else {
                        b = cornersLast.get(98-j , 0);
                    }

                    differ = differ + Math.abs( a[1] - b[1] );
                }

                //如果两帧间角点坐标变化超过3个像素，则判断当前为抖动帧
                double threshold = 5 * corners.rows();
                if( differ > threshold ){
                    tsCam = tsCameraSeq.get( i ) ;
                    break outer;
                }
                corners.copyTo( cornersLast );

            }else{
                tsCam = tsCameraSeq.get(i);
                break outer;
            }
        }


        return tsCam;
    }



}
