package slt.database;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import slt.database.entities.UserAccount;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

interface UserAccountCrudRepository extends CrudRepository<slt.database.entities.UserAccount,Integer> {

    List<UserAccount> findByUsername(String username);

    List<UserAccount> findByEmailIgnoreCase(String email);

    List<UserAccount> findAll();

}

@Repository
public class UserAccountRepository {

    @Autowired
    UserAccountCrudRepository userAccountCrudRepository;

    public Integer updatePassword(Integer accountId, String password, String resetPassword, LocalDateTime resetDate) {

        Optional<slt.database.entities.UserAccount> byId = userAccountCrudRepository.findById(accountId);
        if (byId.isPresent()) {
            byId.get().setPassword(password);
            byId.get().setResetDate(resetDate);
            byId.get().setResetPassword(resetPassword);
            slt.database.entities.UserAccount save = userAccountCrudRepository.save(byId.get());
            return save.getId();
        }
        return null;
    }

    public slt.database.entities.UserAccount insertUser(String username, String password, String email) {
        slt.database.entities.UserAccount userAccount = slt.database.entities.UserAccount.builder()
                .email(email)
                .username(username)
                .password(password)
                .build();
        return  userAccountCrudRepository.save(userAccount);
    }

    public List<UserAccount> getAllUsers() {
        List<slt.database.entities.UserAccount> all = userAccountCrudRepository.findAll();
        List<UserAccount> allAccounts = new ArrayList<>();
        for(UserAccount account : all) {
            allAccounts.add(transformToOther(account));
        }
        return allAccounts;
    }

    public UserAccount getUser(String username) {
        List<slt.database.entities.UserAccount> byUsername = userAccountCrudRepository.findByUsername(username);
        return byUsername.isEmpty() ? null : transformToOther((byUsername.get(0)));
    }

    public UserAccount getUserByEmail(String email) {
        List<slt.database.entities.UserAccount> byUsername = userAccountCrudRepository.findByEmailIgnoreCase(email);
        return byUsername.isEmpty() ? null : transformToOther((byUsername.get(0)));
    }

    public UserAccount getUserById(Integer id) {
        Optional<slt.database.entities.UserAccount> byId = userAccountCrudRepository.findById(id);
        return byId.isPresent() ? transformToOther(byId.get()) : null;
    }

    public void deleteUser(Integer id) {
        userAccountCrudRepository.deleteById(id);
    }

    private UserAccount transformToOther(slt.database.entities.UserAccount jpaEntity) {
        return UserAccount.builder()
                .id(jpaEntity.getId())
                .username(jpaEntity.getUsername())
                .email(jpaEntity.getEmail())
                .password(jpaEntity.getPassword())
                .resetDate(jpaEntity.getResetDate())
                .resetPassword(jpaEntity.getResetPassword())
                .isAdmin(jpaEntity.isAdmin())
                .build();
    }
}

