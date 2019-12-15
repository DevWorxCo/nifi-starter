package com.jsteenkamp.processors.state;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.lang.System.Logger;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;

import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Some utility functions to help work with the DBoerseState
 */
public class DBoerseStateService
{
	private static Logger logger = System.getLogger(DBoerseStateService.class.getName());
	
	public static final int ISIN_COL_INDEX = 0;
	public static final int MNEMONIC_COL_INDEX = 1;
	public static final int SECURITY_DESCR_COL_INDEX = 2;
	public static final int DATE_COL_INDEX = 6;
	public static final int MAX_PRICE_COL_INDEX = 9;
	public static final int MIN_PRICE_COL_INDEX = 10;
	public static final int TRADED_VOLUME_COL_INDEX = 12;
	public static final int NUMBER_OF_TRADES_COL_INDEX = 13;
	
	private final static ObjectMapper s_mapper;
	static
	{
		s_mapper = new ObjectMapper();
	}
	
	private static final String FILENAME = "DBoerseState.json";
	private final Path stateDir;
	private final Path stateFile;	
	
	public DBoerseStateService(String stateDir)
	{
		this(Paths.get(stateDir));
	}
	
	public DBoerseStateService(Path stateDir)
	{
		this.stateDir = stateDir;
		if(Files.exists(this.stateDir) == false)
		{
			logger.log(System.Logger.Level.INFO, "Creating the Directory : " + stateDir);
			try
			{
				Files.createDirectories(this.stateDir);
			} 
			catch (IOException e)
			{
				throw new RuntimeException("Unable to create the parent directory : " + e);
			}
		}
		stateFile = stateDir.resolve(FILENAME);
	}
	
	public DBoerseState getNewDBoerseState()
	{
		try
		{
			DBoerseState dbstate = new DBoerseState();
			dbstate.setAccumulations(new ConcurrentHashMap<String, DBoerseDailyState>());
			s_mapper.writeValue(stateFile.toFile(),dbstate);
			return dbstate;
		}
		catch (IOException e)
		{
			throw new RuntimeException("Unable to perform the required I/O operations on this file - " + stateFile + " - got exception : " + e);
		}
	}
	
	public DBoerseState getOrLoadDBoerseState()
	{
		try
		{
			if(Files.exists(stateFile) == true)
			{
				DBoerseState dbstate = s_mapper.readValue(stateFile.toFile(), DBoerseState.class);
				return dbstate;
			}
			else
			{
				return getNewDBoerseState();
			}
		} 
		catch (IOException e)
		{
			throw new RuntimeException("Unable to perform the required I/O operations on this file - " + stateFile + " - got exception : " + e);
		}
	}
	
	public byte[] persistAndReturnDBoerseState(DBoerseState dbState)
	{
		try
		{
			byte[] vabs = s_mapper.writeValueAsBytes(dbState);
			Files.write(stateFile, vabs, StandardOpenOption.TRUNCATE_EXISTING);
			return vabs;
		} 
		catch (IOException e)
		{
			throw new RuntimeException("Unable to perform the required I/O operations on this file - " + stateFile + " - got exception : " + e);
		} 
	}
	
	/**
	 * Updates the given DBBoerse state with the values from the CSV file. This is in the format as specified by DBoerse - e.g. XETR csv
	 *  
	 * @throws IOException 
	 */
	public void updateDBoerseStateWithCSVStream(final DBoerseState state, final InputStream csvFile)
	{
		updateDBoerseStateWithCSVStream(state, new BufferedReader(new InputStreamReader(csvFile)));
	}
	
	/**
	 * Updates the given DBBoerse state with the values from the CSV file. This is in the format as specified by DBoerse - e.g. XETR csv
	 *  
	 * @throws IOException 
	 */
	public void updateDBoerseStateWithCSVStream(final DBoerseState state, final Reader csvFile)
	{
		try(CSVParser parser = CSVParser.parse(csvFile, CSVFormat.RFC4180))
		{
			for (CSVRecord csvRecord : parser) 
			{
				final long recNumber = csvRecord.getRecordNumber(); 
				if(recNumber == 1)
				{
					//Skip the header!
					continue;
				}
				
				final String isin = csvRecord.get(ISIN_COL_INDEX);
				final String mnemonic = csvRecord.get(MNEMONIC_COL_INDEX);
				final String secType = csvRecord.get(SECURITY_DESCR_COL_INDEX);
				final String dateString = csvRecord.get(DATE_COL_INDEX);
				final String maxPriceString = csvRecord.get(MAX_PRICE_COL_INDEX);
				final String minPriceString = csvRecord.get(MIN_PRICE_COL_INDEX);
				final String tradeVolStr = csvRecord.get(TRADED_VOLUME_COL_INDEX);
				final String numTradesStr = csvRecord.get(NUMBER_OF_TRADES_COL_INDEX);
				
				DBoerseDailyState dbDailyState = state.getAccumulations().computeIfAbsent(dateString, d -> 
				{
					DBoerseDailyState dailyState = new DBoerseDailyState();
					dailyState.setDate(d);
					dailyState.setEntries(new ConcurrentHashMap<>());
					return dailyState;
				});
				
				DBoerseEntry dbentry = dbDailyState.getEntries().computeIfAbsent(isin, x -> 
				{
					DBoerseEntry e = new DBoerseEntry();
					e.setIsin(x);
					return e;
				});
				
				dbentry.setMnemonic(mnemonic);
				dbentry.setSecurityDescription(secType);
				dbentry.setMaxPrice( Math.max(dbentry.getMaxPrice(), Double.parseDouble(maxPriceString) ));
				if(dbentry.getMinPrice() == 0)
				{
					dbentry.setMinPrice(Double.parseDouble(minPriceString));
				}
				dbentry.setMinPrice( Math.min(dbentry.getMinPrice(), Double.parseDouble(minPriceString) ));
				dbentry.setTradedVolume( Long.parseLong(tradeVolStr) + dbentry.getTradedVolume());
				dbentry.setNumberOfTrades( Long.parseLong(numTradesStr) +  dbentry.getNumberOfTrades());
			}
		}
		catch(Exception e)
		{
			throw new RuntimeException("Unable to handle the contents of the supplied CSV stream. Are you sure it conforms to the correct schemantics ?");
		}
	}
	
	
	
	
}






















