package roomescape.util;

import java.time.LocalDate;
import org.springframework.stereotype.Component;

@Component
public class CurrentUtilImpl implements CurrentUtil {

    @Override
    public LocalDate getCurrentDate() {
        return LocalDate.now();
    }
}
