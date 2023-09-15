package com.zspc.sentinel.config;

import lombok.Data;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@ConfigurationProperties(prefix = "zspc.sentinel")
public class SentinelClientInitParams implements InitializingBean {

    private String dashboardServer;

    private String env;

    private String appName;

    /**
     * 懒加载，就是项目启动后，等待多少秒开始加载流控规则
     * 主要是因为：项目启动初期，流量较大，可能会导致统计错误，所里不建议启动之后就立刻加载规则，容易误判
     */
    private int ruleDelayLoadSeconds = 0;

    private String appToken;

    private String nacosServer;

    private String username;

    private String password;


    @Override
    public void afterPropertiesSet() throws Exception {
        System.setProperty("project.name", this.appName);
        System.setProperty("csp.sentinel.dashboard.server", this.dashboardServer);
    }
}
