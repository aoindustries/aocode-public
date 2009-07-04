package com.aoindustries.util;

/*
 * Copyright 2009 by AO Industries, Inc.,
 * 7262 Bull Pen Cir, Mobile, Alabama, 36695, U.S.A.
 * All rights reserved.
 */
import java.io.IOException;

/**
 * Provides encoding and escaping for various type of data.
 *
 * @author  AO Industries, Inc.
 */
public final class EncodingUtils {

    private EncodingUtils() {
    }

    private static final String EOL = System.getProperty("line.separator");
    private static final String BR_EOL = "<br />"+EOL;

    private static final char[] hexChars={'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f'};
    private static char getHex(int value) {
        return hexChars[value & 15];
    }

    // <editor-fold defaultstate="collapsed" desc="XML Body">
    /**
     * @see  #encodeXml(java.lang.CharSequence, int, int, Appendable)
     */
    public static void encodeXml(CharSequence S, Appendable out) throws IOException {
        if(S!=null) encodeXml(S, 0, S.length(), out);
    }

    /**
     * Escapes for use in a XML body and writes to the provided <code>Appendable</code>.
     * Any characters less than 0x1f that are not \t, \r, or \n are completely filtered.
     *
     * http://www.w3.org/TR/REC-xml/#dt-escape
     * 
     * @param S the string to be escaped.  If S is <code>null</code>, nothing is written.
     *
     * @see  #encodeXmlAttribute(java.lang.CharSequence, Appendable)
     */
    public static void encodeXml(CharSequence S, int start, int end, Appendable out) throws IOException {
        if (S != null) {
            int toPrint = 0;
            for (int c = start; c < end; c++) {
                char ch = S.charAt(c);
                switch(ch) {
                    case '<':
                        if(toPrint>0) {
                            out.append(S, c-toPrint, c);
                            toPrint=0;
                        }
                        out.append("&lt;");
                        break;
                    case '>':
                        if(toPrint>0) {
                            out.append(S, c-toPrint, c);
                            toPrint=0;
                        }
                        out.append("&gt;");
                        break;
                    case '&':
                        if(toPrint>0) {
                            out.append(S, c-toPrint, c);
                            toPrint=0;
                        }
                        out.append("&amp;");
                        break;
                    default:
                        if(ch<' ' && ch!='\t' && ch!='\r' && ch!='\n') {
                            if(toPrint>0) {
                                out.append(S, c-toPrint, c);
                                toPrint=0;
                            }
                            // skip the character
                        } else {
                            toPrint++;
                        }
                }
            }
            if(toPrint>0) out.append(S, end-toPrint, end);
        }
    }

    /**
     * @see #encodeXml(CharSequence, Appendable)
     *
     * @param S the string to be escaped.
     *
     * @return if S is null then null otherwise value escaped
     */
    public static String encodeXml(CharSequence S) throws IOException {
        if(S==null) return null;
        StringBuilder result = new StringBuilder(S.length());
        encodeXml(S, result);
        return result.toString();
    }

    /**
     * @see #encodeXml(java.lang.CharSequence, int, int, java.lang.Appendable)
     *
     * @param ch the character to be escaped
     */
    public static void encodeXml(char ch, Appendable out) throws IOException {
        switch(ch) {
            case '<':
                out.append("&lt;");
                break;
            case '>':
                out.append("&gt;");
                break;
            case '&':
                out.append("&amp;");
                break;
            default:
                if(ch<' ' && ch!='\t' && ch!='\r' && ch!='\n') {
                    // skip the character
                } else {
                    out.append(ch);
                }
        }
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="XML Attributes">
    /**
     * Escapes for use in a XML attribute and writes to the provided <code>Appendable</code>.
     * Any characters less than 0x1f that are not \t, \r, or \n are completely filtered.
     *
     * @param S the string to be escaped.  If S is <code>null</code>, nothing is written.
     *
     * @see  #encodeXml(CharSequence, Appendable)
     */
    public static void encodeXmlAttribute(CharSequence S, Appendable out) throws IOException {
        if (S != null) {
            int len = S.length();
            int toPrint = 0;
            for (int c = 0; c < len; c++) {
                char ch = S.charAt(c);
                switch(ch) {
                    case '<':
                        if(toPrint>0) {
                            out.append(S, c-toPrint, c);
                            toPrint=0;
                        }
                        out.append("&lt;");
                        break;
                    case '>':
                        if(toPrint>0) {
                            out.append(S, c-toPrint, c);
                            toPrint=0;
                        }
                        out.append("&gt;");
                        break;
                    case '&':
                        if(toPrint>0) {
                            out.append(S, c-toPrint, c);
                            toPrint=0;
                        }
                        out.append("&amp;");
                        break;
                    case '\'':
                        if(toPrint>0) {
                            out.append(S, c-toPrint, c);
                            toPrint=0;
                        }
                        out.append("&#39;");
                        break;
                    case '"':
                        if(toPrint>0) {
                            out.append(S, c-toPrint, c);
                            toPrint=0;
                        }
                        out.append("&quot;");
                        break;
                    case '\t':
                        if(toPrint>0) {
                            out.append(S, c-toPrint, c);
                            toPrint=0;
                        }
                        out.append("&#9;");
                        break;
                    case '\r':
                        if(toPrint>0) {
                            out.append(S, c-toPrint, c);
                            toPrint=0;
                        }
                        out.append("&#xD;");
                        break;
                    case '\n':
                        if(toPrint>0) {
                            out.append(S, c-toPrint, c);
                            toPrint=0;
                        }
                        out.append("&#xA;");
                        break;
                    default:
                        if(ch<' ') {
                            if(toPrint>0) {
                                out.append(S, c-toPrint, c);
                                toPrint=0;
                            }
                            // skip the character
                        } else {
                            toPrint++;
                        }
                }
            }
            if(toPrint>0) {
                out.append(S, len-toPrint, len);
            }
        }
    }

    /**
     * @see #encodeXmlAttribute(CharSequence, Appendable)
     *
     * @param S the string to be escaped.
     *
     * @return if S is null then null otherwise value escaped
     */
    public static String encodeXmlAttribute(CharSequence S) throws IOException {
        if(S==null) return null;
        StringBuilder result = new StringBuilder(S.length());
        encodeXmlAttribute(S, result);
        return result.toString();
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="(X)HTML">
    /**
     * Escapes for use in a (X)HTML document and writes to the provided <code>Appendable</code>.
     * In addition to the standard XML Body encoding, it turns newlines into &lt;br /&gt;, tabs to &amp;#x9;, and spaces to &amp;#160;
     *
     * @param S the string to be escaped.  If S is <code>null</code>, nothing is written.
     */
    public static void encodeHtml(CharSequence S, Appendable out) throws IOException {
        encodeHtml(S, true, true, out);
    }

    /**
     * Escapes for use in a (X)HTML document and writes to the provided <code>Appendable</code>.
     * In addition to the standard XML Body encoding, it turns newlines into &lt;br /&gt; and spaces to &amp;#160;
     *
     * @param S the string to be escaped.  If S is <code>null</code>, nothing is written.
     */
    public static void encodeHtml(CharSequence S, int start, int end, Appendable out) throws IOException {
        encodeHtml(S, start, end, true, true, out);
    }

    /**
     * @see #encodeHtml(CharSequence, Appendable)
     */
    public static String encodeHtml(CharSequence S) throws IOException {
        if(S==null) return null;
        StringBuilder result = new StringBuilder(S.length());
        encodeHtml(S, result);
        return result.toString();
    }

    /**
     * @see #encodeHtml(CharSequence, Appendable)
     */
    public static void encodeHtml(char ch, Appendable out) throws IOException {
        encodeHtml(ch, true, true, out);
    }

    /**
     * @see #encodeHtml(java.lang.CharSequence, int, int, boolean, boolean, java.lang.Appendable)
     */
    public static void encodeHtml(CharSequence S, boolean make_br, boolean make_nbsp, Appendable out) throws IOException {
        if(S!=null) encodeHtml(S, 0, S.length(), make_br, make_nbsp, out);
    }

    /**
     * Escapes for use in a (X)HTML document and writes to the provided <code>Appendable</code>.
     * Optionally, it turns newlines into &lt;br /&gt; and spaces to &amp;#160;
     * Any characters less than 0x1f that are not \t, \r, or \n are completely filtered.
     *
     * @param S the string to be escaped.  If S is <code>null</code>, nothing is written.
     * @param make_br  will write &lt;br /&gt; tags for every newline character
     * @param make_nbsp  will write &amp;#160; for a space when another space follows
     */
    public static void encodeHtml(CharSequence S, int start, int end, boolean make_br, boolean make_nbsp, Appendable out) throws IOException {
        if (S != null) {
            int toPrint = 0;
            for (int c = start; c < end; c++) {
                char ch = S.charAt(c);
                switch(ch) {
                    // Standard XML escapes
                    case '<':
                        if(toPrint>0) {
                            out.append(S, c-toPrint, c);
                            toPrint=0;
                        }
                        out.append("&lt;");
                        break;
                    case '>':
                        if(toPrint>0) {
                            out.append(S, c-toPrint, c);
                            toPrint=0;
                        }
                        out.append("&gt;");
                        break;
                    case '&':
                        if(toPrint>0) {
                            out.append(S, c-toPrint, c);
                            toPrint=0;
                        }
                        out.append("&amp;");
                        break;
                    // Special (X)HTML options
                    case ' ':
                        if(make_nbsp) {
                            if(toPrint>0) {
                                out.append(S, c-toPrint, c);
                                toPrint=0;
                            }
                            out.append("&#160;");
                        } else {
                            toPrint++;
                        }
                        break;
                    case '\t':
                        if(toPrint>0) {
                            out.append(S, c-toPrint, c);
                            toPrint=0;
                        }
                        out.append("&#9;");
                        break;
                    case '\r':
                        if(toPrint>0) {
                            out.append(S, c-toPrint, c);
                            toPrint=0;
                        }
                        // skip '\r'
                        break;
                    case '\n':
                        if(make_br) {
                            if(toPrint>0) {
                                out.append(S, c-toPrint, c);
                                toPrint=0;
                            }
                            out.append("<br />\n");
                        } else {
                            toPrint++;
                        }
                        break;
                    default:
                        if(ch<' ') {
                            if(toPrint>0) {
                                out.append(S, c-toPrint, c);
                                toPrint=0;
                            }
                            // skip the character
                        } else {
                            toPrint++;
                        }
                }
            }
            if(toPrint>0) out.append(S, end-toPrint, end);
        }
    }

    /**
     * @see #encodeHtml(CharSequence, boolean, boolean, Appendable)
     *
     * @param S the string to be escaped.
     *
     * @return if S is null then null otherwise value escaped
     */
    public static String encodeHtml(CharSequence S, boolean make_br, boolean make_nbsp) throws IOException {
        if(S==null) return null;
        StringBuilder result = new StringBuilder(S.length());
        encodeHtml(S, make_br, make_nbsp, result);
        return result.toString();
    }

    /**
     * @see #encodeHtml(CharSequence, boolean, boolean, Appendable)
     */
    public static void encodeHtml(char ch, boolean make_br, boolean make_nbsp, Appendable out) throws IOException {
        switch(ch) {
            // Standard XML escapes
            case '<':
                out.append("&lt;");
                break;
            case '>':
                out.append("&gt;");
                break;
            case '&':
                out.append("&amp;");
                break;
            // Special (X)HTML options
            case ' ':
                if(make_nbsp) {
                    out.append("&#160;");
                } else {
                    out.append(' ');
                }
                break;
            case '\t':
                out.append("&#9;");
                break;
            case '\r':
                // skip '\r'
                break;
            case '\n':
                if(make_br) {
                    out.append(BR_EOL);
                } else {
                    out.append('\n');
                }
                break;
            default:
                if(ch<' ') {
                    // skip the character
                } else {
                    out.append(ch);
                }
        }
    }
    // </editor-fold>

    /**
     * Escapes the specified <code>CharSequence</code> so that it can be put in a JavaScript string in
     * a XML CDATA area.  The string should be in double quotes, the quotes are not provided by
     * this call.
     *
     * Writes to the provided <code>Appendable</code>.
     *
     * @param S the string to be escaped.
     */
    // TODO: Review
    public static void encodeJavaScriptString(CharSequence S, Appendable out) throws IOException {
        if (S != null) {
            int len = S.length();
            for (int c = 0; c < len; c++) {
                char ch = S.charAt(c);
                if (ch == '"') out.append("\\\"");
                else if (ch == '\'') out.append("\\'");
                else if (ch == '\\') out.append("\\\\");
                else if (ch == '\b') out.append("\\b");
                else if (ch == '\f') out.append("\\f");
                else if (ch == '\r') out.append("\\r");
                else if (ch == '\n') out.append("\\n");
                else if (ch == '\t') out.append("\\t");
                // Also escape any < > & and ] to avoid early end of XHTML CDATA sections
                // and to not allow data to interfere with the XML parser.
                else if (ch == '<') out.append("\\u003c");
                else if (ch == '>') out.append("\\u003e");
                else if (ch == '&') out.append("\\u0026");
                else if (ch == ']') out.append("\\u005d");
                else if (ch<' ') {
                    out.append("\\u00");
                    int chInt = ch;
                    out.append(getHex(chInt>>>4));
                    out.append(getHex(chInt));
                } else {
                    out.append(ch);
                }
            }
        }
    }

    /**
     * @see #encodeJavaScriptString(CharSequence, Appendable)
     *
     * @param S the string to be escaped.
     *
     * @return if S is null then null otherwise value escaped
     */
    public static String encodeJavaScriptString(CharSequence S) throws IOException {
        if(S==null) return null;
        StringBuilder result = new StringBuilder(S.length());
        encodeJavaScriptString(S, result);
        return result.toString();
    }

    /**
     * Escapes the specified <code>CharSequence</code> so that it can be put in a JavaScript string in
     * an HTML attribute.  The string should be in JavaScript quotes, the quotes
     * are not provided by this call.
     *
     * Writes to the provided <code>Appendable</code>.
     *
     * @param S the string to be escaped.
     */
    // TODO: Review - combine with encodeJavaScriptString without in XML attribute?
    public static void encodeJavaScriptStringInXmlAttribute(CharSequence S, Appendable out) throws IOException {
        if (S != null) {
            int len = S.length();
            for (int c = 0; c < len; c++) {
                char ch = S.charAt(c);
                if (ch == '"') out.append("\\&quot;");
                else if (ch == '\'') out.append("\\&#39;");
                else if (ch == '\\') out.append("\\\\");
                else if (ch == '\b') out.append("\\b");
                else if (ch == '\f') out.append("\\f");
                else if (ch == '\r') out.append("\\r");
                else if (ch == '\n') out.append("\\n");
                else if (ch == '\t') out.append("\\t");
                else if (ch == ' ') out.append("\\u0020");
                // Also escape any < > & and ] to avoid early end of XHTML CDATA sections
                // and to not allow data to interfere with the XML parser.
                else if (ch == '<') out.append("\\u003c");
                else if (ch == '>') out.append("\\u003e");
                else if (ch == '&') out.append("\\u0026");
                else if (ch == ']') out.append("\\u005d");
                else if (ch<' ') {
                    out.append("\\u00");
                    int chInt = ch;
                    out.append(getHex(chInt>>>4));
                    out.append(getHex(chInt));
                } else {
                    out.append(ch);
                }
            }
        }
    }

    /**
     * @see #encodeJavaScriptStringInXmlAttribute(CharSequence, Appendable)
     *
     * @param S the string to be escaped.
     *
     * @return if S is null then null otherwise value escaped to be a JavaScript string in a XML attribute.
     */
    public static String encodeJavaScriptStringInXmlAttribute(CharSequence S) throws IOException {
        if(S==null) return null;
        StringBuilder result = new StringBuilder(S.length());
        encodeJavaScriptStringInXmlAttribute(S, result);
        return result.toString();
    }
}
