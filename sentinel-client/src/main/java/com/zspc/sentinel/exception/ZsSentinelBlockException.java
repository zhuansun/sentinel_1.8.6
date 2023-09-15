package com.zspc.sentinel.exception;

/**
 * Created by zs on 20/3/25.
 */
public class ZsSentinelBlockException extends ZsSentinelException {
    public ZsSentinelBlockException(String msg) {
        super(msg);
    }

    public ZsSentinelBlockException(String msg, Throwable throwable) {
        super(msg, throwable);
    }

    public ZsSentinelBlockException(Throwable throwable) {
        super(throwable);
    }
}
