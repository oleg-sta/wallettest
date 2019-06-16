package com.ef.service;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.TimeZone;

public class LoadFromFile {
    private static final String DELIMITER_LOG = "\\|";
    private static final DateFormat DATE_PARSER = new SimpleDateFormat("yyyy-MM-dd H:mm:ss");
    private final Connection connection;
    private static final int BATCH_SIZE = 1000;
    private static final String INSERT_STATEMENT = "insert into logs (date, ip, request, status, user_agent) values (?, ?, ?, ?, ?)";

    public LoadFromFile(Connection connection) {
        this.connection = connection;
    }

    // file format
    // Date, IP, Request, Status, User Agent

    public void parseFile(String fullPathName) throws IOException {
        DATE_PARSER.setTimeZone(TimeZone.getTimeZone("UTC"));
        Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
        File f = new File(fullPathName);
        if (f.exists()) {
            try (BufferedReader reader = new BufferedReader(new FileReader(f))) {
                String line = reader.readLine();
                connection.setAutoCommit(false);
                try (PreparedStatement preparedStatement = connection.prepareStatement(INSERT_STATEMENT)) {
                    int count = 0;
                    while (line != null) {
                        String[] values = line.split(DELIMITER_LOG);
                        if (values.length == 5) {
                            int i = 1;
                            preparedStatement.setTimestamp(i++, new java.sql.Timestamp(DATE_PARSER.parse(values[0]).getTime()), cal);
                            preparedStatement.setString(i++, values[1]);
                            preparedStatement.setString(i++, values[2]);
                            preparedStatement.setString(i++, values[3]);
                            preparedStatement.setString(i++, values[4]);
                            preparedStatement.addBatch();
                            if (++count % BATCH_SIZE == 0) {
                                preparedStatement.executeBatch();
                                connection.commit();
                            }
                        }
                        line = reader.readLine();
                    }
                    preparedStatement.executeBatch();
                    connection.commit();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
                connection.setAutoCommit(true);
            } catch (ParseException e) {
                e.printStackTrace();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }
}
