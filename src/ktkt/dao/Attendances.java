package ktkt.dao;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import ktkt.Main;
import ktkt.dto.Attendance;

public class Attendances {
  public static void createTable() {
    try (Statement statement = Main.con.createStatement()) {
      statement.execute("CREATE TABLE IF NOT EXISTS attendances (\n"
          + "id           INT NOT NULL,\n" + "state        BOOLEAN NOT NULL,\n"
          + "time         DATETIME NOT NULL,\n" + "created_at   DATETIME DEFAULT CURRENT_TIMESTAMP,"
          + "updated_at   DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,\n"
          + "FOREIGN KEY  fk_id(id) REFERENCES employees(id),\n" + "PRIMARY KEY  (id, time)\n"
          + ")");
    } catch (SQLException e) {
      System.out.println("出退勤テーブルの作成に失敗しました。");
      throw new RuntimeException(e);
    }
  }

  public static void dropTable() {
    try (Statement statement = Main.con.createStatement()) {
      statement.execute("DROP TABLE IF EXISTS attendances");
    } catch (SQLException e) {
      System.out.println("出退勤テーブルの削除に失敗しました。");
      throw new RuntimeException(e);
    }
  }

  public static List<Attendance> getAttendances(Integer id, int num) {
    String query = "SELECT * FROM attendances WHERE id = ? ORDER BY time DESC LIMIT ?";
    PreparedStatement stmt = null;
    try {
      stmt = Main.con.prepareStatement(query);
      stmt.setInt(1, id);
      stmt.setInt(2, num);
    } catch (SQLException e) {
    }
    return getAttendances(stmt);
  }

  public static List<Attendance> getAttendances(LocalDate begin, LocalDate end) {
    String query = "SELECT * FROM attendances WHERE ? <= time AND time <= ? ORDER BY time DESC";
    PreparedStatement stmt = null;
    try {
      stmt = Main.con.prepareStatement(query);
      stmt.setString(1, begin.toString());
      stmt.setString(2, end.atTime(23, 59, 59).toString());

    } catch (SQLException e) {
      e.printStackTrace();
    }
    return getAttendances(stmt);
  }

  public static List<Attendance> getAttendances(Integer id, LocalDate begin, LocalDate end) {
    String query =
        "SELECT * FROM attendances WHERE id = ? AND ? <= time AND time <= ? ORDER BY time DESC";
    PreparedStatement stmt = null;
    try {
      stmt = Main.con.prepareStatement(query);
      stmt.setInt(1, id);
      stmt.setString(2, begin.toString());
      stmt.setString(3, end.atTime(23, 59, 59).toString());

    } catch (SQLException e) {
      e.printStackTrace();
    }
    return getAttendances(stmt);
  }

  private static List<Attendance> getAttendances(PreparedStatement stmt) {
    List<Attendance> ret = new ArrayList<Attendance>();
    if (stmt == null) {
      return ret;
    }
    try {
      ResultSet rs = stmt.executeQuery();
      while (rs.next()) {
        ret.add(new Attendance(rs));
      }
      stmt.close();
    } catch (SQLException e) {
    }
    return ret;
  }

  public static boolean insert(Integer id, Boolean state, LocalDateTime time) {
    String query = "INSERT INTO attendances VALUES (?, ?, ?, DEFAULT, DEFAULT)";
    try (PreparedStatement stmt = Main.con.prepareStatement(query)) {
      stmt.setInt(1, id);
      stmt.setBoolean(2, state);
      stmt.setString(3, time.truncatedTo(ChronoUnit.SECONDS).toString());
      return stmt.executeUpdate() > 0;
    } catch (SQLException e) {
      e.printStackTrace();
      return false;
    }
  }

  public static boolean update(Attendance a, LocalDateTime newTime) {
    String query = "UPDATE attendances SET time = ? WHERE id = ? AND state = ? AND time = ?";
    try (PreparedStatement stmt = Main.con.prepareStatement(query)) {
      stmt.setString(1, newTime.truncatedTo(ChronoUnit.SECONDS).toString());
      stmt.setInt(2, a.getId());
      stmt.setBoolean(3, a.getState());
      stmt.setString(4, a.getTime().toString());
      return stmt.executeUpdate() > 0;
    } catch (SQLException e) {
      e.printStackTrace();
      return false;
    }
  }

  public static Attendance getNewestBefore(Attendance a) {
    String query = "SELECT * FROM attendances WHERE id = ? AND time < ? ORDER BY time DESC LIMIT 1";
    PreparedStatement stmt = null;
    try {
      stmt = Main.con.prepareStatement(query);
      stmt.setInt(1, a.getId());
      stmt.setString(2, a.getTime().toString());
    } catch (SQLException e) {
      return null;
    }
    List<Attendance> as = getAttendances(stmt);
    if (as.isEmpty()) {
      return null;
    }
    return as.get(0);
  }

  public static Attendance getOldestAfter(Attendance a) {
    String query = "SELECT * FROM attendances WHERE id = ? AND time > ? ORDER BY time ASC LIMIT 1";
    PreparedStatement stmt = null;
    try {
      stmt = Main.con.prepareStatement(query);
      stmt.setInt(1, a.getId());
      stmt.setString(2, a.getTime().toString());
    } catch (SQLException e) {
      return null;
    }
    List<Attendance> as = getAttendances(stmt);
    if (as.isEmpty()) {
      return null;
    }
    return as.get(0);
  }

  public static String nextAction(boolean currentState) {
    return currentState ? "退社" : "出社";
  }

  public static void insertDummy(Integer id) {
    Random rng = new Random(id);
    LocalDate end = LocalDate.now();
    for (LocalDate begin = end.minusMonths(3); begin.compareTo(end) < 0; begin =
        begin.plusDays(1)) {
      int arriveMin = rng.nextInt(31);
      int arriveSec = rng.nextInt(60);
      insert(id, true, begin.atTime(arriveMin == 0 ? 9 : 8,
          arriveMin != 0 ? 60 - arriveMin : arriveMin, arriveSec));
      int leaveMin = rng.nextInt(31);
      int leaveSec = rng.nextInt(60);
      insert(id, false, begin.atTime(18, leaveMin, leaveSec));
    }
  }
}
