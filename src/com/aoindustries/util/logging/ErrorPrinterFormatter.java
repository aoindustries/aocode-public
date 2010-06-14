/*
 * aocode-public - Reusable Java library of general tools with minimal external dependencies.
 * Copyright (C) 2009, 2010  AO Industries, Inc.
 *     support@aoindustries.com
 *     7262 Bull Pen Cir
 *     Mobile, AL 36695
 *
 * This file is part of aocode-public.
 *
 * aocode-public is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * aocode-public is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with aocode-public.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.aoindustries.util.logging;

import com.aoindustries.util.ErrorPrinter;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Formatter;
import java.util.logging.LogRecord;

/**
 * Uses <code>ErrorPrinter</code> to format log messages.
 *
 * @see  ErrorPrinter
 *
 * @author  AO Industries, Inc.
 */
public class ErrorPrinterFormatter extends Formatter {

    private static final ErrorPrinterFormatter errorPrinterFormatter = new ErrorPrinterFormatter();
    public static ErrorPrinterFormatter getInstance() {
        return errorPrinterFormatter;
    }

    private ErrorPrinterFormatter() {
    }

    public void format(LogRecord record, Appendable out) {
        List<Object> extraInfo = new ArrayList<Object>(9); // At most 9 elements added below
        String loggerName = record.getLoggerName();
        if(loggerName!=null) extraInfo.add("record.loggerName="+loggerName);
        extraInfo.add("record.level="+record.getLevel());
        extraInfo.add("record.sequenceNumber="+record.getSequenceNumber());
        String sourceClassName = record.getSourceClassName();
        if(sourceClassName!=null) extraInfo.add("record.sourceClassName="+sourceClassName);
        String sourceMethodName = record.getSourceMethodName();
        if(sourceMethodName!=null) extraInfo.add("record.sourceMethodName="+sourceMethodName);
        String message = record.getMessage();
        if(message==null) message = "";
        extraInfo.add("record.message="+message);
        String formatted = formatMessage(record);
        if(formatted==null) formatted = "";
        if(!message.equals(formatted)) extraInfo.add("record.message.formatted="+formatted);
        extraInfo.add("record.threadID="+record.getThreadID());
        extraInfo.add("record.millis="+record.getMillis());
        Throwable thrown = record.getThrown();
        ErrorPrinter.printStackTraces(
            thrown,
            out,
            extraInfo.toArray()
        );
    }

    public String format(LogRecord record) {
        StringBuilder buffer = new StringBuilder(1024);
        format(record, buffer);
        return buffer.toString();
    }
}
