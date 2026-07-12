package pl.backend.weddinggallery;

import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

public class HashTest {
    @Test
    void testHash() {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        System.out.println("NEW HASH: " + encoder.encode("password123"));
        System.out.println("admin: " + encoder.matches("admin", "$2a$10$c1eO4x0R9HqV.H9hRj2E7.Nq05S6Jc.sZ3v4mR/W4Xm6D0cZ9f7dO"));
    }
}
