package slt.database;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import slt.database.model.UserAccount;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public class UserAcccountRepository {

    @Autowired
    UserAccountCrudRepository userAccountCrudRepository;

    @Autowired
    DatabaseHelper databaseHelper;

    public UserAcccountRepository() {
    }

    public Integer updatePassword(Long accountId, String password, String resetPassword, LocalDateTime resetDate) {

        Optional<slt.database.entities.UserAccount> byId = userAccountCrudRepository.findById(accountId.intValue());
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
        slt.database.entities.UserAccount savedAccount = userAccountCrudRepository.save(userAccount);
        return savedAccount;
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
                .email(jpaEntity.getEmail())
                .id(jpaEntity.getId())
                .password(jpaEntity.getPassword())
                .resetDate(jpaEntity.getResetDate())
                .resetPassword(jpaEntity.getResetPassword())
                .username(jpaEntity.getUsername())
                .build();
    }
}

