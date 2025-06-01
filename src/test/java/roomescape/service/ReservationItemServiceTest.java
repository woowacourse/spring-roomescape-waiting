package roomescape.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.DirtiesContext.ClassMode;
import org.springframework.test.context.TestPropertySource;
import roomescape.domain.reservationitem.ReservationItem;
import roomescape.domain.reservationitem.ReservationItemRepository;
import roomescape.domain.reservationitem.ReservationTheme;
import roomescape.domain.reservationitem.ReservationThemeRepository;
import roomescape.domain.reservationitem.ReservationTime;
import roomescape.domain.reservationitem.ReservationTimeRepository;
import roomescape.service.reservation.ReservationItemService;

@SpringBootTest
@DirtiesContext(classMode = ClassMode.BEFORE_EACH_TEST_METHOD)
@TestPropertySource(properties = {
        "spring.sql.init.mode=never",
        "spring.jpa.hibernate.ddl-auto=create-drop"
})
class ReservationItemServiceTest {

    @Autowired
    private ReservationItemService reservationItemService;

    @Autowired
    private ReservationItemRepository reservationItemRepository;
    @Autowired
    private ReservationTimeRepository reservationTimeRepository;
    @Autowired
    private ReservationThemeRepository reservationThemeRepository;

    private ReservationItem item;
    private ReservationTime time;
    private ReservationTime timeBeforeHour;
    private ReservationTheme theme;
    private LocalDate date = LocalDate.now().plusDays(1);

    @BeforeEach
    void setUp() {
        time = reservationTimeRepository.save(new ReservationTime(LocalTime.now().plusHours(1)));
        theme = reservationThemeRepository.save(new ReservationTheme("Theme1", "Description 1", "Thumbnail 1"));
        timeBeforeHour = reservationTimeRepository.save(new ReservationTime(LocalTime.now().minusHours(1)));

        item = reservationItemRepository.save(
                ReservationItem.builder()
                        .date(date)
                        .theme(theme)
                        .time(time)
                        .build()
        );
    }

    @Test
    @DisplayName("예약 아이템을 저장한다")
    void saveReservationItem() {
        // given
        LocalDate date = LocalDate.now().plusDays(2L);

        // when
        final ReservationItem reservationItem = reservationItemService.createReservationItemIfNotExist(date, time, theme);

        // then
        assertAll(
                () -> assertThat(reservationItem.getDate()).isEqualTo(date),
                () -> assertThat(reservationItem.getTime().getId()).isEqualTo(time.getId()),
                () -> assertThat(reservationItem.getTheme().getId()).isEqualTo(theme.getId())
        );
    }

    @Test
    @DisplayName("이미 예약이 존재하는 경우 기존 예약을 가져온다.")
    void duplicateReservationItemTest() {
        // given
        LocalDate date = LocalDate.now().plusDays(1L);

        // when
        final ReservationItem reservationItem = reservationItemService.createReservationItemIfNotExist(
                date, time, theme
        );

        // then
        assertThat(reservationItem.getId()).isEqualTo(item.getId());
    }

    @Test
    @DisplayName("미래의 예약이 아닌 경우 예약을 생성할 수 없다.")
    void notFutureReservationItemTest() {
        // given
        final LocalDateTime timeBeforeMinute = LocalDateTime.now().minusMinutes(1L);
        final ReservationTime reservationTimeBeforeMinute = reservationTimeRepository.save(
                new ReservationTime(timeBeforeMinute.toLocalTime()));
        LocalDate yesterday = LocalDate.now().minusDays(1);

        // when, then
        assertAll(
                () -> assertThatThrownBy(() -> reservationItemService.createReservationItemIfNotExist(timeBeforeMinute.toLocalDate(), reservationTimeBeforeMinute, theme))
                        .isInstanceOf(IllegalArgumentException.class),
                () -> assertThatThrownBy(() -> reservationItemService.createReservationItemIfNotExist(yesterday, time, theme))
                        .isInstanceOf(IllegalArgumentException.class)
        );
    }

    @Test
    @DisplayName("예약의 아이템이 이미 존재하는지 확인한다")
    void existReservationItemTest() {
        // when
        final boolean exist = reservationItemService.isExistReservationItem(date, time, theme);
        final boolean nonExist = reservationItemService.isExistReservationItem(LocalDate.now(), time, theme);

        // then
        assertAll(
                () -> assertThat(exist).isTrue(),
                () -> assertThat(nonExist).isFalse()
        );
    }
}
