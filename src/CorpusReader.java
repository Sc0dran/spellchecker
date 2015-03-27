
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author s132303
 */
public class CorpusReader {
    static final String CNTFILE_LOC = "samplecnt.txt";
    static final String VOCFILE_LOC = "samplevoc.txt";
    protected HashMap<String, Integer> ngrams;
    protected Set<String> vocabulary;
    
    private int unigramN;
    private double k = 1;

    public CorpusReader() {
    }

    /**
     * Returns the n-gram count of <NGram> in the file
     *
     *
     * @param nGram : space-separated list of words, e.g. "adopted by him"
     * @return 0 if <NGram> cannot be found,
     * otherwise count of <NGram> in file
     */
    public int getNGramCount(String nGram) throws NumberFormatException {
        if (nGram == null || nGram.length() == 0) {
            throw new IllegalArgumentException("NGram must be non-empty.");
        }
        Integer value = ngrams.get(nGram);
        return value == null ? 0 : value;
    }

    protected void readNGrams() throws FileNotFoundException, IOException, NumberFormatException {
        ngrams = new HashMap<>();
        FileInputStream fis;
        fis = new FileInputStream(CNTFILE_LOC);
        BufferedReader in = new BufferedReader(new InputStreamReader(fis));
        while (in.ready()) {
            String phrase = in.readLine().trim();
            String s1;
            String s2;
            int j = phrase.indexOf(" ");
            s1 = phrase.substring(0, j);
            s2 = phrase.substring(j + 1, phrase.length());
            int count = 0;
            try {
                count = Integer.parseInt(s1);
                ngrams.put(s2, count);
                if (s2.split(" ").length == 1) {
                    //Increase unigrams we have seen
                    unigramN += count;
                }
            } catch (NumberFormatException nfe) {
                throw new NumberFormatException("NumberformatError: " + s1);
            }
        }
    }

    protected void readVocabulary() throws FileNotFoundException, IOException {
        vocabulary = new HashSet<>();
        FileInputStream fis = new FileInputStream(VOCFILE_LOC);
        BufferedReader in = new BufferedReader(new InputStreamReader(fis));
        while (in.ready()) {
            String line = in.readLine();
            vocabulary.add(line);
        }
    }

    /**
     * Returns the size of the number of unique words in the dataset
     *
     * @return the size of the number of unique words in the dataset
     */
    public int getVocabularySize() {
        return vocabulary.size();
    }

    /**
     * Returns the subset of words in set that are in the vocabulary
     *
     * @param set
     * @return
     */
    public HashSet<String> inVocabulary(Set<String> set) {
        HashSet<String> h = new HashSet<>(set);
        h.retainAll(vocabulary);
        return h;
    }

    public boolean inVocabulary(String word) {
        return vocabulary.contains(word);
    }
    
    //Default implementation is add one
    public double getSmoothedCount(String NGram)
    {
        if(NGram == null || NGram.length() == 0)
        {
            throw new IllegalArgumentException("NGram must be non-empty.");
        }
        
        double smoothedCount = 0.0;
        
        List<String> words = Arrays.asList(NGram.split(" "));
        int count = getNGramCount(NGram);
        
        //Good-Turing Smoothing
        if (words.size() == 1) { //Unigrams
            smoothedCount = (double)(count + k) / (unigramN + (k*getVocabularySize()));
        } else if (words.size() == 2){ //Bigrams
            smoothedCount = ((double)count + k) 
                        / (getNGramCount(words.get(0)) + (k*getVocabularySize()));
        }
        
        return smoothedCount;        
    }
}
