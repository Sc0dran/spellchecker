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

public class CorpusReaderKNSmooth extends CorpusReader 
{
    
    private double unigramN; //Unigrams we have seen
    
    double d = 0.75; //Bigram discount
    
    private double bigramTypes; //Bigram types we have seen
    private Map<String,Double> uniquePreceders = 
            new HashMap<String,Double>(); 
    //How many unique words precede String
    private Map<String,Double> uniqueFollowers = 
            new HashMap<String,Double>();
    //How many unique words follow String
        
    public CorpusReaderKNSmooth() throws IOException
    {  
        readNGrams();
        readVocabulary();
    }
    
    @Override
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
                } else {
                    //Bigram
                    String[] words = s2.split(" ");
                    uniquePreceders.put(words[1], uniquePreceders.getOrDefault(words[1], 0.0) + 1);
                    uniqueFollowers.put(words[0], uniqueFollowers.getOrDefault(words[0], 0.0) + 1);
                    //Increase bigrams we have seen
                    bigramTypes ++;
                }
            } catch (NumberFormatException nfe) {
                throw new NumberFormatException("NumberformatError: " + s1);
            }
        }
    }
    
    @Override
    public double getSmoothedCount(String NGram)
    {
        if(NGram == null || NGram.length() == 0)
        {
            throw new IllegalArgumentException("NGram must be non-empty.");
        }
        
        double smoothedCount = 0.0;
        
        List<String> words = Arrays.asList(NGram.split(" "));
        int count = getNGramCount(NGram);
        
        if (words.size() == 1) { //Unigrams
            //Add 1 Smoothing
            smoothedCount = (double)(count + 1) / (unigramN + getVocabularySize());
        } else if (words.size() == 2){ //Bigrams
            //Kneser-Ney Smoothing
            smoothedCount = ( Math.max(count-d, 0.0) 
                        / (getSmoothedCount(words.get(0)) * unigramN) )
                        + ( interpolationWeight(words.get(0)) * Pcontinuation(words.get(1)) );
        }
        
        return smoothedCount;        
    }
    
    //How likely is w to appear as a novel continuation
    private double Pcontinuation(String w) {
        return uniquePreceders.getOrDefault(w, 0.0) / bigramTypes;
    }
    
    //
    private double interpolationWeight(String w) {
        return (d / (getSmoothedCount(w) * unigramN)) * uniqueFollowers.getOrDefault(w, 0.0);
    }
}
