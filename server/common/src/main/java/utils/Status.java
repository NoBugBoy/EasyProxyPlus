package utils;

/**
 * Author yujian
 * Description 状态管理
 * Date 2021/2/2
 */ 
public interface Status {
    int conn = 1;
    int connbak = 2;
    int data = 3;
    int back = 4;
    int ping = 5;
    int pong = 6;
}
