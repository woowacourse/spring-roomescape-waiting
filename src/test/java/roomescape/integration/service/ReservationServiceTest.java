package roomescape.integration.service;

import static org.assertj.core.api.Assertions.*;
import static roomescape.common.Constant.FIXED_CLOCK;
import static roomescape.integration.fixture.ReservationDateFixture.예약날짜_오늘;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.NoSuchElementException;
import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.transaction.annotation.Transactional;
import roomescape.common.ClockConfig;
import roomescape.domain.member.Member;
import roomescape.domain.member.MemberEmail;
import roomescape.domain.member.MemberEncodedPassword;
import roomescape.domain.member.MemberName;
import roomescape.domain.member.MemberRole;
import roomescape.domain.reservation.Reservation;
import roomescape.domain.reservation.ReservationDate;
import roomescape.domain.reservation.ReservationDateTime;
import roomescape.domain.theme.Theme;
import roomescape.domain.theme.ThemeDescription;
import roomescape.domain.theme.ThemeName;
import roomescape.domain.theme.ThemeThumbnail;
import roomescape.domain.time.ReservationTime;
import roomescape.integration.fixture.MemberDbFixture;
import roomescape.integration.fixture.ReservationDbFixture;
import roomescape.integration.fixture.ReservationTimeDbFixture;
import roomescape.integration.fixture.ThemeDbFixture;
import roomescape.repository.MemberRepository;
import roomescape.repository.ReservationRepository;
import roomescape.repository.ReservationTimeRepository;
import roomescape.repository.ThemeRepository;
import roomescape.service.ReservationService;
import roomescape.service.request.ReservationCreateRequest;
import roomescape.service.response.MyReservationResponse;
import roomescape.service.response.ReservationResponse;

@Transactional
@SpringBootTest
@Import(ClockConfig.class)
class ReservationServiceTest {

    @Autowired
    private ReservationRepository reservationRepository;

    @Autowired
    private ReservationTimeRepository reservationTimeRepository;

    @Autowired
    private ThemeRepository themeRepository;

    @Autowired
    private MemberRepository memberRepository;

    private final LocalDate today = LocalDate.now();

    @Autowired
    private ReservationService service;

    private final LocalTime time = LocalTime.of(10, 0);

    @Autowired
    private ReservationDbFixture reservationDbFixture;

    @Autowired
    private MemberDbFixture memberDbFixture;

    @Autowired
    private ReservationTimeDbFixture reservationTimeDbFixture;

    @Autowired
    private ThemeDbFixture themeDbFixture;

    @Test
    void 모든_예약을_조회한다() {
        // given
        ReservationTime reservationTime = reservationTimeRepository.save(new ReservationTime(
                null,
                time
        ));
        Theme theme = themeRepository.save(new Theme(
                        null,
                        new ThemeName("공포"),
                        new ThemeDescription("무섭다"),
                        new ThemeThumbnail("thumb.jpg")
                )
        );
        Member member = memberRepository.save(new Member(
                        null,
                        new MemberName("홍길동"),
                        new MemberEmail("leehyeonsu4888@gmail.com"),
                        new MemberEncodedPassword("dsadsa"),
                        MemberRole.MEMBER
                )
        );
        ReservationDateTime reservationDateTime = new ReservationDateTime(예약날짜_오늘, reservationTime, FIXED_CLOCK);
        reservationRepository.save(
                new Reservation(
                        null,
                        member,
                        reservationDateTime.getReservationDate(),
                        reservationDateTime.getReservationTime(),
                        theme
                )
        );

        // when
        List<ReservationResponse> all = service.findAllReservations();

        // then
        SoftAssertions.assertSoftly(softly -> {
            softly.assertThat(all).hasSize(1);
            ReservationResponse response = all.get(0);
            softly.assertThat(response.name()).isEqualTo("홍길동");
            softly.assertThat(response.date()).isEqualTo(예약날짜_오늘.date());
            softly.assertThat(response.time().startAt()).isEqualTo(time);
            softly.assertThat(response.theme().id()).isEqualTo(theme.getId());
            softly.assertThat(response.theme().name()).isEqualTo(theme.getName().name());
            softly.assertThat(response.theme().description()).isEqualTo(theme.getDescription().description());
            softly.assertThat(response.theme().thumbnail()).isEqualTo(theme.getThumbnail().thumbnail());
        });
    }

    @Test
    void 예약을_생성할_수_있다() {
        Member member = memberRepository.save(new Member(
                null,
                new MemberName("홍길동"),
                new MemberEmail("leehyeonsu4888@gmail.com"),
                new MemberEncodedPassword("encoded"),
                MemberRole.MEMBER
        ));
        ReservationTime reservationTime = reservationTimeRepository.save(new ReservationTime(null, time));
        Theme theme = themeRepository.save(new Theme(
                null,
                new ThemeName("공포"),
                new ThemeDescription("무섭다"),
                new ThemeThumbnail("thumb.jpg")
        ));

        ReservationCreateRequest request = new ReservationCreateRequest(today, reservationTime.getId(), theme.getId());
        ReservationResponse response = service.createReservation(request, member.getId());

        assertThat(response.name()).isEqualTo("홍길동");
    }

    @Test
    void 예약시간이_없으면_예외가_발생한다() {
        Member member = memberRepository.save(new Member(
                        null,
                        new MemberName("홍길동"),
                        new MemberEmail("leehyeonsu4888@gmail.com"),
                        new MemberEncodedPassword("encoded"),
                        MemberRole.MEMBER
                )
        );
        ReservationCreateRequest request = new ReservationCreateRequest(today, 999L, 1L);

        assertThatThrownBy(() -> service.createReservation(request, member.getId()))
                .isInstanceOf(NoSuchElementException.class);
    }

    @Test
    void 이미_예약된_시간이면_예외가_발생한다() {
        Member member = memberRepository.save(new Member(
                        null,
                        new MemberName("홍길동"),
                        new MemberEmail("leehyeonsu4888@gmail.com"),
                        new MemberEncodedPassword("encoded"),
                        MemberRole.MEMBER
                )
        );
        ReservationTime reservationTime = reservationTimeRepository.save(new ReservationTime(null, time));
        Theme theme = themeRepository.save(new Theme(
                        null,
                        new ThemeName("공포"),
                        new ThemeDescription("무섭다"),
                        new ThemeThumbnail("thumb.jpg")
                )
        );
        ReservationDateTime reservationDateTime = new ReservationDateTime(예약날짜_오늘, reservationTime, FIXED_CLOCK);
        reservationRepository.save(new Reservation(
                        null,
                        member,
                        reservationDateTime.getReservationDate(),
                        reservationDateTime.getReservationTime(),
                        theme
                )
        );
        ReservationCreateRequest request = new ReservationCreateRequest(
                예약날짜_오늘.date(),
                reservationTime.getId(),
                theme.getId()
        );

        assertThatThrownBy(() -> service.createReservation(request, member.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void 테마가_없으면_예외가_발생한다() {
        Member member = memberRepository.save(new Member(
                null,
                new MemberName("홍길동"),
                new MemberEmail("leehyeonsu4888@gmail.com"),
                new MemberEncodedPassword("encoded"),
                MemberRole.MEMBER
        ));
        ReservationTime reservationTime = reservationTimeRepository.save(new ReservationTime(
                null,
                time
        ));
        ReservationCreateRequest request = new ReservationCreateRequest(today, reservationTime.getId(), 999L);

        assertThatThrownBy(() -> service.createReservation(request, member.getId()))
                .isInstanceOf(NoSuchElementException.class);
    }

    @Test
    void 예약을_삭제할_수_있다() {
        Member member = memberRepository.save(new Member(
                null,
                new MemberName("홍길동"),
                new MemberEmail("leehyeonsu4888@gmail.com"),
                new MemberEncodedPassword("encoded"),
                MemberRole.MEMBER)
        );
        ReservationTime reservationTime = reservationTimeRepository.save(new ReservationTime(
                null,
                time
        ));
        Theme theme = themeRepository.save(new Theme(
                        null,
                        new ThemeName("공포"),
                        new ThemeDescription("무섭다"),
                        new ThemeThumbnail("thumb.jpg")
                )
        );
        ReservationDateTime reservationDateTime = new ReservationDateTime(new ReservationDate(today), reservationTime,
                FIXED_CLOCK);
        Reservation reservation = reservationRepository.save(new Reservation(
                        null,
                        member,
                        reservationDateTime.getReservationDate(),
                        reservationDateTime.getReservationTime(),
                        theme
                )
        );

        service.deleteReservationById(reservation.getId());

        assertThat(reservationRepository.findById(reservation.getId())).isEmpty();
    }

    @Test
    void 삭제할_예약이_없으면_예외() {
        assertThatThrownBy(() -> service.deleteReservationById(999L))
                .isInstanceOf(NoSuchElementException.class);
    }

    @Test
    void 내_예약_조회() {
        // given
        Member 한스 = memberDbFixture.한스_leehyeonsu4888_지메일_일반_멤버();
        ReservationTime 예약시간_10시 = reservationTimeDbFixture.예약시간_10시();
        Theme 공포 = themeDbFixture.공포();
        Reservation reservation = reservationDbFixture.예약_25_4_22(예약시간_10시, 공포, 한스);

        // when
        List<MyReservationResponse> all = service.findAllMyReservation(한스.getId());

        // then
        SoftAssertions.assertSoftly(softly -> {
            softly.assertThat(all).hasSize(1);
            MyReservationResponse response = all.get(0);
            softly.assertThat(response.reservationId()).isEqualTo(reservation.getId());
            softly.assertThat(response.theme()).isEqualTo(reservation.getTheme().getName().name());
            softly.assertThat(response.date()).isEqualTo(reservation.getDate());
            softly.assertThat(response.time()).isEqualTo(reservation.getStartAt());
            softly.assertThat(response.status()).isEqualTo("예약");
        });
    }
}
