package ma.messaging.usermanagementservice.repository;

import jakarta.transaction.Transactional;
import ma.messaging.usermanagementservice.model.Account;
import ma.messaging.usermanagementservice.model.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
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

    @Query("SELECT a.roles FROM Account a WHERE a.account_id = :accountId")
    List<Role> findRolesByAccountId(@Param("accountId") int accountId);

    @Query("SELECT a FROM Account a WHERE a.account_id = :account_id")
    Optional<Account> findAccountByAccount_id(int account_id);

    @Transactional
    @Modifying
    @Query("DELETE FROM Account a WHERE a.account_id = :account_id")
    void deleteAccountByAccount_id(int account_id);

}
