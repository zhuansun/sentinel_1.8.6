package com.zspc.sentinel.core;

import com.alibaba.csp.sentinel.datasource.ReadableDataSource;
import com.alibaba.csp.sentinel.datasource.nacos.NacosDataSource;
import com.alibaba.csp.sentinel.slots.block.degrade.DegradeRule;
import com.alibaba.csp.sentinel.slots.block.degrade.DegradeRuleManager;
import com.alibaba.csp.sentinel.slots.block.degrade.DegradeRuleProvider;
import com.alibaba.csp.sentinel.slots.block.flow.FlowRule;
import com.alibaba.csp.sentinel.slots.block.flow.FlowRuleManager;
import com.alibaba.csp.sentinel.util.AppNameUtil;
import com.alibaba.csp.sentinel.util.StringUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.alibaba.nacos.api.PropertyKeyConst;
import com.zspc.sentinel.commom.Constants;
import com.zspc.sentinel.config.SentinelClientInitParams;
import com.zspc.sentinel.exception.ZsSentinelException;
import com.zspc.sentinel.util.EnvUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import java.util.List;
import java.util.Properties;
import java.util.Timer;
import java.util.TimerTask;
//import com.dianping.cat.Cat;

public class ZsSentinelClient {

    protected Logger logger = LoggerFactory.getLogger(this.getClass());

    private String appName;

    private String env;

    private int ruleDelayLoadSeconds = 0;

    public ZsSentinelClient() {

    }

    public ZsSentinelClient(SentinelClientInitParams params) {
        this.appName = params.getAppName();
        this.env = params.getEnv();
        this.ruleDelayLoadSeconds = params.getRuleDelayLoadSeconds();
    }

    public String getEnv() {
        return env;
    }

    public void setEnv(String env) {
        this.env = env;
    }

    public int getRuleDelayLoadSeconds() {
        return ruleDelayLoadSeconds;
    }

    public void setRuleDelayLoadSeconds(int ruleDelayLoadSeconds) {
        this.ruleDelayLoadSeconds = ruleDelayLoadSeconds;
    }

    @PostConstruct
    public void init() {

//        Cat.getManager();//相当于饿汉式加载

        if (!DegradeRuleProvider.isInit()) {
            throw new ZsSentinelException("DegradeRuleProvider init error");
        }
        if (env == null) {
            env = System.getProperty("env");
        }

        if (env == null || env.isEmpty()) {
            throw new ZsSentinelException("env of NacosSentinelRuleListener is null");
        }
//        appName = System.getProperty(AppNameUtil.getAppName());
        appName = AppNameUtil.getAppName();
//        appName = System.getProperty("APP_NAME");
        if (appName == null || appName.isEmpty()) {
            throw new ZsSentinelException("project.name is null");
        }

        //load rules form nacos
        if (ruleDelayLoadSeconds <= 0) {
            if (StringUtil.isNotBlank(System.getProperty("ruleDelayLoadSeconds"))) {
                ruleDelayLoadSeconds = Integer.parseInt(System.getProperty("ruleDelayLoadSeconds"));
            }
        }
        if (ruleDelayLoadSeconds <= 0) {
            logger.info("load Sentinel rules from Nacos immediately.");
            loadRulesFromNacos();
        } else {
            logger.info(String.format("load Sentinel rules from Nacos after %s seconds", ruleDelayLoadSeconds));
            loadRulesFromNacos(ruleDelayLoadSeconds);
        }

    }


    private void loadRulesFromNacos() {
        logger.info("begin load Sentinel rules from Nacos.");
        String degradeDataId = String.format("%s%s", appName, Constants.nacos_degrade_data_id_postfix);
        String flowDataId = String.format("%s%s", appName, Constants.nacos_flow_data_id_postfix);
        String serverAddr = EnvUtil.getNacosServerAddr(env);
        String username = EnvUtil.getNacosUserName(env);
        String password = EnvUtil.getNacosPassword(env);
        Properties properties = new Properties();
        properties.put(PropertyKeyConst.SERVER_ADDR, serverAddr);
        properties.put(PropertyKeyConst.USERNAME, username);
        properties.put(PropertyKeyConst.PASSWORD, password);
        loadFlowRules(properties, flowDataId);
        loadDegradeRules(properties, degradeDataId);
        logger.info("load Sentinel rules from Nacos success.");
    }

    private void loadRulesFromNacos(long ruleDelayLoadSeconds) {
        Timer timer = new Timer("hello-sentinel-load-rules", true);
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                loadRulesFromNacos();
            }
        }, ruleDelayLoadSeconds * 1000L);
    }

    private void loadFlowRules(Properties properties, String flowDataId) {
        ReadableDataSource<String, List<FlowRule>> dataSource = new NacosDataSource<>(
                properties, Constants.nacos_sentinel_group_id, flowDataId,
                source -> JSON.parseObject(source, new TypeReference<List<FlowRule>>() {
                }));
        FlowRuleManager.register2Property(dataSource.getProperty());
    }

    private void loadDegradeRules(Properties properties, String degradeDataId) {
        ReadableDataSource<String, List<DegradeRule>> dataSource = new NacosDataSource<>(
                properties, Constants.nacos_sentinel_group_id, degradeDataId,
                source -> JSON.parseObject(source, new TypeReference<List<DegradeRule>>() {
                }));
        DegradeRuleManager.register2Property(dataSource.getProperty());
    }
}
