import be.ac.ulg.montefiore.run.jahmm.*;
import be.ac.ulg.montefiore.run.jahmm.test.SimpleExample;
import be.ac.ulg.montefiore.run.jahmm.toolbox.MarkovGenerator;
import libsvm.svm_predict;
import libsvm.unit.svm;
import libsvm.unit.svm_model;
import libsvm.unit.svm_node;
import libsvm.unit.svm_result;
import unit.ViterbiCalculatorV2;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
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
public class hmmTest {

//    private static List<ObservationInteger> sequence;
    public enum Packet {
        LG, OB,US,DS,UR,DR;

        public ObservationDiscrete<Packet> observation() { return new ObservationDiscrete<hmmTest.Packet>(this);
        }
    };

    public static void main(String[] args) throws IOException {

        int numPatten = 6;//模式数量
        int windowLength = 9;//hmm窗口大小/状态链长度
        String modelPath = "F:\\stereoCamera\\实验数据\\20220713在线测试实验\\14_train.model";
        String dataPath = "F:\\stereoCamera\\实验数据\\20220713在线测试实验\\07141030_features.txt";
//        int[] trueOb = { 0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,0,0,0,0,0,0,0,0,0,0,0,1,1,1,1,1,1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,1,1,1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,1,1,1,1,1,1,1,1,1,1,1,1,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0};
//        int[] aa = {0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,0,0,0,0,0,0,0,1,1,1,1,1,1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,1,1,3,0,0,0,0,0,0,0,0,0,0,1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,1,1,1,1,1,1,1,1,1,1,1,1,1,4,4,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,4,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,5,5,5,5,0,0,0,0,0,0,0,0,0,0,0,0};
        int[] trueOb = {0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,1,1,1,1,1,1,1,1,1,1,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,0,0,0,0,0,0,0,0};
        int[] aa = {0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,4,0,0,0,0,0,2,2,2,2,2,1,1,1,4,4,4,4,4,4,4,4,2,2,1,2,2,2,2,2,2,2,2,2,2,2,2,2,0,0,4,0,0,0,0,0,0,0,0,0,0,0,0,0,0,1,1,1,4,4,0,0,0,0,0,4,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,3,3,5,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,3,3,3,0,0,0,0,0,4,1,1,1,1,1,1,1,1,1,1,1,1,1,1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,4,4,4,0,0,0,0,0,0,0,0,0,0,0,0,0,3,3,0,3,5,3,3,0,0,0,0,0,4,4,0,4,5,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,0,0,0,0,0,0,0,0};
        //初始状态概率向量π
        double[] pi = new double[]{0.9, 0.02, 0.02, 0.02, 0.02, 0.02};
        //状态转移概率矩阵A
        double[][] A = new double[][]{  { 0.7  , 0.06, 0.08, 0.08, 0.04, 0.04},
                                        { 0.06 , 0.7 , 0.08, 0.08, 0.04, 0.04},
                                        { 0.1  , 0.1 , 0.8 , 0   , 0   , 0   },
                                        { 0.1  , 0.1 , 0   , 0.8 , 0   , 0   },
                                        { 0.1  , 0.1 , 0   , 0   , 0.8 , 0   },
                                        { 0.1  , 0.1 , 0   , 0   , 0   , 0.8 }  };
//        double[][] A = new double[][]{  { 0.7  , 0.06, 0.08, 0.08, 0.04, 0.04},
//                { 0.06 , 0.7 , 0.08, 0.08, 0.04, 0.04},
//                { 0.1  , 0.1 , 0.8 , 1e-22   , 1e-99   , 1e-99   },
//                { 0.1  , 0.1 , 1e-99   , 0.8 , 1e-22   , 1e-99   },
//                { 0.1  , 0.1 , 1e-99   , 1e-99   , 0.8 , 1e-99   },
//                { 0.1  , 0.1 , 1e-99   , 1e-99   , 1e-99   , 0.8 }  };
        //观测概率矩阵b
        double[][] b = new double[][]{{1,0,0,0,0,0},{0,1,0,0,0,0},{0,0,1,0,0,0},{0,0,0,1,0,0},{0,0,0,0,1,0},{0,0,0,0,0,1}};
        int[] svmOB = new int[trueOb.length];
        //导入序列的特征值数据文件
        ArrayList<double[]> data = readTxtFile( dataPath );
        if(data.size()!=trueOb.length) System.out.println("输入特征数据帧数与实际结果序列长度不一致");
        //SVM模型载入
        svm_model svmModel = svm.svm_load_model(modelPath);

        //观测概率分布矩阵序列生成
        List<List< OpdfDiscrete >> opdfs = new ArrayList<>();
        for(int i=0 ; i< data.size() ; i++ ){//帧序列循环
            //获取单帧的特征向量
            double[] dataPer = data.get(i);
            //SVM分类结果
            svm_result result = svm_predict.predict(dataPer, svmModel,1);
            svmOB[i] = result.getPredict_label();
            //生成单帧的观测概率分布矩阵（数组形式）
            double[][] b_frame = new double[][]{{1,0,0,0,0,0},{0,1,0,0,0,0},{0,0,1,0,0,0},{0,0,0,1,0,0},{0,0,0,0,1,0},{0,0,0,0,0,1}};
//            double[][] b_frame = b.clone();
            for( int j = 0; j<numPatten ; j++) {//模式序列循环
                b_frame[result.getPredict_label()][j] = result.getProb_estimates()[j];
            }
            // 写入【观测概率分布矩阵】
            List< OpdfDiscrete > opdf = new ArrayList<>(numPatten);
            for (int K = 0; K < numPatten; K++) {//模式序列循环
                opdf.add( K , new OpdfDiscrete<>(Packet.class, b_frame[K] ) ) ;
            }
            // 写入【观测概率分布矩阵序列】
            opdfs.add(i,opdf);

        }

        // 观测概率矩阵
        List< OpdfDiscrete > opdf_hmm = new ArrayList<>(numPatten);
        for (int K = 0; K < numPatten; K++) {//模式序列循环
            opdf_hmm.add( K , new OpdfDiscrete<>(Packet.class, b[K] )) ;
        }
        // HMM模型
        Hmm<ObservationDiscrete> hmm = new Hmm ( pi , A , opdf_hmm );

        //离散观测值枚举
        ObservationDiscrete<hmmTest.Packet> packetLG = hmmTest.Packet.LG.observation();
        ObservationDiscrete<hmmTest.Packet> packetOB = hmmTest.Packet.OB.observation();
        ObservationDiscrete<hmmTest.Packet> packetUS = hmmTest.Packet.US.observation();
        ObservationDiscrete<hmmTest.Packet> packetDS = hmmTest.Packet.DS.observation();
        ObservationDiscrete<hmmTest.Packet> packetUR = hmmTest.Packet.UR.observation();
        ObservationDiscrete<hmmTest.Packet> packetDR = hmmTest.Packet.DR.observation();
        List<ObservationDiscrete<hmmTest.Packet>> terrainList = new ArrayList<>();
        terrainList.add(packetLG);terrainList.add(packetOB);terrainList.add(packetUS);
        terrainList.add(packetDS);terrainList.add(packetUR);terrainList.add(packetDR);

        int[] SQ_all = svmOB.clone();
        int indexOfFrame = windowLength ;//所预测的帧在窗口的位置
        for(int i = indexOfFrame-1 ; i < svmOB.length-i+indexOfFrame ; i++ ) {
            //根据窗口大小获取观测序列窗口
            List<ObservationDiscrete<hmmTest.Packet>> testSequence = new ArrayList<>();
            for (int j = i-indexOfFrame+1 ; j < i-indexOfFrame+1+windowLength; j++) {
                testSequence.add( terrainList.get( SQ_all[j] ) ) ;
            }
            List<List< OpdfDiscrete >> opdfsWindow = opdfs.subList( i-indexOfFrame+1 , i-indexOfFrame+1+windowLength );
            //构建viterbi计算器
            ViterbiCalculatorV2 viterbiCalculator = new ViterbiCalculatorV2( testSequence ,  hmm , opdfsWindow );
            //计算最大概率状态序列
            int[] stateSequence = viterbiCalculator.stateSequence( );
            SQ_all[i]=stateSequence[ indexOfFrame-1 ];

        }

        //投票策略
        int[] votingSeq = aa.clone();
        int votLength = 3;
        int end = votingSeq.length-votLength/2;
        for(int k = votLength/2; k<end;k++){
            int[] times = new int[6];
            for(int j= k-votLength/2;j<k+votLength/2+1;j++){
                times[votingSeq[j]]++;
            }
            int more = 0,max=-1;
            for(int m=0;m<times.length;m++){
                if(times[m]>max){
                    more = m;
                    max=times[m];
                }
            }
            votingSeq[k] = more;


//            if(votingSeq[k-1]==votingSeq[k+1]){
//                votingSeq[k]=votingSeq[k+1];
//            }
//            if(votingSeq[k-1]!=votingSeq[k]&&votingSeq[k]!=votingSeq[k+1]){
//                votingSeq[k]=votingSeq[k-1];
//            }
        }
//        votingSeq = voting(votingSeq,1);
        double accuracySVM = checkResult(svmOB,trueOb);
        double accuracyHMM = checkResult(aa,trueOb);
        double accuracyVoting = checkResult(votingSeq,trueOb);

        System.out.println("原始序列："+Arrays.toString(svmOB));
        System.out.println("H M M后："+Arrays.toString(SQ_all));
        System.out.println("实际序列："+Arrays.toString(trueOb));
        System.out.println("投票序列："+Arrays.toString(votingSeq));
        System.out.println("原始正确率："+accuracySVM);
        System.out.println("HMM正确率："+accuracyHMM);
        System.out.println("HMM+投票正确率："+accuracyVoting);
    }

      static Hmm<ObservationInteger> buildHmm()
    {
//        Hmm<ObservationDiscrete<SimpleExample.Packet>> hmm = new Hmm<ObservationDiscrete<SimpleExample.Packet>>(2, new OpdfDiscreteFactory<SimpleExample.Packet>(SimpleExample.Packet.class));
        Hmm<ObservationInteger> hmm = new Hmm (6, new OpdfDiscreteFactory (SimpleExample.Packet.class) );
        //设置初始概率向量π
        hmm.setPi(0, 0.95);
        hmm.setPi(1, 0.05);
//        //设置观测概率矩阵B
//        hmm.setOpdf(0, new OpdfDiscrete<SimpleExample.Packet>(SimpleExample.Packet.class,
//                new double[] { 0.95, 0.05 }));
//        hmm.setOpdf(1, new OpdfDiscrete<SimpleExample.Packet>(SimpleExample.Packet.class,
//                new double[] { 0.20, 0.80 }));
        //设置状态转移概率矩阵A
        hmm.setAij(0, 1, 0.05);
        hmm.setAij(0, 0, 0.95);
        hmm.setAij(1, 0, 0.10);
        hmm.setAij(1, 1, 0.90);
        //返回HMM模型
        return hmm;
    }

    /* Generate several observation sequences using a HMM 使用 HMM 生成多个观测序列 */
    static < O extends Observation> List<List<O>>  generateSequences(Hmm<O> hmm)
    {
        MarkovGenerator<O> mg = new MarkovGenerator<O>(hmm);

        List<List<O>> sequences = new ArrayList<List<O>>();
        for (int i = 0; i < 200; i++)
            sequences.add(mg.observationSequence(100));

        return sequences;
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
     * 对比两个序列数组相符程度
     * @param seq1
     * @param seq2
     * @return
     */
    public static double checkResult(int[] seq1 , int[] seq2){
        double right = 0;

        for(int i=0;i<seq1.length;i++){
            if(seq1[i] == seq2[i]){
                right=right+1;
            }
        }
        return right/seq1.length;
    }

    /**
     * 投票函数
     * @param seq
     * @param delay
     * @return
     */
    private static int[] voting(int[] seq , int delay){
        for(int k = delay; k<seq.length-1;k++){
            int[] sub = Arrays.copyOfRange(seq,k-delay,k+delay);
            seq[k]=getMainE(sub);
        }
        return seq;
    }

    public static int getMainE(int[] arr){
        // 过程1 Collectors.groupingBy代表是分类，按照本身Function.identity()进行分类，那相同数字就会放在一起，Collectors.counting是统计相同数字的个数
        Map<Integer, Long> map = IntStream.of(arr).boxed().collect(Collectors.groupingBy(Function.identity(), Collectors.counting()));

        // 过程2 max方法是根据比较器（按照map的value进行排序）找出最大值
        Optional<Integer> maxOptional = map.entrySet().stream().max(Comparator.comparing(Map.Entry::getValue)).map(Map.Entry::getKey);
        return maxOptional.get();
    }

    /**
     * 使用HashMap提高性能。如果是自己定义的类，则要合理重写hashCode和equals方法
     * @param a
     * @return
     */
    public static <T> int getMostFrequentByMap(T[] a){
        if(a == null||a.length == 0){
            return 0;
        }
        int result = 0;
        int size = a.length;

        HashMap<T, LinkedList<T>> severalMap = new HashMap<>();

        for (int i = 0; i < size; i++) {
            T t = a[i];
            //以元素本身为键，元素分配到的LinkedList为值
            if(severalMap.get(t) != null){
                severalMap.get(t).add(t);
            }else{
                LinkedList<T> temp = new LinkedList<T>();
                temp.add(t);
                severalMap.put(t, temp);
            }
        }

        //指向长度最大的集合
        LinkedList<T> largestList = null;
        //找到元素最多的集合
        for (LinkedList<T> tempList : severalMap.values()) {
            if(largestList == null){
                largestList = tempList;
                continue;
            }

            if(tempList.size() > largestList.size()){
                largestList = tempList;
            }
            result = largestList.size();
        }

        return result;

    }

}
