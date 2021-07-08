package handler;

import io.netty.handler.traffic.ChannelTrafficShapingHandler;

/**
 * author yujian
 * description
 * create 2021-03-25 16:56
 **/
public class FlowChannelTrafficShapingHandler extends ChannelTrafficShapingHandler {
    public FlowChannelTrafficShapingHandler(long writeLimit, long readLimit, long checkInterval, long maxTime) {
        super(writeLimit, readLimit, checkInterval, maxTime);
    }

}
