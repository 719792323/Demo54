package open.demo.netty;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.protobuf.InvalidProtocolBufferException;
import com.sun.xml.internal.messaging.saaj.util.ByteOutputStream;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import open.demo.common.pojo.NtripData;
import open.demo.common.pojo.NtripDataProto;
import org.apache.tomcat.util.http.fileupload.ByteArrayOutputStream;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
public class NtripServerBio {

    public static void main(String[] args) throws IOException {
        ServerSocket serverSocket = new ServerSocket(8088);
        ThreadGroup workThreadGroup = new ThreadGroup("workThreadGroup");
        AtomicInteger ids = new AtomicInteger(0);
        Thread bossThread = new Thread(() -> {
            Socket socket;
            while (true) {
                try {
                    socket = serverSocket.accept();
                    NtripBioTask task = new NtripBioTask(socket);
                    new Thread(workThreadGroup, task, String.format("worker-%s", ids.getAndIncrement())).start();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        }, "bossThread");
        bossThread.start();
        while (true) {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            log.info("active threads:{}", workThreadGroup.activeCount());
        }
    }

}

@Data
@AllArgsConstructor
@Slf4j
class NtripBioTask implements Runnable {
    private static ObjectMapper mapper = new ObjectMapper();
    private Socket socket;

    @Override
    public void run() {
        try {
            readAndWrite();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            log.info("socket close:{}", socket);
        }
    }

    private void readAndWrite() throws Exception {
        try (InputStream inputStream = socket.getInputStream()) {
            byte[] bytes = new byte[1024];
            while (true) {
                int len = inputStream.read(bytes);
                byte[] data = new byte[len];
                System.arraycopy(bytes, 0, data, 0, len);
                readJson(data);
//            readProto(data);
                try (OutputStream outputStream = socket.getOutputStream()) {
                    outputStream.write("bye".getBytes());
                    outputStream.flush();
                }
            }
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
