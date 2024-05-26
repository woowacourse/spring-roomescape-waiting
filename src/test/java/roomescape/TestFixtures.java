package roomescape;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import roomescape.domain.member.Role;
import roomescape.dto.AdminReservationRequest;
import roomescape.dto.AvailableTimeResponse;
import roomescape.dto.MemberResponse;
import roomescape.dto.MemberSignUpRequest;
import roomescape.dto.MyReservationResponse;
import roomescape.dto.ReservationCriteriaRequest;
import roomescape.dto.ReservationResponse;
import roomescape.dto.ReservationTimeRequest;
import roomescape.dto.ReservationTimeResponse;
import roomescape.dto.ThemeResponse;

public class TestFixtures {
    public static final LocalDate TODAY = LocalDate.now();
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
    public static final List<ThemeResponse> THEME_RESPONSES_4 = List.of(
            THEME_RESPONSE_1, THEME_RESPONSE_2, THEME_RESPONSE_3, THEME_RESPONSE_4, THEME_RESPONSE_5
    );
    public static final MemberSignUpRequest MEMBER_SIGN_UP_REQUEST = new MemberSignUpRequest(
            "새멤버", "newmember@wooteco.com", "wootecoCrew6!"
    );
    public static final MemberResponse MEMBER_RESPONSE_1 = new MemberResponse(
            1L, "찰리", "gomding@wooteco.com", Role.BASIC
    );
    public static final MemberResponse MEMBER_RESPONSE_2 = new MemberResponse(
            2L, "비토", "bito@wooteco.com", Role.BASIC
    );
    public static final MemberResponse MEMBER_RESPONSE_3 = new MemberResponse(
            3L, "회원", "member@wooteco.com", Role.BASIC
    );
    public static final MemberResponse MEMBER_RESPONSE_4 = new MemberResponse(
            4L, "운영자", "admin@wooteco.com", Role.ADMIN
    );
    public static final MemberResponse MEMBER_RESPONSE_5 = new MemberResponse(
            5L, "새멤버", "newmember@wooteco.com", Role.BASIC
    );
    public static final List<MemberResponse> MEMBER_RESPONSES_1 = List.of(
            MEMBER_RESPONSE_1, MEMBER_RESPONSE_2, MEMBER_RESPONSE_3, MEMBER_RESPONSE_4
    );
    public static final List<MemberResponse> MEMBER_RESPONSES_2 = List.of(
            MEMBER_RESPONSE_1, MEMBER_RESPONSE_2, MEMBER_RESPONSE_3, MEMBER_RESPONSE_4, MEMBER_RESPONSE_5
    );
    public static final AvailableTimeResponse AVAILABLE_TIME_RESPONSE_1 = new AvailableTimeResponse(
            1L, LocalTime.of(10, 0), true
    );
    public static final AvailableTimeResponse AVAILABLE_TIME_RESPONSE_2 = new AvailableTimeResponse(
            2L, LocalTime.of(11, 0), true
    );
    public static final AvailableTimeResponse AVAILABLE_TIME_RESPONSE_3 = new AvailableTimeResponse(
            3L, LocalTime.of(12, 0), true
    );
    public static final AvailableTimeResponse AVAILABLE_TIME_RESPONSE_4 = new AvailableTimeResponse(
            4L, LocalTime.of(13, 0), false
    );
    public static final List<AvailableTimeResponse> AVAILABLE_TIME_RESPONSES = List.of(
            AVAILABLE_TIME_RESPONSE_1, AVAILABLE_TIME_RESPONSE_2, AVAILABLE_TIME_RESPONSE_3, AVAILABLE_TIME_RESPONSE_4
    );
    public static final ReservationCriteriaRequest RESERVATION_CRITERIA_REQUEST = new ReservationCriteriaRequest(
            1L, 1L, TODAY.minusDays(6), TODAY.minusDays(4)
    );
    public static final ReservationTimeRequest RESERVATION_TIME_REQUEST = new ReservationTimeRequest(LocalTime.of(14, 0));
    public static final ReservationTimeResponse RESERVATION_TIME_RESPONSE_1 = new ReservationTimeResponse(
            1L, LocalTime.of(10, 0)
    );
    public static final ReservationTimeResponse RESERVATION_TIME_RESPONSE_2 = new ReservationTimeResponse(
            2L, LocalTime.of(11, 0)
    );
    public static final ReservationTimeResponse RESERVATION_TIME_RESPONSE_3 = new ReservationTimeResponse(
            3L, LocalTime.of(12, 0)
    );
    public static final ReservationTimeResponse RESERVATION_TIME_RESPONSE_4 = new ReservationTimeResponse(
            4L, LocalTime.of(13, 0)
    );
    public static final ReservationTimeResponse RESERVATION_TIME_RESPONSE_5 = new ReservationTimeResponse(
            5L, LocalTime.of(14, 0)
    );
    public static final List<ReservationTimeResponse> RESERVATION_TIME_RESPONSES_1 = List.of(
            RESERVATION_TIME_RESPONSE_1, RESERVATION_TIME_RESPONSE_2, RESERVATION_TIME_RESPONSE_3,
            RESERVATION_TIME_RESPONSE_4, RESERVATION_TIME_RESPONSE_5
    );
    public static final List<ReservationTimeResponse> RESERVATION_TIME_RESPONSES_2 = List.of(
            RESERVATION_TIME_RESPONSE_1, RESERVATION_TIME_RESPONSE_2, RESERVATION_TIME_RESPONSE_3
    );
    public static final ReservationResponse RESERVATION_RESPONSE_1 = new ReservationResponse(
            1L, MEMBER_RESPONSE_1, TODAY.minusDays(5), RESERVATION_TIME_RESPONSE_1, THEME_RESPONSE_1
    );
    public static final ReservationResponse RESERVATION_RESPONSE_2 = new ReservationResponse(
            2L, MEMBER_RESPONSE_1, TODAY.minusDays(5), RESERVATION_TIME_RESPONSE_2, THEME_RESPONSE_1
    );
    public static final ReservationResponse RESERVATION_RESPONSE_3 = new ReservationResponse(
            3L, MEMBER_RESPONSE_1, TODAY.minusDays(5), RESERVATION_TIME_RESPONSE_3, THEME_RESPONSE_1
    );
    public static final ReservationResponse RESERVATION_RESPONSE_4 = new ReservationResponse(
            4L, MEMBER_RESPONSE_1, TODAY.minusDays(5), RESERVATION_TIME_RESPONSE_4, THEME_RESPONSE_1
    );
    public static final ReservationResponse RESERVATION_RESPONSE_5 = new ReservationResponse(
            5L, MEMBER_RESPONSE_1, TODAY.minusDays(5), RESERVATION_TIME_RESPONSE_1, THEME_RESPONSE_2
    );
    public static final ReservationResponse RESERVATION_RESPONSE_6 = new ReservationResponse(
            6L, MEMBER_RESPONSE_1, TODAY.minusDays(5), RESERVATION_TIME_RESPONSE_2, THEME_RESPONSE_2
    );
    public static final ReservationResponse RESERVATION_RESPONSE_7 = new ReservationResponse(
            7L, MEMBER_RESPONSE_2, TODAY.minusDays(5), RESERVATION_TIME_RESPONSE_3, THEME_RESPONSE_2
    );
    public static final ReservationResponse RESERVATION_RESPONSE_8 = new ReservationResponse(
            8L, MEMBER_RESPONSE_2, TODAY.minusDays(5), RESERVATION_TIME_RESPONSE_1, THEME_RESPONSE_3
    );
    public static final ReservationResponse RESERVATION_RESPONSE_9 = new ReservationResponse(
            9L, MEMBER_RESPONSE_2, TODAY.minusDays(5), RESERVATION_TIME_RESPONSE_2, THEME_RESPONSE_3
    );
    public static final ReservationResponse RESERVATION_RESPONSE_10 = new ReservationResponse(
            10L, MEMBER_RESPONSE_2, TODAY.minusDays(5), RESERVATION_TIME_RESPONSE_1, THEME_RESPONSE_4
    );
    public static final ReservationResponse RESERVATION_RESPONSE_11 = new ReservationResponse(
            11L, MEMBER_RESPONSE_2, TODAY.minusDays(5), RESERVATION_TIME_RESPONSE_1, THEME_RESPONSE_5
    );
    public static final ReservationResponse RESERVATION_RESPONSE_12 = new ReservationResponse(
            12L, MEMBER_RESPONSE_2, TODAY.minusDays(5), RESERVATION_TIME_RESPONSE_1, THEME_RESPONSE_6
    );
    public static final ReservationResponse RESERVATION_RESPONSE_13 = new ReservationResponse(
            13L, MEMBER_RESPONSE_3, TODAY.minusDays(5), RESERVATION_TIME_RESPONSE_1, THEME_RESPONSE_7
    );
    public static final ReservationResponse RESERVATION_RESPONSE_14 = new ReservationResponse(
            14L, MEMBER_RESPONSE_3, TODAY.minusDays(5), RESERVATION_TIME_RESPONSE_1, THEME_RESPONSE_8
    );
    public static final ReservationResponse RESERVATION_RESPONSE_15 = new ReservationResponse(
            15L, MEMBER_RESPONSE_3, TODAY.minusDays(5), RESERVATION_TIME_RESPONSE_1, THEME_RESPONSE_9
    );
    public static final ReservationResponse RESERVATION_RESPONSE_16 = new ReservationResponse(
            16L, MEMBER_RESPONSE_1, TODAY.plusDays(1), RESERVATION_TIME_RESPONSE_1, THEME_RESPONSE_1
    );
    public static final List<ReservationResponse> RESERVATION_RESPONSES_1 = List.of(
            RESERVATION_RESPONSE_1, RESERVATION_RESPONSE_2, RESERVATION_RESPONSE_3, RESERVATION_RESPONSE_4
    );
    public static final List<ReservationResponse> RESERVATION_RESPONSES_2 = List.of(
            RESERVATION_RESPONSE_1, RESERVATION_RESPONSE_2, RESERVATION_RESPONSE_3, RESERVATION_RESPONSE_4,
            RESERVATION_RESPONSE_5, RESERVATION_RESPONSE_6, RESERVATION_RESPONSE_7, RESERVATION_RESPONSE_8,
            RESERVATION_RESPONSE_9, RESERVATION_RESPONSE_10, RESERVATION_RESPONSE_11, RESERVATION_RESPONSE_12,
            RESERVATION_RESPONSE_13, RESERVATION_RESPONSE_14, RESERVATION_RESPONSE_15, RESERVATION_RESPONSE_16
    );
    public static final List<ReservationResponse> RESERVATION_RESPONSES_3 = List.of(
            RESERVATION_RESPONSE_2, RESERVATION_RESPONSE_3, RESERVATION_RESPONSE_4, RESERVATION_RESPONSE_5,
            RESERVATION_RESPONSE_6, RESERVATION_RESPONSE_7, RESERVATION_RESPONSE_8, RESERVATION_RESPONSE_9,
            RESERVATION_RESPONSE_10, RESERVATION_RESPONSE_11, RESERVATION_RESPONSE_12, RESERVATION_RESPONSE_13,
            RESERVATION_RESPONSE_14, RESERVATION_RESPONSE_15
    );
    public static final MyReservationResponse MY_RESERVATION_RESPONSE_1 = new MyReservationResponse(
            1L, "name1", TODAY.minusDays(5), LocalTime.of(10, 0), "예약"
    );
    public static final MyReservationResponse MY_RESERVATION_RESPONSE_2 = new MyReservationResponse(
            2L, "name1", TODAY.minusDays(5), LocalTime.of(11, 0), "예약"
    );
    public static final MyReservationResponse MY_RESERVATION_RESPONSE_3 = new MyReservationResponse(
            3L, "name1", TODAY.minusDays(5), LocalTime.of(12, 0), "예약"
    );
    public static final MyReservationResponse MY_RESERVATION_RESPONSE_4 = new MyReservationResponse(
            4L, "name1", TODAY.minusDays(5), LocalTime.of(13, 0), "예약"
    );
    public static final MyReservationResponse MY_RESERVATION_RESPONSE_5 = new MyReservationResponse(
            5L, "name2", TODAY.minusDays(5), LocalTime.of(10, 0), "예약"
    );
    public static final MyReservationResponse MY_RESERVATION_RESPONSE_6 = new MyReservationResponse(
            6L, "name2", TODAY.minusDays(5), LocalTime.of(11, 0), "예약"
    );
    public static final List<MyReservationResponse> MY_RESERVATION_RESPONSES = List.of(
            MY_RESERVATION_RESPONSE_1, MY_RESERVATION_RESPONSE_2, MY_RESERVATION_RESPONSE_3,
            MY_RESERVATION_RESPONSE_4, MY_RESERVATION_RESPONSE_5, MY_RESERVATION_RESPONSE_6
    );
    public static final AdminReservationRequest ADMIN_RESERVATION_REQUEST_1 = new AdminReservationRequest(
            2L, TODAY.plusDays(5), 1L, 9L
    );
    public static final AdminReservationRequest ADMIN_RESERVATION_REQUEST_2 = new AdminReservationRequest(
            3L, TODAY.plusDays(5), 1L, 9L
    );
}
