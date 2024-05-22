package roomescape.learningtest.idstrategy;

import org.springframework.data.jpa.repository.JpaRepository;

public interface SomeEntityRepository extends JpaRepository<SomeEntity, Long> {

}
