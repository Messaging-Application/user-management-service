package ma.messaging.usermanagementservice.repository;

import ma.messaging.usermanagementservice.model.Account;
import ma.messaging.usermanagementservice.model.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.Optional;
import java.util.List;

@Repository
public interface AccountRepository extends JpaRepository<Account, Integer> {
    Optional<Account> findByUsername(String username);

    Boolean existsByUsername(String username);

    Boolean existsByEmail(String email);

    Optional<Account> findById(int id);

    void deleteById(int id);

    @Query("SELECT a.roles FROM Account a WHERE a.account_id = :accountId")
    List<Role> findRolesByAccountId(@Param("accountId") int accountId);

    @Query("SELECT a FROM Account a WHERE a.account_id = :account_id")
    Optional<Account> findAccountByAccount_id(int account_id);

}
