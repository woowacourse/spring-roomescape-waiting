//package roomescape.integration.repository;
//
//import static org.assertj.core.api.Assertions.*;
//import static org.assertj.core.api.SoftAssertions.assertSoftly;
//import static roomescape.common.Constant.FIXED_CLOCK;
//
//import java.util.List;
//import java.util.Optional;
//import org.assertj.core.api.SoftAssertions;
//import org.junit.jupiter.api.Test;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.data.jpa.domain.Specification;
//import roomescape.common.RepositoryBaseTest;
//import roomescape.domain.member.Member;
//import roomescape.domain.member.MemberName;
//import roomescape.domain.reservation.Reservation;
//import roomescape.domain.reservation.ReservationDateTime;
//import roomescape.domain.reservation.schdule.ReservationDate;
//import roomescape.domain.theme.Theme;
//import roomescape.domain.time.ReservationTime;
//import roomescape.integration.fixture.MemberDbFixture;
//import roomescape.integration.fixture.MemberNameFixture;
//import roomescape.integration.fixture.ReservationDateFixture;
//import roomescape.integration.fixture.ReservationDbFixture;
//import roomescape.integration.fixture.ReservationTimeDbFixture;
//import roomescape.integration.fixture.ThemeDbFixture;
//import roomescape.repository.ReservationRepository;
//import roomescape.repository.ReservationSpecifications;
//
//class ReservationRepositoryTest extends RepositoryBaseTest {
//
//    @Autowired
//    private ReservationRepository reservationRepository;
//
//    @Autowired
//    private ReservationDbFixture reservationDbFixture;
//
//    @Autowired
//    private ReservationTimeDbFixture reservationTimeDbFixture;
//
//    @Autowired
//    private MemberDbFixture memberDbFixture;
//
//    @Autowired
//    private ThemeDbFixture themeDbFixture;
//
//    @Test
//    void 예약을_저장할_수_있다() {
//        // given
//        ReservationDate date = ReservationDateFixture.예약날짜_오늘;
//        ReservationTime time = reservationTimeDbFixture.예약시간_10시();
//        Theme theme = themeDbFixture.공포();
//        MemberName name = MemberNameFixture.한스;
//        ReservationDateTime dateTime = new ReservationDateTime(date, time, FIXED_CLOCK);
//        Member member = memberDbFixture.한스_leehyeonsu4888_지메일_일반_멤버();
//
//        // when
//        Reservation saved = reservationRepository.save(
//                new Reservation(null, member, dateTime.getReservationDate(), dateTime.getReservationTime(), theme));
//
//        // then
//        Reservation found = reservationRepository.findById(saved.getId()).get();
//        assertSoftly(softly -> {
//            softly.assertThat(found.getDate()).isEqualTo(date.date().toString());
//            softly.assertThat(found.getReservationTime().getId()).isEqualTo(time.getId());
//            softly.assertThat(found.getTheme().getId()).isEqualTo(theme.getId());
//            softly.assertAll();
//        });
//    }
//
//    @Test
//    void 예약을_모두_조회할_수_있다() {
//        // given
//        Reservation 예약1 = reservationDbFixture.예약_생성(
//                ReservationDateFixture.예약날짜_오늘,
//                reservationTimeDbFixture.예약시간_10시(),
//                themeDbFixture.공포(),
//                memberDbFixture.한스_leehyeonsu4888_지메일_일반_멤버()
//        );
//
//        Reservation 예약2 = reservationDbFixture.예약_생성(
//                ReservationDateFixture.예약날짜_7일전,
//                reservationTimeDbFixture.예약시간_11시(),
//                themeDbFixture.로맨스(),
//                memberDbFixture.한스_leehyeonsu4888_지메일_일반_멤버()
//        );
//
//        // when
//        List<Reservation> reservations = reservationRepository.findAll();
//
//        // then
//        assertSoftly(softly -> {
//            softly.assertThat(reservations).hasSize(2);
//            softly.assertThat(reservations).hasSize(2);
//            softly.assertThat(reservations).anySatisfy(row -> {
//                SoftAssertions nested = new SoftAssertions();
//                nested.assertThat(row.getDate()).isEqualTo(예약1.getDate());
//                nested.assertThat(row.getTimeId()).isEqualTo(예약1.getTimeId());
//                nested.assertThat(row.getTheme().getId()).isEqualTo(예약1.getTheme().getId());
//                nested.assertAll();
//            });
//        });
//    }
//
//    @Test
//    void ID로_예약을_조회할_수_있다() {
//        // given
//        Reservation 예약 = reservationDbFixture.예약_생성(
//                ReservationDateFixture.예약날짜_오늘,
//                reservationTimeDbFixture.예약시간_10시(),
//                themeDbFixture.공포(),
//                memberDbFixture.한스_leehyeonsu4888_지메일_일반_멤버()
//        );
//
//        // when
//        Optional<Reservation> found = reservationRepository.findById(예약.getId());
//
//        // then
//        assertThat(found).isPresent();
//        assertThat(found.get().getMember().getName()).isEqualTo(new MemberName("한스"));
//    }
//
//    @Test
//    void 예약을_삭제할_수_있다() {
//        // given
//        Reservation 예약 = reservationDbFixture.예약_생성(
//                ReservationDateFixture.예약날짜_오늘,
//                reservationTimeDbFixture.예약시간_10시(),
//                themeDbFixture.공포(),
//                memberDbFixture.한스_leehyeonsu4888_지메일_일반_멤버()
//        );
//
//        // when
//        reservationRepository.deleteById(예약.getId());
//
//        // then
//        Long count = reservationRepository.count();
//        assertThat(count).isZero();
//    }
//
//    @Test
//    void 동일한_시간에_예약이_존재하는지_확인할_수_있다() {
//        // given
//        ReservationDate date = ReservationDateFixture.예약날짜_오늘;
//        ReservationTime time = reservationTimeDbFixture.예약시간_10시();
//        Member member = memberDbFixture.한스_leehyeonsu4888_지메일_일반_멤버();
//        reservationDbFixture.예약_생성(date, time, themeDbFixture.공포(), member);
//
//        // when
//        boolean exists = reservationRepository.existsByReservationDateAndReservationTime_Id(date, time.getId());
//
//        // then
//        assertThat(exists).isTrue();
//    }
//
//    @Test
//    void 동일한_시간에_예약이_없으면_false를_반환한다() {
//        // given
//        ReservationDate date = ReservationDateFixture.예약날짜_오늘;
//        ReservationTime time = reservationTimeDbFixture.예약시간_10시();
//
//        // when
//        boolean exists = reservationRepository.existsByReservationDateAndReservationTime_Id(date, time.getId());
//
//        // then
//        assertThat(exists).isFalse();
//    }
//
//    @Test
//    void 특정_시간에_예약이_있는지_확인할_수_있다() {
//        // given
//        ReservationTime time = reservationTimeDbFixture.예약시간_10시();
//        Member member = memberDbFixture.한스_leehyeonsu4888_지메일_일반_멤버();
//        reservationDbFixture.예약_생성(
//                ReservationDateFixture.예약날짜_오늘, time, themeDbFixture.공포(), member
//        );
//
//        // when
//        boolean exists = reservationRepository.existsByReservationTime_Id(time.getId());
//
//        // then
//        assertThat(exists).isTrue();
//    }
//
//    @Test
//    void 특정_시간에_예약이_없으면_false를_반환한다() {
//        // given
//        ReservationTime time = reservationTimeDbFixture.예약시간_10시();
//
//        // when
//        boolean exists = reservationRepository.existsByReservationTime_Id(time.getId());
//
//        // then
//        assertThat(exists).isFalse();
//    }
//
//    @Test
//    void 특정_테마에_예약이_있는지_확인할_수_있다() {
//        // given
//        Theme theme = themeDbFixture.공포();
//        Member member = memberDbFixture.한스_leehyeonsu4888_지메일_일반_멤버();
//        reservationDbFixture.예약_생성(
//                ReservationDateFixture.예약날짜_오늘, reservationTimeDbFixture.예약시간_10시(), theme, member
//        );
//
//        // when
//        boolean exists = reservationRepository.existsByTheme_Id(theme.getId());
//
//        // then
//        assertThat(exists).isTrue();
//    }
//
//    @Test
//    void 특정_테마에_예약이_없으면_false를_반환한다() {
//        // given
//        Theme theme = themeDbFixture.공포();
//
//        // when
//        boolean exists = reservationRepository.existsByTheme_Id(theme.getId());
//
//        // then
//        assertThat(exists).isFalse();
//    }
//
//
//    @Test
//    void 시작일을_조건으로_예약을_조회한다() {
//        // given
//        ReservationTime 열시 = reservationTimeDbFixture.예약시간_10시();
//        Theme 공포 = themeDbFixture.공포();
//        Theme 로맨스 = themeDbFixture.로맨스();
//        Member 한스 = memberDbFixture.한스_leehyeonsu4888_지메일_일반_멤버();
//        ReservationDate 예약날짜_7일전 = ReservationDateFixture.예약날짜_7일전;
//        ReservationDate 예약날짜_오늘 = ReservationDateFixture.예약날짜_오늘;
//        Reservation 예약_7일전 = reservationDbFixture.예약_생성(예약날짜_7일전, 열시, 공포, 한스);
//        Reservation 예약_오늘 = reservationDbFixture.예약_생성(예약날짜_오늘, 열시, 로맨스, 한스);
//
//        // when
//        Specification<Reservation> spec = Specification
//                .where(ReservationSpecifications.hasMemberId(null)
//                        .and(ReservationSpecifications.hasThemeId(null))
//                        .and(ReservationSpecifications.dateAfterOrEqual(예약_오늘.getDate()))
//                        .and(ReservationSpecifications.dateBeforeOrEqual(null)));
//
//        List<Reservation> allReservations = reservationRepository.findAll(spec);
//
//        // then
//        assertSoftly(softly -> {
//            softly.assertThat(allReservations).hasSize(1);
//            Reservation reservation = allReservations.getFirst();
//            softly.assertThat(reservation.getId()).isEqualTo(예약_오늘.getId());
//        });
//    }
//
//    @Test
//    void 종료일을_조건으로_예약을_조회한다() {
//        // given
//        ReservationTime 열시 = reservationTimeDbFixture.예약시간_10시();
//        Theme 공포 = themeDbFixture.공포();
//        Theme 로맨스 = themeDbFixture.로맨스();
//        Member 한스 = memberDbFixture.한스_leehyeonsu4888_지메일_일반_멤버();
//        ReservationDate 예약날짜_7일전 = ReservationDateFixture.예약날짜_7일전;
//        ReservationDate 예약날짜_오늘 = ReservationDateFixture.예약날짜_오늘;
//        Reservation 예약_7일전 = reservationDbFixture.예약_생성(예약날짜_7일전, 열시, 공포, 한스);
//        Reservation 예약_오늘 = reservationDbFixture.예약_생성(예약날짜_오늘, 열시, 로맨스, 한스);
//
//        // when
//        Specification<Reservation> spec = Specification
//                .where(ReservationSpecifications.hasMemberId(null)
//                        .and(ReservationSpecifications.hasThemeId(null))
//                        .and(ReservationSpecifications.dateAfterOrEqual(예약_7일전.getDate()))
//                        .and(ReservationSpecifications.dateBeforeOrEqual(null)));
//
//        List<Reservation> allReservations = reservationRepository.findAll(spec);
//
//        // then
//        assertThat(allReservations).hasSize(2);
//    }
//
//    @Test
//    void 테마를_조건으로_예약을_조회한다() {
//        // given
//        ReservationTime 열시 = reservationTimeDbFixture.예약시간_10시();
//        Theme 공포 = themeDbFixture.공포();
//        Theme 로맨스 = themeDbFixture.로맨스();
//        Member 한스 = memberDbFixture.한스_leehyeonsu4888_지메일_일반_멤버();
//        ReservationDate 예약날짜_7일전 = ReservationDateFixture.예약날짜_7일전;
//        ReservationDate 예약날짜_오늘 = ReservationDateFixture.예약날짜_오늘;
//        Reservation 예약_7일전 = reservationDbFixture.예약_생성(예약날짜_7일전, 열시, 공포, 한스);
//        Reservation 예약_오늘 = reservationDbFixture.예약_생성(예약날짜_오늘, 열시, 로맨스, 한스);
//
//        // when
//        Specification<Reservation> spec = Specification
//                .where(ReservationSpecifications.hasMemberId(null)
//                        .and(ReservationSpecifications.hasThemeId(공포.getId()))
//                        .and(ReservationSpecifications.dateAfterOrEqual(null))
//                        .and(ReservationSpecifications.dateBeforeOrEqual(null)));
//
//        List<Reservation> allReservations = reservationRepository.findAll(spec);
//
//        // then
//        assertSoftly(softly -> {
//            softly.assertThat(allReservations).hasSize(1);
//            Reservation reservation = allReservations.getFirst();
//            softly.assertThat(reservation.getId()).isEqualTo(예약_7일전.getId());
//        });
//    }
//
//    @Test
//    void 예약자를_조건으로_예약을_조회한다() {
//        // given
//        ReservationTime 열시 = reservationTimeDbFixture.예약시간_10시();
//        Theme 공포 = themeDbFixture.공포();
//        Theme 로맨스 = themeDbFixture.로맨스();
//        Member 한스 = memberDbFixture.한스_leehyeonsu4888_지메일_일반_멤버();
//        ReservationDate 예약날짜_7일전 = ReservationDateFixture.예약날짜_7일전;
//        ReservationDate 예약날짜_오늘 = ReservationDateFixture.예약날짜_오늘;
//        Reservation 예약_7일전 = reservationDbFixture.예약_생성(예약날짜_7일전, 열시, 공포, 한스);
//        Reservation 예약_오늘 = reservationDbFixture.예약_생성(예약날짜_오늘, 열시, 로맨스, 한스);
//
//        // when
//        Specification<Reservation> spec = Specification
//                .where(ReservationSpecifications.hasMemberId(한스.getId())
//                        .and(ReservationSpecifications.hasThemeId(null))
//                        .and(ReservationSpecifications.dateAfterOrEqual(null))
//                        .and(ReservationSpecifications.dateBeforeOrEqual(null)));
//
//        List<Reservation> allReservations = reservationRepository.findAll(spec);
//
//        // then
//        assertThat(allReservations).hasSize(2);
//    }
//}
