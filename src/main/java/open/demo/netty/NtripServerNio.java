package open.demo.netty;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.protobuf.InvalidProtocolBufferException;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.ByteToMessageDecoder;
import io.netty.util.CharsetUtil;

import lombok.extern.slf4j.Slf4j;
import open.demo.common.pojo.NtripData;
import open.demo.common.pojo.NtripDataProto;

import java.io.IOException;
import java.util.List;

public class NtripServerNio {
    public static void main(String[] args) throws Exception {
        EventLoopGroup bossGroup = new NioEventLoopGroup(1);
        EventLoopGroup workerGroup = new NioEventLoopGroup(args.length >= 1 ? Integer.parseInt(args[0]) : 0);
        ServerBootstrap serverBootstrap = new ServerBootstrap()
                .group(bossGroup, workerGroup)
                .channel(NioServerSocketChannel.class)
                .option(ChannelOption.SO_BACKLOG, 128)
                .childOption(ChannelOption.SO_KEEPALIVE, true)
                .childHandler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel socketChannel) throws Exception {
                        socketChannel.pipeline().addLast(new JsonDecoder());
//                        socketChannel.pipeline().addLast(new ProtoDecoder());
                    }
                });

        serverBootstrap.bind(8088).sync();

    }
}

@Slf4j
abstract class AbstractDecoder extends ByteToMessageDecoder {

    @Override
    protected void decode(ChannelHandlerContext context, ByteBuf in, List<Object> list) {
        //长度信息是否可读
        int availableBytes = in.readableBytes();
        if (availableBytes < 4) {
            return;
        }

        in.markReaderIndex();
        int payloadLength = in.readInt();
        //数据是否到齐
        if (in.readableBytes() < payloadLength) {
            in.resetReaderIndex();//重设readerIndex到markReaderIndex
            return;
        }

        byte[] payload = new byte[payloadLength];
        in.readBytes(payload);
        doRead(payload);
        context.channel().writeAndFlush(Unpooled.wrappedBuffer("bye".getBytes()));
    }

    protected abstract void doRead(byte[] data);
}


@Slf4j
class JsonDecoder extends AbstractDecoder {
    private static ObjectMapper mapper = new ObjectMapper();


    @Override
    protected void doRead(byte[] data) {
        NtripData ntripData = null;
        try {
            ntripData = mapper.readValue(data, NtripData.class);
            log.info("ntripDataJson:{}", ntripData);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}

@Slf4j
class ProtoDecoder extends AbstractDecoder {

    @Override
    protected void doRead(byte[] data) {
        NtripDataProto.NtripData ntripData = null;
        try {
            ntripData = NtripDataProto.NtripData.parseFrom(data);
            log.info("ntripDataProto:{}", ntripData);
        } catch (InvalidProtocolBufferException e) {
            throw new RuntimeException(e);
        }
    }
}