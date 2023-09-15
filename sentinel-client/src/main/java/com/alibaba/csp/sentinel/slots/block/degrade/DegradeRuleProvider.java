package com.alibaba.csp.sentinel.slots.block.degrade;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Field;
import java.util.Collection;
import java.util.Map;
import java.util.Set;

public class DegradeRuleProvider {

    private static Logger logger = LoggerFactory.getLogger(DegradeRuleProvider.class);

    private static Map<String, Set<DegradeRule>> degradeRules;

    static {
        try {
            // 版本不同时需要修改
            Field field = DegradeRuleManager.class.getDeclaredField("ruleMap");
            field.setAccessible(true);
            degradeRules = (Map<String, Set<DegradeRule>>) field.get(DegradeRuleManager.class);
        } catch (Exception e) {
            logger.error("get degradeRules from DegradeRuleManager.class error.", e);
        }
    }

    public static boolean isInit() {
        if (degradeRules==null) {
            return false;
        }
        return true;
    }

    public static Collection<DegradeRule> apply(String resourceName) {
        return degradeRules.get(resourceName);
    }
}
