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
    static Random random = new Random();
    static ObjectMapper mapper = new ObjectMapper();
    static String host = "localhost";
    static int port = 8088;
    static int threadNums = 200;
    static int testTime = 60;

    static void writeAndRead(Socket socket, byte[] data, ByteBuf buffer) {
        try {
            if (socket.isConnected() && !socket.isClosed()) {
                buffer.writeInt(data.length);
                buffer.writeBytes(data);
                data = new byte[buffer.readableBytes()];
                buffer.readBytes(data);
                OutputStream outputStream = socket.getOutputStream();
                outputStream.write(data);
                InputStream inputStream = socket.getInputStream();
                byte bytes[] = new byte[1024];
                int len = inputStream.read(bytes);
                log.info("read:{}", new String(bytes, 0, len));
                count.incrementAndGet();
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    static NtripData getData() {
        NtripData ntripData = new NtripData();
        ntripData.setMessageType(1001);
        ntripData.setSatelliteId(random.nextInt());
        ntripData.setPhaseCorrection(random.nextDouble());
        return ntripData;
    }

    static byte[] getJsonData() throws Exception {
        NtripData ntripData = getData();
        byte[] bytes = mapper.writeValueAsString(ntripData).getBytes();
        return bytes;
    }

    static byte[] getProtoData() throws Exception {
        NtripData ntripData = getData();
        NtripDataProto.NtripData ntripDataProto = NtripDataProto.NtripData.newBuilder()
                .setMesssageType(ntripData.getMessageType())
                .setSatelliteId(ntripData.getSatelliteId())
                .setPhaseCorrection(ntripData.getPhaseCorrection()).build();
        return ntripDataProto.toByteArray();
    }

    static Socket getSocket() {
        while (true) {
            try {
                Socket socket = new Socket(host, port);
                return socket;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    static void socketClient() throws Exception {
        Thread[] threads = new Thread[threadNums];
        for (int i = 0; i < threadNums; i++) {
            threads[i] = new Thread(() -> {
                ByteBuf buffer = Unpooled.buffer(1024);
                Socket socket = getSocket();
                while (!stop) {
                    try {
                        writeAndRead(socket, getJsonData(), buffer);
                        buffer.clear();
                        if (random.nextInt(100) > 95) {
                            socket.close();
                            socket = getSocket();
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        try {
                            socket.close();
                            socket = getSocket();
                        } catch (IOException ex) {
                            log.info("reconnect");
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

    static void nioClient() {

    }

    //完成测试次数:2623951,请求延迟:43.73245
    //完成测试次数:2402152,请求延迟:40.035583333333335
    public static void main(String[] args) throws Exception {
        socketClient();
    }
}

