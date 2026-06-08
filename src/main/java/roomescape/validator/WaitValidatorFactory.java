package roomescape.validator;

public class WaitValidatorFactory {

    public static WaitValidator getValidator(boolean isAdmin) {
        if (isAdmin) {
            return new AdminWaitValidator();
        }
        return new UserWaitValidator();
    }
}
