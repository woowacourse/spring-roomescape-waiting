package roomescape.repository;

import static org.junit.jupiter.api.Assertions.assertAll;
import static roomescape.fixture.MemberFixture.DEFAULT_MEMBER;
import static roomescape.fixture.ReservationTimeFixture.DEFAULT_TIME;
import static roomescape.fixture.ThemeFixture.DEFAULT_THEME;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import roomescape.domain.Member;
import roomescape.domain.Reservation;
import roomescape.domain.ReservationTime;
import roomescape.domain.Theme;

@SpringBootTest
@Transactional
class JpaReservationTimeRepositoryTest {
    @Autowired
    private ReservationTimeRepository reservationTimeRepository;

    @Autowired
    private ReservationRepository reservationRepository;

    @Autowired
    private ThemeRepository themeRepository;

    @Autowired
    private MemberRepository memberRepository;

    @Test
    @DisplayName("ReservationTime 을 잘 저장하는지 확인한다.")
    void save() {
        List<Long> reservationTimeIdsBeforeSave = reservationTimeRepository.findAll()
                .stream()
                .map(ReservationTime::getId)
                .toList();
        ReservationTime saved = reservationTimeRepository.save(DEFAULT_TIME);
        List<ReservationTime> afterSave = reservationTimeRepository.findAll();

        Assertions.assertThat(afterSave)
                .extracting(ReservationTime::getId)
                .containsAll(reservationTimeIdsBeforeSave)
                .contains(saved.getId());
    }

    @Test
    @DisplayName("ReservationTime 을 잘 조회하는지 확인한다.")
    void findAll() {
        List<ReservationTime> beforeSave = reservationTimeRepository.findAll();
        reservationTimeRepository.save(DEFAULT_TIME);

        List<ReservationTime> afterSave = reservationTimeRepository.findAll();

        Assertions.assertThat(afterSave.size())
                .isEqualTo(beforeSave.size() + 1);
    }

    @Test
    @DisplayName("ReservationTime 을 잘 지우는지 확인한다.")
    void delete() {
        List<ReservationTime> beforeSaveAndDelete = reservationTimeRepository.findAll();
        ReservationTime savedTime = reservationTimeRepository.save(DEFAULT_TIME);

        reservationTimeRepository.deleteById(savedTime.getId());

        List<ReservationTime> afterSaveAndDelete = reservationTimeRepository.findAll();

        Assertions.assertThat(beforeSaveAndDelete)
                .containsExactlyInAnyOrderElementsOf(afterSaveAndDelete);
    }

    @Test
    @DisplayName("특정 시작 시간을 가지는 예약 시간이 있는지 여부를 잘 반환하는지 확인한다.")
    void existsByStartAt() {
        LocalTime time = DEFAULT_TIME.getStartAt();
        reservationTimeRepository.save(DEFAULT_TIME);

        assertAll(
                () -> Assertions.assertThat(reservationTimeRepository.existsByStartAt(time))
                        .isTrue(),
                () -> Assertions.assertThat(reservationTimeRepository.existsByStartAt(time.plusHours(1)))
                        .isFalse()
        );
    }

    @Test
    @DisplayName("특정 날짜와 테마에 예약이 있는 예약 시간의 목록을 잘 반환하는지 확인한다.")
    void findUsedTimeByDateAndTheme() {
        Member member = memberRepository.save(DEFAULT_MEMBER);
        ReservationTime time = reservationTimeRepository.save(DEFAULT_TIME);
        Theme theme = themeRepository.save(DEFAULT_THEME);
        Reservation reservation = reservationRepository.save(
                new Reservation(member, LocalDate.now().plusDays(1), time, theme));

        List<ReservationTime> response = reservationRepository.findAllByDateAndTheme(
                        reservation.getDate(), reservation.getTheme())
                .stream()
                .map(Reservation::getReservationTime)
                .toList();

        Assertions.assertThat(response)
                .extracting(ReservationTime::getId)
                .containsExactly(time.getId());
    }
}
