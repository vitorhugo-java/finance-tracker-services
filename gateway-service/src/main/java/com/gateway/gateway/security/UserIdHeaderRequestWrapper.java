package com.gateway.gateway.security;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;

final class UserIdHeaderRequestWrapper extends HttpServletRequestWrapper {

    private final String userId;

    UserIdHeaderRequestWrapper(HttpServletRequest request, String userId) {
        super(request);
        this.userId = userId;
    }

    @Override
    public String getHeader(String name) {
        if (UserIdHeaderEnrichmentFilter.USER_ID_HEADER.equalsIgnoreCase(name)) {
            return userId;
        }
        return super.getHeader(name);
    }

    @Override
    public Enumeration<String> getHeaders(String name) {
        if (UserIdHeaderEnrichmentFilter.USER_ID_HEADER.equalsIgnoreCase(name)) {
            return Collections.enumeration(List.of(userId));
        }
        return super.getHeaders(name);
    }

    @Override
    public Enumeration<String> getHeaderNames() {
        Set<String> headerNames = new LinkedHashSet<>();
        Enumeration<String> originalHeaderNames = super.getHeaderNames();
        while (originalHeaderNames.hasMoreElements()) {
            headerNames.add(originalHeaderNames.nextElement());
        }
        headerNames.add(UserIdHeaderEnrichmentFilter.USER_ID_HEADER);
        return Collections.enumeration(new ArrayList<>(headerNames));
    }
}
