package SVM.FeatureSelect;

import org.opencv.core.*;
import org.opencv.imgproc.Imgproc;
import unit.pcProcess;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;

import static org.opencv.imgcodecs.Imgcodecs.IMREAD_GRAYSCALE;
import static org.opencv.imgcodecs.Imgcodecs.imread;
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
public class featuresExtraction_RF {
    static { System.loadLibrary(Core.NATIVE_LIBRARY_NAME); }// 动态链接opencv

    public static void main(String[] args) throws IOException {
        String[] terrainName = { "LG" , "US" , "DS" , "UR" , "DR" };//, "OB"
        String defaultPath = "F:\\stereoCamera\\PCread\\picwithIMU\\database5class\\binaryPic\\default\\";
        int numFea = 20 ;//【特征数量】
//        int[] rankFea = {16,19,13,3,0,12,14,11,1,7,15,6,2,18,10,8,9,5,4,17};
//        int[] rankFea = {19,13,3,0,12,14,11,1,7,15,2,18,10,8,9,4,17,6,5,16};
        int[] rankFea = {14,11,17,4,5,6,3,0,13,8,15,2,19,1,12,10,7,9,18,16};//0708
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
            double[][] features = new double[numFile][numFea];  //特征表
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
                    double[] feaList = { pixelsDist[0],pixelsDist[1],pixelsDist[2],pixelsDist[3],pixelsDist[4],pixelsDist[5],pixelsDist[6],
                                            width_minRect, height_minRect,angle_minRect,yCenter_minRect, ytl_rect,
                                            width_rect,height_rect, yCentroid, dispersion[0],dispersion[1],fullness,
                                             numvertical, numhorizontal};//20个特征完整版列表
                    double[] featurePer = { feaList[0], feaList[1], feaList[2], feaList[3],feaList[4],feaList[5],feaList[6],feaList[7],
                            feaList[8],feaList[9], feaList[10], feaList[11], feaList[12], feaList[13], feaList[14],feaList[15],
                            feaList[16], feaList[17],feaList[18], feaList[19]};

                    //数据写入矩阵
                    features[indexPic] = scale(featurePer,0,1,-1,1);
                    indexPic++;

                }else{

                }
            }
            String savePath_svm = defaultPath.replace("default\\", numFea + ".txt");
            pcProcess.saveAsTxt4SVM_twoFile(features, indexTerrain, savePath_svm, true);

            //以libsvm所需格式保存

            for(int k=rankFea.length-1;k>0;k--) {
                if(k==rankFea.length-1) {
                    double[][] out = deleteCol(features,rankFea[k]);
                    String savePath = defaultPath.replace("default\\", String.valueOf(k)+".txt");
                    pcProcess.saveAsTxt4SVM_twoFile(out, indexTerrain, savePath, true);
                    saveAsTxt4SVM_twoFileRF(out, indexTerrain, savePath, true);
                }else {
                    int[] del = Arrays.copyOfRange(rankFea, k, rankFea.length);
                    double[][] out = deleteCols(features, del);
                    String savePath = defaultPath.replace("default\\", k + ".txt");
                    pcProcess.saveAsTxt4SVM_twoFile(out, indexTerrain, savePath, true);
                    saveAsTxt4SVM_twoFileRF(out, indexTerrain, savePath, true);
                }
            }

        }
        System.out.println("程序完成！" );
    }

    /**
     * 将二维数组保存为libsvm能够识别的txt文件格式
     * [label] [index1]:[value1] [index2]:[value2] …
     * @param data  保存的特征值
     * @param label 特征值对应标签
     * @param savePath 文件名
     * @param append true从地址对应文件末尾写入文本
     */
    public static void saveAsTxt4SVM_twoFileRF(double[][] data ,double label, String savePath ,boolean append){
        String savePath1 = savePath.replace(".txt","_train");
        String savePath2 = savePath.replace(".txt","_test");
        try {
            FileWriter output1 = new FileWriter( savePath1 , append );
            FileWriter output2 = new FileWriter( savePath2 , append );

            for (int i = 0 ; i < data.length ; i++ ){
                output1.write((int)label+"\t");
                for (int j = 0 ; j < data[0].length ; j++ ) {
                    int index = j+1;
                    if(Double.isNaN(data[i][j]) || Double.isInfinite(data[i][j])) {
                        output1.write(index + ":" + 0 + "\t");
                        System.out.println("NaN or Infinity in features data. ");
                    }else{
                        output1.write(index + ":" + data[i][j] + "\t");
                    }
                }
                output1.write("\n");
                if(i < data.length-1) i++;
                output2.write((int)label+"\t");
                for (int j = 0 ; j < data[0].length ; j++ ) {
                    int index = j+1;
                    if(Double.isNaN(data[i][j]) || Double.isInfinite(data[i][j])) {
                        output2.write(index + ":" + 0 + "\t");
                        System.out.println("NaN or Infinity in features data. ");
                    }else{
                        output2.write(index + ":" + data[i][j] + "\t");
                    }
                }
                output2.write("\n");
            }
            output1.close();
            output2.close();

        } catch (IOException e) {
            e.printStackTrace();
        }

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
     * 缩放特征值
     * @param input 特征
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

    /**
     * 删除二维数组的多列
     * @param input 二维数组
     * @param index 删除的列索引
     * @return 输出数组
     */
    public static double[][] deleteCols(double[][] input ,int[] index){
        //输入数组复制
        double[][] inCopy = new double[input.length][input[0].length];
        for(int i = 0;i < input.length;i++) {
            for (int j = 0; j < input[i].length; j++) {
                inCopy[i][j] = input[i][j];
            }
        }

        int length = input.length;
        int numFea = input[0].length;
        int deleteLength = index.length;
        //输出数组
        Arrays.sort(index);//排序

        for(int k=deleteLength-1;k>=0;k--){
            inCopy = deleteCol(inCopy,index[k]);
        }

        return inCopy;
    }
    /**
     * 删除二维数组的某一列
     * @param input 二维数组
     * @param index 删除的列索引
     * @return 输出数组
     */
    public static double[][] deleteCol(double[][] input ,int index){
        //输入数组复制
        double[][] inCopy = new double[input.length][input[0].length];
        for(int i = 0;i < input.length;i++) {
            for (int j = 0; j < input[i].length; j++) {
                inCopy[i][j] = input[i][j];
            }
        }

        int length = input.length;
        int numFea = input[0].length;
        double[][] output = new double[length][numFea-1];
        for( int i=0 ; i < length ; i++ ){
            for(int j=0;j<numFea;j++){
                if(j<index) output[i][j] = inCopy[i][j];
                if(j>index) output[i][j-1] = inCopy[i][j];
            }
        }
        return output;
    }



}


