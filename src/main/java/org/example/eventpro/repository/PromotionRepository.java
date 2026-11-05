package org.example.eventpro.repository;

import jakarta.persistence.LockModeType;
import org.example.eventpro.entity.Promotion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PromotionRepository extends JpaRepository<Promotion, Long> {
    boolean existsByTicketCategoryIdAndCodeIgnoreCase(Long categoryId, String code);

    boolean existsByTicketCategoryIdAndCodeIgnoreCaseAndIdNot(Long categoryId, String code, Long id);

    Optional<Promotion> findByCodeIgnoreCase(String code); // Used in the reservation

    List<Promotion> findByTicketCategoryId(Long ticketCategoryId);

    Optional<Promotion> findByIdAndTicketCategoryEventOrganizerId(Long id, UUID organizerId);

    List<Promotion> findByTicketCategoryEventOrganizerId(UUID organizerId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT p FROM Promotion p WHERE p.id = :id")
    Optional<Promotion> findByIdForUpdate(@Param("id") Long id);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT p FROM Promotion p WHERE UPPER(p.code) = UPPER(:code) ")
    Optional<Promotion> findByCodeIgnoreCaseForUpdate(@Param("code") String code);
}
