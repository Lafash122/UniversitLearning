#include <iostream>
#include <string>
#include <iomanip>
#include <sstream>
#include <opencv2/core.hpp>
#include <opencv2/imgproc.hpp>
#include <opencv2/videoio.hpp>
#include <opencv2/highgui.hpp>
#include <opencv2/imgcodecs.hpp>

using namespace std;
using namespace cv;

int main(int argc, char** argv) {
    VideoCapture cap(0); //use cv::CAP_V4L2 to avoid warning, small window
    if (!cap.isOpened()) {
	    cout << "Couldn't open the camera" << endl;
	    return 0;
    }

    Mat frame, edges_fr, contrast_fr;
    int frame_cnt = 0, total_frame_cnt = 0;
    double ticks = getTickFrequency();
    double fps = 0.0;

    double input_time = 0, proc_time = 0, demo_time = 0;
    double input_start, proc_start, demo_start;
    double input_end, proc_end, demo_end;

    double start_ticks = getTickCount(), start_time = getTickCount();

    while (true) {
	    input_start = getTickCount();
        cap.read(frame);
	    input_end = getTickCount();
	    input_time += (input_end - input_start) / ticks;

	    if (frame.empty()) {
	        cout << "Couldn't read frame" << endl;
	        break;
	    }

	    proc_start = getTickCount();
	    frame.convertTo(contrast_fr, -1, 7.5, 0);
	    Canny(contrast_fr, edges_fr, 10, 70);
	    proc_end = getTickCount();
	    proc_time += (proc_end - proc_start) / ticks;

	    demo_start = getTickCount();
	    imshow("Demo", edges_fr);
	    demo_end = getTickCount();
	    demo_time += (demo_end - demo_start) / ticks;

	    if (waitKey(1) == 'z') break;

	    frame_cnt++;
	    total_frame_cnt++;

	    if (frame_cnt == 5) {
            double time_elapsed = (getTickCount() - start_ticks) / ticks;
            fps = frame_cnt / time_elapsed;
	        cout << "FPS: " << fps << endl;

            frame_cnt = 0;
            start_ticks = getTickCount();
        }
    }

    cap.release();
    destroyWindow("Demo");

    double total_time = (getTickCount() - start_time) / ticks;
    double input_part = input_time / total_time * 100;
    double proc_part = proc_time / total_time * 100;
    double demo_part = demo_time / total_time * 100;

    double average_input_time = input_time / total_frame_cnt;
    double average_proc_time = proc_time / total_frame_cnt;
    double average_demo_time = demo_time / total_frame_cnt;

    cout << "Avarage Input time: " << average_input_time << " - " << input_part << "%" << endl;
    cout << "Avarage Processing time: " << average_proc_time << " - " << proc_part << "%" << endl;
    cout << "Avarage Demonstration time: " << average_demo_time << " - " << demo_part << "%" << endl;

    return 0;
}
