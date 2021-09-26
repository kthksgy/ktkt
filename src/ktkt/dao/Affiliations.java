package ktkt.dao;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import ktkt.Main;
import ktkt.dto.Employee;
import ktkt.dto.Team;

public class Affiliations {
  public static void createTable() {
    try (Statement statement = Main.con.createStatement()) {
      statement.execute("CREATE TABLE IF NOT EXISTS affiliations (\n"
          + "member       INT NOT NULL,\n" + "team         INT NOT NULL,\n"
          + "FOREIGN KEY  fk_member(member) REFERENCES employees(id),\n"
          + "FOREIGN KEY  fk_team(team) REFERENCES teams(team_id),\n"
          + "PRIMARY KEY  (member, team)\n" + ")");
    } catch (SQLException e) {
      System.out.println("所属テーブルの作成に失敗しました。");
      throw new RuntimeException(e);
    }
  }

  public static void dropTable() {
    try (Statement statement = Main.con.createStatement()) {
      statement.execute("DROP TABLE IF EXISTS affiliations");
    } catch (SQLException e) {
      System.out.println("所属テーブルの削除に失敗しました。");
      throw new RuntimeException(e);
    }
  }

  public static boolean insert(Employee employee, Team team) {
    return insert(employee.getId(), team.getTeamId());
  }

  public static boolean insert(Integer id, Integer teamId) {
    String query = "INSERT INTO affiliations VALUES (?, ?)";
    try (PreparedStatement stmt = Main.con.prepareStatement(query)) {
      stmt.setInt(1, id);
      stmt.setInt(2, teamId);
      return stmt.executeUpdate() > 0;
    } catch (SQLException e) {
      return false;
    }
  }

  public static boolean delete(Employee employee, Team team) {
    return delete(employee.getId(), team.getTeamId());
  }

  public static boolean delete(Integer id, Integer teamId) {
    String query = "DELETE FROM affiliations WHERE member = ? AND team = ?";
    try (PreparedStatement stmt = Main.con.prepareStatement(query)) {
      stmt.setInt(1, id);
      stmt.setInt(2, teamId);
      return stmt.executeUpdate() > 0;
    } catch (SQLException e) {
      return false;
    }
  }
}
