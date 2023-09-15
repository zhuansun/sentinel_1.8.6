package com.zspc.sentinel.interceptor;

import com.alibaba.csp.sentinel.Entry;
import com.alibaba.csp.sentinel.EntryType;
import com.alibaba.csp.sentinel.SphU;
import com.alibaba.csp.sentinel.context.ContextUtil;
import com.alibaba.csp.sentinel.slots.block.BlockException;
import com.zspc.sentinel.commom.CommonUtil;
import com.zspc.sentinel.commom.Constants;
import org.apache.ibatis.executor.Executor;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.plugin.*;
import org.apache.ibatis.session.ResultHandler;
import org.apache.ibatis.session.RowBounds;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Properties;
//import com.dianping.cat.message.Message;

@Intercepts({
        @Signature(args = {MappedStatement.class, Object.class}, method = "update", type = Executor.class),
        @Signature(args = {MappedStatement.class, Object.class, RowBounds.class,
                ResultHandler.class}, method = "query", type = Executor.class)})
public class SentinelMybatisInterceptor implements Interceptor {

    protected Logger logger = LoggerFactory.getLogger(this.getClass());

    @Override
    public Object intercept(Invocation invocation) throws Throwable {

        MappedStatement mappedStatement = (MappedStatement) invocation.getArgs()[0];
        String resourceName = getClassMethod(mappedStatement.getId());
        Entry entry = null;

        Object returnObj;
        try {
            ContextUtil.enter("SQL.OUT");
            CommonUtil.DEFAULT_FLOW_RULE.set(Constants.DEFAULT_FLOW_RULE_SQL_OUT);
            entry = SphU.entry(resourceName, EntryType.OUT);
            returnObj = invocation.proceed();
        } catch (BlockException e) {
            String blockInfo = String.format("%s:%s", CommonUtil.getRuleType(e), e.getRule().getResource());
            logger.error(String.format("SentinelBlock %s %s %s", "Sentinel.SQL.OUT", resourceName, blockInfo));
//            AsyncSendMessagePool.submitRunnable(() -> {
//                SendMessageSingle.sendMessage(resourceName, e.getRule(), "SQL.OUT");
//            });
            throw e;
        } finally {
            ContextUtil.exit();
            CommonUtil.DEFAULT_FLOW_RULE.remove();
            if (entry != null) {
                entry.exit();
            }
        }
        return returnObj;
    }

    /**
     * 获取类名和方法名
     *
     * @param mappedStatementId
     * @return
     */
    private String getClassMethod(String mappedStatementId) {
        String[] strArr = mappedStatementId.split("\\.");
        String className = strArr[strArr.length - 2];
        String methodName = strArr[strArr.length - 1];
        return String.format("%s.%s", className, methodName);
    }


    @Override
    public Object plugin(Object target) {
        if (target instanceof Executor) {
            return Plugin.wrap(target, this);
        }
        return target;
    }

    @Override
    public void setProperties(Properties properties) {
    }

}
