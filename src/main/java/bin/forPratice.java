package bin;

import java.io.IOException;
import java.util.ArrayList;


/**
 * @className: forPratice
 * @description: 测试用代码文件
 * @author: fxh
 * @date: 2021/10/03 22:48
 * @version: 1.0
 **/
public class forPratice {
    public static void main(String[] args) throws IOException {
        int a = 1534236410;
        int b = reverse( a );
        System.out.println( b );
    }
    public static int reverse(int x) {
        int y;
        int n=0;
        int reverse = 0;
        ArrayList<Object> num = new ArrayList<>();
        do{
            n++;
            y = (int) Math.pow(10,n);
            int remainder = (int) (x % y);
            remainder = (int) (remainder/(y*0.1));
            num.add(remainder);

        }while(( x / y ) != 0);

        for (int i = 0 ; i<n ; i++){
            int b = (int) Math.pow(10,n-i-1);
            int a = (int) num.get(i);
            reverse = reverse + a*b ;
        }

        return reverse;
    }

}
