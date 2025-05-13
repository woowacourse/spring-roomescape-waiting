package roomescape.unit.service;

import static org.assertj.core.api.Assertions.*;
import static roomescape.common.Constant.FIXED_CLOCK;
import static roomescape.integration.fixture.ReservationDateFixture.예약날짜_오늘;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.NoSuchElementException;
import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.Test;
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
import roomescape.repository.MemberRepository;
import roomescape.repository.ReservationRepository;
import roomescape.repository.ReservationTimeRepository;
import roomescape.repository.ThemeRepository;
import roomescape.service.ReservationService;
import roomescape.service.request.ReservationCreateRequest;
import roomescape.service.response.ReservationResponse;
import roomescape.unit.fake.FakeMemberRepository;
import roomescape.unit.fake.FakeReservationRepository;
import roomescape.unit.fake.FakeReservationTimeRepository;
import roomescape.unit.fake.FakeThemeRepository;

class ReservationServiceTest {

    private final ReservationRepository reservationRepository = new FakeReservationRepository();
    private final ReservationTimeRepository reservationTimeRepository = new FakeReservationTimeRepository();
    private final ThemeRepository themeRepository = new FakeThemeRepository();
    private final MemberRepository memberRepository = new FakeMemberRepository();

    private final LocalDate today = LocalDate.now();

    private ReservationService service = new ReservationService(
            reservationRepository,
            reservationTimeRepository,
            themeRepository,
            memberRepository,
            FIXED_CLOCK
    );

    private final LocalTime time = LocalTime.of(10, 0);

    @Test
    void 모든_예약을_조회한다() {
        // given
        ReservationTime reservationTime = reservationTimeRepository.save(time);
        Theme theme = themeRepository.save(
                new ThemeName("공포"),
                new ThemeDescription("무섭다"),
                new ThemeThumbnail("thumb.jpg")
        );
        Member member = memberRepository.save(
                new MemberEmail("leehyeonsu4888@gmail.com"),
                new MemberName("홍길동"),
                new MemberEncodedPassword("dsadsa"),
                MemberRole.MEMBER
        );
        reservationRepository.save(
                member,
                new ReservationDateTime(예약날짜_오늘, reservationTime, FIXED_CLOCK),
                theme
        );

        // when
        List<ReservationResponse> all = service.findAllReservations();

        // then
        SoftAssertions.assertSoftly(softly -> {
            softly.assertThat(all).hasSize(1);
            ReservationResponse response = all.get(0);
            softly.assertThat(response.name()).isEqualTo("홍길동");
            softly.assertThat(response.date()).isEqualTo(예약날짜_오늘.getDate());
            softly.assertThat(response.time().startAt()).isEqualTo(time);
            softly.assertThat(response.theme().id()).isEqualTo(theme.getId());
            softly.assertThat(response.theme().name()).isEqualTo(theme.getName().name());
            softly.assertThat(response.theme().description()).isEqualTo(theme.getDescription().description());
            softly.assertThat(response.theme().thumbnail()).isEqualTo(theme.getThumbnail().thumbnail());
        });
    }

    @Test
    void 예약을_생성할_수_있다() {
        Member member = memberRepository.save(
                new MemberEmail("leehyeonsu4888@gmail.com"),
                new MemberName("홍길동"),
                new MemberEncodedPassword("encoded"),
                MemberRole.MEMBER
        );
        ReservationTime reservationTime = reservationTimeRepository.save(time);
        Theme theme = themeRepository.save(
                new ThemeName("공포"),
                new ThemeDescription("무섭다"),
                new ThemeThumbnail("thumb.jpg")
        );

        ReservationCreateRequest request = new ReservationCreateRequest(today, reservationTime.getId(), theme.getId());
        ReservationResponse response = service.createReservation(request, member.getId());

        assertThat(response.name()).isEqualTo("홍길동");
    }

    @Test
    void 예약시간이_없으면_예외가_발생한다() {
        Member member = memberRepository.save(
                new MemberEmail("leehyeonsu4888@gmail.com"),
                new MemberName("홍길동"),
                new MemberEncodedPassword("encoded"),
                MemberRole.MEMBER
        );
        ReservationCreateRequest request = new ReservationCreateRequest(today, 999L, 1L);

        assertThatThrownBy(() -> service.createReservation(request, member.getId()))
                .isInstanceOf(NoSuchElementException.class);
    }

    @Test
    void 이미_예약된_시간이면_예외가_발생한다() {
        Member member = memberRepository.save(
                new MemberEmail("leehyeonsu4888@gmail.com"),
                new MemberName("홍길동"),
                new MemberEncodedPassword("encoded"),
                MemberRole.MEMBER
        );
        ReservationTime reservationTime = reservationTimeRepository.save(time);
        Theme theme = themeRepository.save(
                new ThemeName("공포"),
                new ThemeDescription("무섭다"),
                new ThemeThumbnail("thumb.jpg")
        );
        reservationRepository.save(
                member,
                new ReservationDateTime(예약날짜_오늘, reservationTime, FIXED_CLOCK),
                theme
        );
        ReservationCreateRequest request = new ReservationCreateRequest(
                예약날짜_오늘.getDate(),
                reservationTime.getId(),
                theme.getId()
        );

        assertThatThrownBy(() -> service.createReservation(request, member.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void 테마가_없으면_예외가_발생한다() {
        Member member = memberRepository.save(
                new MemberEmail("leehyeonsu4888@gmail.com"),
                new MemberName("홍길동"),
                new MemberEncodedPassword("encoded"),
                MemberRole.MEMBER
        );
        ReservationTime reservationTime = reservationTimeRepository.save(time);
        ReservationCreateRequest request = new ReservationCreateRequest(today, reservationTime.getId(), 999L);

        assertThatThrownBy(() -> service.createReservation(request, member.getId()))
                .isInstanceOf(NoSuchElementException.class);
    }

    @Test
    void 예약을_삭제할_수_있다() {
        Member member = memberRepository.save(
                new MemberEmail("leehyeonsu4888@gmail.com"),
                new MemberName("홍길동"),
                new MemberEncodedPassword("encoded"),
                MemberRole.MEMBER
        );
        ReservationTime reservationTime = reservationTimeRepository.save(time);
        Theme theme = themeRepository.save(
                new ThemeName("공포"),
                new ThemeDescription("무섭다"),
                new ThemeThumbnail("thumb.jpg")
        );
        Reservation reservation = reservationRepository.save(
                member,
                new ReservationDateTime(new ReservationDate(today), reservationTime, FIXED_CLOCK),
                theme
        );

        service.deleteReservationById(reservation.getId());

        assertThat(reservationRepository.findById(reservation.getId())).isEmpty();
    }

    @Test
    void 삭제할_예약이_없으면_예외() {
        assertThatThrownBy(() -> service.deleteReservationById(999L))
                .isInstanceOf(NoSuchElementException.class);
    }
}
