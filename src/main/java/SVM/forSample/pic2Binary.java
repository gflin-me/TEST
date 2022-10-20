package SVM.forSample;

import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.imgproc.Imgproc;
import unit.StereoBMUtil;
import unit.pcProcess;

import java.io.File;
import java.io.IOException;

import static org.opencv.imgcodecs.Imgcodecs.imread;
import static org.opencv.imgcodecs.Imgcodecs.imwrite;
import static unit.featureExtractionFunction.binaryMorphologyEx;
import static unit.pcProcess.PointCloud2Binary;

/*
读取指定文件夹内的双目图像，执行双目匹配获取深度图，深度图获取点云数据，剔除指定范围外的点云数据，对点云进行矫正并投影为二值图像
即读取【双目图像】，输出【二值图】
操作说明：指定变量【defaultPath】:"F:\\stereoCamera\\PCread\\picwithIMU\\picture\\default"
                二值图将保存至："F:\\stereoCamera\\PCread\\picwithIMU\\binaryPic\\default"
更新时间：2021/10/08
 */
public class pic2Binary {
    static { System.loadLibrary(Core.NATIVE_LIBRARY_NAME); }// 动态链接opencv

    private static final StereoBMUtil stereoBMUtil = new StereoBMUtil(1 ,1  , 170 ,220 );
    public static void main(String[] args) throws IOException {
        //图像文件路径
        String defaultPath = "F:\\stereoCamera\\PCread\\picwithIMU\\database0708\\picture\\terrainFolder";
        String saveFolder = "binaryPic";
        String[] terrainName = { "LG" , "OB" , "US" , "DS" , "UR" , "DR" } ;
        //范围【 xMin , xMax , yMin , yMax , zMin , zMax 】
        double[] range = { -200 , 200   , 200 , 1750 , -2400 , 800 };
        //点云采样率
        int rate = 2;
        //不同类别的文件夹历遍
        for (int indexTerrain = 0 ; indexTerrain<terrainName.length ;indexTerrain++) {
            //读取图片，计算深度图，点云图
            String terrain = terrainName[indexTerrain];
            String filePath = defaultPath.replace("terrainFolder", terrain);
            File file = new File(filePath);
            File[] fs = file.listFiles();
            //文件夹内图像历遍
            assert fs != null;
            for (File picFile : fs) {
                if (!picFile.isDirectory()) {   //如果不是该路径下的目录，即是一个文件
                    System.out.println(picFile);
                    //获取图像
                    String picPath = picFile.getAbsolutePath();           //获取文件绝对路径
                    Mat frameBit = imread(picPath);                 //读取图像
                    //双目匹配算法
                    Mat depthImg = stereoBMUtil.SGBMCompute(frameBit);
                    Mat mat3d = stereoBMUtil.get3Dmat();                //获取点云图
                    Mat mat3dNew = new Mat();
//                    Imgproc.bilateralFilter(mat3d , mat3dNew , 10 , 40 , 45 );//高斯双边滤波
                    //获取欧拉角
                    String fileName = picFile.getName();
                    double[] Euler = pcProcess.findEuler(fileName);
                    //投影二值图
                    Mat binaryPic = PointCloud2Binary( mat3d , Euler, rate , range);
//                    binaryMorphologyEx( binaryPic ,1,5,12);
                    binaryMorphologyEx(binaryPic, 1, 5, 12);
                    //保存图像
                    String binaryPicPath = picPath.replace("picture", saveFolder);
                    imwrite(binaryPicPath, binaryPic);
                }

            }
        }

    }

}


