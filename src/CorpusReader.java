import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class CorpusReader 
{
    final static String CNTFILE_LOC = "samplecnt.txt";
    final static String VOCFILE_LOC = "samplevoc.txt";
    
    private HashMap<String,Integer> ngrams;
    private Set<String> vocabulary;
    
    private double N; //Ngrams we have seen
    private Map<Integer,Double> Nc = 
            new HashMap<Integer,Double>(); //Amount of Ngrams we have seen c times
    int GTSThreshold; //First not occuring count in Nc
        
    public CorpusReader() throws IOException
    {  
        readNGrams();
        readVocabulary();
        
        //Find first not occuring count in Nc
        int i = 1;
        while(i < Nc.size()){
            if(!Nc.containsKey(i)){
                GTSThreshold = i - 1;
                break;
            }
            i++;
        }
    }
    
    /**
     * Returns the n-gram count of <NGram> in the file
     * 
     * 
     * @param nGram : space-separated list of words, e.g. "adopted by him"
     * @return 0 if <NGram> cannot be found, 
     * otherwise count of <NGram> in file
     */
     public int getNGramCount(String nGram) throws  NumberFormatException
    {
        if(nGram == null || nGram.length() == 0)
        {
            throw new IllegalArgumentException("NGram must be non-empty.");
        }
        Integer value = ngrams.get(nGram);
        return value==null?0:value;
    }
    
    private void readNGrams() throws 
            FileNotFoundException, IOException, NumberFormatException
    {
        ngrams = new HashMap<>();

        FileInputStream fis;
        fis = new FileInputStream(CNTFILE_LOC);
        BufferedReader in = new BufferedReader(new InputStreamReader(fis));

        while (in.ready()) {
            String phrase = in.readLine().trim();
            String s1, s2;
            int j = phrase.indexOf(" ");

            s1 = phrase.substring(0, j);
            s2 = phrase.substring(j + 1, phrase.length());

            int count = 0;
            try {
                count = Integer.parseInt(s1);
                ngrams.put(s2, count);
                
                Nc.put(count, Nc.getOrDefault(count, 0.0) + 1); 
                //Increase things we have seen "count" times
                N++; //Increase things seen
            } catch (NumberFormatException nfe) {
                throw new NumberFormatException("NumberformatError: " + s1);
            }
        }
    }
    
    
    private void readVocabulary() throws FileNotFoundException, IOException {
        vocabulary = new HashSet<>();
        
        FileInputStream fis = new FileInputStream(VOCFILE_LOC);
        BufferedReader in = new BufferedReader(new InputStreamReader(fis));
        
        while(in.ready())
        {
            String line = in.readLine();
            vocabulary.add(line);
        }
    }
    
    /**
     * Returns the size of the number of unique words in the dataset
     * 
     * @return the size of the number of unique words in the dataset
     */
    public int getVocabularySize() 
    {
        return vocabulary.size();
    }
    
    /**
     * Returns the subset of words in set that are in the vocabulary
     * 
     * @param set
     * @return 
     */
    public HashSet<String> inVocabulary(Set<String> set) 
    {
        HashSet<String> h = new HashSet<>(set);
        h.retainAll(vocabulary);
        return h;
    }
    
    public boolean inVocabulary(String word) 
    {
       return vocabulary.contains(word);
    }    
    
    public double getSmoothedCount(String NGram)
    {
        if(NGram == null || NGram.length() == 0)
        {
            throw new IllegalArgumentException("NGram must be non-empty.");
        }
        
        double smoothedCount = 0.0;
        
        List<String> words = Arrays.asList(NGram.split(" "));
        int count = getNGramCount(NGram);
        
        //if (words.size() == 1) {
            //Good-Turing Smoothing
            if (count == 0) {
                smoothedCount = Nc.get(1) / N;
            } else if (count < GTSThreshold) {
                smoothedCount = (double)(count + 1) * Nc.getOrDefault(count + 1, 0.0) / Nc.getOrDefault(count, 0.0) / N;
            } else { //Here Good-Turing Smoothing becomes unreliable
                smoothedCount = count / N;
            }
        /*} else if (words.size() == 2){
            //Kneser-Ney Smoothing
            if (count == 0) {
                smoothedCount = Nc.get(1) / N;
            } else {
                smoothedCount = (count-0.75) - getNGramCount(words.get(0)) + getSmoothedCount(words.get(1));
            }
        }*/
        
        
        return smoothedCount;        
    }
}
