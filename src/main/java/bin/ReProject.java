package bin;

import org.opencv.core.Mat;

import static org.opencv.core.CvType.CV_8UC1;

public class ReProject {
    //3D点云投影成二值图
    public static Mat reProject(Mat mat3D){
        //建立黑色底图
        Mat binaryMat = new Mat(600, 600, CV_8UC1);
        for (int i = 0 ; i < binaryMat.cols() ; i++ ){
            for (int j = 0 ; j < binaryMat.rows(); j++ ) {
                binaryMat.put(j,i,0);
            }
        }

        for (int i = 0 ; i < mat3D.cols() ; i++ ){
            for (int j = 0 ; j < mat3D.rows(); j++ ){
                double[] coordinate = mat3D.get(j,i);
                double x = coordinate[0];
                double y = coordinate[1];
                double z = coordinate[2];
                if( y<3000 & y>-3000 & z<6000 & x<100 & x>-100){
                    int yb = (int)(y+3000)/10;
                    int zb = (int)z/10;
                    binaryMat.put(yb,zb,255);
                }

            }
        }
        return binaryMat;
    }

}
