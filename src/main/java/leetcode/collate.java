package leetcode;

import java.lang.reflect.Array;
import java.util.*;

/**
 * @className: collate
 * @description: 常见排序方法的代码
 * @author: Lin Guifeng
 * @date: 2022/08/01 16:56
 * @version: 1.0
 **/
public class collate {
    public static void main(String[] args){
        int[] t1 = new int[]{1,6,9,2,5,0,3,4,7,8,16,13,11,10,15,12,14};
        int[] t2 = new int[]{1,2,2};
//        System.out.println(Arrays.toString(mergeSort(t1,0,t1.length-1)));
        System.out.println(Arrays.toString(heapSort(t2)));
        Vector<Integer> v = new Vector<>();
        HashMap<Integer,Integer> map = new HashMap<>();
    }






    /**
     * 基排序法
     * @param nums 输入数组
     * @param maxFigures 数组元素最大位数
     * @return 输出数组
     */
    public static int[] radixSort(int[] nums, int maxFigures){
        int k = 0;
        int n = 1;
        int m = 1; //控制键值排序依据在哪一位
        int[][] temp = new int[10][nums.length]; //数组的第一维表示可能的余数0-9
        int[] order = new int[10]; //指针数组：用于循环中存放该数位上数字为i的数字有多少。
        while(m <= maxFigures){
            //根据【当前数位】上的值，将数组元素存入对应的‘桶’中
            for (int num : nums) {
                int lsd = ((num / n) % 10);
                temp[lsd][order[lsd]] = num;
                order[lsd]++;
            }
            //根据数位上的数字的顺序将‘桶’中的元素存入数组
            for(int i = 0; i < 10; i++){
                if(order[i] != 0) {
                    for (int j = 0; j < order[i]; j++) {
                        nums[k] = temp[i][j];
                        k++;
                    }
                }
                order[i] = 0;//顺便恢复指针
            }
            k = 0;//恢复指针
            m++; n *= 10;//进位
        }

        return nums;
    }

    /**
     * 直接插入排序
     * @param nums 输入数组
     * @return 输出数组
     */
    public static int[] insertSort(int[] nums){
        for(int i=1;i<nums.length;i++){ //第0位独自作为有序数列，从第1位开始向后遍历
            if(nums[i]<nums[i-1]){      //0~i-1位为有序，若第i位小于i-1位，继续寻位并插入，否则认为0~i位也是有序的，忽略此次循环，相当于continue
                int temp=nums[i];       //保存第i位的值
                //【j】从i-1的位置开始向前轮询，当【j】处的值大于【nums[i]】，【j】处的值向后“挪”一位
                //直到【j】处的值小于【nums[i]】
                int j = i - 1;
                while(j>=0 && temp<nums[j]){
                    nums[j+1]=nums[j];
                    j--;
                }
                //【nums[i]】的值存入j+1处
                nums[j+1]=temp;
            }
        }
        return nums;
    }

    /**
     * 折半插入排序：直接插入排序的优化版，其实就是用二分法在已排序部分找到插入位置
     * @param nums 输入数组
     * @return 输出数组
     */
    private static int[] binaryInsertSort(int[] nums){

        for(int i = 1; i < nums.length; i++){

            int temp = nums[i];
            int low = 0;
            int high = i - 1;
            //二分法找到插入temp的位置
            while(low <= high){
                int mid = (low + high) / 2;
                if(temp < nums[mid]){
                    high = mid - 1;
                }else{
                    low = mid + 1;
                }
            }
            //将该位置后的数逐个向后“挪”一位
            for(int j = i; j >= low + 1; j--){
                nums[j] = nums[j - 1];
            }
            //插入temp
            nums[low] = temp;
        }
        return nums;
    }
    //希尔排序法
    public static int[] shellSort(int[] nums){
        int length = nums.length;
        while (true) {
            //增量每次减半
            length /= 2;
            for (int i = 0; i < length; i++) {
                for (int j = i + length; j < nums.length; j += length) {
                    //对分组内的元素进行插入排序
                    int k = j - length;
                    while (k >= 0 && nums[k] > nums[k+length]) {
                        swap( nums , k , k+length );
                        k -= length;
                    }
                }
            }
            if (length == 1)
                break;
        }
        return nums;
    }
    //归并排序

    /**
     * 归并排序
     * @param nums 输入数组
     * @param l 左指针
     * @param h 右指针
     * @return 输出数组
     */
    public static int[] mergeSort(int[] nums, int l, int h) {

        if (l == h) return new int[]{nums[l]};
        //分治数组
        int mid = l + (h - l) / 2;
        int[] leftArr = mergeSort(nums, l, mid); //左有序数组
        int[] rightArr = mergeSort(nums, mid + 1, h); //右有序数组
        int[] newNum = new int[leftArr.length + rightArr.length]; //新有序数组
        //比较两个数组的数字，每次将小的放入输出数组中
        int m = 0, i = 0, j = 0;
        while (i < leftArr.length && j < rightArr.length) {
            newNum[m++] = leftArr[i] <= rightArr[j] ? leftArr[i++] : rightArr[j++];//【注意=号】当左数组和右数组轮询的元素相同时，优先存入左数组元素，保证了稳定性
        }
        //若左（右）数组还有剩余，直接放入新数组，因为左右数组本身是有序的
        while (i < leftArr.length) newNum[m++] = leftArr[i++];
        while (j < rightArr.length) newNum[m++] = rightArr[j++];

        return newNum;
    }

    /**
     * 冒泡排序法
     * @param nums 输入数组
     * @return 输出数组
     */
    public static int[] bubbleSort(int[] nums){
        int n = nums.length;
        for(int i=0;i<n-1;i++){
            //从0开始轮询数组，每次轮询会将最大值排到最后，所以下一次轮询需要缩小范围（最大下标-i）
            for(int j=0;j<n-i-1;j++){
                if(nums[j]>nums[j+1]){
                    swap(nums, j, j+1);
                }
            }
        }
        return nums;
    }

    //快速排序法
    public static int[] quickSort(int[] nums){
        quickSort(nums,0,nums.length-1);
        return nums;
    }
    public static void quickSort(int[] nums,int left,int right){
        if(left>=right) return;

        int r=right,l=left;
        while(l<r){
            while( l<r && nums[r]>=nums[left] ) r--;
            while( l<r && nums[l]<=nums[left] ) l++;

            swap(nums, l, r);
        }
        swap(nums, left, l);

        quickSort(nums, left,l-1);
        quickSort(nums,l+1,right);
    }

    /**
     * 选择排序
     * @param nums 输入数组
     * @return 输出数组
     */
    public int[] selectSort(int[] nums) {
        // 总共要经过 N-1 轮比较
        for (int i = 0; i < nums.length - 1; i++) {
            int min = i;
            // 每轮需要比较的次数 N-i
            for (int j = i + 1; j < nums.length; j++) {
                if (nums[j] < nums[min]) {
                    // 记录目前能找到的最小值元素的下标
                    min = j;
                }
            }
            // 将找到的最小值和i位置所在的值进行交换
            if (i != min)  swap(nums,i,min);
        }
        return nums;
    }

    /**
     * 堆排序
     * @param nums 待排序数组
     * @return 已排序数组
     */
    public static int[] heapSort(int[] nums) {
        if (nums == null || nums.length == 0) {
            return nums;
        }
        int length = nums.length;
        // 构建大顶堆:把待排序序列，变成一个大顶堆结构的数组
        // 从最后一个非叶节点开始向前遍历，调整节点性质，使之成为大顶堆
        for (int i = length / 2 - 1; i >= 0; i--) {
            heapify(nums, i, length);
        }

        // 交换堆顶和当前末尾的节点，重置大顶堆
        //上一步骤将当前数组调整为【大顶堆】结构，最大值在索引0处，将索引0处的值放到数组末尾，然后再次根据【大顶堆】对数组进行调整
        for (int i = length - 1; i > 0; i--) {
            swap(nums, 0, i);
            length--;
            heapify(nums, 0, length);
        }
        return nums;
    }

    /**
     * 堆排序子函数：【堆调整】对节点i和他的左右子节点比较，将较大值交换至i节点
     * @param nums 输入数组
     * @param i 被调整节点
     * @param length 范围
     */
    private static void heapify(int[] nums, int i,int length) {
        // 先根据堆性质，找出它左右节点的索引
        int left = 2 * i + 1;
        int right = 2 * i + 2;
        // 默认当前节点（父节点）是最大值。
        int largestIndex = i;
        // 如果有左节点，并且左节点的值更大，更新最大值的索引
        if (left < length && nums[left] > nums[largestIndex]) {
            largestIndex = left;
        }
        // 如果有右节点，并且右节点的值更大，更新最大值的索引
        if (right < length && nums[right] > nums[largestIndex]) {
            largestIndex = right;
        }
        //如果最大值不是当前非叶子节点的值，那么就把当前节点和最大值的子节点值互换
        if (largestIndex != i) {
            swap(nums, i, largestIndex);
            // 因为互换之后，子节点的值变了，如果该子节点也有自己的子节点，仍需要再次调整。
            heapify(nums, largestIndex, length);
        }
    }

    /**
     * 交换数组元素
     * @param nums
     * @param i 元素的下标
     * @param j 元素的下标
     */
    public static void swap(int[] nums, int i, int j) {
        // 交换 nums[i] 和 nums[j]
        int tmp = nums[i];
        nums[i] = nums[j];
        nums[j] = tmp;
    }

}
