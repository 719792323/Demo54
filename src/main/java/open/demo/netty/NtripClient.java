package open.demo.netty;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.unix.Buffer;
import lombok.extern.slf4j.Slf4j;
import open.demo.common.pojo.NtripData;
import open.demo.common.pojo.NtripDataProto;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.ReentrantLock;

@Slf4j
public class NtripClient {
    static volatile boolean stop = false;
    static AtomicInteger count = new AtomicInteger(0);
    static AtomicInteger next = new AtomicInteger(0);
    static Random random = new Random();
    static ObjectMapper mapper = new ObjectMapper();
    static List<Socket> sockets = new ArrayList<>();
    static List<ReentrantLock> locks = new ArrayList<>();

    static void writeAndRead(Socket socket, byte[] data, ByteBuf buffer) {
        try {
            buffer.writeInt(data.length);
            buffer.writeBytes(data);
            data = new byte[buffer.readableBytes()];
            buffer.readBytes(data);
            OutputStream outputStream = socket.getOutputStream();
            outputStream.write(data);
            outputStream.flush();
            InputStream inputStream = socket.getInputStream();
            byte bytes[] = new byte[1024];
            int len = inputStream.read(bytes);
            log.info("read:{}", new String(bytes, 0, len));
            count.incrementAndGet();
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

    //完成测试次数:3639271,请求延迟:60.654066666666665
    //完成测试次数:384161,请求延迟:6.40255
    //完成测试次数:3237188,请求延迟:53.953
    public static void main(String[] args) throws Exception {

        int socketSize = 50000;
        for (int i = 0; i < socketSize; i++) {
            sockets.add(new Socket("localhost", 8088));
            locks.add(new ReentrantLock());
        }
        int threadNums = 1000;
        int testTime = 60;
        Thread[] threads = new Thread[threadNums];
        for (int i = 0; i < threadNums; i++) {
            threads[i] = new Thread(() -> {
                ByteBuf buffer = Unpooled.buffer(1024);
                while (!stop) {
//                    int index = next.getAndIncrement() % socketSize;
                    int index = random.nextInt(socketSize);
                    if (locks.get(index).tryLock()) {
                        try {
                            Socket socket = sockets.get(index);
                            if (socket.isClosed()) {
                                log.info("reconnect");
                                socket = new Socket("localhost", 8088);
                                sockets.set(index, socket);
                            }
                            writeAndRead(sockets.get(index), getJsonData(), buffer);
//                            writeAndRead(sockets.get(index), getProtoData());
                            buffer.clear();
                        } catch (Exception e) {
                        } finally {
                            locks.get(index).unlock();
                        }
                    }
                }
            });
            threads[i].setDaemon(true);
            threads[i].start();
        }
        TimeUnit.SECONDS.sleep(testTime);
        stop = true;
        log.info("完成测试次数:{},请求延迟:{}", count, count.get() / (double) (testTime * 1000));
    }
}

