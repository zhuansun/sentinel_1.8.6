package com.zspc.sentinel.interceptor;

import com.alibaba.csp.sentinel.Entry;
import com.alibaba.csp.sentinel.EntryType;
import com.alibaba.csp.sentinel.SphU;
import com.alibaba.csp.sentinel.context.ContextUtil;
import com.alibaba.csp.sentinel.slots.block.BlockException;
import com.google.common.collect.ImmutableList;
import com.zspc.sentinel.commom.CommonUtil;
import com.zspc.sentinel.commom.Constants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.List;


public class SentinelHandlerInterceptor implements HandlerInterceptor {

    protected Logger logger = LoggerFactory.getLogger(this.getClass());

    private static final List<String> FRONT_RESOURCE = ImmutableList.of(".css", ".js", ".html", "swagger-resources", ".ico");

    public static final ThreadLocal<Entry> THREAD_LOCAL = new ThreadLocal<>();

    /**
     * 在请求处理之前进行调用（Controller方法调用之前）
     */
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {

        String resourceName = request.getRequestURI();
        for (String exclude : FRONT_RESOURCE) {
            if (resourceName.contains(exclude)) {
                return true;
            }
        }

        try {
            ContextUtil.enter("URL.IN");
            CommonUtil.DEFAULT_FLOW_RULE.set(Constants.DEFAULT_FLOW_RULE_URL_IN);
            Entry entry = SphU.entry(resourceName, EntryType.IN);
            THREAD_LOCAL.set(entry);
        } catch (BlockException e) {
            ContextUtil.exit();
            String blockInfo = String.format("%s:%s", CommonUtil.getRuleType(e), e.getRule().getResource());
            logger.error(String.format("SentinelBlock %s %s %s", "Sentinel.URL.IN", resourceName, blockInfo));
//            AsyncSendMessagePool.submitRunnable(() -> {
//                SendMessageSingle.sendMessage(resourceName, e.getRule(), "URL.IN");
//            });
//            CatUtil.logEvent("Sentinel.URL.IN", resourceName, Message.SUCCESS, blockInfo);
            throw e;
        } finally {
            CommonUtil.DEFAULT_FLOW_RULE.remove();
        }
        return true;
    }

    /**
     * 请求处理之后进行调用，但是在视图被渲染之前（Controller方法调用之后）
     */
    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) {
    }

    /**
     * 在整个请求结束之后被调用，也就是在DispatcherServlet 渲染了对应的视图之后执行（主要是用于进行资源清理工作）
     */
    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        Entry entry = THREAD_LOCAL.get();
        if (entry != null) {
            THREAD_LOCAL.remove();
            entry.exit();
            ContextUtil.exit();
        }
    }
}
