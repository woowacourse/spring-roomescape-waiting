package roomescape.application.service;

import java.time.LocalDate;
import org.springframework.stereotype.Service;
import roomescape.common.exception.NotFoundException;
import roomescape.dto.request.ReservationAdminRegisterDto;
import roomescape.model.Member;
import roomescape.model.Reservation;
import roomescape.model.ReservationTime;
import roomescape.model.Theme;
import roomescape.infrastructure.db.MemberJpaRepository;
import roomescape.infrastructure.db.ReservationJpaRepository;
import roomescape.infrastructure.db.ReservationTimeJpaRepository;
import roomescape.infrastructure.db.ThemeJpaRepository;

@Service
public class ReservationAdminService {

    private final ReservationJpaRepository reservationJpaRepository;
    private final ThemeJpaRepository themeJpaRepository;
    private final ReservationTimeJpaRepository reservationTimeJpaRepository;
    private final MemberJpaRepository memberJpaRepository;

    public ReservationAdminService(ReservationJpaRepository reservationJpaRepository,
                                   ThemeJpaRepository themeJpaRepository,
                                   ReservationTimeJpaRepository reservationTimeJpaRepository,
                                   MemberJpaRepository memberJpaRepository) {
        this.reservationJpaRepository = reservationJpaRepository;
        this.themeJpaRepository = themeJpaRepository;
        this.reservationTimeJpaRepository = reservationTimeJpaRepository;
        this.memberJpaRepository = memberJpaRepository;
    }

    public void saveReservation(ReservationAdminRegisterDto registerDto) {
        Member member = findMemberById(registerDto.memberId());
        ReservationTime reservationTime = findReservationTimeById(registerDto.timeId());
        Theme theme = findThemeById(registerDto.themeId());

        Reservation reservation = new Reservation(registerDto.date(), reservationTime, theme, member, LocalDate.now());

        reservationJpaRepository.save(reservation);
    }

    private Theme findThemeById(Long id) {
        return themeJpaRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("해당 id 를 가진 테마는 존재하지 않습니다."));
    }

    private ReservationTime findReservationTimeById(Long id) {
        return reservationTimeJpaRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("해당 id 를 가진 예약 시각은 존재하지 않습니다."));
    }

    private Member findMemberById(Long id) {
        return memberJpaRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("해당 id 를 가진 회원은 존재하지 않습니다."));
    }
}
