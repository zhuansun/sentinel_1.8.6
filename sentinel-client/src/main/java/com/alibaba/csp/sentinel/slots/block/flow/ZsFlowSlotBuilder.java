package com.alibaba.csp.sentinel.slots.block.flow;


public class ZsFlowSlotBuilder {

    public static FlowSlot build() {
        return new FlowSlot(new ZsFlowRuleChecker());
    }
}
