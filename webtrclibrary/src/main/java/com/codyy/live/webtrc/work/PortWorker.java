package com.codyy.live.webtrc.work;

import android.content.Context;
import androidx.annotation.NonNull;

import androidx.work.Data;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketTimeoutException;

/**
 * 端口检测任务
 * created by lijian on 2019.07.24
 */
public class PortWorker extends Worker {
    public PortWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
    }

    @NonNull
    @Override
    public Result doWork() {
        DatagramSocket socket = null;
        Result result;
        try {
            /*
             * 向服务器端发送数据
             */
            // 1.定义服务器的地址、端口号、数据
            InetAddress address = InetAddress.getByName("255.255.255.255");
            int port = getInputData().getInt("port",52807);
            byte[] data = "ping".getBytes();
            // 2.创建数据报，包含发送的数据信息
            DatagramPacket packet = new DatagramPacket(data, data.length, address, port);
            // 3.创建DatagramSocket对象
            socket = new DatagramSocket();
            socket.setSoTimeout(1000);
            // 4.向服务器端发送数据报
            socket.send(packet);

            /*
             * 接收服务器端响应的数据
             */
            // 1.创建数据报，用于接收服务器端响应的数据
            byte[] data2 = new byte[1024];
            DatagramPacket packet2 = new DatagramPacket(data2, data2.length);
            // 2.接收服务器响应的数据
            socket.receive(packet2);
            // 3.读取数据
            String reply = new String(data2, 0, packet2.getLength());
            Data output = new Data.Builder()
                    .putString("port", reply)
                    .putString("ip", packet2.getAddress().getHostAddress()).build();

            // 4.关闭资源
//            socket.close();
            result = Result.success(output);
        } catch (Exception e) {
            if(e instanceof SocketTimeoutException){
                result = Result.failure(new Data.Builder()
                        .putString("exception","SocketTimeoutException").build());
            }else{
                result = Result.failure(new Data.Builder()
                        .putString("exception","NetWorkException").build());
            }

        } finally {
            if (socket != null) {
                socket.close();
            }
        }

        return result;
    }
}
