package roomescape.util;

import java.time.LocalDate;

public class Fixture {

    public static final String HORROR_THEME_NAME = "공포";
    public static final String HORROR_DESCRIPTION = "공포";
    public static final String THUMBNAIL = "https://i.pinimg.com/236x.jpg";

    public static final String HOUR_10 = "10:00";
    public static final String HOUR_11 = "11:00";

    public static final String KAKI_NAME = "카키";
    public static final String KAKI_EMAIL = "kaki@email.com";
    public static final String KAKI_PASSWORD = "1234";

    public static final String JOJO_NAME = "조조";
    public static final String JOJO_EMAIL = "jojo@email.com";
    public static final String JOJO_PASSWORD = "1234";

    public static final LocalDate TODAY = LocalDate.now();
    public static final LocalDate YESTERDAY = LocalDate.now().plusDays(1);

    private Fixture() {
    }
}
