package roomescape.reservation.service;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.util.List;
import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import roomescape.common.CleanUp;
import roomescape.fixture.db.MemberDbFixture;
import roomescape.fixture.db.ReservationDateTimeDbFixture;
import roomescape.fixture.db.ReservationTimeDbFixture;
import roomescape.fixture.db.ThemeDbFixture;
import roomescape.fixture.entity.ReservationDateFixture;
import roomescape.member.domain.Member;
import roomescape.reservation.controller.response.MyReservationResponse;
import roomescape.reservation.controller.response.ReservationResponse;
import roomescape.reservation.domain.Reservation;
import roomescape.reservation.domain.ReservationDate;
import roomescape.reservation.domain.ReservationDateTime;
import roomescape.reservation.repository.ReservationRepository;
import roomescape.theme.domain.Theme;
import roomescape.time.controller.response.ReservationTimeResponse;
import roomescape.time.domain.ReservationTime;

@SpringBootTest(webEnvironment = WebEnvironment.NONE)
public class ReservationQueryServiceTest {

    @Autowired
    private ReservationQueryService reservationQueryService;
    @Autowired
    private ReservationTimeDbFixture reservationTimeDbFixture;
    @Autowired
    private ThemeDbFixture themeDbFixture;
    @Autowired
    private MemberDbFixture memberDbFixture;
    @Autowired
    private ReservationRepository reservationRepository;
    @Autowired
    private ReservationDateTimeDbFixture reservationDateTimeDbFixture;

    @Autowired
    private CleanUp cleanUp;

    @BeforeEach
    void setUp() {
        cleanUp.all();
    }

    @Test
    void 예약을_모두_조회한다() {
        Member reserver = memberDbFixture.유저1_생성();
        ReservationDateTime reservationDateTime = reservationDateTimeDbFixture.내일_열시();
        Theme theme = themeDbFixture.공포();

        Reservation reservation = reservationRepository.save(Reservation.reserve(reserver, reservationDateTime, theme));

        List<ReservationResponse> responses = reservationQueryService.getFilteredReservations(null, null, null, null);
        ReservationResponse response = responses.get(0);

        SoftAssertions.assertSoftly(softly -> {
            softly.assertThat(response.id()).isNotNull();
            softly.assertThat(response.member().name()).isEqualTo(reservation.getReserverName());
            softly.assertThat(response.date()).isEqualTo(reservation.getDate());
            softly.assertThat(response.time())
                    .isEqualTo(ReservationTimeResponse.from(reservation.getReservationTime()));
        });
    }

    @Test
    void 예약_목록을_필터링해서_조회한다() {
        final LocalDate today = LocalDate.now();
        final LocalDate tomorrow = today.plusDays(1);

        Theme theme = themeDbFixture.공포();
        Member member1 = memberDbFixture.유저1_생성();
        Member member2 = memberDbFixture.유저2_생성();
        ReservationDate reservationDate = ReservationDateFixture.예약날짜_내일;
        ReservationTime 열시 = reservationTimeDbFixture.열시();
        ReservationTime 열한시 = reservationTimeDbFixture.열한시();

        Reservation reservation1 = Reservation.reserve(
                member1, ReservationDateTime.create(reservationDate, 열시), theme
        );
        Reservation reservation2 = Reservation.reserve(
                member1, ReservationDateTime.create(reservationDate, 열한시), theme
        );
        Reservation reservation3 = Reservation.reserve(
                member2, ReservationDateTime.create(reservationDate, 열시), theme
        );

        reservationRepository.saveAll(List.of(reservation1, reservation2, reservation3));
        // when & then
        // 공포 필터링

        SoftAssertions.assertSoftly(softly -> {
            softly.assertThat(
                            reservationQueryService.getFilteredReservations(theme.getId(), null, null, null))
                    .hasSize(3);
            // 사용자1 필터링
            softly.assertThat(reservationQueryService.getFilteredReservations(null, member1.getId(), null, null))
                    .hasSize(2);
            // 오늘 필터링
            softly.assertThat(reservationQueryService.getFilteredReservations(null, null, today, today))
                    .isEmpty();
            // 공포 테마 & 내일 필터링
            softly.assertThat(
                            reservationQueryService.getFilteredReservations(theme.getId(), null, tomorrow, tomorrow))
                    .hasSize(3);
            // 모든 필터 조합
            softly.assertThat(
                            reservationQueryService.getFilteredReservations(theme.getId(), member2.getId(), tomorrow, tomorrow))
                    .hasSize(1);
            // 일치하는 결과가 없는 필터 조합
            softly.assertThat(
                            reservationQueryService.getFilteredReservations(theme.getId(), member2.getId(), today, today))
                    .isEmpty();

            // 모든 결과 조회
            softly.assertThat(reservationQueryService.getFilteredReservations(null, null, null, null))
                    .hasSize(3);
        });
    }


    @Test
    void 내_예약_목록을_조회한다() {
        Member member1 = memberDbFixture.유저1_생성();
        Member member2 = memberDbFixture.유저2_생성();

        ReservationDateTime reservationDateTime = reservationDateTimeDbFixture.내일_열시();
        Theme theme = themeDbFixture.공포();

        Reservation reservation1 = reservationRepository.save(Reservation.reserve(member1, reservationDateTime, theme));
        reservationRepository.save(Reservation.reserve(member2, reservationDateTime, theme));

        List<MyReservationResponse> myReservations = reservationQueryService.getReservations(member1.getId());

        SoftAssertions.assertSoftly(softly -> {
            softly.assertThat(myReservations).hasSize(1);
            softly.assertThat(myReservations.get(0).theme()).isEqualTo(reservation1.getTheme().getName());
            softly.assertThat(myReservations.get(0).date()).isEqualTo(reservation1.getDate());
            softly.assertThat(myReservations.get(0).time()).isEqualTo(reservation1.getStartAt());
        });
    }

    @Test
    void 존재하지_않는_회원의_예약목록을_조회하면_빈_리스트를_반환한다() {
        List<MyReservationResponse> myReservations = reservationQueryService.getReservations(999L);

        assertThat(myReservations).isEmpty();
    }

    @Test
    void 날짜가_범위를_벗어나는_필터_조건으로_조회하면_빈_리스트를_반환한다() {
        // given
        Member reserver = memberDbFixture.유저1_생성();
        ReservationDateTime reservationDateTime = reservationDateTimeDbFixture.내일_열시();
        Theme theme = themeDbFixture.공포();

        reservationRepository.save(Reservation.reserve(reserver, reservationDateTime, theme));

        LocalDate pastDate = LocalDate.now().minusDays(10);
        LocalDate pastDateEnd = LocalDate.now().minusDays(5);

        List<ReservationResponse> responses = reservationQueryService.getFilteredReservations(
                null, null, pastDate, pastDateEnd);

        assertThat(responses).isEmpty();
    }

    @Test
    void 비어있는_날짜_필터링으로_조회하면_모든_예약을_반환한다() {
        // given
        Member member1 = memberDbFixture.유저1_생성();
        Member member2 = memberDbFixture.유저2_생성();

        ReservationDateTime reservationDateTime = reservationDateTimeDbFixture.내일_열시();
        Theme theme = themeDbFixture.공포();

        Reservation reservation1 = Reservation.reserve(member1, reservationDateTime, theme);
        Reservation reservation2 = Reservation.reserve(member2, reservationDateTime, theme);

        reservationRepository.saveAll(List.of(reservation1, reservation2));

        // when
        List<ReservationResponse> responses = reservationQueryService.getFilteredReservations(
                null, null, null, null);

        // then
        assertThat(responses).hasSize(2);
    }

    @Test
    void 다수의_회원_예약을_테마별로_필터링_조회할_수_있다() {
        // given
        Member member = memberDbFixture.유저1_생성();
        Theme horror = themeDbFixture.공포();
        Theme adventure = themeDbFixture.커스텀_테마("모험");

        ReservationDateTime tomorrow10 = reservationDateTimeDbFixture.내일_열시();

        // 두 개의 다른 테마로 예약
        Reservation horrorReservation = Reservation.reserve(member, tomorrow10, horror);
        Reservation adventureReservation = Reservation.reserve(member, tomorrow10, adventure);

        reservationRepository.saveAll(List.of(horrorReservation, adventureReservation));

        // when - 공포 테마만 필터링
        List<ReservationResponse> horrorOnly = reservationQueryService.getFilteredReservations(
                horror.getId(), null, null, null);

        // then

        SoftAssertions.assertSoftly(softly -> {
            softly.assertThat(horrorOnly).hasSize(1);
            softly.assertThat(horrorOnly.get(0).theme().name()).isEqualTo(horror.getName());
        });
    }

    @Test
    void 예약_대기와_함께_조회할_수_있다(){
        // TODO 작성할 것
    }
}
