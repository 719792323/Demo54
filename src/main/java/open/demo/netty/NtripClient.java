package open.demo.netty;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import open.demo.common.pojo.NtripData;
import open.demo.common.pojo.NtripDataProto;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

@Slf4j
public class NtripClient {
    static volatile boolean stop = false;
    static AtomicLong count = new AtomicLong(0);
    static Random random = new Random();
    static ObjectMapper mapper = new ObjectMapper();

    static List<Socket> sockets = new ArrayList<>();

    static void writeAndRead(byte[] data) {
        try {
            Socket socket = new Socket("192.168.0.44", 8088);
            OutputStream outputStream = socket.getOutputStream();
            outputStream.write(data);
            outputStream.flush();
            InputStream inputStream = socket.getInputStream();
            byte bytes[] = new byte[1024];
            int len = inputStream.read(bytes);
            log.info("read:{}", new String(bytes, 0, len));
            count.incrementAndGet();
            if (random.nextInt() % 2 == 0) {
                socket.close();
            } else {
                sockets.add(socket);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    static NtripData getData() {
        NtripData ntripData = new NtripData();
        ntripData.setMessageType(1001);
        ntripData.setSatelliteId(random.nextInt());
        ntripData.setPhaseCorrection(random.nextDouble());
        return ntripData;
    }

    static byte[] getJsonData() {
        NtripData ntripData = getData();
        byte[] bytes = null;
        try {
            bytes = mapper.writeValueAsString(ntripData).getBytes();
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
        return bytes;
    }

    static byte[] getProtoData() {
        NtripData ntripData = getData();
        NtripDataProto.NtripData ntripDataProto = NtripDataProto.NtripData.newBuilder()
                .setMesssageType(ntripData.getMessageType())
                .setSatelliteId(ntripData.getSatelliteId())
                .setPhaseCorrection(ntripData.getPhaseCorrection()).build();
        return ntripDataProto.toByteArray();
    }

    //bio服务端，线程数：50，序列化：json，完成测试次数:55-60w,请求延迟:9.5s左右
    //bio服务端，线程数：100，序列化：json，完成测试次数:55-60w,请求延迟:9.5s左右
    //bio服务端，线程数: 50，序列化：proto，完成测试次数:57-63w,请求延迟:9.5s左右
    //bio服务端，线程数: 100，序列化：proto，完成测试次数:57-63w,请求延迟:9.5s左右
    //nio服务端，线程数：50，序列化：json，完成测试次数:63w,请求延迟:10.44左右
    //nio服务端，线程数：100，序列化：json，完成测试次数：63w，请求延迟：10.56左右
    //nio服务端，线程数：50，序列化：proto，完成测试次数:63w,请求延迟:10.44左右
    public static void main(String[] args) throws InterruptedException {
        int threadNums = 10;
        int testTime = 60;
        Thread[] threads = new Thread[threadNums];
        for (int i = 0; i < threadNums; i++) {
            threads[i] = new Thread(() -> {
                while (!stop) {
                    writeAndRead(getJsonData());
//                    writeAndRead(getProtoData());
                }
            });
            threads[i].setDaemon(true);
            threads[i].start();
        }
        Thread clean = new Thread(() -> {
            Iterator<Socket> iterator = sockets.iterator();
            while (iterator.hasNext()) {
                Socket socket = iterator.next();
                if (random.nextInt() % 2 == 0) {
                    try {
                        socket.close();
                        iterator.remove();
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }
            }
        });
        clean.setDaemon(true);
        clean.start();
        TimeUnit.SECONDS.sleep(testTime);
        stop = true;
        log.info("完成测试次数:{},请求延迟:{}", count, count.get() / (double) (testTime * 1000));
    }
}

