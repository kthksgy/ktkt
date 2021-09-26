package ktkt;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Properties;
import java.util.Scanner;
import ktkt.dao.Affiliations;
import ktkt.dao.Attendances;
import ktkt.dao.Employees;
import ktkt.dao.Teams;
import ktkt.dto.Attendance;
import ktkt.dto.Employee;
import ktkt.dto.Team;

public class Main {
  public static Scanner sc = new Scanner(System.in);
  public static Connection con;
  public static Employee me = null;
  public static Summary mySummary = null;

  public static void main(String[] args) {
    System.out.println("# =========================== #");
    System.out.println("#                             #");
    System.out.println("#   勤怠くんテキスト (KTKT)   #");
    System.out.println("#          ver 1.0            #");
    System.out.println("#                             #");
    System.out.println("# =========================== #");

    try {
      Class.forName("com.mysql.cj.jdbc.Driver");
    } catch (ClassNotFoundException e) {
      System.out.println("データベース接続ドライバが見つかりませんでした。");
      throw new RuntimeException(e);
    }

    String connectionUrl = C.DEFAULT_DB_URL;
    String connectionUser = C.DEFAULT_DB_USER;
    String connectionPassword = C.DEFAULT_DB_PASSWORD;
    try {
      Properties properties = new Properties();
      properties.load(
          Files.newBufferedReader(Paths.get(C.DEFAULT_PROPERTIES_PATH), StandardCharsets.UTF_8));
      String tmpUrl = null, tmpUser = null, tmpPassword = null;
      tmpUrl = properties.getProperty("url");
      tmpUser = properties.getProperty("user");
      tmpPassword = properties.getProperty("password");
      if (tmpUrl != null && tmpUser != null && tmpPassword != null) {
        connectionUrl = tmpUrl;
        connectionUser = tmpUser;
        connectionPassword = tmpPassword;
      } else {
        System.out.println("DB接続設定の項目が一部見つからなかったのでデフォルト設定を使用します。");
      }
    } catch (IOException e) {
      System.out.println("DB接続設定(connection.properties)が見つからないためデフォルト設定を使用します。");
    }

    try {
      con = DriverManager.getConnection(connectionUrl, connectionUser, connectionPassword);
    } catch (SQLException e) {
      System.out.println("データベースへの接続に失敗しました。");
      throw new RuntimeException(e);
    }

    List<String> tableNames = U.getTableNames();
    if (tableNames == null) {
      System.out.println("データベースの状態確認に失敗しました。");
      throw new RuntimeException();
    }
    if (tableNames.size() == 0) {
      System.out.println("テーブルが存在しないため新規作成します。");
      Employees.createTable();
      Teams.createTable();
      Attendances.createTable();
      Affiliations.createTable();
      System.out.println("テーブルの作成を行いました。");
      tableNames = U.getTableNames();
    }
    if (tableNames.size() < C.TABLE_NAMES.size() || !tableNames.containsAll(C.TABLE_NAMES)) {
      System.out.println("存在するテーブル情報が要件を満たしていません。");
      System.out.println("データベースをクリーンしてからもう一度お試しください。");
      throw new RuntimeException();
    }

    Scanner sc = new Scanner(System.in);
    LOOP: while (true) {
      if (me == null) {
        Integer id = U.inputInt("社員番号を入力してください。", 1, null);
        String password = U.inputPassword("パスワードを入力してください。");
        Employee e = Employees.get(id);
        if (e != null && e.authenticate(password)) {
          me = e;
          System.out.println("ログインに成功しました。");
          e.print();
          mySummary = new Summary(me, 25);
        } else {
          System.out.println("ログインに失敗しました。");
          continue;
        }
      }

      switch (U.inputInt("選択肢を選んでください。(0 = 選択肢一覧)", 0, null)) {

        /*
         * ===================== 0 ～ 9: 基本機能 =====================
         */

        case 0: {
          System.out.println("---- メニュー ----\n" + "  0. 選択肢一覧を表示\n"
              + String.format("  1. %s\n", Attendances.nextAction(mySummary.currentState()))
              + "  2. 勤怠情報表示\n" + "  3. 勤怠情報修正\n" + "  4. 社員情報表示\n" + "  5. チーム情報表示\n"
              + "  6. パスワードの変更\n" + "  7. 勤怠情報表示(チーム)\n" + "  8. ログアウト\n" + "  9. 終了");
          break;
        }

        case 1: {
          boolean nextState = !mySummary.currentState();
          String action = Attendances.nextAction(mySummary.currentState());
          LocalDateTime now = LocalDateTime.now();
          if (U.inputYesOrNo(String.format("%sしますか？ (現在時刻: %s)", action, U.format(now)))
              && (mySummary.validate(now, nextState) || U
                  .inputYesOrNo(String.format("現在時刻は%sです。本当に%sしますか？", U.format(now), action)))) {

            if (Attendances.insert(me.getId(), !mySummary.currentState(), now)) {
              System.out.printf("%sしました。\n", action);
            } else {
              System.out.printf("%sに失敗しました。\n", action);
            }
            mySummary = new Summary(me, 25);
          } else {
            System.out.println("打刻を中止しました。");
          }
          break;
        }
        case 2: {
          Integer id = U.inputInt("社員情報を閲覧する社員の社員番号を入力してください。(0 = 自分)", 0, null);
          if (id == 0) {
            id = me.getId();
          }
          Employee e = Employees.get(id);
          if (e == null) {
            System.out.println("指定した社員番号の社員が見つかりませんでした。");
            break;
          }
          LocalDate beginDate = U.inputDate("閲覧する日付(開始)を入力してください。[yyyy-MM-dd]", null, null);
          LocalDate endDate = U.inputDate("閲覧する日付(終了)を入力してください。[yyyy-MM-dd]", null, null);
          Summary s = new Summary(e, beginDate, endDate);
          if (U.inputYesOrNo("グラフィカルな表示を行いますか？")) {
            s.visualize();
          } else {
            s.print();
          }
          break;
        }
        case 3: {
          Employee e = null;
          if (me.isAdmin()) {
            Integer id = U.inputInt("修正する社員の社員番号を入力してください。(0 = 自分)", 0, null);
            if (id == 0) {
              id = me.getId();
            }
            e = Employees.get(id);
            if (e == null) {
              System.out.println("指定した社員番号の社員が見つかりませんでした。");
              break;
            }
          } else {
            e = me;
          }
          LocalDate date = U.inputDate("修正する勤怠情報の含まれる日付を入力してください。[yyyy-MM-dd]", null, null);
          while (true) {
            Summary s = new Summary(e, date, date);
            s.print();
            Integer idx = U.inputInt("修正する勤怠番号を指定してください。(0 = 終了)", 0, s.size());
            if (idx == 0) {
              break;
            }
            Attendance target = s.get(idx);
            Attendance before = Attendances.getNewestBefore(target);
            Attendance after = Attendances.getOldestAfter(target);
            LocalDateTime min = before == null ? null : before.getTime().plusSeconds(1);
            LocalDateTime max =
                after == null ? LocalDateTime.now() : after.getTime().minusSeconds(1);
            LocalDateTime newDateTime = U.inputDateTime("修正後の日時を指定してください。", min, max);
            if (U.inputYesOrNo(String.format("この内容で修正しますか？ %s → %s", U.format(s.get(idx).getTime()),
                U.format(newDateTime)))) {
              if (Attendances.update(target, newDateTime)) {
                System.out.println("修正に成功しました。");
              } else {
                System.out.println("修正に失敗しました。");
              }
            } else {
              System.out.println("修正を中止します。");
            }

          }
          break;
        }
        case 4: {
          Integer mode =
              U.inputInt(" - 1. 全件表示\n - 2. 苗字／名前で検索\n - 3. 入社日で検索\n - 4. チーム番号で検索", 1, 4);
          switch (mode) {
            case 1: {
              U.print(Employees.getAll());
              break;
            }
            case 2: {
              String lastName = U.inputString("苗字を入力してください。(無入力可)", null, null);
              String firstName = U.inputString("名前を入力してください。(無入力可)", null, null);
              U.print(Employees.search(lastName, firstName));
              break;
            }
            case 3: {
              LocalDate begin = U.inputDate("入社日の検索範囲(始点)を入力してください。", null, null);
              LocalDate end = U.inputDate("入社日の検索範囲(終点)を入力してください。", null, null);
              U.print(Employees.search(begin, end));
              break;
            }
            case 4: {
              Integer teamId = U.inputInt("チーム番号を入力してください。", 1, null);
              U.print(Employees.getMembers(teamId));
              break;
            }
            default:
              break;
          }
          break;
        }
        case 5: {
          Integer mode = U.inputInt(" - 1. 全件表示\n - 2. チーム名で検索\n - 3. 自分の所属チーム", 1, 3);
          switch (mode) {
            case 1: {
              U.print(Teams.getAll());
              break;
            }
            case 2: {
              String teamName = U.inputString("チーム名を入力してください。(無入力可)", null, null);
              U.print(Teams.search(teamName));
              break;
            }
            case 3: {
              U.print(Teams.get(me));
              break;
            }
            default:
              break;
          }
          break;
        }
        case 6: {
          if (U.inputYesOrNo("パスワードを変更しますか？")) {
            String currentPassword = U.inputPassword("現在のパスワードを入力してください。");
            if (me.authenticate(currentPassword)) {
              String newPassword = U.inputPassword("変更後のパスワードを入力してください。");
              Employees.setPassword(me, newPassword);
              System.out.println("パスワードを変更しました。");
            } else {
              System.out.println("入力したパスワードが違います。");
            }
          } else {
            System.out.println("パスワードの変更を中断しました。");
          }
          break;
        }
        case 7: {
          Integer teamId = U.inputInt("チーム番号を入力してください。", 1, null);
          LocalDate beginDate = U.inputDate("閲覧する日付(開始)を入力してください。[yyyy-MM-dd]", null, null);
          LocalDate endDate = U.inputDate("閲覧する日付(終了)を入力してください。[yyyy-MM-dd]", null, null);
          boolean doVisualize = U.inputYesOrNo("グラフィカルな表示を行いますか？");
          List<Employee> es = Employees.getMembers(teamId);
          for (Employee e : es) {
            Summary s = new Summary(e, beginDate, endDate);
            if (doVisualize) {
              s.visualize();
            } else {
              s.print();
            }
          }
          break;
        }
        case 8: {
          if (U.inputYesOrNo("本当にログアウトしますか？")) {
            System.out.println("ログアウトしました。");
            me = null;
            mySummary = null;
          } else {
            System.out.println("ログアウトを中止しました。");
          }
          break;
        }
        case 9: {
          if (U.inputYesOrNo("本当にシステムを終了しますか？")) {
            System.out.println("システムを終了します。");
            break LOOP;
          } else {
            System.out.println("システムの終了を中止しました。");
          }
          break;
        }

        /*
         * ============================ 100 ～ 199: 管理機能(社員) ============================
         */

        case 100: {
          if (!me.isAdmin()) {
            System.out.println(C.ADMIN_ONLY_ERROR);
            break;
          }
          System.out.println("---- 管理者メニュー(社員関連) ----\n" + "100. 選択肢一覧を表示\n" + "101. 新規登録\n"
              + "102. パスワード変更\n" + "103. 氏名変更\n" + "104. 管理権限変更\n" + "105. 有効化／無効化\n");
          break;
        }

        // 社員の新規登録
        case 101: {
          if (!me.isAdmin()) {
            System.out.println(C.ADMIN_ONLY_ERROR);
            break;
          }
          System.out.println("社員登録を開始します。");
          int newId = Employees.interactiveCreate();
          if (newId > 0) {
            System.out.println("社員登録が完了しました。");
            Employee newEmployee = Employees.get(newId);
            newEmployee.print();
          } else {
            System.out.println("社員登録に失敗しました。");
          }
          break;
        }

        // 社員のパスワード変更
        case 102: {
          if (!me.isAdmin()) {
            System.out.println(C.ADMIN_ONLY_ERROR);
            break;
          }
          Integer id = U.inputInt("パスワードを変更する社員の社員番号を指定してください。(0 = キャンセル)", 0, null);
          if (id == 0) {
            System.out.println("変更を中止します。");
            break;
          }
          Employee e = Employees.get(id);
          if (e == null) {
            System.out.println("指定した社員番号の社員が見つかりませんでした。");
            break;
          }
          String newPassword = U.inputPassword("変更後のパスワードを入力してください。");
          if (Employees.setPassword(e, newPassword)) {
            System.out.println("パスワードを変更しました。");
          } else {
            System.out.println("パスワードの変更に失敗しました。");
          }
          break;
        }

        // 社員の氏名変更
        case 103: {
          if (!me.isAdmin()) {
            System.out.println(C.ADMIN_ONLY_ERROR);
            break;
          }
          Integer id = U.inputInt("名前を変更する社員の社員番号を指定してください。(0 = キャンセル)", 0, null);
          if (id == 0) {
            System.out.println("変更を中止します。");
            break;
          }
          Employee e = Employees.get(id);
          if (e == null) {
            System.out.println("指定した社員番号の社員が見つかりませんでした。");
            break;
          }
          e.print();
          String lastName = U.inputString("苗字を入力してください。(無入力で変更無し)", 0, 32);
          String firstName = U.inputString("名前を入力してください。(無入力で変更無し)", 0, 32);
          if (Employees.setName(e, lastName, firstName)) {
            System.out.println("名前の変更に成功しました。");
          } else {
            System.out.println("名前の変更に失敗しました。");
          }
          break;
        }

        // 社員の管理権限変更
        case 104: {
          if (!me.isAdmin()) {
            System.out.println(C.ADMIN_ONLY_ERROR);
            break;
          }
          Integer id = U.inputInt("権限を変更する社員の社員番号を指定してください。(0 = キャンセル)", 0, null);
          if (id == 0) {
            System.out.println("変更を中止します。");
            break;
          }
          Employee e = Employees.get(id);
          if (e == null) {
            System.out.println("指定した社員番号の社員が見つかりませんでした。");
            break;
          }
          System.out.println(" - 1. 管理権限を付与する。\n - 2. 管理権限を剥奪する。");
          Integer choice = U.inputInt("選択してください。", 1, 2);
          Boolean isAdmin = null;
          switch (choice) {
            case 1:
              isAdmin = true;
              break;
            case 2:
              isAdmin = false;
            default:
              break;
          }
          if (isAdmin != null && Employees.setPermission(e, isAdmin)) {
            System.out.println("管理権限の変更に成功しました。");
          } else {
            System.out.println("管理権限の変更に失敗しました。");
          }
          break;
        }

        // 社員の有効化／無効化
        case 105: {
          if (!me.isAdmin()) {
            System.out.println(C.ADMIN_ONLY_ERROR);
            break;
          }
          Integer id = U.inputInt("有効化／無効化する社員の社員番号を指定してください。(0 = キャンセル)", 0, null);
          if (id == 0) {
            System.out.println("変更を中止します。");
            break;
          }
          Employee e = Employees.get(id);
          if (e == null) {
            System.out.println("指定した社員番号の社員が見つかりませんでした。");
            break;
          }
          System.out.println(" - 1. 有効化する。\n - 2. 無効化する。");
          Integer choice = U.inputInt("選択してください。", 1, 2);
          Boolean isValid = null;
          switch (choice) {
            case 1:
              isValid = true;
              break;
            case 2:
              isValid = false;
              break;
            default:
              break;
          }
          if (isValid != null && Employees.setValidity(e, isValid)) {
            System.out.println("有効化／無効化に成功しました。");
          } else {
            System.out.println("有効化／無効化に失敗しました。");
          }
          break;
        }

        /*
         * ====================== 200 ～ 299: チーム ======================
         */

        case 200: {
          if (!me.isAdmin()) {
            System.out.println(C.ADMIN_ONLY_ERROR);
            break;
          }
          System.out.println("---- 管理者メニュー(チーム関連) ----\n" + "200. 選択肢一覧を表示\n" + "201. 新規登録\n"
              + "202. 社員のチーム所属情報変更\n" + "203. チーム名変更\n" + "204. 有効化／無効化\n");
          break;
        }

        case 201: {
          if (!me.isAdmin()) {
            System.out.println(C.ADMIN_ONLY_ERROR);
            break;
          }
          System.out.println("チーム登録を開始します。");
          int newId = Teams.interactiveCreate();
          if (newId > 0) {
            System.out.println("チーム登録が完了しました。");
            Team newTeam = Teams.get(newId);
            newTeam.print();
          } else {
            System.out.println("チーム登録に失敗しました。");
          }
          break;
        }
        case 202: {
          if (!me.isAdmin()) {
            System.out.println(C.ADMIN_ONLY_ERROR);
            break;
          }
          System.out.println("社員とチームの所属情報を編集します。");
          Integer id = U.inputInt("社員番号を指定してください。(0 = キャンセル)", 0, null);
          if (id == 0) {
            System.out.println("変更を中止します。");
            break;
          }
          Employee employee = Employees.get(id);
          if (employee == null) {
            System.out.println("指定した社員番号の社員が見つかりませんでした。");
            break;
          }
          Integer teamId = U.inputInt("チーム番号を指定してください。(0 = キャンセル)", 0, null);
          if (teamId == 0) {
            System.out.println("変更を中止します。");
            break;
          }
          Team team = Teams.get(teamId);
          if (team == null) {
            System.out.println("指定したチームが見つかりませんでした。");
            break;
          }
          employee.print();
          team.print();
          System.out.println(" - 1. 社員をチームに加入させる\n - 2. 社員をチームから除外する\n - 3. キャンセル");
          Integer choice = U.inputInt("選択してください。", 1, 3);
          switch (choice) {
            case 1:
              if (Affiliations.insert(employee, team)) {
                System.out.println("社員をチームに加入させました。");
              } else {
                System.out.println("社員のチーム加入に失敗しました。");
              }
              break;
            case 2:
              if (Affiliations.delete(employee, team)) {
                System.out.println("社員をチームから除外しました。");
              } else {
                System.out.println("社員のチーム除外に失敗しました。");
              }
              break;
            case 3:
              System.out.println("キャンセルします。");
              break;
            default:
              break;
          }
          break;
        }

        case 203: {
          if (!me.isAdmin()) {
            System.out.println(C.ADMIN_ONLY_ERROR);
            break;
          }
          Integer id = U.inputInt("名前を変更するチーム番号を指定してください。(0 = キャンセル)", 0, null);
          if (id == 0) {
            System.out.println("変更を中止します。");
            break;
          }
          Team team = Teams.get(id);
          if (team == null) {
            System.out.println("指定したチームが見つかりませんでした。");
            break;
          }
          String teamName = U.inputString("チーム名を入力してください。", 1, 32);
          if (Teams.setName(team, teamName)) {
            System.out.println("名前の変更に成功しました。");
          } else {
            System.out.println("名前の変更に失敗しました。");
          }
          break;
        }

        case 204: {
          if (!me.isAdmin()) {
            System.out.println(C.ADMIN_ONLY_ERROR);
            break;
          }
          Integer id = U.inputInt("有効化／無効化するチーム番号を指定してください。(0 = キャンセル)", 0, null);
          if (id == 0) {
            System.out.println("変更を中止します。");
            break;
          }
          Team team = Teams.get(id);
          if (team == null) {
            System.out.println("指定したチームが見つかりませんでした。");
            break;
          }
          System.out.println(" - 1. 有効化する。\n - 2. 無効化する。");
          Integer choice = U.inputInt("選択してください。", 1, 2);
          Boolean isValid = null;
          switch (choice) {
            case 1:
              isValid = true;
              break;
            case 2:
              isValid = false;
            default:
              break;
          }
          if (isValid != null && Teams.setValidity(team, isValid)) {
            System.out.println("有効化／無効化に成功しました。");
          } else {
            System.out.println("有効化／無効化に失敗しました。");
          }
          break;
        }

        /*
         * ================================== 800 ～ 899: 初期化／ダミーデータ
         * ==================================
         */

        case 900: {
          if (!me.isAdmin()) {
            System.out.println(C.ADMIN_ONLY_ERROR);
            break;
          }
          System.out
              .println("---- 管理者メニュー(初期化／ダミーデータ関連) ----\n" + "900. 選択肢一覧を表示\n" + "901. テーブルの初期化\n"
                  + "902. ダミー社員の追加\n" + "903. ダミーチームの追加\n" + "904. 社員へのダミー勤怠情報の追加\n");
          break;
        }

        case 901: {
          if (!me.isAdmin()) {
            System.out.println(C.ADMIN_ONLY_ERROR);
            break;
          }
          if (U.inputYesOrNo("テーブルを初期化しますか？")) {
            /* テーブルの削除 */
            Affiliations.dropTable();
            Attendances.dropTable();
            Teams.dropTable();
            Employees.dropTable();

            /* テーブルの作成 */
            Employees.createTable();
            Teams.createTable();
            Attendances.createTable();
            Affiliations.createTable();
            System.out.println("テーブルを初期化しました。");
          } else {
            System.out.println("テーブルの初期化を中止しました。");
          }
          break;
        }
        case 902: {
          if (!me.isAdmin()) {
            System.out.println(C.ADMIN_ONLY_ERROR);
            break;
          }
          if (U.inputYesOrNo("ダミー社員を追加しますか？")) {
            Employees.insertDummy();
            System.out.println("ダミー社員を10人追加しました。初期パスワードは'password'です。");
          } else {
            System.out.println("ダミー社員の追加を中止しました。");
          }
          break;
        }
        case 903: {
          if (!me.isAdmin()) {
            System.out.println(C.ADMIN_ONLY_ERROR);
            break;
          }
          if (U.inputYesOrNo("ダミーチームを追加しますか？")) {
            Teams.insertDummy();
            System.out.println("ダミーチームを4チーム追加しました。");
          } else {
            System.out.println("ダミーチームの追加を中止しました。");
          }
          break;
        }
        case 904: {
          if (!me.isAdmin()) {
            System.out.println(C.ADMIN_ONLY_ERROR);
            break;
          }
          Integer id = U.inputInt("ダミー勤怠情報を挿入する社員番号を指定してください。(0 = キャンセル)", 0, null);
          if (id == 0) {
            System.out.println("変更を中止します。");
            break;
          }
          Employee e = Employees.get(id);
          if (e == null) {
            System.out.println("指定した社員番号の社員が見つかりませんでした。");
            break;
          }
          e.print();
          if (U.inputYesOrNo("本日までの3ヶ月分のダミー勤怠情報を追加しますか？")) {
            Attendances.insertDummy(e.getId());
            System.out.println("ダミー勤怠情報を追加しました。");
          } else {
            System.out.println("ダミー勤怠情報の追加を中止しました。");
          }
          break;
        }
        default:
          System.out.println("入力した選択肢は存在しません。");
          break;
      }

    }
    sc.close();
  }
}
