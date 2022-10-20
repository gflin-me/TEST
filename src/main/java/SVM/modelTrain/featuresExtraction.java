package SVM.modelTrain;

import org.opencv.core.*;
import org.opencv.imgproc.Imgproc;
import unit.pcProcess;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import static org.opencv.imgcodecs.Imgcodecs.IMREAD_GRAYSCALE;
import static org.opencv.imgcodecs.Imgcodecs.imread;
import static org.opencv.imgproc.Imgproc.circle;
import static org.opencv.imgproc.Imgproc.rectangle;
import static unit.featureExtractionFunction.*;

/**
 * @className: featuresExtraction
 * @description: 读取【二值图像】，对二值图像进行处理，输出特征数据文件
 * @operation: 指定文件路径【defaultPath】 :二值图像文件夹、指定子文件夹名称【terrainName】
 *              指定特征数量【numFea】、指定特征向量内容【featurePer】
 *              输出样本特征txt文件
 * @author: Lin Guifeng
 * @date: 2022/06/23 21:11
 * @version: 1.0
 **/
public class featuresExtraction {


    static {
    // 动态链接opencv
    System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
    }

    public static void main(String[] args) throws IOException {
        String[] terrainName = { "LG" , "OB" , "US" , "DS" , "UR" , "DR" };
//        String[] terrainName = { "LG" , "OB"};
//        String[] terrainName = {"DS" ,"DR"};
//        String[] terrainName = {"LGOB" ,"USUR","DSDR"};
//        String[] terrainName = { "US" , "UR"};
        String defaultPath = "F:\\stereoCamera\\PCread\\picwithIMU\\database5class\\binaryPic\\default\\";
        int numFea = 20 ;                                //【特征数量】

        //当前日期时间
        SimpleDateFormat formatter = new SimpleDateFormat ("yyyyMMddHHmm");
        Date curDate =  new Date(System.currentTimeMillis());
        String timeNow =   formatter.format(curDate) ;

        for (int indexTerrain = 0 ; indexTerrain<terrainName.length;indexTerrain++) {

            String terrain = terrainName[indexTerrain];
            String binaryPath = defaultPath.replace("default", terrain);
            File file = new File(binaryPath);//获取文件夹对象
            File[] fs = file.listFiles();    //获取文件夹下的文件列表，包括文件和子文件夹

            int numFile = fs.length;                            //文件夹中图片的数量
            double[][] features = new double[numFile][numFea]; //特征表
            Mat element = Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new Size(11, 1), new Point(-1, -1));
            //循环读取二值图像
            int indexPic = 0;//图片序号索引
            for (File binaryPicFile : fs) {
                //读取图像
                String binaryPicPath = binaryPicFile.getAbsolutePath();   //获取路径
                System.out.println("读取文件：" + binaryPicFile);
                Mat binaryPic = imread(binaryPicPath, IMREAD_GRAYSCALE);//读取二值图像
//                binaryMorphologyEx(binaryPic, 1, 5, 12);
//                String binaryPicProcessPath = binaryPicPath.replace("binaryPic", "binaryPicProcessing2");
//                    imwrite(binaryPicProcessPath, binaryPic);
                if (Core.countNonZero(binaryPic)!=0) {
                /*
                【【特征提取】】
                 */
                    double widthPic = binaryPic.width();
                    double heightPic = binaryPic.height();
                    //最小外接矩形
                    RotatedRect minRect = findminRect(binaryPic);
                    double width_minRect = minRect.size.width/widthPic;     //最小外接矩形宽
                    double height_minRect = minRect.size.height/heightPic;//最小外接矩形高
                    double angle_minRect = Math.abs(minRect.angle-45)/45;//最小外接矩形倾斜角
                    double yCenter_minRect = minRect.center.y/heightPic;//最小外接矩形中心点y坐标
                    double areaMinRect = width_minRect * height_minRect ;//最小外接矩形面积

                    //外接正矩形
                    Rect rect = findRect(binaryPic);
                    double ytl_rect = rect.y/heightPic;         //外接正矩形左上角点坐标
                    double width_rect = rect.width/widthPic;    //外接正矩形宽度
                    double height_rect = rect.height/heightPic; //外接正矩形高度
                    //最小外接矩形内像素填充度
                    Mat roiAll = new Mat( binaryPic , rect );
                    double numPixelAll = Core.countNonZero(roiAll);
                    double fullness = areaMinRect/numPixelAll;
                    //图像质心
                    Point centroid = findCentroid(binaryPic);
                    double yCentroid = (int) centroid.y/heightPic;
                    //区域密度
                    double[] pixelsDist = pixelsDistribution(binaryPic, rect, centroid);

                    //离散度特征
                    double[] dispersion = Dispersion(binaryPic);
                    //水平线提取
                    double numvertical = VerticalLine( binaryPic );
                    double numhorizontal = HorizontalLine( binaryPic );
                    //特征写入
//                  double[] feaRect = {width_minRect, height_minRect, ytl_rect , yCentroid, dispersion[0] ,dispersion[1] , fullness ,numvertical,numhorizontal};
//                  double[] featurePer = AddArray2(pixelsDist, feaRect);
//                    double[] featurePer = { pixelsDist[0],pixelsDist[1],pixelsDist[2],pixelsDist[3],
//                                            width_minRect, height_minRect,angle_minRect,yCenter_minRect, ytl_rect,
//                                            width_rect,height_rect, yCentroid, dispersion[0],
//                                             numvertical, numhorizontal};
////                    double[] featurePer = { pixelsDist[0],pixelsDist[1],pixelsDist[3],yCenter_minRect, ytl_rect, width_rect,height_rect, yCentroid, numvertical, numhorizontal};
                    double[] feaList = { pixelsDist[0],pixelsDist[1],pixelsDist[2],pixelsDist[3],pixelsDist[4],pixelsDist[5],pixelsDist[6],
                                            width_minRect, height_minRect,angle_minRect,yCenter_minRect, ytl_rect,
                                            width_rect,height_rect, yCentroid, dispersion[0],dispersion[1],fullness,
                                             numvertical, numhorizontal};//20个特征完整版列表
                    double[] featurePer = { feaList[0], feaList[1], feaList[2], feaList[3],feaList[4],feaList[5],feaList[6],feaList[7],
                            feaList[8],feaList[9], feaList[10], feaList[11], feaList[12], feaList[13], feaList[14],feaList[15],
                            feaList[16], feaList[17],feaList[18], feaList[19]};
//                    double[] featurePer = { feaList[0], feaList[1], feaList[2], feaList[3],feaList[7],feaList[8],feaList[9],
//                                        feaList[10], feaList[11], feaList[12], feaList[13], feaList[14],feaList[15],  feaList[18], feaList[19]};
                    //数据写入矩阵
                    features[indexPic] = scale(featurePer,0,1,-1,1);
                    indexPic++;


                    //画质心
//                    circle(binaryPic, centroid, 33, new Scalar(255, 0, 0));
//                    drawRotatedRect( binaryPic , minRect );
//                    rectangle(binaryPic,rect.br(),rect.tl(),new Scalar(255,255,255),2,8,0);

//                    //标识后图像保存
//                    String binaryPicProcessPath = binaryPicPath.replace("binaryPicMorphology", "binaryPicSign");
//                    imwrite(binaryPicProcessPath, binaryPic);

                }else{
//                    features[indexPic] = 0;
//                    indexPic++;
//                    String binaryPicProcessPath = binaryPicPath.replace("binaryPic", "binaryPicProcessing");
//                    imwrite(binaryPicProcessPath, binaryPic);
                }
            }


            //特征数据保存(分类保存)
//            String saveName = terrain+".txt";
//            String savePath = defaultPath.replace("default\\", saveName );
//            pcProcess.saveAsTxt4SVM(features , indexTerrain , savePath , false);
//            pcProcess.saveAsTxt( features , savePath );
            //以libsvm所需格式保存
            String savePath_svm = defaultPath.replace("default\\", timeNow+"_allData.txt" );
//            pcProcess.saveAsTxt4SVM(features , indexTerrain , savePath_svm , true);
            pcProcess.saveAsTxt4SVM_twoFile(features , indexTerrain , savePath_svm , true);

        }
        System.out.println("程序完成！" );
    }

    /**
     * 将外接矩形区域内以质心为中心划分为4个区域，计算四个区域的像素密度，归一化后输出
     * @param binaryPic 二值图
     * @param rect      外接正矩形
     * @param centroid  质心点
     * @return  int[]   四个区域的归一化像素密度
     */
    public static double[] pixelsDistribution (Mat binaryPic, Rect rect , Point centroid ){
        Point tl = rect.tl();
        Point br = rect.br();
        Point tr = new Point( br.x , tl.y );
        Point bl = new Point( tl.x , br.y );
        Rect rect1 = new Rect( tl , centroid );//左上角区域
        Rect rect2 = new Rect( tr , centroid );//右上角区域
        Rect rect3 = new Rect( bl , centroid );//左下角区域
        Rect rect4 = new Rect( br , centroid );//右下角区域

        Mat roiAll = new Mat( binaryPic , rect );
        Mat roi1 = new Mat( binaryPic , rect1 );
        Mat roi2 = new Mat( binaryPic , rect2 );
        Mat roi3 = new Mat( binaryPic , rect3 );
        Mat roi4 = new Mat( binaryPic , rect4 );
        double numPixelAll = Core.countNonZero(roiAll);
        double numPixel1 = Core.countNonZero(roi1)/numPixelAll;
        double numPixel2 = Core.countNonZero(roi2)/numPixelAll;
        double numPixel3 = Core.countNonZero(roi3)/numPixelAll;
        double numPixel4 = Core.countNonZero(roi4)/numPixelAll;
        double pixelsDistLevel = (numPixel1+numPixel3)/(numPixel2+numPixel4)/numPixelAll;
        double pixelsDistVertical = (numPixel1+numPixel2)/(numPixel3+numPixel4)/numPixelAll;
        double pixelsDistDiagonal = (numPixel1+numPixel4)/(numPixel2+numPixel3)/numPixelAll;
//        return new double[] { numPixel1 ,numPixel2 ,numPixel3 , numPixel4 };
        return new double[] {numPixel1 ,numPixel2 ,numPixel3 , numPixel4 , pixelsDistLevel,pixelsDistVertical,pixelsDistDiagonal};
    }

    /**
     * 数据缩放
     * @param input
     * @param lower
     * @param upper
     * @param lowerNew
     * @param upperNew
     * @return
     */
    public static double[] scale(double[] input , double lower , double upper , double lowerNew, double upperNew ){
        double scale = (upperNew-lowerNew)/(upper-lower);
        double[] output = new double[input.length];
        for(int i=0;i<input.length;i++){
            output[i] = (input[i]-lower)*scale+lowerNew;
        }
        return output;
    }

}


