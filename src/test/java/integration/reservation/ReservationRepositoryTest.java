package integration.reservation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import integration.BaseIntegrationTest;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import roomescape.domain.DuplicateEntityException;
import roomescape.domain.Reservation;
import roomescape.domain.ReservationTime;
import roomescape.domain.Theme;
import roomescape.domain.TimeStatus;
import roomescape.repository.ReservationRepository;

class ReservationRepositoryTest extends BaseIntegrationTest {
    @Autowired
    private ReservationRepository reservationRepository;
    @Autowired
    private ReservationDataSource dataSource;

    private ReservationTime reservationTime = new ReservationTime(1L, LocalTime.of(10, 0), TimeStatus.ACTIVE);
    private Theme theme = new Theme(1L, "공포", "어마무시한 공포 테마", "https://theme.com/image.png", false);

    @BeforeEach
    void setUp() {
        dataSource.clearTable();
        dataSource.clearId();
        dataSource.insertTheme(theme.getName(), theme.getDescription(), theme.getThumbnailImageUrl());
        dataSource.insertReservationTime(reservationTime.getStartAt());
    }

    @Test
    void 예약을_저장하고_ID로_조회한다() {
        // given: 시간 먼저 저장
        Reservation reservation = Reservation.createNew("이프", LocalDate.now().plusDays(1), theme, reservationTime);

        // when
        Reservation saved = reservationRepository.save(reservation);

        // then
        assertThat(saved.getId()).isEqualTo(1L);
        assertThat(dataSource.hasReservationById(saved.getId())).isTrue();
    }

    @Test
    void 동일한_날짜와_시간으로_저장하면_DB_제약조건_에러가_발생한다() {
        // given
        Reservation first = Reservation.createNew("이프", LocalDate.now().plusDays(1), theme, reservationTime);
        Reservation second = Reservation.createNew("아루", LocalDate.now().plusDays(1), theme, reservationTime);
        reservationRepository.save(first);

        // when & then: DB의 UK Constraint 발생 후 비즈니스 예외로 변환
        assertThatThrownBy(() -> reservationRepository.save(second))
                .isInstanceOf(DuplicateEntityException.class)
                .hasMessageContaining("이미 예약이 존재하는 시간입니다");
    }

    @Test
    void 특정_날짜와_시간에_예약이_존재하는지_확인한다() {
        // given
        LocalDate date = LocalDate.now().plusDays(1);
        reservationRepository.save(Reservation.createNew("이프", date, theme, reservationTime));

        // when & then
        Long otherTimeId = 99L;
        LocalDate otherDate = date.plusDays(1);
        assertThat(reservationRepository.existByDateAndThemeIdAndTimeId(date, 1L, 1L)).isTrue();
        assertThat(reservationRepository.existByDateAndThemeIdAndTimeId(date, 1L, otherTimeId)).isFalse();
        assertThat(reservationRepository.existByDateAndThemeIdAndTimeId(otherDate, 1L, 1L)).isFalse();
    }

    @Test
    void 예약을_삭제한다() {
        // given
        Reservation saved = reservationRepository.save(Reservation.createNew("이프", LocalDate.now().plusDays(1), theme, reservationTime));

        // when
        reservationRepository.delete(saved.getId());

        // then
        assertThat(dataSource.hasReservationById(saved.getId())).isFalse();
    }

    @Test
    void 모든_예약_목록을_조회한다() {
        // given: 과거 부터 미래 예약 목록 주어짐
        dataSource.insertReservation("이프", LocalDate.now().minusDays(1), 1L, 1L);
        dataSource.insertReservation("이프", LocalDate.now(), 1L, 1L);
        dataSource.insertReservation("이프", LocalDate.now().plusDays(1), 1L, 1L);

        // when
        List<Reservation> reservations = reservationRepository.findAll();

        // then
        assertThat(reservations).hasSize(3);
    }

    @Test
    void 존재하는_ID로_예약을_조회하면_Optional에_담겨_반환된다() {
        // given
        Reservation reservation = Reservation.createNew("이프", LocalDate.now().plusDays(1), theme, reservationTime);
        Reservation saved = reservationRepository.save(reservation);

        // when:
        Optional<Reservation> find = reservationRepository.findById(saved.getId());

        // then
        assertThat(find).isPresent();
    }

    @Test
    void 존재하지_않는_ID로_예약을_조회하면_빈_Optional이_반환된다() {
        // when:
        Optional<Reservation> find = reservationRepository.findById(1L);

        // then
        assertThat(find).isEmpty();
    }

    @Test
    void 예약을_수정한다() {
        // given
        Reservation saved = reservationRepository.save(
                Reservation.createNew("이프", LocalDate.now().plusDays(1), theme, reservationTime)
        );

        Reservation updated = new Reservation(
                saved.getId(),
                "아루",
                LocalDate.now().plusDays(2),
                theme,
                reservationTime
        );

        // when
        reservationRepository.update(updated);

        // then
        Optional<Reservation> find = reservationRepository.findById(saved.getId());

        assertThat(find).isPresent();
        assertThat(find.get().getName()).isEqualTo("아루");
        assertThat(find.get().getDate()).isEqualTo(LocalDate.now().plusDays(2));
    }
}
