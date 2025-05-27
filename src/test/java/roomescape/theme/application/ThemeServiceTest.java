package roomescape.theme.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import roomescape.fixture.MemberFixture;
import roomescape.member.domain.Member;
import roomescape.member.domain.repository.MemberRepository;
import roomescape.member.infrastructure.MemberRepositoryAdapter;
import roomescape.reservation.domain.Reservation;
import roomescape.reservation.domain.ReservationDate;
import roomescape.reservation.domain.ReservationSpec;
import roomescape.reservation.domain.repository.ReservationRepository;
import roomescape.reservation.infrastructure.ReservationRepositoryAdapter;
import roomescape.reservationTime.domain.ReservationTime;
import roomescape.reservationTime.domain.respository.ReservationTimeRepository;
import roomescape.reservationTime.infrastructure.ReservationTimeRepositoryAdapter;
import roomescape.theme.application.dto.ThemeRequest;
import roomescape.theme.application.dto.ThemeResponse;
import roomescape.theme.domain.Theme;
import roomescape.theme.domain.repository.ThemeRepository;
import roomescape.theme.exception.UsingThemeException;
import roomescape.theme.infrastructure.ThemeRepositoryAdapter;

@ActiveProfiles("test")
@DataJpaTest
@Import({
        ThemeService.class,
        ThemeRepositoryAdapter.class,
        MemberRepositoryAdapter.class,
        ReservationRepositoryAdapter.class,
        ReservationTimeRepositoryAdapter.class
})
class ThemeServiceTest {

    @Autowired
    private ThemeService themeService;

    @Autowired
    private ThemeRepository themeRepository;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private ReservationRepository reservationRepository;

    @Autowired
    private ReservationTimeRepository timeRepository;

    @DisplayName("테마 생성 - 성공")
    @Test
    void create_success() {
        // given
        // 테마 이름 설정
        String name = "무서운방";
        // 테마 설명 설정
        String description = "덜덜";
        // 테마 썸네일 이미지 설정
        String thumbnail = "무서운 사진";
        // 테마 생성 요청 객체 생성
        ThemeRequest request = new ThemeRequest(name, description, thumbnail);

        // when
        ThemeResponse response = themeService.create(request);

        // then
        assertThat(response).isNotNull();
        assertThat(response.id()).isNotNull();
        assertThat(response.name()).isEqualTo(name);
        assertThat(response.description()).isEqualTo(description);
        assertThat(response.thumbnail()).isEqualTo(thumbnail);
    }

    @DisplayName("모든 테마 조회 - 성공")
    @Test
    void findAll_success() {
        // given
        // 첫 번째 테마 생성 및 저장
        Theme theme1 = new Theme("테마1", "테마 설명1", "thumbnail1.jpg");
        themeRepository.save(theme1);

        // 두 번째 테마 생성 및 저장
        Theme theme2 = new Theme("테마2", "테마 설명2", "thumbnail2.jpg");
        themeRepository.save(theme2);

        // when
        List<ThemeResponse> responses = themeService.findAll();

        // then
        assertThat(responses).hasSize(2);
        assertThat(responses.get(0).id()).isEqualTo(theme1.getId());
        assertThat(responses.get(0).name()).isEqualTo(theme1.getName());
        assertThat(responses.get(1).id()).isEqualTo(theme2.getId());
        assertThat(responses.get(1).name()).isEqualTo(theme2.getName());
    }

    @DisplayName("기간 내 인기 테마 조회 - 성공")
    @Test
    void findRankedByPeriod_success() {
        // given
        // 회원 생성 및 저장
        Member member = MemberFixture.createMember("에드", "test@test.com", "1234");
        memberRepository.save(member);

        // 현재 시간으로 예약 시간 생성 및 저장
        ReservationTime time = new ReservationTime(LocalTime.now());
        timeRepository.save(time);

        // 첫 번째 테마 생성 및 저장 (더 많은 예약이 있는 테마)
        Theme theme1 = new Theme("테마1", "테마 설명1", "thumbnail1.jpg");
        themeRepository.save(theme1);

        // 두 번째 테마 생성 및 저장 (더 적은 예약이 있는 테마)
        Theme theme2 = new Theme("테마2", "테마 설명2", "thumbnail2.jpg");
        themeRepository.save(theme2);

        // 오늘 날짜 설정
        LocalDate today = LocalDate.now();

        // theme1에 예약 2개 생성
        // 5일 전 날짜로 첫 번째 예약 스펙 생성 (테마1)
        ReservationSpec spec1 = new ReservationSpec(new ReservationDate(today.minusDays(5)), time, theme1);
        // 회원으로 첫 번째 예약 생성 및 저장
        Reservation reservation1 = new Reservation(member, spec1);
        reservationRepository.save(reservation1);

        // 3일 전 날짜로 두 번째 예약 스펙 생성 (테마1)
        ReservationSpec spec2 = new ReservationSpec(new ReservationDate(today.minusDays(3)), time, theme1);
        // 회원으로 두 번째 예약 생성 및 저장
        Reservation reservation2 = new Reservation(member, spec2);
        reservationRepository.save(reservation2);

        // theme2에 예약 1개 생성
        // 4일 전 날짜로 세 번째 예약 스펙 생성 (테마2)
        ReservationSpec spec3 = new ReservationSpec(new ReservationDate(today.minusDays(4)), time, theme2);
        // 회원으로 세 번째 예약 생성 및 저장
        Reservation reservation3 = new Reservation(member, spec3);
        reservationRepository.save(reservation3);

        // when
        List<ThemeResponse> responses = themeService.findRankedByPeriod();

        // then
        assertThat(responses).hasSize(2);
        assertThat(responses.get(0).id()).isEqualTo(theme1.getId());
        assertThat(responses.get(1).id()).isEqualTo(theme2.getId());
    }

    @DisplayName("테마 삭제 - 사용 중인 테마인 경우 예외 발생")
    @Test
    void deleteById_usingTheme() {
        // given
        // 회원 생성 및 저장
        Member member = MemberFixture.createMember("에드", "test@test.com", "1234");
        memberRepository.save(member);

        // 현재 시간으로 예약 시간 생성 및 저장
        ReservationTime time = new ReservationTime(LocalTime.now());
        timeRepository.save(time);

        // 테마 생성 및 저장
        Theme theme = new Theme("테마", "테마 설명", "thumbnail.jpg");
        themeRepository.save(theme);
        Long themeId = theme.getId();

        // 내일 날짜로 예약 날짜 설정
        LocalDate date = LocalDate.now().plusDays(1);
        // 예약 스펙 생성 (날짜, 시간, 테마)
        ReservationSpec spec = new ReservationSpec(new ReservationDate(date), time, theme);
        // 회원으로 예약 생성 및 저장 (테마가 사용 중인 상태)
        Reservation reservation = new Reservation(member, spec);
        reservationRepository.save(reservation);

        // when & then
        assertThatThrownBy(() -> themeService.deleteById(themeId))
                .isInstanceOf(UsingThemeException.class);
    }

    @DisplayName("테마 삭제 - 성공")
    @Test
    void deleteById_success() {
        // given
        Theme theme = new Theme("테마", "테마 설명", "thumbnail.jpg");
        themeRepository.save(theme);
        Long themeId = theme.getId();

        // when
        themeService.deleteById(themeId);

        // then
        assertThat(themeRepository.findById(themeId)).isEmpty();
    }
}
