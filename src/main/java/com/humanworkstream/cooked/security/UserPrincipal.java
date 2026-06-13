package com.humanworkstream.cooked.security;

public record UserPrincipal(String email, long userId, String role) {
}
