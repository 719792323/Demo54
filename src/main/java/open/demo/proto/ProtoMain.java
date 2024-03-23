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
        //json测试
        ObjectMapper mapper = new ObjectMapper();
        long t1 = System.currentTimeMillis();
        String json = mapper.writeValueAsString(ntripData);
        double len1 = json.getBytes().length;
        mapper.readValue(json, NtripData.class);
        long t2 = System.currentTimeMillis();
        NtripDataProto.NtripData ntripDataProto = NtripDataProto.NtripData.newBuilder()
                .setMesssageType(ntripData.getMessageType())
                .setSatelliteId(ntripData.getSatelliteId())
                .setPhaseCorrection(ntripData.getPhaseCorrection()).build();
        double len2 = ntripDataProto.toByteArray().length;
        NtripDataProto.NtripData.parseFrom(ntripDataProto.toByteArray());
        long t3 = System.currentTimeMillis();
        System.out.println(String.format("json长度:%s,proto长度:%s,压缩比:%s", len1, len2, len1 / len2));
        System.out.println(String.format("json耗时:%s,proto耗时:%s,耗时比:%s", t2 - t1, t3 - t2, (double) (t2 - t1) / (double) (t3 - t2)));
    }
}
