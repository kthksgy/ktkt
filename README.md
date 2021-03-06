# 勤怠くんテキスト (KTKT)
勤怠くんテキスト (KTKT)はCUIの社員情報／勤怠管理システムです。

- マルチユーザー
- ログイン／ログアウト機能
- 出社打刻／退社打刻
- 誤入力防止

## ビルド
[JDBC Driver for MySQL (Connector/J)](https://dev.mysql.com/downloads/connector/j/)が必要です。
開発時は以下のバージョンを使用しました。
- `mysql-connector-java-8.0.24.jar`

## 実行
### データベースの用意
システムの利用開始に際して、MySQLで専用のユーザーと新規のデータベースを作成してください。

```
CREATE DATABASE ktktdb;
CREATE USER 'ktkt'@'localhost' IDENTIFIED BY 'ktktpassword';
GRANT ALL ON ktktdb.* TO 'ktkt'@'localhost';
```

#### システムのDB接続情報の設定
`ktkt.jar` と同じ階層に `connection.properties` というファイルを作成すると、このファイルから自動的に内容を読み込みます。

```
directory
|- ktkt.jar
|- connection.properties  # ここに配置
```

ファイルの内容は以下を参考にしてください。また、ファイルが見つからない場合は以下の接続情報で自動接続を試みます。

```
url=jdbc:mysql://localhost/ktktdb
user=ktkt
password=ktktpassword
```

#### Eclipseでの開発時
Eclipseでの開発時はプロジェクトルートにファイルを配置してください。

```
project
|- ...
|- bin
|- src
|- connection.properties  # ここに配置
|- ...
```

### 起動
以下のコマンドでJARファイルからシステムを起動してください。

```
$ java -jar ktkt.jar
```

### 管理者ログイン
初期状態では社員番号 `1` にシステム管理用の社員が作成されています。初期パスワードは `password` です。

```
# =========================== #
#                             #
#   勤怠くんテキスト (KTKT)   #
#          ver 1.0            #
#                             #
# =========================== #
社員番号を入力してください。
1
パスワードを入力してください。 [8～24文字アルファベット／数字／記号./?-]
(passwordと入力)
```

## 操作方法
ログイン後はメニューから希望する操作を番号で入力して、以降は画面に従ってください。

### 操作一覧
この一覧は下一桁が0のメニュー `XX0` でシステム上からも参照可能です。

#### 基本操作
- `0` : 操作一覧表示
- `1` : 打刻(出社／退社は自動判別)
- `2` : 勤怠情報表示
- `3` : 勤怠情報修正(管理者は他社員の情報を修正可能)
- `4` : 社員情報表示
- `5` : チーム情報表示
- `6` : パスワードの変更
- `7` : 勤怠情報表示(チーム単位)
- `8` : ログアウト
- `9` : システム終了

#### (管理者用)社員関連操作
- `100` : 操作一覧表示
- `101` : 社員の新規登録
- `102` : パスワード変更
- `103` : 氏名変更
- `104` : 管理権限変更
- `105` : 社員の有効化／無効化

#### (管理者用)チーム関連操作
- `200` : 操作一覧表示
- `201` : チームの新規登録
- `202` : 社員のチーム所属情報変更
- `203` : チーム名変更
- `204` : チームの有効化／無効化

#### ダミーデータ
本システムはテスト用のダミーデータを追加する機能があり、メニューの `900` 番台にマッピングしています。

- `900` : メニュー表示
初期化やダミーデータに関連するメニューを表示します。

- `901` : テーブルの初期化
システムで管理している全テーブルを初期化(削除と再生成)します。
データは復旧不可能なので、注意してください。

- `902` : ダミーユーザー(社員)の追加
ハードコードされた社員情報を社員テーブルに挿入します。
社員番号は自動設定されるので、社員情報表示( メニュー `4` )から参照してください。

- `903` : ダミーチームの追加
ハードコードされたチーム情報をチームテーブルに挿入します。
チーム番号は自動設定されるので、チーム情報表示( メニュー `5` )から参照してください。
社員をチームに加入させるにはメニュー `202` より操作を行ってください。

- `904` : ダミー勤怠情報の追加
指定した社員番号の社員に現在時刻で昨日までの3ヶ月分の勤怠情報を挿入します。
