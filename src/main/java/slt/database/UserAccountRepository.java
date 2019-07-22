package slt.database;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import slt.database.entities.UserAccount;

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

    public UserAccount saveAccount(UserAccount account) {
        return userAccountCrudRepository.save(account);
    }
    public UserAccount insertUser(String username, String password, String email) {
        UserAccount userAccount = UserAccount.builder()
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
            allAccounts.add(account);
        }
        return allAccounts;
    }

    public UserAccount getUser(String username) {
        List<slt.database.entities.UserAccount> byUsername = userAccountCrudRepository.findByUsername(username);
        return byUsername.isEmpty() ? null : byUsername.get(0);
    }

    public UserAccount getUserByEmail(String email) {
        List<slt.database.entities.UserAccount> byUsername = userAccountCrudRepository.findByEmailIgnoreCase(email);
        return byUsername.isEmpty() ? null : byUsername.get(0);
    }

    public UserAccount getUserById(Integer id) {
        Optional<slt.database.entities.UserAccount> byId = userAccountCrudRepository.findById(id);
        return byId.isPresent() ? byId.get():null;
    }

    public void deleteUser(Integer id) {
        userAccountCrudRepository.deleteById(id);
    }
}

