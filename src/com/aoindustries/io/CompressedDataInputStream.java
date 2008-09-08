package com.aoindustries.io;

/*
 * Copyright 2003-2008 by AO Industries, Inc.,
 * 7262 Bull Pen Cir, Mobile, Alabama, 36695, U.S.A.
 * All rights reserved.
 */
import java.io.*;

/**
 * Adds compressed data transfer to DataInputStream.
 *
 * @author  AO Industries, Inc.
 */
public class CompressedDataInputStream extends DataInputStream {
    
    public CompressedDataInputStream(InputStream in) {
        super(in);
        for(int c=0;c<64;c++) {
            lastStrings[c]="";
            lastCommonLengths[c]=0;
        }
    }

    /**
     * Reads a compressed integer from the stream.
     *
     * The 31 bit pattern is as follows:
     * <pre>
     * 5 bit   - 000SXXXX
     * 13 bit  - 001SXXXX XXXXXXXX
     * 22 bit  - 01SXXXXX XXXXXXXX XXXXXXXX
     * 31 bit  - 1SXXXXXX XXXXXXXX XXXXXXXX XXXXXXXX
     * </pre>
     *
     * @exception  EOFException if the end of file is reached
     */
    public int readCompressedInt() throws IOException {
        int b1=in.read();
        if(b1==-1) throw new EOFException();
        if((b1&0x80)!=0) {
            // 31 bit
            int b2=in.read();
            if(b2==-1) throw new EOFException();
            int b3=in.read();
            if(b3==-1) throw new EOFException();
            int b4=in.read();
            if(b4==-1) throw new EOFException();
            return
                ((b1&0x40)==0 ? 0 : 0xc0000000)
                | ((b1&0x3f)<<24)
                | (b2<<16)
                | (b3<<8)
                | b4
            ;
        } else if((b1&0x40)!=0) {
            // 22 bit
            int b2=in.read();
            if(b2==-1) throw new EOFException();
            int b3=in.read();
            if(b3==-1) throw new EOFException();
            return
                ((b1&0x20)==0 ? 0 : 0xffe00000)
                | ((b1&0x1f)<<16)
                | (b2<<8)
                | b3
            ;
        } else if((b1&0x20)!=0) {
            // 13 bit
            int b2=in.read();
            if(b2==-1) throw new EOFException();
            return
                ((b1&0x10)==0 ? 0 : 0xfffff000)
                | ((b1&0x0f)<<8)
                | b2
            ;
        } else {
            // 5 bit
            return
                ((b1&0x10)==0 ? 0 : 0xfffffff0)
                | (b1&0x0f)
            ;
        }
    }
    
    private final String[] lastStrings=new String[64];
    private final int[] lastCommonLengths=new int[64];

    /**
     * @exception  EOFException if the end of file is reached
     */
    public String readCompressedUTF() throws IOException {
        synchronized(this) {
            int b1=in.read();
            if(b1==-1) throw new EOFException();
            int slot=b1&0x3f;

            // Is there a difference to the common
            if((b1&0x80)!=0) {
                int diff=readCompressedInt();
                if(diff>=0) diff++;
                lastCommonLengths[slot]+=diff;
            };

            // Is there a suffix String
            int common=lastCommonLengths[slot];
            if((b1&0x40)!=0) {
                String suffix=readUTF();
                if(common==0) return lastStrings[slot]=suffix;
                else return lastStrings[slot]=lastStrings[slot].substring(0, common)+suffix;
            } else {
                String last=lastStrings[slot];
                if(common==last.length()) return last;
                else return lastStrings[slot]=lastStrings[slot].substring(0, common);
            }
        }
    }

    public String readNullUTF() throws IOException {
        return readBoolean() ? readUTF() : null;
    }
}
