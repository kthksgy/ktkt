package ktkt.dto;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import ktkt.Printable;
import ktkt.U;

public class Employee implements Printable {
  private Integer id;
  private String password;
  private byte[] salt;
  private String lastName;
  private String firstName;
  private LocalDate joinedAt;
  private Boolean isValid;
  private Boolean isAdmin;
  private LocalDateTime createdAt;
  private LocalDateTime updatedAt;

  public Employee(Integer id, String password, String salt, String lastName, String firstName,
      LocalDate joinedAt, Boolean isValid, Boolean isAdmin, LocalDateTime createdAt,
      LocalDateTime updatedAt) {
    this.id = id;
    this.password = password;
    this.salt = U.toBytes(salt);
    this.firstName = firstName;
    this.lastName = lastName;
    this.joinedAt = joinedAt;
    this.isValid = isValid;
    this.isAdmin = isAdmin;
    this.createdAt = createdAt;
    this.updatedAt = updatedAt;
  }

  public Employee(ResultSet rs) throws SQLException {
    this(rs.getInt("id"), rs.getString("password"), rs.getString("salt"), rs.getString("last_name"),
        rs.getString("first_name"), rs.getDate("joined_at").toLocalDate(),
        rs.getBoolean("is_valid"), rs.getBoolean("is_admin"),
        rs.getTimestamp("created_at").toLocalDateTime(),
        rs.getTimestamp("updated_at").toLocalDateTime());
  }

  public Integer getId() {
    return id;
  }

  public String getPassword() {
    return password;
  }

  public byte[] getSalt() {
    return salt;
  }

  public String getLastName() {
    return lastName;
  }

  public String getFirstName() {
    return firstName;
  }

  public LocalDate getJoinedAt() {
    return joinedAt;
  }

  public boolean isValid() {
    return isValid;
  }

  public boolean isAdmin() {
    return isAdmin;
  }

  public LocalDateTime getCreatedAt() {
    return createdAt;
  }

  public LocalDateTime getUpdatedAt() {
    return updatedAt;
  }

  public String hash(String plainPassword) {
    return U.secureHash(plainPassword, salt);
  }

  public boolean authenticate(String plainPassword) {
    return password.equals(hash(plainPassword));
  }

  public String getDetail() {
    return String.format("<= 社員番号: %d, 氏名: %s %s, 入社日: %s%s =>", id, lastName, firstName, joinedAt,
        isValid ? "" : " (退職済み)");
  }

  public void print() {
    System.out.println(getDetail());
  }
}
