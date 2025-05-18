package roomescape.member.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

@Embeddable
public class Email {

    @Column(nullable = false)
    private String email;

    public Email(String email) {
        validateBlank(email);
        this.email = email;
    }

    public Email() {

    }

    private void validateBlank(String email) {
        if (email == null || email.isBlank()) {
            throw new IllegalArgumentException("[ERROR] 이메일은 공백이 될 수 없습니다.");
        }
    }

    public String getEmail() {
        return email;
    }
}
