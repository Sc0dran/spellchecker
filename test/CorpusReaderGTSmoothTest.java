/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author s132303
 */
public class CorpusReaderGTSmoothTest {
    
    /**
     * Test of getSmoothedCount method, of class CorpusReader.
     */
    @Test
    public void testGetSmoothedCount() throws IOException {
        System.out.println("getSmoothedCount");
        String NGram = "the some";
        CorpusReaderGTSmooth instance = new CorpusReaderGTSmooth();
        double expResult = 0.0;
        double result = instance.getSmoothedCount(NGram);
        assertEquals(expResult, result, 0.0);
    }
    
}
