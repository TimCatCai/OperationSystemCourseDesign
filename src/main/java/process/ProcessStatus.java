package process;

/**
 * 线程执行状态
 * @author TimCat
 * @version 0.1 2019/12/01
 */
public enum ProcessStatus {
    /**
     * 执行态
     */
    RUNNINNG,
    /**
     * 就绪态
     */
    READY,
    /**
     * 阻塞态
     */
    BLOCKED,
    /**
     * 执行完成
     */
    FINISH;

    @Override
    public String toString() {
        switch (this){
            case RUNNINNG:
                return "RUNNING";
            case READY:
                return "READY";
            case BLOCKED:
                return "BLOCKED";
            case FINISH:
                return "FINISH";
            default:
                return "Unknown";
        }
    }
}
