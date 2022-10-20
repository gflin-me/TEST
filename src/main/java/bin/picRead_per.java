package bin;

import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.highgui.HighGui;
import unit.StereoBMUtil;
import unit.pcProcess;

import java.io.FileWriter;
import java.io.IOException;

import static org.opencv.imgcodecs.Imgcodecs.imread;
import static org.opencv.imgcodecs.Imgcodecs.imwrite;

public class picRead_per {


    static {
    // 动态链接opencv
    System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
    }
    private static final StereoBMUtil stereoBMUtil = new StereoBMUtil( 0 , 2 ,0 ,0);
    public static void main(String[] args) throws IOException {
        //读取图片，计算深度图，点云图
        String savePath = "C:\\Users\\vincent\\Desktop\\stereoCamera\\pic\\";
        String pointPath = "pointData\\";
        String depthPath = "depthImg\\";
        String readName = "0715152955_-43.901367_-1.2194824_-95.42175";//obstacle00  down004  floor005
        Mat frameBit =imread(savePath+readName+".png");
        Mat result = stereoBMUtil.SGBMCompute(frameBit);
        Mat mat3D = stereoBMUtil.get3Dmat();
        //投影为二值图
        Mat binary = ReProject.reProject(mat3D);
        //获取坐标
        double[][] coordinate = pcProcess.coordinateArray(mat3D);
        double[] x = coordinate[0];
        double[] y = coordinate[1];
        double[] z = coordinate[2];
        //存入数据
        FileWriter fw = new FileWriter(savePath+pointPath+readName+".txt");
        for (int i = 0; i < x.length; i++) {
            fw.write(x[i]+"\t"+y[i]+"\t"+z[i]+"\t\n");//读取一个数字,就写入文件一次
        }
        fw.close();//输出流用完就关闭

//        PlotPC.plot3D(x,z,y);

        HighGui.imshow("深度图", result);
        imwrite(savePath+depthPath+readName+"_depth.jpg",result);
//        HighGui.imshow("二值图", binary);
        HighGui.waitKey(0);
    }


}


