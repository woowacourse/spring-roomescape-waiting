package roomescape.model.member;

import jakarta.persistence.Embeddable;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@Embeddable
public class MemberPassword {

    @NotNull
    @NotBlank
    private String password;

    public MemberPassword(String password) {
        this.password = password;
    }

    protected MemberPassword() {
    }

    public String getPassword() {
        return password;
    }
}
