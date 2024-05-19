package roomescape;

import jakarta.transaction.Transactional;
import java.time.LocalDate;
import java.time.LocalTime;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import roomescape.member.domain.Member;
import roomescape.member.domain.Role;
import roomescape.member.domain.repository.MemberRepository;
import roomescape.reservation.domain.MemberReservation;
import roomescape.reservation.domain.Reservation;
import roomescape.reservation.domain.ReservationStatus;
import roomescape.reservation.domain.ReservationTime;
import roomescape.reservation.domain.Theme;
import roomescape.reservation.domain.repository.MemberReservationRepository;
import roomescape.reservation.domain.repository.ReservationRepository;
import roomescape.reservation.domain.repository.ReservationTimeRepository;
import roomescape.reservation.domain.repository.ThemeRepository;

@Transactional
@Component
public class DataLoader implements ApplicationRunner {

    private final ThemeRepository themeRepository;
    private final ReservationTimeRepository timeRepository;
    private final ReservationRepository reservationRepository;
    private final MemberRepository memberRepository;
    private final MemberReservationRepository memberReservationRepository;

    @Value("${dataloader.enable}")
    private boolean enableDataloader;

    public DataLoader(ThemeRepository themeRepository,
                      ReservationTimeRepository timeRepository,
                      ReservationRepository reservationRepository,
                      MemberRepository memberRepository,
                      MemberReservationRepository memberReservationRepository) {
        this.themeRepository = themeRepository;
        this.timeRepository = timeRepository;
        this.reservationRepository = reservationRepository;
        this.memberRepository = memberRepository;
        this.memberReservationRepository = memberReservationRepository;
    }

    @Override
    public void run(ApplicationArguments args) {
        if (enableDataloader) {
            runDataLoader();
        }
    }

    private void runDataLoader() {
        Theme theme1 = themeRepository.save(new Theme("파라오의 비밀(이집트덕후모험/중급)", """
                새로운 피라미드유적을 탐사하던 우리 일행은 마침내 파라오의 무덤을 발견하였고 잠입에 성공하여 그의 비밀을 파헤치던 중 실수로 부비트랩을 건드려 파라오의 방이 무너지기 시작하고 60분뒤면 파라오와 함께 생매장 당하게 될 위기에 처해졌다! 
                상형문자를 해독하여 분노한 파라오에게 제사를 지내고 아누비스의 저주를 풀어서 안전하게 피라미드 밖으로 탈출하자!!
                """,
                "https://naverbooking-phinf.pstatic.net/20201109_121/1604908062178KzsNV_JPEG/%C6%C4%B6%F3%BF%C0%C0%C7%BA%F1%B9%D0.jpeg")
        );

        Theme theme2 = themeRepository.save(
                new Theme("좀비어택(재미있는공포/초중급)", """
                        2024년, 정체불명의 좀비 바이러스가 전 세계적으로 확산되면서 소수의 비감염자들만이 작은 실험실에서 가까스로 생명을 이어가고 있다.
                        좀비 백신을 개발하던 제이박사님 마저 바이러스에 감염되면서, 살아 있는 이들의 유일한 희망은 그의 조수였던 당신뿐이다. 좀비들이 실험실 안으로 침투하기 직전인 긴박한 상황!
                        백신을 완성하고 좀비들을 퇴치하여 탈출! 빼앗긴 세계를 되찾아라!!
                        """,
                        "https://naverbooking-phinf.pstatic.net/20201109_262/1604908247778Wgjrb_JPEG/%C1%BB%BA%F1%BE%EE%C5%C3.jpeg")
        );

        Theme theme3 = themeRepository.save(
                new Theme("추적자(스릴러/초급)", """
                        늦은 밤, 어두운 골목길을 지나던 당신은 변태적 성향을 가진 싸이코패스 살인마에게 붙잡히게 된다.
                        눈을 떠 보니, 그의 방 안에 갇혀 있는 자신을 발견하게 되고, 살인마가 잠시 나간 사이 60분 안에 방에서 탈출하지 못하면 무시무시한 흉기를 소지한 변태적인 살인마가 다시 돌아와서 당신을...
                        """,
                        "https://naverbooking-phinf.pstatic.net/20201109_271/1604906887897VLkai_JPEG/%C3%DF%C0%FB%C0%DA.jpeg")
        );

        Theme theme4 = themeRepository.save(
                new Theme("비밀갤러리생일파티(안무서워/중급)", """
                        1920년대 프랑스 파리에 어느 평범한 가정, 그러나 불치병에 걸린 아이가 그만 생일을 몇일 앞두고 하늘나라로... 
                        시간이 흘러 지금은 갤러리로 쓰이고 있는 집에 떠도는 소문으로 몰래 들어가서 생일파티를 하면 100여년전 아이가 자기의 생일파티인줄 알고 기뻐하며 생일인 친구의 소원을 들어준다고하여 기억에 남는 생일파티를 하기 위해 미스터리한 비밀갤러리에 몰래 들어갔다! 
                        경비원에게 들키지 않게 오래전 생일파티만을 기다리던 아이의 마지막 소원을 들어주고 우리만의 비밀 생일파티를 즐겁게 마치고 탈출하자..
                        """,
                        "https://naverbooking-phinf.pstatic.net/20201104_210/160445696075352J6M_JPEG/%BA%F1%B9%D0%B0%B6%B7%AF%B8%AE.jpeg")
        );

        ReservationTime time1 = timeRepository.save(new ReservationTime(LocalTime.of(11, 50)));
        ReservationTime time2 = timeRepository.save(new ReservationTime(LocalTime.of(13, 10)));
        ReservationTime time3 = timeRepository.save(new ReservationTime(LocalTime.of(14, 30)));
        ReservationTime time4 = timeRepository.save(new ReservationTime(LocalTime.of(15, 50)));
        ReservationTime time5 = timeRepository.save(new ReservationTime(LocalTime.of(17, 10)));
        ReservationTime time6 = timeRepository.save(new ReservationTime(LocalTime.of(18, 30)));
        ReservationTime time7 = timeRepository.save(new ReservationTime(LocalTime.of(19, 50)));
        ReservationTime time8 = timeRepository.save(new ReservationTime(LocalTime.of(21, 10)));
        ReservationTime time9 = timeRepository.save(new ReservationTime(LocalTime.of(22, 30)));

        Reservation reservation1 = reservationRepository.save(
                new Reservation(LocalDate.now().minusDays(2), time1, theme1));
        Reservation reservation2 = reservationRepository.save(
                new Reservation(LocalDate.now().plusDays(16), time1, theme2));
        Reservation reservation3 = reservationRepository.save(
                new Reservation(LocalDate.now().plusYears(1), time2, theme1));
        Reservation reservation4 = reservationRepository.save(
                new Reservation(LocalDate.now().plusDays(1), time2, theme2));
        Reservation reservation5 = reservationRepository.save(
                new Reservation(LocalDate.now().minusDays(4), time3, theme3));
        Reservation reservation6 = reservationRepository.save(
                new Reservation(LocalDate.now().plusDays(4), time4, theme4));
        Reservation reservation7 = reservationRepository.save(
                new Reservation(LocalDate.now().plusMonths(1), time6, theme4));
        Reservation reservation8 = reservationRepository.save(
                new Reservation(LocalDate.now().plusDays(4), time8, theme4));

        Member member1 = memberRepository.save(
                new Member("초코칩", "dev.chocochip@gmail.com",
                        "$2a$10$hNWX2lluVCgwaorpX8TnZO2XadZKdzI6qCNGoSm/ptdBxvFYAGFw.", Role.USER));
        Member member2 = memberRepository.save(
                new Member("이든", "dev.eden@gmail.com",
                        "$2a$10$opT2WtzYtjCgcWrHAOxe/u7DcNQXPBgoEVjoM8ld8nc1DIaMOfmvm",
                        Role.USER));
        Member member3 = memberRepository.save(
                new Member("클로버", "dev.clover@gmail.com",
                        "$2a$10$SpRsR566UrP/bK2pfKJhe.ghb5Y9/GLjXi/kifJ8x53y5opxHqkr6", Role.USER));
        Member member4 = memberRepository.save(
                new Member("관리자", "admin@roomescape.com",
                        "$2a$10$5xUHgA2/scLa/9YzqkCrXuAoIwLYiZTif8F8QrjuFfSFRgsUdJYhC", Role.ADMIN));

        MemberReservation memberReservation1 = memberReservationRepository.save(
                new MemberReservation(member1, reservation1, ReservationStatus.APPROVED));
        MemberReservation memberReservation2 = memberReservationRepository.save(
                new MemberReservation(member1, reservation3, ReservationStatus.APPROVED));
        MemberReservation memberReservation3 = memberReservationRepository.save(
                new MemberReservation(member1, reservation7, ReservationStatus.APPROVED));
        MemberReservation memberReservation4 = memberReservationRepository.save(
                new MemberReservation(member2, reservation2, ReservationStatus.APPROVED));
        MemberReservation memberReservation5 = memberReservationRepository.save(
                new MemberReservation(member2, reservation4, ReservationStatus.APPROVED));
        MemberReservation memberReservation6 = memberReservationRepository.save(
                new MemberReservation(member2, reservation8, ReservationStatus.APPROVED));
        MemberReservation memberReservation8 = memberReservationRepository.save(
                new MemberReservation(member3, reservation5, ReservationStatus.APPROVED));
        MemberReservation memberReservation9 = memberReservationRepository.save(
                new MemberReservation(member3, reservation6, ReservationStatus.APPROVED));

        MemberReservation memberReservation10 = memberReservationRepository.save(
                new MemberReservation(member1, reservation4, ReservationStatus.PENDING));
        MemberReservation memberReservation11 = memberReservationRepository.save(
                new MemberReservation(member2, reservation6, ReservationStatus.PENDING));
        MemberReservation memberReservation12 = memberReservationRepository.save(
                new MemberReservation(member1, reservation6, ReservationStatus.PENDING));
    }

}
