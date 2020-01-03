package process;
import java.util.Stack;

/**
 * pcb的接口操作
 * @author TimCat
 * @version 0.1 2019/12/01
 */
public interface IPcb extends Comparable<IPcb>{

    /**
     * 执行PCB
     * @param time 执行的时间
     */
    void execute(long time);

    /**
     * 保存CPU现场信息
     * @param cpuInfo cpu的现场信息
     */
    void setCpuInfo(Stack<Long> cpuInfo);

    long getExecutedTime();
    long getExecutionTime();
    int getPriority();
    int getProcessId();
    void setExecutedTime(long executedTime);
    void setFinishedTime(long currentTime);
    ProcessStatus getProcessStatus();
    void setCompareTo(IPcbComparator comparator);
    IPcbComparator getCompareTo();
    ProcessScheduleInfo getProcessScheduleInfo();
    long getWaitedTime();
    void setWaitedTime(long waitedTime);
    long getArrivalTime();

    long getWaitedTimeWithoutUpdate();
}
