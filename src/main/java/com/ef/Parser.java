package com.ef;

import com.ef.service.IpAnalyzer;
import com.ef.service.LoadFromFile;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.TimeZone;
import java.util.stream.Collectors;

public class Parser {

    protected LoadFromFile loadFromFile;
    protected IpAnalyzer ipAnalyzer;
    private static final DateFormat DATE_PARSER = new SimpleDateFormat("yyyy-MM-dd.H:mm:ss");
    private static final String START_DATE = "startDate";
    private static final String DURATION = "duration";
    private static final String THRESHOLD = "threshold";
    private static final String DURATION_HOURLY = "hourly";
    private static final String DURATION_DAILY = "daily";
    private static final String ACCESSLOG = "accesslog";

    private static final String URL = "url";
    private static final String USER = "user";
    private static final String PASSWORD = "password";
    private static final String DEFAULT_SCHEMA = "wallettest";

    private static final String PROPERTIES_FILE = "config.properties";


    public static void main(String[] args) throws ParseException, SQLException, ClassNotFoundException, IOException {
        new Parser().run(args);
    }

    private void run(String[] args) throws ParseException, ClassNotFoundException, SQLException, IOException {
        // init
        Map<String, String> parameters = loadConfig();
        parameters.putAll(commandLineToParameters(args));
        Class.forName("com.mysql.jdbc.Driver");
        Connection connection = DriverManager.getConnection(parameters.get(URL), parameters.get(USER), parameters.get(PASSWORD));
        connection.setSchema(DEFAULT_SCHEMA);
        loadFromFile = new LoadFromFile(connection);
        ipAnalyzer = new IpAnalyzer(connection);
        doWork(parameters);
    }

    public void doWork(Map<String, String> parameters) throws IOException, ParseException {
        DATE_PARSER.setTimeZone(TimeZone.getTimeZone("UTC"));
        Date startDate = parameters.get(START_DATE) != null? DATE_PARSER.parse(parameters.get(START_DATE)) : null;
        Integer duration = parameters.get( THRESHOLD)!= null? Integer.parseInt(parameters.get( THRESHOLD)) : null;
        int type = DURATION_HOURLY.equals(parameters.get(DURATION))? 1 : 24;
        String accesslog = parameters.get(ACCESSLOG);
        if (accesslog != null) {
            loadFromFile.parseFile(accesslog);
        }
        if (startDate != null && duration != null) {
            ipAnalyzer.analyze(startDate, type, duration);
        }
    }

    public Map<String, String> loadConfig() {
        Map<String, String> result = new HashMap<>();
        try (InputStream input = new FileInputStream(PROPERTIES_FILE)) {
            Properties prop = new Properties();
            prop.load(input);
            result.putAll((Map)prop);
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        return result;
    }

    public static Map<String, String> commandLineToParameters(String[] args) {
        return Arrays.stream(args).filter(o->o.startsWith("--"))
                .map(o->o.substring(2))
                .filter(o->o.contains("="))
                .map(o->o.split("="))
                .collect(Collectors.toMap(o->o[0], o->o[1], (o1, o2) -> o1));
    }
}
