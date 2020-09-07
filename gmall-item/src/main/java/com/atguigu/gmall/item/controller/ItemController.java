package com.atguigu.gmall.item.controller;

import com.atguigu.gmall.common.bean.ResponseVo;
import com.atguigu.gmall.item.service.ItemService;
import com.atguigu.gmall.item.vo.ItemVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.io.IOException;
import java.util.concurrent.*;


//@RequestMapping("item")
@Controller
public class ItemController {

    @Autowired
    private ItemService itemService;

    @GetMapping("{skuId}.html")
    public String toItem(@PathVariable("skuId")Long skuId, Model model){
        ItemVo itemVo = itemService.loadData(skuId);
        model.addAttribute("itemVo",itemVo);
        return "item";
    }

    @GetMapping("{skuId}")
    @ResponseBody
    public ResponseVo<ItemVo> toItem(@PathVariable("skuId")Long skuId){
        ItemVo itemVo = itemService.loadData(skuId);
        return ResponseVo.ok(itemVo);
    }

    public static void main(String[] args) throws IOException {
        CompletableFuture.runAsync(() -> {
            System.out.println("初始化了一个没有返回结果集的子任务");
        });

        CompletableFuture.supplyAsync(() -> {
            System.out.println("初始化了一个有返回结果集的子任务");
//            int i = 1/0;
            return "hello CompletableFuture supplyAsync";
        }).thenApplyAsync(t -> {
            System.out.println("========thenApplyAsync B===========");
            System.out.println("上一个任务的返回结果是: " + t);
            return "hello thenApplyAsync B";
        }).thenApplyAsync(t -> {
            System.out.println("========thenApplyAsync C===========");
            System.out.println("上一个任务的返回结果是: " + t);
            return "hello thenApplyAsync C";
        }).thenAcceptAsync(t -> {
            System.out.println("========thenAcceptAsync D===========");
            System.out.println("上一个任务的返回结果是: " + t);
        }).thenRunAsync(() -> {
            System.out.println("========thenRunAsync E===========");
            System.out.println("上一个任务的返回结果是: ");
        });


                /*.whenCompleteAsync((t,u) -> { //上一个任务执行完就会执行该方法，不管任务有没有异常
            System.out.println("==============whenComplete=================");
            try {
                TimeUnit.SECONDS.sleep(2);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            System.out.println("t = " + t);//t是上一个任务的返回结果集
            System.out.println("u = " + u);//u 是上一个任务的异常信息
        }).exceptionally(t -> { //上一个任务出现异常时，才会执行该方法
            System.out.println("==============exceptionally=================");
            System.out.println("t = " + t);
            return "hello exceptionally!";
        });*/

//        System.out.println("==============main===============");

        System.in.read();
//        new ThreadPoolExecutor(3,5,60,TimeUnit.SECONDS,new ArrayBlockingQueue<>(10));

//        ExecutorService executorService = Executors.newSingleThreadExecutor();
//        ExecutorService executorService = Executors.newFixedThreadPool(3);
//        ExecutorService executorService = Executors.newCachedThreadPool();
      /*  for (int i = 0; i < 5; i++) {
            executorService.submit(() -> {
                System.out.println(Thread.currentThread().getName() + "执行了一个子任务");
            });
        }*/
       /* ScheduledExecutorService scheduledExecutorService = Executors.newScheduledThreadPool(2);
        scheduledExecutorService.scheduleAtFixedRate(() -> {
            System.out.println("这是一个定时任务: " + System.currentTimeMillis());
        },5,10,TimeUnit.SECONDS);*/
//        new MyThread().start();
//        new Thread(new MyRunnable()).start();
        /*new Thread(new Runnable() {
            @Override
            public void run() {
                System.out.println("Runnable匿名内部类方式实现多线程");
            }
        }).start();*/
     /*   new Thread(() -> {
            System.out.println("这是lamba表达式初始化一个线程");
        },"").start();*/
       /* FutureTask futureTask = new FutureTask<>(new MyCallable());
        new Thread(futureTask).start();
        try {
            //阻塞方式获取
            System.out.println(futureTask.get());
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }*/
    }
}

class MyThread extends Thread{
    @Override
    public void run() {
        System.out.println("这是继承Thread方式实现多线程");
    }
}

class MyRunnable implements Runnable{
    @Override
    public void run() {
        System.out.println("这是实现Runnable接口实现多线程");
    }
}

class MyCallable implements Callable{

    @Override
    public Object call() throws Exception {
        System.out.println("这是callable方式实现多线程");
        return "11111";
    }
}
//自定义拒绝策略
class MyReject implements RejectedExecutionHandler{

    @Override
    public void rejectedExecution(Runnable r, ThreadPoolExecutor executor) {

    }
}
