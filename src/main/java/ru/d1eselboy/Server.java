package ru.d1eselboy;

/**
 * Created by ermolaev on 22/12/16.
 */

import io.netty.bootstrap.Bootstrap;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.channel.*;
import io.netty.channel.epoll.EpollDomainSocketChannel;
import io.netty.channel.epoll.EpollEventLoopGroup;
import io.netty.channel.epoll.EpollServerDomainSocketChannel;
import io.netty.channel.unix.DomainSocketAddress;

import java.io.File;
import java.util.Arrays;

/**
 * @author ermolaev
 */

//Сделать в 2 треда

public class Server {
    public static void main(String[] args) throws Exception {
        final ServerBootstrap bootstrap = new ServerBootstrap();
        File socketFile = new File("/usr/echo.sock");
        EventLoopGroup serverBossEventLoopGroup = new EpollEventLoopGroup();
        EventLoopGroup serverWorkerEventLoopGroup = new EpollEventLoopGroup();
        bootstrap.group(serverBossEventLoopGroup, serverWorkerEventLoopGroup)
            .localAddress(new DomainSocketAddress(socketFile))
            .channel(EpollServerDomainSocketChannel.class)
            .childHandler(
                new ChannelInitializer<Channel>() {
                    @Override
                    protected void initChannel(final Channel channel) throws Exception {
                        channel.pipeline().addLast(
                            new ChannelInboundHandlerAdapter() {
                                @Override
                                public void channelActive(final ChannelHandlerContext ctx) throws Exception {
                                    final ByteBuf buff = ctx.alloc().buffer();
                                    buff.writeBytes("Socket read test successfully completed".getBytes());
                                    ctx.writeAndFlush(buff).addListeners((ChannelFutureListener) future -> {
                                        future.channel().close();
                                        future.channel().parent().close();
                                    });
                                }
                            }
                        );
                    }
                }
            );
        final ChannelFuture serverFuture = bootstrap.bind().sync();

        final Bootstrap bootstrapClient = new Bootstrap();
        EventLoopGroup clientEventLoop = new EpollEventLoopGroup();
        bootstrapClient.group(clientEventLoop)
            .channel(EpollDomainSocketChannel.class)
            .handler(new ChannelInitializer<Channel>() {
                         @Override
                         protected void initChannel(final Channel channel) throws Exception {
                             channel.pipeline().addLast(
                                 new ChannelInboundHandlerAdapter() {
                                     @Override
                                     public void channelRead(final ChannelHandlerContext ctx, final Object msg) throws Exception {
                                         final ByteBuf buff = (ByteBuf) msg;
                                         while (true) {
                                             if (buff.isReadable()) {
                                                 byte[] bytes = new byte[buff.readableBytes()];
                                                 buff.getBytes(0, bytes);
                                                 System.out.println(new String(bytes));
                                             }
                                             buff.clear();
                                         }
                                     }

                                     @Override
                                     public void exceptionCaught(final ChannelHandlerContext ctx, final Throwable cause) throws
                                         Exception {
                                         System.err.println("Error occur when reading from Unix domain socket: " + cause.getMessage());
                                         System.err.println("Trace: " + Arrays.toString(cause.getStackTrace()));
                                         ctx.close();
                                     }
                                 }
                             );
                         }
                     }
            );
        final ChannelFuture clientFuture = bootstrapClient.connect(new DomainSocketAddress(socketFile)).sync();

        clientFuture.channel().closeFuture().sync();
        serverFuture.channel().closeFuture().sync();
        serverBossEventLoopGroup.shutdownGracefully();
        serverWorkerEventLoopGroup.shutdownGracefully();
        clientEventLoop.shutdownGracefully();
    }
}