package com.scoring.pmescoring.Repository;

import com.scoring.pmescoring.Domain.Firm;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface FirmRepository extends JpaRepository<Firm, Long> {
}
