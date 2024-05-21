package roomescape.util;

import java.time.LocalDate;
import java.time.LocalTime;

public class Fixture {

    public static final String HORROR_THEME_NAME = "공포";
    public static final String HORROR_DESCRIPTION = "공포";
    public static final String THUMBNAIL = "https://i.pinimg.com/236x.jpg";

    public static final LocalTime HOUR_10 = LocalTime.parse("10:00");
    public static final LocalTime HOUR_11 = LocalTime.parse("11:00");

    public static final String KAKI_NAME = "카키";
    public static final String KAKI_EMAIL = "kaki@email.com";
    public static final String KAKI_PASSWORD = "1234";

    public static final String JOJO_NAME = "조조";
    public static final String JOJO_EMAIL = "jojo@email.com";
    public static final String JOJO_PASSWORD = "1234";

    public static final LocalDate TODAY = LocalDate.now();
    public static final LocalDate TOMORROW = LocalDate.now().plusDays(1);

    private Fixture() {
    }
}
