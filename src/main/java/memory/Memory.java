package memory;

import java.util.LinkedList;

public class Memory {
    /**
     * 分割最小不能小于5kb
     */
    public static long MINUS_SIZE = 5;
    private LinkedList<FreeNode> freeNodeList;
    private AbstractMallocStrategy mallocStrategy;
    public Memory(AbstractMallocStrategy mallocStrategy){
        freeNodeList = new LinkedList<>();
        // 初始位置为0，大小640kb
        freeNodeList.add(new FreeNode(0, 640));
        this.mallocStrategy = mallocStrategy;
        this.mallocStrategy.setFreeNodeList(freeNodeList);
    }

    public FreeNode malloc(long size){
        return mallocStrategy.malloc(size);
    }

    public void free(long startAddress, long size){
        mallocStrategy.free(startAddress, size);
    }

    public void setFreeNodeList(AbstractMallocStrategy mallocStrategy){
        this.mallocStrategy = mallocStrategy;
    }

    @Override
    public String toString() {
        return "Memory{\n"+freeNodeList.toString() +"\n}\n";
    }
}
