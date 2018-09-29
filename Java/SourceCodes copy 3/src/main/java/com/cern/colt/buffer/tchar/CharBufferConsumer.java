/*
Copyright (C) 1999 CERN - European Organization for Nuclear Research.
Permission to use, copy, modify, distribute and sell this software and its documentation for any purpose 
is hereby granted without fee, provided that the above copyright notice appear in all copies and 
that both that copyright notice and this permission notice appear in supporting documentation. 
CERN makes no representations about the suitability of this software for any purpose. 
It is provided "as is" without expressed or implied warranty.
 */
package com.cern.colt.buffer.tchar;

import com.cern.colt.list.tchar.CharArrayList;

/**
 * Target of a streaming <tt>CharBuffer</tt> into which data is flushed upon
 * buffer overflow.
 * 
 * @author wolfgang.hoschek@cern.ch
 * @version 1.0, 09/24/99
 * @author Piotr Wendykier (piotr.wendykier@gmail.com)
 */
public interface CharBufferConsumer {
    /**
     * Adds all elements of the specified list to the receiver.
     * 
     * @param list
     *            the list of which all elements shall be added.
     */
    public void addAllOf(CharArrayList list);
}
