package bin;

import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.Size;
import org.opencv.highgui.HighGui;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

public class opencvTest {

        static {
            //在使用OpenCV前必须加载Core.NATIVE_LIBRARY_NAME类,否则会报错
            System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
        }

        public static void main(String[] args) {
            getPixelImage_1();
        }
        /**
         * OpenCV-4.0.0 获取图像像素点
         * @return: void
         * @date: 2019年1月17日 下午8:24:07
         */
        public static void getPixelImage_1() {
            Mat src = Imgcodecs.imread("C:/Users/vincent/Desktop/pic/down001.png");
            Mat dst = new Mat(src.size(),src.type());

            int channels = src.channels();//获取图像通道数
            double[] pixel = new double[3];//用于存放像素点

            for (int i = 0, rlen = src.rows(); i < rlen; i++) {
                for (int j = 0, clen = src.cols(); j < clen; j++) {
                    if (channels == 3) {//图片为3通道即平常的(B,G,R)
                        pixel = src.get(i, j).clone();
                        pixel[0] = 255 - pixel[0];//B
                        pixel[1] = 255 - pixel[1];//G
                        pixel[2] = 255 - pixel[2];//R
                        dst.put(i, j, pixel);
                    } else {//图片为单通道
                        dst.put(i, j, src.get(i, j).clone());
                    }
                }
            }
            Imgproc.resize(src, src, new Size(src.cols()/2,src.rows()/2));
            HighGui.imshow("图片测试", dst);
            HighGui.waitKey(0);
        }

}
