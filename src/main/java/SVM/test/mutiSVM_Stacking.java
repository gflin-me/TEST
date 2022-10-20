package SVM.test;

import libsvm.svm_predict;
import libsvm.unit.svm;
import libsvm.unit.svm_model;
import libsvm.unit.svm_result;
import org.opencv.core.Core;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.StringTokenizer;

import static SVM.modelTrain.libsvmValidation.*;

/**
 * @className: mutiSVM
 * @description: TODO
 * @author: Lin Guifeng
 * @date: 2022/06/23 21:11
 * @version: 1.0
 **/
public class mutiSVM_Stacking {
    // 动态链接opencv
    static { System.loadLibrary(Core.NATIVE_LIBRARY_NAME); }

    //识别样本地址
    public static String testDataPath = "F:\\stereoCamera\\PCread\\picwithIMU\\dataBaseAll\\binaryPic\\6c_20fea_test.txt";
    public static String svmModelPath_6Class = "F:\\stereoCamera\\PCread\\picwithIMU\\databaseAll\\binaryPic2\\3c_18fea_train.model";
    public static svm_model triClassSVM;
    public static String svmModelPath_LGOB = "F:\\stereoCamera\\PCread\\picwithIMU\\databaseAll\\binaryPic_LGOB\\LGOB_20fea_train.model";
    public static svm_model LGOBSVM;
    public static String svmModelPath_USUR = "F:\\stereoCamera\\PCread\\picwithIMU\\databaseAll\\binaryPic_USUR\\USUR_20fea_train.model";
    public static svm_model USURSVM;
    public static String svmModelPath_DSDR = "F:\\stereoCamera\\PCread\\picwithIMU\\databaseAll\\binaryPic_DSDR\\DSDR_20fea_train.model";
    public static svm_model DSDRSVM;
    public static List<Double> targetLabel = new ArrayList<>();
    public static List<Double> resultLabel = new ArrayList<>();



    public static void main(String[] args){
        //将训练数据导入进行验证
        FileReader trainDataFile = null;
        BufferedReader trainDataReader;
        try {
            triClassSVM = svm.svm_load_model(svmModelPath_6Class);
            LGOBSVM = svm.svm_load_model(svmModelPath_LGOB);
            USURSVM = svm.svm_load_model(svmModelPath_USUR);
            DSDRSVM = svm.svm_load_model(svmModelPath_DSDR);
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
                double[] feaList = new double[m];
                for (int j = 0; j < m; j++) {
                    int a = atoi(st.nextToken());
                    feaList[j] = atof(st.nextToken()) ;
                }
                //SVM1
//                double[] fea2 = { features[0], features[1], features[2], features[3],features[4],features[5],features[6],features[7],
//                        features[8],features[9], features[10], features[11], features[12], features[13], features[14],features[15],
//                        features[16], features[17],features[18], features[19]};
                double[] fea1 = { feaList[0], feaList[1], feaList[2], feaList[3],feaList[5],feaList[6],feaList[7],
                        feaList[8],feaList[9], feaList[10], feaList[11], feaList[12], feaList[13], feaList[14],feaList[15],
                        feaList[16], feaList[18], feaList[19]};
                svm_result result_6Class = svm_predict.predict(fea1, triClassSVM,1);
                int label = result_6Class.getPredict_label();
                double[] prob = result_6Class.getProb_estimates();
                switch (label) {
                    //SVM2
                    case 0:
                        double[] fea2 = { feaList[0], feaList[1], feaList[2], feaList[3],feaList[4],feaList[5],feaList[6],feaList[7],
                        feaList[8],feaList[9], feaList[10], feaList[11], feaList[12], feaList[13], feaList[14],feaList[15],
                        feaList[16], feaList[17],feaList[18], feaList[19]};
                        svm_result result_LGOB = svm_predict.predict(fea2, LGOBSVM, 1);
                        double[] prob_LGOB = result_LGOB.getProb_estimates();
                        label = prob_LGOB[0] >= prob_LGOB[1] ? 0 : 1;
                        break;
                    //SVM3
                    case 1:
                        double[] fea3 = { feaList[0], feaList[1], feaList[2], feaList[3],feaList[4],feaList[5],feaList[6],feaList[7],
                                feaList[8],feaList[9], feaList[10], feaList[11], feaList[12], feaList[13], feaList[14],feaList[15],
                                feaList[16], feaList[17],feaList[18], feaList[19]};
                        svm_result result_USUR = svm_predict.predict(fea3, USURSVM, 1);
                        double[] prob_USUR = result_USUR.getProb_estimates();
                        label = prob_USUR[0] >= prob_USUR[1] ? 2 : 4;
                        break;
                    //SVM4
                    case 2:
                        double[] fea4 = { feaList[0], feaList[1], feaList[2], feaList[3],feaList[4],feaList[5],feaList[6],feaList[7],
                                feaList[8],feaList[9], feaList[10], feaList[11], feaList[12], feaList[13], feaList[14],feaList[15],
                                feaList[16], feaList[17],feaList[18], feaList[19]};
                        svm_result result_DSDR = svm_predict.predict(fea4, DSDRSVM, 1);
                        double[] prob_DSDR = result_DSDR.getProb_estimates();
                        label = prob_DSDR[0] >= prob_DSDR[1] ? 3 : 5;
                        break;
                }

                resultLabel.add((double) label);
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
        return Double.parseDouble(s);
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
