package com.zspc.sentinel.config.pool;

import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;


@Slf4j
public class AsyncSendMessagePool {

    private static final AtomicInteger SEND_MESSAGE_POOL_NUMBER = new AtomicInteger(1);

    private final static ExecutorService ASYNC_SEND_MESSAGE_POOL = new ThreadPoolExecutor(
            20,
            60,
            10,
            TimeUnit.SECONDS,
            new LinkedBlockingQueue<>(1000),
            r -> new Thread(r, "ASYNC_SEND_MESSAGE_POOL" + SEND_MESSAGE_POOL_NUMBER.getAndIncrement()),
            new ThreadPoolExecutor.DiscardOldestPolicy()
    );

    public static Future<?> submitRunnable(Runnable runnable) {
        return ASYNC_SEND_MESSAGE_POOL.submit(runnable);
    }

    public static void shutDown() {
        ASYNC_SEND_MESSAGE_POOL.shutdown();
        try {
            boolean isStopSuccess = ASYNC_SEND_MESSAGE_POOL.awaitTermination(1, TimeUnit.MINUTES);
            if (isStopSuccess) {
                log.info("ASYNC_SEND_MESSAGE_POOL stop successfully");
            } else {
                log.info("Failed stop ASYNC_SEND_MESSAGE_POOL");
            }
        } catch (InterruptedException e) {
            log.info("An interrupt was encountered while stopping ASYNC_SEND_MESSAGE_POOL", e);
            Thread.currentThread().interrupt();
        }
    }
}
