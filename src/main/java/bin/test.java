package bin;

import java.util.*;

/**
 * @className: test
 * @description: TODO
 * @author: Lin Guifeng
 * @date: 2022/07/29 20:20
 * @version: 1.0
 **/
public class test {
    public static void main(String[] args){
        int[] nums = new int[]{2,3,2};

    }
    public int findMinArrowShots(int[][] points) {
        Arrays.sort(points, new Comparator<int[]>() {
            @Override
            public int compare(int[] o1, int[] o2) {
                return o1[0]-o2[0];
            }
        });
        int[] last = points[0];
        int ans = points.length;
        for(int i=1;i<points.length;i++){
            if(points[i][0]<=last[1]){
                last[0]=points[i][0];
                ans--;
            }else{
                last = points[i];
            }
        }
        return ans;
    }
    public TreeNode lowestCommonAncestor(TreeNode root, TreeNode p, TreeNode q) {
        if(root == null) return null; // 如果树为空，直接返回null
        if(root == p || root == q) return root; // 如果 p和q中有等于 root的，那么它们的最近公共祖先即为root（一个节点也可以是它自己的祖先）
        TreeNode left = lowestCommonAncestor(root.left, p, q); // 递归遍历左子树，只要在左子树中找到了p或q，则先找到谁就返回谁
        TreeNode right = lowestCommonAncestor(root.right, p, q); // 递归遍历右子树，只要在右子树中找到了p或q，则先找到谁就返回谁
        if(left == null) return right; // 如果在左子树中 p和 q都找不到，则 p和 q一定都在右子树中，右子树中先遍历到的那个就是最近公共祖先（一个节点也可以是它自己的祖先）
        else if(right == null) return left; // 否则，如果 left不为空，在左子树中有找到节点（p或q），这时候要再判断一下右子树中的情况，如果在右子树中，p和q都找不到，则 p和q一定都在左子树中，左子树中先遍历到的那个就是最近公共祖先（一个节点也可以是它自己的祖先）
        else return root; //否则，当 left和 right均不为空时，说明 p、q节点分别在 root异侧, 最近公共祖先即为 root
    }

    public static int findMaxForm(String[] strs, int m, int n) {
        int length = strs.length;
        int[][] zerosones = new int[length][2];
        for(int i=0;i<length;i++){
            zerosones[i] = calZreoOne(strs[i]);
        }
        int[][][] dp = new int[length+1][m+1][n+1];
        for(int i=1;i<length+1;i++){
            int numZero = zerosones[i-1][0];
            int numOne = zerosones[i-1][1];
            for(int j=0;j<=m;j++){
                for(int k=0;k<=n;k++){
                    dp[i][j][k] = dp[i-1][j][k];
                    if(j>=numZero&&k>=numOne) dp[i][j][k] = Math.max( dp[i-1][j][k] , dp[i-1][j-numZero][k-numOne]+1 );
                }
            }

        }
        return dp[length][m][n];
    }
    public static int[] calZreoOne(String str){
        int length = str.length();
        int[] ans = new int[2];
        for(int i=0;i<length;i++){
            ans[str.charAt(i)-'0']++;
        }
        return ans;
    }

    public static int numSquares(int n) {
        int length = (int)Math.sqrt(n);
        int[] sqNum = new int[length+1];
        for(int i=1;i<length+1;i++){
            sqNum[i] = (int) Math.pow(i,2);
        }
        int[][] dp = new int[length+1][n+1];

        for(int i=0;i<=n;i++) {
            dp[0][i] = n+1;
        }

        for(int i=1;i<=length;i++){
            for (int j=1;j<=n;j++){
                if(j>sqNum[i]){
                    dp[i][j] = Math.min(dp[i-1][j],dp[i][j-sqNum[i]]+1);
                }else if(j<sqNum[i]) {
                    dp[i][j] = dp[i-1][j];
                }else if(j==sqNum[i]){
                    dp[i][j] = 1;
                }
            }
        }
        return dp[length][n];
    }


    public static int[] exchange(int[] nums) {
        int left=0,right=nums.length-1;
        while(left<right){
            while(nums[left]%2==1) left++;
            while(nums[right]%2==0) right--;

            int temp = nums[left];
            nums[left] = nums[right];
            nums[right]=temp;
        }
        return nums;
    }
    public int[] exchange1(int[] nums) {
        int i = 0, j = nums.length - 1, tmp;
        while(i < j) {
            while(i < j && (nums[i] & 1) == 1) i++;
            while(i < j && (nums[j] & 1) == 0) j--;
            tmp = nums[i];
            nums[i] = nums[j];
            nums[j] = tmp;
        }
        return nums;
    }
    public String reverseWords(String s) {

        s=s.trim();
        StringBuilder word = new StringBuilder();
        int head = s.length()-1,tail = head;
        while(head>=0){
            while(head>=0&&s.charAt(head)!=' ') head--;
            word.append(s.substring(head+1,tail+1)+" ");
            while(head>=0&&s.charAt(head)==' ') head--;
            tail = head;
        }
        word.deleteCharAt(word.length()-1);
        return word.toString();
    }
    public static int[] spiralOrder(int[][] matrix) {
        if(matrix.length==0) return new int[1];
        int[][] way = new int[][]{{0,1},{1,0},{0,-1},{-1,0}};
        int m = matrix.length;
        int n = matrix[0].length;
        int[] ans = new int[m*n];
        boolean[][] used = new boolean[m][n];
        int i=0,j=0,q=0,ii=way[q][0],jj=way[q][1];
        for(int k=0;k<m*n;k++){
            ans[k] = matrix[i][j];
            used[i][j]=true;
            if(i+ii<m&&i+ii>=0&&j+jj<n&&j+jj>=0&&!used[i+ii][j+jj]){

            }else{
                q++;
                ii=way[q%4][0];
                jj=way[q%4][1];
            }
            i=i+ii;
            j=j+jj;
        }
        return ans;
    }

}
