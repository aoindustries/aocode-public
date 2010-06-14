/*
 * aocode-public - Reusable Java library of general tools with minimal external dependencies.
 * Copyright (C) 2003, 2004, 2005, 2006, 2007, 2008, 2009, 2010  AO Industries, Inc.
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
package com.aoindustries.util;

/**
 * @author  AO Industries, Inc.
 */
public class IntegerFilter extends DataFilter {

    public IntegerFilter(String expression) {
        super(expression);
    }
    
    public boolean matches(Object O) {
        if(O==null) return false;
        Integer I=(Integer)O;
        switch(function) {
            case LIKE: return I.toString().indexOf(value.toLowerCase())!=-1;
            case NOT_LIKE: return I.toString().indexOf(value.toLowerCase())==-1;
            case EQUALS: return I.intValue()==Integer.parseInt(value);
            case NOT_EQUALS: return I.intValue()!=Integer.parseInt(value);
            case GREATER_THAN: return I.intValue()>Integer.parseInt(value);
            case GREATER_THAN_OR_EQUAL: return I.intValue()>=Integer.parseInt(value);
            case LESS_THAN: return I.intValue()<Integer.parseInt(value);
            case LESS_THAN_OR_EQUAL: return I.intValue()<=Integer.parseInt(value);
            default: throw new RuntimeException("Unexpected function: "+function);
        }
    }
}