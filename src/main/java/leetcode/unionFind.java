package leetcode;

/**
 * @className: unionFind
 * @description: TODO
 * @author: Lin Guifeng
 * @date: 2022/08/12 0:59
 * @version: 1.0
 **/
public class unionFind {
    public static void main(String[] args){
    }
    public int findCircleNum(int[][] isConnected) {
        int numCity = isConnected.length;
        //构建集合索引数组
        int[] parent = new int[numCity];
        for(int i=0;i<numCity;i++){
            parent[i]=i;
        }
        //历遍关系数组，当两个元素关联时，合并集合
        for(int i=0;i<numCity;i++){
            for(int j=i+1;j<numCity;j++){
                if(isConnected[i][j]==1) union(parent,i,j);
            }
        }
        int ans=0;
        for(int i=0;i<numCity;i++){
            if(parent[i]==i) ans++;
        }
        return ans;
    }
    //联通两个元素：给定相同的索引
    public void union(int[] parents,int i,int j){
        parents[find(parents,j)] = parents[find(parents,i)];
    }
    //找到某个元素的最初的那个索引
    public int find(int[] parents,int index){
        if(parents[index]!=index){
            parents[index] = find(parents,parents[index]);
        }
        return parents[index];
    }
}
