package process;

import java.util.concurrent.LinkedBlockingQueue;

public interface ICpu extends Runnable{
    Register[] getRegisters();
    boolean isRunning();
    void interrupt();
    void start(IPcb pcb, long time);
    void powerOff();
    void setController(IProcessesController controller);
    void notifier(LinkedBlockingQueue<Boolean> eventQueue);
}
