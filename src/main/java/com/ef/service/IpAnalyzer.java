package com.ef.service;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

public class IpAnalyzer {

    private static final String STATEMENT = "select ip, count(*) from logs\n" +
            " where date >= ? and date < ?\n" +
            " group by ip\n" +
            " having count(*) > ?";

    private static final String INSERT_STATEMENT = "insert into blocks (ip, reason) values (?, ?)";
    private static final String REASON = "Has %s requests since %s in %s hours";

    private static final int BATCH_SIZE = 1000;

    private final Connection connection;

    public IpAnalyzer(Connection connection) {
        this.connection = connection;
    }

    public void analyze(Date date, int hours, int threshold) {
        Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
        try (PreparedStatement statementFind = connection.prepareStatement(STATEMENT);
             PreparedStatement statementInsert = connection.prepareStatement(INSERT_STATEMENT)) {
            statementFind.setTimestamp(1, new java.sql.Timestamp(date.getTime()), cal);
            Date dateEnd = getDateShift(date, hours);
            statementFind.setTimestamp(2, new java.sql.Timestamp(dateEnd.getTime()), cal);
            statementFind.setInt(3, threshold);
            int countBatch = 0;
            try (ResultSet resultSet = statementFind.executeQuery()) {
                while(resultSet.next()) {
                    String ip = resultSet.getString(1);
                    Integer count = resultSet.getInt(2);
                    System.out.println(ip);
                    statementInsert.setString(1, ip);
                    statementInsert.setString(2, String.format(REASON, count, date.toString(), hours));
                    statementInsert.addBatch();
                    if (countBatch % BATCH_SIZE == 0) {
                        statementInsert.executeBatch();
                    }
                }
            }
            statementInsert.executeBatch();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static Date getDateShift(Date date, int shiftHours) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.add(Calendar.HOUR_OF_DAY, shiftHours);
        return calendar.getTime();
    }
}
