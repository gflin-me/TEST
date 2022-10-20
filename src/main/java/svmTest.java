import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.Scalar;
import org.opencv.ml.Ml;
import org.opencv.ml.ParamGrid;
import org.opencv.ml.SVM;
import org.opencv.ml.TrainData;
import unit.featureExtractionFunction;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static org.opencv.core.CvType.CV_32FC1;
import static org.opencv.core.CvType.CV_32SC1;
import static org.opencv.imgcodecs.Imgcodecs.IMREAD_GRAYSCALE;
import static org.opencv.imgcodecs.Imgcodecs.imread;

/**
 * @className: svmTest
 * @description: svm训练识别相关，基于openCV.ml.svm模块进行
 * @author: Guifeng lin
 * @date: 2021/12/28 17:39
 * @version: 1.0
 **/
public class svmTest {

//    public static SVM svm = SVM.create();
    static {
    // 动态链接opencv
    System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
    }

    public static void main(String[] args) throws IOException {

        //------------------------------构建数据集和标签集-----------------------------------
        String[] terrainName = { "LG" , "OB" , "US" , "DS" , "UR" , "DR" };
        ArrayList<Mat> dataList = new ArrayList<Mat>();
        ArrayList<Mat> labelList = new ArrayList<Mat>();
        for (int indexTerrain = 0 ; indexTerrain<terrainName.length;indexTerrain++) {
            //特征数组txt读取
            String defaultPath = "F:\\stereoCamera\\PCread\\SVM\\1220_16\\default.txt" ;
            String terrain = terrainName[indexTerrain];
            String filePath = defaultPath.replace("default", terrain);
            float[][] data1 = readArray(filePath);
            //特征数组转为mat格式
            Mat dataMat = new Mat( data1.length , data1[0].length , CV_32FC1 );
            for (int i = 0; i < data1.length; i ++){
                for (int j = 0; j < data1[0].length; j ++){
                    dataMat.put( i , j , data1[i][j]);
                }
            }
            //标签点向量Mat建立
            Mat label_per = new Mat(data1.length,1,CV_32SC1,new Scalar(indexTerrain+1));
            //存入总集合
            dataList.add(dataMat);
            labelList.add(label_per);
        }
        //将集合中的数组垂直拼接为一个Mat
        Mat trainFea = new Mat();
        Mat trainLabel = new Mat();
        Core.vconcat(dataList,trainFea);
        Core.vconcat(labelList,trainLabel);
        //建立训练数据TrainData
        TrainData trainData = TrainData.create(trainFea, Ml.ROW_SAMPLE,trainLabel);

        //---------------------------------------------SVM训练与测试---------------------------------
//        //创建svm
//        SVM svm = SVM.create();
//        svm.setType(SVM.C_SVC);
//        svm.setKernel(SVM.RBF);
//        svm.setC(86.87216524793878);
//        svm.setGamma( 0.37974983358324127 );
//        //使用训练数据训练svm
//        boolean success = svm.train(trainData);
//        System.out.println(success);
//        //测试训练集
//        Mat responseMat = new Mat();
//        Mat resultMat = new Mat();
//        svm.predict(trainFea, responseMat, 0);
//        //统计正确率
//        responseMat.convertTo(responseMat,CV_32SC1);//格式转换
//        Core.subtract(trainLabel,responseMat,resultMat);
//        int right = resultMat.rows()-Core.countNonZero(resultMat);
//        double accurancy = (double) right/resultMat.rows();
//        System.out.println(accurancy*100+"%,("+right+"/"+resultMat.rows()+")");
//        svm.save("F:\\stereoCamera\\PCread\\SVM\\1220_16\\SVM_Model_220104.xml");

        //------------------------------------SVM进行交叉验证，参数寻优---------------------------------
        //使用交叉验证寻优参数
//        SVM svm = SVM.create();
//        ParamGrid CParamGrid = ParamGrid.create(0.1,500,3);
//        ParamGrid gammaParamGrid = ParamGrid.create(1e-5,20,2);
//        boolean success = svm.trainAuto(trainFea, Ml.ROW_SAMPLE,trainLabel,5,CParamGrid,gammaParamGrid);
//        System.out.println(success);
//        System.out.println("C："+svm.getC());
//        System.out.println("gamma："+svm.getGamma());
//        svm.save("F:\\stereoCamera\\PCread\\SVM\\1220_16\\SVM_Model_1229.xml");

        //------------------------------------SVM单样本识别---------------------------------
        SVM svm = SVM.load("F:\\stereoCamera\\PCread\\SVM\\1220_16\\svm.xml");
        String picPath = "F:\\stereoCamera\\PCread\\picwithIMU\\camera2_70det_60fps_1214_02\\LG\\" +
                                "1214_10.48.23.200_-44.159546_1.774292_-151.29822.png";
        Mat bianryPic = imread(picPath, IMREAD_GRAYSCALE);
        double[] features = featureExtractionFunction.featuresExtraction(bianryPic);
        Mat featureMat = new Mat(1 , features.length , CV_32FC1 );
        Mat predictResult = new Mat();
        for (int i = 0; i < features.length; i ++){
            featureMat.put( 0 , i , features[i]);
        }
        svm.predict(featureMat,predictResult,0);
        int result = (int)predictResult.get(0,0)[0];
        System.out.println("svm recognition result："+result);

    }

    /**
     * 读取txt文件并保存为float数组
     * @param filePath 文件路径
     * @return float二维数组
     */
    public static float[][] readArray(String filePath) {
        //1.声明一个字符输入流
        FileReader reader = null;
        //2.声明一个字符输入缓冲流
        BufferedReader readerBuf = null;
        //3.声明一个二维数组
        float[][] array = null;
        try {
            //4.指定reader的读取路径
            reader = new FileReader(filePath);
            //5.通过BufferedReader包装字符输入流
            readerBuf = new BufferedReader(reader);
            //6.创建一个集合，用来存放读取的文件的数据
            List<String> strList = new ArrayList<>();
            //7.用来存放一行的数据
            String lineStr;
            //8.逐行读取txt文件中的内容
            while((lineStr = readerBuf.readLine()) != null) {
                //9.把读取的行添加到list中
                strList.add(lineStr);
            }
            //10.获取文件有多少行
            int lineNum = strList.size();
            //11.获取数组有多少列
            String s =  strList.get(0);
            int columnNum = s.split("\\t").length;
            //12.根据文件行数创建对应的数组
            array = new float[strList.size()][columnNum];
            //13.记录输出当前行
            int count = 0;
            //14.循环遍历集合，将集合中的数据放入数组中
            for(String str : strList) {
                //15.将读取的str按照","分割，用字符串数组来接收
                String[] strs = str.split("\\t");
                for(int i = 0; i < columnNum; i++) {
                    array[count][i] = Float.parseFloat(strs[i]);
                }
                //16.将行数 + 1
                count++;
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            //17.关闭字符输入流
            try {
                if(reader != null)
                    reader.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            //18.关闭字符输入缓冲流
            try {
                if(readerBuf != null)
                    readerBuf.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        //19.返回稀疏数组
        return array;
    }


}
