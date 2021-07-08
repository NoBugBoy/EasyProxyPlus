package utils;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;
/**
 * Author yujian
 * Description tcp阻塞队列
 * Date 2021/2/2
 */
public class TcpQueue extends LinkedBlockingQueue<Object>{
    private final static Map<String,TcpQueue> queues = new ConcurrentHashMap<>();
    public synchronized static TcpQueue getQueue(String port){
        TcpQueue queue = queues.get(port);
        if(queue == null){
            queue = new TcpQueue();
            queues.put(port,queue);
            return queue;
        }
        return queue;
    }

}
