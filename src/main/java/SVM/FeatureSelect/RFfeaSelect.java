package SVM.FeatureSelect;

import libsvm.svm_predict;
import libsvm.unit.svm;
import libsvm.unit.svm_model;
import org.opencv.core.Core;

import java.io.*;
import java.util.List;
import java.util.StringTokenizer;

import static SVM.modelTrain.libsvmValidation.*;

/**
 * @className: RFfeaSelect
 * @description: TODO
 * @author: Lin Guifeng
 * @date: 2022/06/28 16:32
 * @version: 1.0
 **/
public class RFfeaSelect {
    static { System.loadLibrary(Core.NATIVE_LIBRARY_NAME); }// 动态链接opencv
    //识别样本地址
    public static String testDataPath = "F:\\stereoCamera\\PCread\\picwithIMU\\databaseAll0710\\binaryPic\\20_test.txt";
    //模型文件地址
    public static String svmModelPath = "F:\\stereoCamera\\PCread\\picwithIMU\\databaseAll0710\\binaryPic\\20_train.model";
    //【测试样本数量】
    private static int numSamp = 4589;
    //【特征数量】
    private static int numFea = 20;
    public static svm_model svmModel;
    private static svm_predict svmPredict;

    public static void main(String[] args) throws IOException {
        double[] feaEvaluation = new double[numFea];
        for(int k=0;k<21;k++) {
            System.out.println("【round】:"+k+"----------------------------------------------------------------------");
            try {
                //模型导入
                svmModel = svm.svm_load_model(svmModelPath);
                //读取样本
                FileReader testDataFile = new FileReader(testDataPath);
                BufferedReader testDataReader = new BufferedReader(testDataFile);
                double[] targetLabel = new double[numSamp];
                double[][] feaAll = new double[numSamp][numFea];
                double[][] feaRange = new double[numFea][2];
                int indexSamp = 0;
                while (true) {
                    //读取一行样本数据
                    String line = null;
                    //读行
                    line = testDataReader.readLine();
                    if (line == null) break;
                    //通过分隔符取出每个词
                    StringTokenizer st = new StringTokenizer(line, " \t\n\r\f:");
                    //取出标记的类别标签
                    targetLabel[indexSamp] = atof(st.nextToken());
                    //将特征及其标号存入特征数组

                    for (int j = 0; j < numFea; j++) {
                        //将特征及其标号存入特征数组
                        int index = atoi(st.nextToken());
                        feaAll[indexSamp][j] = atof(st.nextToken());
                    }
                    indexSamp++;
                }

                //在对应特征的取值范围内随机取值并
                double[] feaEvaluationPer = new double[numFea];
                for (int j = 0; j < numFea; j++) {
                    feaRange[j] = findMaxMin(feaAll, j);//求范围
                    double[][] feaAll_change = changeFea(feaAll, j, feaRange[j]);//随机取值
                    String savePath = testDataPath.replace(".txt", "_" + j + ".txt");//保存路径
                    saveAsTxt4SVM(feaAll_change, targetLabel, savePath, false);

                    //将训练数据导入进行验证
                    FileReader DataFile = new FileReader(savePath);
                    BufferedReader trainDataReader = new BufferedReader(DataFile);
                    //训练结果输出流
                    String resultPath = savePath.replace(".txt", "_result.txt");
                    DataOutputStream resultOutput = new DataOutputStream(new FileOutputStream(resultPath));

                    //识别
                    List<Double> resultLabel = svmPredict.predict(trainDataReader, resultOutput, svmModel, 1);
                    List<Double> targetLabelList = svmPredict.getLabelTarget(); //获取样本数据标签
                    int[][] refuseM = getRefuseMatrix(resultLabel, targetLabelList, 6);
                    double[] rate = getRate(refuseM);
                    feaEvaluation[j] = feaEvaluation[j]+ rate[6];
                    feaEvaluationPer[j] = rate[6];
                }

                for (double a : feaEvaluationPer) System.out.println(a);

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        for (double a : feaEvaluation) System.out.println(a);
    }

    private static double[] findMaxMin( double[][] input, int index ){
        int length = input.length;
        double[] MaxMin = {0,0};
        for(int i=0;i<length;i++){
            if(input[i][index]>MaxMin[0]) MaxMin[0] = input[i][index];
            if(input[i][index]<MaxMin[1]) MaxMin[1] = input[i][index];

        }
        return MaxMin;
    }

    /**
     * 在指定特征列的范围内随机修改该列的值
     * @param input 输入数组
     * @param index 特征列索引
     * @param range 范围
     * @return 输出数组
     */
    private static double[][] changeFea( double[][] input, int index , double[] range ){
        double[][] output = new double[input.length][input[0].length];
        for(int i = 0;i < input.length;i++) {
            for (int j = 0; j < input[i].length; j++) {
                output[i][j] = input[i][j];
            }
        }
        for(int i=0;i< input.length;i++){
            output[i][index] = range[1] + Math.random()*(range[0]-range[1]);
        }
        return output;
    }

    private static double atof(String s) {
        return Double.valueOf(s).doubleValue();
    }

    private static int atoi(String s) {
        return Integer.parseInt(s);
    }

    /**
     * 将二维数组保存为libsvm能够识别的txt文件格式
     * [label] [index1]:[value1] [index2]:[value2] …
     * @param data  保存的特征值
     * @param label 特征值对应标签
     * @param savePath 文件名
     * @param append true从地址对应文件末尾写入文本
     */
    public static void saveAsTxt4SVM(double[][] data ,double[] label, String savePath ,boolean append){

        try {
            FileWriter output = new FileWriter(savePath , append );

            for (int i = 0 ; i < data.length ; i++ ){
                output.write((int)label[i]+"\t");
                for (int j = 0 ; j < data[0].length ; j++ ) {
                    int index = j+1;
                    if(Double.isNaN(data[i][j]) || Double.isInfinite(data[i][j])) {
                        output.write(index + ":" + 0 + "\t");
                        System.out.println("NaN or Infinity in features data. ");
                    }else{
                        output.write(index + ":" + data[i][j] + "\t");
                    }
                }
                output.write("\n");
            }
            output.close();

        } catch (IOException e) {
            e.printStackTrace();
        }

    }



}
