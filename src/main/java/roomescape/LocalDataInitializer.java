package roomescape;

import jakarta.annotation.PostConstruct;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;
import roomescape.business.model.entity.Reservation;
import roomescape.business.model.entity.ReservationTime;
import roomescape.business.model.entity.Theme;
import roomescape.business.model.entity.User;
import roomescape.business.model.repository.ReservationRepository;
import roomescape.business.model.repository.ReservationTimeRepository;
import roomescape.business.model.repository.ThemeRepository;
import roomescape.business.model.repository.UserRepository;
import roomescape.business.model.vo.ReservationStatus;
import roomescape.business.model.vo.UserRole;

@Component
@Profile("local")
@RequiredArgsConstructor
public class LocalDataInitializer {

    private static final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
    private final Logger logger = LoggerFactory.getLogger(LocalDataInitializer.class);
    private final ThemeRepository themeRepository;
    private final ReservationTimeRepository timeRepository;
    private final UserRepository userRepository;
    private final ReservationRepository reservationRepository;

    @PostConstruct
    public void init() {
        final Theme theme1 = Theme.restore("1", "미스터리 저택", "기묘한 사건이 벌어지는 저택을 탈출하라!", "mystery.jpg");
        final Theme theme2 = Theme.restore("2", "사라진 시간", "시간을 거슬러 단서를 찾아라!", "time.jpg");
        final ReservationTime time1 = ReservationTime.restore("3", LocalTime.of(14, 0));
        final ReservationTime time2 = ReservationTime.restore("4", LocalTime.of(16, 0));
        final User user1 = User.restore("1", UserRole.USER.name(), "ddiyong", "ddiyong@gmail.com",
                encoder.encode("1234"));
        final User user5 = User.restore("5", UserRole.USER.name(), "dompoo", "dompoo@gmail.com",
                encoder.encode("1234"));
        final User user6 = User.restore("6", UserRole.USER.name(), "lemon", "lemon@gmail.com", encoder.encode("1234"));
        final User admin = User.restore("7", UserRole.ADMIN.name(), "admin", "admin@gmail.com", encoder.encode("1234"));
        final Reservation reservation1 = Reservation.restore("10", user5, LocalDate.now().plusDays(1), time1, theme1,
                ReservationStatus.RESERVED, LocalDateTime.now());
        final Reservation reservation2 = Reservation.restore("11", user5, LocalDate.now().plusDays(1), time2, theme2,
                ReservationStatus.RESERVED, LocalDateTime.now());
        final Reservation reservation3 = Reservation.restore("12", user5, LocalDate.now().plusDays(2), time2, theme1,
                ReservationStatus.RESERVED, LocalDateTime.now());
        final Reservation reservation4 = Reservation.restore("13", user5, LocalDate.now().plusDays(2), time1, theme2,
                ReservationStatus.RESERVED, LocalDateTime.now());
        final Reservation reservation5 = Reservation.restore("14", user6, LocalDate.now().plusDays(3), time1, theme1,
                ReservationStatus.RESERVED, LocalDateTime.now());
        final Reservation reservation6 = Reservation.restore("15", user1, LocalDate.now().plusDays(3), time1, theme2,
                ReservationStatus.RESERVED, LocalDateTime.now());
        final Reservation reservation7 = Reservation.restore("16", user1, LocalDate.now().plusDays(4), time2, theme2,
                ReservationStatus.RESERVED, LocalDateTime.now());
        final Reservation reservation7_1 = Reservation.restore("17", user5, LocalDate.now().plusDays(4), time2, theme2,
                ReservationStatus.WAITING, LocalDateTime.now());
        insertThemes(theme1, theme2);
        insertTimes(time1, time2);
        insertUsers(user1, user5, user6, admin);
        insertReservations(reservation1, reservation2, reservation3, reservation4, reservation5, reservation6,
                reservation7, reservation7_1);
        logger.info("local 테스트용 데이터 init 성공!");
    }

    private void insertThemes(final Theme... themes) {
        for (Theme theme : themes) {
            themeRepository.save(theme);
        }
    }

    private void insertTimes(final ReservationTime... times) {
        for (ReservationTime time : times) {
            timeRepository.save(time);
        }
    }

    private void insertUsers(final User... users) {
        for (User user : users) {
            userRepository.save(user);
        }
    }

    private void insertReservations(final Reservation... reservations) {
        for (Reservation reservation : reservations) {
            reservationRepository.save(reservation);
        }
    }

}
