package com.example.cine_booking.repository;

import com.example.cine_booking.model.Screening;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface ScreeningRepository extends JpaRepository<Screening, Long> {
    List<Screening> findByStartTimeAfter(LocalDateTime time);

    /**
     Pour simplifier la requête JPQL complexe, on peut récupérer les séances
     d'une salle sur une plage large (ex : la journée) et filtrer en Java,
     OU faire une requête native. Voici l'approche JPQL :
    */

    @Query("""
        SELECT s FROM Screening s\s
        WHERE s.cinemaHall.id = :hallId\s
        AND s.startTime BETWEEN :start AND :end
   \s""")
    List<Screening> findScreeningsInInterval(@Param("hallId") Long hallId,
                                             @Param("start") LocalDateTime start,
                                             @Param("end") LocalDateTime end);
}
