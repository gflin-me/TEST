package leetcode;

import java.io.*;
import java.util.*;

public class kedaxunfei {
    //流水线查找
//    public static void main(String[] args) throws IOException {
//        Scanner sc = new Scanner(System.in);
//        String str = sc.next();
//        int m = sc.nextInt();
//        int n =sc.nextInt();
//        List<String> list = new ArrayList<>();
//        while(sc.hasNext()){
//            list.add(sc.next());
//        }
//
//        char[][] graph = new char[m][n];
//        int index = 0;
//        for(int i=0;i<m;i++){
//            for(int j=0;j<n;j++){
//                graph[i][j] = str.charAt(index++);
//            }
//        }
//        Set<String> set = cal(graph,m,n,list);
//        for(String a:set){
//            System.out.print(a+" ");
//        }
//    }
//    static boolean[][] visited;
//    static Set<String> set = new HashSet<>();
//    public static Set<String> cal(char[][] graph,int m,int n,List<String> list){
//        for(String s:list){
//            for(int i=0;i<m;i++){
//                for(int j=0;j<n;j++){
//                    visited = new boolean[m][n];
//                    dfs(graph,i,j,s,0);
//                }
//            }
//        }
//        return set;
//    }
//    public static void dfs(char[][] graph,int i,int j ,String s,int index){
//        if(index==s.length()) {
//            set.add(s);
//            return;
//        }
//        if(i<0||i>=graph.length||j<0||j>=graph[0].length||visited[i][j]) return;
//        if(graph[i][j]!=s.charAt(index)) return;
//        visited[i][j] = true;
//        dfs(graph,i-1,j,s,index+1);
//        dfs(graph,i+1,j,s,index+1);
//        dfs(graph,i,j-1,s,index+1);
//        dfs(graph,i,j+1,s,index+1);
//    }

    //编辑距离
    public static void main(String[] args) throws IOException {
            Scanner sc = new Scanner(System.in);
        String dic = sc.next();
        String word = sc.next();
        StringBuffer sb = new StringBuffer();
        int length = word.length();
        List<String> list = new ArrayList<>();
        for(int i=0;i<dic.length();i++){
            char c = dic.charAt(i);
            if(c=='|'){
                list.add(sb.toString());
                sb.setLength(0);
            }else{
                sb.append(dic.charAt(i));
            }
            if(i==dic.length()-1){
                list.add(sb.toString());
            }
        }
        for(int i=0;i<list.size();i++){
            String str = list.get(i);
            int distance = minDistance(str,word);
            if(distance<=2) System.out.println(str);
        }
    }
    public static int minDistance(String word1,String word2){
        int n = word1.length() , m = word2.length();
        if(m*n==0) return n+m;
        int[][] dp = new int[n+1][m+1];
        for(int i=0;i<=n;i++){
            dp[i][0] = i;
        }
        for(int i=0;i<=m;i++){
            dp[0][i] = i;
        }
        for(int i=1;i<=n;i++){
            for(int j=1;j<=m;j++){
                char a1 = word1.charAt(i-1);
                char a2 = word2.charAt(j-1);
                if(a1!=a2){
                    dp[i-1][j-1]+=1;
                }
                dp[i][j] = Math.min(dp[i-1][j]+1,Math.min(dp[i][j-1]+1,dp[i-1][j-1]));
            }
        }
        return dp[n][m];
    }

//    public static void main(String[] args) throws IOException {
//        int[] nums = {1,2,3,4,5};
//        System.out.println("原 排 列 ："+Arrays.toString(nums));
//        System.out.println("上一个排列："+Arrays.toString(lastPermuation(nums)));
//        System.out.println("下一个排列："+Arrays.toString(nextPermutation(nums)));
//    }
//    //上一个排列
//    public static int[] lastPermuation(int[] nums){
//        //找到倒数第一个正向递减的位置i
//        int i = nums.length-2;
//        while( i>=0 && nums[i]<=nums[i+1]) i--;
//        //2.在[i,length-1]区间找到倒数第一个大于nums[i]的数，交换i,j
//        if(i>=0) {  //当i<0说明整个数组单调递增
//            int j = nums.length - 1;
//            while (j > i && nums[i] <= nums[j]) j--;
//            swap(nums, i, j);
//        }
//        //3.将整个区间[i+1,end]翻转
//        reverse(nums,i+1);
//        return nums;
//    }
//    //下一个字典序排列
//    public static int[] nextPermutation(int[] nums) {
//        //1.倒数第二个数开始遍历，找到倒数第二个正序递增的位置i
//        int i = nums.length - 2;
//        while (i >= 0 && nums[i] >= nums[i + 1]) i--;
//        //2.在[i,length-1]区间找到倒数第一个大于nums[i]的数，交换i,j
//        if (i >= 0) {  //当i<0说明整个数组单调递减
//            int j = nums.length - 1;
//            while (j >= 0 && nums[i] >= nums[j]) j--;
//            swap(nums, i, j);
//        }
//        //3.将整个区间[i+1,end]翻转
//        reverse(nums, i + 1);
//        return nums;
//    }
//    public static void swap(int[] nums, int i, int j) {
//        int temp = nums[i];
//        nums[i] = nums[j];
//        nums[j] = temp;
//    }
//
//    public static void reverse(int[] nums, int start) {
//        int left = start, right = nums.length - 1;
//        while (left < right) {
//            swap(nums, left, right);
//            left++;
//            right--;
//        }
//    }

}
