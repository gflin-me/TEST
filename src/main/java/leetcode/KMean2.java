package leetcode;

import java.io.IOException;
import java.util.*;

/**
 * @className: KMean2
 * @description: 实现kmean无监督分类
 * @author: Lin Guifeng
 * @date: 2022/09/03 15:07
 * @version: 1.0
 **/
public class KMean2 {

    public static void main(String[] args) throws IOException {
        float[][] data = {{13,13},{-13,-3},{-5,-3},{14,13},{15,13},{13,14},{16,13},{13,17},{18,13},{13,15},{15,14},
                {-4,-3},{-3,-4},{-6,-3},{-3,-5},{-4,-3},{-3,-5}};
        int k=2;
        k_mean(k,data);
        for(int i=0;i<k;i++){
            List<Integer> per = list.get(i);
            for(int j=0;j<per.size();j++){
                System.out.print(per.get(j)+" ");
            }
            System.out.println(" ");
        }
    }
    static int numFea;
    static int numSample;
    static int kk;
    static List<List<Integer>> list = new ArrayList<>();
    public static void k_mean(int k,float[][] data){
        kk = k;
        numSample = data.length;
        numFea = data[0].length;
        int numIteration = 10;
        Map<Integer, float[]> dataAll = new HashMap<>();
        for(int i=0;i<numSample;i++){
            dataAll.put(i,data[i]);
        }
        for(int i=0;i<kk;i++){
            List<Integer> t = new ArrayList<>();
            list.add(t );
        }
        //生成第一次随机中心点
        float[][] points = randomPoint(dataAll,k);
        //迭代分类
        for(int i=0;i<numIteration;i++){
            //样本分类
            list.clear();
            for(int m=0;m<kk;m++){
                List<Integer> t = new ArrayList<>();
                list.add(t );
            }
            for(int j=0;j<numSample;j++){

                list.get(Classify(dataAll.get(j),points)).add(j);
            }
            points = newPoint(dataAll);
        }
    }

    /**
     * 随机取k个样本作为中心点
     * @param dataAll 总样本
     * @param k 类数量
     * @return 中心点坐标
     */
    public static float[][] randomPoint( Map<Integer, float[]> dataAll, int k ){
        Random random = new Random();
        float[][] points = new float[k][dataAll.get(0).length];
        int numData = dataAll.size();
        for(int i=0;i<k;i++){
            points[i] = dataAll.get(random.nextInt(numData));
        }
        return points;
    }

    /**
     * 单样本分类
     * @param feaVector 输入样本特征向量
     * @param points 中心点
     * @return 类别索引
     */
    public static int Classify( float[] feaVector,float[][] points){
        int classNum = -1;
        float minDist = Float.MAX_VALUE;
        for(int i = 0; i< kk; i++){
            float distance=0;
            for(int j=0;j<numFea;j++){
                distance+=Math.abs(feaVector[j]-points[i][j]);
            }
            if(distance<minDist){
                minDist = distance;
                classNum=i;
            }
        }
        return classNum;
    }

    /**
     * 计算类中心点
     * @param dataAll 总数据
     * @return k个类的中心点坐标
     */
    public static float[][] newPoint(Map<Integer, float[]> dataAll){
        float[][] points = new float[kk][numFea];
        for(int i = 0; i< kk; i++){
            List<Integer> listOf = list.get(i);
            int numSmp = listOf.size();
            for(int j=0;j<numFea;j++){
                for(int k=0;k<numSmp;k++){
                    points[i][j]+=dataAll.get(listOf.get(k))[j]/numSmp;
                }
            }
        }
        return points;
    }
}
