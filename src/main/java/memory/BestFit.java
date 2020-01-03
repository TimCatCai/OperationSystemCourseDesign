package memory;

public class BestFit extends AbstractMallocStrategy {
    @Override
    public FreeNode malloc(long size) {
        FreeNode suitableNode = null;
        for(FreeNode freeNode: freeNodeList){
            if(freeNode.size > size) {
                if (freeNode.size - size <= Memory.MINUS_SIZE) {
                    suitableNode = freeNode;
                    freeNodeList.remove(freeNode);
                }else{
                    suitableNode = new FreeNode(freeNode.startAddress, size);
                    freeNode.startAddress += size;
                    freeNode.size -= size;
                }
                break;
            }
        }
        return suitableNode;
    }

    @Override
    public void free(long startAddress, long size) {
        // 找到合适的位置
        int i;
        for(i=0; i < freeNodeList.size() && freeNodeList.get(i).size < size;i++){

        }
        FreeNode before = null;
        if(i > 0){
            before = freeNodeList.get(i - 1);
        }
        FreeNode after = null;
        if(i < freeNodeList.size()){
            after = freeNodeList.get(i);
        }
        boolean linkedBefore = false;
        boolean linkedAfter = false;
        // 前相接
        if(before != null && startAddress == before.startAddress + before.size){
            linkedBefore = true;
        }
        // 后相接
        if(after != null && startAddress  + size == after.startAddress){
            linkedAfter = true;
        }

        // 前后相接
        if(linkedBefore && linkedAfter){
            before.size = size + after.size;
            freeNodeList.remove(i);
        }else if(linkedBefore){
            // 前相接，与前结点合并
            before.size += size;
        }else if(linkedAfter){
            // 后相接，与后结点合并
            after.startAddress = startAddress;
            after.size += size;
        }else{
            // 前后不相接,创建新的结点
            freeNodeList.add(i, new FreeNode(startAddress, size));
        }

    }
}
