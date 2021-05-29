package com.pingidentity.apac.magiclink.otp.impl;

import java.util.Date;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class TimeLimitedHashMap<K, V> extends ConcurrentHashMap<K, V> {

    private static final long serialVersionUID = 1L;

    private Map<K, Long> timeMap = new ConcurrentHashMap<K, Long>();
    private Map<K, String> ipAddressMap = new ConcurrentHashMap<K, String>();
    private final long expiryInMillis;

    public TimeLimitedHashMap(long expiryInMillis) {
        this.expiryInMillis = expiryInMillis;
    }
    
    public V get(Object key, String expectedIpAddress)
    {
        long currentTime = new Date().getTime();
        if (currentTime > timeMap.get(key))
        	return null;
        
        if(ipAddressMap.containsKey(key))
        {
	        String ipAddress = ipAddressMap.get(key);
	        if(ipAddress == null || !ipAddress.equals(expectedIpAddress))
	        	return null;
        }
        
    	return super.get(key);
    }
    
    @Override
    public V remove(Object key)
    {
    	if(!this.containsKey(key))
    		return null;
    	
    	V item = get(key);
    	
    	super.remove(key);
    	timeMap.remove(key);
    	ipAddressMap.remove(key);
    	
    	return item;
    }

    public V put(K key, V value, String ipAddress) {
        V returnVal = this.put(key, value, expiryInMillis, ipAddress);
        return returnVal;
    }

    public V put(K key, V value, long expiryInMillis, String ipAddress) {
        Date date = new Date();
        timeMap.put(key, date.getTime() + expiryInMillis);
        
        if(ipAddress != null && !ipAddress.trim().equals(""))
        	ipAddressMap.put(key, ipAddress);
        
        V returnVal = super.put(key, value);
        return returnVal;
    }
}