package tech.blackbookai.vacationmanagementapi.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import tech.blackbookai.vacationmanagementapi.entity.Vacation;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface VacationRepository extends JpaRepository<Vacation, Long> {
    List<Vacation> findByAuthor(Long id);
    List<Vacation> findByAuthorAndStatusIn(Long id, List<String> status);
    List<Vacation> findByStatusIn(List<String> status);
    int countByAuthorAndStatusAndStartDateIsGreaterThanEqual(Long id, String status, LocalDateTime startDate);
}
