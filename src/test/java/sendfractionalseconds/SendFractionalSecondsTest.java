package sendfractionalseconds;

import org.apache.commons.dbcp2.BasicDataSource;
import org.apache.commons.lang3.time.DateUtils;
import org.databene.contiperf.PerfTest;
import org.databene.contiperf.junit.ContiPerfRule;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Date;

/**
 * Test MySQL Connector/J 5.1.37's sendFractionalSeconds performance.
 */
public class SendFractionalSecondsTest {
    public static final int MAX_THREADS = 60;
    public static final String JDBC_URL_FOR_SEND_FRACTIONAL_SECONDS_TRUE = "jdbc:mysql://kwon37xi-dev-pc:3306/test?useUnicode=true&characterEncoding=utf8&sendFractionalSeconds=true";
    public static final String JDBC_URL_FOR_SEND_FRACTIONAL_SECONDS_FALSE = "jdbc:mysql://kwon37xi-dev-pc:3306/test?useUnicode=true&characterEncoding=utf8&sendFractionalSeconds=false";
    public static final String JDBC_USERNAME = "";
    public static final String JDBC_PASSWORD = "";

    @Rule
    public ContiPerfRule contiPerfRule = new ContiPerfRule();

    private BasicDataSource dataSourceForSendFractionalSecondsTrue;
    private BasicDataSource dataSourceForSendFractionalSecondsFalse;
    private Date lastDay;

    @Before
    public void setUp() throws Exception {
        lastDay = DateUtils.parseDate("2015-12-31 23:59:59.500", "yyyy-MM-dd HH:mm:ss.SSS");

        dataSourceForSendFractionalSecondsTrue = new BasicDataSource();
        dataSourceForSendFractionalSecondsFalse = new BasicDataSource();

        initializeDataSource(dataSourceForSendFractionalSecondsTrue, JDBC_URL_FOR_SEND_FRACTIONAL_SECONDS_TRUE);
        initializeDataSource(dataSourceForSendFractionalSecondsFalse, JDBC_URL_FOR_SEND_FRACTIONAL_SECONDS_FALSE);

        try (Connection conn = dataSourceForSendFractionalSecondsTrue.getConnection()) {
            try (Statement stmt = conn.createStatement()) {
                stmt.execute("drop table if exists `send_fractional_seconds_test`");
                stmt.execute("CREATE TABLE send_fractional_seconds_test (\n" +
                        "\tid int AUTO_INCREMENT PRIMARY KEY,\n" +
                        "\tdatetime1 datetime not null,\n" +
                        "\tdatetime2 datetime(3) not null,\n" +
                        "\tdatetime3 datetime(6) not null\n" +
                        ");\n");
            }
        }

        try (Connection conn = dataSourceForSendFractionalSecondsFalse.getConnection()) {
            // do nothing.
            // just for initialization;
        }
    }

    private void initializeDataSource(BasicDataSource dataSource, String jdbcUrl) {
        dataSource.setMaxTotal(MAX_THREADS);
        dataSource.setMinIdle(10);
        dataSource.setMaxIdle(MAX_THREADS);
        dataSource.setInitialSize(10);
        dataSource.setUrl(jdbcUrl);
        dataSource.setUsername(JDBC_USERNAME);
        dataSource.setPassword(JDBC_PASSWORD);
    }

    @After
    public void tearDown() throws Exception {
        if (dataSourceForSendFractionalSecondsTrue != null) {
            dataSourceForSendFractionalSecondsTrue.close();
        }

        if (dataSourceForSendFractionalSecondsFalse != null) {
            dataSourceForSendFractionalSecondsFalse.close();
        }

    }

    @Test
    @PerfTest(invocations = 100000, threads = MAX_THREADS, rampUp = 10, warmUp = 300)
    public void insert_for_sendFractionalSeconds_true() throws Exception {
        insertPerfTest(dataSourceForSendFractionalSecondsTrue);
    }

    @Test
    @PerfTest(invocations = 100000, threads = MAX_THREADS, rampUp = 10, warmUp = 300)
    public void insert_for_sendFractionalSeconds_false() throws Exception {
        insertPerfTest(dataSourceForSendFractionalSecondsFalse);
    }

    private void insertPerfTest(BasicDataSource dataSource) throws SQLException {
        try (Connection conn = dataSource.getConnection()) {
            try (PreparedStatement prepStmt = conn.prepareStatement("INSERT INTO send_fractional_seconds_test (datetime1, datetime2, datetime3) values(?, ?, ?)")) {
                prepStmt.setObject(1, lastDay);
                prepStmt.setObject(2, lastDay);
                prepStmt.setObject(3, lastDay);
                prepStmt.addBatch();

                Date date = new Date();
                prepStmt.setObject(1, date);
                prepStmt.setObject(2, date);
                prepStmt.setObject(3, date);
                prepStmt.addBatch();
                prepStmt.executeBatch();
            }
        }
    }
}
