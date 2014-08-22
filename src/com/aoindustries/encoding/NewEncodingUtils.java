/*
 * aocode-public - Reusable Java library of general tools with minimal external dependencies.
 * Copyright (C) 2009, 2010, 2011, 2012, 2013  AO Industries, Inc.
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
package com.aoindustries.encoding;

import static com.aoindustries.encoding.JavaScriptInXhtmlAttributeEncoder.encodeJavaScriptInXhtmlAttribute;
import static com.aoindustries.encoding.JavaScriptInXhtmlEncoder.encodeJavaScriptInXhtml;
import static com.aoindustries.encoding.TextInJavaScriptEncoder.encodeTextInJavaScript;
import com.aoindustries.util.StringUtility;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;

/**
 * Encoding helper utilities.
 * TODO: Rename to EncodingUtils once com.aoindustries.util.EncodingUtils has been eliminated.
 *
 * @author  AO Industries, Inc.
 */
public class NewEncodingUtils {

    private NewEncodingUtils() {
    }

    static final String EOL = System.getProperty("line.separator");
    static final String BR_EOL = "<br />"+EOL;

    static final char[] hexChars={'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f'};
    static char getHex(int value) {
        return hexChars[value & 15];
    }

    /**
     * The Strings are kept here after first created.
     */
	// 0x0 <= ch < 0x20
	private static final int ENCODE_RANGE_1_START = 0x0;
	private static final int ENCODE_RANGE_1_END   = 0x20;
    private static final String[] javaScriptUnicodeEscapeStrings1 = new String[ENCODE_RANGE_1_END - ENCODE_RANGE_1_START];
	// 0xD800 <= ch < 0xE000
	private static final int ENCODE_RANGE_2_START = 0xD800;
	private static final int ENCODE_RANGE_2_END   = 0xE000;
    private static final String[] javaScriptUnicodeEscapeStrings2 = new String[ENCODE_RANGE_2_END - ENCODE_RANGE_2_START];
	// 0xFFFE <= ch < 0x10000
	private static final int ENCODE_RANGE_3_START = 0xFFFE;
	private static final int ENCODE_RANGE_3_END   = 0x10000;
    private static final String[] javaScriptUnicodeEscapeStrings3 = new String[ENCODE_RANGE_3_END - ENCODE_RANGE_3_START];
    static {
		for(int ch=ENCODE_RANGE_1_START; ch<ENCODE_RANGE_1_END; ch++) {
			// Escape using JavaScript unicode escape.
			javaScriptUnicodeEscapeStrings1[ch - ENCODE_RANGE_1_START] = "\\u" + getHex(ch>>>12) + getHex(ch>>>8) + getHex(ch>>>4) + getHex(ch);
		}
		for(int ch=ENCODE_RANGE_2_START; ch<ENCODE_RANGE_2_END; ch++) {
			// Escape using JavaScript unicode escape.
			javaScriptUnicodeEscapeStrings2[ch - ENCODE_RANGE_2_START] = "\\u" + getHex(ch>>>12) + getHex(ch>>>8) + getHex(ch>>>4) + getHex(ch);
		}
		for(int ch=ENCODE_RANGE_3_START; ch<ENCODE_RANGE_3_END; ch++) {
			// Escape using JavaScript unicode escape.
			javaScriptUnicodeEscapeStrings3[ch - ENCODE_RANGE_3_START] = "\\u" + getHex(ch>>>12) + getHex(ch>>>8) + getHex(ch>>>4) + getHex(ch);
		}
    }

	/**
     * Gets the unicode escape for a JavaScript character or null if may be passed-through without escape.
     * @param ch
     * @return
     */
    static String getJavaScriptUnicodeEscapeString(char ch) {
		int chInt = (int)ch;
		if(chInt>=ENCODE_RANGE_1_START && chInt<ENCODE_RANGE_1_END) {
			return javaScriptUnicodeEscapeStrings1[chInt - ENCODE_RANGE_1_START];
		}
		if(chInt>=ENCODE_RANGE_2_START && chInt<ENCODE_RANGE_2_END) {
			return javaScriptUnicodeEscapeStrings2[chInt - ENCODE_RANGE_2_START];
		}
		if(chInt>=ENCODE_RANGE_3_START && chInt<ENCODE_RANGE_3_END) {
			return javaScriptUnicodeEscapeStrings3[chInt - ENCODE_RANGE_3_START];
		}
		// No encoding needed
		return null;
    }

    public static void encodeTextInJavaScriptInXhtml(String text, Appendable out) throws IOException {
        StringBuilder javascript = new StringBuilder(text.length());
        encodeTextInJavaScript(text, javascript);
        encodeJavaScriptInXhtml(javascript, out);
    }

    public static void encodeTextInJavaScriptInXhtmlAttribute(String text, Appendable out) throws IOException {
        StringBuilder javascript = new StringBuilder(text.length());
        encodeTextInJavaScript(text, javascript);
        encodeJavaScriptInXhtmlAttribute(javascript, out);
    }

    public static String getTextInJavaScriptInXhtmlAttribute(String text) throws IOException {
        StringBuilder xhtml = new StringBuilder(text.length());
        encodeTextInJavaScriptInXhtmlAttribute(text, xhtml);
        return xhtml.toString();
    }

    private static final char[] noEncodeCharacters = {
        '?', ':', '/', ';', '#', '+'
    };

    /**
     * UTF-8 encodes the URL up to the first ?, if present.  Does not encode
     * any characters in the set { '?', ':', '/', ';', '#', '+' }.
	 * 
	 * @see  #decodeUrlPath(java.lang.String) 
     */
    public static String encodeUrlPath(String href) throws UnsupportedEncodingException {
        int len = href.length();
        int pos = 0;
        StringBuilder SB = new StringBuilder(href.length()*2); // Leave a little room for encoding
        while(pos<len) {
            int nextPos = StringUtility.indexOf(href, noEncodeCharacters, pos);
            if(nextPos==-1) {
                SB.append(URLEncoder.encode(href.substring(pos, len), "UTF-8"));
                pos = len;
            } else {
                SB.append(URLEncoder.encode(href.substring(pos, nextPos), "UTF-8"));
                char nextChar = href.charAt(nextPos);
                if(nextChar=='?') {
                    // End encoding
                    SB.append(href, nextPos, len);
                    pos = len;
                } else {
                    SB.append(nextChar);
                    pos = nextPos+1;
                }
            }
        }
        return SB.toString();
    }

	/**
     * UTF-8 decodes the URL up to the first ?, if present.  Does not decode
     * any characters in the set { '?', ':', '/', ';', '#', '+' }.
	 * 
	 * @see  #encodeUrlPath(java.lang.String) 
     */
    public static String decodeUrlPath(String href) throws UnsupportedEncodingException {
        int len = href.length();
        int pos = 0;
        StringBuilder SB = new StringBuilder(href.length()*2); // Leave a little room for encoding
        while(pos<len) {
            int nextPos = StringUtility.indexOf(href, noEncodeCharacters, pos);
            if(nextPos==-1) {
                SB.append(URLDecoder.decode(href.substring(pos, len), "UTF-8"));
                pos = len;
            } else {
                SB.append(URLDecoder.decode(href.substring(pos, nextPos), "UTF-8"));
                char nextChar = href.charAt(nextPos);
                if(nextChar=='?') {
                    // End decoding
                    SB.append(href, nextPos, len);
                    pos = len;
                } else {
                    SB.append(nextChar);
                    pos = nextPos+1;
                }
            }
        }
        return SB.toString();
    }
}
