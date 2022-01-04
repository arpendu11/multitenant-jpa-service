package com.stackabuse.multitenantjpaservice.aspect;

import com.stackabuse.multitenantjpaservice.util.TenantContext;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.hibernate.Session;

import java.util.Objects;

@Aspect
public class TenantFilterAspect {

    @Pointcut("execution (* org.hibernate.internal.SessionFactoryImpl.SessionBuilderImpl.openSession(..))")
    public void openSession() {
    }

    @AfterReturning(pointcut = "openSession()", returning = "session")
    public void afterOpenSession(Object session) {
        if (Objects.nonNull(session) && (session instanceof Session)) {
            final String tenantId = TenantContext.getTenantId();
            if (tenantId != null) {
                org.hibernate.Filter filter = ((Session) session).enableFilter("tenantFilter");
                filter.setParameter("tenantId", tenantId);
            }
        }
    }
}
