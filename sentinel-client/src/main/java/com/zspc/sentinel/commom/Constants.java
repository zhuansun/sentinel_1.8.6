package com.zspc.sentinel.commom;


public class Constants {

    public static final String default_flow_rule_resource = "default_flow_rule_resource";

    public static final String nacos_sentinel_group_id = "zspc_sentinel_group";

    public static final String nacos_degrade_data_id_postfix = "_sentinel_degrade_rules";

    public static final String nacos_flow_data_id_postfix = "_sentinel_flow_rules";

    /**
     * 默认的降级限流规则
     */
    public static final String DEFAULT_FLOW_RULE_URL_IN = "default_flow_rule_url_in";

    public static final String DEFAULT_FLOW_RULE_SQL_OUT = "default_flow_rule_sql_out";

    public static final String DEFAULT_FLOW_RULE_FEIGN_OUT = "default_flow_rule_feign_out";

    public static final String DEFAULT_FLOW_RULE_URL_OUT = "default_flow_rule_url_out";

}
