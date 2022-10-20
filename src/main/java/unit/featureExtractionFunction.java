package unit;

import org.opencv.core.*;
import org.opencv.imgproc.Imgproc;
import org.opencv.imgproc.Moments;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;

import static java.lang.Math.pow;
import static org.opencv.imgproc.Imgproc.DIST_L2;
import static org.opencv.imgproc.Imgproc.drawContours;


/**
 * @className: featureExtraction
 * @description: 二值图提取一维特征提取相关函数
 * @author: Osmanthus
 * @date: 2021/10/15 21:11
 * @version: 1.0
 **/
public class featureExtractionFunction {
    /**
     * 图像形态学处理函数，对二值图进行，三个数字取小于0的值时不进行操作
     * @param binaryPic 输入二值图
     * @param elementOpenSize 开操作卷积核大小
     * @param elementCloseSize 闭操作卷积核大小
     * @param minArea 最小封闭区域
     * @return 处理后的二值图
     */
    public static Mat binaryMorphologyEx(Mat binaryPic,int elementOpenSize,int elementCloseSize , int minArea){
        //形态学处理
        // 开操作：去除小亮点
        if( elementOpenSize > 0 ) {
            Mat elementOpen = Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new Size(elementOpenSize, elementOpenSize), new Point(-1, -1));//3
            Imgproc.morphologyEx(binaryPic, binaryPic, Imgproc.MORPH_OPEN, elementOpen);
        }
        //闭操作：去除小黑点
        if( elementCloseSize > 0 ) {
            Mat elementClose = Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new Size(elementCloseSize, elementCloseSize), new Point(-1, -1));//6
            Imgproc.morphologyEx(binaryPic, binaryPic, Imgproc.MORPH_CLOSE, elementClose);
        }
        //填充面积低于阈值的封闭区域
        if(minArea>0) {
            fillContours(binaryPic, minArea);
        }

        return binaryPic;
    }

    public static double[] featuresExtraction(Mat binaryPic){

        double widthPic = binaryPic.width();
        double heightPic = binaryPic.height();
        //最小外接矩形
        RotatedRect minRect = findminRect(binaryPic);
        double width_minRect = minRect.size.width/widthPic;     //最小外接矩形宽
        double height_minRect = minRect.size.height/heightPic;//最小外接矩形高
                    double angle_minRect = Math.abs(minRect.angle-45)/45;//最小外接矩形倾斜角
                    double yCenter_minRect = minRect.center.y/heightPic;//最小外接矩形中心点y坐标
        double areaMinRect = width_minRect * height_minRect ;//最小外接矩形面积

        //外接正矩形
        Rect rect = findRect(binaryPic);
        double ytl_rect = rect.y/heightPic;         //外接正矩形左上角点坐标
                    double width_rect = rect.width/widthPic;    //外接正矩形宽度
                    double height_rect = rect.height/heightPic; //外接正矩形高度
        //最小外接矩形内像素填充度
        Mat roiAll = new Mat( binaryPic , rect );
        double numPixelAll = Core.countNonZero(roiAll);
        double fullness = areaMinRect/numPixelAll;
        //图像质心
        Point centroid = findCentroid(binaryPic);
        double yCentroid = (int) centroid.y/heightPic;
        //区域密度
        double[] pixelsDist = pixelsDistribution(binaryPic, rect, centroid);

        //离散度特征
        double[] dispersion = Dispersion(binaryPic);
        //水平线提取
        double numvertical = VerticalLine( binaryPic );
        double numhorizontal = HorizontalLine( binaryPic );
        //特征写入
        //注意matlab程序得出的序号是从大到小排列的，所以特征向量也要按这个序号排列
//        double[]  feaRect = {numvertical, dispersion[1] , yCentroid , ytl_rect ,  height_minRect, width_minRect};
//        double[] featurePer = AddArray2( feaRect , pixelsDist );
//        double[] feaRect = {width_minRect, height_minRect, ytl_rect , yCentroid, dispersion[0] ,dispersion[1] , fullness ,numvertical,numhorizontal};

        double[] featureList = new double[]{pixelsDist[0],pixelsDist[1],pixelsDist[2],pixelsDist[3],pixelsDist[4],pixelsDist[5],pixelsDist[6],
                width_minRect, height_minRect,angle_minRect,yCenter_minRect, ytl_rect,
                width_rect,height_rect, yCentroid, dispersion[0],dispersion[1],fullness,
                numvertical, numhorizontal};
        return scale(featureList,0,1,-1,1);
    }


    /**
     * 数据缩放
     * @param input
     * @param lower
     * @param upper
     * @param lowerNew
     * @param upperNew
     * @return
     */
    public static double[] scale(double[] input , double lower , double upper , double lowerNew, double upperNew ){
        double scale = (upperNew-lowerNew)/(upper-lower);
        double[] output = new double[input.length];
        for(int i=0;i<input.length;i++){
            output[i] = (input[i]-lower)*scale+lowerNew;
        }
        return output;
    }

    /**
     * 寻找二值图质心
     * @param binaryPic 二值图
     * @return Point 质心点
     */
    public static Point findCentroid(Mat binaryPic ){
        //求矩
        Moments m = Imgproc.moments(binaryPic);
        int x = (int)Math.round(m.m10 / m.m00 ) ;
        int y = (int)Math.round(m.m01 / m.m00 ) ;
        //返回质心坐标
        return new Point(x,y);
    }

    /**
     * 寻找图像外接正矩形
     * @param binaryPic 二值图
     * @return Rect 外接正矩形
     */
    public static Rect findRect(Mat binaryPic){

        List<MatOfPoint> contours = new ArrayList<MatOfPoint>();//轮廓列表
        Mat hierarchy = new Mat();//轮廓图
        //轮廓识别
        Imgproc.findContours(binaryPic, contours, hierarchy, Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_SIMPLE, new Point(0, 0));

        int numRect = 0;                  //外接矩阵数量
        int numContours = contours.size();//轮廓数量
        //特征变量
        int xtl = binaryPic.width()-1; //外接矩形左上点x坐标
        int ytl = binaryPic.height()-1;//外接矩形左上点y坐标
        int xbr = 0;                   //外接矩形右下点x坐标
        int ybr = 0;                   //外接矩形右下点y坐标
        int height = 0;                //外接矩形高度

        //绘制外接矩形框
        for (MatOfPoint contour : contours) {
            double area = Imgproc.contourArea(contour);//获取轮廓的面积
            //如果面积大于33像素
            if (area > 50) {
                //绘制轮廓外接矩形
                Rect rect = Imgproc.boundingRect(contour);
//                Imgproc.rectangle(binaryPic, rect.tl(), rect.br(), new Scalar(255, 255, 255), 2, 0);
                numRect++;
                //获取封闭面积的外接矩形的左上角点和右下角点的坐标，比较多个矩形的坐标，最后得到最左上角和最右下角的坐标值
                Point tl = rect.tl();//左上角点
                Point br = rect.br();//右下角点
                int x1 = (int) tl.x;int y1 = (int) tl.y;
                int x2 = (int) br.x;int y2 = (int) br.y;

                if (x1 < xtl) {
                    xtl = x1;
                }
                if (y1 < ytl) {
                    ytl = y1;
                }
                if (x2 > xbr) {
                    xbr = x2;
                }
                if (y2 > ybr) {
                    ybr = y2;
                }

            }
        }
        //如果绘制的矩形多余一个，再次绘制一个把所有矩形囊括的矩形
//        if(numRect>1) {
//            Imgproc.rectangle(binaryPic, new Point(xtl, ytl), new Point(xbr, ybr), new Scalar(255, 255, 255), 2, 0);
//        }
        Point tlMax = new Point(xtl,ytl);
        Point brMax = new Point(xbr,ybr);
        //返回 外接正矩形
        return new Rect(tlMax,brMax);

    }

    /**
     * 识别二值图中轮廓，填充面积小于阈值的轮廓
     * @param binaryPic 二值图
     * @param minArea 封闭区域面积阈值
     */
    public static void fillContours(Mat binaryPic, int minArea){
        List<MatOfPoint> contours = new ArrayList<>();//轮廓列表
        Mat hierarchy = new Mat();//轮廓图
        //轮廓识别
        Imgproc.findContours(binaryPic, contours, hierarchy, Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_SIMPLE, new Point(0, 0));

        int numContours = contours.size();//轮廓数量
        for (int i = 0; i < numContours; i++) {
            double area = Imgproc.contourArea(contours.get(i));//获取轮廓的面积
            //如果面积大于33像素
            if ( area < minArea ) {
                //将小闭合轮廓填充
                drawContours(binaryPic, contours, i, new Scalar(0, 0, 0), -1, 8);

            }
        }
    }

    /**
     * 合并两个一维数组，输出结果需转换
     * @param A 数组1
     * @param B 数组2
     * @return 合并后数组
     */
    public static Object[] addArrays(Object[] A, Object[] B) {
        if (A == null) {
            return clone(B);
        } else if (B == null) {
            return clone(A);
        }
        Object[] joinedArray = (Object[]) Array.newInstance(A.getClass().getComponentType(), A.length + B.length);
        System.arraycopy(A, 0, joinedArray, 0, A.length);
        try {
            System.arraycopy(B, 0, joinedArray, A.length, B.length);
        } catch (ArrayStoreException ase) {
            //需要保证合并的对象类型相同
            final Class<?> type1 = A.getClass().getComponentType();
            final Class<?> type2 = B.getClass().getComponentType();
            if (!type1.isAssignableFrom(type2)) {
                throw new IllegalArgumentException("Cannot store " + type2.getName() + " in an array of " + type1.getName());
            }
            throw ase;
        }
        return joinedArray;
    }
    public static Object[] clone(Object[] array) {
        if (array == null) {
            return null;
        }
        return (Object[]) array.clone();
    }

    /**
     * 历遍从二值图中识别的轮廓，并将所有轮廓合并为一个点集Point[]
     * @param contours 轮廓List
     * @return Point[] 轮廓集
     */
    public static Point[] addContours(List<MatOfPoint> contours){
        int numContours = contours.size();//轮廓数量
        Point[] pointsAll = new Point[0] ;
        if(numContours>1) {
            for (int i = 0; i < numContours; i++) {
                Point[] p1 = contours.get(i).toArray();
                pointsAll = (Point[]) addArrays(pointsAll, p1);
            }
        }else{
            pointsAll = contours.get(0).toArray();
        }
        return pointsAll;
    }

    /**
     * 获取二值图图像最小外接矩形
     * @param binaryPic 二值图
     * @return RotatedRect 最小外接矩形
     */
    public static RotatedRect findminRect(Mat binaryPic){
        List<MatOfPoint> contours = new ArrayList<MatOfPoint>();//轮廓列表
        Mat hierarchy = new Mat();//轮廓图
        Imgproc.findContours(binaryPic, contours, hierarchy, Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_SIMPLE, new Point(0, 0));
        Point[] contoursAll = addContours(contours);
        return Imgproc.minAreaRect(new MatOfPoint2f(contoursAll));
    }

    /**
     * 在图像上绘制斜矩形
     * @param binaryPic 绘制的图像
     * @param rotatedRect 斜矩形
     */
    public static void drawRotatedRect(Mat binaryPic,RotatedRect rotatedRect){
        Point[] vertices = new Point[4];
        rotatedRect.points(vertices);
        for(int j = 0;j<3;j++) {
            Imgproc.line(binaryPic, vertices[j],vertices[j+1] , new Scalar(255,255,255),2,8,0 );
        }
        Imgproc.line(binaryPic, vertices[0],vertices[3] , new Scalar(255,255,255),2,8,0 );
    }

    /**
     * 将外接矩形区域内以质心为中心划分为4个区域，计算四个区域的像素密度，归一化后输出
     * @param binaryPic 二值图
     * @param rect      外接正矩形
     * @param centroid  质心点
     * @return  int[]   四个区域的归一化像素密度
     */
    public static double[] pixelsDistribution (Mat binaryPic, Rect rect , Point centroid ){
        Point tl = rect.tl();
        Point br = rect.br();
        Point tr = new Point( br.x , tl.y );
        Point bl = new Point( tl.x , br.y );
        Rect rect1 = new Rect( tl , centroid );//左上角区域
        Rect rect2 = new Rect( tr , centroid );//右上角区域
        Rect rect3 = new Rect( bl , centroid );//左下角区域
        Rect rect4 = new Rect( br , centroid );//右下角区域

        Mat roiAll = new Mat( binaryPic , rect );
        Mat roi1 = new Mat( binaryPic , rect1 );
        Mat roi2 = new Mat( binaryPic , rect2 );
        Mat roi3 = new Mat( binaryPic , rect3 );
        Mat roi4 = new Mat( binaryPic , rect4 );
        double numPixelAll = Core.countNonZero(roiAll);
        double numPixel1 = Core.countNonZero(roi1)/numPixelAll;
        double numPixel2 = Core.countNonZero(roi2)/numPixelAll;
        double numPixel3 = Core.countNonZero(roi3)/numPixelAll;
        double numPixel4 = Core.countNonZero(roi4)/numPixelAll;
        double pixelsDistLevel = (numPixel1+numPixel3)/(numPixel2+numPixel4)/numPixelAll;
        double pixelsDistVertical = (numPixel1+numPixel2)/(numPixel3+numPixel4)/numPixelAll;
        double pixelsDistDiagonal = (numPixel1+numPixel4)/(numPixel2+numPixel3)/numPixelAll;
//        return new double[] { numPixel1 ,numPixel2 ,numPixel3 , numPixel4 };
        return new double[] {numPixel1 ,numPixel2 ,numPixel3 , numPixel4 , pixelsDistLevel,pixelsDistVertical,pixelsDistDiagonal};
    }

    /**
     * 拼接多个double数组
     * @param categorys 被拼接的数组
     * @return 拼接在一起的数组
     */
    public static double[] AddArray2(double[]... categorys) {
        int size = categorys.length;  //获取参数个数
        //计算拼接后数组总长度
        int length = 0;
        for (double[] category : categorys) {
            length += category.length;  //每个数组的长度
        }
        //创建输出数组
        double[] Result = new double[length];
        //写入
        int index = 0;
        for (double[] category : categorys) {
            for (double v : category) {
                Result[index] = v;
                index++;
            }
        }
        return Result;
    }

    /**
     * 离散度
     * @param binaryPic
     * @return
     */
    public static double[] Dispersion(Mat binaryPic){
        //角点检测
        MatOfPoint corners = new MatOfPoint();
        Imgproc.goodFeaturesToTrack(binaryPic,corners,100,0.01,22,new Mat(),3,false,0.04);
        //绘制角点
        Point[] points = corners.toArray();//角点点集
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
        Point lineP2 = new Point(binaryPic.width()-1 , kline*(binaryPic.width()-1-lineParams[2])+lineParams[3]);
        //计算距离
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
        double dispersion = dis/(numW*binaryPic.height());
        return new double[]{dispersion,kline};

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

    public static double horizontalLine(Mat binaryPic ){
        List<MatOfPoint> contours = new ArrayList<>();//轮廓列表
        Mat hierarchy = new Mat();//轮廓图
        Mat hierarchyPic = new Mat();//轮廓图
        Mat horizontalPic = new Mat();//水平线轮廓图
        //轮廓识别
        Imgproc.findContours(binaryPic, contours, hierarchy, Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_SIMPLE, new Point(0, 0));
        double numPixelAll = Core.countNonZero(binaryPic);
        hierarchyPic.create(binaryPic.size(),binaryPic.type());
        for(int i = 0;i <contours.size(); i++){
            Imgproc.drawContours(hierarchyPic,contours,i,new Scalar(255,255,255),1);
        }
        Mat element = Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new Size(11, 1), new Point(-1, -1));
        //先腐蚀再膨胀
        Imgproc.morphologyEx(hierarchyPic, horizontalPic, Imgproc.MORPH_ERODE, element);
        Imgproc.morphologyEx(horizontalPic, horizontalPic, Imgproc.MORPH_DILATE, element);
        double numPixel_hierarchyPic = Core.countNonZero(hierarchyPic);
        double numPixel_Horizontal = Core.countNonZero(horizontalPic);
        return numPixel_Horizontal /numPixelAll;
    }

    /**
     * 检测垂直线段
     * @param binaryPic
     * @return 垂直线段占比
     */
    public static double VerticalLine(Mat binaryPic ){

        double numPixelAll = Core.countNonZero(binaryPic);//全部图形像素数量
        Mat horizontalPic = new Mat();//垂直线轮廓图
        Mat element = Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new Size(1, 11), new Point(-1, -1));
        //先腐蚀再膨胀
        Imgproc.morphologyEx(binaryPic, horizontalPic, Imgproc.MORPH_ERODE, element);
        Imgproc.morphologyEx(horizontalPic, horizontalPic, Imgproc.MORPH_DILATE, element);
        double numPixel_Horizontal = Core.countNonZero(horizontalPic);
        return numPixel_Horizontal/numPixelAll ;
    }

    /**
     * 检测水平线段
     * @param binaryPic
     * @return 水平线段占比
     */
    public static double HorizontalLine( Mat binaryPic ){

        double numPixelAll = Core.countNonZero(binaryPic);//全部图形像素数量
        Mat horizontalPic = new Mat();//水平线轮廓图
        Mat element = Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new Size(11, 1), new Point(-1, -1));
        //先腐蚀再膨胀
        Imgproc.morphologyEx(binaryPic, horizontalPic, Imgproc.MORPH_ERODE, element);
        Imgproc.morphologyEx(horizontalPic, horizontalPic, Imgproc.MORPH_DILATE, element);
        double numPixel_Horizontal = Core.countNonZero(horizontalPic);
        return numPixel_Horizontal/numPixelAll ;
    }


    /**
     * 发现台阶特征
     * @param binaryPic
     * @return 台阶卷积核处理后的面积比
     */
    public static double findStairs( Mat binaryPic, int width , int height ){
        double numPixelAll = Core.countNonZero(binaryPic);
        Mat pic = new Mat();//水平线轮廓图
        Mat element = getStairElement( width , height );
        //形态学开操作
        Imgproc.morphologyEx(binaryPic, binaryPic, Imgproc.MORPH_OPEN, element);
        double numPixel = Core.countNonZero(binaryPic);
        return numPixel/numPixelAll ;
    }

    /**
     * 构建台阶卷积核
     * @param width 台阶宽
     * @param height 台阶高
     * @return 台阶卷积核
     */
    public static Mat getStairElement(int width , int height ){
        Mat element = Imgproc.getStructuringElement(Imgproc.MORPH_CROSS, new Size(width*2-1, height*2-1), new Point(-1, -1));
        Imgproc.line(element,new Point(width-1,0) , new Point(width-1,height-2) , new Scalar(0,0,0) , 1);
        Imgproc.line(element,new Point(0,height-1), new Point(width-2,height-1) , new Scalar(0,0,0) , 1);
        return element;
    }
    public static Mat getStairElement1(int width , int height ){
        Mat element = Imgproc.getStructuringElement(Imgproc.MORPH_CROSS, new Size(width, height), new Point(-1, -1));
        int widthCenter = width/2;
        int heightCenter = height/2;
        Imgproc.line(element,new Point(widthCenter,0) , new Point(widthCenter,heightCenter-1) , new Scalar(0,0,0) , 1);
        Imgproc.line(element,new Point(0,heightCenter), new Point(widthCenter-1,heightCenter) , new Scalar(0,0,0) , 1);
        return element;
    }

}
