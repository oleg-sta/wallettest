package com.ef.service;

import org.junit.Test;
import org.mockito.ArgumentCaptor;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class IpAnalyzerTest {
    @Test
    public void analyzeZeroReturn() throws Exception {
        Connection connection = mock(Connection.class);
        PreparedStatement statementFind = mock(PreparedStatement.class);
        PreparedStatement statementInsert = mock(PreparedStatement.class);
        when(connection.prepareStatement(any())).thenReturn(statementFind).thenReturn(statementInsert);
        ResultSet resultSet = mock(ResultSet.class);
        when(statementFind.executeQuery()).thenReturn(resultSet);
        IpAnalyzer ipAnalyzer = new IpAnalyzer(connection);
        ipAnalyzer.analyze(new Date(), 1, 1);
        verify(statementInsert, never()).addBatch();
        verify(connection, never()).close();
    }

    @Test
    public void analyzeOneReturn() throws Exception {
        Connection connection = mock(Connection.class);
        PreparedStatement statementFind = mock(PreparedStatement.class);
        PreparedStatement statementInsert = mock(PreparedStatement.class);
        when(connection.prepareStatement(any())).thenReturn(statementFind).thenReturn(statementInsert);
        ResultSet resultSet = mock(ResultSet.class);
        when(statementFind.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(true).thenReturn(false);
        when(resultSet.getString(anyInt())).thenReturn("ip");
        when(resultSet.getInt(anyInt())).thenReturn(22);

        IpAnalyzer ipAnalyzer = new IpAnalyzer(connection);
        ipAnalyzer.analyze(new Date(), 1, 1);

        ArgumentCaptor<String> argument = ArgumentCaptor.forClass(String.class);
        verify(statementInsert, times(2)).setString(anyInt(), argument.capture());
        List<String> capturedString = argument.getAllValues();
        assertEquals("ip", capturedString.get(0));
        assertTrue( capturedString.get(1).contains("22"));
        verify(statementInsert, times(1)).addBatch();
        verify(connection, never()).close();
    }

    @Test
    public void checkShift() throws Exception {
        DateFormat df = new SimpleDateFormat("yyyyMMdd HH:mm:ss");
        Date date = df.parse("20190616 14:00:00");
        assertEquals(df.parse("20190616 15:00:00"), IpAnalyzer.getDateShift(date, 1));
        assertEquals(df.parse("20190617 14:00:00"), IpAnalyzer.getDateShift(date, 24));
    }
}