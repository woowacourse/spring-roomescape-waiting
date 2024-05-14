package roomescape.repository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import javax.sql.DataSource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.support.DependencyInjectionTestExecutionListener;
import roomescape.domain.member.Member;
import roomescape.domain.reservation.Reservation;
import roomescape.domain.reservation.ReservationDate;
import roomescape.domain.reservation.ReservationTime;
import roomescape.domain.reservation.Theme;
import roomescape.repository.rowmapper.MemberRowMapper;
import roomescape.repository.rowmapper.ReservationRowMapper;
import roomescape.repository.rowmapper.ReservationTimeRowMapper;
import roomescape.repository.rowmapper.ThemeRowMapper;
import roomescape.service.dto.reservation.ReservationSearchParams;

@TestExecutionListeners(value = {
        DatabaseCleanupListener.class,
        DependencyInjectionTestExecutionListener.class
})
@JdbcTest
class JdbcReservationRepositoryTest {

    @Autowired
    private DataSource dataSource;
    private JdbcReservationRepository reservationRepository;
    private JdbcReservationTimeRepository reservationTimeRepository;
    private JdbcThemeRepository themeRepository;
    private JdbcMemberRepository memberRepository;

    private final String startAt1 = "13:00";
    private final String startAt2 = "15:00";

    private final ReservationTime time1 = new ReservationTime(null, startAt1);
    private final ReservationTime time2 = new ReservationTime(null, startAt2);

    private final Theme theme1 = new Theme(null, "공포", "난이도 1", "hi.jpg");
    private final Theme theme2 = new Theme(null, "우테코", "난이도 2", "hi.jpg");

    private final Member member1 = new Member(null, "t1@t1.com", "123", "러너덕", "MEMBER");
    private final Member member2 = new Member(null, "t2@t2.com", "124", "재즈", "MEMBER");

    private final Reservation reservation1 = new Reservation(
            null, new Member(1L, "t1@t1.com", "1234", "러너덕", "MEMBER"),
            new Theme(1L, null, null, null),
            new ReservationDate("2023-09-08"),
            new ReservationTime(1L, (LocalTime) null)
    );
    private final Reservation reservation2 = new Reservation(
            null, new Member(2L, "t2@t2.com", "12345", "재즈", "MEMBER"),
            new Theme(2L, null, null, null),
            new ReservationDate("2024-04-22"),
            new ReservationTime(2L, (LocalTime) null)
    );

    @BeforeEach
    void setUp() {
        memberRepository = new JdbcMemberRepository(dataSource, new MemberRowMapper());
        reservationRepository = new JdbcReservationRepository(dataSource, new ReservationRowMapper());
        reservationTimeRepository = new JdbcReservationTimeRepository(dataSource, new ReservationTimeRowMapper());
        themeRepository = new JdbcThemeRepository(dataSource, new ThemeRowMapper());
        initializeTimesAndThemeAndMemberData();
    }

    private void initializeTimesAndThemeAndMemberData() {
        reservationTimeRepository.insertReservationTime(time1);
        reservationTimeRepository.insertReservationTime(time2);
        themeRepository.insertTheme(theme1);
        themeRepository.insertTheme(theme2);
        memberRepository.insertMember(member1);
        memberRepository.insertMember(member2);
    }

    @Test
    @DisplayName("검색 조건에 따라 저장된 예약 정보를 가져온다")
    void find_all_reservations() {
        reservationRepository.insertReservation(reservation1);
        reservationRepository.insertReservation(reservation2);

        ReservationSearchParams params1 = new ReservationSearchParams(null, null, null, null);
        List<Reservation> reservations1 = reservationRepository.findReservationsWithParams(params1);

        ReservationSearchParams params2 = new ReservationSearchParams(1L, null, null, null);
        List<Reservation> reservations2 = reservationRepository.findReservationsWithParams(params2);

        LocalDate from = LocalDate.of(2024, 04, 25);
        ReservationSearchParams params3 = new ReservationSearchParams(null, null, from, null);
        List<Reservation> reservations3 = reservationRepository.findReservationsWithParams(params3);

        assertAll(
                () -> assertThat(reservations1.size()).isEqualTo(2),
                () -> assertThat(reservations2.size()).isEqualTo(1),
                () -> assertThat(reservations3.size()).isEqualTo(0)
        );
    }

    @Test
    @DisplayName("예약을 저장한다.")
    void save_reservation() {
        Reservation reservation = reservationRepository.insertReservation(reservation2);

        assertAll(
                () -> assertThat(reservation.getMemberName()).isEqualTo("재즈"),
                () -> assertThat(reservation.getTheme().getId()).isEqualTo(2),
                () -> assertThat(reservation.getTheme().getName()).isEqualTo("우테코"),
                () -> assertThat(reservation.getTheme().getDescription()).isEqualTo("난이도 2"),
                () -> assertThat(reservation.getTheme().getThumbnail()).isEqualTo("hi.jpg"),
                () -> assertThat(reservation.getDate()).isEqualTo("2024-04-22"),
                () -> assertThat(reservation.getTimeId()).isEqualTo(2),
                () -> assertThat(reservation.getTimeStartAt()).isEqualTo("15:00")
        );
    }

    @Test
    @DisplayName("예약을 id로 삭제한다.")
    void delete_reservation_by_id() {
        ReservationSearchParams params = new ReservationSearchParams(null, null, null, null);

        reservationRepository.insertReservation(reservation1);
        int beforeSize = reservationRepository.findReservationsWithParams(params).size();

        reservationRepository.deleteReservationById(1L);
        int afterSize = reservationRepository.findReservationsWithParams(params).size();

        assertAll(
                () -> assertThat(beforeSize).isEqualTo(1),
                () -> assertThat(afterSize).isEqualTo(0)
        );
    }

    @Test
    @DisplayName("예약 존재 여부를 시간 id로 판단한다.")
    void is_reservation_exists_by_time_id() {
        Reservation savedReservation = reservationRepository.insertReservation(reservation1);

        boolean exist = reservationRepository.isReservationExistsByTimeId(savedReservation.getTimeId());
        boolean notExist = reservationRepository.isReservationExistsById(2L);

        assertAll(
                () -> assertThat(exist).isTrue(),
                () -> assertThat(notExist).isFalse()
        );
    }

    @Test
    @DisplayName("예약 존재 여부를 테마 id로 판단한다.")
    void is_reservation_exists_by_theme_id() {
        Reservation savedReservation = reservationRepository.insertReservation(reservation1);

        boolean exist = reservationRepository.isReservationExistsById(savedReservation.getThemeId());
        boolean notExist = reservationRepository.isReservationExistsById(2L);

        assertAll(
                () -> assertThat(exist).isTrue(),
                () -> assertThat(notExist).isFalse()
        );
    }

    @Test
    @DisplayName("예약 존재 여부를 id로 판단한다.")
    void is_reservation_exists_by_id() {
        Reservation savedReservation = reservationRepository.insertReservation(reservation1);

        boolean exist = reservationRepository.isReservationExistsById(savedReservation.getId());
        boolean notExist = reservationRepository.isReservationExistsById(2L);

        assertAll(
                () -> assertThat(exist).isTrue(),
                () -> assertThat(notExist).isFalse()
        );
    }

    @Test
    @DisplayName("예약 테이블에 테마, 시간, 날짜가 동일한 예약이 존재하는지 판단한다.")
    void is_reservation_exists_by_date_and_time_id_and_theme_id() {
        Reservation inputReservation = new Reservation(
                3L,
                new Member(3L, "t3@t3.com", "12", "재즈덕", "MEMBER"),
                new Theme(1L, null, null, null),
                new ReservationDate("2023-09-08"),
                new ReservationTime(1L, (LocalTime) null)
        );
        reservationRepository.insertReservation(reservation1);

        boolean isExists = reservationRepository.isReservationExistsByDateAndTimeIdAndThemeId(inputReservation);

        assertThat(isExists).isTrue();
    }
}
