package com.stackabuse.multitenantjpaservice.interceptor;

import com.stackabuse.multitenantjpaservice.util.TenantContext;
import lombok.NonNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.ui.ModelMap;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.context.request.WebRequestInterceptor;

import java.util.Objects;

@Component
public class TenantInterceptor implements WebRequestInterceptor {

    private final String defaultTenant;

    private static final String  TENANT_AUTH_HEADER = "X-TENANT-KEY";

    @Autowired
    public TenantInterceptor(
            @Value("${multitenancy.master.schema:#{null}}")
                    String defaultTenant) {
        this.defaultTenant = defaultTenant;
    }

    @Override
    public void preHandle(WebRequest request) {
        String tenantId;
        if (Objects.nonNull(request.getHeader(TENANT_AUTH_HEADER))) {
            tenantId = request.getHeader(TENANT_AUTH_HEADER);
        } else {
            tenantId = this.defaultTenant;
        }
        TenantContext.setTenantId(tenantId);
    }

    @Override
    public void postHandle(@NonNull WebRequest request, ModelMap model) {
        TenantContext.clear();
    }

    @Override
    public void afterCompletion(@NonNull WebRequest request, Exception ex) {
        // NOOP
    }
}
