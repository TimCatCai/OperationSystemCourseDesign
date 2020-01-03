package process;

import org.apache.log4j.Logger;

import java.util.AbstractQueue;
import java.util.ArrayList;
import java.util.List;

public class TimePieceFirst extends AbstractScheduleStrategy {
    private Logger logger = Logger.getLogger(TimePieceFirst.class.getName());
    private static long TIME_PIECE_LEN = 500;
    public TimePieceFirst(AbstractQueue<IPcb> pcbQueue) {
        super(pcbQueue);
        setScheduleTime(TIME_PIECE_LEN);
    }

    @Override
    public IPcb getNextProcess() {
        synchronized (lock){
            if(getPcbQueue().isEmpty()){
                try {
                    lock.wait();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            logger.debug(getDebugInfo());
            return getPcbQueue().poll();
        }
    }

    @Override
    public boolean isReSchedule(IPcb pcbRunning, IPcb newPcb) {
        return false;
    }

    @Override
    protected String getDebugInfo() {
        StringBuilder pcbIds = new StringBuilder();
        StringBuilder pcbLen = new StringBuilder();
        List<IPcb> pcbs = new ArrayList<>();
        while (!getPcbQueue().isEmpty()){
            IPcb pcb = getPcbQueue().poll();
            pcbLen.append(pcb.getExecutionTime() - pcb.getExecutedTime());
            pcbLen.append(",");
            pcbIds.append(pcb.getProcessId());
            pcbIds.append(",");
            pcbs.add(pcb);
        }

        getPcbQueue().addAll(pcbs);

        return super.getDebugInfo() + "当前就绪队列进程Id序列及长度信息：\n"
                + "[" + pcbIds + "]\n"
                + "[" + pcbLen + "]\n";

    }
}
