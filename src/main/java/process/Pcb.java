package process;

import org.apache.log4j.Logger;
import utils.DebugUtils;

import java.util.Stack;
import java.util.concurrent.TimeUnit;


public class Pcb implements IPcb, Comparable<IPcb> {
    private Logger logger = Logger.getLogger(Pcb.class.getName());

    /**
     * 进程id号，唯一标识进程
     */
    private int processId;
    /**
     * 进程名
     */
    private String processName;
    /**
     * cpu 状态，即CPU现场信息
     */
    private Stack<Long> cpuInfo;
    /**
     *  进程调度信息
      */
    private ProcessScheduleInfo processScheduleInfo;
    private IPcbComparator IPcbComparator;


    public Pcb(int processId, String processName, Stack<Long> cpuInfo, ProcessScheduleInfo processScheduleInfo) {
        this.processId = processId;
        this.processName = processName;
        this.cpuInfo = cpuInfo;
        this.processScheduleInfo = processScheduleInfo;
    }

    public Pcb(int processId, String processName, ProcessScheduleInfo processScheduleInfo){
        this(processId,processName,new Stack<Long>(), processScheduleInfo);
    }

    public Pcb(int processId, String processName){
        this(processId, processName, new ProcessScheduleInfo());
    }


    @Override
    public synchronized void execute(long time){
        long startTime = System.currentTimeMillis();
        long endTime = 0;
        long executedTime = time;
        try {

            TimeUnit.MILLISECONDS.sleep(time);
            logger.debug("线程 id: " + getProcessId() + " name: " + getProcessName() + "开始执行：\n" + "线程 id: " + getProcessId() + " name: " + getProcessName() + "本次调度执行完成。\n" +
                    "执行时间：" + executedTime + "ms");
        } catch (InterruptedException e) {
            endTime = System.currentTimeMillis();
            executedTime = endTime - startTime;
            logger.debug("线程 id: " + getProcessId() + " name: " + getProcessName() + "开始执行：\n" + "线程 "+ getPcbAbstractInfo() + " 已被中断。\n" +
                    "执行时间：" + executedTime + "ms");
        }
        Stack<Long> cpuInfo = new Stack<>();
        cpuInfo.push((long)this.getProcessId());
        this.setCpuInfo(cpuInfo);
        setExecutedTimeAfterExecute(executedTime);
    }

    @Override
    public int getProcessId() {
        return processId;
    }

    public String getProcessName() {
        return processName;
    }

    public Stack<Long> getCpuInfo() {
        return cpuInfo;
    }

    @Override
    public ProcessScheduleInfo getProcessScheduleInfo() {
        return processScheduleInfo;
    }

    @Override
    public long getWaitedTime() {
        return processScheduleInfo.getWaitingTime();
    }

    @Override
    public void setWaitedTime(long waitedTime) {
        processScheduleInfo.setWaitingTime(waitedTime);
    }

    @Override
    public long getArrivalTime() {
        return processScheduleInfo.getArrivedTime();
    }

    @Override
    public long getWaitedTimeWithoutUpdate() {
        return processScheduleInfo.getWaitingTimeWithoutUpdate();
    }

    @Override
    public void setCpuInfo(Stack<Long> cpuInfo) {

    }

    @Override
    public synchronized long getExecutedTime() {
        return processScheduleInfo.getExecutedTime();
    }

    @Override
    public long getExecutionTime() {
        return processScheduleInfo.getExecutionTime();
    }
    @Override
    public int getPriority() {
        return processScheduleInfo.getPriority();
    }
    @Override
    public synchronized  void setExecutedTime(long executedTime) {
        processScheduleInfo.setExecutedTime(executedTime);
    }
    @Override
    public void setFinishedTime(long currentTime) {
        this.processScheduleInfo.setFinishedTime(currentTime);
    }
    @Override
    public synchronized ProcessStatus getProcessStatus() {
        return this.processScheduleInfo.getProcessStatus();
    }
    @Override
    public void setCompareTo(IPcbComparator comparator) {
        this.IPcbComparator = comparator;
    }

    @Override
    public IPcbComparator getCompareTo() {
        return this.IPcbComparator;
    }

    public String getPcbAbstractInfo(){
        return "Thread-id: " + getProcessId() + " name: " + getProcessName();
    }
    @Override
    public String toString() {
        String pcbInfo = "Thread-id: " + getProcessId() + " name: " + getProcessName() + "\n"
                + processScheduleInfo.toString();
        return DebugUtils.modifyToSameLength(pcbInfo);
    }

    @Override
    public int compareTo(IPcb o) {

        // 默认以优先级作为比较的依据
        if(IPcbComparator == null){
            return Integer.compare(this.getPriority(), o.getPriority());
        }
        return IPcbComparator.compareTo(this, o);
    }

    private void setExecutedTimeAfterExecute(long delta){
        if(delta + getExecutedTime() < getExecutionTime()){
            setExecutedTime(getExecutedTime() + delta);
        }else if(delta + getExecutedTime() >= getExecutionTime()){
            setExecutedTime(getExecutionTime());
            this.setFinishedTime(System.currentTimeMillis());
            this.processScheduleInfo.setProcessStatus(ProcessStatus.FINISH);
            ProcessesController.getPcbFinishedList().add(this);
        }
    }
}
