package roomescape.common.config;

import static roomescape.common.config.DataFixture.ADMIN;
import static roomescape.common.config.DataFixture.MEMBER_BRE;
import static roomescape.common.config.DataFixture.MEMBER_BROWN;
import static roomescape.common.config.DataFixture.MEMBER_GOOGOO;
import static roomescape.common.config.DataFixture.MEMBER_JOJO;
import static roomescape.common.config.DataFixture.MEMBER_LISA;
import static roomescape.common.config.DataFixture.MEMBER_NEO;
import static roomescape.common.config.DataFixture.MEMBER_POBI;
import static roomescape.common.config.DataFixture.MEMBER_SOLAR;
import static roomescape.common.config.DataFixture.MEMBER_TOMI;
import static roomescape.common.config.DataFixture.THEME_ART_GALLERY;
import static roomescape.common.config.DataFixture.THEME_AZTEC_TEMPLE;
import static roomescape.common.config.DataFixture.THEME_BASEMENT;
import static roomescape.common.config.DataFixture.THEME_DENTIST;
import static roomescape.common.config.DataFixture.THEME_HORROR;
import static roomescape.common.config.DataFixture.THEME_HORROR_THEME_PARK;
import static roomescape.common.config.DataFixture.THEME_MONKEY;
import static roomescape.common.config.DataFixture.THEME_NAGAYA_SANDA;
import static roomescape.common.config.DataFixture.THEME_PRISON_BREAK;
import static roomescape.common.config.DataFixture.THEME_SECRET_AGENT;
import static roomescape.common.config.DataFixture.THEME_SF;
import static roomescape.common.config.DataFixture.THEME_SPACE_STATION;
import static roomescape.common.config.DataFixture.THEME_TITANIC;
import static roomescape.common.config.DataFixture.THEME_VIRUS;
import static roomescape.common.config.DataFixture.THEME_ZOMBIE;
import static roomescape.common.config.DataFixture.TIME_10_00;
import static roomescape.common.config.DataFixture.TIME_10_30;
import static roomescape.common.config.DataFixture.TIME_11_00;
import static roomescape.common.config.DataFixture.TIME_11_30;
import static roomescape.common.config.DataFixture.TIME_12_00;
import static roomescape.common.config.DataFixture.TIME_12_30;
import static roomescape.common.config.DataFixture.TIME_13_00;
import static roomescape.common.config.DataFixture.TIME_13_30;
import static roomescape.common.config.DataFixture.TIME_14_00;
import static roomescape.common.config.DataFixture.TIME_14_30;
import static roomescape.common.config.DataFixture.TODAY;
import static roomescape.common.config.DataFixture.TOMORROW;
import static roomescape.reservation.domain.Status.SUCCESS;
import static roomescape.reservation.domain.Status.WAIT;

import java.util.List;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import roomescape.member.domain.Member;
import roomescape.member.repository.MemberRepository;
import roomescape.reservation.domain.Reservation;
import roomescape.reservation.domain.ReservationTime;
import roomescape.reservation.domain.Theme;
import roomescape.reservation.repository.ReservationRepository;
import roomescape.reservation.repository.ReservationTimeRepository;
import roomescape.reservation.repository.ThemeRepository;

@Profile("!test")
@Component
public class DataLoader implements ApplicationRunner {

    private final MemberRepository memberRepository;
    private final ThemeRepository themeRepository;
    private final ReservationTimeRepository reservationTimeRepository;
    private final ReservationRepository reservationRepository;

    public DataLoader(
            MemberRepository memberRepository,
            ThemeRepository themeRepository,
            ReservationTimeRepository reservationTimeRepository,
            ReservationRepository reservationRepository
    ) {
        this.memberRepository = memberRepository;
        this.themeRepository = themeRepository;
        this.reservationTimeRepository = reservationTimeRepository;
        this.reservationRepository = reservationRepository;
    }

    @Override
    public void run(ApplicationArguments args) throws Exception {
        // member
        Member jojo = memberRepository.save(MEMBER_JOJO);
        Member admin = memberRepository.save(ADMIN);
        Member solar = memberRepository.save(MEMBER_SOLAR);
        Member brown = memberRepository.save(MEMBER_BROWN);
        Member neo = memberRepository.save(MEMBER_NEO);
        Member bre = memberRepository.save(MEMBER_BRE);
        Member pobi = memberRepository.save(MEMBER_POBI);
        Member googoo = memberRepository.save(MEMBER_GOOGOO);
        Member tomi = memberRepository.save(MEMBER_TOMI);
        Member risa = memberRepository.save(MEMBER_LISA);

        // theme
        Theme horror = themeRepository.save(THEME_HORROR);
        Theme sf = themeRepository.save(THEME_SF);
        Theme monkey = themeRepository.save(THEME_MONKEY);
        Theme zombie = themeRepository.save(THEME_ZOMBIE);
        Theme nagayaSanda = themeRepository.save(THEME_NAGAYA_SANDA);
        Theme horrorThemePark = themeRepository.save(THEME_HORROR_THEME_PARK);
        Theme basement = themeRepository.save(THEME_BASEMENT);
        Theme titanic = themeRepository.save(THEME_TITANIC);
        Theme artGallery = themeRepository.save(THEME_ART_GALLERY);
        Theme virus = themeRepository.save(THEME_VIRUS);
        Theme prisonBreak = themeRepository.save(THEME_PRISON_BREAK);
        Theme aztecTemple = themeRepository.save(THEME_AZTEC_TEMPLE);
        Theme spaceStation = themeRepository.save(THEME_SPACE_STATION);
        Theme dentist = themeRepository.save(THEME_DENTIST);
        Theme secretAgent = themeRepository.save(THEME_SECRET_AGENT);

        // reservationTime
        ReservationTime time10_00 = reservationTimeRepository.save(TIME_10_00);
        ReservationTime time10_30 = reservationTimeRepository.save(TIME_10_30);
        ReservationTime time11_00 = reservationTimeRepository.save(TIME_11_00);
        ReservationTime time11_30 = reservationTimeRepository.save(TIME_11_30);
        ReservationTime time12_00 = reservationTimeRepository.save(TIME_12_00);
        ReservationTime time12_30 = reservationTimeRepository.save(TIME_12_30);
        ReservationTime time13_00 = reservationTimeRepository.save(TIME_13_00);
        ReservationTime time13_30 = reservationTimeRepository.save(TIME_13_30);
        ReservationTime time14_00 = reservationTimeRepository.save(TIME_14_00);
        ReservationTime time14_30 = reservationTimeRepository.save(TIME_14_30);

        // reservation
        List<Reservation> reservations = List.of(
                new Reservation(jojo, TODAY, horror, time10_00, SUCCESS),
                new Reservation(solar, TODAY, horror, time10_00, WAIT),
                new Reservation(brown, TODAY, sf, time12_00, SUCCESS),
                new Reservation(jojo, TODAY, sf, time12_00, WAIT),
                new Reservation(googoo, TOMORROW, zombie, time14_30, SUCCESS),
                new Reservation(jojo, TOMORROW, zombie, time14_30, WAIT),
                new Reservation(jojo, TODAY.plusDays(2), monkey, time11_00, SUCCESS),
                new Reservation(jojo, TODAY.plusDays(5), nagayaSanda, time10_00, SUCCESS),
                new Reservation(jojo, TODAY.plusDays(7), virus, time12_30, SUCCESS),
                new Reservation(neo, TODAY.plusDays(3), virus, time11_30, SUCCESS),
                new Reservation(bre, TODAY.plusDays(2), horrorThemePark, time14_30, SUCCESS),
                new Reservation(pobi, TODAY.plusDays(2), horror, time14_30, SUCCESS),
                new Reservation(tomi, TODAY.plusDays(3), titanic, time10_30, SUCCESS),
                new Reservation(risa, TODAY.plusDays(4), artGallery, time14_00, SUCCESS),
                new Reservation(solar, TODAY.plusDays(2), horror, time14_30, WAIT),
                new Reservation(jojo, TODAY.plusDays(2), horror, time14_30, WAIT)
        );
        reservationRepository.saveAll(reservations);
    }
}
