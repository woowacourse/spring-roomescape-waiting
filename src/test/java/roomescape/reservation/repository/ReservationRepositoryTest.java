package roomescape.reservation.repository;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;

import roomescape.member.model.Member;
import roomescape.member.model.MemberRole;
import roomescape.reservation.model.Reservation;
import roomescape.reservation.model.ReservationDate;
import roomescape.reservation.model.ReservationTime;
import roomescape.reservation.model.Theme;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

@SpringBootTest
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
class ReservationRepositoryTest {

    @Autowired
    private ReservationRepository reservationRepository;

    @DisplayName("모든 예약 정보를 조회한다.")
    @Test
    void findAllTest() {
        // When
        final List<Reservation> reservations = reservationRepository.findAll();

        // Then
        assertThat(reservations).hasSize(16);
    }

    @DisplayName("예약 정보를 저장한다.")
    @Test
    void saveTest() {
        // Given
        final String clientName = "브라운";
        final LocalDate reservationDate = LocalDate.now().plusDays(10);
        final ReservationTime reservationTime = new ReservationTime(1L, LocalTime.of(10, 10));
        final Theme theme = Theme.of(1L, "테바의 비밀친구", "테바의 은밀한 비밀친구", "대충 테바 사진 링크");
        final Member member = Member.createMemberWithId(1L, MemberRole.USER, "password1111", "kelly", "kelly6bf@mail.com");
        final Reservation reservation = Reservation.of( reservationDate, reservationTime, theme, member);

        // When
        final Reservation savedReservation = reservationRepository.save(reservation);

        // Then
        final List<Reservation> reservations = reservationRepository.findAll();
        assertAll(
                () -> assertThat(reservations).hasSize(17),
                () -> assertThat(savedReservation.getId()).isEqualTo(17L),
                () -> assertThat(savedReservation.getMember()).isEqualTo(reservation.getMember()),
                () -> assertThat(savedReservation.getDate()).isEqualTo(reservation.getDate()),
                () -> assertThat(savedReservation.getTime()).isEqualTo(reservation.getTime())
        );
    }

    @DisplayName("예약 정보를 삭제한다.")
    @Test
    void deleteByIdTest() {
        // When
        reservationRepository.deleteById(1L);

        // Then
        final long count = reservationRepository.count();
        assertThat(count).isEqualTo(15);
    }

    @DisplayName("특정 날짜와 시간 아이디를 가진 예약이 존재하는지 조회한다.")
    @Test
    void existByDateAndTimeIdTest() {
        // Given
        final ReservationDate reservationDate = new ReservationDate(LocalDate.now().plusDays(2));
        final Long timeId = 4L;
        final Long themeId = 9L;

        // When
        final boolean isExist = reservationRepository.existsByDateAndTimeIdAndThemeId(
                reservationDate, timeId, themeId);

        // Then
        assertThat(isExist).isTrue();
    }

    @DisplayName("특정 시간 아이디를 가진 예약이 존재하는지 조회한다.")
    @Test
    void existsByTimeIdTest() {
        // Given
        final Long timeId = 1L;

        // When
        final boolean isExist = reservationRepository.existsByTimeId(timeId);

        // Then
        assertThat(isExist).isTrue();
    }

    @DisplayName("특정 날짜와 테마 아이디를 가진 예약 정보를 모두 조회한다.")
    @Test
    void findAllByDateAndThemeIdTest() {
        // Given
        final ReservationDate reservationDate = new ReservationDate(LocalDate.now().minusDays(3));
        final Long themeId = 1L;

        // When
        final List<Reservation> allByDateAndThemeId = reservationRepository.findAllByDateAndThemeId(reservationDate, themeId);

        // Then
        assertThat(allByDateAndThemeId).hasSize(2);
    }

    @DisplayName("테마 id, 시작일, 종료일을 기준으로 예약 정보를 조회한다.")
    @Test
    void searchReservationsWithThemeIdAndFromAndToConditionTest() {
        // Given
        final long themeId = 1L;
        final LocalDate from = LocalDate.now().minusDays(7);
        final LocalDate to = LocalDate.now().plusDays(1);

        // When
        final List<Reservation> reservations = reservationRepository.searchReservations(null, themeId, new ReservationDate(from), new ReservationDate(to));

        // Then
        assertThat(reservations).hasSize(4);
    }

    @DisplayName("시작일, 종료일을 기준으로 예약 정보를 조회한다.")
    @Test
    void searchReservationsWithFromAndToConditionTest() {
        // Given
        final LocalDate from = LocalDate.now().minusDays(7);
        final LocalDate to = LocalDate.now().plusDays(1);

        // When
        final List<Reservation> reservations = reservationRepository.searchReservations(null, null, new ReservationDate(from), new ReservationDate(to));

        // Then
        assertThat(reservations).hasSize(12);
    }

    @DisplayName("시작일 이후의 예약 정보를 조회한다.")
    @Test
    void searchReservationsAfterFromConditionTest() {
        // Given
        final LocalDate from = LocalDate.now().minusDays(7);

        // When
        final List<Reservation> reservations = reservationRepository.searchReservations(null, null, new ReservationDate(from), null);

        // Then
        assertThat(reservations).hasSize(16);
    }

    @DisplayName("회원 id, 테마 id, 시작일, 종료일을 기준으로 예약 정보를 조회한다.")
    @Test
    void searchReservationsWithAllConditionTest() {
        // Given
        final long memberId = 1L;
        final long themeId = 11L;
        final LocalDate from = LocalDate.now().minusDays(7);
        final LocalDate to = LocalDate.now().plusDays(1);

        // When
        final List<Reservation> reservations = reservationRepository.searchReservations(memberId, themeId, new ReservationDate(from), new ReservationDate(to));

        // Then
        assertThat(reservations).hasSize(1);
    }
}
