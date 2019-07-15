package slt.database;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import slt.database.entities.Setting;
import slt.database.entities.UserAccount;

import java.util.Date;
import java.util.List;

public interface SettingsCrudRepository extends CrudRepository<Setting,Integer> {

    List<Setting> findByUserIdOrderByDayDesc(Integer userId);
    List<Setting> findByUserIdAndNameOrderByDayDesc(Integer userId, String name);

    void deleteByUserId(Integer userId);




    @Query("select s from Setting s where s.userId = :userId and s.name = :name and s.day <= :day")
    List<Setting> findByUserIdAndNameWithDayBeforeDay(@Param("userId") Integer userId, @Param("name") String name, @Param("day")Date day);

//    @Query("select a from Article a where a.creationDateTime <= :creationDateTime")
//    List<Article> findAllWithCreationDateTimeBefore(
//            @Param("creationDateTime") Date creationDateTime);
//    List<UserAccount> findByEmailIgnoreCase(String email);

//    @Query("SELECT s FROM Setting s WHERE s.name=:name and s.category=:category")
//    List<Setting> fetchArticles(@Param("title") String title, @Param("category") String category);
}
