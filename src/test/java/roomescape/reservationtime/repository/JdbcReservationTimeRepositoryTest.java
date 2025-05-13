package roomescape.reservationtime.repository;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabase;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseBuilder;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseType;
import roomescape.member.domain.Member;
import roomescape.reservation.domain.Reservation;
import roomescape.reservation.repository.JdbcReservationRepository;
import roomescape.reservationtime.domain.ReservationTime;
import roomescape.reservationtime.dto.AvailableReservationTimeResponse;
import roomescape.theme.domain.Theme;

class JdbcReservationTimeRepositoryTest {

    private static EmbeddedDatabase db;
    private JdbcReservationTimeRepository repository;
    private JdbcReservationRepository jdbcReservationRepository;

    @BeforeEach
    void setUp() {
        db = new EmbeddedDatabaseBuilder()
                .setType(EmbeddedDatabaseType.H2)
                .addScript("classpath:schema.sql")
                .addScript("classpath:data.sql")
                .build();
        repository = new JdbcReservationTimeRepository(db);
        jdbcReservationRepository = new JdbcReservationRepository(db);
    }

    @AfterEach
    void shutdownDatabase() {
        db.shutdown();
    }

    @Test
    void 예약_시간을_올바르게_저장한다() {
        // given
        ReservationTime reservationTime = new ReservationTime(3L, LocalTime.of(11, 0));

        // when
        ReservationTime savedReservationTime = repository.save(reservationTime);

        // then
        SoftAssertions.assertSoftly(soft -> {
            soft.assertThat(reservationTime.getId()).isNotNull();
            soft.assertThat(reservationTime.getStartAt()).isEqualTo(savedReservationTime.getStartAt());
        });
    }

    @Test
    void 모든_예약_시간을_조회한다() {
        // given
        // when
        List<ReservationTime> reservationTimes = repository.findAll();

        // then
        assertThat(reservationTimes).hasSize(2);
    }

    @Test
    void 예약가능한_모든_시간을_조회한다() {
        // given
        jdbcReservationRepository.save(
                new Reservation(
                        LocalDate.of(2999, 5, 1),
                        new ReservationTime(1L, LocalTime.of(10, 0)),
                        new Theme(1L, "이름1", "썸네일1", "설명1"),
                        new Member("테스트", "테스트@naver.com", "1234")
                )
        );
        jdbcReservationRepository.save(
                new Reservation(
                        LocalDate.of(2999, 5, 1),
                        new ReservationTime(2L, LocalTime.of(11, 0)),
                        new Theme(1L, "이름1", "썸네일1", "설명1"),
                        new Member("테스트", "테스트@naver.com", "1234")
                )
        );

        // when
        List<AvailableReservationTimeResponse> allAvailable = repository.findAllAvailable(
                LocalDate.of(2999, 5, 1), 1L);

        // then
        SoftAssertions.assertSoftly(soft -> {
                    soft.assertThat(allAvailable).hasSize(2);
                    soft.assertThat(allAvailable.get(0).alreadyBooked()).isTrue();
                    soft.assertThat(allAvailable.get(1).alreadyBooked()).isTrue();
                }
        );
    }

    @Test
    void id에_알맞은_예약_시간을_삭제한다() {
        // given
        // when
        repository.deleteById(1L);
        List<ReservationTime> reservationTimes = repository.findAll();

        // then
        assertThat(reservationTimes).hasSize(1)
                .extracting(ReservationTime::getId)
                .doesNotContain(1L);
    }

    @Test
    void id에_알맞은_예약_시간을_가져온다() {
        // given
        Long id = 1L;

        // when
        ReservationTime reservationTime = repository.findById(id).get();

        // then
        assertThat(reservationTime.getId()).isEqualTo(id);
    }

    @Test
    void 존재하지_않는_id면_빈_Optional을_반환한다() {
        // given
        Long invalidId = 999L;
        // when
        // then
        assertThat(repository.findById(invalidId)).isEmpty();
    }
}
