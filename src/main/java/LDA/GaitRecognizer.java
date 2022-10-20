package LDA;



import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;


public class GaitRecognizer {

    //中值滤波
    private int medDelay ;
    private List<Float> accY_filter;
    private List<Float> gyrY_filter;
    private List<Float> eulerY_filter;


    private static int rmsWinLength;//用于识别站立状态的计算均方根的窗口长度
    private static int feaWinLength;//计算特征的窗口长度
    private static int standThreshold;//识别为站立状态的阈值
    private static int signDelay;//标记点延迟
    private static int signMinLag;//标记点间最小延迟
    //降维矩阵
    public static EbmMatrix dimReMatrix;
    // 标签点
    public static EbmVector labDot_Walk ;
    public static EbmVector labDot_UpStair ;
    public static EbmVector labDot_DownStair ;


    /**
     * 初始化函数
     */
    public GaitRecognizer(){
        accY_filter = new ArrayList<>();
        gyrY_filter = new ArrayList<>();
        eulerY_filter = new ArrayList<>();
        medDelay = 15;//中值滤波延迟（窗口长度=延迟*2+1）
        rmsWinLength=100;//计算均方根的窗口长度
        feaWinLength=100;//计算特征的窗口长度
        standThreshold = 4;//识别为站立状态的阈值
        signDelay = 50;//标记点查找延迟
        signMinLag = 70;//标记点间最小间隔
        lastSign=-999;//记录上一标记点索引
        //LDA相关
        dimReMatrix = new EbmMatrix(new double[][]{
            {-0.00514166341586638,-0.149228032700997,-0.476876372856100,0.866109542016315,1.04413782504730e-05,0.000193613388564202,-0.000867238927578682,-0.0119927635261554,0.00176853552096421},
            {-0.00158370638102929,-0.804413797567576,0.334766934534700,0.490727454686333,8.60299973579913e-06,-0.000329077240235641,0.00224545466730481,-0.00530741235793384,0.000529015955412995}});
        labDot_Walk = EbmVector.mv(0.243040907793991	,-0.0608223167867531);
        labDot_UpStair = EbmVector.mv(0.230647171270803	,-0.0239789440960612);
        labDot_DownStair = EbmVector.mv(0.223921666649787	,-0.0689027514857912);

    }


    private String currentSate = "站立";//当前状态值（使用全局变量以在标记点帧之间能够持续输出连续的步态状态值）
    private String[] gait = new String[]{"步行","上楼梯","下楼梯"};
    /**
     * 步态模式识别器
     * @param gyrX x轴角速度
     * @param accY y轴加速度
     * @param gyrY y轴角速度
     * @param eulerY y轴角度
     * @return 步态模式识别结果：0为站立静止，1为步行、2为上楼梯、3为下楼梯
     */
    public String gaitRecog( List<Float> gyrX , List<Float> accY , List<Float> gyrY , List<Float> eulerY ){
        //当数据序列长度足够时，开始进行识别
        if( gyrX.size() > rmsWinLength+10 && gyrX.size() >  feaWinLength+10 ){
            List<Float> winGyrX = gyrX.subList(gyrX.size()-rmsWinLength,gyrX.size());//截取窗口数据
            double rms = 1;//计算均方根
            if ( rms < standThreshold ){
                currentSate= "站立";//当x轴角速度的窗口数据均方根值小于4时，识别为【站立】
            }else {
                //进行动态动作识别
                //中值滤波并存入新序列
                accY_filter.add(medianFilter(accY,medDelay,accY.size()-medDelay-1));
                gyrY_filter.add(medianFilter(gyrY,medDelay,accY.size()-medDelay-1));
                eulerY_filter.add(medianFilter(eulerY,medDelay,accY.size()-medDelay-1));
                //寻找标记点
                int sign = signFind(eulerY_filter, signDelay, signMinLag);
                //当发现标记点，计算特征向量&识别
                if (sign > 0 && sign>feaWinLength) {
                    //根据窗口数据计算特征
                    double[] fea_eulerY= feaExtract( eulerY_filter , sign , feaWinLength );
                    double[] fea_gyrY  = feaExtract( gyrY_filter , sign , feaWinLength  );
                    double[] fea_accY  = feaExtract( accY_filter , sign , feaWinLength  );
                    //所选特征向量[12,11,10,9,6,5,4,3,2]
                    double[] feaChosen = new double[]{fea_accY[3],fea_accY[2],fea_accY[1],fea_accY[0], fea_gyrY[1],fea_gyrY[0], fea_eulerY[3],fea_eulerY[2],fea_eulerY[1]};
                    //步态识别结果
                    currentSate = gait[ LDARecognition( feaChosen ) ];//注意分类结果序号要加一
                }
            }
        }
        return currentSate;
    }


    /**
     * 中值滤波
     * @param data 数据流
     * @param delay 延迟，即中值滤波窗口为[outputIndex-delay,outputIndex+delay]=[end-2*delay,end],中间值为
     * @param outputIndex  输出值序号
     * @return 中值滤波输出
     */
    private static float medianFilter(List<Float> data, int delay , int outputIndex){
        //tips:List对象的subList方法是对原List元素的引用，对子list元素修改会直接影响原list
        //这里因为需要进行排序处理，所以必须新建变量的形式使用sublist
        List<Float> sortList = new ArrayList<>(data.subList(outputIndex - delay, outputIndex + delay));
        //排序
        Collections.sort(sortList);
        //输出中值
        return sortList.get(sortList.size()/2);
    }

    private static int lastSign=0;//上一标记点
    /**
     * 逐帧判定是否存在标记点
     * @param data 数据
     * @param delay 延迟 即标记点在【当前数据末尾】-【delay】处进行判定
     * @param minLag 与上一标记点的最小间隔
     * @return 标记点索引值，当判定为非标记点时此项返回负值
     */
    public int signFind( double[] data , int delay , int minLag ){
        int sign=-99;
        //当【数据长度】大于【上一标记点+最小间隔+延迟】时
        if(data.length>lastSign+minLag+delay) {
            int i = data.length - delay - 1;
            double frame = data[i];
            //判断当前数据是否处于下降阶段
            if (data[i-1]> frame && data[i-2] > frame ) {
                for (int j = 1; j < delay + 1; j++) {
                    //在i帧后，delay长度范围内，进行递增查找，以判定当前帧是否为波谷
                    if ( frame < data[i + j] && i - lastSign > minLag && frame < 0.5) {
                        sign = i;
                        lastSign = i;
                        break;
                    }else if ( frame > data[i + j]) {
                        //当出现递减帧时，即i不是标记点,中断查找并返回默认值（负值）
                        break;
                    }
                }
            }
        }
        return sign;
    }
    /**
     * 逐帧判定是否存在标记点
     * @param data 数据
     * @param delay 延迟 即标记点在【当前数据末尾】-【delay】处进行判定
     * @param minLag 与上一标记点的最小间隔
     * @return 标记点索引值，当判定为非标记点时此项返回负值
     */
    private static int signFind( List<Float> data , int delay , int minLag ){
        int sign=-99;
        //当【数据长度】大于【上一标记点+最小间隔+延迟】时
        if(data.size()>lastSign+minLag+delay) {
            int i = data.size() - delay - 1;
            Float frame = data.get(i);
            //判断当前数据是否处于下降阶段
            if (data.get(i - 1) > frame && data.get(i - 1) > frame ) {
                for (int j = 1; j < delay + 1; j++) {
                    //在i帧后，delay长度范围内，进行递增查找，以判定当前帧是否为波谷
                    if ( frame < data.get(i + j) && i - lastSign > minLag && frame < 0.5) {
                        sign = i;
                        lastSign = i;
                        break;
                    }else if ( frame > data.get(i + j)) {
                        //当出现递减帧时，即i不是标记点,中断查找并返回默认值（负值）
                        break;
                    }
                }
            }
        }
        return sign;
    }

    /**
     * 截取数据窗口为[标记点-窗口长度，标记点]，进行{MAV,VAR,RMS,WL}特征提取
     * @param data 数据
     * @param sign 标记点，
     * @param winLength 窗口长度
     * @return 特征向量
     */
    public double[] feaExtract(List<Float> data , int sign,int winLength){
        List<Float> winData = new ArrayList<>(data.subList(sign-winLength+1,sign));//截取窗口数据
//        return new double[]{ calMAV(winData) , calVAR(winData) , calRMS(winData) , calWL(winData) };
        return new double[]{1,1};
    }
    public double[] feaExtract(double[] data , int sign,int winLength){
        double[] a = Arrays.copyOfRange(data,sign-winLength+1,sign);
//        List<Double> winData = new ArrayList<Double>();//截取窗口数据
//        Collections.addAll(winData,a);
        List<Double> winData =Arrays.stream(a).boxed().collect(Collectors.toList());
        return new double[]{ calMAV(winData) , calVAR(winData) , calRMS(winData) , calWL(winData) };
    }

    /**
     * 均方根计算
     * @param data 输入数据
     * @return 均方根
     */
    public double calRMS(List<Double> data) {
        float total =0;

        for(int i=0;i<data.size();i++){
            total += Math.pow(data.get(i),2);//平方和
        }
        return Math.sqrt(total/(data.size()));
    }
    public double calRMS(double[] data) {
        float total =0;

        for(int i=0;i<data.length;i++){
            total += Math.pow(data[i],2);//平方和
        }
        return Math.sqrt(total/(data.length));
    }

    /**
     * 平均值计算
     * @param data 输入数据
     * @return 平均值
     */
    private static double calMAV(List<Double> data) {
        double sum = 0;
        for (double value : data) {
            sum += Math.abs(value);
        }
        double k = (double) sum / data.size();

        return featureFormat(k) ;
    }

     /**
     * 方差计算
     * @param data 输入数据
     * @return 方差
     */
    private static double calVAR(List<Double> data) {
        double sum = 0;
        double total =0;
        for (double value : data) {
            sum += Math.pow(value,2);
        }
        double k = sum/(data.size()-1);

        return featureFormat(k);
    }

    //计算波长公式

    /**
     *
     * @param data 输入数据
     * @return 波长
     */
    private static double calWL(List<Double> data) {
        double sum = 0;
        for(int i=0;i<data.size()-1;i++){
            sum += Math.abs(data.get(i+1)-data.get(i));
        }
        double k =sum;

        return featureFormat(k);
    }

    /**
     * 对double数进行有效数字保留
     * @param k 输入数据
     * @return 输出数据
     */
    private static double featureFormat(double k) {
        k = (double)Math.round(k*100000)/100000;
        return k ;
    }

//    /**
//     * 数组合并
//     * @param first 数组1
//     * @param rest 数组
//     * @param <T> 泛型
//     * @return 合并后的同类型数组
//     */
//    public static <T> T[] concatAll(T[] first, T[]... rest) {
//        int totalLength = first.length;
//        for (T[] array : rest) {
//            totalLength += array.length;
//        }
//        T[] result = Arrays.copyOf(first, totalLength);
//        int offset = first.length;
//        for (T[] array : rest) {
//            System.arraycopy(array, 0, result, offset, array.length);
//            offset += array.length;
//        }
//
//        return result;
//    }

    public static double[] concatAll(double[] first, double[]... rest) {
        int totalLength = first.length;
        for (double[] array : rest) {
            totalLength += array.length;
        }
        double[] result = Arrays.copyOf(first, totalLength);
        int offset = first.length;
        for (double[] array : rest) {
            System.arraycopy(array, 0, result, offset, array.length);
            offset += array.length;
        }

        return result;
    }

    /**
     * LDA计算
     * @param features 特征值数组
     * @return 与各标签点距离
     */
    public double[] LDACalculator(double[] features){
        //特征向量
        EbmVector vector = EbmVector.mv(features);
        //降维：左乘降维矩阵
        EbmVector distance = dimReMatrix.times(vector);
        //计算降维后向量距离各标签点欧氏距离
        double d_Walk = EbmVector.distance(distance,labDot_Walk);
        double d_UpStair = EbmVector.distance(distance,labDot_UpStair);
        double d_DownStair = EbmVector.distance(distance,labDot_DownStair);

        return new double[]{ d_Walk , d_UpStair , d_DownStair };
    }

    /**
     * LDA识别
     * @param features 特征值
     * @return 识别结果
     */
    public int LDARecognition(double[] features){
        double[] distance = LDACalculator(features);
        return calMin(distance);
    }

    /**
     * 查找数组内的最小值
     * @param arr 数组
     * @return 最小值的序号
     */
    public static int calMin(double arr[]) {
        double aarMin =arr[0];
        int aar_index=0;
        if(arr.length>0){
            for(int i=0;i<arr.length;i++){
                if(arr[i]<	aarMin){
                    aarMin=arr[i];
                    aar_index = i;
                }
            }
        }
        return aar_index;
    }


}
