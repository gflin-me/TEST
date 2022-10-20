import be.ac.ulg.montefiore.run.jahmm.*;
import be.ac.ulg.montefiore.run.jahmm.test.SimpleExample;
import be.ac.ulg.montefiore.run.jahmm.toolbox.MarkovGenerator;
import libsvm.svm_predict;
import libsvm.unit.svm;
import libsvm.unit.svm_model;
import libsvm.unit.svm_result;
import unit.ViterbiCalculatorV2;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * @className: hmmTest
 * @description: TODO
 * @author: Lin Guifeng
 * @date: 2022/01/14 16:30
 * @version: 1.0
 **/
public class getConfidence {

    public static void main(String[] args) throws IOException {
        String modelPath = "F:\\stereoCamera\\实验数据\\20220713在线测试实验\\14_train.model";
        String dataPath = "F:\\stereoCamera\\实验数据\\20220809在线识别数据\\08091602_features.txt";
        String outPath = dataPath.replace("features","prob");
        //SVM模型载入
        svm_model svmModel = svm.svm_load_model(modelPath);
        //导入序列的特征值数据文件
        ArrayList<double[]> data = readTxtFile( dataPath );
        List<String> out = new ArrayList<>();
        for(int i=0 ; i< data.size() ; i++ ){//帧序列循环
            //获取单帧的特征向量
            double[] dataPer = data.get(i);
            //SVM分类结果
            svm_result result = svm_predict.predict(dataPer, svmModel,1);
            double[] prob = result.getProb_estimates();
            String str = prob[0]+"\t"+prob[1]+"\t"+prob[2]+"\t"+prob[3]+"\t"+prob[4]+"\t"+prob[5];
            out.add(str);
        }
        saveLog(out,outPath);


    }

    public static ArrayList<double[]> readTxtFile(String inputPath ){
        FileReader inputFile = null;
        ArrayList<double[]> data = new ArrayList<>();
        try {
            inputFile = new FileReader(inputPath);
            BufferedReader inputReader = new BufferedReader(inputFile);
            while(true) {
                //读取一行样本数据
                String line = inputReader.readLine();
                if (line == null) break;
                //通过分隔符取出每个词
                StringTokenizer st = new StringTokenizer(line," \t\n\r\f:");
                //计算单行数据数量
                int m = st.countTokens();
                //将特征及其标号存入特征数组
                double[] fea = new double[m];
                for(int j=0;j<m;j++) {
                    fea[j] = atof(st.nextToken());
                }
                data.add(fea);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return data;
    }
    private static double atof(String s)
    {
        return Double.valueOf(s).doubleValue();
    }
    /**
     * 保存日志文件
     * @param blurDetectLog 日志文件
     */
    public static void saveLog(List<String> blurDetectLog ,String fileName){
        BufferedWriter bw;
        try {
            bw = new BufferedWriter(new FileWriter(fileName));
            for (int j = 0; j < blurDetectLog.size(); j++) {
                bw.write(blurDetectLog.get(j));
                bw.newLine();
                bw.flush();

            }
            bw.close();
        }catch (IOException E ){
            E.printStackTrace();
        }

    }

}
