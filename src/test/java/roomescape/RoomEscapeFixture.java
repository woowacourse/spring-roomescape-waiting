package roomescape;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import roomescape.controller.dto.request.ReservationCreateRequest;
import roomescape.controller.dto.request.ReservationUpdateRequest;
import roomescape.controller.dto.request.ThemeFamousFindRequest;
import roomescape.domain.reservation.Rank;
import roomescape.domain.reservation.Reservation;
import roomescape.domain.reservation.ReservationDate;
import roomescape.domain.reservation.ReservationName;
import roomescape.domain.reservation.ReservationResult;
import roomescape.domain.reservation.ReservationTime;
import roomescape.domain.reservation.Slot;
import roomescape.domain.reservation.Status;
import roomescape.domain.theme.Theme;
import roomescape.domain.theme.ThemeName;
import roomescape.domain.theme.ThumbnailUrl;

public class RoomEscapeFixture {
    private final static Clock FIXED_CLOCK = Clock.fixed(
            Instant.parse("2026-05-10T03:00:00Z"),
            ZoneId.of("Asia/Seoul")
    );
    private static final ReservationName NAME = new ReservationName("zeze");
    private static final ReservationDate FUTURE_DATE = new ReservationDate(LocalDate.of(2099, 11, 11));
    private static final ReservationDate PAST_DATE = new ReservationDate(LocalDate.of(2000, 11, 11));
    private static final ReservationTime TIME = ReservationTime.of(LocalTime.of(10, 0));
    private static final Theme THEME = Theme.create(new ThemeName("공포"), "무서워요", new ThumbnailUrl("https://zeze.com"));
    private static final Slot SLOT = Slot.create(FUTURE_DATE, TIME, THEME);
    private static final Rank APPROVE_RANK = new Rank(1);
    private static final Rank WAITING_RANK = new Rank(2);

    public static Theme theme() {
        return THEME;
    }

    public static ReservationName reservationName() {
        return NAME;
    }

    public static ReservationDate reservationDate() {
        return FUTURE_DATE;
    }

    public static ReservationTime reservationTime() {
        return TIME;
    }

    public static Slot slot() {
        return SLOT;
    }

    public static Reservation reservationWithApproved() {
        return Reservation.load(1L, NAME, SLOT, Status.APPROVED, LocalDateTime.now(FIXED_CLOCK));
    }

    public static Reservation reservationWithWaiting() {
        return Reservation.load(2L, NAME, SLOT, Status.WAITING, LocalDateTime.now(FIXED_CLOCK));
    }

    public static ReservationResult reservationResultWithApproved() {
        return new ReservationResult(APPROVE_RANK, reservationWithApproved());
    }

    public static ReservationResult reservationResultWithWaiting() {
        return new ReservationResult(WAITING_RANK, reservationWithWaiting());
    }

    public static ThemeFamousFindRequest themeFamousFindRequest() {
        return new ThemeFamousFindRequest(7L, FUTURE_DATE.getValue(), 10L);
    }

    public static ReservationCreateRequest reservationCreateRequest() {
        return new ReservationCreateRequest(NAME.getValue(), FUTURE_DATE.getValue(), 1L, 1L);
    }

    public static ReservationCreateRequest reservationCreateRequestWithName(ReservationName name) {
        return new ReservationCreateRequest(name.getValue(), FUTURE_DATE.getValue(), 1L, 1L);
    }

    public static ReservationCreateRequest reservationCreateRequestWithNullName() {
        return new ReservationCreateRequest(null, FUTURE_DATE.getValue(), 1L, 1L);
    }

    public static ReservationCreateRequest reservationCreateRequestWithNullDate() {
        return new ReservationCreateRequest(NAME.getValue(), null, 1L, 1L);
    }

    public static ReservationCreateRequest reservationCreateRequestWithNullTimeId() {
        return new ReservationCreateRequest(NAME.getValue(), FUTURE_DATE.getValue(), null, 1L);
    }

    public static ReservationCreateRequest reservationCreateRequestWithPastDate() {
        return new ReservationCreateRequest(NAME.getValue(), PAST_DATE.getValue(), 1L, 1L);
    }

    public static ReservationUpdateRequest reservationUpdateRequest() {
        return new ReservationUpdateRequest(NAME.getValue(), FUTURE_DATE.getValue(), 1L, 1L);
    }

    public static ReservationUpdateRequest reservationUpdateRequestWithPastDate() {
        return new ReservationUpdateRequest(NAME.getValue(), PAST_DATE.getValue(), 1L, 1L);
    }
}
