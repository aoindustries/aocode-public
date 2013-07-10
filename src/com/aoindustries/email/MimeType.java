/*
 * aocode-public - Reusable Java library of general tools with minimal external dependencies.
 * Copyright (C) 2003, 2004, 2005, 2006, 2007, 2008, 2009, 2010, 2011, 2013  AO Industries, Inc.
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
package com.aoindustries.email;

import java.util.*;

/**
 * Obtains MIME types for file names.
 *
 * @author  AO Industries, Inc.
 */
public class MimeType {

    public static final String DEFAULT_MIME_TYPE="unknown/unknown";

    private static final String[] types={
        "aif", "audio/x-aiff",
        "aifc", "audio/x-aiff",
        "aiff", "audio/x-aiff",
        "asc", "text/plain",
        "au", "audio/basic",
        "avi", "video/x-msvideo",
        "bin", "application/octet-stream",
        "bmp", "image/bmp",
        "c", "text/plain",
        "class", "application/octet-stream",
        "cpio", "application/x-cpio",
        "csh", "application/x-csh",
        "css", "text/css",
        "doc", "application/msword",
        "dvi", "application/x-dvi",
        "eps", "application/postscript",
        "exe", "application/octet-stream",
        "gif", "image/gif",
        "gtar", "application/x-gtar",
        "gz", "application/x-gzip",
        "h", "text/plain",
        "html", "text/html",
        "htm", "text/html",
        "jad", "text/vnd.sun.j2me.app-descriptor",
        "jar", "application/java-archive",
        "java", "text/plain",
        "jpeg", "image/jpeg",
        "jpe", "image/jpeg",
        "jpg", "image/jpeg",
        "js", "application/x-javascript",
        "latex", "application/x-latex",
        "log", "text/plain",
        "m3u", "audio/x-mpegurl",
        "man", "application/x-troff-man",
        "mid", "audio/midi",
        "midi", "audio/midi",
        "movie", "video/x-sgi-movie",
        "mov", "video/quicktime",
        "mpeg", "video/mpeg",
        "mpe", "video/mpeg",
        "mp2", "audio/mpeg",
        "mp3", "audio/mpeg",
        "mpga", "audio/mpeg",
        "mpg", "video/mpeg",
        "pbm", "image/x-portable-bitmap",
        "pdf", "application/pdf",
        "pid", "text/plain",
        "pgm", "image/x-portable-graymap",
        "png", "image/png",
        "pnm", "image/x-portable-anymap",
        "ppm", "image/x-portable-pixmap",
        "ppt", "application/vnd.ms-powerpoint",
        "properties", "text/plain",
        "ps", "application/postscript",
        "qt", "video/quicktime",
        "ra", "audio/x-realaudio",
        "ram", "audio/x-pn-realaudio",
        "rar", "application/x-rar-compressed",
        "ras", "image/x-cmu-raster",
        "rgb", "image/x-rgb",
        "rm", "audio/x-pn-realaudio",
        "rtf", "text/rtf",
        "rtx", "text/richtext",
        "sgml", "text/sgml",
        "sgm", "text/sgml",
        "sh", "application/x-sh",
        "sit", "application/x-stuffit",
        "snd", "audio/basic",
        "sql", "text/plain",
        "swf", "application/x-shockwave-flash",
        "tar", "application/x-tar",
        "tcl", "application/x-tcl",
        "tex", "application/x-tex",
        "texi", "application/x-texinfo",
        "texinfo", "application/x-texinfo",
        "tiff", "image/tiff",
        "tif", "image/tiff",
        "ts", "text/tab-separated-values",
        "txt", "text/plain",
        "vrml", "model/vrml",
        "wav", "audio/x-wav",
        "wmlc", "application/vnd.wap.wmlc",
        "wmlsc", "application/vnd.wap.wmlscriptc",
        "wmls", "text/vnd.wap.wmlscript",
        "wml", "text/vnd.wap.wml",
        "wrl", "model/vrml",
        "xbm", "image/x-xbitmap",
        "xls", "application/vnd.ms-excel",
        "xml", "text/xml",
        "xpm", "image/x-xpixmap",
        "xwd", "image/x-xwindowdump",
        "z", "application/x-compress",
        "zip", "application/zip"
    };
    private static final Map<String,String> hash = new HashMap<>();
    static {
        for(int c=0;c<types.length;c+=2) {
            String extension = types[c].toLowerCase(Locale.ENGLISH);
            if(hash.put(extension, types[c+1])!=null) throw new AssertionError(MimeType.class.getName()+": extension found more than once: "+extension);
        }
    }

    private MimeType() {}
    
    public static String getMimeType(String filename) {
        int pos=filename.lastIndexOf('.');
        if(pos!=-1) {
            String type=hash.get(filename.substring(pos+1).toLowerCase(Locale.ENGLISH));
            if(type!=null) return type;
        }
        return DEFAULT_MIME_TYPE;
    }
}