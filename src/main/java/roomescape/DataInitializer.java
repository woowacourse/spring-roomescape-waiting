package roomescape;

import java.time.LocalDate;
import java.time.LocalTime;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import roomescape.domain.member.Member;
import roomescape.domain.member.MemberRepository;
import roomescape.domain.member.Role;
import roomescape.domain.reservation.Reservation;
import roomescape.domain.reservation.ReservationRepository;
import roomescape.domain.reservation.Waiting;
import roomescape.domain.reservation.WaitingRepository;
import roomescape.domain.reservation.detail.ReservationDetail;
import roomescape.domain.reservation.detail.ReservationTime;
import roomescape.domain.reservation.detail.ReservationTimeRepository;
import roomescape.domain.reservation.detail.Theme;
import roomescape.domain.reservation.detail.ThemeRepository;

@Component
@Transactional
@Profile("default")
public class DataInitializer implements ApplicationRunner {

    private final ThemeRepository themeRepository;
    private final ReservationTimeRepository reservationTimeRepository;
    private final MemberRepository memberRepository;
    private final ReservationRepository reservationRepository;
    private final WaitingRepository waitingRepository;

    public DataInitializer(
            ThemeRepository themeRepository,
            ReservationTimeRepository reservationTimeRepository, MemberRepository memberRepository,
            ReservationRepository reservationRepository,
            WaitingRepository waitingRepository
    ) {
        this.themeRepository = themeRepository;
        this.reservationTimeRepository = reservationTimeRepository;
        this.memberRepository = memberRepository;
        this.reservationRepository = reservationRepository;
        this.waitingRepository = waitingRepository;
    }

    @Override
    public void run(ApplicationArguments args) {
        initData();
    }

    private void initData() {
        // Member
        Member admin = memberRepository.save(new Member(
                "admin@gmail.com",
                "$2a$10$MbGFqyn/u4wfggRK7HAqDeC1y9s1mESgmXV3b7e7GZT5u1JkIT.gm",
                "어드민",
                Role.ADMIN)); // password: abc123
        Member user = memberRepository.save(new Member(
                "user@gmail.com",
                "$2a$10$MbGFqyn/u4wfggRK7HAqDeC1y9s1mESgmXV3b7e7GZT5u1JkIT.gm",
                "유저",
                Role.USER)); // password: abc123
        Member member1 = memberRepository.save(new Member("example1@gmail.com", "1234", "구름1", Role.USER));
        Member member2 = memberRepository.save(new Member("example2@gmail.com", "1234", "구름2", Role.USER));
        Member member3 = memberRepository.save(new Member("example3@gmail.com", "1234", "구름3", Role.USER));
        Member member4 = memberRepository.save(new Member("example4@gmail.com", "1234", "구름4", Role.USER));

        // Theme
        Theme theme1 = themeRepository.save(
                new Theme("고풍 한옥 마을", "한국의 전통적인 아름다움이 당신을 맞이합니다.", "https://via.placeholder.com/150/92c952"));
        Theme theme2 = themeRepository.save(
                new Theme("우주 탐험", "끝없는 우주에 숨겨진 비밀을 파헤치세요.", "https://via.placeholder.com/150/771796"));
        Theme theme3 = themeRepository.save(
                new Theme("시간여행", "과거와 미래를 오가며 역사의 비밀을 밝혀보세요.", "https://via.placeholder.com/150/24f355"));
        Theme theme4 = themeRepository.save(
                new Theme("마법의 숲", "요정과 마법사들이 사는 신비로운 숲 속으로!", "https://via.placeholder.com/150/30f9e7"));
        Theme theme5 = themeRepository.save(
                new Theme("타임캡슐", "오래된 타임캡슐을 찾아내어 그 안의 비밀을 풀어보세요.", "https://via.placeholder.com/150/56a8c2"));
        Theme theme6 = themeRepository.save(
                new Theme("로맨틱 유럽 여행", "로맨틱한 분위기 속에서 유럽을 여행하세요.", "https://via.placeholder.com/150/7472e7"));
        Theme theme7 = themeRepository.save(
                new Theme("신화 속의 세계", "신화와 전설 속으로 당신을 초대합니다.", "https://via.placeholder.com/150/24f355"));
        Theme theme8 = themeRepository.save(
                new Theme("바다 속 신비", "깊은 바다에서의 모험을 경험하세요.", "https://via.placeholder.com/150/56a8c2"));

        // ReservationTime
        ReservationTime time1 = reservationTimeRepository.save(new ReservationTime(LocalTime.of(9, 0)));
        ReservationTime time2 = reservationTimeRepository.save(new ReservationTime(LocalTime.of(12, 0)));
        ReservationTime time3 = reservationTimeRepository.save(new ReservationTime(LocalTime.of(17, 0)));
        ReservationTime time4 = reservationTimeRepository.save(new ReservationTime(LocalTime.of(21, 0)));

        // Reservation
        Reservation reservation1 = reservationRepository.save(
                new Reservation(new ReservationDetail(LocalDate.now().minusDays(7), time1, theme5), admin));
        Reservation reservation2 = reservationRepository.save(
                new Reservation(new ReservationDetail(LocalDate.now().minusDays(6), time3, theme4), user));
        Reservation reservation3 = reservationRepository.save(
                new Reservation(new ReservationDetail(LocalDate.now().minusDays(5), time3, theme3), user));
        Reservation reservation4 = reservationRepository.save(
                new Reservation(new ReservationDetail(LocalDate.now().minusDays(4), time1, theme2), user));
        Reservation reservation5 = reservationRepository.save(
                new Reservation(new ReservationDetail(LocalDate.now().minusDays(3), time4, theme1), admin));
        Reservation reservation6 = reservationRepository.save(
                new Reservation(new ReservationDetail(LocalDate.now().minusDays(2), time1, theme1), member1));
        Reservation reservation7 = reservationRepository.save(
                new Reservation(new ReservationDetail(LocalDate.now().minusDays(1), time3, theme2), user));
        Reservation reservation8 = reservationRepository.save(
                new Reservation(new ReservationDetail(LocalDate.now().plusDays(2), time1, theme1), user));

        // Waiting
        Waiting waiting1 = waitingRepository.save(
                new Waiting(new ReservationDetail(LocalDate.now().minusDays(7), time1, theme5), user));
        Waiting waiting2 = waitingRepository.save(
                new Waiting(new ReservationDetail(LocalDate.now().minusDays(7), time1, theme5), member1));
        Waiting waiting3 = waitingRepository.save(
                new Waiting(new ReservationDetail(LocalDate.now().minusDays(6), time3, theme4), admin));
        Waiting waiting4 = waitingRepository.save(
                new Waiting(new ReservationDetail(LocalDate.now().minusDays(6), time3, theme4), member1));
        Waiting waiting5 = waitingRepository.save(
                new Waiting(new ReservationDetail(LocalDate.now().minusDays(6), time3, theme4), member2));
        Waiting waiting6 = waitingRepository.save(
                new Waiting(new ReservationDetail(LocalDate.now().minusDays(5), time3, theme3), member2));
        Waiting waiting7 = waitingRepository.save(
                new Waiting(new ReservationDetail(LocalDate.now().minusDays(4), time1, theme2), admin));
        Waiting waiting8 = waitingRepository.save(
                new Waiting(new ReservationDetail(LocalDate.now().minusDays(3), time4, theme1), member2));
        Waiting waiting9 = waitingRepository.save(
                new Waiting(new ReservationDetail(LocalDate.now().minusDays(1), time3, theme2), admin));
        Waiting waiting10 = waitingRepository.save(
                new Waiting(new ReservationDetail(LocalDate.now().plusDays(2), time1, theme1), member1));
        Waiting waiting11 = waitingRepository.save(
                new Waiting(new ReservationDetail(LocalDate.now().plusDays(2), time1, theme1), admin));
    }
}
