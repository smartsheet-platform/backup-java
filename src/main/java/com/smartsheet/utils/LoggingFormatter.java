package com.smartsheet.utils;

import java.util.Date;
import java.util.logging.Formatter;
import java.util.logging.LogRecord;

public class LoggingFormatter extends Formatter  {
	 
		/**
		 * Returns simple formatted log text
		 */
	    @Override
	    public String format(final LogRecord record) {
	    
	    	
	        return new Date(record.getMillis())+"::"
	        		//+record.getSourceClassName()+"::"
	                //+record.getSourceMethodName()+"::"
	                +record.getLevel()+"::"
	                +record.getMessage()+"\n";
	    }
}
