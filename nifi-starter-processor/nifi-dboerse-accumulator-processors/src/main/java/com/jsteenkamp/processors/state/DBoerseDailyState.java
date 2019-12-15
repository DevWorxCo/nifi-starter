package com.jsteenkamp.processors.state;

import java.util.Map;

/**
 * The daily state for a particular entry.  
 * 
 * @author jsteenkamp
 *
 */
public class DBoerseDailyState
{
	private String date;
	private Map<String, DBoerseEntry> entries;
	
	public String getDate()
	{
		return date;
	}
	
	public void setDate(String date)
	{
		this.date = date;
	}
	public Map<String, DBoerseEntry> getEntries()
	{
		return entries;
	}

	public void setEntries(Map<String, DBoerseEntry> entries)
	{
		this.entries = entries;
	}

	@Override
	public int hashCode()
	{
		final int prime = 31;
		int result = 1;
		result = prime * result + ((date == null) ? 0 : date.hashCode());
		result = prime * result + ((entries == null) ? 0 : entries.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj)
	{
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		DBoerseDailyState other = (DBoerseDailyState) obj;
		if (date == null)
		{
			if (other.date != null)
				return false;
		} else if (!date.equals(other.date))
			return false;
		if (entries == null)
		{
			if (other.entries != null)
				return false;
		} else if (!entries.equals(other.entries))
			return false;
		return true;
	}
	
}

