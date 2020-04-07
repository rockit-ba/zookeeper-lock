package com.pjx;

import org.I0Itec.zkclient.IZkDataListener;
import org.I0Itec.zkclient.ZkClient;
import org.I0Itec.zkclient.exception.ZkNodeExistsException;

import java.util.concurrent.CountDownLatch;

public class ZkLock {

    private ZkClient zkClient = new ZkClient("192.168.1.23:2181");

    private String zkLock = "/zk_lock";

    private CountDownLatch countDownLatch = null;
    //获取锁,即创建节点
    public void lock(){
        //如果不存在创建
        try {
            zkClient.createEphemeral(zkLock);
        }catch (Exception e){
            //精确点的异常应该是ZkNodeExistsException
            //否接监听此节点
            IZkDataListener listener = new IZkDataListener() {
                @Override
                public void handleDataChange(String s, Object o) throws Exception { }

                //如果节点删除了就不再阻塞
                @Override
                public void handleDataDeleted(String s) throws Exception {
                    if(countDownLatch != null){
                        countDownLatch.countDown();
                    }

                }
            };
            zkClient.subscribeDataChanges(zkLock,listener);
            //如果存在

            countDownLatch = new CountDownLatch(1);
            //证明被锁了，那么注册监听，阻塞等待
            try {
                countDownLatch.await();
            } catch (InterruptedException interruptedException) {
                interruptedException.printStackTrace();
            }

            //以完成操作，不再需要监听
            zkClient.unsubscribeDataChanges(zkLock,listener);
            //如果释放锁了立马再创建，抢着创建
            lock();
        }

    }

    //释放锁
    public void unlock(){
        if(zkClient != null){
            zkClient.close();
            System.out.println("释放锁");
        }
    }

}
