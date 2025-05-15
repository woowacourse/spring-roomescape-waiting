package roomescape.reservation.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import roomescape.reservation.dao.reservation.ReservationDaoImpl;
import roomescape.reservation.dao.reservation.ReservationDao;
import roomescape.reservation.dao.reservationTime.ReservationTimeDaoImpl;
import roomescape.reservation.dao.reservationTime.ReservationTimeDao;
import roomescape.reservation.dao.theme.ThemeDaoImpl;
import roomescape.reservation.dao.theme.ThemeDao;

@Configuration
public class ReservationConfig {

    @Bean
    public ReservationDao reservationDao(@Autowired ReservationDaoImpl reservationDao) {
        return reservationDao;
    }

    @Bean
    public ReservationTimeDao reservationTimeDao(@Autowired ReservationTimeDaoImpl reservationTimeDao) {
        return reservationTimeDao;
    }

    @Bean
    public ThemeDao themeDao(@Autowired ThemeDaoImpl themeDao) {
        return themeDao;
    }
}
