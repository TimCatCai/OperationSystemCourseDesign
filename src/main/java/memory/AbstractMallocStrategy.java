package memory;

import java.util.LinkedList;

public abstract class AbstractMallocStrategy {
    protected LinkedList<FreeNode> freeNodeList;

    public void setFreeNodeList(LinkedList<FreeNode> freeNodeList) {
        this.freeNodeList = freeNodeList;
    }

    public abstract FreeNode malloc(long size);

    public abstract void free(long startAddress, long size);
}
