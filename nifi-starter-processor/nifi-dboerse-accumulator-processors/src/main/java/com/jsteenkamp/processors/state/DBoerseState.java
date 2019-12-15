package com.jsteenkamp.processors.state;

import java.util.Map;

/**
 * A simple type that holds the state for the accumulations.
 *  
 * @author jsteenkamp
 *
 */
public class DBoerseState
{
	private Map<String, DBoerseDailyState> accumulations;

	public Map<String, DBoerseDailyState> getAccumulations()
	{
		return accumulations;
	}

	public void setAccumulations(Map<String, DBoerseDailyState> accumulations)
	{
		this.accumulations = accumulations;
	}

	@Override
	public int hashCode()
	{
		final int prime = 31;
		int result = 1;
		result = prime * result + ((accumulations == null) ? 0 : accumulations.hashCode());
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
		DBoerseState other = (DBoerseState) obj;
		if (accumulations == null)
		{
			if (other.accumulations != null)
				return false;
		} else if (!accumulations.equals(other.accumulations))
			return false;
		return true;
	}
	
}
