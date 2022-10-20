package SVM.modelTrain;

import libsvm.svm_predict;
import libsvm.unit.svm;
import libsvm.unit.svm_model;
import org.opencv.core.Core;

import java.io.*;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;


/**
 * @className: libsvmValidation
 * @description: libsvm进行数据集的验证，输出识别率
 * @author: Lin Guifeng
 * @date: 2022/06/12
 * @version: 1.0
 **/
public class libsvmValidation {
    // 动态链接opencv
    static { System.loadLibrary(Core.NATIVE_LIBRARY_NAME); }
    static int num = 16  ;
    //识别样本地址
    public static String testDataPath = "F:\\stereoCamera\\PCread\\picwithIMU\\database5class\\binaryPic\\"+num+"_test.txt";
    //模型文件地址
    public static String svmModelPath = "F:\\stereoCamera\\PCread\\picwithIMU\\database5class\\binaryPic\\"+num+"_train.model";
    //结果输出地址
    public static String resultPath = testDataPath.replace(".txt","_result.txt");
    //svm模型
    public static svm_model svmModel;
    private static svm_predict svmPredict;

    public static void main(String[] args){
        //------------------------------------样本数据【训练】&【识别】------------------------------------
        try {
            //模型导入
            svmModel = svm.svm_load_model(svmModelPath);
            //将训练数据导入进行验证
            FileReader trainDataFile = new FileReader(testDataPath);
            BufferedReader trainDataReader = new BufferedReader(trainDataFile);
            //训练结果输出流
            DataOutputStream resultOutput = new DataOutputStream(new  FileOutputStream(resultPath));
            //识别
            List<Double> resultLabel = svmPredict.predict(trainDataReader,resultOutput,svmModel,1);
            List<Double> targetLabel = svmPredict.getLabelTarget(); //获取样本数据标签
            int[][] refuseM = getRefuseMatrix(resultLabel,targetLabel,6);
            double[] rate = getRate(refuseM);
            double[][] refuseMR = getRefuseMR(refuseM);
            System.out.println(Arrays.deepToString(refuseM));
            System.out.println(Arrays.deepToString(refuseMR));
            double rateAvg = 0;
            for (double rateP:rate) {
                System.out.println(rateP+" / ");
                rateAvg = rateAvg+rateP;
            }
            rateAvg = (rateAvg-rate[rate.length-1])/(rate.length-1);
            System.out.println("【识别率平均值】："+rateAvg);

        } catch (IOException e) {
            e.printStackTrace();
        }

        //------------------------------------单样本识别测试---------------------------------

//        String picPath = "F:\\stereoCamera\\PCread\\picwithIMU\\camera2_70det_60fps_1214_02\\LG\\" +
//                "1214_10.48.23.200_-44.159546_1.774292_-151.29822.png";
//        Mat bianryPic = imread(picPath, IMREAD_GRAYSCALE);
//        double[] features = featureExtractionFunction.SVM.modelTrain.featuresExtraction(bianryPic);
//        Mat featureMat = new Mat(1 , features.length , CV_32FC1 );
//        Mat predictResult = new Mat();
//        for (int i = 0; i < features.length; i ++){
//            featureMat.put( 0 , i , features[i]);
//        }
//        try {
//            svmModel = svm.svm_load_model(modelPath);
//            svm_result result = svm_predict.predict(featureMat,svmModel,1);
//            System.out.println("svm recognition result："+result.getPredict_label());
//            System.out.println("svm recognition probability estimates："+ Arrays.toString(result.getProb_estimates()));
//        } catch (IOException e) {
//            e.printStackTrace();
//        }

    }

    /**
     * 获取混淆矩阵
     * @param resultLabel 分类结果标签
     * @param targetLabel 样本类别标签
     * @param numClass 类别数量
     * @return 混淆矩阵
     */
    public static int[][] getRefuseMatrix(List<Double> resultLabel,List<Double> targetLabel , int numClass){
        int[][] refuseMatrix = new int[numClass][numClass];
        //类别标签决定行、结果标签决定列
        for(int i=0;i<resultLabel.size();i++){
            int row = targetLabel.get(i).intValue();
            int col = resultLabel.get(i).intValue();
            refuseMatrix[row][col]++;
        }
        return refuseMatrix;
    }

    /**
     * 根据混淆矩阵计算识别率和总识别率
     * @param refuseMatrix 混淆矩阵
     * @return 识别率数组，最后一个是总识别率
     */
    public static double[] getRate(int[][] refuseMatrix){
        int numClass = refuseMatrix.length;
        double[] rate = new double[numClass+1];//输出识别率对象
        int total = 0,totalRight = 0;
        for(int i=0;i<numClass;i++){
            int all=0;
            for(int j = 0; j<numClass;j++){
                all=all+refuseMatrix[i][j];
                total = total+refuseMatrix[i][j];
                if(i==j) totalRight = totalRight+refuseMatrix[i][j];
            }
            rate[i]=(float)refuseMatrix[i][i]/all;
        }
        rate[rate.length-1] = (float)totalRight/total;
        return rate;
    }

    public static double[][] getRefuseMR(int[][] refuseMatrix){
        int numClass = refuseMatrix.length;
        double[][] refuseMR = new double[numClass][numClass];
        for(int i=0;i<numClass;i++){
            int all=0;
            for(int j = 0; j<numClass;j++){
                all=all+refuseMatrix[i][j];
            }
            for(int j = 0; j<numClass;j++){
                refuseMR[i][j] = (float)refuseMatrix[i][j]/all;
            }
        }
        return refuseMR;
    }

}
