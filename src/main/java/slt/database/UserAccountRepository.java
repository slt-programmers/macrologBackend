package slt.database;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import slt.database.entities.UserAccount;

import java.util.List;
import java.util.Optional;

interface UserAccountCrudRepository extends CrudRepository<UserAccount, Long> {

    Optional<UserAccount> findByUsername(final String username);

    Optional<UserAccount> findByEmailIgnoreCase(final String email);

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
                .userName(username)
                .password(password)
                .build();
        return userAccountCrudRepository.save(userAccount);
    }

    public List<UserAccount> getAllUsers() {
        return (List<UserAccount>) userAccountCrudRepository.findAll();
    }

    public Optional<UserAccount> getUserByName(final String username) {
        return userAccountCrudRepository.findByUsername(username);
    }

    public Optional<UserAccount> getUserByEmail(final String email) {
        return userAccountCrudRepository.findByEmailIgnoreCase(email);
    }

    public Optional<UserAccount> getUserById(final Long id) {
        return userAccountCrudRepository.findById(id);
    }

    public void deleteUser(final Long id) {
        userAccountCrudRepository.deleteById(id);
    }
}

