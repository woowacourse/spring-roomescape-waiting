package roomescape.entity;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import roomescape.exception.custom.InvalidReservationException;
import roomescape.global.ReservationStatus;
import roomescape.global.Role;

@Entity
public class Member {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "member_id")
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String email;

    @Column(nullable = false)
    private String password;

    @Enumerated(value = EnumType.STRING)
    @Column(nullable = false)
    private Role role;

    @OneToMany(mappedBy = "member",
            cascade = CascadeType.ALL,
            orphanRemoval = true)
    private List<Reservation> reservations = new ArrayList<>();

    protected Member() {
    }

    public Member(Long id, String name, String email, String password, Role role) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.password = password;
        this.role = role;
    }

    public Member(String name, String email, String password, Role role) {
        this(null, name, email, password, role);
    }

    public Reservation reserve(LocalDate date, ReservationTime time, Theme theme) {
        Reservation reservation = new Reservation(this, date, time, theme, ReservationStatus.RESERVED);

        validateDuplicateReservation(reservation);
        validatePastDateTime(reservation);

        this.reservations.add(reservation);
        return reservation;
    }

    private void validateDuplicateReservation(Reservation target) {
        boolean exist = reservations.stream()
                .anyMatch(target::equals);
        if (exist) {
            throw new InvalidReservationException("중복된 예약신청입니다");
        }
    }

    private void validatePastDateTime(Reservation reservation) {
        reservation.isBefore(LocalDateTime.now());
    }


    public void removeReservation(Reservation reservation) {
        reservations.remove(reservation);
        reservation.setMember(null);
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }

    public String getPassword() {
        return password;
    }

    public Role getRole() {
        return role;
    }

    public List<Reservation> getReservations() {
        return Collections.unmodifiableList(reservations);
    }

    @Override
    public boolean equals(final Object o) {
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Member member = (Member) o;
        return Objects.equals(getId(), member.getId());
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(getId());
    }
}
