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
 * @className: libsvmCheck
 * @description: 使用训练好的分类器对文件夹内的图像进行判别，将错误图像存至另外的文件夹
 * @author: Lin Guifeng
 * @date: 2022/06/18 21:50
 * @version: 1.0
 **/
public class libsvmCheck {
    static {
        // 动态链接opencv
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
    }
    public static svm_model svmModel;
    //模型文件地址
    static String[] terrainName = { "LG" , "OB" , "US" , "DS" , "UR" , "DR" };
//    static String[] terrainName = { "US" , "UR" };
    public static String svmModelPath = "F:\\stereoCamera\\PCread\\picwithIMU\\dataBaseAll0710\\binaryPic\\20_train.model";
    public static int label = 4;
    public static String folderName = terrainName[label];
    public static String newFolderName = folderName+"W";
    public static String picFolder = "F:\\stereoCamera\\PCread\\picwithIMU\\dataBaseAll0710\\binaryPic\\"+folderName+"\\";

    public static void main(String[] args){

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
                svmModel = svm.svm_load_model(svmModelPath);
                svm_result result = svm_predict.predict(featureMat,svmModel,1);
                System.out.println("svm recognition result：" + result.getPredict_label());
                System.out.println("svm recognition probability estimates：" + Arrays.toString(result.getProb_estimates()));
//                if(label!=result.getPredict_label()){
                if(result.getPredict_label()!=label ){

                    String newPath = binaryPicPath.replace(folderName,newFolderName);
                    File reNameFile = new File(newPath);
                    boolean re = binaryPicFile.renameTo(reNameFile);
                    if(re) System.out.println("错误图像，已移动");
                }
            } catch (IOException e) {
                e.printStackTrace();
            }



        }

    }


}
