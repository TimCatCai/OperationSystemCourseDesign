package process;

import org.apache.log4j.Logger;

import java.util.AbstractQueue;
import java.util.ArrayList;
import java.util.List;
import java.util.PriorityQueue;

public class HighestResponseRatioNextStrategy extends AbstractScheduleStrategy {
    private Logger logger = Logger.getLogger(HighestResponseRatioNextStrategy.class.getName());
    protected HighestResponseRatioNextStrategy(AbstractQueue<IPcb> pcbQueue, IProcessesController controller) {
        super(pcbQueue, controller);
    }

    public HighestResponseRatioNextStrategy(AbstractQueue<IPcb> pcbQueue) {
        this(pcbQueue, null);

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
            long currentTime = System.currentTimeMillis();
            // 更新等待时间
            getPcbQueue()
                    .forEach(pcb -> pcb.setWaitedTime(currentTime - pcb.getArrivalTime() - pcb.getExecutedTime()));
            System.out.println();
            List<IPcb> currentPcbs = new ArrayList<>(getPcbQueue());
            getPcbQueue().clear();
            getPcbQueue().addAll(currentPcbs);
            logger.debug(getDebugInfo());
            IPcb pcb = getNextPcbFromQueue();
            return pcb;
        }

    }

    @Override
    public boolean isReSchedule(IPcb pcbRunning, IPcb newPcb) {
        return false;
    }

    @Override
    protected String getDebugInfo() {
        StringBuilder pcbIds = new StringBuilder();
        StringBuilder pcbResponseRation = new StringBuilder();

        List<IPcb> pcbs = new ArrayList<>();
        while (!getPcbQueue().isEmpty()){
            IPcb pcb = getPcbQueue().poll();
            pcbResponseRation.append(String.format("%.2f", (double) pcb.getWaitedTimeWithoutUpdate() / pcb.getExecutionTime() + 1));
            pcbResponseRation.append(",");
            pcbIds.append(pcb.getProcessId());
            pcbIds.append(",");
            pcbs.add(pcb);
        }
        getPcbQueue().addAll(pcbs);

        return super.getDebugInfo() + "当前就绪队列进程Id序列及长度信息：\n"
                + "[" + pcbIds + "]\n"
                + "[" + pcbResponseRation + "]\n";
    }

    private IPcb getNextPcbFromQueue() {
        IPcb nextPcb =
             ((PriorityQueue<IPcb>) getPcbQueue()).poll();
        if (nextPcb != null) {
            setScheduleTime(nextPcb.getExecutionTime() - nextPcb.getExecutedTime());
        } else {
            setScheduleTime(0);
        }
        return nextPcb;
    }


}
