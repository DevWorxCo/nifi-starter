package com.jsteenkamp.processors.state;

import java.io.BufferedReader;
import java.lang.System.Logger;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.junit.Assert;
import org.junit.Test;


public class DBoerseStateServiceTest
{
	private static Logger logger = System.getLogger(DBoerseStateServiceTest.class.getName());

	@Test
	public void testRoundtrip()
	{
		DBoerseStateService srv = new DBoerseStateService(Paths.get("target/test-state"));
		final DBoerseState dbstate = srv.getOrLoadDBoerseState();
		
		Map<String, DBoerseDailyState> accums = dbstate.getAccumulations();
		DBoerseDailyState dailyState = accums.computeIfAbsent(LocalDate.now().toString(), f -> 
		{
			DBoerseDailyState s = new DBoerseDailyState();
			s.setDate(f);
			s.setEntries(new ConcurrentHashMap<String, DBoerseEntry>());
			return s;
		});

		String date = dailyState.getDate();
		Assert.assertEquals(LocalDate.now().toString(), date);
		
		Map<String, DBoerseEntry> entries = dailyState.getEntries();
		DBoerseEntry entry = entries.computeIfAbsent("DE0005200000", isin -> 
		{
			DBoerseEntry e = new DBoerseEntry();
			e.setIsin(isin);
			e.setMaxPrice(10);
			e.setMinPrice(10);
			e.setMnemonic("DE00");
			e.setNumberOfTrades(10);
			e.setSecurityDescription("SEC DESCR");
			e.setTradedVolume(120);
			return e;
		});
		
		Assert.assertNotNull(entry);
		
		byte[] bytedata = srv.persistAndReturnDBoerseState(dbstate);
		
		logger.log(System.Logger.Level.INFO, "\n" + new String(bytedata));
		
		final DBoerseState dbstate_fromDisk = srv.getOrLoadDBoerseState();
		
		Assert.assertFalse(dbstate == dbstate_fromDisk);
		
		Assert.assertEquals(dbstate, dbstate_fromDisk);
		
	}
	
	
	@Test
	public void testCSVReading() throws Exception
	{
		DBoerseStateService srv = new DBoerseStateService(Paths.get("target/test-state-csv"));
		final DBoerseState dbstate = srv.getNewDBoerseState();
		
		try(BufferedReader csvReader = Files.newBufferedReader(Paths.get("src/test/resources/2019-12-13_BINS_XETR22.csv")))
		{
			srv.updateDBoerseStateWithCSVStream(dbstate, csvReader);
		}
		
		Map<String, DBoerseDailyState> accums = dbstate.getAccumulations();
		DBoerseDailyState dbState = accums.get("2019-12-13");
		Assert.assertNotNull(dbState);
		
		Map<String, DBoerseEntry> entries = dbState.getEntries();
		
		entries.forEach((k,v) -> 
		{
			logger.log(System.Logger.Level.INFO, v);
		});
		
		
	}
	
	
}






















