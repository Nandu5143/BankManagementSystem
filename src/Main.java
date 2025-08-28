import java.util.Scanner;
import java.math.BigDecimal;
import java.util.List;

public class Main {
    public static void main(String[] args) {
        BankOperations ops = new BankOperations();
        Scanner sc = new Scanner(System.in);

        while (true) {
            System.out.println("\n1) Create customer  2) Create account  3) Deposit  4) Withdraw  5) Balance  6) Transactions  7) List accounts  8) Exit");
            System.out.print("Choose: ");
            String choice = sc.nextLine().trim();

            try {
                switch (choice) {
                    case "1":
                        System.out.print("Name: ");
                        String name = sc.nextLine().trim();
                        System.out.print("Email: ");
                        String email = sc.nextLine().trim();
                        System.out.print("Phone: ");
                        String phone = sc.nextLine().trim();
                        System.out.print("Address: ");
                        String addr = sc.nextLine().trim();
                        long cid = ops.createCustomer(name, email, phone, addr);
                        System.out.println("Customer created with id: " + cid);
                        break;

                    case "2":
                        System.out.print("Customer id: ");
                        long custId = Long.parseLong(sc.nextLine().trim());
                        System.out.print("Initial deposit (e.g. 5000.00): ");
                        BigDecimal init = new BigDecimal(sc.nextLine().trim());
                        long accId = ops.createAccount(custId, init);
                        System.out.println("Account created with id: " + accId);
                        break;

                    case "3":
                        System.out.print("Account id: ");
                        long accDep = Long.parseLong(sc.nextLine().trim());
                        System.out.print("Amount: ");
                        BigDecimal damt = new BigDecimal(sc.nextLine().trim());
                        ops.deposit(accDep, damt);
                        System.out.println("Deposited successfully.");
                        break;

                    case "4":
                        System.out.print("Account id: ");
                        long accW = Long.parseLong(sc.nextLine().trim());
                        System.out.print("Amount: ");
                        BigDecimal wamt = new BigDecimal(sc.nextLine().trim());
                        ops.withdraw(accW, wamt);
                        System.out.println("Withdrawn successfully.");
                        break;

                    case "5":
                        System.out.print("Account id: ");
                        long accB = Long.parseLong(sc.nextLine().trim());
                        System.out.println("Balance: " + ops.getBalance(accB));
                        break;

                    case "6":
                        System.out.print("Account id: ");
                        long accT = Long.parseLong(sc.nextLine().trim());
                        List<String> txs = ops.getTransactions(accT);
                        if (txs.isEmpty()) System.out.println("No transactions found.");
                        else txs.forEach(System.out::println);
                        break;

                    case "7":
                        List<String> accs = ops.listAccounts();
                        if (accs.isEmpty()) System.out.println("No accounts found.");
                        else accs.forEach(System.out::println);
                        break;

                    case "8":
                        System.out.println("Goodbye!");
                        sc.close();
                        return;

                    default:
                        System.out.println("Invalid option. Try again.");
                }
            } catch (Exception e) {
                System.out.println("Error: " + e.getMessage());
                // for debugging you can uncomment the next line:
                // e.printStackTrace();
            }
        }
    }
}
