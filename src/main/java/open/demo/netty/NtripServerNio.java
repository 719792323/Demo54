package open.demo.netty;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.util.CharsetUtil;
import lombok.extern.slf4j.Slf4j;
import open.demo.common.pojo.NtripData;
import open.demo.common.pojo.NtripDataProto;

public class NtripServerNio {
    public static void main(String[] args) throws Exception {
        EventLoopGroup bossGroup = new NioEventLoopGroup(1);
        EventLoopGroup workerGroup = new NioEventLoopGroup();
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
class JsonDecoder extends SimpleChannelInboundHandler {
    private static ObjectMapper mapper = new ObjectMapper();

    @Override
    protected void channelRead0(ChannelHandlerContext context, Object o) throws Exception {
        ByteBuf byteBuf = (ByteBuf) o;
        int len = byteBuf.readableBytes();
        byte[] bytes = new byte[len];
        byteBuf.readBytes(bytes, 0, len);
        NtripData ntripData = mapper.readValue(bytes, NtripData.class);
        log.info("ntripDataJson:{}", ntripData);
        context.channel().writeAndFlush(Unpooled.copiedBuffer("bye", CharsetUtil.UTF_8));
    }
}

@Slf4j
class ProtoDecoder extends SimpleChannelInboundHandler {

    @Override
    protected void channelRead0(ChannelHandlerContext context, Object o) throws Exception {
        ByteBuf byteBuf = (ByteBuf) o;
        int len = byteBuf.readableBytes();
        byte[] bytes = new byte[len];
        byteBuf.readBytes(bytes, 0, len);
        NtripDataProto.NtripData ntripData = NtripDataProto.NtripData.parseFrom(bytes);
        log.info("ntripDataProto:{}", ntripData);
        context.channel().writeAndFlush(Unpooled.copiedBuffer("bye", CharsetUtil.UTF_8));
    }
}