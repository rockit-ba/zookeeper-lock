package com.pjx;

import java.text.SimpleDateFormat;
import java.util.Date;

public class Test{

    public static void main(String[] args) {
        //开启十个线程模拟十台不同的机器在生产id
        for (int i = 0; i < 10; i++) {
            new Thread(() ->{
                //创建我们定义的分布式锁
                ZkLock zkLock = new ZkLock();
                //加锁
                zkLock.lock();
                String id = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss").format(new Date());
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                System.out.println(id);
                //完成业务逻辑之后释放锁
                zkLock.unlock();
            }).start();
        }

        //没有加zookeeper锁的
//        for (int i = 0; i < 10; i++) {
//            new Thread(() ->{
//
//                String id = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss").format(new Date());
//                try {
//                    Thread.sleep(1000);
//                } catch (InterruptedException e) {
//                    e.printStackTrace();
//                }
//                System.out.println(id);
//
//            }).start();
//        }

    }



}
