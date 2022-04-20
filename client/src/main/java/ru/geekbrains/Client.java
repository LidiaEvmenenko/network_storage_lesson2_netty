package ru.geekbrains;


import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.util.ReferenceCountUtil;

import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.Scanner;

public class Client
{
    public static void main(String[] args) {
        new Client().start();
    }

    public void start() {
        //Клиенту достаточно одного ThreadPool для обработки сообщений
        final NioEventLoopGroup group = new NioEventLoopGroup(1);
        try {
            Bootstrap bootstrap = new Bootstrap()
                    .group(group)
                    .channel(NioSocketChannel.class)
                    .option(ChannelOption.SO_KEEPALIVE, true)
                    .handler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel ch) {
                            ch.pipeline().addLast(
                                    new ChannelInboundHandlerAdapter() {
                                        @Override
                                        public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
                                            System.out.println("channelRead");
                                            final ByteBuf buf = (ByteBuf) msg;
                                            byte[] bytes = new byte[buf.readableBytes()];
                                            buf.readBytes(bytes);
                                            String s = new String(bytes);
                                            System.out.println(s);
                                            System.out.flush();
                                            ReferenceCountUtil.release(msg);
                                        }
                                    }
                            );
                        }
                    });

            System.out.println("Client started");

            Channel channel = bootstrap.connect("localhost", 9000).sync().channel();
            Scanner sc = new Scanner(System.in);
            ByteBuf msg;
            byte[] b = new byte[2];
            b[0] = 13;
            b[1] = 10;
            while (true) {
                String in = sc.next();
                if (in.equals("\\n")){
                    msg = Unpooled.wrappedBuffer(b);
                }else {
                    msg = Unpooled.wrappedBuffer(in.getBytes(StandardCharsets.UTF_8));
                }
                channel.write(msg);
                channel.flush();
                if (in.equals("0")) {
                    break;
                }
            }
            channel.closeFuture().sync();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            group.shutdownGracefully();
        }
    }
}
