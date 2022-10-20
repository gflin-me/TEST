package leetcode;

import java.util.*;


public class NIO {

    public static void main(String[] args) {
        Scanner sc=  new Scanner(System.in);
        int m = sc.nextInt();
        Map<Integer,Long> map = new HashMap<>();
        List<int[]> list = new ArrayList<>();
        int index=0;
        while(sc.hasNext()){
            list.add(new int[]{sc.nextInt(),index++});
        }
        int[][] nums = list.toArray(new int[list.size()][2]);
        Arrays.sort(nums, new Comparator<int[]>() {
            @Override
            public int compare(int[] o1, int[] o2) {
                return o1[0]-o2[0];
            }
        });

        for(int k=0;k<nums.length;k++){
            int num =nums[k][0];
            long sum=0;
            int i = num;
            for(;num>=1;num--){
                if(map.containsKey(num)){
                    sum+=map.get(num);
                    break;
                }
                int b = num&(num-1);
                int a = num-b;
                sum+=num/a;
            }
            map.put(i,sum);
        }
        Arrays.sort(nums, new Comparator<int[]>() {
            @Override
            public int compare(int[] o1, int[] o2) {
                return o1[1]-o2[1];
            }
        });
        for(int k=0;k<nums.length;k++){
            System.out.println(map.get(nums[k][0]));
        }
    }
}
