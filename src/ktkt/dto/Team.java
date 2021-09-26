package ktkt.dto;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import ktkt.Printable;

public class Team implements Printable {
  private Integer teamId;
  private String teamName;
  private Boolean isValid;
  private LocalDateTime createdAt;
  private LocalDateTime updatedAt;

  public Team(Integer teamId, String teamName, Boolean isValid, LocalDateTime createdAt,
      LocalDateTime updatedAt) {
    this.teamId = teamId;
    this.teamName = teamName;
    this.isValid = isValid;
    this.createdAt = createdAt;
    this.updatedAt = updatedAt;
  }

  public Team(ResultSet rs) throws SQLException {
    this(rs.getInt("team_id"), rs.getString("team_name"), rs.getBoolean("is_valid"),
        rs.getTimestamp("created_at").toLocalDateTime(),
        rs.getTimestamp("updated_at").toLocalDateTime());
  }

  public Integer getTeamId() {
    return teamId;
  }

  public String getTeamName() {
    return teamName;
  }

  public Boolean isValid() {
    return isValid;
  }

  public LocalDateTime getCreatedAt() {
    return createdAt;
  }

  public LocalDateTime getUpdatedAt() {
    return updatedAt;
  }

  public String getDetail() {
    return String.format("[= %d. %s%s =]", teamId, teamName, isValid ? "" : " (無効なチーム)");
  }

  public void print() {
    System.out.println(getDetail());
  }
}
