package roomescape.model.member;

import jakarta.persistence.Embeddable;
import jakarta.validation.constraints.NotBlank;

@Embeddable
public class Email {

    @NotBlank
    @jakarta.validation.constraints.Email
    private String email;

    public Email(String email) {
        this.email = email;
    }

    public Email() {
    }

    public String getEmail() {
        return email;
    }
}
