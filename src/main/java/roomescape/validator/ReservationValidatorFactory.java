package roomescape.validator;

public class ReservationValidatorFactory {

    public static ReservationValidator getValidator(boolean isAdmin) {
        if (isAdmin) {
            return new AdminReservationValidator();
        }
        return new UserReservationValidator();
    }
}
