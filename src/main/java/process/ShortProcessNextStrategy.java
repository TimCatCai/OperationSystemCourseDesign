package process;

import org.apache.log4j.Logger;

import java.util.AbstractQueue;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.PriorityBlockingQueue;


public class ShortProcessNextStrategy extends AbstractScheduleStrategy {
    private Logger logger = Logger.getLogger(ShortProcessNextStrategy.class.getName());
    public ShortProcessNextStrategy(AbstractQueue<IPcb> pcbQueue, IProcessesController controller) {
        super(pcbQueue, controller);
    }

    public ShortProcessNextStrategy(AbstractQueue<IPcb> pcbQueue){
        super(pcbQueue);
    }

    @Override
    public IPcb getNextProcess() {
        synchronized (lock) {
            if (getPcbQueue().isEmpty()) {
                try {
                    lock.wait();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            logger.debug(getDebugInfo());

            IPcb nextPcb = null;
            try {
                nextPcb = ((PriorityBlockingQueue<IPcb>) getPcbQueue()).take();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            if (nextPcb != null) {
                setScheduleTime(nextPcb.getExecutionTime() - nextPcb.getExecutedTime());
            } else {
                setScheduleTime(0);
            }
            return nextPcb;
        }
    }

    @Override
    public boolean isReSchedule(IPcb pcbRunning, IPcb newPcb) {
        // 短进程可以抢夺当前cpu调度
        long currentTime = System.currentTimeMillis();
        // 当前进程已执行时间
        long executedTime = currentTime - pcbRunning.getArrivalTime() - pcbRunning.getWaitedTime();
        // 当前进程需执行总时间
        long executionTime = pcbRunning.getExecutionTime();
        long pubRunningExecutingTimeNeed = executionTime -  executedTime;
        return pubRunningExecutingTimeNeed > newPcb.getExecutionTime();
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
