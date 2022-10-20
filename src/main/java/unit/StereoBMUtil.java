package unit;

import org.opencv.calib3d.Calib3d;
import org.opencv.calib3d.StereoBM;
import org.opencv.calib3d.StereoSGBM;
import org.opencv.core.*;
import org.opencv.imgproc.Imgproc;

import java.util.ArrayList;
import java.util.List;

import static org.opencv.core.CvType.*;
import static unit.sobel.ImgCut;

/**
 * @className: StereoBMUtil 立体匹配
 * @description: 实现对双目图像的立体匹配 获取深度图
 * @author: Osmanthus
 * @date: 2021/10/18 10:28
 * @version: 1.1
 * @update: 2022/01/10 实现了通过双目相机进行地形识别的基本功能，使用SVM进行识别分类
 **/
public class StereoBMUtil {

//    private static final String TAG = unit.StereoBMUtil.class.getName();
    //单目图像尺寸
    private final int imageWidth_single;
    private final int imageHeight_single;
    private final Mat Q_original = new Mat(); private final Mat Q = new Mat();

    //映射表
    private final Mat mapLx = new Mat();    private final Mat mapRx = new Mat();
    private final Mat mapLy = new Mat();    private final Mat mapRy = new Mat();
    //灰度图
    private Mat grayImageL ;                private Mat grayImageR ;
    //校正图像
    private  Mat rectifyMatL = new Mat();  private Mat rectifyMatR = new Mat();
    //有效区域
    private final Rect validROIL = new Rect();  private final Rect validROIR = new Rect();
    //视差图
    private Mat disparity = new Mat();
    //3D坐标图
    private final Mat mat3d = new Mat();private Mat mat3dFiler = new Mat();
    //深度图
    private Mat disp8U;
    //BM算法对象
    private final StereoBM BM = StereoBM.create();
    //SGBM算法对象
    private final static int numDisparities = 64 ;
    private final StereoSGBM SGBMmatcher = StereoSGBM.create(
            0,
            numDisparities,
            9,             //
            1944,                  //算法中的参数P1
            7776,                  //算法中的参数P2
            1,         //左右一致性检测最大容许误差阈值
            63,           //水平sobel预处理后，映射滤波器大小(默认为15)
            5,       //唯一性检测参数,官方为10
            100,      //视差连通区域像素点个数的大小
            2,          //视差连通条件
            StereoSGBM.MODE_SGBM    //mode
    );
    private int cutL; private int cutR;
    private int sigmaColor = 10;
    /*
    初始化函数
     */
    public StereoBMUtil(int chosenCamera , int chosenRatio , int mCutL , int mCutR ) {
        //相机参数
        Mat cameraMatrixL = new Mat(3, 3, CvType.CV_64F );//【输入参数】cameraMatrixL左目相机内参 fc_left_x  0  cc_left_x  0  fc_left_y  cc_left_y  0  0  1 (Matlab结果转置使用)
        Mat distCoeffL = new Mat(5, 1, CvType.CV_64F );   //【输入参数】distCoeffL左目相机畸变参数 kc_left_01,  kc_left_02,  p_left_01,  p_left_02,   kc_left_03
        Mat cameraMatrixR = new Mat(3, 3, CvType.CV_64F );//【输入参数】cameraMatrixR右目相机内参 fc_right_x  0  cc_right_x  0  fc_right_y  cc_right_y  0  0  1(Matlab结果转置使用)
        Mat distCoeffR = new Mat(5, 1, CvType.CV_64F);    //【输入参数】distCoeffR右目相机畸变参数 kc_right_01,  kc_right_02,  kc_right_03,  kc_right_04,   kc_right_05
        Mat TranslationVector = new Mat(3, 1, CvType.CV_64F );            //【输入参数】T平移向量
        Mat rotationMatrix = new Mat(3, 3, CvType.CV_64F );            //【输入参数】R旋转矩阵(matlab中的旋转矩阵与opencv的存在转置）
        //设置图像分辨率
        int[][] resolution = new int[][] { {640 , 240} , {1280, 480 } , {2560 , 720 } , {2560 , 960 }};
        imageWidth_single  = resolution[ chosenRatio ][0] / 2 ;
        imageHeight_single = resolution[ chosenRatio ][1] ;
        cutL = mCutL;
        cutR = mCutR;
        Size imageSize = new Size(imageWidth_single, imageHeight_single);

        switch (chosenCamera){
            case 0: //【相机1】60mm，30fps 100°
                switch(chosenRatio){
                    case 1://1280*480
                        cameraMatrixL.put(0, 0, 484.533610705492, 0, 318.215208465150, 0, 484.475505983674, 184.393790462220, 0, 0, 1);
                        distCoeffL.put(0, 0, 0.0868578902838703, -0.0693019618895340, 0, 0, 0.00000);
                        cameraMatrixR.put(0, 0, 486.028955708165, 0, 308.999928817797, 0, 485.765815939220, 215.674484011271, 0, 0, 1);
                        distCoeffR.put(0, 0, 0.0847628717190068, -0.0662325123141161, 0, 0, 0);
                        break;
                    case 2://2560*720
                        cameraMatrixL.put(0, 0, 725.266749532833, 0, 643.425266308346, 0, 725.277147622248, 280.362602252634, 0, 0, 1);
                        distCoeffL.put(0, 0, 0.118309339814526, -0.142410679876765, 0, 0, 0.00000);
                        cameraMatrixR.put(0, 0, 729.303697795197, 0, 626.370025948089, 0, 729.758964452465, 327.466074574375, 0, 0, 1);
                        distCoeffR.put(0, 0, 0.123252591793372, -0.152703933687346, 0, 0, 0);
                        break;
                }
                TranslationVector.put(0, 0, -60.1133537911737 , 0.196828236963096 , -0.205030506181837 );
                rotationMatrix.put(0,0, 0.999955783178648  ,0.00189700206288935,-0.00921048699855976,
                        -0.00192636166161639,0.999993088968905,-0.00317980269473356,
                        0.00920439125232621,0.00319740482300471,0.999952526765182);
                break;
            case 1://【相机2】60mm，60fps，70°
                switch(chosenRatio){
                    case 1:   //1280*480 345.350123417816 342.027471414427

                        cameraMatrixL.put(0, 0,  421.227658416046 , 0 , 345.291417764192 , 0 , 421.15927077044 , 249.730523293086 , 0 , 0 , 1  );
                        distCoeffL.put(0, 0,  0.0218092523535896 , -0.0310706603782487  , 0, 0, 0 );
                        cameraMatrixR.put(0, 0,  424.447841287623 , 0 , 342.526387342648 , 0 , 424.272282513329 , 265.610482496166 , 0 , 0 , 1  );
                        distCoeffR.put(0, 0,  0.0257031433403211 , -0.0325770250671644  , 0, 0, 0 );
                        break;
                    case 2: //2560*720
                        cameraMatrixL.put(0, 0, 843.415071412122 , 0 , 688.078174907954 , 0 , 842.740320339267 , 504.390419163581 , 0 , 0 , 1 );
                        distCoeffL.put(0, 0, 0.0487479161676086 , -0.384518227197889, 0, 0, 0.00000);
                        cameraMatrixR.put(0, 0, 847.935578227559 , 0 , 687.652401054240 , 0 , 848.732645820870 , 546.786933325984 , 0 , 0 , 1 );
                        distCoeffR.put(0, 0, -0.00353067904927581 , 0.0847391960534249, 0, 0, 0);
                        break;
                    case 3: //2560*960
                        cameraMatrixL.put(0, 0, 843.374014166102 , 0 , 690.270329950152 , 0 , 844.374940748746 , 505.996749542691 , 0 , 0 , 1 );
                        distCoeffL.put(0, 0, 0.0487479161676086 , -0.384518227197889, 0, 0, 0 );
                        cameraMatrixR.put(0, 0, 847.935578227559 , 0 , 687.652401054240 , 0 , 848.732645820870 , 546.786933325984 , 0 , 0 , 1 );
                        distCoeffR.put(0, 0, -0.00353067904927581 , 0.0847391960534249, 0, 0, 0);
                        break;
                }

                TranslationVector.put(0, 0,  -59.8855092784065 , 0.240039962783308 , 0.345485748119097  );
                rotationMatrix.put(0,0,  0.99999004213669 , 0.0016996027627352 , 0.00412637588072717 ,
                        -0.00165873710257966 , 0.999949748346203 , -0.00988682828769794 ,
                        -0.00414297220418744 , 0.00987988526324073 , 0.999942610177454  );

                break;
            case 3:
                break;
        }



        /*
        【双目校正的参数】计算
        图像校正之后，会对图像进行裁剪，这里的validROI就是指裁剪之后的区域
         */
        Mat rectificateLeft = new Mat();//校正变换矩阵
        Mat rectificateRight = new Mat();
        Mat Pl = new Mat();
        Mat Pr = new Mat();
        Calib3d.stereoRectify(cameraMatrixL , distCoeffL , cameraMatrixR, distCoeffR, imageSize, rotationMatrix, TranslationVector, rectificateLeft, rectificateRight, Pl, Pr, Q_original, Calib3d.CALIB_ZERO_DISPARITY,
                0, imageSize, validROIL, validROIR);
        //【校正映射】生成映射表mapLx, mapLy, mapRx, mapRy
        Imgproc.initUndistortRectifyMap(cameraMatrixL , distCoeffL , rectificateLeft, Pl, imageSize, CvType.CV_32FC1, mapLx, mapLy);
        Imgproc.initUndistortRectifyMap(cameraMatrixR , distCoeffR , rectificateRight, Pr, imageSize, CvType.CV_32FC1, mapRx, mapRy);

        Mat cameraMatrixLNew = new Mat(3, 3, CvType.CV_64F );Mat cameraMatrixRNew = new Mat(3, 3, CvType.CV_64F );
        double chang1 = imageWidth_single-cutL*2;
        double chang2 = imageWidth_single;
        double change = chang1/chang2;
        cameraMatrixLNew.put(0, 0, 421.227658416046 , 0 , 345.291417764192*change , 0 , 421.15927077044 , 249.730523293086 , 0 , 0 , 1);
        cameraMatrixRNew.put(0, 0, 424.447841287623 , 0 , 342.526387342648*change , 0 , 424.272282513329 , 265.610482496166 , 0 , 0 , 1);
        Mat certificateLeftNew = new Mat();
        Mat certificateRightNew = new Mat();
        Calib3d.stereoRectify(cameraMatrixLNew , distCoeffL , cameraMatrixRNew, distCoeffR, imageSize, rotationMatrix, TranslationVector,
                certificateLeftNew, certificateRightNew, Pl, Pr, Q, Calib3d.CALIB_ZERO_DISPARITY,
                0, imageSize, validROIL, validROIR);


    }

    /*
     将摄像头输入图像进行校正
    */
    private void imgRemap(Mat frameBit){
        //图像转灰度图像
        Mat grayMat = new Mat();
        Imgproc.cvtColor(frameBit, grayMat , Imgproc.COLOR_RGB2GRAY );
        //分割为左右目图像
        Rect roiL = new Rect(0, 0, imageWidth_single, imageHeight_single );
        Rect roiR = new Rect( imageWidth_single , 0, imageWidth_single, imageHeight_single );
        grayImageR = new Mat(grayMat, roiR);
        grayImageL = new Mat(grayMat, roiL);
        //图像校正
        Mat recL = new Mat();   Mat recR = new Mat();
        Imgproc.remap(grayImageL, recL, mapLx, mapLy, Imgproc.INTER_LINEAR);
        Imgproc.remap(grayImageR, recR, mapRx, mapRy, Imgproc.INTER_LINEAR);

        rectifyMatL = ImgCut( recL , cutL , cutR );
        rectifyMatR = ImgCut( recR , cutL , cutR );
    }

    /**
     * SGBM算法计算深度图
     * @param frameBit 双目图像
     * @return 深度图
     */
    public Mat SGBMCompute(Mat frameBit) {
        long Time1 = System.currentTimeMillis();
        //图像校正
        imgRemap(frameBit);
        //深度图计算
        SGBMmatcher.compute(rectifyMatL, rectifyMatR, disparity);
        //获取点云
        Calib3d.reprojectImageTo3D(disparity, mat3d, Q, true);



        Core.multiply(mat3d, new Mat(mat3d.size(), CvType.CV_32FC3, new Scalar(16, 16, 16)), mat3d);

        // 将视差图
        disp8U = new Mat(disparity.rows(), disparity.cols(), CvType.CV_8UC1);
        disparity.convertTo(disparity, CV_32F, 1.0 / 16);                               //转为32f格式并除以16得到真实视差值
        Core.normalize(disparity, disp8U, 0, 255, Core.NORM_MINMAX, CvType.CV_8U);//归一化至（0,255）
//        Imgproc.medianBlur(disp8U, disp8U, 7);                                          //中值滤波
        Imgproc.applyColorMap(disp8U, disp8U, Imgproc.COLORMAP_HSV);                          //映射为彩色
        return disp8U;
    }

    private Mat disparityBilateraFilter(Mat disparity_16S){
        Mat disp_32F = new Mat();
        Mat disp_16s = new Mat();
        Mat dispFilter_32F = new Mat();
        disparity_16S.convertTo( disp_32F , CV_32F );
        Imgproc.bilateralFilter(disp_32F,dispFilter_32F,0,150,15);
        dispFilter_32F.convertTo(disp_16s, CV_16S );
        return disp_16s;
    }



    /**
     * 获取像素点在相机坐标系下的3D坐标
     * @param dstX 像素x坐标
     * @param dstY 像素y坐标
     * @return 三维坐标double数组
     */
    public double[] getCoordinate(int dstX, int dstY) {
        double x = mat3dFiler.get(dstY, dstX)[0];
        double y = mat3dFiler.get(dstY, dstX)[1];
        double z = mat3dFiler.get(dstY, dstX)[2];
        return new double[]{x, y, z};
    }

    public int getNumDisparities(){
        return numDisparities ;
    }
    public Mat get3Dmat(){
        return mat3d ;
    }
    public Mat get3DmatFilter(){
        return mat3dFiler ;
    }


    public void setCutLength(int mCutL , int mCutR){
        cutL = mCutL ;
        cutR = mCutR ;
    }
    public void setSigmaColor(int mSigmaColor){
        sigmaColor = mSigmaColor;
    }

//    public Mat guidedFilter( Mat src , int radius , double eps){
//        src.convertTo(src, CvType.CV_32F);
//        List<Mat> matList = new ArrayList<>();
//        Core.split(src,matList);
//        Mat matZ = matList.get(2);
//        Core.divide(src, new Scalar(255.0, 255.0, 255.0), src );
//        Core.divide(matZ, new Scalar(255.0), matZ );
//
//        double s = radius / 4;
//
////        FastGuidedFilter fastGuidedFilter = new FastGuidedFilter();
////        Mat q = fastGuidedFilter.filter(src, matZ, 2*radius+1, eps, s, -1);
//        Core.multiply( q, new Scalar(255), q );
//        matList.add(2, q );
//        Mat mat3dNew = new Mat();
//        Core.merge(matList,mat3dNew);
//
//        return mat3dNew ;
//    }

    public Mat guidedFilter1(Mat src , Mat guideMat , int radius , double eps){
        src.convertTo(src , CV_64FC1);
        guideMat.convertTo(guideMat , CV_64FC1);

        Mat meanP = new Mat();
        Mat meanI = new Mat();
        Mat meanIP = new Mat();
        Mat meanII = new Mat();

        Imgproc.boxFilter(src , meanP , CV_64FC1 , new Size(radius,radius) );
        Imgproc.boxFilter(guideMat , meanI , CV_64FC1 , new Size(radius,radius) );
        Imgproc.boxFilter(src.mul(guideMat) , meanIP , CV_64FC1 , new Size(radius,radius) );
        Imgproc.boxFilter(guideMat.mul(guideMat) , meanII , CV_64FC1 , new Size(radius,radius) );

        Mat meanIMP = new Mat();
        Mat meanIMI = new Mat();
        Mat covIP = new Mat();
        Mat varI = new Mat();
        Core.multiply( meanI , meanP , meanIMP );
        Core.multiply( meanI , meanI , meanIMI );
        Core.subtract( meanIP , meanI.mul(meanP) , covIP );
        Core.subtract( meanII , meanI.mul(meanI) , varI );

        Mat varIAeps = new Mat();
        Mat a = new Mat();
        Mat b = new Mat();
        Core.add(varI,new Scalar(eps),varIAeps);
        Core.divide(covIP,varIAeps,a);
        Core.subtract(meanP,a.mul(meanI),b);

        Mat meanA = new Mat();
        Mat meanB = new Mat();
        Imgproc.boxFilter(a , meanA , CV_64FC1 , new Size(radius,radius) );
        Imgproc.boxFilter(b , meanB , CV_64FC1 , new Size(radius,radius) );

        Mat dstImg = new Mat();
        Core.add(meanA.mul(guideMat),meanB,dstImg);
        dstImg.convertTo(dstImg , CV_32FC1);
        return dstImg;
    }
}
