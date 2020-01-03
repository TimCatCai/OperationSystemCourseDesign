import org.apache.commons.lang3.RandomUtils;
import org.apache.log4j.Logger;
import process.*;

import java.util.PriorityQueue;
import java.util.concurrent.*;

public class Main {
    private static Logger logger = Logger.getLogger(Main.class.getName());

    public static void main(String[] args) throws InterruptedException {
        ExecutorService service = Executors.newCachedThreadPool();
        ICpu cpu = new Cpu();
        AbstractScheduleStrategy strategy = new TimePieceFirst(new LinkedBlockingQueue<>());

       IProcessesController controller =
               new ProcessesController(cpu, strategy);;
        cpu.setController(controller);

        TimeUnit.SECONDS.sleep(1);
        service.execute(cpu);
        service.execute(controller);
        for (int i = 0; i < 5; i++) {
            TimeUnit.MILLISECONDS.sleep(RandomUtils.nextInt(10, 800));
            synchronized (strategy.lock){
                controller.offer(new Pcb(i, "Pcb-" + i));
                strategy.lock.notify();
            }
        }
        TimeUnit.SECONDS.sleep(20);
        controller.shutdown();
        service.shutdown();
        logger.debug("运行结束");
    }

}
