
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;


public class CorpusReaderAddKSmooth extends CorpusReader 
{
    private int unigramN;
    private double k = 1;
    public CorpusReaderAddKSmooth() throws IOException
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
                    //Increase bigrams we have seen
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
