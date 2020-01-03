package memory;

import process.Pcb;

public class MemoryRequest {
    public boolean isRequest;
    public Pcb pcb;
    public long size;

    public MemoryRequest(Pcb pcb, boolean isRequest, long size) {
        this.pcb = pcb;
        this.isRequest = isRequest;
        this.size = size;
    }

    public Pcb getPcb() {
        return pcb;
    }
}
