package roomescape.support.util;
import java.time.LocalDate;

public class DateUtil {

    public static LocalDate tomorrow() {
        return LocalDate.now().plusDays(1);
    }
}
