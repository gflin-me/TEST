package SVM.FeatureSelect;

import libsvm.unit.svm_model;
import org.opencv.core.Core;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

/**
 * @className: FScoreCal
 * @description: 读取单个特征值的文件（由featureSelect代码输出），计算各个特征的F-SCORE值
 * @author: Lin Guifeng
 * @date: 2022/06/27 22:49
 * @version: 1.0
 **/
public class FScoreCal {
    static { System.loadLibrary(Core.NATIVE_LIBRARY_NAME); }// 动态链接opencv
    public static svm_model svmModel;
    public static String dataFolder = "F:\\stereoCamera\\PCread\\picwithIMU\\database5class\\binaryPic\\0features\\";
    public static int numFea = 20;
    public static int numClass = 5;
    public static void main(String[] args){
        for(int i=0;i<numFea;i++) {
            //。。。。。。。。。。。。。。。非交叉验证模式，识别结果。。。。。。。。。。。。。。。
            String testDataPath = dataFolder+i+".txt";
            try {
                //将测试数据导入进行验证
                FileReader testDataFile = new FileReader(testDataPath);
                BufferedReader testDataReader = new BufferedReader(testDataFile);
                List<Double> feaList = new ArrayList<>();
                List<Double> targetLabel = new ArrayList<>();
                //特征数据读取
                while(true){
                    //读取一行样本数据
                    String line = null;
                    try {
                        //读行
                        line = testDataReader.readLine();
                        if(line == null) break;
                        //通过分隔符取出每个词
                        StringTokenizer st = new StringTokenizer(line," \t\n\r\f:");
                        //取出标记的类别标签
                        targetLabel.add(atof(st.nextToken()));
                        //将特征及其标号存入特征数组
                        int index = atoi(st.nextToken());
                        feaList.add(atof(st.nextToken()));

                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                //计算Fscore值
                double FScore = CalFScore(targetLabel,feaList,numClass);
                System.out.println(FScore);
            } catch (IOException e) {
                e.printStackTrace();
            }

        }
    }

    /**
     * 计算FS值
     * @param targetLabel 实际分类标签
     * @param feaList   特征向量
     * @param numClass 分类数
     * @return FScore值
     */
    public static double CalFScore( List<Double> targetLabel , List<Double> feaList , int numClass ){
        double FScore = 0;
        for(int trueLabel=0 ; trueLabel< numClass ; trueLabel++){
            int numSamp = targetLabel.size();
            int numTrue = 0,numFalse = 0;
            double feaAll=0,feaTrue=0,feaFalse=0;
            List<Double> feaTrueList = new ArrayList<>();
            List<Double> feaFalseList = new ArrayList<>();
            for(int j=0;j<numSamp;j++){
                feaAll = feaAll+feaList.get(j);
                if(targetLabel.get(j)==trueLabel){
                    feaTrue = feaAll+feaList.get(j);
                    feaTrueList.add(feaList.get(j));
                    numTrue++;
                }else {
                    feaFalse = feaAll+feaList.get(j);
                    feaFalseList.add(feaList.get(j));
                    numFalse++;
                }
            }

            double feaAvgAll = feaAll/numSamp;
            double feaAvgTrue = feaTrue/numTrue;
            double feaAvgFalse = feaFalse/numFalse;
            double varTrue = calVar(feaTrueList,feaAvgTrue);
            double varFalse = calVar(feaFalseList,feaAvgFalse);
            double a = Math.pow((feaAvgTrue-feaAvgAll),2) + Math.pow((feaAvgFalse-feaAvgAll),2) ;
            double b = varTrue+varFalse;
            FScore = FScore + a/(b*numClass);
        }
        return FScore;
    }

    /**
     * 方差计算
     * @param input 输入数据
     * @param avg 输入数据的平均值
     * @return 方差
     */
    public static double calVar(List<Double> input,double avg){
        int length = input.size();
        double out =0;
        for(int i =0;i<length;i++){
            out = out+Math.pow((input.get(i)-avg),2);
        }
        return out/(length-1);
    }



    private static double atof(String s) {
        return Double.valueOf(s).doubleValue();
    }

    private static int atoi(String s) {
        return Integer.parseInt(s);
    }
}
