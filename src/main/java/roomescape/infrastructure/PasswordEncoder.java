package roomescape.infrastructure;

import java.util.IllegalFormatFlagsException;

public class PasswordEncoder {

    public String encode(String password) {
        validateEmpty(password);
        return String.valueOf(password.hashCode());
    }

    public boolean matches(String rawPassword, String encodedPassword) {
        validateEmpty(rawPassword);
        String loginPassword = encode(rawPassword);
        return loginPassword.equals(encodedPassword);
    }

    private void validateEmpty(String password) {
        if (password == null || password.isEmpty()) {
            throw new IllegalFormatFlagsException("[ERROR] 비밀번호가 없습니다.");
        }
    }
}
