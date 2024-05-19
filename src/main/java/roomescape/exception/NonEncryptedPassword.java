package roomescape.exception;

public class NonEncryptedPassword extends IllegalArgumentException {
    public NonEncryptedPassword(String s) {
        super(s);
    }
}
