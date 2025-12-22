package slt.database;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import slt.database.entities.UserAccount;

import java.util.List;
import java.util.Optional;

interface UserAccountCrudRepository extends CrudRepository<UserAccount, Long> {

    List<UserAccount> findByUsername(final String username);

    List<UserAccount> findByEmailIgnoreCase(final String email);

}

@Repository
public class UserAccountRepository {

    @Autowired
    UserAccountCrudRepository userAccountCrudRepository;

    public UserAccount saveAccount(final UserAccount account) {
        return userAccountCrudRepository.save(account);
    }

    public UserAccount insertUser(final String username, final String password, final String email) {
        UserAccount userAccount = UserAccount.builder()
                .email(email)
                .username(username)
                .password(password)
                .build();
        return userAccountCrudRepository.save(userAccount);
    }

    public List<UserAccount> getAllUsers() {
        return (List<UserAccount>) userAccountCrudRepository.findAll();
    }

    public UserAccount getUser(final String username) {
        List<UserAccount> byUsername = userAccountCrudRepository.findByUsername(username);
        return byUsername.isEmpty() ? null : byUsername.getFirst();
    }

    public UserAccount getUserByEmail(final String email) {
        List<UserAccount> byUsername = userAccountCrudRepository.findByEmailIgnoreCase(email);
        return byUsername.isEmpty() ? null : byUsername.getFirst();
    }

    public UserAccount getUserById(final Long id) {
        Optional<UserAccount> byId = userAccountCrudRepository.findById(id);
        return byId.orElse(null);
    }

    public void deleteUser(final Long id) {
        userAccountCrudRepository.deleteById(id);
    }
}

