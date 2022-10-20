package SVM.FeatureSelect;

import org.opencv.core.*;
import org.opencv.imgproc.Imgproc;
import unit.pcProcess;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;

import static org.opencv.imgcodecs.Imgcodecs.IMREAD_GRAYSCALE;
import static org.opencv.imgcodecs.Imgcodecs.imread;
import static unit.featureExtractionFunction.*;
import static unit.featureExtractionFunction.AddArray2;

/**
 * @className: featureSelect
 * @description: 保存单个特征的txt文件
 * @author: Lin Guifeng
 * @date: 2022/06/27 20:58
 * @version: 1.0
 **/
public class featureSelect {
    static { System.loadLibrary(Core.NATIVE_LIBRARY_NAME); }// 动态链接opencv

    public static void main(String[] args) throws IOException {
        String[] terrainName = {"LG", "US", "DS", "UR", "DR"};// "OB",
//        String[] terrainName = {"LGOB", "USUR", "DSDR"};
        String defaultPath = "F:\\stereoCamera\\PCread\\picwithIMU\\database5class\\binaryPic\\default\\";
        int numFeatures =20;                                //特征数量

        //当前日期时间
        SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMddHHmm");
        Date curDate = new Date(System.currentTimeMillis());
        String timeNow = formatter.format(curDate);

        for (int indexTerrain = 0; indexTerrain < terrainName.length; indexTerrain++) {

            String terrain = terrainName[indexTerrain];
            String binaryPath = defaultPath.replace("default", terrain);
            File file = new File(binaryPath);//获取文件夹对象
            File[] fs = file.listFiles();    //获取文件夹下的文件列表，包括文件和子文件夹

            int numFile = fs.length;                            //文件夹中图片的数量
            double[][] features = new double[numFile][numFeatures]; //特征表
            Mat element = Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new Size(11, 1), new Point(-1, -1));
            //循环读取二值图像
            int indexPic = 0;//图片序号索引
            for (File binaryPicFile : fs) {
                //读取图像
                String binaryPicPath = binaryPicFile.getAbsolutePath();   //获取路径
                System.out.println("读取文件：" + binaryPicFile);
                Mat binaryPic = imread(binaryPicPath, IMREAD_GRAYSCALE);//读取二值图像

                if (Core.countNonZero(binaryPic) != 0) {
                /*
                【【特征提取】】
                 */
                    double widthPic = binaryPic.width();
                    double heightPic = binaryPic.height();
                    //最小外接矩形
                    RotatedRect minRect = findminRect(binaryPic);
                    double width_minRect = minRect.size.width / widthPic;     //最小外接矩形宽
                    double height_minRect = minRect.size.height / heightPic;//最小外接矩形高
                    double angle_minRect = Math.abs(minRect.angle - 45) / 45;//最小外接矩形倾斜角
                    double yCenter_minRect = minRect.center.y / heightPic;//最小外接矩形中心点y坐标
                    double areaMinRect = width_minRect * height_minRect;//最小外接矩形面积

                    //外接正矩形
                    Rect rect = findRect(binaryPic);
                    double ytl_rect = rect.y / heightPic;         //外接正矩形左上角点坐标
                    double width_rect = rect.width / widthPic;    //外接正矩形宽度
                    double height_rect = rect.height / heightPic; //外接正矩形高度

                    Mat roiAll = new Mat(binaryPic, rect);
                    double numPixelAll = Core.countNonZero(roiAll);
                    double fullness = areaMinRect / numPixelAll;//最小外接矩形内像素填充度
                    //图像质心
                    Point centroid = findCentroid(binaryPic);
                    double yCentroid = (int) centroid.y / heightPic;//图像质心y坐标
                    //区域密度
                    double[] pixelsDist = pixelsDistribution(binaryPic, rect, centroid);//7维区域密度特征

                    //离散度特征
                    double[] dispersion = Dispersion(binaryPic);//离散度
                    //水平线提取
                    double numvertical = VerticalLine(binaryPic);//垂直结构元素操作
                    double numhorizontal = HorizontalLine(binaryPic);//水平结构元素操作
                    //特征写入
                    double[] featurePer = { pixelsDist[0],pixelsDist[1],pixelsDist[2],pixelsDist[3],pixelsDist[4],pixelsDist[5],pixelsDist[6],
                            width_minRect, height_minRect,angle_minRect,yCenter_minRect, ytl_rect,
                            width_rect,height_rect, yCentroid, dispersion[0],dispersion[1],fullness,
                            numvertical, numhorizontal};//20个特征完整版列表
                    //数据写入矩阵
//                    features[indexPic] = featurePer;
                    features[indexPic] = scale(featurePer,0,1,-1,1);
                    indexPic++;

                }


            }
            for(int i=0;i<numFeatures;i++) {
                //以libsvm所需格式保存
                double[] features_single = getRow(features,i);
                String savePath_svm = defaultPath.replace("default\\",  "0features\\"+i+".txt");
                saveAsTxt4SVM_twoFile(features_single, indexTerrain, savePath_svm, true);
            }
        }
        System.out.println("特征提取程序完成！");

    }

    public static double[] getRow(double[][] input ,int index){
        if(index>=input[0].length) return null;
        int length = input.length;
        double[] output = new double[length];
        for( int i=0 ; i < length ; i++ ){
            output[i] = input[i][index];
        }
        return output;
    }
    /**
     * 将二维数组保存为libsvm能够识别的txt文件格式
     * [label] [index1]:[value1] [index2]:[value2] …
     * @param data  保存的特征值
     * @param label 特征值对应标签
     * @param savePath 文件名
     * @param append true从地址对应文件末尾写入文本
     */
    public static void saveAsTxt4SVM_twoFile(double[] data ,double label, String savePath ,boolean append){
        try {
            FileWriter output1 = new FileWriter( savePath , append );

            for (int i = 0 ; i < data.length ; i++ ){
                output1.write((int)label+"\t");
                if(Double.isNaN(data[i]) || Double.isInfinite(data[i])) {
                    output1.write( "1:" + 0 + "\t");
                    System.out.println("NaN or Infinity in features data in line"+i+". ");
                }else{
                    output1.write( "1:" + data[i] + "\t");
                }
                output1.write("\n");
            }
            output1.close();

        } catch (IOException e) {
            e.printStackTrace();
        }

    }


}
