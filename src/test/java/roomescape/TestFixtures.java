package roomescape;

import java.util.List;
import roomescape.dto.ThemeResponse;

public class TestFixtures {
    public static final ThemeResponse THEME_RESPONSE_1 = new ThemeResponse(
            1L, "name1", "description1", "thumbnail1"
    );
    public static final ThemeResponse THEME_RESPONSE_2 = new ThemeResponse(
            2L, "name2", "description2", "thumbnail2"
    );
    public static final ThemeResponse THEME_RESPONSE_3 = new ThemeResponse(
            3L, "name3", "description3", "thumbnail3"
    );
    public static final ThemeResponse THEME_RESPONSE_4 = new ThemeResponse(
            4L, "name4", "description4", "thumbnail4"
    );
    public static final ThemeResponse THEME_RESPONSE_5 = new ThemeResponse(
            5L, "name5", "description5", "thumbnail5"
    );
    public static final ThemeResponse THEME_RESPONSE_6 = new ThemeResponse(
            6L, "name6", "description6", "thumbnail6"
    );
    public static final ThemeResponse THEME_RESPONSE_7 = new ThemeResponse(
            7L, "name7", "description7", "thumbnail7"
    );
    public static final ThemeResponse THEME_RESPONSE_8 = new ThemeResponse(
            8L, "name8", "description8", "thumbnail8"
    );
    public static final ThemeResponse THEME_RESPONSE_9 = new ThemeResponse(
            9L, "name9", "description9", "thumbnail9"
    );
    public static final ThemeResponse THEME_RESPONSE_10 = new ThemeResponse(
            10L, "name10", "description10", "thumbnail10"
    );
    public static final ThemeResponse THEME_RESPONSE_11 = new ThemeResponse(
            11L, "name11", "description11", "thumbnail11"
    );
    public static final List<ThemeResponse> THEME_RESPONSES_1 = List.of(
            THEME_RESPONSE_1, THEME_RESPONSE_2, THEME_RESPONSE_3, THEME_RESPONSE_4, THEME_RESPONSE_5, THEME_RESPONSE_6,
            THEME_RESPONSE_7, THEME_RESPONSE_8, THEME_RESPONSE_9
    );
    public static final List<ThemeResponse> THEME_RESPONSES_2 = List.of(
            THEME_RESPONSE_1, THEME_RESPONSE_2, THEME_RESPONSE_3, THEME_RESPONSE_4, THEME_RESPONSE_5, THEME_RESPONSE_6,
            THEME_RESPONSE_7, THEME_RESPONSE_8, THEME_RESPONSE_9, THEME_RESPONSE_10
    );
    public static final List<ThemeResponse> THEME_RESPONSES_3 = List.of(
            THEME_RESPONSE_1, THEME_RESPONSE_2, THEME_RESPONSE_3, THEME_RESPONSE_11, THEME_RESPONSE_4, THEME_RESPONSE_5,
            THEME_RESPONSE_6, THEME_RESPONSE_7, THEME_RESPONSE_8, THEME_RESPONSE_9
    );
}
