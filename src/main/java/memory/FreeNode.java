package memory;

public class FreeNode {
    public long startAddress;
    /**
     * 单位kb
     */
    public long size;

    public FreeNode(long startAddress, long size) {
        this.startAddress = startAddress;
        this.size = size;
    }

    @Override
    public String toString() {
        return "FreeNode{" +
                "startAddress=" + startAddress +
                ", size=" + size +
                "}\n";
    }
}
