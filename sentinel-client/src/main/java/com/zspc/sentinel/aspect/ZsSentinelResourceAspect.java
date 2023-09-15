package com.zspc.sentinel.aspect;

import com.alibaba.csp.sentinel.Entry;
import com.alibaba.csp.sentinel.EntryType;
import com.alibaba.csp.sentinel.SphU;
import com.alibaba.csp.sentinel.annotation.SentinelResource;
import com.alibaba.csp.sentinel.annotation.aspectj.AbstractSentinelAspectSupport;
import com.alibaba.csp.sentinel.slots.block.BlockException;
import com.zspc.sentinel.commom.CommonUtil;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;

import java.lang.reflect.Method;

@Slf4j
@Aspect
public class ZsSentinelResourceAspect extends AbstractSentinelAspectSupport {


    @Pointcut("@annotation(com.alibaba.csp.sentinel.annotation.SentinelResource)")
    public void sentinelResourceAnnotationPointcut() {
    }

    @Around("sentinelResourceAnnotationPointcut()")
    public Object invokeResourceWithSentinel(ProceedingJoinPoint pjp) throws Throwable {
        Method originMethod = resolveMethod(pjp);

        SentinelResource annotation = originMethod.getAnnotation(SentinelResource.class);
        if (annotation == null) {
            throw new IllegalStateException("Wrong state for SentinelResource annotation");
        }
        String resourceName = getResourceName(annotation.value(), originMethod);
        EntryType entryType = annotation.entryType();
        int resourceType = annotation.resourceType();
        Entry entry = null;
        try {
            entry = SphU.entry(resourceName, resourceType, entryType, pjp.getArgs());
            return pjp.proceed();
        } catch (BlockException ex) {
            String blockInfo = String.format("%s:%s", CommonUtil.getRuleType(ex), ex.getRule().getResource());
            log.error("SentinelBlock {} {} {}", "Sentinel.SentinelResource", resourceName, blockInfo);
//            AsyncSendMessagePool.submitRunnable(() -> SendMessageSingle.sendMessage(resourceName, ex.getRule(), "SentinelResource"));
            return handleBlockException(pjp, annotation, ex);
        } catch (Throwable ex) {
            Class<? extends Throwable>[] exceptionsToIgnore = annotation.exceptionsToIgnore();
            if (exceptionsToIgnore.length > 0 && exceptionBelongsTo(ex, exceptionsToIgnore)) {
                throw ex;
            }
            if (exceptionBelongsTo(ex, annotation.exceptionsToTrace())) {
                traceException(ex);
                return handleFallback(pjp, annotation, ex);
            }

            throw ex;
        } finally {
            if (entry != null) {
                entry.exit(1, pjp.getArgs());
            }
        }
    }
}
