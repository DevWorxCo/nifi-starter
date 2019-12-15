package com.jsteenkamp.processors.state;

/**
 * An individual entry 
 * 
 * @author jsteenkamp
 *
 */
public class DBoerseEntry
{
	private String isin;
	private String mnemonic;
	private String securityDescription;

	private long tradedVolume;
	private long numberOfTrades;
	private double maxPrice;
	private double minPrice;
	
	public DBoerseEntry()
	{
	}

	public void setIsin(String isin)
	{
		this.isin = isin;
	}

	public void setMnemonic(String mnemonic)
	{
		this.mnemonic = mnemonic;
	}

	public void setSecurityDescription(String securityDescription)
	{
		this.securityDescription = securityDescription;
	}

	public void setTradedVolume(long tradedVolume)
	{
		this.tradedVolume = tradedVolume;
	}

	public void setNumberOfTrades(long numberOfTrades)
	{
		this.numberOfTrades = numberOfTrades;
	}

	public void setMaxPrice(double maxPrice)
	{
		this.maxPrice = maxPrice;
	}

	public void setMinPrice(double minPrice)
	{
		this.minPrice = minPrice;
	}

	public long getTradedVolume()
	{
		return tradedVolume;
	}

	public long getNumberOfTrades()
	{
		return numberOfTrades;
	}
 
	public double getMaxPrice()
	{
		return maxPrice;
	}
 
	public double getMinPrice()
	{
		return minPrice;
	}

	public String getIsin()
	{
		return isin;
	}

	public String getMnemonic()
	{
		return mnemonic;
	}

	public String getSecurityDescription()
	{
		return securityDescription;
	}

	@Override
	public int hashCode()
	{
		final int prime = 31;
		int result = 1;
		result = prime * result + ((isin == null) ? 0 : isin.hashCode());
		long temp;
		temp = Double.doubleToLongBits(maxPrice);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		temp = Double.doubleToLongBits(minPrice);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		result = prime * result + ((mnemonic == null) ? 0 : mnemonic.hashCode());
		result = prime * result + (int) (numberOfTrades ^ (numberOfTrades >>> 32));
		result = prime * result + ((securityDescription == null) ? 0 : securityDescription.hashCode());
		result = prime * result + (int) (tradedVolume ^ (tradedVolume >>> 32));
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
		DBoerseEntry other = (DBoerseEntry) obj;
		if (isin == null)
		{
			if (other.isin != null)
				return false;
		} else if (!isin.equals(other.isin))
			return false;
		if (Double.doubleToLongBits(maxPrice) != Double.doubleToLongBits(other.maxPrice))
			return false;
		if (Double.doubleToLongBits(minPrice) != Double.doubleToLongBits(other.minPrice))
			return false;
		if (mnemonic == null)
		{
			if (other.mnemonic != null)
				return false;
		} else if (!mnemonic.equals(other.mnemonic))
			return false;
		if (numberOfTrades != other.numberOfTrades)
			return false;
		if (securityDescription == null)
		{
			if (other.securityDescription != null)
				return false;
		} else if (!securityDescription.equals(other.securityDescription))
			return false;
		if (tradedVolume != other.tradedVolume)
			return false;
		return true;
	}

	@Override
	public String toString()
	{
		return "DBoerseEntry [isin=" + isin + ", mnemonic=" + mnemonic + ", securityDescription=" + securityDescription + ", tradedVolume=" + tradedVolume + ", numberOfTrades=" + numberOfTrades + ", maxPrice=" + maxPrice + ", minPrice=" + minPrice + "]";
	}

}
