package com.DevCast.Fleet_Management.security.annotation;

import java.lang.annotation.*;

/**
 * Annotation to mark methods that need multi-tenant filtering
 * Ensures user can only access data from their own company
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface MultiTenant {
    /**
     * Name of the parameter that contains the company ID
     * Default: "companyId"
     */
    String value() default "companyId";
}
