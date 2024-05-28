package roomescape.repository;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import roomescape.domain.Member;
import roomescape.domain.Password;
import roomescape.domain.Reservation;
import roomescape.domain.ReservationStatus;
import roomescape.domain.ReservationTime;
import roomescape.domain.Role;
import roomescape.domain.Theme;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class ReservationRepositoryTest {
    @Autowired
    private ReservationRepository reservationRepository;

    private long getItemSize() {
        return reservationRepository.findAll().size();
    }

    @DisplayName("Db에 등록된 모든 예약 목록을 조회한다.")
    @Test
    void given_when_findAll_then_returnReservations() {
        //when
        final List<Reservation> reservations = reservationRepository.findAll();
        //then
        assertThat(reservations).hasSize(10);
    }

    @DisplayName("Db에 예약 정보를 저장한다.")
    @Test
    void given_reservation_when_create_then_returnCreatedReservationId() {
        //given
        Reservation expected = Reservation.reserved(
                new Member("poke@test.com", new Password("password", "salt"), "poke", Role.USER),
                LocalDate.parse("2099-01-11"),
                new ReservationTime(1L, LocalTime.parse("10:00")),
                new Theme(1L, "name", "description", "thumbnail"));
        //when
        final Reservation savedReservation = reservationRepository.save(expected);
        //then
        assertThat(savedReservation).isEqualTo(expected);
    }

    @DisplayName("예약 id로 Db에서 예약 정보를 삭제한다.")
    @Test
    void given_when_delete_then_deletedFromDb() {
        //given
        long initialSize = getItemSize();
        //when
        reservationRepository.deleteById(1L);
        long afterSize = getItemSize();
        //then
        assertThat(afterSize).isEqualTo(initialSize - 1);
    }

    @DisplayName("예약 날짜, 시간Id, 테마Id, 사용자Id 를 통해 중복 예약이 있는지 확인할 수 있다.")
    @Test
    void given_dateAndTimeIdAndThemeIdAndMemberId_when_isExist_then_true() {
        //given
        LocalDate date = LocalDate.parse("2024-05-01");
        Long timeId = 3L;
        Long themeId = 2L;
        Long memberId = 1L;
        //when
        boolean actual = reservationRepository
                .existsByDateAndTime_IdAndTheme_IdAndMember_Id(date, timeId, themeId, memberId);
        //then
        assertThat(actual).isTrue();
    }

    @DisplayName("예약 날짜, 시간Id, 테마Id를 통해 예약여부를 확인할 수 있다.")
    @Test
    void given_dateAndTimeIdAndThemeId_when_isExist_then_true() {
        //given
        LocalDate date = LocalDate.parse("2024-05-01");
        Long timeId = 3L;
        Long themeId = 2L;
        //when
        boolean actual = reservationRepository
                .existsByDateAndTime_IdAndTheme_Id(date, timeId, themeId);
        //then
        assertThat(actual).isTrue();
    }

    @DisplayName("시간 Id로 등록한 예약이 존재하는지 확인할 수 있다.")
    @Test
    void given_when_isExistByReservationId_then_true() {
        //given
        Long reservationId = 1L;
        //when
        boolean actual = reservationRepository.existsById(reservationId);
        //then
        assertThat(actual).isTrue();
    }

    @DisplayName("타임 Id로 등록한 예약이 존재하는지 확인할 수 있다.")
    @Test
    void given_timeId_when_existsByTime_Id_then_true() {
        //given
        Long timeId = 3L;
        //when
        boolean actual = reservationRepository.existsByTime_Id(timeId);
        //then
        assertThat(actual).isTrue();
    }

    @DisplayName("테마 Id로 등록한 예약이 존재하는지 확인할 수 있다.")
    @Test
    void given_themeId_when_existsByTheme_Id_then_true() {
        //given
        Long themeId = 2L;
        //when
        boolean actual = reservationRepository.existsByTheme_Id(themeId);
        //then
        assertThat(actual).isTrue();
    }

    @DisplayName("memberId, themeId, 기간을 이용하여 예약을 조회할 수 있다.")
    @Test
    void given_memberIdThemeIdAndPeriod_when_find_then_Reservations() {
        //given
        Long themeId = 2L;
        Long memberId = 1L;
        LocalDate dateFrom = LocalDate.parse("2024-04-30");
        LocalDate dateTo = LocalDate.parse("2024-05-01");
        //when
        final List<Reservation> reservations = reservationRepository
                .findAllByTheme_IdAndMember_IdAndDateBetween(themeId, memberId, dateFrom, dateTo);
        //then
        assertThat(reservations.size()).isEqualTo(3);
    }

    @DisplayName("특정 member의 예약을 조회할 수 있다.")
    @Test
    void given_member_when_findByMember_then_Reservations() {
        //given
        Password password = new Password("hashedpassword", "salt");
        Member member = new Member(1L, "user@test.com", password, "poke", Role.USER);
        //when
        final List<Reservation> reservations = reservationRepository.findByMember(member);
        //then
        assertThat(reservations).hasSize(8);
    }

    @DisplayName("예약 날짜와 테마Id에 대한 예약을 조회할 수 있다.")
    @Test
    void given_dateAndThemeId_when_findByDateAndThemeId_then_Reservations() {
        //given
        LocalDate date = LocalDate.parse("2024-05-01");
        Long themeId = 2L;
        //when
        final List<Reservation> reservations = reservationRepository.findByDateAndTheme_Id(date, themeId);
        //then
        assertThat(reservations).hasSize(2);
    }

    @DisplayName("예약 날짜, 시간Id, 테마Id에 대한 예약을 조회할 수 있다.")
    @Test
    void given_dateAndTimeIdAndThemeId_when_findByDateAndTimeIdAndThemeId_then_Reservations() {
        //given
        LocalDate date = LocalDate.parse("2999-04-30");
        Long timeId = 1L;
        Long themeId = 1L;
        //when
        final List<Reservation> reservations = reservationRepository.findByDateAndTime_IdAndTheme_Id(date, timeId, themeId);
        //then
        assertThat(reservations).hasSize(3);
    }

    @DisplayName("예약 날짜, 시간Id, 테마Id에 대해 우선 대기중인 예약을 조회할 수 있다.")
    @Test
    void given_when_findFirstByDateAndTime_IdAndTheme_IdAndStatus_then_Reservation() {
        //given
        LocalDate date = LocalDate.parse("2999-04-30");
        Long timeId = 1L;
        Long themeId = 1L;
        ReservationStatus status = ReservationStatus.WAITING;
        //when
        final Reservation reservation = reservationRepository
                .findFirstByDateAndTime_IdAndTheme_IdAndStatus(date, timeId, themeId, status).get();
        //then
        assertThat(reservation.getId()).isEqualTo(9);
    }

    @DisplayName("예약 Id가 회원의 소유자인지 검증한다.")
    @Test
    void given_when_existByIdAndMemberId_then_True() {
        //given
        Long reservationId = 1L;
        Long memberId = 1L;
        //when
        boolean actual = reservationRepository.existsByIdAndMember_Id(reservationId, memberId);
        //then
        assertThat(actual).isTrue();
    }

    @DisplayName("예약 상태에 맞는 예약들을 반환한다.")
    @Test
    void given_when_findByStatus_then_True() {
        //when
        final List<Reservation> reservations = reservationRepository.findByStatus(ReservationStatus.WAITING);
        //then
        assertThat(reservationRepository.findByStatus(ReservationStatus.WAITING)).hasSize(2);
    }
}
