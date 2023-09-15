package com.zspc.sentinel.exception;

/**
 * Created by zs on 20/3/24.
 */
public class ZsSentinelException extends RuntimeException {

    public ZsSentinelException(String msg) {
        super(msg);
    }

    public ZsSentinelException(String msg, Throwable throwable) {
        super(msg, throwable);
    }

    public ZsSentinelException(Throwable throwable) {
        super(throwable);
    }

}
