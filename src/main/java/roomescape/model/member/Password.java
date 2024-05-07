package roomescape.model.member;

import jakarta.persistence.Embeddable;
import jakarta.validation.constraints.NotBlank;

@Embeddable
public class Password {

    @NotBlank
    private String password;

    public Password(String password) {
        this.password = password;
    }

    public Password() {
    }

    public String getPassword() {
        return password;
    }
}
