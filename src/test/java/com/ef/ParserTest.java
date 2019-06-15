package com.ef;

import com.ef.service.IpAnalyzer;
import com.ef.service.LoadFromFile;
import org.junit.Test;

import java.io.IOException;
import java.text.ParseException;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

public class ParserTest {

    @Test
    public void commandLineToParameters() throws Exception {
        Map<String, String> parameters = Parser.commandLineToParameters(new String[]{"--a=3", "p", "a2wls'", "-s=3", "--w=3"});
        assertEquals(2, parameters.size());
        assertEquals("3", parameters.get("a"));
        assertEquals("3", parameters.get("w"));
    }

    @Test
    public void testDoWorkNoCalls() throws IOException, ParseException {
        Parser parser = new Parser();
        parser.loadFromFile = mock(LoadFromFile.class);
        parser.ipAnalyzer = mock(IpAnalyzer.class);
        parser.doWork(new HashMap<>());
        verify(parser.loadFromFile, never()).parseFile(any());
        verify(parser.ipAnalyzer, never()).analyze(any(), anyInt(), anyInt());
    }

    @Test
    public void testDoWorkLoadFile() throws IOException, ParseException {
        Parser parser = new Parser();
        parser.loadFromFile = mock(LoadFromFile.class);
        parser.ipAnalyzer = mock(IpAnalyzer.class);
        Map<String, String> parameters = new HashMap<>();
        parameters.put("accesslog", "fileName");
        parser.doWork(parameters);
        verify(parser.loadFromFile, times(1)).parseFile(any());
        verify(parser.ipAnalyzer, never()).analyze(any(), anyInt(), anyInt());

    }

    @Test
    public void testDoWorkOnlyAnalyze() throws IOException, ParseException {
        Parser parser = new Parser();
        parser.loadFromFile = mock(LoadFromFile.class);
        parser.ipAnalyzer = mock(IpAnalyzer.class);
        Map<String, String> parameters = new HashMap<>();
        parameters.put("startDate", "2017-01-01.13:00:00");
        parameters.put("duration", "daily");
        parameters.put("threshold", "250");
        parser.doWork(parameters);
        verify(parser.loadFromFile, never()).parseFile(any());
        verify(parser.ipAnalyzer, times(1)).analyze(any(), anyInt(), anyInt());
    }

    @Test
    public void testDoWorkLoadAndAnalyze() throws IOException, ParseException {
        Parser parser = new Parser();
        parser.loadFromFile = mock(LoadFromFile.class);
        parser.ipAnalyzer = mock(IpAnalyzer.class);
        Map<String, String> parameters = new HashMap<>();
        parameters.put("accesslog", "fileName");
        parameters.put("startDate", "2017-01-01.13:00:00");
        parameters.put("duration", "daily");
        parameters.put("threshold", "250");
        parser.doWork(parameters);
        verify(parser.loadFromFile, times(1)).parseFile(any());
        verify(parser.ipAnalyzer, times(1)).analyze(any(), anyInt(), anyInt());
    }

}