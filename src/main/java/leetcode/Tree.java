package leetcode;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @className: Tree
 * @description: TODO
 * @author: Lin Guifeng
 * @date: 2022/08/03 2:15
 * @version: 1.0
 **/
public class Tree {
    //定义二叉树的结构

    public static void main(String[] args) throws IOException {
        double[] x = new double[1000];
        for(int i=0;i<1000;i++){
            x[i] = i/5.0;
        }
        double[] y = new double[1000];
        for(int i=0;i<1000;i++){
            y[i] = 2 * Math.cos(2 * Math.PI * 300 * x[i])+ 5 * Math.sin(2 * Math.PI * 100 * x[i])+4*Math.random() ;
        }
        int[] ans = ampd(y);
        System.out.println(Arrays.toString(x));
        System.out.println(Arrays.toString(y));
        System.out.println(Arrays.toString(ans));
    }
    public static int[] ampd(double[] data){

        int count = data.length;
        int[] pData = new int[count];
        List<Integer> arr_rowSum = new ArrayList<>();
        int min = Integer.MAX_VALUE;
        int min_index = -1;
        for(int k=0;k<count/2+1;k++){
            int row_sum = 0;
            for(int i=k;i<count-k;i++){
                if(data[i]>data[i-k]&&data[i]>data[i+k]){
                    row_sum-=1;
                }
            }
            arr_rowSum.add(row_sum);
            if(row_sum<min) {
                min_index = arr_rowSum.size()-1;
                min = row_sum;
            }
        }
        int max_windowlength = min_index;
        for(int k=0;k<max_windowlength+1;k++){
            for(int i=k;i<count-k;i++){
                if(data[i]>data[i-k]&&data[i]>data[i+k]){
                    pData[i]+=1;
                }
            }
        }
        int[] ans = new int[count];
        for(int i=0;i<count;i++){
            if(pData[i]==max_windowlength) ans[i]=1;
        }
        return ans;

    }


}
