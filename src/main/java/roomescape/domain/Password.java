package roomescape.domain;

import de.mkammerer.argon2.Argon2;
import de.mkammerer.argon2.Argon2Factory;

public class Password {

    private static final int ITERATIONS = 2;
    private static final int MEMORY_KIB = 8 * 1024;
    private static final int PARALLELISM = 1;

    private static final Argon2 ARGON2 = Argon2Factory.create(
            Argon2Factory.Argon2Types.ARGON2id
    );

    private String value;

    private Password(String value) {
        this.value = value;
    }

    public static Password ofEncrypted(String plainPassword) {
        return new Password(encrypt(plainPassword));
    }

    public static Password ofHashed(String hashedValue) {
        return new Password(hashedValue);
    }

    public boolean matches(String plainPassword) {
        if (plainPassword == null || plainPassword.isBlank()) {
            return false;
        }

        char[] passwordChars = plainPassword.toCharArray();

        try {
            return ARGON2.verify(value, passwordChars);
        } finally {
            ARGON2.wipeArray(passwordChars);
        }
    }

    private static String encrypt(String plainPassword) {
        char[] passwordChars = plainPassword.toCharArray();
        try {
            String hashedPassword = ARGON2.hash(ITERATIONS, MEMORY_KIB, PARALLELISM, passwordChars);
            return hashedPassword;
        } finally {
            ARGON2.wipeArray(passwordChars);
        }
    }

    public String getValue() {
        return value;
    }
}
