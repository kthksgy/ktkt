package ktkt.dao;

import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import ktkt.Main;
import ktkt.U;
import ktkt.dto.Employee;

public class Employees {

  public static void createTable() {
    try (Statement statement = Main.con.createStatement()) {
      statement.execute("CREATE TABLE IF NOT EXISTS employees (\n"
          + "id          INT PRIMARY KEY AUTO_INCREMENT,\n" + "password    VARCHAR(64) NOT NULL,\n"
          + "salt        VARCHAR(64) NOT NULL,\n" + "last_name   VARCHAR(32) NOT NULL,\n"
          + "first_name  VARCHAR(32) NOT NULL,\n" + "joined_at   DATE NOT NULL,\n"
          + "is_valid    BOOLEAN NOT NULL DEFAULT TRUE,\n"
          + "is_admin    BOOLEAN NOT NULL DEFAULT FALSE,\n"
          + "created_at  DATETIME DEFAULT CURRENT_TIMESTAMP,"
          + "updated_at  DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP\n" + ")");
      insert(1, "password", "管理者", "管理者", LocalDate.of(1900, 1, 1));
      setPermission(1, true);
    } catch (SQLException e) {
      System.out.println("社員テーブルの作成に失敗しました。");
      throw new RuntimeException(e);
    }
  }

  public static void dropTable() {
    try (Statement statement = Main.con.createStatement()) {
      statement.execute("DROP TABLE IF EXISTS employees");
    } catch (SQLException e) {
      System.out.println("社員テーブルの削除に失敗しました。");
      throw new RuntimeException(e);
    }
  }

  private static List<Employee> get(PreparedStatement stmt) {
    if (stmt == null) {
      return null;
    }
    try {
      List<Employee> ret = new ArrayList<>();
      ResultSet rs = stmt.executeQuery();
      while (rs.next()) {
        ret.add(new Employee(rs));
      }
      stmt.close();
      return ret;
    } catch (SQLException e) {
      return null;
    }
  }

  public static Employee get(Integer id) {
    String query = "SELECT * FROM employees WHERE id = ?";
    try (PreparedStatement stmt = Main.con.prepareStatement(query)) {
      stmt.setInt(1, id);
      List<Employee> es = get(stmt);
      if (es == null || es.isEmpty()) {
        return null;
      } else {
        return es.get(0);
      }
    } catch (SQLException e) {
      return null;
    }
  }

  public static List<Employee> getAll() {
    String query = "SELECT * FROM employees";
    try (PreparedStatement stmt = Main.con.prepareStatement(query)) {
      return get(stmt);
    } catch (SQLException e) {
      return null;
    }
  }

  public static List<Employee> getMembers(Integer team_id) {
    String query = "SELECT * FROM affiliations INNER JOIN employees ON member = id WHERE team = ?";
    try (PreparedStatement stmt = Main.con.prepareStatement(query)) {
      stmt.setInt(1, team_id);
      return get(stmt);
    } catch (SQLException e) {
      return null;
    }
  }

  public static List<Employee> search(LocalDate begin, LocalDate end) {
    String query = "SELECT * FROM employees WHERE DATE(?) <= joined_at AND joined_at <= DATE(?)";
    try (PreparedStatement stmt = Main.con.prepareStatement(query)) {
      stmt.setString(1, begin.toString());
      stmt.setString(2, end.toString());
      return get(stmt);
    } catch (SQLException e) {
      return null;
    }
  }

  public static List<Employee> search(String lastName, String firstName) {
    String query = "SELECT * FROM employees WHERE last_name LIKE ? AND first_name LIKE ?";
    try (PreparedStatement stmt = Main.con.prepareStatement(query)) {
      stmt.setString(1, lastName != null ? "%" + lastName + "%" : "%");
      stmt.setString(2, firstName != null ? "%" + firstName + "%" : "%");
      return get(stmt);
    } catch (SQLException e) {
      return null;
    }
  }

  public static int insert(Integer id, String plainPassword, String lastName, String firstName,
      LocalDate joinedAt) {
    String query =
        "INSERT INTO employees VALUES (?, ?, ?, ?, ?, ?, DEFAULT, DEFAULT, DEFAULT, DEFAULT)";
    try (PreparedStatement stmt =
        Main.con.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {
      stmt.setInt(1, id);
      byte[] salt = new byte[32];
      try {
        SecureRandom.getInstanceStrong().nextBytes(salt);
      } catch (NoSuchAlgorithmException e) {
        throw new RuntimeException(e);
      }
      String password = U.secureHash(plainPassword, salt);
      stmt.setString(2, password);
      stmt.setString(3, U.toHex(salt));
      stmt.setString(4, lastName);
      stmt.setString(5, firstName);
      stmt.setString(6, joinedAt.toString());
      stmt.executeUpdate();
      ResultSet rs = stmt.getGeneratedKeys();
      int newId = 0;
      while (rs.next()) {
        newId = rs.getInt(1);
      }
      return newId;
    } catch (SQLException e) {
      return 0;
    }
  }

  public static int interactiveCreate() {
    Integer id = U.inputInt("社員番号を入力してください。(0 = 自動設定)", 0, null);
    String plainPassword = U.inputString("パスワードを入力してください。", 6, 64);
    String lastName = U.inputString("苗字を入力してください。", 1, 32);
    String firstName = U.inputString("名前を入力してください。", 1, 32);
    LocalDate joinedAt = U.inputDate("入社(予定)日を入力してください。", null, null);
    return insert(id, plainPassword, lastName, firstName, joinedAt);
  }

  public static boolean setPermission(Employee employee, Boolean isAdmin) {
    return setPermission(employee.getId(), isAdmin);
  }

  public static boolean setPermission(Integer id, Boolean isAdmin) {
    String query = "UPDATE employees SET is_admin = ? WHERE id = ?";
    try (PreparedStatement stmt = Main.con.prepareStatement(query)) {
      stmt.setBoolean(1, isAdmin);
      stmt.setInt(2, id);
      int rs = stmt.executeUpdate();
      return rs > 0;
    } catch (SQLException e) {
      return false;
    }
  }

  public static boolean setValidity(Employee employee, Boolean isValid) {
    return setValidity(employee.getId(), isValid);
  }

  public static boolean setValidity(Integer id, Boolean isValid) {
    String query = "UPDATE employees SET is_valid = ? WHERE id = ?";
    try (PreparedStatement stmt = Main.con.prepareStatement(query)) {
      stmt.setBoolean(1, isValid);
      stmt.setInt(2, id);
      int rs = stmt.executeUpdate();
      return rs > 0;
    } catch (SQLException e) {
      return false;
    }
  }

  public static boolean setPassword(Employee employee, String plainPassword) {
    String query = "UPDATE employees SET password = ? WHERE id = ?";
    try (PreparedStatement stmt = Main.con.prepareStatement(query)) {
      stmt.setString(1, employee.hash(plainPassword));
      stmt.setInt(2, employee.getId());
      int rs = stmt.executeUpdate();
      return rs > 0;
    } catch (SQLException e) {
      return false;
    }
  }

  public static boolean setName(Employee employee, String lastName, String firstName) {
    String query = "UPDATE employees SET last_name = ?, first_name = ? WHERE id = ?";
    try (PreparedStatement stmt = Main.con.prepareStatement(query)) {
      stmt.setString(1, lastName == null || lastName.isEmpty() ? employee.getLastName() : lastName);
      stmt.setString(2,
          firstName == null || firstName.isEmpty() ? employee.getFirstName() : firstName);
      stmt.setInt(3, employee.getId());
      int rs = stmt.executeUpdate();
      return rs > 0;
    } catch (SQLException e) {
      return false;
    }
  }

  public static void insertDummy() {
    Employees.insert(0, "password", "佐藤", "一郎", LocalDate.of(1967, 4, 1));
    Employees.insert(0, "password", "鈴木", "二郎", LocalDate.of(1985, 10, 1));
    Employees.insert(0, "password", "高橋", "三郎", LocalDate.of(1988, 6, 15));
    Employees.insert(0, "password", "田中", "四郎", LocalDate.of(1989, 4, 10));
    Employees.insert(0, "password", "伊藤", "五郎", LocalDate.of(1996, 4, 1));
    Employees.insert(0, "password", "渡辺", "六郎", LocalDate.of(1996, 4, 1));
    Employees.insert(0, "password", "山本", "七郎", LocalDate.of(1997, 11, 25));
    Employees.insert(0, "password", "中村", "八郎", LocalDate.of(1998, 5, 4));
    Employees.insert(0, "password", "小林", "九郎", LocalDate.of(2000, 7, 16));
    Employees.insert(0, "password", "加藤", "十郎", LocalDate.of(2012, 4, 8));
  }
}
