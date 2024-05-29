package roomescape.security.authentication;

public interface Authentication {

    long getId();

    String getName();

    boolean isNotAdmin();
}
