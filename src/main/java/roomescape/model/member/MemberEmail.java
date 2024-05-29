package roomescape.model.member;

import jakarta.persistence.Embeddable;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@Embeddable
public class MemberEmail {

    @NotNull
    @NotBlank
    @Email
    private String email;

    public MemberEmail(String email) {
        this.email = email;
    }

    protected MemberEmail() {
    }

    public String getEmail() {
        return email;
    }
}
