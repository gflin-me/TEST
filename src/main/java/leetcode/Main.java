package leetcode;

import java.io.IOException;
import java.util.*;


public class Main {
    public static void main(String[] args) throws IOException{
        int[] a = {1,56,3,23,67,8,90,43,3,45,5,8,34,11};
        bubbleSort(a);
        System.out.println(Arrays.toString(a));

    }
    public static void  quickSort(int[] nums,int left,int right){
        if(left>=right) return;
        int l = left,r=right;
        while(l<r){
            while(l<r&&nums[r]>=nums[left]) r--;
            while(l<r&&nums[l]<=nums[left]) l++;
            swap(nums,l,r);
        }
        swap(nums,l,left);
        quickSort(nums,left,l-1);
        quickSort(nums,l+1,right);

    }
    public static void bubbleSort(int[] nums){
        for(int i=0;i<nums.length-1;i++){
            for (int j=0;j<nums.length-1-i;j++){
                if(nums[j]>nums[j+1]){
                    swap(nums,j,j+1);
                }
            }
        }
    }
    public static void swap(int[] nums,int i,int j){
        int temp = nums[i];
        nums[i] = nums[j];
        nums[j] = temp;
    }

}





