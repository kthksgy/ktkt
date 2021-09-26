package ktkt.dao;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import ktkt.Main;
import ktkt.U;
import ktkt.dto.Employee;
import ktkt.dto.Team;

public class Teams {
  public static void createTable() {
    try (Statement statement = Main.con.createStatement()) {
      statement.execute("CREATE TABLE IF NOT EXISTS teams (\n"
          + "team_id     INT PRIMARY KEY AUTO_INCREMENT,\n" + "team_name   VARCHAR(64) NOT NULL,\n"
          + "is_valid    BOOLEAN NOT NULL DEFAULT TRUE,\n"
          + "created_at  DATETIME DEFAULT CURRENT_TIMESTAMP,"
          + "updated_at  DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP\n" + ")");
    } catch (SQLException e) {
      System.out.println("チームテーブルの作成に失敗しました。");
      throw new RuntimeException(e);
    }
  }

  public static void dropTable() {
    try (Statement statement = Main.con.createStatement()) {
      statement.execute("DROP TABLE IF EXISTS teams");
    } catch (SQLException e) {
      System.out.println("チームテーブルの削除に失敗しました。");
      throw new RuntimeException(e);
    }
  }

  public static int insert(Integer teamId, String teamName) {
    String query = "INSERT INTO teams VALUES (?, ?, DEFAULT, DEFAULT, DEFAULT)";
    try (PreparedStatement stmt =
        Main.con.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {
      stmt.setInt(1, teamId);
      stmt.setString(2, teamName);
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
    Integer teamId = U.inputInt("チーム番号を入力してください。(0 = 自動設定)", 0, null);
    String teamName = U.inputString("チーム名を入力してください。", 1, 32);
    return insert(teamId, teamName);
  }

  private static List<Team> get(PreparedStatement stmt) {
    if (stmt == null) {
      return null;
    }
    try {
      List<Team> ret = new ArrayList<>();
      ResultSet rs = stmt.executeQuery();
      while (rs.next()) {
        ret.add(new Team(rs));
      }
      stmt.close();
      return ret;
    } catch (SQLException e) {
      return null;
    }
  }

  public static Team get(Integer teamId) {
    String query = "SELECT * FROM teams WHERE team_id = ?";
    try (PreparedStatement stmt = Main.con.prepareStatement(query)) {
      stmt.setInt(1, teamId);
      List<Team> ts = get(stmt);
      if (ts == null || ts.isEmpty()) {
        return null;
      }
      return ts.get(0);
    } catch (SQLException e) {
      return null;
    }
  }

  public static List<Team> getAll() {
    String query = "SELECT * FROM teams";
    try (PreparedStatement stmt = Main.con.prepareStatement(query)) {
      return get(stmt);
    } catch (SQLException e) {
      return null;
    }
  }

  public static List<Team> search(String teamName) {
    String query = "SELECT * FROM teams WHERE team_name LIKE ?";
    try (PreparedStatement stmt = Main.con.prepareStatement(query)) {
      stmt.setString(1, teamName != null ? "%" + teamName + "%" : "%");
      return get(stmt);
    } catch (SQLException e) {
      return null;
    }
  }

  public static List<Team> get(Employee employee) {
    String query = "SELECT * FROM affiliations INNER JOIN teams ON team = team_id WHERE member = ?";
    try (PreparedStatement stmt = Main.con.prepareStatement(query)) {
      stmt.setInt(1, employee.getId());
      return get(stmt);
    } catch (SQLException e) {
      return null;
    }
  }

  public static boolean setName(Team team, String teamName) {
    String query = "UPDATE teams SET team_name = ? WHERE team_id = ?";
    try (PreparedStatement stmt = Main.con.prepareStatement(query)) {
      stmt.setString(1, teamName == null || teamName.isEmpty() ? team.getTeamName() : teamName);
      stmt.setInt(2, team.getTeamId());
      int rs = stmt.executeUpdate();
      return rs > 0;
    } catch (SQLException e) {
      return false;
    }
  }

  public static boolean setValidity(Team team, Boolean isValid) {
    return setValidity(team.getTeamId(), isValid);
  }

  public static boolean setValidity(Integer teamId, Boolean isValid) {
    String query = "UPDATE teams SET is_valid = ? WHERE team_id = ?";
    try (PreparedStatement stmt = Main.con.prepareStatement(query)) {
      stmt.setBoolean(1, isValid);
      stmt.setInt(2, teamId);
      int rs = stmt.executeUpdate();
      return rs > 0;
    } catch (SQLException e) {
      return false;
    }
  }

  public static void insertDummy() {
    insert(0, "チーム1");
    insert(0, "案件A");
    insert(0, "案件B");
    insert(0, "仲良しグループ");
  }
}
