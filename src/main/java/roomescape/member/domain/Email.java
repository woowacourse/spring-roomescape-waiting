package roomescape.member.domain;

public class Email {

    private final String email;

    public Email(String email) {
        validateEmail(email);
        this.email = email;
    }

    private void validateEmail(String email){
        if (email == null || email.isBlank()) {
            throw new IllegalArgumentException("[ERROR] email 은 공백이 될 수 없습니다.");
        }
    }

    public String getEmail() {
        return email;
    }
}
