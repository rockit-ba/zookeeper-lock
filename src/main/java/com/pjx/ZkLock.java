package com.pjx;

import org.I0Itec.zkclient.IZkDataListener;
import org.I0Itec.zkclient.ZkClient;
import org.I0Itec.zkclient.exception.ZkNodeExistsException;

import java.util.concurrent.CountDownLatch;

public class ZkLock {

    private ZkClient zkClient = new ZkClient("192.168.1.23:2181");
    //锁节点
    private String zkLock = "/zk_lock";
    //并发工具
    private CountDownLatch countDownLatch = null;
    //获取锁,即创建节点
    public void lock(){

        try {
            //创建锁节点
            zkClient.createEphemeral(zkLock);
        }catch (Exception e){
            //精确点的异常应该是ZkNodeExistsException
            //所有创建失败的都会进入此逻辑
            //创建监听
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
            //订阅节点变化消息
            zkClient.subscribeDataChanges(zkLock,listener);

            countDownLatch = new CountDownLatch(1);
            try {
                //在节点未被删除之前都处于阻塞状态
                countDownLatch.await();
            } catch (InterruptedException interruptedException) {
                interruptedException.printStackTrace();
            }
            //当执行到此，说明监听已经使用，已经完成一轮操作，要清除监听
            zkClient.unsubscribeDataChanges(zkLock,listener);
            //上一次没抢到的节点此时已经释放，可以重新抢着创建，递归
            lock();
        }

    }

    //释放锁
    public void unlock(){
        if(zkClient != null){
            //当执行了close，临时节点救护自动被删除
            zkClient.close();
            System.out.println("释放锁");
        }
    }

}
