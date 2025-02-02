package au.com.telstra.simcardactivator.repository;

import au.com.telstra.simcardactivator.model.SimCard;
import org.springframework.data.repository.CrudRepository;

import java.util.Optional;

public interface SimCardRepository extends CrudRepository<SimCard, Long> {
    Optional<SimCard> findByIccid(String iccid);
}