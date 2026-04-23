package dev.modernjava.upgrade.example;

final class RequestContext {

    private static final ThreadLocal<String> TENANT = new ThreadLocal<>();

    private RequestContext() {
    }

    static void setTenant(String tenant) {
        TENANT.set(tenant);
    }

    static void clear() {
        TENANT.remove();
    }
}
