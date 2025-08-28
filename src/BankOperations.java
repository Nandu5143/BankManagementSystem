import java.sql.*;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class BankOperations {

    // Insert a new customer, return generated customer id
    public long createCustomer(String name, String email, String phone, String address) throws Exception {
        String sql = "INSERT INTO customers (name, email, phone, address) VALUES (?,?,?,?)";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, name);
            ps.setString(2, email);
            ps.setString(3, phone);
            ps.setString(4, address);
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) return rs.getLong(1);
            }
        }
        throw new SQLException("Failed to create customer");
    }

    // Create account for an existing customer, return account_id
    public long createAccount(long customerId, BigDecimal initialDeposit) throws Exception {
        String sql = "INSERT INTO accounts (customer_id, balance) VALUES (?, ?)";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setLong(1, customerId);
            ps.setBigDecimal(2, initialDeposit);
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) return rs.getLong(1);
            }
        }
        throw new SQLException("Failed to create account");
    }

    // Deposit (transactional)
    public void deposit(long accountId, BigDecimal amount) throws Exception {
        if (amount.compareTo(BigDecimal.ZERO) <= 0) throw new IllegalArgumentException("Amount must be > 0");
        String update = "UPDATE accounts SET balance = balance + ? WHERE account_id = ?";
        String insertTxn = "INSERT INTO transactions (account_id, amount, transaction_type) VALUES (?, ?, ?)";
        try (Connection con = DBConnection.getConnection()) {
            con.setAutoCommit(false);
            try (PreparedStatement ps1 = con.prepareStatement(update);
                 PreparedStatement ps2 = con.prepareStatement(insertTxn)) {

                ps1.setBigDecimal(1, amount);
                ps1.setLong(2, accountId);
                if (ps1.executeUpdate() != 1) throw new SQLException("Account not found: " + accountId);

                ps2.setLong(1, accountId);
                ps2.setBigDecimal(2, amount);
                ps2.setString(3, "DEPOSIT");
                ps2.executeUpdate();

                con.commit();
            } catch (Exception e) {
                con.rollback();
                throw e;
            } finally {
                con.setAutoCommit(true);
            }
        }
    }

    // Withdraw (checks balance first)
    public void withdraw(long accountId, BigDecimal amount) throws Exception {
        if (amount.compareTo(BigDecimal.ZERO) <= 0) throw new IllegalArgumentException("Amount must be > 0");

        String select = "SELECT balance FROM accounts WHERE account_id = ?";
        String update = "UPDATE accounts SET balance = balance - ? WHERE account_id = ?";
        String insertTxn = "INSERT INTO transactions (account_id, amount, transaction_type) VALUES (?, ?, ?)";
        try (Connection con = DBConnection.getConnection()) {
            con.setAutoCommit(false);
            try (PreparedStatement psSel = con.prepareStatement(select)) {
                psSel.setLong(1, accountId);
                try (ResultSet rs = psSel.executeQuery()) {
                    if (!rs.next()) throw new SQLException("Account not found: " + accountId);
                    BigDecimal bal = rs.getBigDecimal("balance");
                    if (bal.compareTo(amount) < 0) throw new SQLException("Insufficient funds");
                }

                try (PreparedStatement psUpd = con.prepareStatement(update);
                     PreparedStatement psTxn = con.prepareStatement(insertTxn)) {
                    psUpd.setBigDecimal(1, amount);
                    psUpd.setLong(2, accountId);
                    psUpd.executeUpdate();

                    psTxn.setLong(1, accountId);
                    psTxn.setBigDecimal(2, amount);
                    psTxn.setString(3, "WITHDRAW");
                    psTxn.executeUpdate();

                    con.commit();
                }
            } catch (Exception e) {
                con.rollback();
                throw e;
            } finally {
                con.setAutoCommit(true);
            }
        }
    }

    // Get current balance
    public BigDecimal getBalance(long accountId) throws Exception {
        String sql = "SELECT balance FROM accounts WHERE account_id = ?";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setLong(1, accountId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getBigDecimal("balance");
                else throw new SQLException("Account not found: " + accountId);
            }
        }
    }

    // Get recent transactions for an account (string list for console)
    public List<String> getTransactions(long accountId) throws Exception {
        String sql = "SELECT transaction_id, amount, transaction_type, transaction_date FROM transactions WHERE account_id = ? ORDER BY transaction_id DESC";
        List<String> out = new ArrayList<>();
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setLong(1, accountId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    out.add(String.format("Txn #%d | %s | %s | %s",
                            rs.getLong("transaction_id"),
                            rs.getString("transaction_type"),
                            rs.getBigDecimal("amount"),
                            rs.getTimestamp("transaction_date")));
                }
            }
        }
        return out;
    }

    // List accounts (with holder name)
    public List<String> listAccounts() throws Exception {
        String sql = "SELECT a.account_id, a.balance, c.name FROM accounts a LEFT JOIN customers c ON a.customer_id = c.id ORDER BY a.account_id";
        List<String> out = new ArrayList<>();
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                out.add(String.format("Account #%d | Holder: %s | Balance: %s",
                        rs.getLong("account_id"),
                        rs.getString("name"),
                        rs.getBigDecimal("balance")));
            }
        }
        return out;
    }
}
