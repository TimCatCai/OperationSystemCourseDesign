package utils;

import jdk.management.resource.ResourceRequest;
import org.apache.commons.lang3.StringUtils;
import process.Pcb;

import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

public class DebugUtils {
    // 将所有行调成相同长度（以最长一行为标准）
    public static String modifyToSameLength(String target){
        String [] lines = StringUtils.split(target, "\n");

        int maxLen = Stream.of(lines).map(line -> line.toCharArray().length).max(Integer::compareTo).get();
        for(int i = 0; i < lines.length;i++){
            lines[i] = String.format("%-" + maxLen + "s",lines[i]);
        }
        return StringUtils.join(lines, "\n");
    }

    // 将两个多行字符串并行输出
    public static String combineInOneLine(String a, String b, String interval){
        String [] aSplit = StringUtils.split(a, "\n");
        String [] bSplit = StringUtils.split(b, "\n");
        StringBuilder result = new StringBuilder();
        int i;
        for(i = 0; i < aSplit.length && i < bSplit.length; i++){
            result.append(aSplit[i]);
            result.append(interval);
            result.append(bSplit[i]);
            result.append("\n");
        }

        for(int j = i; j < aSplit.length; j++){
            result.append(aSplit[j]);
            result.append("\n");
        }


        for(int j = i; j < bSplit.length; j++) {
            result.append(bSplit[j]);
            result.append("\n");
        }
        return result.toString();
    }

    public static String  log(Pcb pcbRequest, List<Pcb> list){
        StringBuilder info = new StringBuilder("[");
        info.append(pcbRequest.getProcessId());
        info.append(",");

        for(Pcb pcb:list){
            info.append(pcb.getProcessId());
            info.append(",");
        }
        info.append("]");
        return info.toString();
    }

    public static String  log(List<Pcb> list){
        StringBuilder info = new StringBuilder("[");
        for(Pcb pcb:list){
            info.append(pcb.getProcessId());
            info.append(",");
        }
        info.append("]");
        return info.toString();
    }
    public static String  log(List<ResourceRequest> list, int type){
        StringBuilder info = new StringBuilder("[");
        for(ResourceRequest resourceRequest: list){
            info.append(resourceRequest.toString());
            info.append(",");
        }
        info.append("]");
        return info.toString();
    }

}
