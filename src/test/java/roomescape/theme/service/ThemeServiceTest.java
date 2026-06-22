package roomescape.theme.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.jdbc.Sql;
import roomescape.common.exception.ConflictException;
import roomescape.common.exception.NotFoundException;
import roomescape.reservation.domain.Reservation;
import roomescape.reservation.repository.ReservationRepository;
import roomescape.reservationslot.domain.ReservationSlot;
import roomescape.reservationslot.repository.ReservationSlotRepository;
import roomescape.reservationtime.domain.ReservationTime;
import roomescape.reservationtime.repository.ReservationTimeRepository;
import roomescape.theme.controller.dto.request.ThemeCreateRequest;
import roomescape.theme.controller.dto.response.ThemeResponse;
import roomescape.theme.domain.Theme;
import roomescape.theme.repository.ThemeRepository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@Sql("/clear.sql")
class ThemeServiceTest {

    @Autowired
    ThemeService themeService;

    @Autowired
    ThemeRepository themeRepository;

    @Autowired
    ReservationTimeRepository reservationTimeRepository;

    @Autowired
    ReservationSlotRepository reservationSlotRepository;

    @Autowired
    ReservationRepository reservationRepository;

    @Test
    @DisplayName("테마를 생성한다")
    void createTheme() {
        // when
        ThemeResponse response = themeService.create(
                new ThemeCreateRequest("링", "공포 테마", "http:~", 10000)
        );

        // then
        assertThat(response.id()).isEqualTo(1L);
        assertThat(response.name()).isEqualTo("링");
        assertThat(themeRepository.findById(response.id()))
                .get()
                .extracting(Theme::getName)
                .isEqualTo("링");
    }

    @Test
    @DisplayName("최근 예약이 많은 테마를 조회한다")
    void findPopularThemes() {
        // given
        final ReservationTime time = saveReservationTime("10:00:00");
        final Theme popularTheme = saveTheme("링", "공포 테마", "http:~", 10000);
        final Theme otherTheme = saveTheme("탈출", "추리 테마", "http:~", 10000);

        saveReservation("브라운", "brown@example.com", LocalDate.now().minusDays(2), time, popularTheme);
        saveReservation("재키", "jaekkii@example.com", LocalDate.now().minusDays(1), time, popularTheme);
        saveReservation("코로구", "korogoo@example.com", LocalDate.now().minusDays(1), time, otherTheme);

        // when
        List<ThemeResponse> responses = themeService.getPopularThemes();

        // then
        assertThat(responses)
                .extracting(ThemeResponse::name)
                .startsWith("링", "탈출");
    }

    @Test
    @DisplayName("존재하지 않는 테마를 삭제하면 예외가 발생한다")
    void throwExceptionWhenDeletingNonExistingTheme() {
        // when & then
        assertThatThrownBy(() -> themeService.delete(1L))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    @DisplayName("해당 테마에 예약 슬롯이 있으면 테마 삭제시 예외가 발생한다")
    void throwExceptionWhenDeletingThemeInUse() {
        // given
        final ReservationTime time = saveReservationTime("10:00:00");
        final Theme theme = saveTheme("링", "공포 테마", "http:~", 10000);
        saveReservation("브라운", "customer@example.com", LocalDate.now().plusDays(1), time, theme);

        // when & then
        assertThatThrownBy(() -> themeService.delete(theme.getId()))
                .isInstanceOf(ConflictException.class)
                .hasMessage("해당 테마에 예약이 존재하여 삭제할 수 없습니다.");
    }

    private ReservationTime saveReservationTime(final String startAt) {
        return reservationTimeRepository.save(ReservationTime.create(LocalTime.parse(startAt)));
    }

    private Theme saveTheme(
            final String name,
            final String description,
            final String thumbnailUrl,
            final int price
    ) {
        return themeRepository.save(Theme.create(name, description, thumbnailUrl, price));
    }

    private void saveReservation(
            final String customerName,
            final String customerEmail,
            final LocalDate date,
            final ReservationTime time,
            final Theme theme
    ) {
        final ReservationSlot slot = reservationSlotRepository.findOrCreate(date, time, theme);
        reservationRepository.save(Reservation.of(
                null,
                customerName,
                customerEmail,
                slot
        ));
    }
}
