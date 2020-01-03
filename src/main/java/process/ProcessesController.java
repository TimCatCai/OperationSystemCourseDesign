package process;

import org.apache.log4j.Logger;

import java.util.AbstractQueue;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

public class ProcessesController implements IProcessesController {
    private Logger logger = Logger.getLogger(ProcessesController.class.getName());
    private LinkedBlockingQueue<Boolean> eventQueue = new LinkedBlockingQueue<>(1);
    private AbstractQueue<IPcb> pcbQueue;
    private IPcb pcbRunning;
    private static List<IPcb> pcbFinishedList = new ArrayList<>();
    private final ICpu cpu;
    private final AbstractScheduleStrategy scheduleStrategy;
    private Thread controllerThread;
    private volatile boolean isSchedule = true;


    public ProcessesController(AbstractQueue<IPcb> pcbQueue, IPcb pcbRunning,
                               List<IPcb> pcbList, ICpu cpu, AbstractScheduleStrategy scheduleStrategy) {
        this.pcbQueue = pcbQueue;
        this.pcbRunning = pcbRunning;
        this.cpu = cpu;
        this. pcbFinishedList = pcbList;
        cpu.notifier(eventQueue);
        this.scheduleStrategy = scheduleStrategy;
        this.init();
    }

    public ProcessesController(ICpu cpu, AbstractScheduleStrategy scheduleStrategy) {
        this(scheduleStrategy.getPcbQueue(), null, new ArrayList<IPcb>(), cpu, scheduleStrategy);
    }

    public ProcessesController(ICpu cpu) {
        this(cpu, new ShortProcessNextStrategy(new LinkedBlockingQueue<>()));
    }

    @Override
    public void offer(IPcb pcb) {
        // 动态设置pcb的比较策略
        if (pcb.getCompareTo() == null) {
            pcb.setCompareTo(scheduleStrategy.getIPcbComparator());
        }

        pcbQueue.offer(pcb);

        notifyController(pcb);
    }

    @Override
    public IPcb take() {
        return scheduleStrategy.getNextProcess();
    }

    @Override
    public void processFinished(IPcb pcb) {
        pcb.setFinishedTime(System.currentTimeMillis());
        pcbFinishedList.add(pcb);
    }

    @Override
    public void shutdown() {
        isSchedule = false;
        cpu.interrupt();
        cpu.powerOff();
        if(controllerThread != null){

                controllerThread.interrupt();

        }
    }


    public IPcb getPcbRunning() {
        return pcbRunning;
    }

    private void notifyController(IPcb newPcb) {
        // 第一次调度默认直接运行
        if (pcbRunning != null) {
            // 默认所有的新建进程必须等到调度完成，cpu开始执行才能进行下一步操作
            try {
                // 保证调度完成，cpu执行为原子操作
                eventQueue.poll();
                // 保证cpu进入睡眠状态（模拟cpu执行）
                TimeUnit.MILLISECONDS.sleep(20);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            // 如果不是第一次调度，判断是否需要重新调度
            if (scheduleStrategy.isReSchedule(pcbRunning, newPcb)) {
                // 新进程进来的情况：
                // 1。controller正在调度，cpu未开始运行，
                // controller完成调度，CPU未开始运行。
                // controller不运行，CPU刚好执行完毕，未通知重新调度
                // controller等待，cpu正在运行 直接调用调度策略通知功能，
                // 1,2,3：因为如果不是非可中断的语句，是不能强行中断的，所以如何让其他情况都到达第四种状态呢？
                // 4种情况:
                // 1. cpu control都不运行：cpu 未被中断执行完毕，重新调度开始之前，两个是停止的。
                // controller调度完成，cpu为开始执行
                // 2. cpu运行，control不运行，正常情况
                // 3. control 运行， cpu不运行，正常情况
                // 等待cpu运行完才能重新调度，否则会陷入死锁，即CPU与Controller同时执行会导致分别获取对方的资源
                // 这里通过锁cpu来判断CPU是否正在运行会导致，
                // 在调度完成，将要将pcb放入cpu运行时，准备获取cpu的锁，刚好有新的进程进来，这时会判断是否重新调度
                // 即这段代码块获取了cpu的锁，同时又准备获得scheduleStrategy的锁, 而原有准备运行的controller获取了该锁
                // 所以这里要同时判断controller是否在调度，如果在调度则等待调度完成(cpu要开始运行，如果cpu不开始运行，也会陷入死锁)，
                // 再重新调度
//            newPcbReSchedule();

                // cpu中断，促使其重新调度
                cpu.interrupt();
            }
        }

    }

    private void schedule() {
        if (cpu.isRunning()) {
            cpu.interrupt();
        }
        // 不是第一次调度且当前Pcb还未执行完毕，将其放回原队列
        if (pcbRunning != null && pcbRunning.getProcessStatus() != ProcessStatus.FINISH) {
            offerWithoutNotify(pcbRunning);
        }

        IPcb nextPcb = take();
        long executingTime = scheduleStrategy.getScheduleTime();
        pcbRunning = nextPcb;
        cpu.start(nextPcb, executingTime);
        // 设置非调度状态
        scheduleStrategy.setIsSchedule(false);
    }

    private void newPcbReSchedule() {
//        synchronized (cpu){
        // cpu 和 controller不能不同时运行， 否则会陷入死锁
        // 如果cpu执行完成，这是cpu 和 controller同时不运行，怎么办？？

        while (isSchedule || !cpu.isRunning()) {
            newPcbReSchedule();
        }

//        if(!cpu.isRunning()){
//                try {
//                    cpu.wait();
//                } catch (InterruptedException e) {
//                    e.printStackTrace();
//                }
//            }
        scheduleStrategy.reSchedule();
//        }
    }

    @Override
    public void reSchedule() {
//        synchronized (cpu){
//            // cpu 和 controller不能不同时运行， 否则会陷入死锁
//            // 如果cpu执行完成，这是cpu 和 controller同时不运行，怎么办？？
//            if(!cpu.isRunning()){
//                try {
//                    cpu.wait();
//                } catch (InterruptedException e) {
//                    e.printStackTrace();
//                }
//            }
        scheduleStrategy.reSchedule();
//        }
    }

    private void offerWithoutNotify(IPcb pcb) {
        pcbQueue.offer(pcb);
    }

    @Override
    public String toString() {
        return super.toString();
    }

    @Override
    public void run() {
        controllerThread = Thread.currentThread();
        while (isSchedule) {
            synchronized (scheduleStrategy) {
                if (scheduleStrategy.isSchedule()) {
//                    logger.info("begin to schedule");
                    schedule();
                } else {
                    try {
//                        logger.info("Controller wait");
                        scheduleStrategy.wait();
                    } catch (InterruptedException e) {
                         logger.debug("");
                    }
                }
            }

        }
    }

    private void init() {
        this.scheduleStrategy.setController(this);
    }

    public static List<IPcb> getPcbFinishedList() {
        return pcbFinishedList;
    }
}
