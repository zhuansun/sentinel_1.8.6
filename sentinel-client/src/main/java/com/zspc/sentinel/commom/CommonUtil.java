package com.zspc.sentinel.commom;

import com.alibaba.csp.sentinel.slots.block.BlockException;
import com.alibaba.csp.sentinel.slots.block.degrade.DegradeException;
import com.alibaba.csp.sentinel.slots.block.flow.FlowException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CommonUtil {

    protected static Logger logger = LoggerFactory.getLogger(CommonUtil.class);

    public static final ThreadLocal<String> DEFAULT_FLOW_RULE = new ThreadLocal<>();

    public static String getRuleType(BlockException e) {
        if (e instanceof FlowException) {
            return "Flow";
        } else if (e instanceof DegradeException) {
            return "Degrade";
        }
        return "Other";
    }
}
