package process;

import org.apache.commons.lang3.RandomUtils;

/**
 * 线程调度信息
 * @author TimCat
 * @version 0.1 2019/12/01
 */
public class ProcessScheduleInfo {
    /**
     * 进程优先级
     */
    private int priority;
    /**
     * 进程所处状态
     */
    private ProcessStatus processStatus;
    /**
     * 进程到达时间
     */
    private long arrivedTime;
    /**
     * 进程已等待时间， 单位毫秒
     */
    private long waitingTime;
    /**
     * 进程已执行时间总和，单位毫秒
     */
    private long executedTime;
    /**
     * 进程完成所需时间，单位毫秒
      */
    private final long executionTime;

    /**
     * 进程运行结束时间
     */
    private long finishedTime;
    /**
     * 阻塞原因
     */
    private int  blockedReason;

    public ProcessScheduleInfo(int priority, ProcessStatus processStatus, long arrivedTime,
                               long waitingTime, long executedTime, long executionTime, int blockedReason) {
        this.priority = priority;
        this.processStatus = processStatus;
        this.arrivedTime = arrivedTime;
        this.waitingTime = waitingTime;
        this.executedTime = executedTime;
        this.executionTime = executionTime;
        this.blockedReason = blockedReason;
    }

    public ProcessScheduleInfo(){
        priority =  RandomUtils.nextInt(1, 11);
        processStatus = ProcessStatus.READY;
        // 到达时间为系统时间
        arrivedTime = System.currentTimeMillis();
        waitingTime = 0;
        executedTime = 0;
        // 单位毫秒
        executionTime  = RandomUtils.nextInt(100, 1000);
        // 阻塞原因初初始化为 0
        blockedReason  = 0;
    }

    public int getPriority() {
        return priority;
    }

    public ProcessStatus getProcessStatus() {
        return processStatus;
    }

    public long getArrivedTime() {
        return arrivedTime;
    }

    public long getWaitingTime() {
        // 等待时间 = 当前周转时间 - 执行时间
        waitingTime = getCycleTime() - executedTime;
        return waitingTime;
    }

    public long getWaitingTimeWithoutUpdate(){
        return this.waitingTime;
    }

    public long getExecutedTime() {
        return executedTime;
    }

    public long getExecutionTime() {
        return executionTime;
    }

    public int getBlockedReason() {
        return blockedReason;
    }

    public long getFinishedTime() {
        return finishedTime;
    }

    public void setExecutedTime(long executedTime) {
        this.executedTime = executedTime;
    }

    public void setFinishedTime(long finishedTime) {
        this.finishedTime = finishedTime;
    }

    /**
     * 获取进程当前的周转时间
     * @return 进程周转时间
     */
    public long getCycleTime(){
        if(processStatus == ProcessStatus.FINISH){
            // 进程已经结束，使用结束时间减去到达时间
            return finishedTime - arrivedTime;
        }else{
            // 进程还未结束，使用当前时间减去达到时间
            return System.currentTimeMillis() - arrivedTime;
        }
    }


    public void setPriority(int priority) {
        this.priority = priority;
    }

    public void setProcessStatus(ProcessStatus processStatus) {
        this.processStatus = processStatus;
    }

    public void setArrivedTime(long arrivedTime) {
        this.arrivedTime = arrivedTime;
    }

    public void setWaitingTime(long waitingTime) {
        this.waitingTime = waitingTime;
    }

    public void setBlockedReason(int blockedReason) {
        this.blockedReason = blockedReason;
    }

    @Override
    public String toString() {
        StringBuilder info = new StringBuilder();
        info.append("------------ pcb info ------------\n");
        info.append("priority: ");
        info.append(priority);
        info.append("\n");

        info.append("processStatus: ");
        info.append(processStatus);
        info.append("\n");

        info.append("arrivedTime: ");
        info.append(arrivedTime);
        info.append("\n");

        info.append("waitingTime: ");
        info.append(waitingTime);
        info.append("\n");

        info.append("executedTime: ");
        info.append(executedTime);
        info.append("\n");

        info.append("executionTime: ");
        info.append(executionTime);
        info.append("\n");

        info.append("blockedReason: ");
        info.append(blockedReason);
        info.append("\n");

        info.append("----------------------------------\n");
        return info.toString();
    }
}
