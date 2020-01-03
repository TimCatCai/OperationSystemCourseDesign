package process;

import org.apache.log4j.Logger;
import utils.DebugUtils;

import java.util.concurrent.LinkedBlockingQueue;


public class Cpu implements ICpu {
    private Logger logger = Logger.getLogger(Cpu.class.getName());
    private volatile boolean isRunning = false;
    private volatile boolean powerOff = false;
    private IPcb pcbRunning;
    private long runTime;
    private Thread currentThread;
    private IProcessesController controller;
    private LinkedBlockingQueue<Boolean> eventQueue;

    public Cpu(IProcessesController controller) {
        this.controller = controller;
    }

    public Cpu() {
    }

    @Override
    public Register[] getRegisters() {
        return new Register[0];
    }

    @Override
    public boolean isRunning() {
        return isRunning;
    }

    @Override
    public void interrupt() {
        // 这里如果判断过后，cpu执行完毕，这里有两种情况
        // 1. 保存cpu现场，或者说保存pcb信息，中被打断，会忽略
        // 2. 要么打断的是当前的this锁的wait()，重新进入循环
        // 若cpu在执行中，即休眠状态，会保存pcb运行时间（pcb运行过程会持有pcb的锁，要获取pcb枷锁信息，必须等pcb执行完）, 为重新执行做准备
        // 上面三种情况，重新执行一个pcb都需要放弃this锁，即调用this.wait();之后在start中获取锁，调用notify
        if (isRunning && currentThread != null) {
            currentThread.interrupt();
        }
    }

    @Override
    public synchronized void start(IPcb pcb, long time) {
        pcbRunning = pcb;
        runTime = time;
        this.notify();
    }

    @Override
    public void powerOff() {
        powerOff = true;
    }

    @Override
    public void setController(IProcessesController controller) {
        this.controller = controller;
    }

    @Override
    public void notifier(LinkedBlockingQueue<Boolean> eventQueue) {
        this.eventQueue = eventQueue;
    }

    @Override
    public void run() {
        currentThread = Thread.currentThread();
        while (!powerOff) {
            synchronized (this) {
                if (pcbRunning != null) {
                    isRunning = true;
//                    logger.info("begin run");
                    // 通知调度完成，cpu开始完成
                    if (eventQueue.isEmpty()) {
                        eventQueue.add(true);
                    }
                    // 设置pcb的等待时间
                    long pcbWaitedTime = System.currentTimeMillis() - pcbRunning.getArrivalTime() - pcbRunning.getExecutedTime();
                    pcbRunning.setWaitedTime(System.currentTimeMillis());

                    // pcb执行前信息
                    String pcbInfoBeforeSchedule = "before: \n" + pcbRunning.toString();
                    pcbInfoBeforeSchedule = DebugUtils.modifyToSameLength(pcbInfoBeforeSchedule);
                    logger.debug("%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%\n" + "本次调度开始:");

                    pcbRunning.execute(runTime);

                    // pcb执行后信息
                    String pcbInfoAfterSchedule = "After: \n" + pcbRunning.toString();
                    pcbInfoAfterSchedule = DebugUtils.modifyToSameLength(pcbInfoAfterSchedule);
                    String interval = "       |       ";

                    String pcbInfo = DebugUtils.combineInOneLine(pcbInfoBeforeSchedule, pcbInfoAfterSchedule, interval);

                    logger.debug("pcb执行前后信息：\n" + pcbInfo);
                    logger.debug("本次调度结束\n" + "%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%\n");

                    pcbRunning = null;
                    isRunning = false;
                    this.controller.reSchedule();
                } else {
                    try {
//                        logger.info("Cpu wait");
                        this.wait();
                    } catch (InterruptedException e) {
                        logger.info("cpu 等待中，却被中断，重新等待！");
                    }
                }
            }
        }
    }

}
