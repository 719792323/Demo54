package open.demo.netty;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.util.NettyRuntime;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import open.demo.common.pojo.NtripData;
import open.demo.common.pojo.NtripDataProto;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Slf4j
public class NtripServerBio {

    public static void main(String[] args) throws Exception {
        ServerSocket serverSocket = null;
        ExecutorService service = Executors.newFixedThreadPool(args.length > 0 ? Integer.parseInt(args[0]) : Runtime.getRuntime().availableProcessors() * 2);
        try {
            serverSocket = new ServerSocket(8088);
            while (true) {
                Socket socket = serverSocket.accept();
                NtripBioTask ntripBioTask = new NtripBioTask(socket);
                service.submit(ntripBioTask);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        } finally {
            if (serverSocket != null) {
                serverSocket.close();
            }
            service.shutdownNow();
        }
    }

}

@Data
@AllArgsConstructor
@Slf4j
class NtripBioTask implements Runnable {
    private static ObjectMapper mapper = new ObjectMapper();
    private Socket socket;
    private static final int maxWaitTime = 10;
    private static Random random = new Random();

    @Override
    public void run() {
        try {
            readAndWrite();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void readAndWrite() throws Exception {
        byte[] bytes = new byte[1024];
        InputStream inputStream = socket.getInputStream();
        OutputStream outputStream = socket.getOutputStream();
        ByteBuf buffer = Unpooled.buffer(1024);
        int count = 0;
        while (socket.isConnected() && !socket.isClosed() && count < 10) {
            int len = inputStream.read(bytes);
            if (len == -1) {
                count++;
                Thread.sleep(random.nextInt(10));
                continue;
//                return;
            }
            buffer.writeBytes(bytes, 0, len);
            buffer.markReaderIndex();
            if (buffer.readableBytes() < 4) {
                continue;
            }
            int payloadLength = buffer.readInt();
            //数据是否到齐
            if (buffer.readableBytes() < payloadLength) {
                buffer.resetReaderIndex();//重设readerIndex到markReaderIndex
                continue;
            }
            byte[] data = new byte[payloadLength];
            buffer.readBytes(data);
            buffer.discardReadBytes();
            readJson(data);
            outputStream.write("bye".getBytes());
            outputStream.flush();
            buffer.clear();
        }

    }

    private void readJson(byte[] data) throws Exception {
        NtripData ntripData = mapper.readValue(data, NtripData.class);
        log.info("ntripDataJson:{}", ntripData);
    }

    private void readProto(byte[] data) throws Exception {
        NtripDataProto.NtripData ntripData = NtripDataProto.NtripData.parseFrom(data);
        log.info("ntripDataProto:{}", ntripData);
    }
}
