package process;

import java.util.AbstractQueue;
import java.util.List;

public abstract class AbstractScheduleStrategy{
    private AbstractQueue<IPcb> pcbQueue;
    private IProcessesController controller;
    private volatile boolean isSchedule;
    private IPcbComparator IPcbComparator;
    private long scheduleTime;
    public final Object lock = new Object();
    public abstract IPcb getNextProcess();
    protected AbstractScheduleStrategy(AbstractQueue<IPcb> pcbQueue, IProcessesController controller){
        this.pcbQueue = pcbQueue;
        this.controller = controller;
        this.isSchedule = true;
    }
    protected AbstractScheduleStrategy(AbstractQueue<IPcb> pcbQueue){
        this.pcbQueue = pcbQueue;
        this.isSchedule = true;
    }

    public AbstractQueue<IPcb> getPcbQueue(){
        return this.pcbQueue;
    }
    public abstract boolean isReSchedule(IPcb pcbRunning, IPcb newPcb);
    public boolean isSchedule(){
        return this.isSchedule;
    }
    public synchronized void reSchedule(){
        // 这里一定要先设置调度标志 ，再提醒控制器重新调度，否则控制器可能会错过调度
        isSchedule = true;
        this.notify();
    }
    protected void setIPcbComparator(IPcbComparator comparator){
        this.IPcbComparator = comparator;
    }
    public IPcbComparator getIPcbComparator(){
        return this.IPcbComparator;
    }
    protected void setIsSchedule(boolean isSchedule){
        this.isSchedule = isSchedule;
    }
    public void setController(IProcessesController controller){
        this.controller = controller;
    }

    public long getScheduleTime(){
        return this.scheduleTime;
    }
    protected void setScheduleTime(long scheduleTime){
        this.scheduleTime = scheduleTime;
    }

    protected String getDebugInfo(){
        List<IPcb> processFinished = ProcessesController.getPcbFinishedList();
        StringBuilder info = new StringBuilder("已完成队列：\n [");
        for(IPcb pcb: processFinished){
            info.append(pcb.getProcessId());
            info.append(",");
        }
        info.append("]\n");
        return  info.toString();
    }
}