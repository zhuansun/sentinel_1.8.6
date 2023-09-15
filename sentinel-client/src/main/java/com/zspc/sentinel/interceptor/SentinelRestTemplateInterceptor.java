package com.zspc.sentinel.interceptor;

import com.alibaba.csp.sentinel.Entry;
import com.alibaba.csp.sentinel.EntryType;
import com.alibaba.csp.sentinel.SphU;
import com.alibaba.csp.sentinel.slots.block.BlockException;
import com.zspc.sentinel.commom.CommonUtil;
import com.zspc.sentinel.commom.Constants;
import com.zspc.sentinel.exception.ZsSentinelBlockException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;

import java.io.IOException;

//import com.dianping.cat.message.Message;

/**
 * @author Zhou
 */
public class SentinelRestTemplateInterceptor implements ClientHttpRequestInterceptor {

    protected Logger logger = LoggerFactory.getLogger(this.getClass());

    @Override
    public ClientHttpResponse intercept(HttpRequest request, byte[] body, ClientHttpRequestExecution execution) throws IOException {

        String resourceName = request.getURI().getPath();
        Entry entry = null;

        ClientHttpResponse response;
        try {
            CommonUtil.DEFAULT_FLOW_RULE.set(Constants.DEFAULT_FLOW_RULE_URL_OUT);
            entry = SphU.entry(resourceName, EntryType.OUT);
            response = execution.execute(request, body);
        } catch (BlockException e) {
            String blockInfo = String.format("%s:%s", CommonUtil.getRuleType(e), e.getRule().getResource());
            logger.error(String.format("SentinelBlock %s %s %s", "Sentinel.URL.OUT", resourceName, blockInfo));
//            CatUtil.logEvent("Sentinel.URL.OUT", resourceName, Message.SUCCESS, blockInfo);
//            AsyncSendMessagePool.submitRunnable(() -> {
//                SendMessageSingle.sendMessage(resourceName, e.getRule(), "URL.OUT");
//            });
            throw new ZsSentinelBlockException(e);
        } finally {
            CommonUtil.DEFAULT_FLOW_RULE.remove();
            if (entry != null) {
                entry.exit();
            }
        }
        return response;
    }
}
