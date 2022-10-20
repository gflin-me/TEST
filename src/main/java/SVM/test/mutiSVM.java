package SVM.test;

import libsvm.svm_predict;
import libsvm.unit.svm;
import libsvm.unit.svm_model;
import libsvm.unit.svm_result;
import org.opencv.core.Core;
import org.opencv.core.Mat;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.StringTokenizer;

import static SVM.modelTrain.libsvmValidation.*;
import static org.opencv.core.CvType.CV_32FC1;

/**
 * @className: mutiSVM
 * @description: TODO
 * @author: Lin Guifeng
 * @date: 2022/06/23 21:11
 * @version: 1.0
 **/
public class mutiSVM {
    // 动态链接opencv
    static { System.loadLibrary(Core.NATIVE_LIBRARY_NAME); }

    //识别样本地址
    public static String testDataPath = "F:\\stereoCamera\\PCread\\picwithIMU\\dataBaseAll\\binaryPic\\6Class_test.txt";
    public static String svmModelPath_6Class = "F:\\stereoCamera\\PCread\\picwithIMU\\databaseAll\\binaryPic\\6Class.model";
    public static svm_model sixClassSVM;
    public static String svmModelPath_LGOB = "F:\\stereoCamera\\PCread\\picwithIMU\\databaseAll\\binaryPic\\LGOB.model";
    public static svm_model LGOBSVM;
    public static String svmModelPath_USUR = "F:\\stereoCamera\\PCread\\picwithIMU\\databaseAll\\binaryPic\\USUR.model";
    public static svm_model USURSVM;
    public static List<Double> targetLabel = new ArrayList<>();
    public static List<Double> resultLabel = new ArrayList<>();



    public static void main(String[] args){
        //将训练数据导入进行验证
        FileReader trainDataFile = null;
        BufferedReader trainDataReader;
        try {
            sixClassSVM = svm.svm_load_model(svmModelPath_6Class);
            LGOBSVM = svm.svm_load_model(svmModelPath_LGOB);
            USURSVM = svm.svm_load_model(svmModelPath_USUR);
            trainDataFile = new FileReader(testDataPath);
            trainDataReader = new BufferedReader(trainDataFile);
            while(true) {
                //读取一行样本数据
                String line = trainDataReader.readLine();
                if (line == null) break;
                //通过分隔符取出每个词
                StringTokenizer st = new StringTokenizer(line, " \t\n\r\f:");
                //取出标记的类别标签
                double target_label = atof(st.nextToken());
                targetLabel.add(target_label);
                //计算单个样本的特征数量，因为样本的格式中每个特征前带一个序号，所以要除以2
                int m = st.countTokens() / 2;
                //将特征及其标号存入特征数组
                Mat featureMat = new Mat( 1 , m , CV_32FC1);
                for (int j = 0; j < m; j++) {
                    int a = atoi(st.nextToken());
                    featureMat.put(0 , j , atof(st.nextToken()) );
                }
                //SVM1
                svm_result result_6Class = svm_predict.predict(featureMat, sixClassSVM,1);
                double label = result_6Class.getPredict_label();
                double[] prob = result_6Class.getProb_estimates();
                //SVM2
                if(label==0||label==1){
                    svm_result result_LGOB = svm_predict.predict(featureMat, LGOBSVM,1);
                    double[] prob_LGOB = result_LGOB.getProb_estimates();
                    double probAll = prob[0]+prob[1];
//                    double probLG = (prob_LGOB[0]*probAll+prob[0])*0.5;
//                    double probOB = (prob_LGOB[1]*probAll+prob[1])*0.5;
                    double probLG = prob_LGOB[0]*probAll;
                    double probOB = prob_LGOB[1]*probAll;
                    prob[0] = probLG;
                    prob[1] = probOB;
                    label = findMax(prob);
//                    label = prob[0]>=prob[1] ? 0:1;
                }
                //SVM3
                if(label==2||label==4){
                    svm_result result_USUR = svm_predict.predict(featureMat, USURSVM,1);
                    double[] prob_USUR = result_USUR.getProb_estimates();
                    double probAll = prob[2]+prob[4];
//                    double probLG = (prob_LGOB[0]*probAll+prob[0])*0.5;
//                    double probOB = (prob_LGOB[1]*probAll+prob[1])*0.5;
                    double probLG = prob_USUR[0]*probAll;
                    double probOB = prob_USUR[1]*probAll;
                    prob[2] = probLG;
                    prob[4] = probOB;
                    label = findMax(prob);
//                    label = prob[0]>=prob[1] ? 2:4;
                }
                resultLabel.add(label);
            }
            int[][] refuseM = getRefuseMatrix(resultLabel,targetLabel,6);
            double[] rate = getRate(refuseM);
            double[][] refuseMR = getRefuseMR(refuseM);
            System.out.println(Arrays.deepToString(refuseM));
            System.out.println(Arrays.deepToString(refuseMR));
            double rateAverage = 0;
            for (double rateP:rate) {
                System.out.println(rateP+" / ");
                rateAverage = rateAverage+rateP;
            }


        } catch (IOException e) {
            e.printStackTrace();
        }


    }

    private static double atof(String s) {
        return Double.valueOf(s).doubleValue();
    }

    private static int atoi(String s) {
        return Integer.parseInt(s);
    }

    private static int findMax(double[] arr){
        double max = arr[0];
        int index = 0;
        for (int i = 0; i < arr.length; i++) {
            if(arr[i] >max){
                max= arr[i];
                index = i;

            }
        }
        return index;
    }
}
