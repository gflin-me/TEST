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
 * @className: findBinary
 * @description: 用于对新采集的图像二值图，使用已训练好的模型对未分类的二值图进行分类并存入对应的文件夹
 * @author: Lin Guifeng
 * @date: 2022/07/12 15:23
 * @version: 1.0
 **/
public class findBinary {
    static { System.loadLibrary(Core.NATIVE_LIBRARY_NAME); }// 动态链接opencv

    public static svm_model svmModel;
    //模型文件地址
    public static String svmModelPath = "F:\\stereoCamera\\PCread\\picwithIMU\\dataBaseAll0710\\binaryPic\\20_train.model";
    //二值图文件夹
    public static String picFolder = "F:\\stereoCamera\\PCread\\picwithIMU\\database0708\\binaryall_sy";
    //分类后存入的文件夹（该文件夹下需包含各分类的子文件夹）
    public static String picSaveFolder = "F:\\stereoCamera\\PCread\\picwithIMU\\database0708";

    public static void main(String[] args){
        String[] terrainName = { "LG" , "OB" , "US" , "DS" , "UR" , "DR" };
        File folder = new File(picFolder);
        File[] file = folder.listFiles();

        for(File binaryPicFile:file){
            String binaryPicPath = binaryPicFile.getAbsolutePath();
            System.out.println("读取文件：" + binaryPicFile);

            Mat bianryPic = imread(binaryPicPath, IMREAD_GRAYSCALE);
            double[] feaList = featureExtractionFunction.featuresExtraction(bianryPic);
            double[] features = { feaList[0], feaList[1], feaList[2], feaList[3],feaList[4],feaList[5],feaList[6],feaList[7],
                    feaList[8],feaList[9], feaList[10], feaList[11], feaList[12], feaList[13], feaList[14],feaList[15],
                    feaList[16], feaList[17],feaList[18], feaList[19]};
            Mat featureMat = new Mat(1 , features.length , CV_32FC1 );
            for (int i = 0; i < features.length; i ++){
                featureMat.put( 0 , i , features[i]);
            }
            try {
                //识别
                svmModel = svm.svm_load_model(svmModelPath);
                svm_result result = svm_predict.predict(featureMat,svmModel,1);
                System.out.println("svm recognition result：" + result.getPredict_label());
                //根据结果存入对应文件夹
                int label = result.getPredict_label();
                String newPath = picSaveFolder+"\\"+terrainName[label]+"\\"+binaryPicFile.getName();
                File reNameFile = new File(newPath);
                boolean re = binaryPicFile.renameTo(reNameFile);
                if(re) System.out.println("已分类至"+terrainName[label]);

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
