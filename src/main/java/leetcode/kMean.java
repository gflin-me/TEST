package leetcode;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;
/**
 * @className: kMean
 * @description: 使用kmean将数据分为三类
 * @author: Lin Guifeng
 * @date: 2022/09/03 14:28
 * @version: 1.0
 **/
public class kMean {
    static String filePath = System.getProperty("user.dir")+"\\src\\sources\\consumption.csv";
    //key是样本id,List<Float>是特征向量
    static Map<Integer,List<Float>> map = new HashMap<>();//总数据
    static Map<Integer,List<Float>> map1 = new HashMap<>();//第一类数据
    static Map<Integer,List<Float>> map2 = new HashMap<>();//第二类数据
    static Map<Integer,List<Float>> map3 = new HashMap<>();//第三类数据
    static List<Float> list1 = new ArrayList();//第一个中心
    static List<Float> list2 = new ArrayList();//第二个中心
    static List<Float> list3 = new ArrayList();//第三个中心
    static int numSamples ;

    public static void main(String[] args) {
        System.out.print("请输入迭代次数：");
        Scanner input = new Scanner(System.in);
        int m = input.nextInt();
        //读取数据
        ReadFile();
        //生成第一次的中心点
        System.out.print("第一次随机生成中心点：");
        RandPoint();
        //分类，求中心，再分类
        KMeans(m);
    }

    //读取数据，存入map
    public static void  ReadFile(){
        BufferedReader br = null;
        String line = "";
        String csvSplitBy = ",";
        try {
            br = new BufferedReader(new FileReader(filePath));
            while ((line = br.readLine()) != null) {
                // 分割点为
                List<String> post = Arrays.asList(line.split(csvSplitBy));
                if (isNumeric(post.get(0))) {
                    int x = Integer.parseInt(post.get(0));
                    List<Float> list = new ArrayList<>();
                    list.add(Float.valueOf(post.get(1)));
                    list.add(Float.valueOf(post.get(2)));
                    list.add(Float.valueOf(post.get(3)));
                    map.put(x,list);
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (br != null) {
                try {
                    br.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
    //判断是否是数字
    public static boolean isNumeric(String str){
        for (int i = str.length();--i>=0;){
            if (!Character.isDigit(str.charAt(i))){
                return false;
            }
        }
        return true;
    }
    //第一次，产生三个随机点
    public static void RandPoint(){
        Random r = new Random();
        list1 = map.get((r.nextInt(numSamples)));
        list2 = map.get((r.nextInt(numSamples)));
        list3 = map.get((r.nextInt(numSamples)));
        System.out.print(list1.toString());
        System.out.print(list2.toString());
        System.out.println(list3.toString());
    }
    //给定一个map的value，判断他是哪个类,给数据分类

    /**
     * 根据样本中心点判断类别
     * @param list
     * @param index
     */
    public static void IsKM(List<Float> list,int index){
        //计算距离几个样本点的欧式距离，这里仅到index2是示例数据特征数量为3
        float x1 = Math.abs(list1.get(0)-list.get(0))+Math.abs(list1.get(1)-list.get(1))
                +Math.abs(list1.get(2)-list.get(2));
        float x2 = Math.abs(list2.get(0)-list.get(0))+Math.abs(list2.get(1)-list.get(1))
                +Math.abs(list2.get(2)-list.get(2));
        float x3 = Math.abs(list3.get(0)-list.get(0))+Math.abs(list3.get(1)-list.get(1))
                +Math.abs(list3.get(2)-list.get(2));
        float min = Math.min(x1, x2);
        min = Math.min(min, x3);
        //根据距离分类
        if (min == x1){
            map1.put(index,list);
            //System.out.println(index + "属于第1类，中心点为"+list1.toString());
        }
        if(min == x2){
            map2.put(index,list);
            //System.out.println(index + "属于第2类，中心点为"+list2.toString());
        }
        if(min == x3){
            map3.put(index,list);
            //System.out.println(index + "属于第3类，中心点为"+list3.toString());
        }
    }
    //计算map中数据与中心点的距离

    /**
     * k-mean
     * @param m 迭代次数
     */
    public static void KMeans(int m) {
        for (int i = 0;i<m;i++){
            map1.clear();
            map2.clear();
            map3.clear();
            //轮询总数据每一个样本，计算欧氏距离，分类
            for (Map.Entry<Integer,List<Float>> entry : map.entrySet()) {
                IsKM(entry.getValue(),entry.getKey());
            }
            NewPoint();
            System.out.print(list1.toString());
            System.out.print(list2.toString());
            System.out.println(list3.toString());
        }
        System.out.println("第一个中心点"+map1);
        System.out.println("第二个中心点"+map2);
        System.out.println("第三个中心点"+map3);
    }
    //计算三个类的新中心
    public static void NewPoint(){
        //重置中心点
        list1.clear();
        list2.clear();
        list3.clear();
        //一列数据的和
        float sum1 = 0;
        float sum2 = 0;
        float sum3 = 0;
        for (Map.Entry<Integer,List<Float>> entry : map1.entrySet()) {
            //System.out.println(entry.getValue());
            //map最后一个value为空，要进行一波判断
            if (entry.getValue().size()>0) {
                sum1 = sum1 + entry.getValue().get(0);
                sum2 = sum2 + entry.getValue().get(1);
                sum3 = sum3 + entry.getValue().get(2);
            }
        }
        list1.add(sum1/map1.size());
        list1.add(sum2/map1.size());
        list1.add(sum3/map1.size());
        sum1=0;
        sum2=0;
        sum3=0;
        for (Map.Entry<Integer,List<Float>> entry : map2.entrySet()) {
            if (entry.getValue().size()>0){
                sum1 = sum1 + entry.getValue().get(0);
                sum2 = sum2 + entry.getValue().get(1);
                sum3 = sum3 + entry.getValue().get(2);
            }
        }
        list2.add(sum1/map2.size());
        list2.add(sum2/map2.size());
        list2.add(sum3/map2.size());
        sum1=0;
        sum2=0;
        sum3=0;
        for (Map.Entry<Integer,List<Float>> entry : map3.entrySet()) {
            if (entry.getValue().size()>0){
                sum1 = sum1 + entry.getValue().get(0);
                sum2 = sum2 + entry.getValue().get(1);
                sum3 = sum3 + entry.getValue().get(2);
            }
        }
        list3.add(sum1/map3.size());
        list3.add(sum2/map3.size());
        list3.add(sum3/map3.size());
    }




}
