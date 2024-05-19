package roomescape.learningtest.idstrategy;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

@Entity
public class SomeEntity {
    @Id
    //아래의 strategy 를 변경해 가면서 어떻게 쿼리가 달라지는지 확인해 볼 수 있다.
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    public SomeEntity(String name) {
        this.name = name;
    }

    public SomeEntity() {
    }

    @Override
    public String toString() {
        return "SomeEntity{" +
                "id=" + id +
                ", name='" + name + '\'' +
                '}';
    }

    public long getId() {
        return id;
    }
}
