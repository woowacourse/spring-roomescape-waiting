package roomescape.unit.infrastructure;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.jdbc.Sql;
import roomescape.domain.Member;
import roomescape.domain.Reservation;
import roomescape.domain.ReservationTime;
import roomescape.domain.Theme;
import roomescape.dto.request.ReservationCondition;
import roomescape.infrastructure.JpaMemberRepository;
import roomescape.infrastructure.JpaReservationRepository;
import roomescape.infrastructure.JpaReservationTimeRepository;
import roomescape.infrastructure.JpaThemeRepository;
import roomescape.infrastructure.ReservationRepositoryAdaptor;

@DataJpaTest
@Sql(value =
        {
                "/sql/testMember.sql",
                "/sql/testReservationTime.sql",
                "/sql/testTheme.sql",
        }
)
class ReservationRepositoryAdaptorTest {

    @Autowired
    private JpaReservationRepository jpaReservationRepository;

    @Autowired
    private JpaThemeRepository jpaThemeRepository;

    @Autowired
    private JpaReservationTimeRepository jpaReservationTimeRepository;

    @Autowired
    private JpaMemberRepository jpaMemberRepository;

    private ReservationRepositoryAdaptor reservationRepositoryAdaptor;

    private List<Reservation> reservations;


    private Member member1;
    private Member member2;
    private Member member3;

    private ReservationTime reservationTime1;
    private ReservationTime reservationTime2;
    private ReservationTime reservationTime3;

    private Theme theme1;
    private Theme theme2;
    private Theme theme3;
    private Theme theme4;
    
    @BeforeEach
    void setUp() {
        reservationRepositoryAdaptor = new ReservationRepositoryAdaptor(jpaReservationRepository);
        List<Member> members = jpaMemberRepository.findAll();
        List<ReservationTime> reservationTimes = jpaReservationTimeRepository.findAll();
        List<Theme> themes = jpaThemeRepository.findAll();

        member1 = members.get(0);
        member2 = members.get(1);
        member3 = members.get(2);

        reservationTime1 = reservationTimes.get(0);
        reservationTime2 = reservationTimes.get(1);
        reservationTime3 = reservationTimes.get(2);

        theme1 = themes.get(0);
        theme2 = themes.get(1);
        theme3 = themes.get(2);
        theme4 = themes.get(3);

        reservationRepositoryAdaptor.save(
                Reservation.createWithoutId(member1, LocalDate.now().minusDays(1), reservationTime1, theme1));
        reservationRepositoryAdaptor.save(
                Reservation.createWithoutId(member2, LocalDate.now().minusDays(2), reservationTime2, theme2));
        reservationRepositoryAdaptor.save(
                Reservation.createWithoutId(member3, LocalDate.now().minusDays(3), reservationTime3, theme3));
        reservationRepositoryAdaptor.save(
                Reservation.createWithoutId(member1, LocalDate.now().minusDays(4), reservationTime3, theme4));

        reservations = reservationRepositoryAdaptor.findAll();
    }

    @Test
    void 모든_예약_조회_테스트() {
        assertThat(reservations.size()).isEqualTo(4);
    }

    @Test
    void 예약_저장_테스트() {
        //given
        Reservation reservation = Reservation.createWithoutId(member1, LocalDate.now().plusDays(1), reservationTime1,
                theme2);

        //when & then
        reservationRepositoryAdaptor.save(reservation);
        List<Reservation> reservations = reservationRepositoryAdaptor.findAll();

        assertThat(reservations.size()).isEqualTo(5);
    }

    @Test
    void 예약_삭제_태스트() {
        //given
        Long reservationId = reservations.getFirst().getId();

        //when & then
        reservationRepositoryAdaptor.deleteById(reservationId);
        List<Reservation> reservations = reservationRepositoryAdaptor.findAll();

        assertThat(reservations.size()).isEqualTo(3);
    }

    @Test
    void 예약시간id로_예약_조회_테스트() {
        List<Reservation> reservations = reservationRepositoryAdaptor.findByReservationTimeId(reservationTime1.getId());

        assertThat(reservations.size()).isEqualTo(1);
    }

    @Test
    void 테마id로_예약_조회_테스트() {
        List<Reservation> reservations = reservationRepositoryAdaptor.findByThemeId(theme1.getId());

        assertThat(reservations.size()).isEqualTo(1);
    }

    @Test
    void 멤버id로_예약_조회_테스트() {
        List<Reservation> reservations = reservationRepositoryAdaptor.findByMemberId(member1.getId());

        assertThat(reservations.size()).isEqualTo(2);
    }

    @Test
    void id로_예약_조회_테스트() {
        //given
        Long reservationId = reservations.getFirst().getId();

        //when & then
        Optional<Reservation> reservation = reservationRepositoryAdaptor.findById(reservationId);

        assertThat(reservation).isPresent();
    }

    @Test
    void 날짜_예약시간_테마로_예약_조회() {
        Reservation reservation = reservationRepositoryAdaptor.findByDateAndReservationTimeAndTheme(
                LocalDate.now().minusDays(1), reservationTime1, theme1).get();

        assertAll(
                () -> assertThat(reservation.getDate()).isEqualTo(LocalDate.now().minusDays(1)),
                () -> assertThat(reservation.getReservationTime()).isEqualTo(reservationTime1),
                () -> assertThat(reservation.getTheme()).isEqualTo(theme1)
        );
    }

    @Test
    void 날짜_테마로_예약_조회() {
        List<Reservation> reservations = reservationRepositoryAdaptor.findByDateAndTheme(
                LocalDate.now().minusDays(1), theme1);

        assertThat(reservations.size()).isEqualTo(1);
    }

    @Test
    void 특정_날짜구간_사이_예약_조회() {
        List<Reservation> reservations = reservationRepositoryAdaptor.findByDateBetween(
                LocalDate.now().minusDays(4),
                LocalDate.now());

        assertThat(reservations.size()).isEqualTo(4);
    }

    @Test
    void 특정_조건으로_예약_조회() {
        //given
        ReservationCondition condition = new ReservationCondition(
                Optional.empty(),
                Optional.of(member1.getId()),
                Optional.of(LocalDate.now().minusDays(4)),
                Optional.of(LocalDate.now()));

        //when & then
        List<Reservation> reservations = reservationRepositoryAdaptor.findByCondition(condition);

        assertThat(reservations.size()).isEqualTo(2);
    }
}