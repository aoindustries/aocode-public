/*
 * aocode-public - Reusable Java library of general tools with minimal external dependencies.
 * Copyright (C) 2009  AO Industries, Inc.
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

import java.io.IOException;
import java.io.Writer;
import java.util.Locale;

/**
 * Makes sure that all data going through this writer has the correct characters
 * for URI/URL data.
 *
 * @author  AO Industries, Inc.
 */
public class UrlValidator extends MediaValidator {

    /**
     * Checks one character, throws IOException if invalid.
     * @see java.net.URLEncoder
     */
    public static void checkCharacter(Locale userLocale, int c) throws IOException {
        if(
            (c<'a' || c>'z')
            && (c<'A' || c>'Z')
            && (c<'0' || c>'9')
            && c!='.'
            && c!='-'
            && c!='*'
            && c!='_'
            && c!='+' // converted space
            && c!='%' // encoded value
            // Other characters used outside the URL data
            && c!=':'
            && c!='/'
            && c!=';'
            && c!='?'
            && c!='='
            && c!='&'
            && c!='#'
        ) throw new IOException(ApplicationResourcesAccessor.getMessage(userLocale, "UrlMediaValidator.invalidCharacter", Integer.toHexString(c)));
    }

    /**
     * Checks a set of characters, throws IOException if invalid
     */
    public static void checkCharacters(Locale userLocale, char[] cbuf, int off, int len) throws IOException {
        int end = off + len;
        while(off<end) checkCharacter(userLocale, cbuf[off++]);
    }

    /**
     * Checks a set of characters, throws IOException if invalid
     */
    public static void checkCharacters(Locale userLocale, CharSequence str, int off, int end) throws IOException {
        while(off<end) checkCharacter(userLocale, str.charAt(off++));
    }

    private final Locale userLocale;

    protected UrlValidator(Writer out, Locale userLocale) {
        super(out);
        this.userLocale = userLocale;
    }

    public boolean isValidatingMediaInputType(MediaType inputType) {
        return
            inputType==MediaType.URL
            || inputType==MediaType.XHTML_ATTRIBUTE // All valid URL characters are also valid XHTML+ATTRIBUTE characters
            || inputType==MediaType.XHTML_PRE   // All valid URL characters are also valid XHTML+PRE characters
            || inputType==MediaType.XHTML       // All valid URL characters are also valid XHTML characters
            || inputType==MediaType.JAVASCRIPT  // No validation required
            || inputType==MediaType.TEXT        // No validation required
        ;
    }

    public MediaType getValidMediaOutputType() {
        return MediaType.URL;
    }

    @Override
    public void write(int c) throws IOException {
        checkCharacter(userLocale, c);
        out.write(c);
    }

    @Override
    public void write(char[] cbuf, int off, int len) throws IOException {
        checkCharacters(userLocale, cbuf, off, len);
        out.write(cbuf, off, len);
    }

    @Override
    public void write(String str, int off, int len) throws IOException {
        if(str==null) throw new IllegalArgumentException("str is null");
        checkCharacters(userLocale, str, off, off + len);
        out.write(str, off, len);
    }

    @Override
    public UrlValidator append(CharSequence csq) throws IOException {
        checkCharacters(userLocale, csq, 0, csq.length());
        out.append(csq);
        return this;
    }

    @Override
    public UrlValidator append(CharSequence csq, int start, int end) throws IOException {
        checkCharacters(userLocale, csq, start, end);
        out.append(csq, start, end);
        return this;
    }

    @Override
    public UrlValidator append(char c) throws IOException {
        checkCharacter(userLocale, c);
        out.append(c);
        return this;
    }
}