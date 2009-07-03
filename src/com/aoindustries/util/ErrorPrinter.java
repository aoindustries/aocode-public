package com.aoindustries.util;

/*
 * Copyright 2004-2009 by AO Industries, Inc.,
 * 7262 Bull Pen Cir, Mobile, Alabama, 36695, U.S.A.
 * All rights reserved.
 */
import com.aoindustries.sql.WrappedSQLException;
import java.io.Flushable;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.security.AccessControlException;
import java.security.Permission;
import java.sql.SQLException;
import java.sql.SQLWarning;

/**
 * Prints errors with more detail than a standard printStackTrace() call.  Is also able to
 * capture the error into a <code>String</code>.
 *
 * @author  AO Industries, Inc.
 */
public class ErrorPrinter {

    private static final String EOL = System.getProperty("line.separator");

    private ErrorPrinter() {}

    public static void printStackTraces(Throwable T) {
        printStackTraces(T, System.err, null);
    }

    public static void printStackTraces(Throwable T, Object[] extraInfo) {
        printStackTraces(T, System.err, extraInfo);
    }

    public static void printStackTraces(Throwable T, Appendable out) {
        printStackTraces(T, out, null);
    }

    private static void appendln(Appendable out) {
        try {
            out.append(EOL);
        } catch(IOException err) {
            // Ignored
        }
    }

    private static void append(String S, Appendable out) {
        try {
            out.append(S);
        } catch(IOException err) {
            // Ignored
        }
    }

    private static void appendln(String S, Appendable out) {
        try {
            out.append(S);
            out.append(EOL);
        } catch(IOException err) {
            // Ignored
        }
    }

    private static void append(char ch, Appendable out) {
        try {
            out.append(ch);
        } catch(IOException err) {
            // Ignored
        }
    }

    private static void append(Object O, Appendable out) {
        append(O==null ? "null" : O.toString(), out);
    }

    private static void appendln(Object O, Appendable out) {
        appendln(O==null ? "null" : O.toString(), out);
    }

    /**
     * Prints a detailed error report, including all stack traces, to the provided out.
     * Synchronizes on out to make sure concurrently reported errors will not be mixed.
     * If out is <code>Flushable</code>, will flush the output.
     * @param T
     * @param out
     * @param extraInfo
     */
    public static void printStackTraces(Throwable T, Appendable out, Object[] extraInfo) {
        
        synchronized(out) {
            appendln(out);
            appendln("**************************", out);
            appendln("* BEGIN EXCEPTION REPORT *", out);
            appendln("**************************", out);
            appendln(out);
            appendln("    Time ", out);
            append("        ", out);
            try {
                appendln(new java.util.Date(System.currentTimeMillis()).toString(), out);
            } catch(Exception err) {
                append("Unable to display date: ", out); appendln(err.toString(), out);
            }

            // Extra info
            if(extraInfo!=null && extraInfo.length>0) {
                appendln("    Extra Information", out);
                for(int c=0;c<extraInfo.length;c++) {
                    append("        ", out); appendln(extraInfo[c], out);
                }
            }

            // Threads
            appendln("    Threading", out);
            appendln("        Thread", out);
            append("            Name........: ", out);
            Thread thread=Thread.currentThread();
            appendln(thread.getName(), out);
            append("            Class.......: ", out);
            appendln(thread.getClass().getName(), out);
            append("            Priority....: ", out);
            appendln(thread.getPriority(), out);
            try {
                ThreadGroup TG=thread.getThreadGroup();
                while(TG!=null) {
                    String name=TG.getName();
                    String classname=TG.getClass().getName();
                    int maxPriority=TG.getMaxPriority();
                    appendln("        ThreadGroup", out);
                    append("            Name........: ", out); appendln(name, out);
                    append("            Class.......: ", out); appendln(classname, out);
                    append("            Max Priority: ", out); appendln(maxPriority, out);
                    TG=TG.getParent();
                }
            } catch(SecurityException err) {
                append("Unable to print all Thread Groups: ", out); appendln(err.toString(), out);
            }

            appendln("    Exceptions", out);
            printThrowables(T, out, 8);

            // End Report
            appendln(out);
            appendln("**************************", out);
            appendln("*  END EXCEPTION REPORT  *", out);
            appendln("**************************", out);

            // Flush output
            try {
                if(out instanceof Flushable) ((Flushable)out).flush();
            } catch(IOException err) {
                // Ignored
            }
        }
    }

    private static void printThrowables(Throwable T, Appendable out, int indent) {
        for(int c=0;c<indent;c++) append(' ', out);
        appendln(T.getClass().getName(), out);
        printMessage(out, indent+4, "Message...........: ", T.getMessage());
        printMessage(out, indent+4, "Localized Message.: ", T.getLocalizedMessage());
        if(T instanceof SQLException) {
            SQLException sql=(SQLException)T;
            if(sql instanceof WrappedSQLException) printMessage(out, indent+4, "SQL Statement.....: ", ((WrappedSQLException)sql).getSqlString());
            for(int c=0;c<(indent+4);c++) append(' ', out);
            append("SQL Error Code....: ", out);
            appendln(sql.getErrorCode(), out);
            for(int c=0;c<(indent+4);c++) append(' ', out);
            append("SQL State.........: ", out);
            appendln(sql.getSQLState(), out);
        } else if(T instanceof WrappedException) {
            WrappedException wrapped=(WrappedException)T;
            Object[] wrappedInfo=wrapped.getExtraInfo();
            if(wrappedInfo!=null && wrappedInfo.length>0) {
                for(int c=0;c<(indent+4);c++) append(' ', out);
                appendln("Extra Information", out);
                for(int c=0;c<wrappedInfo.length;c++) {
                    for(int d=0;d<(indent+8);d++) append(' ', out);
                    appendln(wrappedInfo[c], out);
                }
            }
        } else if(T instanceof AccessControlException) {
            try {
                AccessControlException ace = (AccessControlException)T;
                Permission permission = ace.getPermission();
                for(int c=0;c<(indent+4);c++) append(' ', out);
                append("Permission........: ", out);
                appendln(permission, out);
                if(permission!=null) {
                    for(int c=0;c<(indent+4);c++) append(' ', out);
                    append("Permission Class..: ", out);
                    appendln(permission.getClass().getName(), out);

                    for(int c=0;c<(indent+4);c++) append(' ', out);
                    append("Permission Name...: ", out);
                    appendln(permission.getName(), out);

                    for(int c=0;c<(indent+4);c++) append(' ', out);
                    append("Permission Actions: ", out);
                    appendln(permission.getActions(), out);
                }
            } catch(SecurityException err) {
                appendln("Permission........: Unable to get permission details: ", out); append(err.toString(), out);
            }
        }
        for(int c=0;c<(indent+4);c++) append(' ', out);
        appendln("Stack Trace", out);
        StackTraceElement[] stack=T.getStackTrace();
        for(int c=0;c<stack.length;c++) {
            for(int d=0;d<(indent+8);d++) append(' ', out);
            append("at ", out);
            appendln(stack[c].toString(), out);
        }
        Throwable cause=T.getCause();
        if(cause!=null) {
            for(int c=0;c<(indent+4);c++) append(' ', out);
            appendln("Caused By", out);
            printThrowables(cause, out, indent+8);
        }
        // Uses reflection avoid binding to JspException directly.
        try {
            Class<?> clazz=T.getClass();
            if(isSubclass(clazz, "javax.servlet.jsp.JspException")) {
                Method method=clazz.getMethod("getRootCause", new Class[0]);
                Throwable rootCause=(Throwable)method.invoke(T, new Object[0]);
                if(rootCause!=null) {
                    for(int c=0;c<(indent+4);c++) append(' ', out);
                    appendln("Caused By", out);
                    printThrowables(rootCause, out, indent+8);
                }
            }
        } catch(NoSuchMethodException err) {
            // OK, future versions of JspException might not have getRootCause
        } catch(IllegalAccessException err) {
            // OK, future versions of JspException could make it private
        } catch(InvocationTargetException err) {
            // Ignored because we are dealing with one exception at a time
            // Afterall, this is the exception handling code
        }
        // Uses reflection avoid binding to ServletException directly.
        try {
            Class<?> clazz=T.getClass();
            if(isSubclass(clazz, "javax.servlet.ServletException")) {
                Method method=clazz.getMethod("getRootCause", new Class[0]);
                Throwable rootCause=(Throwable)method.invoke(T, new Object[0]);
                if(rootCause!=null) {
                    for(int c=0;c<(indent+4);c++) append(' ', out);
                    appendln("Caused By", out);
                    printThrowables(rootCause, out, indent+8);
                }
            }
        } catch(NoSuchMethodException err) {
            // OK, future versions of ServletException might not have getRootCause
        } catch(IllegalAccessException err) {
            // OK, future versions of ServletException could make it private
        } catch(InvocationTargetException err) {
            // Ignored because we are dealing with one exception at a time
            // Afterall, this is the exception handling code
        }
        if(T instanceof SQLException) {
            if(T instanceof SQLWarning) {
                SQLWarning nextSQL=((SQLWarning)T).getNextWarning();
                if(nextSQL!=null) printThrowables(nextSQL, out, indent);
            } else {
                SQLException nextSQL=((SQLException)T).getNextException();
                if(nextSQL!=null) printThrowables(nextSQL, out, indent);
            }
        }
    }

    private static void printMessage(Appendable out, int indent, String label, String message) {
        for(int c=0;c<indent;c++) append(' ', out);
        append(label, out);
        if(message==null) {
            appendln("null", out);
        } else {
            message=message.trim();
            int messageLen=message.length();
            for(int c=0;c<messageLen;c++) {
                char ch=message.charAt(c);
                if(ch=='\n') {
                    int lineIndent=indent+label.length();
                    appendln(out);
                    for(int d=0;d<lineIndent;d++) append(' ', out);
                } else if(ch!='\r') append(ch, out);
            }
            appendln(out);
        }
    }
    
    private static boolean isSubclass(Class clazz, String classname) {
        while(clazz!=null) {
            if(clazz.getName().equals(classname)) return true;
            clazz=clazz.getSuperclass();
        }
        return false;
    }

    public static String getStackTraces(Throwable T) {
        return getStackTraces(T, null);
    }

    /**
     * Gets the entire exception report as a <code>String</code>.  This is not
     * as efficient as directly writing the report due to the extra buffering.
     */
    public static String getStackTraces(Throwable T, Object[] extraInfo) {
        StringBuilder out = new StringBuilder();
        printStackTraces(T, out, extraInfo);
        return out.toString();
    }
}
