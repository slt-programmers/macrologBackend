package slt.database;

import org.springframework.data.repository.CrudRepository;
import slt.database.entities.UserAccount;

import java.util.List;

public interface UserAccountCrudRepository extends CrudRepository<UserAccount,Integer> {

    List<UserAccount> findByUsername(String username);
    List<UserAccount> findByEmailIgnoreCase(String email);

//    @Query("SELECT s FROM Setting s WHERE s.name=:name and s.category=:category")
//    List<Setting> fetchArticles(@Param("title") String title, @Param("category") String category);
}
