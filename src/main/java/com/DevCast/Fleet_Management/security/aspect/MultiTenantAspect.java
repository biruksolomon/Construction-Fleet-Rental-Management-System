package com.DevCast.Fleet_Management.security.aspect;

import com.DevCast.Fleet_Management.security.annotation.MultiTenant;
import com.DevCast.Fleet_Management.security.util.SecurityUtils;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.lang.reflect.Method;

/**
 * AOP Aspect for multi-tenant data isolation
 * Ensures users can only access data from their own company
 */
@Aspect
@Component
public class MultiTenantAspect {
    private static final Logger logger = LoggerFactory.getLogger(MultiTenantAspect.class);

    @Before("@annotation(com.DevCast.Fleet_Management.security.annotation.MultiTenant)")
    public void enforceMultiTenant(JoinPoint joinPoint, MultiTenant multiTenant) {
        try {
            String paramName = multiTenant.value();
            Object[] args = joinPoint.getArgs();
            String[] paramNames = getMethodParameterNames(joinPoint);

            Long requestedCompanyId = null;
            for (int i = 0; i < paramNames.length; i++) {
                if (paramNames[i].equals(paramName)) {
                    requestedCompanyId = (Long) args[i];
                    break;
                }
            }

            if (requestedCompanyId != null) {
                Long currentUserCompanyId = SecurityUtils.getCurrentCompanyId();

                if (!requestedCompanyId.equals(currentUserCompanyId)) {
                    String methodName = joinPoint.getSignature().getName();
                    String className = joinPoint.getTarget().getClass().getSimpleName();

                    logger.warn("Multi-tenant access violation in {}.{} - User company: {}, Requested company: {}",
                            className, methodName, currentUserCompanyId, requestedCompanyId);

                    throw new AccessDeniedException(
                            "Access denied: Cannot access data from different company");
                }
            }
        } catch (AccessDeniedException e) {
            throw e;
        } catch (Exception e) {
            logger.error("Error in multi-tenant check: {}", e.getMessage(), e);
            throw new AccessDeniedException("Multi-tenant validation failed");
        }
    }

    /**
     * Extract method parameter names
     */
    private String[] getMethodParameterNames(JoinPoint joinPoint) {
        try {
            Method method = ((org.aspectj.lang.reflect.MethodSignature) joinPoint.getSignature()).getMethod();
            java.lang.reflect.Parameter[] parameters = method.getParameters();
            String[] names = new String[parameters.length];

            for (int i = 0; i < parameters.length; i++) {
                names[i] = parameters[i].getName();
            }

            return names;
        } catch (Exception e) {
            logger.error("Could not extract parameter names: {}", e.getMessage());
            return new String[0];
        }
    }
}
