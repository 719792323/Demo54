package open.demo.proto;

import com.fasterxml.jackson.databind.ObjectMapper;
import open.demo.common.pojo.NtripData;
import open.demo.common.pojo.NtripDataProto;


public class ProtoMain {
    public static void main(String[] args) throws Exception {
        NtripData ntripData = new NtripData();
        ntripData.setMessageType(1301);
        ntripData.setSatelliteId(10);
        ntripData.setPhaseCorrection(0.5);
        ObjectMapper mapper = new ObjectMapper();
        String json = mapper.writeValueAsString(ntripData);
        double len1 = json.getBytes().length;
        NtripDataProto.NtripData ntripDataProto = NtripDataProto.NtripData.newBuilder()
                .setMesssageType(ntripData.getMessageType())
                .setSatelliteId(ntripData.getSatelliteId())
                .setPhaseCorrection(ntripData.getPhaseCorrection()).build();
        double len2 = ntripDataProto.toByteArray().length;
        System.out.println(String.format("原长度:%s,压缩长度:%s,压缩比:%s", len1, len2, len1 / len2));
    }
}
