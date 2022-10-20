package leetcode;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.*;

/**
 * @className: leetcode
 * @description: TODO
 * @author: Lin Guifeng
 * @date: 2022/08/10 21:02
 * @version: 1.0
 **/
class leetcode {
//    public static void main(String[] args) throws Exception {
//        Scanner sc = new Scanner(System.in);
//        int money = sc.nextInt();
//        int num = sc.nextInt();
//        Good[] goods = new Good[num+1];
//        for(int i=1;i<=num;i++){
//            int cost = sc.nextInt();
//            int worth = sc.nextInt();
//            int q = sc.nextInt();
//            goods[i] = new Good(cost,worth,q);
//        }
//        for(int i=1;i<=num;i++){
//            int q = goods[i].id;
//            if(q!=0){
//                if(goods[q].q1==0){
//                    goods[q].setQ1(i);
//                }else{
//                    goods[q].setQ2(i);
//                }
//            }
//        }
//
//        int[][] dp = new int[num+1][money+1];
//        for(int i=1;i<=num;i++){
//            int cost=0,cost1=0,cost2=0,cost3=0;
//            int tdp=0,tdp1=0,tdp2=0,tdp3=0;
//            cost = goods[i].cost;
//            tdp=goods[i].worth*cost;
//            if(goods[i].q1!=0){
//                cost1 = goods[i].cost+goods[goods[i].q1].cost;
//                tdp1 = tdp+goods[goods[i].q1].cost*goods[goods[i].q1].worth;
//            }
//            if(goods[i].q2!=0){
//                cost2 = goods[i].cost+goods[goods[i].q2].cost;
//                tdp2 = tdp+goods[goods[i].q2].cost*goods[goods[i].q2].worth;
//            }
//            if(goods[i].q1!=0&&goods[i].q2!=0){
//                cost3=goods[i].cost+goods[goods[i].q1].cost+goods[goods[i].q2].cost;
//                tdp3 = tdp+goods[goods[i].q1].cost*goods[goods[i].q1].worth+goods[goods[i].q2].cost*goods[goods[i].q2].worth;
//            }
//            for(int j=1;j<=money;j++){
//                dp[i][j] = dp[i-1][j];
//                if(goods[i].id==0){
//                    if(j>=cost) dp[i][j] = Math.max(dp[i][j],dp[i-1][j-cost]+tdp);
//                    if(j>=cost1&&cost1!=0) dp[i][j] = Math.max(dp[i][j],dp[i-1][j-cost1]+tdp1);
//                    if(j>=cost2&&cost2!=0) dp[i][j] = Math.max(dp[i][j],dp[i-1][j-cost2]+tdp2);
//                    if(j>=cost3&&cost3!=0) dp[i][j] = Math.max(dp[i][j],dp[i-1][j-cost3]+tdp3);
//                }
//            }
//
//        }
//        System.out.println(dp[num][money]);
//    }
    public static void main(String[] args) throws IOException {
        int[][] a = {{2,3},{4,5},{6,7},{8,9},{1,10}};
        System.out.println( champagneTower(2,1,1));
    }
    int m,n;
    public void solve(char[][] board) {
        m = board.length;
        n=board[0].length;
        if(m<3||n<3) return;
        for(int i=0;i<m;i++){
            dfs(board,i,0);
            dfs(board,i,n-1);
        }
        for(int i=1;i<n-1;i++){
            dfs(board,0,i);
            dfs(board,m-1,i);
        }
        for(int i=0;i<m;i++){
            for(int j=0;j<n;j++){
                if(board[i][j]=='A') {
                    board[i][j]='O';
                }else if(board[i][j]=='O') {
                    board[i][j]='X';
                }
            }
        }
    }
    public String reverseWords(String s) {
        int length= s.length();
        StringBuffer sb = new StringBuffer();
        Stack<String> stack = new Stack<>();
        for(int i=0;i<length;i++){
            char c = s.charAt(i);
            if(c==' ') {
                if(sb.length()==0 ) continue;
                stack.add(sb.toString());
                sb.setLength(0);
            }else{
                sb.append(c);
            }
        }
        if(sb.length()>0) stack.add(sb.toString());
        StringBuffer ans = new StringBuffer();
        while(!stack.isEmpty()){
            ans.append(stack.pop());
            ans.append(" ");
        }
        ans.deleteCharAt(ans.length()-1);
        return  ans.toString();
    }
    public int[] prisonAfterNDays(int[] cells, int n) {
        int length = cells.length;
        int[] newCells = Arrays.copyOf(cells,length);
        newCells[0]=0;newCells[length]=0;
        StringBuffer sb = new StringBuffer();
        Set<String> set = new HashSet<>();
        List<int[]> list = new ArrayList<>();
        int day=0;
        for(int i=0;i<n;i++){
            sb.append('0');
            for(int j=1;j<length-1;j++){
                newCells[j] = ~(newCells[j-1]^newCells[j+1]);
                sb.append(newCells[j]+'0');
            }
            sb.append('0');
            String str = sb.toString();
            sb.setLength(0);
            if(set.contains(str)){
                break;
            }else{
                set.add(str);
                list.add(Arrays.copyOf(newCells,length));
                day++;
            }
        }

        return list.get(n%day);
    }
    public void dfs(char[][] board,int i,int j){
        if(i<0||i>=m||j<0||j>=n||board[i][j]!='o') return;
        board[i][j]='A';
        dfs(board,i+1,j);
        dfs(board,i-1,j);
        dfs(board,i,j+1);
        dfs(board,i,j-1);
    }
    public String removeDuplicateLetters(String s) {
        //vis(26)用来存储是否存在栈中
        boolean[] vis = new boolean[26];
        //numChar统计每个字母的出现次数
        int[] numChar = new int[26];
        for(int i=0;i<s.length();i++) numChar[s.charAt(i)-'a']++;

        StringBuffer sb = new StringBuffer();
        for(int i=0;i<s.length();i++){
            char c = s.charAt(i);
            //若【当前字符】在栈中未出现过
            if(!vis[c-'a']){
                //当【当前字符】小于【栈顶字符】，
                // 且【栈顶字符】还有相同字符未入栈，
                // 删除【栈顶字符】，存入【当前字符】
                while(sb.length()>0 && sb.charAt(sb.length()-1)>c){
                    char lastChar = sb.charAt(sb.length()-1);
                    //当【栈顶字符】的数量大于0（即还有未存入栈sb中的同字符）
                    if(numChar[lastChar-'a']>0){
                        sb.deleteCharAt(sb.length()-1);
                        vis[lastChar-'a']=false;
                    }else {
                        break;
                    }
                }
                //当【当前字符】大于等于【栈顶字符】，【当前字符】入栈
                sb.append(c);
                vis[c-'a']=true;
            }
            //若【当前字符】栈中已有，跳过，并减去次数
            numChar[c-'a']-=1;
        }
        return sb.toString();
    }

    public static double champagneTower(int poured, int query_row, int query_glass) {

        double pour = poured;
        if(query_row==0) return pour >1 ? 1:pour ;
        double[][] all = new double[query_row+1][query_row+1];
        all[0][0] = pour ;
        for(int i=1;i<=query_row;i++){
            for(int j=0;j<=i;j++){
                if(j<i) all[i][j] += all[i-1][j]<1 ? 0:(all[i-1][j]-1)*0.5;
                if(j>0) all[i][j] += all[i-1][j-1]<1 ? 0:(all[i-1][j-1]-1)*0.5;
            }
        }
        return all[query_row][query_glass]>1 ? 1:all[query_row][query_glass];
    }
    public int numIslands(char[][] grid) {
        int m = grid.length;
        int n = grid[0].length;
        int[] parents = new int[m*n];
        for(int k=0;k<m*n;k++){
            parents[k] = k;
        }
        for(int i=0;i<m;i++){
            for(int j=0;j<n;j++){
                if(grid[i][j]=='1'){
                    int index = i*m+j;
                    if(j<n-1&&grid[i][j+1]=='1') union(parents,index,index+1);
                    if(j<n-1&&grid[i+1][j]=='1') union(parents,index,index+m);
                }
            }
        }
        int ans=0;
        for(int k=0;k<m*n;k++){
            if(parents[k] == k) ans++;
        }
        return ans;
    }
    public void union(int[] parents,int i,int j){
        parents[find(parents,i)]=parents[find(parents,j)];
    }
    public int find(int[] parents,int i){
        if(parents[i]!=i){
            parents[i] = find(parents,parents[i]);
        }
        return parents[i];
    }
}
class Good{
    public int cost;
    public int worth;
    public int id;
    public int q1=0;
    public int q2=0;
    public Good(int cost, int worth , int id){
        this.cost=cost;
        this.worth=worth;
        this.id = id;
    }
    public void setQ1(int q1){
        this.q1=q1;
    }
    public void setQ2(int q2){
        this.q2 = q2;
    }

}