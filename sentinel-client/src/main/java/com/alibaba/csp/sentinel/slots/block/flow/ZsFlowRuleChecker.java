package com.alibaba.csp.sentinel.slots.block.flow;

import com.alibaba.csp.sentinel.context.Context;
import com.alibaba.csp.sentinel.node.DefaultNode;
import com.alibaba.csp.sentinel.slotchain.ResourceWrapper;
import com.alibaba.csp.sentinel.slots.block.BlockException;
import com.alibaba.csp.sentinel.util.function.Function;
import com.zspc.sentinel.commom.CommonUtil;
import com.zspc.sentinel.config.ApplicationContextProvider;
import com.zspc.sentinel.helper.SendMessageHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.concurrent.atomic.AtomicLong;


public class ZsFlowRuleChecker extends FlowRuleChecker {

    private static Logger logger = LoggerFactory.getLogger(ZsFlowRuleChecker.class);

    private static final String flow_rule_degrade_rule_prefix = "FR_";

    private AtomicLong highRTPassCount = new AtomicLong(0);

    private AtomicLong highThreadPassCount = new AtomicLong(0);

    private SendMessageHelper sendMessageHelper = ApplicationContextProvider.getBean(SendMessageHelper.class);

    @Override
    public void checkFlow(Function<String, Collection<FlowRule>> ruleProvider, ResourceWrapper resource, Context context, DefaultNode node, int count, boolean prioritized) throws BlockException {

        if (ruleProvider == null || resource == null) {
            return;
        }
        // 获取当前资源是否有限流、降级规则
        Collection<FlowRule> specificRules = ruleProvider.apply(resource.getName());

        if (specificRules != null && !specificRules.isEmpty()) {

            // 假如有专门的限流降级规则，则执行相应的限流、降级规则
            passCheckSpecific(specificRules, resource, context, node, count, prioritized);

        } else {
            // 否则执行默认的降级限流规则
            passCheckDefault(ruleProvider, resource, context, node, count, prioritized);
        }
    }

    /**
     * 走特殊制定的流控规则
     */
    private void passCheckSpecific(Collection<FlowRule> rules, ResourceWrapper resource, Context context, DefaultNode node, int count, boolean prioritized) throws FlowException {

        if (rules != null) {
            for (FlowRule rule : rules) {
                if (!canPassCheck(rule, context, node, count, prioritized)) {
                    throw new FlowException(rule.getLimitApp(), rule);
                }
            }
        }

//        FlowRule triggeredRule = null;
//        for (FlowRule rule : rules) {
//            if (!canPassCheck(rule, context, node, count, prioritized)) {
//                triggeredRule = rule;
//                break;
//            }
//        }
//
//        if (triggeredRule == null) {
//            return;
//        }
//
//        Collection<DegradeRule> fRDegradeRules = DegradeRuleProvider.apply(flow_rule_degrade_rule_prefix + resource.getName());
//
//        if (fRDegradeRules != null && !fRDegradeRules.isEmpty()) {
//            furtherCheckBySpecificFRDegradeRule(triggeredRule, fRDegradeRules, resource, node);
//        } else {
//            furtherCheckByAvgThread(triggeredRule, node, resource);
//        }
    }


    /**
     * 走默认的流控规则
     */
    private void passCheckDefault(Function<String, Collection<FlowRule>> ruleProvider, ResourceWrapper resource, Context context, DefaultNode node, int count, boolean prioritized) throws FlowException {

        // 获取当前上下文中默认的降级限流规则
        String defaultFlowRuleName = CommonUtil.DEFAULT_FLOW_RULE.get();
        if (null == defaultFlowRuleName) {
            return;
        }
        // 获取相应的降级限流规则，上下文中虽然放的是规则但实际上应该是默认的资源名称
        Collection<FlowRule> defaultRules = ruleProvider.apply(defaultFlowRuleName);
        if (defaultRules == null || defaultRules.isEmpty()) {
            return;
        }
        FlowRule rule = defaultRules.iterator().next();

        // 判断是否限流
        if (!canPassCheck(rule, context, node, count, prioritized)) {
            throw new FlowException(rule.getLimitApp(), rule);
        }


//        // 获取当前上下文中默认的降级限流规则
//        String defaultFlowRuleName = CommonUtil.DEFAULT_FLOW_RULE.get();
//        if (null == defaultFlowRuleName) {
//            return;
//        }
//        // 获取相应的降级限流规则，上下文中虽然放的是规则但实际上应该是默认的资源名称
//        Collection<FlowRule> defaultRules = ruleProvider.apply(defaultFlowRuleName);
//        if (defaultRules == null || defaultRules.isEmpty()) {
//            return;
//        }
//        FlowRule rule = defaultRules.iterator().next();
//
//        // 判断是否限流
//        if (canPassCheck(rule, context, node, count, prioritized)) {
//            return;
//        }
//         求平均线程
//        furtherCheckByAvgThread(rule, node, resource);
    }


//
//    private void furtherCheckByAvgThread(FlowRule rule, DefaultNode node, ResourceWrapper resource) throws FlowException {
//
//        ClusterNode clusterNode = node.getClusterNode();
//
//        if (clusterNode == null) {
//            throw new FlowException(rule.getLimitApp(), rule);
//        }
//
//        if (rule.getGrade() != RuleConstant.FLOW_GRADE_THREAD) {
//            throw new FlowException(rule.getLimitApp(), rule);
//        }
//
////        long avgThread = clusterNode.avgThread();
//        long avgThread = clusterNode.avgThread(5);
//        if (avgThread < rule.getCount()) {
//            highThreadPassCount.set(0);
//            // 发送丰声消息
//            logger.warn("SentinelBlock Attempt, trigger_flow_low_avgThread, avgThread={},  ={}", avgThread, resource.getName());
//            return;
//        }
//
//        if (highThreadPassCount.incrementAndGet() < 5) {
//            logger.warn("SentinelBlock Attempt, trigger_flow_few_high_avgThread, avgThread={}, resource={}", avgThread, resource.getName());
//            return;
//        }
//
//        throw new FlowException(rule.getLimitApp(), rule);
//    }


//
//    private void furtherCheckBySpecificFRDegradeRule(FlowRule triggeredRule, Collection<DegradeRule> fRDegradeRules, ResourceWrapper resource, DefaultNode node) throws FlowException {
//
//        ClusterNode clusterNode = node.getClusterNode();
//        if (clusterNode == null) {
//            throw new FlowException(triggeredRule.getLimitApp(), triggeredRule);
//        }
//
//        DegradeRule fRDegradeRule = fRDegradeRules.iterator().next();
//        if (RuleConstant.DEGRADE_GRADE_RT != fRDegradeRule.getGrade()) {
//            throw new FlowException(triggeredRule.getLimitApp(), triggeredRule);
//        }
//
//        double rt = clusterNode.avgRt();
//        //triggered flow but RT is low
//        if (rt < fRDegradeRule.getCount()) {
//            highRTPassCount.set(0);
//            logger.warn("SentinelBlock Attempt, trigger_flow_low_rt {}", resource.getName());
//            return;
//        }
//        //trigger flow, rt, but count is lower than 5.
//        if (highRTPassCount.incrementAndGet() < 5) {
//            logger.warn("SentinelBlock Attempt, trigger_flow_few_high_rt {}", resource.getName());
//            return;
//        }
//        //trigger flow, rt, count
//        throw new FlowException(triggeredRule.getLimitApp(), triggeredRule);
//    }

}
