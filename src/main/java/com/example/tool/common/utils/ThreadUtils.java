package com.example.tool.common.utils;

import com.example.tool.common.model.MyHttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.*;

public class ThreadUtils {
    private static final ThreadPoolExecutor threadPoolExecutor = new ContextAwarePoolExecutor(30, 40, 30L
        , TimeUnit.SECONDS, new LinkedBlockingQueue<>(1000));

    private static ThreadUtils instance;
    private static Logger logger = LoggerFactory.getLogger(ThreadUtils.class);


    public static class ContextAwarePoolExecutor extends ThreadPoolExecutor {

        public static class ContextAwareCallable<T> implements Callable<T> {
            private Callable<T> task;
            //		private RequestAttributes context;
            private HttpServletRequest request;
            public ContextAwareCallable(Callable<T> task, ServletRequestAttributes context) {
                this.task = task;
                this.request = new MyHttpServletRequest(context.getRequest());
            }

            @Override
            public T call() throws Exception {
                if (request != null) {
                    MyRequestUtils.requestThread.set(request);
//				RequestContextHolder.setRequestAttributes(context);
                }
                try {
                    return task.call();
                } finally {
                    MyRequestUtils.requestThread.remove();
//				RequestContextHolder.resetRequestAttributes();
                }
            }
        }
        public static class ContextAwareRunnable implements Runnable {
            private Runnable task;
            //		private RequestAttributes context;
            private HttpServletRequest request;
            public ContextAwareRunnable(Runnable task, ServletRequestAttributes context) {
                this.task = task;
                this.request = new MyHttpServletRequest(context.getRequest());
            }

            @Override
            public void run() {
                if (request != null) {
                    MyRequestUtils.requestThread.set(request);
//				RequestContextHolder.setRequestAttributes(context);
                }
                try {
                    task.run();
                } finally {
                    MyRequestUtils.requestThread.remove();
//				RequestContextHolder.resetRequestAttributes();
                }
            }
        }
        public <E> ContextAwarePoolExecutor(int corePoolSize,
                                            int maximumPoolSize,
                                            long keepAliveTime,
                                            TimeUnit unit,
                                            BlockingQueue<Runnable> workQueue) {
            super(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue);
        }

        @Override
        public void execute(Runnable runnable){
            super.execute(new ContextAwareRunnable(runnable, (ServletRequestAttributes) RequestContextHolder.currentRequestAttributes()));
        }
        @Override
        public <T> Future<T> submit(Callable<T> task) {
            return super.submit(new ContextAwareCallable(task, (ServletRequestAttributes)RequestContextHolder.currentRequestAttributes()));
        }

    }
    public static void runAsync(Runnable runnable){
        threadPoolExecutor.execute(runnable);
    }
    /**
     * 懒加载，把在堆创建实例这个行为延迟到类的使用时
     * @return
     */
    public static ThreadUtils getInstance(){
        if(instance == null){
            synchronized (ThreadUtils.class){
                if(instance == null){
                    instance = new ThreadUtils();
                }
            }
        }
        return instance;
    }
    /**
     * 阻塞限制线程方法
     */
    public static class ChokeLimitThreadPool{
        private Semaphore semaphore;//最多同时运行的线程数量
        private CountDownLatch latch;//总执行线程数，用来实现阻塞
        public ChokeLimitThreadPool(Integer latchCount, Integer semaphoreCount) {
            latch = new CountDownLatch(latchCount);
            semaphore = new Semaphore(semaphoreCount);
        }
        public void run(RunThread runThread) {
            Runnable runnable = new Runnable() {
                @Override
                public void run() {
                    try {
                        semaphore.acquire(); // 获取permit
                        //执行方法
                        runThread.run();
                        latch.countDown();
                        semaphore.release(); // 释放permit
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }finally {
                    }
                }
            };
            threadPoolExecutor.execute(runnable);
//            new Thread(runnable).start();
        }
        public void choke() throws InterruptedException {
            latch.await();
        }
        public interface RunThread{
            void run() throws InterruptedException;
        }
    }

    public static class SimpleChokeLimitThreadPool{
        private CountDownLatch latch;//总执行线程数，用来实现阻塞
        public SimpleChokeLimitThreadPool(Integer latchCount) {
            latch = new CountDownLatch(latchCount);
        }
        public void run(RunThread runThread) {
            Runnable runnable = new Runnable() {
                @Override
                public void run() {
                    try {
                        //执行方法
                        runThread.run();
                        latch.countDown();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }finally {
                    }
                }
            };
            threadPoolExecutor.execute(runnable);
//            new Thread(runnable).start();
        }
        public void choke() throws InterruptedException {
            latch.await();
        }
        public long getLeaveCount() throws InterruptedException {
            return latch.getCount();
        }
        public interface RunThread{
            void run() throws InterruptedException;
        }
    }
    public static class FutureLimitThreadPool<T>{
        private Semaphore semaphore;//最多同时运行的线程数量
        private List<Future<T>> futureList = new ArrayList<>();
        public FutureLimitThreadPool(Integer semaphoreCount) {
            semaphore = new Semaphore(semaphoreCount);
        }
        public void run(RunThread<T> runThread) throws Exception{
            Callable<T> runnable = new Callable<T>() {
                @Override
                public T call() throws Exception {
                    try {
                        semaphore.acquire(); // 获取permit
                        //执行方法
                        T run = runThread.run();
                        semaphore.release(); // 释放permit
                        return run;
                    } finally {
                    }
                }
            };
            Future<T> submit = threadPoolExecutor.submit(runnable);
            futureList.add(submit);
//            new Thread(runnable).start();
        }

        /**
         * 可以按顺序返回List列表
         * @return
         * @throws InterruptedException
         * @throws ExecutionException
         */
        public List<T> choke() throws InterruptedException, ExecutionException {
            List<T> list = new ArrayList<>();
            for(Future<T> f:futureList){
                T t = f.get();
                list.add(t);
            }
            return list;
        }
        public interface RunThread<T>{
            T run() throws Exception;
        }
    }

    /**
     * 获得一个阻塞线程类
     * @param latchCount
     * @param semaphoreCount
     * @return
     */
    public ChokeLimitThreadPool chokeLimitThreadPool(Integer latchCount, Integer semaphoreCount){
        return new ChokeLimitThreadPool(latchCount, semaphoreCount);
    }
    /**
     * 获得一个阻塞线程类
     * @param latchCount
     * @return
     */
    public SimpleChokeLimitThreadPool simpleChokeLimitThreadPool(Integer latchCount){
        return new SimpleChokeLimitThreadPool(latchCount);
    }
    /**
     * 获得一个阻塞线程类
     * @param semaphoreCount
     * @param classes
     * @return
     */
    public <T> FutureLimitThreadPool<T> futureLimitThreadPool(Integer semaphoreCount, Class<T> classes){
        return new FutureLimitThreadPool<T>(semaphoreCount);
    }

    public static void main(String[] args) throws Exception {
        {
            Date beginTime = new Date();
            Integer count = 100;
            ChokeLimitThreadPool chokeLimitThreadPool = ThreadUtils.getInstance().chokeLimitThreadPool(count, 5);
            for(int i=0;i<100;i++){
                int finalI = i;
                chokeLimitThreadPool.run(new ChokeLimitThreadPool.RunThread() {
                    @Override
                    public void run() throws InterruptedException {
                        Thread.sleep(1000L);
                        logger.info(Thread.currentThread()+":"+ finalI);
                    }
                });
            }
            chokeLimitThreadPool.choke();
            logger.info("ChokeLimitThreadPool-costTime:{}ms", new Date().getTime() - beginTime.getTime());
        }
        {
            Date beginTime = new Date();
            Integer count = 100;
            FutureLimitThreadPool<Integer> chokeLimitThreadPool = ThreadUtils.getInstance().futureLimitThreadPool(5, Integer.class);
            for(int i=0;i<100;i++){
                int finalI = i;
                chokeLimitThreadPool.run(new FutureLimitThreadPool.RunThread() {
                    @Override
                    public Integer run() throws InterruptedException {
                        Thread.sleep(1000L);
                        logger.info(Thread.currentThread()+":"+ finalI);
                        return finalI;
                    }
                });
            }
            List<Integer> choke = chokeLimitThreadPool.choke();
            logger.info(choke.toString());
            logger.info("FutureLimitThreadPool-costTime:{}ms", new Date().getTime() - beginTime.getTime());
        }
        threadPoolExecutor.shutdown();

    }
}
