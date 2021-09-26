package ktkt.dto;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import ktkt.Printable;
import ktkt.U;

public class Attendance implements Comparable<Attendance>, Printable {
  private Integer id;
  private Boolean state;
  private LocalDateTime time;
  private LocalDateTime createdAt;
  private LocalDateTime updatedAt;

  public Attendance(Integer id, Boolean state, LocalDateTime time, LocalDateTime createdAt,
      LocalDateTime updatedAt) {
    this.id = id;
    this.state = state;
    this.time = time;
    this.createdAt = createdAt;
    this.updatedAt = updatedAt;
  }

  public Attendance(ResultSet rs) throws SQLException {
    this(rs.getInt("id"), rs.getBoolean("state"), rs.getTimestamp("time").toLocalDateTime(),
        rs.getTimestamp("created_at").toLocalDateTime(),
        rs.getTimestamp("updated_at").toLocalDateTime());
  }

  public Integer getId() {
    return id;
  }

  public Boolean getState() {
    return state;
  }

  public LocalDateTime getTime() {
    return time;
  }

  public LocalDateTime getCreatedAt() {
    return createdAt;
  }

  public LocalDateTime getUpdatedAt() {
    return updatedAt;
  }

  @Override
  public int compareTo(Attendance o) {
    return time.compareTo(o.time);
  }

  public String getDetail() {
    return String.format("[%d:%s] %s", id, state ? "出社" : "退社", U.format(time));
  }

  @Override
  public void print() {
    System.out.println(getDetail());
  }
}
