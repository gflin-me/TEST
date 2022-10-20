package leetcode;

import java.util.*;

public class Midea {
    static List<double[]> ans = new ArrayList<>();
    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        double threshold = sc.nextDouble();
        List<double[]> point = new ArrayList<>();
        while (sc.hasNext()){
            double x = sc.nextDouble();
            double y = sc.nextDouble();
            point.add(new double[]{x,y});
        }
        double[][] position = point.toArray(new double[point.size()][2]);
        douglas(position,0,position.length-1,threshold);
        for(double[] p:ans){
            System.out.println((int)p[0]+" "+(int)p[1]);
        }

    }
    public static void douglas(double[][] position,int s,int e,double threshold){
        if(s>e) return;
        double[] head = position[s];
        double[] tail = position[e];
        double a = (tail[1]-head[1])/(tail[0]-head[0]);
        double b = a*tail[0]-tail[1];
        double dMax = 0;
        int indexMax = 0;
        for(int i=s;i<=e;i++){
            double d = 0;
            if(a==0){
                d = position[i][1]-tail[1];
            }else{
                d = Math.abs((a*position[i][0]-position[i][1]-b)/Math.sqrt(a*a+1));
            }

            if(d>dMax){
                dMax=d;
                indexMax=i;
            }
        }
        if(dMax>=threshold){
            ans.add(position[indexMax]);
            douglas(position,indexMax,e,threshold);
            douglas(position,s,indexMax,threshold);


        }
    }

//    public static void main(String[] args) {
//        Scanner sc = new Scanner(System.in);
//        List<Integer> nums = new ArrayList<>();
//        while (sc.hasNext()){
//            nums.add(sc.nextInt());
//        }
//        int maxF = nums.get(0);
//        int minF =nums.get(0);
//        int ans = nums.get(0);
//
//        for(int i=1;i<nums.size();i++){
//            int mx=maxF ,mn = minF;
//            maxF = Math.max(mx* nums.get(i),Math.max(nums.get(i),mn*nums.get(i)));
//            minF= Math.min(mn* nums.get(i),Math.min(nums.get(i),mx*nums.get(i)));
//            ans = Math.max(maxF,ans);
//
//        }
//
//
//        System.out.println(ans);
//    }
}
