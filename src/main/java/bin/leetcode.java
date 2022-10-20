package bin;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.*;



public class leetcode {

    public static void main(String[] args) throws IOException {

        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        int length = Integer.parseInt(br.readLine());
        HashMap<Integer,PriorityQueue<Integer>> map = new HashMap<>();
        Map<Integer,Boolean> ifSuc = new LinkedHashMap<>();
        PriorityQueue<Integer> pID = new PriorityQueue<>();
        for(int i=0;i<length;i++){
            String str = br.readLine();
            int f = str.indexOf(" ");
            int s = str.indexOf(" ",f+1);
            int id = Integer.parseInt(str.substring(0,f));//ID
            int pro = Integer.parseInt(str.substring(f+1,s));//PROCESS
            String op = str.substring(s+1);//状态
            if(op.equals("success")){
                ifSuc.put(id,true);
                pID.offer(id);
                continue;
            }
            PriorityQueue<Integer> pq = map.getOrDefault(id,new PriorityQueue<Integer>());
            pq.offer(pro);
            map.put(id,pq);
        }
        if(pID.isEmpty()) {
            System.out.println(0);
            System.exit(0);
        }
        while(!pID.isEmpty()){
            int idS = pID.poll();
            PriorityQueue<Integer> pq = map.getOrDefault(idS,new PriorityQueue<Integer>());
            if(pq.isEmpty()) continue;
            while(!pq.isEmpty()){
                System.out.println(idS+" "+pq.poll()+" process");
            }
            System.out.println(idS+" "+100+" success");
        }

    }
    public boolean compare(int[] nums,int i,int j){
        String a = nums[i] +String.valueOf(nums[j]);
        String b = nums[j] +String.valueOf(nums[i]);
        return Integer.parseInt(a)>Integer.parseInt(b);
    }

}
 class TreeNode {
      int val;
     TreeNode left;
      TreeNode right;
      TreeNode() {}
      TreeNode(int val) { this.val = val; }
      TreeNode(int val, TreeNode left, TreeNode right) {
          this.val = val;
          this.left = left;
          this.right = right;
      }
}
