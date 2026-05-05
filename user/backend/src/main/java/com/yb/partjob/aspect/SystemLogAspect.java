package com.yb.partjob.aspect;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.yb.partjob.annotation.LogOperation;
import com.yb.partjob.model.SysLog;
import com.yb.partjob.repository.SysLogRepository;
import jakarta.servlet.http.HttpServletRequest;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@Aspect
@Component
public class SystemLogAspect {

    @Autowired
    private SysLogRepository sysLogRepository;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Around("@annotation(logOperation)")
    public Object around(ProceedingJoinPoint point, LogOperation logOperation) throws Throwable {
        long beginTime = System.currentTimeMillis();
        Object result = null;
        Exception exception = null;

        try {
            result = point.proceed();
        } catch (Exception e) {
            exception = e;
            throw e;
        } finally {
            long timeCost = System.currentTimeMillis() - beginTime;
            saveLog(point, logOperation, timeCost, exception);
        }
        return result;
    }

    private void saveLog(ProceedingJoinPoint joinPoint, LogOperation logOperation, long timeCost, Exception exception) {
        SysLog sysLog = new SysLog();
        sysLog.setAction(logOperation.value());
        sysLog.setMethod(joinPoint.getSignature().getDeclaringTypeName() + "." + joinPoint.getSignature().getName());
        sysLog.setTimeCost(timeCost);

        try {
            Object[] args = joinPoint.getArgs();
            if (args != null && args.length > 0) {
                try {
                    String params = objectMapper.writeValueAsString(args);
                    sysLog.setParams(params.length() > 2000 ? params.substring(0, 2000) : params);
                } catch (Exception ignore) {
                }
            }
        } catch (Exception e) {
        }

        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes())
                .getRequest();
        if (request != null) {
            sysLog.setIp(request.getRemoteAddr());
        }

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getName() != null) {
            sysLog.setUsername(authentication.getName());
        }

        if (exception != null) {
            sysLog.setStatus(0);
            String errorMsg = exception.getMessage();
            sysLog.setErrorMsg(errorMsg != null && errorMsg.length() > 2000 ? errorMsg.substring(0, 2000) : errorMsg);
        } else {
            sysLog.setStatus(1);
        }

        sysLogRepository.save(sysLog);
    }
}
