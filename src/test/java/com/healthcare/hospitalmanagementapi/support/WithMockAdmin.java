package com.healthcare.hospitalmanagementapi.support;

import org.springframework.security.test.context.support.WithSecurityContext;
import java.lang.annotation.*;

@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@WithSecurityContext(factory = WithMockAdminSecurityContextFactory.class)
public @interface WithMockAdmin {
    String email() default "admin@test.com";
}