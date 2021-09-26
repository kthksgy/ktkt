package ktkt;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class C {
  public static final String DEFAULT_DB_URL = "jdbc:mysql://localhost/ktktdb";
  public static final String DEFAULT_DB_USER = "ktkt";
  public static final String DEFAULT_DB_PASSWORD = "ktktpassword";

  public static final String DEFAULT_PROPERTIES_PATH = "./connection.properties";

  public static final String AFFILIATIONS_TABLE_NAME = "affiliations";
  public static final String ATTENDANCES_TABLE_NAME = "attendances";
  public static final String EMPLOYEES_TABLE_NAME = "employees";
  public static final String TEAMS_TABLE_NAME = "teams";
  public static final Set<String> TABLE_NAMES = new HashSet<>(Arrays.asList(AFFILIATIONS_TABLE_NAME,
      ATTENDANCES_TABLE_NAME, EMPLOYEES_TABLE_NAME, TEAMS_TABLE_NAME));

  public static final String ADMIN_ONLY_ERROR = "この機能は管理者のみ使用できます。";

  public static final int HASH_ITERATION_COUNT = 12121;
  public static final int HASH_KEY_LENGTH = 256;
}
