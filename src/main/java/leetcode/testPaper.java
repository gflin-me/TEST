package leetcode;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * @className: testPaper
 * @description: TODO
 * @author: Lin Guifeng
 * @date: 2022/08/03 1:07
 * @version: 1.0
 **/
public class testPaper {
    //新人考试

//    有10道2分题，10道4分题，5道8分题，满分100分，答对得分，答错不得分，累计错三道则停止作答结算分数
//
//    输入:最终作答者获得分数 n
//
//    输出:作答可能的情况总数 m

    public static void main(String[] args) throws IOException {
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        int N = Integer.parseInt(br.readLine());

        int[] grade = new int[]{2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 8, 8, 8, 8, 8};

        int result=dfs(0,N,3,grade);
        int ans = take(grade,N,3,0);
        System.out.println(result+"  "+ans);
    }

    public static int take(int[] grade,int mark,int times,int index){
        if(times==0) return mark==0?1:0;
        if(mark==0) return 1;
        if(index==grade.length) return 0;
        return take(grade,mark-grade[index],times,index+1)+take(grade,mark,times-1,index+1);
    }

    /**
     *
     * @param index 错题索引
     * @param rest 剩下的分数
     * @param count 错题数量
     * @param grade 题目表
     * @return
     */
    public static int dfs(int index, int rest, int count, int[] grade) {
        //如果错题0道 且刚好分数凑够 （rest剩下要凑的分数为0） 则说明只有一种情况 满分
        //若rest不是0，说明不存在这种情况 return 0即可
        if (count == 0) {
            return rest == 0 ? 1 : 0;
        }
        //若是分数凑够，rest=0，则只有一种情况 0分
        if (rest == 0) {
            return 1;
        }
        //防止数组越界
        if (index >= grade.length) {
            return 0;
        }
        //当前这道题做错的情况+做对的情况
        return dfs(index + 1, rest, count - 1, grade) + dfs(index + 1, rest - grade[index], count, grade);
        //index：无论错对index都得后移，做对得分，做错不得分
        //rest：刚开始是N，我们现在进行凑分数，要是做错这道题不得分所以要凑的分数还是rest
        //做对的话，就把获得的分数减去，得到重新要凑的分数rest
        //count：我们刚开始假设做错了三道题，要是这道题做对则错题数减一；做错不变
    }


}
