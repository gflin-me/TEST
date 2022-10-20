package leetcode;

import java.io.IOException;
import java.util.Arrays;
import java.util.Scanner;

/**
 * @className: IOpratise
 * @description: TODO
 * @author: Lin Guifeng
 * @date: 2022/09/14 10:49
 * @version: 1.0
 **/
public class IOpratise {

    public static void main(String[] args) throws IOException {
        Scanner sc = new Scanner(System.in);
        while (sc.hasNext()){
            String s = sc.nextLine();
            String[] nums = s.split(" ");
            int[] res = new int[nums.length];
            for(int i=0;i<res.length;i++ ){
                res[i] = Integer.parseInt(nums[i]);
            }
            System.out.println(Arrays.toString(res));
        }
    }

}
