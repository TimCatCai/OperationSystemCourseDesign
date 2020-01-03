package process;

public interface IProcessesController extends Runnable{
    /**
     * 提交进程
     * @param pcb
     */
    void offer(IPcb pcb);

    /**
     * 获取进程
     * @return
     */
    IPcb take();

    /**
     * 进程结束
     * @param pcb
     */
    void processFinished(IPcb pcb);

    void shutdown();

    void reSchedule();
}
