package leetcode;

import java.io.IOException;
import java.util.*;

public class huawei {
    //题目1：超级玛丽过吊桥
//    public static void main(String[] args) throws IOException {
//        Scanner sc = new Scanner(System.in);
//        int m = sc.nextInt();
//        int n = sc.nextInt();
//        int k = sc.nextInt();
//        int[] indexK = new int[k];
//        int[] bridge = new int[n+2];
//        for(int i=0;i<k;i++){
//            bridge[sc.nextInt()]=1;
//        }
//
//        int[][] dp = new int[n+2][m];
//        dp[0][0]=1;
//        for(int i=1;i<n+2;i++){
//            if(bridge[i]==0){
//                if(i>=1) {
//                    for(int j=0;j<m;j++){
//                        dp[i][j]+=dp[i-1][j];
//                    }
//                }
//                if(i>=2){
//                    for(int j=0;j<m;j++){
//                        dp[i][j]+=dp[i-2][j];
//                    }
//                }
//                if(i>=3){
//                    for(int j=0;j<m;j++){
//                        dp[i][j]+=dp[i-3][j];
//                    }
//                }
//            }else if(bridge[i]==1){
//                if(i>=1) {
//                    for(int j=1;j<m;j++){
//                        dp[i][j]+=dp[i-1][j-1];
//                    }
//                }
//                if(i>=2){
//                    for(int j=1;j<m;j++){
//                        dp[i][j]+=dp[i-2][j-1];
//                    }
//                }
//                if(i>=3){
//                    for(int j=1;j<m;j++){
//                        dp[i][j]+=dp[i-3][j-1];
//                    }
//                }
//            }
//
//        }
//        int sum = 0;
//        for(int i=0;i<m;i++){
//            sum+=dp[n+1][i];
//        }
//        System.out.println(sum);
//    }


    //题目2：数据发送和接收问题
//    public static void main(String[] args) throws IOException{
//        Scanner sc = new Scanner(System.in);
//        int m = sc.nextInt();
//        int n = sc.nextInt();
//        int[] p = new int[n];
//        int[][] qs = new int[m][2];
//        int[] runtime = new int[n];
//        for(int i=0;i<n;i++){
//            p[i] = sc.nextInt();
//        }
//        for(int i=0;i<m;i++){
//            qs[i][0] = sc.nextInt();
//        }
//        for(int i=0;i<m;i++){
//            qs[i][1] = sc.nextInt();
//        }
//        //根据数据包花费时间排序
//        Arrays.sort(qs, new Comparator<int[]>() {
//            @Override
//            public int compare(int[] o1, int[] o2) {
//                return o2[1]-o1[1];
//            }
//        });
//        //贪心
//        int curMinTime = Integer.MAX_VALUE;
//        int curIndex = 0;
//        for(int i=0;i<m;i++){
//            for(int j=0;j<n;j++){
//                if(qs[i][0]>p[j]) continue;
//                if(curMinTime> runtime[j]){
//                    curMinTime = runtime[j];
//                    curIndex = j;
//                }else if(curMinTime== runtime[j]&&p[curIndex]>p[j]){
//                    curIndex = j;
//                }
//            }
//            runtime[curIndex]+=qs[i][1];
//            curMinTime = Integer.MAX_VALUE;
//        }
//        //输出最长的时间
//        int ans= runtime[0];
//        for (int x = 1; x< runtime.length; x++){
//            if(runtime[x]>ans){
//                ans= runtime[x];
//            }
//        }
//        System.out.println(ans);
//    }

    //题3：路由器与报文传输
    public static void main(String[] args) throws IOException{
        Scanner sc = new Scanner(System.in);
        int n = sc.nextInt();
        int start = sc.nextInt();
        int end = sc.nextInt();
        int ttl = sc.nextInt();
        int[][] length = new int[501][501];
        for(int i=0;i<n;i++){
            int head = sc.nextInt();
            int tail = sc.nextInt();
            int l = sc.nextInt();
            length[head][tail] = l;
            length[tail][head] = l;
        }
    }


}

