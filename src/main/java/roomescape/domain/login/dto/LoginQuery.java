package roomescape.domain.login.dto;

public record LoginQuery(String email, String password) {

    public static LoginQuery from(LoginRequest loginRequest) {
        return new LoginQuery(loginRequest.email(), loginRequest.password());
    }
}
