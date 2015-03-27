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

public class CorpusReaderGTSmooth extends CorpusReader 
{
    
    private double unigramN; //Unigrams we have seen
    private Map<Integer,Double> unigramNc = 
            new HashMap<Integer,Double>(); //Amount of unigrams we have seen c times
    private int unigramThreshold; //First not occuring count in unigramNc
    
    private double bigramN; //Bigrams we have seen
    private Map<Integer,Double> bigramNc = 
            new HashMap<Integer,Double>(); //Amount of bigrams we have seen c times
    private int bigramThreshold; //First not occuring count in bigramNc
        
    public CorpusReaderGTSmooth() throws IOException
    {  
        readNGrams();
        readVocabulary();
        
        //Find first not occuring count in unigramNc
        int i = 1;
        while(i < unigramNc.size()){
            if(!unigramNc.containsKey(i)){
                unigramThreshold = i - 1;
                break;
            }
            i++;
        }
        //Find first not occuring count in unigramNc
        i = 1;
        while(i < bigramNc.size()){
            if(!bigramNc.containsKey(i)){
                bigramThreshold = i - 1;
                break;
            }
            i++;
        }
        System.out.println("unigramscount:" + unigramNc.toString());
        System.out.println("unigramsT:" + unigramThreshold);
        System.out.println("bigramscount:" + bigramNc.toString());
        System.out.println("bigramsT:" + bigramThreshold);
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
                    //Unigram
                    unigramNc.put(count, unigramNc.getOrDefault(count, 0.0) + 1);
                    //Increase unigrams we have seen
                    unigramN += count;
                } else {
                    //Bigram
                    bigramNc.put(count, bigramNc.getOrDefault(count, 0.0) + 1);
                    //Increase bigrams we have seen
                    bigramN += count;
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
            if (count == 0) {
                smoothedCount = unigramNc.get(1) / unigramN;
            } else if (count < unigramThreshold) {
                smoothedCount = (double)(count + 1) * unigramNc.get(count + 1) 
                            / unigramNc.get(count) 
                            / unigramN;
            } else { //Here Good-Turing Smoothing becomes unreliable
                smoothedCount = count / unigramN;
            }
        } else if (words.size() == 2){ //Bigrams
            if (count == 0) {
                smoothedCount = bigramNc.get(1) / bigramN;
            } else if (count < bigramThreshold) {
                smoothedCount = (double)(count + 1) * bigramNc.get(count + 1) 
                            / bigramNc.get(count) 
                            / (getSmoothedCount(words.get(0)) * unigramN);
            } else { //Here Good-Turing Smoothing becomes unreliable
                smoothedCount = count / (getSmoothedCount(words.get(0))*unigramN);
            }
        }
        
        return smoothedCount;        
    }
}
