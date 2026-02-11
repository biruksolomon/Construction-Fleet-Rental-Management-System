package com.devcast.fleetmanagement.security.aspect;


import com.devcast.fleetmanagement.features.user.model.util.Permission;
import com.devcast.fleetmanagement.security.annotation.RequirePermission;
import com.devcast.fleetmanagement.security.util.SecurityUtils;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * AOP Aspect for method-level permission checking
 * Enforces @RequirePermission annotations
 */
@Aspect
@Component
public class PermissionCheckAspect {

    private static final Logger logger =
            LoggerFactory.getLogger(PermissionCheckAspect.class);

    @Before("@annotation(requirePermission)")
    public void checkPermission(JoinPoint joinPoint,
                                RequirePermission requirePermission) {

        Permission requiredPermission = requirePermission.value();

        if (!SecurityUtils.hasPermission(requiredPermission)) {
            String methodName = joinPoint.getSignature().getName();
            String className = joinPoint.getTarget().getClass().getSimpleName();

            logger.warn("Access denied for method {}.{} - Required permission: {}",
                    className, methodName, requiredPermission.getCode());

            throw new AccessDeniedException(
                    "Access denied: Missing required permission: "
                            + requiredPermission.getDescription());
        }
    }
}
