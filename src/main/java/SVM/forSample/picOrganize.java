package SVM.forSample;

import libsvm.svm_predict;
import libsvm.unit.svm;
import libsvm.unit.svm_model;
import libsvm.unit.svm_result;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import unit.featureExtractionFunction;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;

import static org.opencv.core.CvType.CV_32FC1;
import static org.opencv.imgcodecs.Imgcodecs.IMREAD_GRAYSCALE;
import static org.opencv.imgcodecs.Imgcodecs.imread;

/**
 * @className: picOrganize
 * @description: 通过二值图文件名，将对应的双目图像放置到对应分类的文件夹中
 * @author: Lin Guifeng
 * @date: 2022/06/22 21:50
 * @version: 1.0
 **/
public class picOrganize {
    static {
        // 动态链接opencv
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
    }

    public static void main(String[] args){

        String[] terrainName = { "LG" , "OB" , "US" , "DS" , "UR" , "DR" };
        for (int indexTerrain = 0 ; indexTerrain<terrainName.length;indexTerrain++) {
            String folderName = terrainName[indexTerrain];
            String picFolder = "F:\\stereoCamera\\PCread\\picwithIMU\\databaseAll\\binaryPic\\"+folderName+"\\";
            File folder = new File(picFolder);
            File[] file = folder.listFiles();

            for (File binaryPicFile : file) {
                String binaryPicPath = binaryPicFile.getAbsolutePath();
                System.out.println("读取文件：" + binaryPicFile);

                String picPath = binaryPicPath.replace("binaryPic\\"+folderName, "picture\\all");
                File picFile = new File(picPath);
                if(picFile.exists()){
                    String picClassFileName = picPath.replace("all",folderName);
                    File picClassFile = new File(picClassFileName);
                    boolean re = picFile.renameTo(picClassFile);
                    if (re) System.out.println("已找到原始图像并移动至对应分区");
                }

            }
        }

    }


}
