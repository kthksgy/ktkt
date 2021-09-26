package ktkt;

import java.io.Console;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

public class U {
  public static final DateTimeFormatter dateTimeFormatter =
      DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
  private static final char[] HEX_CHARACTERS = "0123456789ABCDEF".toCharArray();
  private static final Pattern PASSWORD_PATTERN = Pattern.compile("^[a-zA-Z0-9.?/-]{8,24}$");

  private U() {}

  public static String toHex(byte[] data) {
    StringBuilder r = new StringBuilder(data.length * 2);
    for (byte b : data) {
      r.append(HEX_CHARACTERS[(b >> 4) & 0xF]);
      r.append(HEX_CHARACTERS[(b & 0xF)]);
    }
    return r.toString();
  }

  public static byte[] toBytes(String hex) {
    byte[] bytes = new byte[hex.length() / 2];

    for (int i = 0; i < bytes.length; i++) {
      bytes[i] = (byte) (Character.digit(hex.charAt(i << 1), 16) << 4);
      bytes[i] += (byte) (Character.digit(hex.charAt((i << 1) + 1), 16));
    }
    return bytes;
  }

  public static String secureHash(String password, byte[] salt) {

    PBEKeySpec keySpec =
        new PBEKeySpec(password.toCharArray(), salt, C.HASH_ITERATION_COUNT, C.HASH_KEY_LENGTH);
    try {
      return toHex(SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256").generateSecret(keySpec)
          .getEncoded());
    } catch (NoSuchAlgorithmException e) {
      throw new RuntimeException(e);
    } catch (InvalidKeySpecException e) {
      throw new RuntimeException(e);
    }
  }

  public static String inputPassword(String prompt) {
    Console console = System.console();
    System.out.printf("%s [8～24文字アルファベット／数字／記号./?-]\n", prompt);
    String ret = null;
    while (true) {
      if (console == null) {
        ret = Main.sc.nextLine();
      } else {
        ret = new String(console.readPassword());
      }
      if (PASSWORD_PATTERN.matcher(ret).matches()) {
        return ret;
      } else {
        System.out.println("パスワードは8～24文字のアルファベット／数字／記号./?-で入力してください。");
      }

    }
  }

  public static LocalDate inputDate() {
    LocalDate ret = null;
    while (true) {
      try {
        ret = LocalDate.parse(Main.sc.nextLine(), DateTimeFormatter.ISO_LOCAL_DATE);
      } catch (DateTimeParseException e) {
        System.out.println("日付はyyyy-MM-ddの形式で入力してください。");
        continue;
      }
      break;
    }
    return ret;
  }

  public static LocalDate inputDate(String prompt, LocalDate min, LocalDate max) {
    LocalDate ret = null;
    System.out.println(prompt);
    while (true) {
      try {
        ret = LocalDate.parse(Main.sc.nextLine(), DateTimeFormatter.ISO_LOCAL_DATE);
        if (min != null && ret.compareTo(min) < 0) {
          System.out.printf("%s以上で入力してください。\n", min);
          continue;
        }
        if (max != null && ret.compareTo(max) > 0) {
          System.out.printf("%s以下で入力してください。\n", max);
          continue;
        }
      } catch (DateTimeParseException e) {
        System.out.println("日付はyyyy-MM-ddの形式で入力してください。");
        continue;
      }
      break;
    }
    return ret;
  }

  public static LocalDateTime inputDateTime(String prompt, LocalDateTime min, LocalDateTime max) {
    LocalDateTime ret = null;
    System.out.println(prompt);
    while (true) {
      try {
        ret = LocalDateTime.parse(Main.sc.nextLine(), dateTimeFormatter);
        if (min != null && ret.compareTo(min) < 0) {
          System.out.printf("%s以上で入力してください。\n", format(min));
          continue;
        }
        if (max != null && ret.compareTo(max) > 0) {
          System.out.printf("%s以下で入力してください。\n", format(max));
          continue;
        }
      } catch (DateTimeParseException e) {
        System.out.println("日時はyyyy-MM-dd HH:mm:ssの形式で入力してください。");
        continue;
      }
      break;
    }
    return ret;
  }

  public static Integer inputInt(String prompt, Integer min, Integer max) {
    Integer ret = null;
    System.out.println(prompt);
    while (true) {
      try {
        ret = Integer.parseInt(Main.sc.nextLine());
        if (min != null && ret < min) {
          System.out.printf("%d以上で入力してください。\n", min);
          continue;
        }
        if (max != null && ret > max) {
          System.out.printf("%d以下で入力してください。\n", max);
          continue;
        }
      } catch (NumberFormatException e) {
        System.out.println("整数で入力してください。");
        continue;
      }
      break;
    }
    return ret;
  }

  public static String inputString(String promptText, Integer min, Integer max) {
    String ret = null;
    System.out.println(promptText);
    while (true) {
      ret = Main.sc.nextLine();
      if (min != null && ret.length() < min) {
        System.out.printf("最低%d文字で入力してください。\n", min);
        continue;
      }
      if (max != null && ret.length() > max) {
        System.out.printf("最大%d文字で入力してください。\n", max);
        continue;
      }
      break;
    }
    return ret;
  }

  public static boolean inputYesOrNo(String prompt) {
    System.out.println(prompt + " [y/n]");
    while (true) {
      switch (Main.sc.nextLine().toLowerCase()) {
        case "y":
        case "yes":
        case "はい":
        case "イエス":
          return true;
        case "n":
        case "no":
        case "いいえ":
        case "ノー":
          return false;
        default:
          System.out.println("yかnで入力してください。");
          break;
      }
    }
  }

  public static List<String> getTableNames() {
    try (PreparedStatement stmt = Main.con.prepareStatement("SHOW TABLES")) {
      ResultSet rs = stmt.executeQuery();
      List<String> tableNames = new ArrayList<>();
      while (rs.next()) {
        tableNames.add(rs.getString(1));
      }
      return tableNames;
    } catch (SQLException e) {
      e.printStackTrace();
      return null;
    }
  }

  public static String format(LocalDateTime ldt) {
    return ldt.format(dateTimeFormatter);
  }

  public static <T extends Printable> void print(List<T> printables) {
    if (printables == null) {
      return;
    }
    for (Printable p : printables) {
      p.print();
    }
  }

}
