package com.DevCast.Fleet_Management.security.annotation;

import com.DevCast.Fleet_Management.model.util.Permission;

import java.lang.annotation.*;

/**
 * Annotation to enforce permission-based access control at method level
 * Usage: @RequirePermission(Permission.CREATE_VEHICLE)
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface RequirePermission {
    Permission value();
}
