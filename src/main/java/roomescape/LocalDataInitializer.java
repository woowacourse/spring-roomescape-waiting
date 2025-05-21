package roomescape;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;
import roomescape.business.model.entity.Reservation;
import roomescape.business.model.entity.ReservationSlot;
import roomescape.business.model.entity.ReservationTime;
import roomescape.business.model.entity.Theme;
import roomescape.business.model.entity.User;
import roomescape.business.model.repository.ReservationSlots;
import roomescape.business.model.repository.ReservationTimes;
import roomescape.business.model.repository.Reservations;
import roomescape.business.model.repository.Themes;
import roomescape.business.model.repository.Users;

import java.time.LocalDate;
import java.time.LocalTime;

@Component
@Profile("local")
@RequiredArgsConstructor
public class LocalDataInitializer {

    private final Logger logger = LoggerFactory.getLogger(LocalDataInitializer.class);
    private final Themes themes;
    private final ReservationTimes times;
    private final Users users;
    private final Reservations reservations;
    private final ReservationSlots slots;
    private static final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

    @PostConstruct
    public void init() {
        final Theme theme1 = new Theme("미스터리 저택", "기묘한 사건이 벌어지는 저택을 탈출하라!", "mystery.jpg");
        final Theme theme2 = new Theme("사라진 시간", "시간을 거슬러 단서를 찾아라!", "time.jpg");
        final ReservationTime time1 = new ReservationTime(LocalTime.of(14, 0));
        final ReservationTime time2 = new ReservationTime(LocalTime.of(16, 0));
        final User user1 = new User("dompoo", "dompoo@gmail.com", encoder.encode("1234"));
        final User user2 = new User("lemon", "lemon@gmail.com", encoder.encode("1234"));
        final User admin = User.admin("admin", "admin@gmail.com", encoder.encode("1234"));
        final ReservationSlot reservationSlot1 = new ReservationSlot(time1, LocalDate.now().plusDays(1), theme1);
        final ReservationSlot reservationSlot2 = new ReservationSlot(time1, LocalDate.now().plusDays(2), theme2);
        final ReservationSlot reservationSlot3 = new ReservationSlot(time1, LocalDate.now().plusDays(3), theme1);
        final ReservationSlot reservationSlot4 = new ReservationSlot(time1, LocalDate.now().plusDays(3), theme2);
        final ReservationSlot reservationSlot5 = new ReservationSlot(time2, LocalDate.now().plusDays(1), theme2);
        final ReservationSlot reservationSlot6 = new ReservationSlot(time2, LocalDate.now().plusDays(2), theme1);
        final ReservationSlot reservationSlot7 = new ReservationSlot(time2, LocalDate.now().plusDays(4), theme2);
        final Reservation reservation1 = new Reservation(user1, reservationSlot1);
        final Reservation reservation2 = new Reservation(user1, reservationSlot2);
        final Reservation reservation3 = new Reservation(user1, reservationSlot3);
        final Reservation reservation4 = new Reservation(user1, reservationSlot4);
        final Reservation reservation5 = new Reservation(user2, reservationSlot5);
        final Reservation reservation6 = new Reservation(user2, reservationSlot6);
        final Reservation reservation7 = new Reservation(user2, reservationSlot7);
        insertThemes(theme1, theme2);
        insertTimes(time1, time2);
        insertSlots(reservationSlot1, reservationSlot2, reservationSlot3, reservationSlot4, reservationSlot5, reservationSlot6, reservationSlot7);
        insertUsers(user1, user2, admin);
        insertReservations(reservation1, reservation2, reservation3, reservation4, reservation5, reservation6, reservation7);
        logger.info("local 테스트용 데이터 init 성공!");
    }

    private void insertThemes(final Theme... themes) {
        for (Theme theme : themes) {
            this.themes.save(theme);
        }
    }

    private void insertTimes(final ReservationTime... times) {
        for (ReservationTime time : times) {
            this.times.save(time);
        }
    }

    private void insertSlots(final ReservationSlot... slots) {
        for (ReservationSlot slot : slots) {
            this.slots.save(slot);
        }
    }

    private void insertUsers(final User... users) {
        for (User user : users) {
            this.users.save(user);
        }
    }

    private void insertReservations(final Reservation... reservations) {
        for (Reservation reservation : reservations) {
            this.reservations.save(reservation);
        }
    }

}
