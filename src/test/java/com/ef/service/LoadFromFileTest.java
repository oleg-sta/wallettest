package com.ef.service;

import org.junit.Test;
import org.mockito.ArgumentCaptor;

import java.io.File;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class LoadFromFileTest {
    @Test
    public void whenParsed3LinesInseertToDb() throws Exception {
        File file = new File(this.getClass().getClassLoader().getResource("test.log").getFile());
        Connection connection = mock(Connection.class);
        PreparedStatement preparedStatement = mock(PreparedStatement.class);
        when(connection.prepareStatement(any())).thenReturn(preparedStatement);
        LoadFromFile loadFromFile = new LoadFromFile(connection);
        loadFromFile.parseFile(file.getAbsolutePath());
        verify(preparedStatement, times(3)).setTimestamp(anyInt(), any(), any());

        ArgumentCaptor<String> argument = ArgumentCaptor.forClass(String.class);
        verify(preparedStatement, times(12)).setString(anyInt(), argument.capture());
        List<String> capturedString = argument.getAllValues();
        assertEquals("192.168.234.82", capturedString.get(0));
        assertEquals("\"GET / HTTP/1.1\"", capturedString.get(1));
        assertEquals("200", capturedString.get(2));
        assertEquals("\"swcd (unknown version) CFNetwork/808.2.16 Darwin/15.6.0\"", capturedString.get(3));
        assertEquals("\"Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/51.0.2704.79 Safari/537.36 Edge/14.14393\"", capturedString.get(11));
        verify(preparedStatement, times(3)).addBatch();
        verify(preparedStatement, times(1)).executeBatch();
        verify(connection, never()).close();
    }

    @Test
    public void whenNoFileNothingHappen() throws Exception {
        Connection connection = mock(Connection.class);
        PreparedStatement preparedStatement = mock(PreparedStatement.class);
        when(connection.prepareStatement(any())).thenReturn(preparedStatement);
        LoadFromFile loadFromFile = new LoadFromFile(connection);
        loadFromFile.parseFile("this file doesn't exist");
        verify(preparedStatement, never()).setTimestamp(anyInt(), any(), any());
        verify(preparedStatement, never()).setString(anyInt(), any());
        verify(preparedStatement, never()).addBatch();
        verify(preparedStatement, never()).executeBatch();
        verify(connection, never()).close();
    }

    @Test
    public void whenParsedIgnoreIncorrectLines() throws Exception {
        File file = new File(this.getClass().getClassLoader().getResource("incorrectLines.log").getFile());
        Connection connection = mock(Connection.class);
        PreparedStatement preparedStatement = mock(PreparedStatement.class);
        when(connection.prepareStatement(any())).thenReturn(preparedStatement);
        LoadFromFile loadFromFile = new LoadFromFile(connection);
        loadFromFile.parseFile(file.getAbsolutePath());
        verify(preparedStatement, times(1)).setTimestamp(anyInt(), any(), any());

        ArgumentCaptor<String> argument = ArgumentCaptor.forClass(String.class);
        verify(preparedStatement, times(4)).setString(anyInt(), argument.capture());
        List<String> capturedPeople = argument.getAllValues();
        assertEquals("192.168.169.194", capturedPeople.get(0));
        assertEquals("\"GET / HTTP/1.1\"", capturedPeople.get(1));
        assertEquals("200", capturedPeople.get(2));
        assertEquals("\"Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/51.0.2704.79 Safari/537.36 Edge/14.14393\"", capturedPeople.get(3));
        verify(preparedStatement, times(1)).addBatch();
        verify(preparedStatement, times(1)).executeBatch();
        verify(connection, never()).close();
    }

}