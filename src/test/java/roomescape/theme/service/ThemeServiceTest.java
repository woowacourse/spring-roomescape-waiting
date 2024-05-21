package roomescape.theme.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import roomescape.global.exception.model.RoomEscapeException;
import roomescape.member.domain.Member;
import roomescape.reservation.domain.Reservation;
import roomescape.reservation.domain.ReservationStatus;
import roomescape.reservation.repository.FakeReservationRepository;
import roomescape.theme.domain.Theme;
import roomescape.theme.exception.ThemeExceptionCode;
import roomescape.theme.repository.FakeThemeRepository;
import roomescape.time.domain.Time;

public class ThemeServiceTest {

    public static final Theme THEME = Theme.of("미르", "미르 방탈출", "썸네일 Url");
    public static final Member MEMBER = Member.of("polla@gmail.com", "polla99");

    private final ThemeService themeService;

    public ThemeServiceTest() {
        this.themeService = new ThemeService(new FakeThemeRepository(), new FakeReservationRepository());
    }

    @Test
    @DisplayName("예약이 존재하는 테마는 삭제하지 못한다.")
    void validateReservationExistence_ShouldThrowException_WhenReservationExist() {
        List<Reservation> reservations = new ArrayList<>();
        reservations.add(Reservation.of(LocalDate.now().plusDays(1), Time.from(LocalTime.now()), THEME, MEMBER,
                ReservationStatus.RESERVED));

        Throwable reservationExistAtTime = assertThrows(RoomEscapeException.class,
                () -> themeService.removeTheme(THEME.getId()));

        assertEquals(ThemeExceptionCode.USING_THEME_RESERVATION_EXIST.getMessage(),
                reservationExistAtTime.getMessage());
    }
}
