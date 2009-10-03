/*
 * aocode-public - Reusable Java library of general tools with minimal external dependencies.
 * Copyright (C) 2008, 2009  AO Industries, Inc.
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
package com.aoindustries.util.persistent;

import com.aoindustries.sql.SQLUtility;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * @author  AO Industries, Inc.
 */
public class BlockBufferTinyBitmapFixedTest extends BlockBufferTestParent {

    public static Test suite() {
        TestSuite suite = new TestSuite(BlockBufferTinyBitmapFixedTest.class);
        return suite;
    }

    public BlockBufferTinyBitmapFixedTest(String testName) {
        super(testName);
    }

    public PersistentBlockBuffer getBlockBuffer() throws IOException {
        return new FixedPersistentBlockBuffer(new MappedPersistentBuffer(), 1);
    }

    @Override
    public long getAllocationSize(Random random) throws IOException {
        return random.nextBoolean() ? 1 : 0;
    }

    public void testAllocateOneMillion() throws Exception {
        PersistentBlockBuffer blockBuffer = getBlockBuffer();
        try {
            for(int c=0;c<1000000;c++) blockBuffer.allocate(1);
        } finally {
            blockBuffer.close();
        }
    }

    public void testAllocateDeallocateTenMillion() throws Exception {
        PersistentBlockBuffer blockBuffer = getBlockBuffer();
        try {
            final int numAdd = 10000000;
            List<Long> ids = new ArrayList<Long>(numAdd);
            long startNanos = System.nanoTime();
            for(int c=0;c<numAdd;c++) ids.add(blockBuffer.allocate(1));
            long endNanos = System.nanoTime();
            System.out.println("BlockBufferTinyBitmapFixedTest: testAllocateDeallocateOneMillion: Allocating "+numAdd+" blocks in "+SQLUtility.getMilliDecimal((endNanos-startNanos)/1000)+" ms");
            //System.out.println("BlockBufferTinyBitmapFixedTest: testAllocateDeallocateOneMillion: Getting "+numAdd+" ids.");
            //Iterator<Long> iter = blockBuffer.iterateBlockIds();
            //int count = 0;
            //while(iter.hasNext()) {
            //    ids.add(iter.next());
            //    count++;
            //}
            //assertEquals(numAdd, count);
            long deallocCount = 0;
            long deallocTime = 0;
            long allocCount = 0;
            long allocTime = 0;
            for(int c=0;c<100;c++) {
                // Remove random items
                int numRemove = random.nextInt(Math.min(10000, ids.size()));
                List<Long> removeList = new ArrayList<Long>(numRemove);
                for(int d=0;d<numRemove;d++) {
                    int index = random.nextInt(ids.size());
                    removeList.add(ids.get(index));
                    ids.set(index, ids.get(ids.size()-1));
                    ids.remove(ids.size()-1);
                }
                //System.out.println("BlockBufferTinyBitmapFixedTest: testAllocateDeallocateOneMillion: Shuffling.");
                //Collections.shuffle(ids, new Random(random.nextLong()));
                startNanos = System.nanoTime();
                for(Long id : removeList) blockBuffer.deallocate(id);
                deallocCount += numRemove;
                deallocTime += System.nanoTime() - startNanos;
                int numAddBack = random.nextInt(10000);
                startNanos = System.nanoTime();
                for(int d=0;d<numAddBack;d++) ids.add(blockBuffer.allocate(1));
                allocCount += numAddBack;
                allocTime += System.nanoTime() - startNanos;
            }
            System.out.println("BlockBufferTinyBitmapFixedTest: testAllocateDeallocateOneMillion: Deallocated "+deallocCount+" blocks in "+SQLUtility.getMilliDecimal(deallocTime/1000)+" ms");
            System.out.println("BlockBufferTinyBitmapFixedTest: testAllocateDeallocateOneMillion: Allocated "+allocCount+" blocks in "+SQLUtility.getMilliDecimal(allocTime/1000)+" ms");
        } finally {
            blockBuffer.close();
        }
    }
}