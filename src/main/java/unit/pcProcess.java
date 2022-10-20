package unit;

import Jama.Matrix;
import org.opencv.core.CvType;
import org.opencv.core.Mat;

import java.io.FileWriter;
import java.io.IOException;

/**
 * 点云处理相关函数库
 * 作者：Osmanthus
 * 时间：2021/10/08
 */
public class pcProcess {

    /**
     * 获取深度图(mat3D)中各点的坐标值,输出为二维数组double[3][点云数]
     * @param mat3D 深度图
     * @param rate 采样率
     * @return double[3][点云数] 坐标
     */
    public static double[][] coordinateArray(Mat mat3D , int rate ){
        double[][] coordinate = new double[3][mat3D.rows()*mat3D.cols()];
        int numPoint = 0;
        for (int i = 0 ; i < mat3D.cols() ; i = i + rate ){          //列
            for (int j = 0 ; j < mat3D.rows(); j=j+rate ){           //行
                double[] coordinateVal = mat3D.get(j,i);
                double x = coordinateVal[0];
                double y = coordinateVal[1];
                double z = coordinateVal[2];
                    if(z<16000){
                        coordinate[0][numPoint] = x;
                        coordinate[1][numPoint] = y;
                        coordinate[2][numPoint] = z;
                        numPoint++;
                    }
            }
        }
        System.out.println("点云数量：" + numPoint);
//        将过滤后的数组提取，去除多余部分
        double[][] coordinate1 = new double[3][ numPoint ];
        for (int k = 0 ; k < coordinate.length ; k++){
            System.arraycopy(coordinate[k],0, coordinate1[k], 0, numPoint);
        }

        return coordinate1 ;
    }

    /**
     * 点云直通滤波：滤除感兴趣区域外的点云数据
     * @param coordinate 点云坐标数据
     * @param range 范围数组 【 xMin , xMax , yMin , yMax , zMin , zMax 】
     * @return 滤波后的点云数据
     */
    public static double[][] coordinateFilter(double[][] coordinate , double[] range) {
        double[][] coordinate0 = new double[3][coordinate[1].length];
        int numPoint = 0;
        for (int i = 0; i < coordinate[1].length ; i++ ) {          //列
            double x = coordinate[0][i];
            double y = coordinate[1][i];
            double z = coordinate[2][i];
            if( z<range[5] && z>range[4] && x>range[0] && x <range[1] && y >range[2] && y<range[3]){
                coordinate0[0][numPoint] = x;
                coordinate0[1][numPoint] = y;
                coordinate0[2][numPoint] = z;
                numPoint++;
            }
        }
        double[][] coordinate1 = new double[3][ numPoint ];
        for (int k = 0 ; k < coordinate0.length ; k++) {
            System.arraycopy(coordinate0[k], 0, coordinate1[k], 0, numPoint);
        }
        return  coordinate1;
    }


    /**
     *获取深度图(mat3D)中各点的坐标值,去除范围(range)外的坐标，并输出为二维数组double[3][点云数]
     * @param mat3D 深度图
     * @return
     */
    public static double[][] coordinateArray(Mat mat3D ){
        return coordinateArray(mat3D, 1 );
    }

    public static void plotmat3D(Mat mat3D){
        for (int i = 0 ; i < mat3D.cols() ; i++ ){
            for (int j = 0 ; j < mat3D.rows(); j++ ){
                double[] coordinateVal = mat3D.get(j,i);
                double x = coordinateVal[0];
                double y = coordinateVal[1];
                double z = coordinateVal[2];
//                plot3DPer(x,y,z);
            }
        }
    }

    /**
     * 从图像文件名中提取欧拉角数值，以double[3]返回
     * @param fileName 图片文件名（后三个用“-”隔开的数值为欧拉角）
     * @return double[] 图像的欧拉角信息
     */
    public static double[] findEuler(String fileName){
        int indexEnd = fileName.indexOf(".png");
        int index3 = fileName.lastIndexOf("_" );//找到倒数第一个“-”的位置
        int index2 = fileName.lastIndexOf("_" ,index3-1 );
        int index1 = fileName.lastIndexOf("_" ,index2-1 );



        double euler1 = Double.parseDouble(fileName.substring(index1+1 , index2 ));
        double euler2 = Double.parseDouble(fileName.substring(index2+1 , index3 ));
        double euler3 = Double.parseDouble(fileName.substring(index3+1 , indexEnd ));
        return  new double[]  {euler1 , euler2 , euler3};
    }

    /**
     * 将欧拉角求旋转矩阵
     * @param euler 角度制的欧拉角（roll,pitch,yaw）
     * @return
     */
    public static Matrix Euler2Rotation(double[] euler){
        //角度制转弧度制
        double roll  = Math.toRadians(euler[0]);
        double pitch = Math.toRadians(euler[1]);
        double yaw   = Math.toRadians(euler[2]);
        //分别计算欧拉角的旋转矩阵
        double[][] Rx = { {         1           ,         0        ,         0           },
                          {         0           ,  Math.cos(roll)  , Math.sin(roll)*-1   },
                          {         0           ,  Math.sin(roll)  , Math.cos(roll)      }  };
        Matrix rotMx = new Matrix(Rx);
        double[][] Ry = { { Math.cos(pitch)     ,        0         ,     Math.sin(yaw)   },
                          {         0           ,        1         ,          0          },
                          { Math.sin(pitch)*-1  ,        0         ,    Math.cos(pitch)  }  };
        Matrix rotMy = new Matrix(Ry);
        double[][] Rz = { { Math.cos(yaw)       , Math.sin(yaw)*-1 ,          0          },
                          { Math.sin(yaw)       , Math.cos(yaw)    ,          0          },
                          {         0           ,         0        ,          1          }  };
        Matrix rotMz = new Matrix(Rz);
        //旋转矩阵R=Rz*Ry*Rx
        return rotMz.times(rotMy.times(rotMx));
    }
    /**
     * 将二维数组保存为libsvm能够识别的txt文件格式
     * [label] [index1]:[value1] [index2]:[value2] …
     * @param data  保存的特征值
     * @param label 特征值对应标签
     * @param savePath 文件名
     * @param append true从地址对应文件末尾写入文本
     */
    public static void saveAsTxt4SVM(double[][] data ,double label, String savePath ,boolean append){

        try {
            FileWriter output = new FileWriter(savePath , append );

            for (int i = 0 ; i < data.length ; i++ ){
                output.write((int)label+"\t");
                for (int j = 0 ; j < data[0].length ; j++ ) {
                    int index = j+1;
                    if(Double.isNaN(data[i][j]) || Double.isInfinite(data[i][j])) {
                        output.write(index + ":" + 0 + "\t");
                        System.out.println("NaN or Infinity in features data in line"+i+". ");
                    }else{
                        output.write(index + ":" + data[i][j] + "\t");
                    }
                }
                output.write("\n");
            }
            output.close();

        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    /**
     * 将二维数组保存为libsvm能够识别的txt文件格式
     * [label] [index1]:[value1] [index2]:[value2] …
     * @param data  保存的特征值
     * @param label 特征值对应标签
     * @param savePath 文件名
     * @param append true从地址对应文件末尾写入文本
     */
    public static void saveAsTxt4SVM_twoFile(double[][] data ,double label, String savePath ,boolean append){
        String savePath1 = savePath.replace(".txt","_train.txt");
        String savePath2 = savePath.replace(".txt","_test.txt");
        try {
            FileWriter output1 = new FileWriter( savePath1 , append );
            FileWriter output2 = new FileWriter( savePath2 , append );

            for (int i = 0 ; i < data.length ; i++ ){
                output1.write((int)label+"\t");
                for (int j = 0 ; j < data[0].length ; j++ ) {
                    int index = j+1;
                    if(Double.isNaN(data[i][j]) || Double.isInfinite(data[i][j])) {
                        output1.write(index + ":" + 0 + "\t");
                        System.out.println("NaN or Infinity in features data. ");
                    }else{
                        output1.write(index + ":" + data[i][j] + "\t");
                    }
                }
                output1.write("\n");
                if(i < data.length-1) i++;
                output2.write((int)label+"\t");
                for (int j = 0 ; j < data[0].length ; j++ ) {
                    int index = j+1;
                    if(Double.isNaN(data[i][j]) || Double.isInfinite(data[i][j])) {
                        output2.write(index + ":" + 0 + "\t");
                        System.out.println("NaN or Infinity in features data. ");
                    }else{
                        output2.write(index + ":" + data[i][j] + "\t");
                    }
                }
                output2.write("\n");
            }
            output1.close();
            output2.close();

        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    /**
     * 将二维数组保存为txt文件
     * @param data 保存的数据
     * @param savePath 文件名
     */
    public static void saveAsTxt(double[][] data , String savePath ){


        try {
            FileWriter out = new FileWriter(savePath);

            for (int i = 0 ; i < data.length ; i++ ){
                for (int j = 0 ; j < data[0].length ; j++ ) {
                    out.write( data[i][j] + "\t");
                }
                out.write("\n");
            }

            out.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    /**
     * 输入双目匹配获取的mat3D,进行点云数据获取、矫正和投影获得二值图
     * @param mat3D 点云图
     * @param Euler 欧拉角
     * @param rate  采样率
     * @param range 投影范围
     * @return binaryPic 二值图
     */
    public static Mat PointCloud2Binary( Mat mat3D , double[] Euler, int rate , double[] range){
        //点云数据数据获取，降采样
        double[][] coordinate = pcProcess.coordinateArray( mat3D , rate );

        //点云从相机坐标系转至IMU坐标系
        Matrix pcInCameraCoor = new Matrix(coordinate);
        Matrix R1 = pcProcess.Euler2Rotation(new double[]{ 90 , 0 , 0 });
        Matrix pcInIMUCoor = R1.transpose().times(pcInCameraCoor);
        //点云从IMU坐标系转至世界坐标系
        Euler[2]=0;
        Matrix R2 = pcProcess.Euler2Rotation(Euler);
        Matrix pcInWorldCoor = R2.times(pcInIMUCoor);
        double[][] coorInWorld = pcInWorldCoor.getArray();
        //去除范围外点云
        coorInWorld = pcProcess.coordinateFilter(coorInWorld,range);

        //投影为二值图
        int height_binary = (int) Math.floor( ( range[5]-range[4] ) *0.1 );
        int width_binary  = (int) Math.floor( ( range[3]-range[2] ) *0.1 );
        Mat binaryPic = Mat.zeros( height_binary , width_binary , CvType.CV_8UC1 );
        for( int i=0 ; i<coorInWorld[0].length ; i++ ){
            int rowsPixel = height_binary - (int)Math.floor((coorInWorld[2][i] - range[4])*0.1);
            int colsPixel = (int)Math.floor((coorInWorld[1][i] - range[2])*0.1);
            if ( rowsPixel < height_binary && colsPixel < width_binary) {
                binaryPic.put(rowsPixel,colsPixel,255) ;
            }
        }
        return binaryPic;
    }


}
