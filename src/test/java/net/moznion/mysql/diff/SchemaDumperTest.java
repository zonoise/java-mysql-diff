package net.moznion.mysql.diff;

import static org.junit.Assert.assertTrue;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.Charset;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.UUID;

import org.junit.Test;
import org.junit.experimental.runners.Enclosed;
import org.junit.runner.RunWith;

@RunWith(Enclosed.class)
public class SchemaDumperTest {
  private static final String SQL_FOR_TEST = "CREATE TABLE `sample` (\n" +
      "  `id` int(10) NOT NULL AUTO_INCREMENT,\n" +
      "  PRIMARY KEY (`id`)\n" +
      ") ENGINE=InnoDB DEFAULT CHARSET=utf8;\n";

  public static class ForConstructors {
    @Test
    public void shouldInstantiateAsDefault() throws SQLException, IOException, InterruptedException {
      SchemaDumper schemaDumper = new SchemaDumper();
      schemaDumper.dump(SQL_FOR_TEST);
      assertTrue(true);
    }

    @Test
    public void shouldInstantiateByMysqldumpPath() throws SQLException, IOException,
        InterruptedException {
      SchemaDumper schemaDumper = new SchemaDumper("mysqldump");
      schemaDumper.dump(SQL_FOR_TEST);
      assertTrue(true);
    }

    @Test
    public void shouldInstantiateByLocalMySQLConnectionInfo() throws SQLException, IOException,
        InterruptedException {
      SchemaDumper schemaDumper = new SchemaDumper(MySQLConnectionInfo.builder().build());
      schemaDumper.dump(SQL_FOR_TEST);
      assertTrue(true);
    }

    @Test
    public void shouldInstantiateByAllArgs() throws SQLException, IOException, InterruptedException {
      SchemaDumper schemaDumper =
          new SchemaDumper(MySQLConnectionInfo.builder().build(), "mysqldump");
      schemaDumper.dump(SQL_FOR_TEST);
      assertTrue(true);
    }

    @Test
    public void shouldRaiseIAExceptionByNullConnectionInfo() {
      try {
        new SchemaDumper(null, "mysqldump");
      } catch (IllegalArgumentException e) {
        assertTrue(true);
        return;
      }
      assertTrue(false);
    }

    @Test
    public void shouldRaiseIAExceptionByNullMysqldumpPath() {
      try {
        new SchemaDumper(MySQLConnectionInfo.builder().build(), null);
      } catch (IllegalArgumentException e) {
        assertTrue(true);
        return;
      }
      assertTrue(false);
    }
  }

  public static class ForDumpMethods {
    private SchemaDumper schemaDumper = new SchemaDumper();

    @Test
    public void shouldDumpBySQLString() throws SQLException, IOException, InterruptedException {
      schemaDumper.dump(SQL_FOR_TEST);
      assertTrue(true);
    }

    @Test
    public void shouldDumpBySQLFileWithDefaultCharset()
        throws IOException, SQLException, InterruptedException {
      File sqlFile = File.createTempFile("tempsql", ".sql");

      try (BufferedWriter bufferedWriter = new BufferedWriter
          (new OutputStreamWriter(new FileOutputStream(sqlFile), Charset.forName("UTF-8")))) {
        bufferedWriter.write(SQL_FOR_TEST);
      }

      schemaDumper.dump(sqlFile);
      assertTrue(true);

      sqlFile.delete();
    }

    @Test
    public void shouldDumpBySQLFileWithSpecifiedCharset()
        throws IOException, SQLException, InterruptedException {
      File sqlFile = File.createTempFile("tempsql", ".sql");

      try (BufferedWriter bufferedWriter = new BufferedWriter
          (new OutputStreamWriter(new FileOutputStream(sqlFile), Charset.forName("EUC-JP")))) {
        bufferedWriter.write(SQL_FOR_TEST);
      }

      schemaDumper.dump(sqlFile, Charset.forName("EUC-JP"));
      assertTrue(true);

      sqlFile.delete();
    }

    @Test
    public void shouldDumpFromLocalMySQL() throws SQLException, IOException, InterruptedException
    {
      MySQLConnectionInfo connInfo = MySQLConnectionInfo.builder().build();

      String tempDBName = new StringBuilder()
          .append("tmp_")
          .append(UUID.randomUUID().toString().replaceAll("-", ""))
          .toString();
      String mysqlURL = connInfo.getJdbcURL();
      String user = connInfo.getUser();
      String pass = connInfo.getPass();

      try (Connection connection = DriverManager.getConnection(mysqlURL, user, pass)) {
        try (Statement stmt = connection.createStatement()) {
          stmt.executeUpdate("CREATE DATABASE " + tempDBName);
        }
        try (Statement stmt = connection.createStatement()) {
          stmt.executeUpdate("USE " + tempDBName + "; " + SQL_FOR_TEST);
        }

        schemaDumper.dumpFromLocalDB(tempDBName);
      } catch (Exception e) {
        throw e;
      } finally {
        try (Connection connectionToTeardown = DriverManager.getConnection(mysqlURL, user, pass)) {
          try (Statement stmt = connectionToTeardown.createStatement()) {
            stmt.executeUpdate("DROP DATABASE " + tempDBName);
          }
        }
      }
      assertTrue(true);
    }

    @Test
    public void shouldDumpFromRemoteMySQL() throws SQLException, IOException, InterruptedException {
      MySQLConnectionInfo connInfo = MySQLConnectionInfo.builder().build();

      String tempDBName = new StringBuilder()
          .append("tmp_")
          .append(UUID.randomUUID().toString().replaceAll("-", ""))
          .toString();
      String mysqlURL = connInfo.getJdbcURL();
      String user = connInfo.getUser();
      String pass = connInfo.getPass();

      try (Connection connection = DriverManager.getConnection(mysqlURL, user, pass)) {
        try (Statement stmt = connection.createStatement()) {
          stmt.executeUpdate("CREATE DATABASE " + tempDBName);
        }
        try (Statement stmt = connection.createStatement()) {
          stmt.executeUpdate("USE " + tempDBName + "; " + SQL_FOR_TEST);
        }
        schemaDumper.dumpFromRemoteDB(tempDBName, connInfo);
      } catch (Exception e) {
        throw e;
      } finally {
        try (Connection connectionToTeardown = DriverManager.getConnection(mysqlURL, user, pass)) {
          try (Statement stmt = connectionToTeardown.createStatement()) {
            stmt.executeUpdate("DROP DATABASE " + tempDBName);
          }
        }
      }
      assertTrue(true);
    }
  }
}