package slt.database;

import lombok.AllArgsConstructor;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import slt.database.entities.Portion;

import java.util.List;

interface PortionCrudRepository extends CrudRepository<Portion, Long> {
}

@Repository
@AllArgsConstructor
public class PortionRepository {

    private PortionCrudRepository portionCrudRepository;

    public List<Portion> getAllPortions() {
        return (List<Portion>) portionCrudRepository.findAll();
    }

}

