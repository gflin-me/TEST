package leetcode;

import java.io.IOException;
import java.util.*;

/**
 * @className: KMP
 * @description: TODO
 * @author: Lin Guifeng
 * @date: 2022/09/08 21:15
 * @version: 1.0
 **/
public class KMP {
    public static void main(String[] args) throws IOException {
        int ans = strStr("ababab","abab");
        String s = "a";
        System.out.println(Arrays.toString(maxSlidingWindow(new int[]{1, 3, 1, 2, 0, 5}, 3)));
    }
    public static int strStr(String haystack, String needle) {
        int[] next = getNext(needle);
        int j=0;
        for(int i=0;i<haystack.length();i++){
            while(j>0 && needle.charAt(j)!=haystack.charAt(i)){
                j=next[j-1];
            }
            if(needle.charAt(j)==haystack.charAt(i)) j++;
            if(j==needle.length()) return i-needle.length()+1;
        }
        return -1;
    }
    public static int[] getNext(String str){
        int n = str.length();
        int[] next = new int[n];
        //i指向【后缀的末尾】位置、j指向【前缀的末尾】位置
        int j=0;
        for(int i=1;i<n;i++){
            while( j>0 && str.charAt(i)!=str.charAt(j) ){
                j=next[j-1];
            }
            if(str.charAt(i)==str.charAt(j)) j++;

            next[i]=j;
        }
        return next;
    }
    public static int[] maxSlidingWindow(int[] nums, int k) {
        int n=nums.length;
        Deque<Integer> dq  = new ArrayDeque<>();
        for(int i=0;i<k;i++){
            while(!dq.isEmpty()&&nums[dq.peekLast()]<=nums[i]){
                dq.pollLast();
            }
            dq.addLast(i);
        }
        int[] ans = new int[n-k+1];
        ans[0] = nums[dq.peekFirst()];
        for(int i=k;i<n;i++){
            while(!dq.isEmpty()&&nums[dq.peekLast()]<=nums[i]){
                dq.pollLast();
            }
            dq.addLast(i);
            while(dq.peekFirst()<=i-k){
                dq.pollFirst();
            }
            ans[i-k+1]=nums[dq.peekFirst()];
        }
        return ans;
    }
    public int[] topKFrequent(int[] nums, int k) {
        int[] ans = new int[k];
        Map<Integer,Integer> map = new HashMap<>();
        for(int i=0;i<nums.length;i++){
            map.put(nums[i],map.getOrDefault(nums[i],0)+1);
        }
        PriorityQueue<Map.Entry<Integer, Integer>> pq = new PriorityQueue<>(new Comparator<Map.Entry<Integer, Integer>>() {
            @Override
            public int compare(Map.Entry<Integer, Integer> o1, Map.Entry<Integer, Integer> o2) {
                return o1.getValue()-o2.getValue();
            }
        });
        for(Map.Entry<Integer,Integer> a:map.entrySet()){
            pq.add(a);
        }
        for(int i=0;i<k;i++){
            ans[i] = pq.poll().getValue();
        }
        return ans;
    }
}
