import org.opencv.core.*;
import org.opencv.imgproc.Imgproc;
import unit.pcProcess;

import java.io.File;
import java.io.IOException;

import static java.lang.Math.pow;
import static org.opencv.imgcodecs.Imgcodecs.*;
import static org.opencv.imgproc.Imgproc.DIST_L2;
import static unit.featureExtractionFunction.findRect;

/**
 * @className: pratice_all
 * @description: TODO
 * @author: fxh
 * @date: 2021/10/15 21:24
 * @version: 1.0
 **/
public class pratice_all {
    static {
        // 动态链接opencv
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
    }

    public static void main(String[] args) throws IOException {
        String[] terrainName = {"LG", "OB", "US", "DS", "UR", "DR"};
        String folderName = "binaryPic_all_original";
        String folderNameNew = "binaryPic_Stair";
        String defaultPath = "F:\\stereoCamera\\PCread\\picwithIMU\\"+folderName+"\\default\\";
        for (int indexTerrain = 0; indexTerrain < terrainName.length; indexTerrain++) {
            //二值图地址
            String terrain = terrainName[indexTerrain];
            String binaryPath = defaultPath.replace("default", terrain);
            File file = new File(binaryPath);//获取文件夹对象
            File[] fileList = file.listFiles();    //获取文件夹下的文件列表，包括文件和子文件夹

            int numFile = fileList.length;                       //文件夹中图片的数量
            int numFeatures = 1 ;                                //特征数量
            double[][] features = new double[numFile][numFeatures];
            //循环读取二值图像
            int indexPic = 0;//图片序号索引
            for (File binaryPicFile : fileList) {
                //读取图像
                String binaryPicPath = binaryPicFile.getAbsolutePath();   //获取路径
                System.out.println("读取文件：" + binaryPicFile);
                Mat binaryPic = imread(binaryPicPath, IMREAD_GRAYSCALE);//读取二值图像
                //======================================【执行区】========================================================
//                binaryMorphologyEx( binaryPic, 0 , 13 , 21 );
//                features[indexPic][0] = findStairs( binaryPic, 27 , 13 );
//                indexPic++;
                Rect rect = findRect(binaryPic);
                double width_rect = rect.width ;
                double height_rect = rect.height;
                Point tl = rect.tl();
                int widthField = 10;
                int numField = (int)Math.floor(width_rect/widthField);
                int[][] fieldPosition = new int[numField][4];
                for( int i=0; i<numField ; i++ ) {
                    fieldPosition[i][0] = (int) (tl.x + widthField * i);
                    fieldPosition[i][1] = (int) tl.y;
                    fieldPosition[i][2] = (int) (tl.x + widthField * (i + 1));
                    fieldPosition[i][3] = (int) (tl.y + height_rect);

                    for (int k = 0; k < binaryPic.rows(); k++) {
                        for (int j = 0; j < binaryPic.cols(); j++) {
                            double[] sss = binaryPic.get( k , j );
                            double ppp = sss[0];
                            if (ppp == 255) {


                            }
                        }
                    }
                }




                //====================================【图像保存】========================================================
                String binaryPicMorphologyPath = binaryPicPath.replace(folderName, folderNameNew );
                imwrite( binaryPicMorphologyPath , binaryPic );
            }
            String saveName = folderNameNew+"\\"+terrain+".txt";
            String savePath = defaultPath.replace(folderName+"\\default\\", saveName );
            pcProcess.saveAsTxt( features , savePath );
        }

    }

    private static void harrisCornerDemo(Mat binaryPic, Mat dst){
        Mat cornerMat = new Mat();

        Imgproc.cornerHarris(binaryPic,cornerMat,2,3,0.04);
        Core.normalize(cornerMat,cornerMat,0,255,Core.NORM_MINMAX , CvType.CV_32F);

        dst.create(binaryPic.size(),binaryPic.type());
        binaryPic.copyTo(dst);
        float[] data = new float[1];
        for(int j=0;j<cornerMat.rows();j++){
            for(int i=0;i<cornerMat.cols();i++){
                cornerMat.get(j,i,data);
                if((int)data[0]>100){
                    Imgproc.circle(dst,new Point(i,j),5,new Scalar(255,0,0),1,8,0);

                }
            }
        }

    }

    private static double shiTomasCornerDemo(Mat binaryPic , Mat dst){
        //角点检测
        int numCoener = 0;
        MatOfPoint corners = new MatOfPoint();
        Imgproc.goodFeaturesToTrack(binaryPic,corners,100,0.01,22,new Mat(),3,false,0.04);
        //绘制角点
        dst.create(binaryPic.size(),binaryPic.type());
        binaryPic.copyTo(dst);
        Point[] points = corners.toArray();//角点点集
        for(int i=0;i<points.length;i++){
            Imgproc.circle( dst , points[i] ,1,new Scalar(255,255,255),5);
            numCoener++;
        }
        //角点直线拟合
        MatOfPoint pointMat = new MatOfPoint();
        pointMat.fromArray(points);
        Mat line = new Mat();
        Imgproc.fitLine(pointMat, line, DIST_L2 , 0 ,0.01,0.01);//拟合直线
        //直线参数获取
        float[] lineParams = new float[4];
        for(int i=0;i<4;i++){
            float[] a = new float[1];
            line.get(i,0,a);
            lineParams[i] = a[0];
        }
        //直线绘制
        double kline = lineParams[1]/lineParams[0];
        Point lineP1 = new Point(0, kline*(0-lineParams[2])+lineParams[3]);
        Point lineP2 = new Point(dst.width()-1 , kline*(dst.width()-1-lineParams[2])+lineParams[3]);
        Imgproc.line(dst,lineP1,lineP2,new Scalar(255,255,255));
        //计算距离
        double[] distance = new double[points.length];
        for( int j=0; j<points.length;j++){
            distance[j] = Distance(lineP1,lineP2,points[j]);
        }
//        double distanceAverag = 0;
//        for (double value : distance) {
//            distanceAverag += value;
//        }
//        distanceAverag=distanceAverag/points.length;
        double dis = 0;
        int numW = 0;
        for(int i=0 ; i<binaryPic.rows() ; i++ ){
            for(int j=0 ; j<binaryPic.cols(); j++ ){
                double[] sss = binaryPic.get(i,j);
                double ppp = sss[0];
                if (ppp == 255){
                    Point point = new Point(i,j);
                    dis = dis + Distance(lineP1,lineP2,point );
                    numW++;
                }

            }
        }
        double distanceAverag = dis/numW;
        System.out.println(corners.rows()+"个角点"+numCoener+"角点偏移距离平均值"+distanceAverag);

        return distanceAverag;

    }

    /**
     * 计算p0点到经过p1、p2点的直线的距离
     * @param p1 经过的点1
     * @param p2 经过的点2
     * @param p0 直线外的点
     * @return distance 距离
     */
    public static double Distance(Point p1,Point p2,Point p0){
        double x1 = p1.x;   double y1 = p1.y;
        double x2 = p2.x;   double y2 = p2.y;
        double x0 = p0.x;   double y0 = p0.y;
        return (Math.abs((y2 - y1) * x0 +(x1 - x2) * y0 + ((x2 * y1) -(x1 * y2))))/ (Math.sqrt(pow(y2 - y1, 2) + pow(x1 - x2, 2)));
    }

}
