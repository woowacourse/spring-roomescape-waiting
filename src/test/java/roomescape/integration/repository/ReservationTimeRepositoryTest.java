package roomescape.integration.repository;

import static org.assertj.core.api.Assertions.*;
import static org.assertj.core.api.SoftAssertions.assertSoftly;
import static roomescape.common.Constant.FIXED_CLOCK;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import roomescape.common.RepositoryBaseTest;
import roomescape.domain.member.Member;
import roomescape.domain.reservation.Reservation;
import roomescape.domain.reservation.ReservationDate;
import roomescape.domain.theme.Theme;
import roomescape.domain.time.AvailableReservationTime;
import roomescape.domain.time.ReservationTime;
import roomescape.integration.fixture.MemberDbFixture;
import roomescape.integration.fixture.ReservationDateFixture;
import roomescape.integration.fixture.ReservationDbFixture;
import roomescape.integration.fixture.ReservationTimeDbFixture;
import roomescape.integration.fixture.ThemeDbFixture;
import roomescape.repository.DynamicReservationSelectQuery;
import roomescape.repository.ReservationTimeRepository;

class ReservationTimeRepositoryTest extends RepositoryBaseTest {

    @Autowired
    private ReservationTimeRepository reservationTimeRepository;

    private static final String SELECT_BY_ID = "SELECT * FROM reservation_time WHERE id = ?";
    private static final String COUNT_BY_ID = "SELECT COUNT(*) FROM reservation_time WHERE id = ?";

    @Autowired
    private ReservationTimeDbFixture reservationTimeDbFixture;

    @Autowired
    private ThemeDbFixture themeDbFixture;

    @Autowired
    private ReservationDbFixture reservationDbFixture;

    @Autowired
    private MemberDbFixture memberDbFixture;

    @Test
    void 예약시간을_저장할_수_있다() {
        // given
        LocalTime time = LocalTime.of(10, 0);

        // when
        ReservationTime saved = reservationTimeRepository.save(time);

        // then
        Map<String, Object> row = jdbcTemplate.queryForMap(SELECT_BY_ID, saved.getId());
        assertThat(row.get("start_at")).isEqualTo("10:00");
    }

    @Test
    void 모든_예약시간을_조회할_수_있다() {
        // given
        LocalTime time1 = LocalTime.of(10, 0);
        LocalTime time2 = LocalTime.of(14, 30);

        ReservationTime saved1 = reservationTimeRepository.save(time1);
        ReservationTime saved2 = reservationTimeRepository.save(time2);

        // when
        List<ReservationTime> all = reservationTimeRepository.findAll();

        // then
        assertSoftly(softly -> {
            softly.assertThat(all).hasSize(2);
            softly.assertThat(all)
                    .extracting(ReservationTime::getId, ReservationTime::getStartAt)
                    .containsExactlyInAnyOrder(
                            tuple(saved1.getId(), time1),
                            tuple(saved2.getId(), time2)
                    );
        });
    }

    @Test
    void 예약시간을_ID로_조회할_수_있다() {
        // given
        ReservationTime saved = reservationTimeRepository.save(LocalTime.of(11, 0));

        // when
        Optional<ReservationTime> found = reservationTimeRepository.findById(saved.getId());

        // then
        assertSoftly(softly -> {
            assertThat(found).isPresent();
            assertThat(found.get().getStartAt()).isEqualTo(LocalTime.of(11, 0));
        });
    }

    @Test
    void 존재하지_않는_ID로_조회하면_빈값을_반환한다() {
        // when
        Optional<ReservationTime> found = reservationTimeRepository.findById(999L);

        // then
        assertThat(found).isNotPresent();
    }

    @Test
    void 예약시간을_삭제할_수_있다() {
        // given
        ReservationTime saved = reservationTimeRepository.save(LocalTime.of(15, 0));

        // when
        reservationTimeRepository.deleteById(saved.getId());

        // then
        Integer count = jdbcTemplate.queryForObject(COUNT_BY_ID, Integer.class, saved.getId());
        assertThat(count).isEqualTo(0);
    }

    @Test
    void 특정_시간이_존재하는지_확인할_수_있다() {
        // given
        reservationTimeRepository.save(LocalTime.of(16, 0));

        // when
        boolean exists = reservationTimeRepository.existByStartAt(LocalTime.of(16, 0));
        boolean notExists = reservationTimeRepository.existByStartAt(LocalTime.of(17, 0));

        // then
        assertSoftly(softly -> {
            softly.assertThat(exists).isTrue();
            softly.assertThat(notExists).isFalse();
        });
    }

    @Test
    void 예약_가능_시간들을_조회할_수_있다() {
        // given
        ReservationTime 예약시간 = reservationTimeDbFixture.예약시간_10시();
        reservationTimeDbFixture.예약시간_11시();
        Theme 공포 = themeDbFixture.공포();
        Member member = memberDbFixture.한스_leehyeonsu4888_지메일_일반_멤버();
        reservationDbFixture.예약_25_4_22(예약시간, 공포, member);

        // when
        List<AvailableReservationTime> available = reservationTimeRepository.findAllAvailableReservationTimes(
                ReservationDateFixture.예약날짜_25_4_22.getDate(), 공포.getId());

        // then
        assertSoftly(softly -> {
            softly.assertThat(available).hasSize(2);
            softly.assertThat(available.get(0).time().getStartAt()).isEqualTo(LocalTime.of(10, 0));
            softly.assertThat(available.get(0).available()).isTrue();
            softly.assertThat(available.get(1).time().getStartAt()).isEqualTo(LocalTime.of(11, 0));
            softly.assertThat(available.get(1).available()).isFalse();
            softly.assertAll();
        });
    }

    @Test
    void 시작일을_조건으로_예약을_조회한다() {
        // given
        ReservationTime 열시 = reservationTimeDbFixture.예약시간_10시();
        Theme 공포 = themeDbFixture.공포();
        Theme 로맨스 = themeDbFixture.로맨스();
        Member 한스 = memberDbFixture.한스_leehyeonsu4888_지메일_일반_멤버();
        ReservationDate 예약날짜_7일전 = ReservationDateFixture.예약날짜_7일전;
        ReservationDate 예약날짜_오늘 = ReservationDateFixture.예약날짜_오늘;
        Reservation 예약_7일전 = reservationDbFixture.예약_생성(예약날짜_7일전, 열시, 공포, 한스);
        Reservation 예약_오늘 = reservationDbFixture.예약_생성(예약날짜_오늘, 열시, 로맨스, 한스);

        // when
        List<Reservation> allReservations = new DynamicReservationSelectQuery(jdbcTemplate)
                .addFromDateCondition(LocalDate.now(FIXED_CLOCK).minusDays(3))
                .findAllReservations();

        // then
        assertSoftly(softly -> {
            softly.assertThat(allReservations).hasSize(1);
            Reservation reservation = allReservations.getFirst();
            softly.assertThat(reservation.getId()).isEqualTo(예약_오늘.getId());
        });
    }

    @Test
    void 종료일을_조건으로_예약을_조회한다() {
        // given
        ReservationTime 열시 = reservationTimeDbFixture.예약시간_10시();
        Theme 공포 = themeDbFixture.공포();
        Theme 로맨스 = themeDbFixture.로맨스();
        Member 한스 = memberDbFixture.한스_leehyeonsu4888_지메일_일반_멤버();
        ReservationDate 예약날짜_7일전 = ReservationDateFixture.예약날짜_7일전;
        ReservationDate 예약날짜_오늘 = ReservationDateFixture.예약날짜_오늘;
        Reservation 예약_7일전 = reservationDbFixture.예약_생성(예약날짜_7일전, 열시, 공포, 한스);
        Reservation 예약_오늘 = reservationDbFixture.예약_생성(예약날짜_오늘, 열시, 로맨스, 한스);

        // when
        List<Reservation> allReservations = new DynamicReservationSelectQuery(jdbcTemplate)
                .addToDateCondition(LocalDate.now(FIXED_CLOCK))
                .findAllReservations();

        // then
        assertThat(allReservations).hasSize(2);
    }

    @Test
    void 테마를_조건으로_예약을_조회한다() {
        // given
        ReservationTime 열시 = reservationTimeDbFixture.예약시간_10시();
        Theme 공포 = themeDbFixture.공포();
        Theme 로맨스 = themeDbFixture.로맨스();
        Member 한스 = memberDbFixture.한스_leehyeonsu4888_지메일_일반_멤버();
        ReservationDate 예약날짜_7일전 = ReservationDateFixture.예약날짜_7일전;
        ReservationDate 예약날짜_오늘 = ReservationDateFixture.예약날짜_오늘;
        Reservation 예약_7일전 = reservationDbFixture.예약_생성(예약날짜_7일전, 열시, 공포, 한스);
        Reservation 예약_오늘 = reservationDbFixture.예약_생성(예약날짜_오늘, 열시, 로맨스, 한스);

        // when
        List<Reservation> allReservations = new DynamicReservationSelectQuery(jdbcTemplate)
                .addThemeCondition(공포.getId())
                .findAllReservations();

        // then
        assertSoftly(softly -> {
            softly.assertThat(allReservations).hasSize(1);
            Reservation reservation = allReservations.getFirst();
            softly.assertThat(reservation.getId()).isEqualTo(예약_7일전.getId());
        });
    }

    @Test
    void 예약자를_조건으로_예약을_조회한다() {
        // given
        ReservationTime 열시 = reservationTimeDbFixture.예약시간_10시();
        Theme 공포 = themeDbFixture.공포();
        Theme 로맨스 = themeDbFixture.로맨스();
        Member 한스 = memberDbFixture.한스_leehyeonsu4888_지메일_일반_멤버();
        ReservationDate 예약날짜_7일전 = ReservationDateFixture.예약날짜_7일전;
        ReservationDate 예약날짜_오늘 = ReservationDateFixture.예약날짜_오늘;
        Reservation 예약_7일전 = reservationDbFixture.예약_생성(예약날짜_7일전, 열시, 공포, 한스);
        Reservation 예약_오늘 = reservationDbFixture.예약_생성(예약날짜_오늘, 열시, 로맨스, 한스);

        // when
        List<Reservation> allReservations = new DynamicReservationSelectQuery(jdbcTemplate)
                .addMemberCondition(한스.getId())
                .findAllReservations();

        // then
        assertThat(allReservations).hasSize(2);
    }
}
