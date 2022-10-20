package SVM.modelTrain;

import java.io.*;
import java.util.List;

import libsvm.*;
import libsvm.unit.svm;
import libsvm.unit.svm_model;
import org.opencv.core.Core;

import static org.opencv.imgcodecs.Imgcodecs.imread;


/**
 * @className: SVM.libsvmTest
 * @description: libsvm库的训练、识别代码
 * @author: Lin Guifeng
 * @date: 2022/05/27 23:56
 * @version: 2.0
 **/
public class libsvmTrain {
    static {
        // 动态链接opencv
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
    }

    //训练样本地址
//    public static String trainDataPath = "F:\\stereoCamera\\PCread\\picwithIMU\\camera2_70det_60fps_1214_02\\202202141506_allData.txt";
    public static String trainDataPath = "F:\\stereoCamera\\PCread\\picwithIMU\\database5class\\binaryPic\\16_train.txt";
    //模型存放地址
    public static String modelPath = trainDataPath.replace(".txt",".model");
    //结果输出地址
    public static String resultPath = trainDataPath.replace(".txt","_result.txt");
    //参数设置
    private static int cost = (int) Math.pow( 2 , 5 );
    private static double gamma = Math.pow( 2 , 0 );

    /*
    svm模型训练参数
    -s 0 : C-SVC模式
    -t 2 : 核函数为RBF核函数
    -c 14.42 : 惩罚系数
    -g 0.732 ：核函数的γ值
    -m 500 : 设置cache内存大小
    -h 0 : 是否使用启发式
    -v n :n折的交叉验证
    -b 1 ：设置输出后验概率
    trainDataPath : 训练数据txt地址
    modelPath ：训练后模型存放地址
     */
//    public static String[] paramTrain = {"-s","0","-t","2","-c","102.4","-g","0.50625","-m","500.0","-b","1", trainDataPath, modelPath};

    //svm模型
    public static svm_model svmModel;

    public static void main(String[] args){
        System.out.println("【Parameters】 C="+ cost +" , gamma="+gamma);
        //------------------------------------样本数据【训练】&【识别】------------------------------------
        try {
//            //参数寻优，模型训练
//            //设置寻优参数寻优起始值，最大值，步长
//            double gammaMin = 3;  double costMin = 6;
//            double gammaMax = 10;    double costMax = 15;
//            double stepGamma = 1; double stepCost = 1;
//            List<List<Double>> accAll = new ArrayList<>();//识别率记录
//            //循环使用交叉验证的方法训练和输出识别率
//            for(int i=0 ; i<9999;i++){
//                double gamma = gammaMin+i*stepGamma;
//                if(gamma>gammaMax) break;
//                List<Double> accList = new ArrayList<>();
//                outer1:for(int j=0 ; j<9999;j++){
//                    double cost = costMin+j*stepCost;
//                    if(cost>costMax) break;
//                    System.out.println("gamma:"+gamma+" , cost: "+cost);
//                    String[] paramTrain = {"-s","0","-t","2","-c", String.valueOf(cost),"-g", String.valueOf(gamma),"-m","500.0","-b","1","-v","5","-h","0", trainDataPath, modelPath};
//                    svm_train.main(paramTrain);
//                    double acc = svm_train.getAcc_CV();
//                    accList.add(acc);
//                }
//                accAll.add(accList);
//            }
//            //识别率结果保存
//            String savePath = trainDataPath.replace(".txt","_acc.txt");
//            saveList2TXT(accAll , savePath);
//
//
//            String[] paramTrain = {"-s","0","-t","2","-c","5","-g","3","-m","500.0","-b","1","-v","5", trainDataPath, modelPath};
//            svm_train.main(paramTrain);
//            double acc = svm_train.getAcc_CV();

            //。。。。。。。。。。。。。。。非交叉验证模式，识别结果。。。。。。。。。。。。。。。
            String[] paramTrain1 = {"-s","0","-t","2","-c", String.valueOf(cost),"-g", String.valueOf(gamma),"-m","500.0","-b","1", trainDataPath, modelPath};//c-9.g-2.9
            svm_train.main(paramTrain1);
            //将训练数据导入进行验证
            FileReader trainDataFile = new FileReader(trainDataPath);
            BufferedReader trainDataReader = new BufferedReader(trainDataFile);
            //训练结果输出流
            DataOutputStream resultOutput = new DataOutputStream(new  FileOutputStream(resultPath));
            svmModel = svm.svm_load_model(modelPath);
            //识别
            svm_predict.predict(trainDataReader,resultOutput,svmModel,1);

        } catch (IOException e) {
            e.printStackTrace();
        }

        //------------------------------------单样本识别测试---------------------------------

//        String picPath = "F:\\stereoCamera\\PCread\\picwithIMU\\camera2_70det_60fps_1214_02\\LG\\" +
//                "1214_10.48.23.200_-44.159546_1.774292_-151.29822.png";
//        Mat bianryPic = imread(picPath, IMREAD_GRAYSCALE);
//        double[] features = featureExtractionFunction.featuresExtraction(bianryPic);
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
     * 保存二维List数据为txt文件
     * @param data 数据
     * @param savePath 保存地址
     */
    public static void saveList2TXT(List<List<Double>> data , String savePath){

        try{
            File outFile1 = new File(savePath);
            Writer out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(outFile1,true), "utf-8"), 10240);
            for(List<Double> list:data) {
                for (int i = 0; i < list.size(); i++) {
                    out.write(list.get(i) + "\t");
                }
                out.write("\n");
            }
            out.flush();
            out.close();
        }catch (Exception e){
            e.printStackTrace();
        }

    }




}
