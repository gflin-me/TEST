import org.opencv.core.*;
import org.opencv.imgproc.Imgproc;
import unit.StereoBMUtil;
import unit.pcProcess;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static java.lang.Math.*;
import static org.opencv.core.CvType.CV_32F;
import static org.opencv.core.CvType.CV_32FC1;
import static org.opencv.imgcodecs.Imgcodecs.imread;

/**
 * @className: laplacian
 * @description: TODO
 * @author: fxh
 * @date: 2021/11/11 20:59
 * @version: 1.0
 **/
public class BlurImgDetection {
    static {
        // 动态链接opencv
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
    }
    private static int mCutL = 0;
    private static int mCutR = 0;
    private static final StereoBMUtil stereoBMUtil = new StereoBMUtil(1 ,1 , mCutL ,mCutR );
    public static void main(String[] args) throws IOException {
        for(int i = 0;i<2;i++) {
            String[] path = {"F:\\stereoCamera\\PCread\\blurPic\\clear\\","F:\\stereoCamera\\PCread\\blurPic\\blur\\"};
            String blurPicPath = path[i];
            File file = new File(blurPicPath);//获取文件夹对象
            File[] fs = file.listFiles();    //获取文件夹下的文件列表，包括文件和子文件夹

            int numFile = fs.length;                            //文件夹中图片的数量
            int numFeatures = 3;                                //特征数量
            double[][] features = new double[numFile][numFeatures]; //特征表
            int indexPic = 0;
            for (File picFile : fs) {
                //读取图像
                String binaryPicPath = picFile.getAbsolutePath();   //获取路径
                System.out.println("读取文件：" + picFile);
                Mat binaryPic = ImgHalf(imread(binaryPicPath));//读取二值图像
                double[] indexOfBlur = getIndex(binaryPic);
                features[indexPic] = indexOfBlur;
                indexPic++;

            }

            String savePath = blurPicPath.replace("r\\", "r.txt");
            pcProcess.saveAsTxt(features, savePath);
        }
//        Mat blurPic = ImgHalf(imread( blurPicPath));
//        Mat clearPic = ImgHalf(imread( clearPicPath));
//        //模糊图像的处理
//        System.out.println( "【模糊图像】"+blurPicPath);
//        double[] indexOfBlur = getIndex(blurPic);
//        //清晰图像的处理
//        System.out.println( "【清晰图像】"+clearPicPath);
//        double[] indexOfClear = getIndex(clearPic);


//        HighGui.waitKey(0);
    }

    /**
     * 计算各类指标
     * @param img
     * @return
     */
    public static double[] getIndex(Mat img){
        double modifiedLaplacian = ModifiedLaplacian(img);
        double tenengrad = Tenengrad(img);
        double tenengradNew = TenengradNew( img );
        double tenengradVariance = TenengradVariance(img);
        double energyOfLaplacian = EnergyOfLaplacian(img);
        double varianceOfLaplacian = VarianceOfLaplacian(img);
        double grayLevelVariance=GrayLevelVariance(img);
        double histogramEntropy = HistogramEntropy( img , 255 );
        return new double[] { modifiedLaplacian
                , tenengrad
                , tenengradNew
//                , tenengradVariance
//                , energyOfLaplacian
//                , varianceOfLaplacian
//                , grayLevelVariance
//                , histogramEntropy
        };
    }

    /**
     * 获取图像左图像
     * @param img
     * @return
     */
    public static Mat ImgHalf(Mat img){
        Rect roiL = new Rect(0, 0, img.width()/2, img.height() );
        return new Mat(img, roiL);
    }

    /**
     * 获取平均值和方差
     * @param dst
     * @param means
     * @param devs
     */
    public static void MeanDev(Mat dst , double[] means , double[] devs){
        MatOfDouble mean = new MatOfDouble();
        MatOfDouble dev = new MatOfDouble();
        Core.meanStdDev( dst , mean , dev );
        for(int i=0;i<3;i++){
            double[] a = new double[1];double[] b = new double[1];
            mean.get(i,0,a); dev.get(i,0,b);
            means[i] = a[0];    devs[i] = b[0];
        }
    }
    /**
     * 使用laplacian算子进行的处理
     * @param img
     * @return
     */
    public static double ModifiedLaplacian( Mat img ){
        Mat dstX = new Mat();
        Mat dstY = new Mat();
        Mat laplacianX = new Mat(1,3, CV_32FC1);
        Mat laplacianY = new Mat(3,1, CV_32FC1);
        float[] data = new float[]{ -1.0F , 2.0f , -1.0F };
        laplacianX.put(0,0,data);
        laplacianY.put(0,0,data);
        Imgproc.filter2D( img , dstX , CV_32F , laplacianX );
        Imgproc.filter2D( img , dstY , CV_32F , laplacianY );
        double numPixel = img.rows()*img.cols();
        double[] rmsX = RMS(dstX);
        double[] rmsY = RMS(dstY);
        double[] modifiedLaplacian = { rmsX[0]/sqrt(numPixel)+rmsY[0]/sqrt(numPixel),
                                        rmsX[1]/sqrt(numPixel)+rmsY[1]/sqrt(numPixel),
                                        rmsX[2]/sqrt(numPixel)+rmsY[2]/sqrt(numPixel),};
//        System.out.println( "ModifiedLaplacian："+(modifiedLaplacian[0]+modifiedLaplacian[1]+modifiedLaplacian[2])/3+"。");
        return (modifiedLaplacian[0]+modifiedLaplacian[1]+modifiedLaplacian[2])/3;
    }

    /**
     * Tenengrad评价值
     * @param img 输入图像
     * @return
     */
    public static double Tenengrad( Mat img ){
        long Time1 =  System.currentTimeMillis();
//        Mat grayMat = new Mat();
//        Imgproc.cvtColor(img, grayMat , Imgproc.COLOR_RGB2GRAY );
        //求sobel导数
        Mat sobelx = new Mat();
        Mat sobely = new Mat();
        Imgproc.Scharr(img,sobelx, CV_32F,1,0);
        Imgproc.Scharr(img,sobely, CV_32F,0,1);
//        Imgproc.Sobel(grayMat,sobelx, CV_32F,1,0);
//        Imgproc.Sobel(grayMat,sobely, CV_32F,0,1);
        long Time2 =  System.currentTimeMillis();
        //分别求均方根
        double[] rmsX = RMS(sobelx);
        long Time3 =  System.currentTimeMillis();
        double[] rmsY = RMS(sobely);
        double tenengrad = pow(rmsX[0],2) + pow(rmsY[0],2);
        long Time4 =  System.currentTimeMillis();
        long time1 = Time2-Time1;long time2 = Time3-Time2;long time3 = Time4-Time3;
        //输出
//        System.out.println( "耗时："+time1+"。"+time2+"/"+time3);
        System.out.println( "Tenengrad评价值："+tenengrad+"。");
        return tenengrad;
    }

    public static double TenengradNew( Mat img ){
        long Time1 =  System.currentTimeMillis();
//        Mat grayMat = new Mat();
//        Imgproc.cvtColor(img, grayMat , Imgproc.COLOR_RGB2GRAY );
        //求sobel导数
        Mat sobelx = new Mat();
        Mat sobely = new Mat();
        Imgproc.Scharr( img , sobelx , CV_32F ,1 ,0 );
        Imgproc.Scharr( img , sobely , CV_32F ,0 ,1 );
        long Time2 =  System.currentTimeMillis();
        //分别求均方根
        Core.multiply(sobelx,sobelx,sobelx);
        Core.multiply(sobely,sobely,sobely);
        Mat tenengradMat = new Mat();
        Core.add(sobelx,sobely,tenengradMat);
        long Time3 =  System.currentTimeMillis();
        double[] means = new double[3];double[] devs = new double[3];
        MeanDev( tenengradMat , means , devs);
        double tenengrad = (means[0]+means[1]+means[2])/3;
        long Time4 =  System.currentTimeMillis();
        long time1 = Time2-Time1;long time2 = Time3-Time2;long time3 = Time4-Time3;long timeAll = Time4-Time1;
        //输出
        System.out.println( "New耗时："+time1+"ms/"+time2+"ms/"+time3+"ms/="+timeAll+"ms");
        System.out.println( "Tenengrad评价值："+tenengrad+"。");
        return tenengrad;
    }

    public static double TenengradVariance( Mat img ){
        //求sobel导数
        Mat sobel = new Mat();
        Imgproc.Sobel(img,sobel, CV_32F,1,1);
        MatOfDouble mean = new MatOfDouble();
        MatOfDouble dev = new MatOfDouble();
        Core.meanStdDev(sobel,mean,dev);
         double[] devs = new double[3];
        for(int i=0;i<3;i++){
            double[] b = new double[1];
            dev.get(i,0,b);
            devs[i] = b[0];
        }
        //输出
//        System.out.println( "TenengradVariance："+devs[0]+","+devs[1]+","+devs[2]+"。");
        return (devs[0]+devs[1]+devs[2])/3;
    }

    /**
     * 计算图像均方根
     * @param img
     * @return
     */
    public static double[] RMS(Mat img){
        double numPixel = img.rows()*img.cols();
        int channels = img.channels();
        double[] data = new double[channels];
        double channel1 = 0;
        double channel2 = 0;
        double channel3 = 0;
        for(int i=0 ; i<img.rows() ; i++ ){
            for(int j=0 ; j<img.cols(); j++ ){
                for(int k = 0 ; k<channels ; k++) {
                    double[] value = img.get(i, j);
                    data[k] = data[k]+pow(value[k],2);
                }
            }
        }
        for(int k = 0 ; k<channels ; k++) {
            data[k] = sqrt( data[k]/numPixel);
        }
        return data;
    }

    public static double EnergyOfLaplacian(Mat img){
        Mat dst = new Mat();
        Imgproc.Laplacian(img,dst,CV_32F);
        double[] EOL = RMS(dst);
        double[] EOLL = {pow(EOL[0],2),pow(EOL[1],2),pow(EOL[2],2)};
        //输出
//        System.out.println( "EnergyofLaplacian："+EOLL[0]+","+EOLL[1]+","+EOLL[2]+"。");
        return (EOLL[0]+EOLL[1]+EOLL[2])/3;
    }

    /**
     * laplacian方差
     * @param img
     * @return
     */
    public static double VarianceOfLaplacian(Mat img){
        Mat dst = new Mat();
        Imgproc.Laplacian(img,dst,CV_32F);
        double[] means = new double[3];double[] devs = new double[3];
        MeanDev( dst , means , devs);
        //输出
//        System.out.println( "VarianceOfLaplacian："+devs[0]+","+devs[1]+","+devs[2]+"。");
        return (devs[0]+devs[1]+devs[2])/3;
    }

    /**
     * 灰度值方差
     * @param img
     * @return
     */
    public static double GrayLevelVariance(Mat img){
        double[] means = new double[3];double[] devs = new double[3];
        MeanDev( img , means , devs);
        return (devs[0]+devs[1]+devs[2])/3;
    }

//    /**
//     * 使用laplacian算子进行的处理
//     * @param img
//     * @return
//     */
//    public static double GrayLevelLocalVariance( Mat img ,int n){
//        Mat dstX = new Mat();
//        Mat laplacianX = new Mat(n,n, CV_32FC1);
//
//        float[] data = new float[n];
//        Arrays.fill(data,1.0f/n );
//        int indexCenter = n*n/2+1;
//        data[indexCenter] = 1.0f - 1.0f/n;
//        laplacianX.put(0 ,0 , data );
//        Imgproc.filter2D( img , dstX , CV_32F , laplacianX );
//
//    }

    public static double HistogramEntropy(Mat img , int num ){
        Mat grayImg = new Mat();
        Imgproc.cvtColor(img,grayImg,Imgproc.COLOR_BGR2GRAY);
        List<Mat> images = new ArrayList<>();
        images.add(grayImg);
        Mat mask = Mat.ones(img.size(),CvType.CV_8UC1);
        Mat hist = new Mat();
        Imgproc.calcHist(images,new MatOfInt(0),mask,hist,new MatOfInt(256),new MatOfFloat(0,255));
        Core.normalize(hist,hist,0,255,Core.NORM_MINMAX);
        double[] bbb = new double[256];
        for(int i = 0 ; i< 256 ; i++){
            double[] a = hist.get(i,0);
            bbb[i] = a[0];
        }
        Arrays.sort(bbb );
        double histogramEntropy = 0;
        for(int j = 200 ; j>255-num ; j=j-20){
            histogramEntropy = histogramEntropy+bbb[j]*log(bbb[j])*-1;
        }
//        System.out.println( "HistogramEntropy："+ histogramEntropy );
        return histogramEntropy;
    }

}
