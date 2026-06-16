package com.humanworkstream.cooked.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Brute-force / abuse protection for the unauthenticated auth endpoints (login, register,
 * forgot-password). Fixed-window, per-(client IP + path) counter held in memory — no extra
 * dependency. Returns 429 with a {@code Retry-After} header once the limit is exceeded.
 *
 * <p>This is per-instance state: behind multiple replicas each instance enforces its own
 * window. For a hard global limit, move this to a shared store (Redis) or enforce at the
 * gateway. It still meaningfully raises the cost of credential stuffing and reset-email
 * bombing on a single node.
 */
@Slf4j
@Component
public class AuthRateLimitFilter extends OncePerRequestFilter {

    private static final Set<String> LIMITED_PATHS =
            Set.of("/auth/login", "/auth/register", "/auth/forgot-password");

    private final boolean enabled;
    private final int maxRequests;
    private final long windowMs;

    /** key = clientIp + "|" + path → counter for the current window. */
    private final ConcurrentHashMap<String, Window> windows = new ConcurrentHashMap<>();

    public AuthRateLimitFilter(
            @Value("${security.rate-limit.enabled:true}") boolean enabled,
            @Value("${security.rate-limit.max-requests:10}") int maxRequests,
            @Value("${security.rate-limit.window-seconds:60}") long windowSeconds) {
        this.enabled = enabled;
        this.maxRequests = maxRequests;
        this.windowMs = windowSeconds * 1000L;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain chain) throws ServletException, IOException {
        if (enabled && "POST".equalsIgnoreCase(request.getMethod())
                && LIMITED_PATHS.contains(request.getRequestURI())) {
            // getRemoteAddr already reflects X-Forwarded-For because
            // server.forward-headers-strategy=FRAMEWORK is set.
            String key = request.getRemoteAddr() + "|" + request.getRequestURI();
            long now = System.currentTimeMillis();
            Window w = windows.compute(key, (k, existing) -> {
                if (existing == null || now - existing.startMs >= windowMs) {
                    return new Window(now);
                }
                return existing;
            });
            int count = w.count.incrementAndGet();
            if (count > maxRequests) {
                long retryAfter = Math.max(1, (windowMs - (now - w.startMs)) / 1000);
                log.warn("[AuthRateLimit] {} blocked on {} ({} reqs in window)",
                        request.getRemoteAddr(), request.getRequestURI(), count);
                response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
                response.setHeader(HttpHeaders.RETRY_AFTER, String.valueOf(retryAfter));
                response.setContentType(MediaType.APPLICATION_JSON_VALUE);
                response.getWriter().write(
                        "{\"message\":\"Too many requests. Please try again later.\"}");
                return;
            }
            // Opportunistically bound memory: drop stale windows when the map grows large.
            if (windows.size() > 10_000) {
                windows.values().removeIf(win -> now - win.startMs >= windowMs);
            }
        }
        chain.doFilter(request, response);
    }

    private static final class Window {
        final long startMs;
        final AtomicInteger count = new AtomicInteger(0);

        Window(long startMs) {
            this.startMs = startMs;
        }
    }
}
