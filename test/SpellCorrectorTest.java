/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
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
public class SpellCorrectorTest {
    
    public SpellCorrectorTest() {
    }
    
    @BeforeClass
    public static void setUpClass() {
    }
    
    @AfterClass
    public static void tearDownClass() {
    }
    
    @Before
    public void setUp() {
    }
    
    @After
    public void tearDown() {
    }

    /**
     * Test of correctPhrase method, of class SpellCorrector.
     */
    @Test
    public void testCorrectPhrase() {
        System.out.println("correctPhrase");
        String phrase = "";
        SpellCorrector instance = null;
        String expResult = "";
        String result = instance.correctPhrase(phrase);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of getCandidateWordsChannel method, of class SpellCorrector.
     */
    @Test
    public void testGetCandidateWordsChannel() throws IOException {
        System.out.println("getCandidateWordsChannel");
        String word = "develoopment";
        ConfusionMatrixReader cmr = new ConfusionMatrixReader();
        SpellCorrector instance = new SpellCorrector(new CorpusReader(), cmr);
        HashMap<String, Double> expResult = new HashMap<String,Double>();
        expResult.put("development", (double)cmr.getConfusionCount("lo", "l"));
        expResult.put(word, 0.95);
        HashMap<String, Double> result = instance.getCandidateWordsChannel(word);
        assertEquals(expResult, result);
    }

    /**
     * Test of calculateChannelModelProbability method, of class SpellCorrector.
     */
    @Test
    public void testCalculateChannelModelProbability() throws IOException {
        System.out.println("calculateChannelModelProbability");
        String suggested = "development";
        String incorrect = "edvelopment";
        ConfusionMatrixReader cmr = new ConfusionMatrixReader();
        SpellCorrector instance = new SpellCorrector(new CorpusReader(), cmr);
        double expResult = cmr.getConfusionCount("ed", "de");
        double result = instance.calculateChannelModelProbability(suggested, incorrect);
        assertEquals(expResult, result, 0.0);
    }

    /**
     * Test of getCandidateWords method, of class SpellCorrector.
     */
    @Test
    public void testGetCandidateWords() throws IOException {
        System.out.println("getCandidateWords");
        String word = "home";
        SpellCorrector instance = new SpellCorrector(new CorpusReader(), new ConfusionMatrixReader());
        HashSet<String> expResult = new HashSet<String>();
        expResult.add("development");
        HashSet<String> result = instance.getCandidateWords(word);
        assertEquals(expResult, result);
    }
    
}
