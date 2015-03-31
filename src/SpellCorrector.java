import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class SpellCorrector {
    final private CorpusReader cr;
    final private ConfusionMatrixReader cmr;
    
    final char[] ALPHABET = "abcdefghijklmnopqrstuvwxyz'".toCharArray();
    final double LAMBDA = 7.5;
    final double NO_ERROR = 0.95;
    final int MAX_EDITS = 2;
    
    private boolean[] corrected;
    
    public SpellCorrector(CorpusReader cr, ConfusionMatrixReader cmr) 
    {
        this.cr = cr;
        this.cmr = cmr;
    }
    
    final public String correctPhrase(String phrase) {
        if (phrase == null || phrase.length() == 0) {
            throw new IllegalArgumentException("phrase must be non-empty.");
        }
            
        String[] words = phrase.split(" ");
        String finalSuggestion = "";
        
        //Corrected array represents is the word at words[i] is corrected
        corrected = new boolean[words.length];
        
        //Get phrase with max probability of all sentences with MAX_EDITS edited words
        for (int i = 0; i < MAX_EDITS; i++) {
            words = getMaxPhrase(words);
        }
        
        for (String word : words) {
            finalSuggestion += word + " ";
        }
        return finalSuggestion.trim();
    }
    
    /*
    Returns a phrase with at most one edited words
    that has the highest probability of all possible sentences of one edit
    */
    final public String[] getMaxPhrase(String[] words) {
        double maxP = calculateNGramProbability(words);
        String[] maxPhrase = words;
        Integer wordEditIndex = null;
        
        //Loop over all words in the phrase and try candidate words if that word can be corrected
        for (int i = 0; i < words.length; i++) {
            if (!corrected[i]) { //Check if this word can be corrected
                double bestValue = Double.NEGATIVE_INFINITY;
                String bestWord = words[i];
                
                //Generate candidate words
                Map<String, Double> candidateWords = getCandidateWordsChannel(words[i]);
                //Loop over all candidate words
                for (String word : candidateWords.keySet()) {
                    //likelihood = P(x|word) * P(word) where x is the error
                    double likelihood = candidateWords.get(word);
                    double preNGramCount =
                            i == 0 ? 1 :
                            cr.getSmoothedCount(words[i - 1] + " " + word);
                    double postNGramCount =
                            i == (words.length - 1) ? 1 :
                            cr.getSmoothedCount(word + " " + words[i + 1]);
                    //prior for w1 = P(w0 w1) * P(w1 w2)
                    double prior =
                            preNGramCount * postNGramCount;
                    double wordProbability =
                            likelihood * Math.pow(prior, LAMBDA);
                    //Check if word has more probability than last best word
                    if (wordProbability > bestValue) {
                        bestWord = word;
                        bestValue = wordProbability;
                    }
                }
                
                //Change newPhrase if it has more probability than the old newPhrase
                String[] newPhrase = words.clone();
                newPhrase[i] = bestWord;
                if (maxP < calculateNGramProbability(newPhrase)) {
                    maxP = calculateNGramProbability(newPhrase);
                    maxPhrase = newPhrase;
                    wordEditIndex = i;
                }
            }
        }
        if (calculateNGramProbability(maxPhrase) - calculateNGramProbability(words) < NO_ERROR) {
            return words; //Return original phrase if the difference in probability is too low
        }
        //If any word has changed, the word and words around it cannot change again
        if (wordEditIndex != null){
            if (wordEditIndex!=0) 
                    corrected[wordEditIndex-1] = true;
            corrected[wordEditIndex] = true;
            if (wordEditIndex!=words.length-1) 
                    corrected[wordEditIndex+1] = true;
        }
        return maxPhrase;
    }
    
    /*
    *Returns the probability of a phrase
    */
    final public double calculateNGramProbability(String[] phrase) {
        double probability = 0.0;
        for (int i = 1; i < phrase.length; i++) {
            if (cr.inVocabulary(phrase[i - 1]) && cr.inVocabulary(phrase[i])) {
                probability += Math.log10(cr.getSmoothedCount(phrase[i - 1] + " " + phrase[i]) * Math.pow(10, 9));
            }
        }
        return probability;
    }
    
    /*
    *Returns candidate words with edit distance 1 and the probability of every word
    */
    final public HashMap<String,Double> getCandidateWordsChannel(String word) {
        
        HashMap<String, Double> MapOfWords = new HashMap<String, Double>();
        StringBuilder sb;
        
        //Add itself
        if (cr.inVocabulary(word)) {
            MapOfWords.put(word, NO_ERROR);
        }
        //Add deletions:
        for (int i = 0; i < word.length(); i++) {
            sb = new StringBuilder(word);
            sb.deleteCharAt(i);
            String newword = sb.toString();
            if (cr.inVocabulary(newword)) {
                String error = "";
                String correct = "";
                error += (i == 0 ? " " : word.charAt(i - 1));
                error += word.charAt(i);
                correct += (i == 0 ? " " : newword.charAt(i - 1));
                double value = cmr.getConfusionCount(error, correct) / cmr.getTotal() * cr.getSmoothedCount(newword);
                if (value == 0.0) {
                    value =  1.0 / cmr.getTotal() * cr.getSmoothedCount(newword);
                }
                MapOfWords.put(newword, value);
            }
        }
        //Add insertions:
        for (int i = 0; i <= word.length(); i++) {
            for (char c : ALPHABET) {
                sb = new StringBuilder(word);
                sb.insert(i, c);
                String newword = sb.toString();
                if (cr.inVocabulary(newword)) {
                    String error = "";
                    String correct = "";
                    error += (i == 0 ? " " : word.charAt(i - 1));
                    correct += (i == 0 ? " " : newword.charAt(i - 1));
                    correct += newword.charAt(i);
                    double value = cmr.getConfusionCount(error, correct) / cmr.getTotal() * cr.getSmoothedCount(newword);
                    if (value == 0.0) {
                        value =  1.0 / cmr.getTotal() * cr.getSmoothedCount(newword);
                    }
                    MapOfWords.put(newword, value);
                }
            }
        }
        //Add transpositions:
        if (word.length() >= 2) {
            for (int i = 0; i < word.length() - 1; i++) {
                sb = new StringBuilder(word);
                sb.setCharAt(i, word.charAt(i + 1));
                sb.setCharAt(i + 1, word.charAt(i));
                String newword = sb.toString();
                if (cr.inVocabulary(newword)) {
                    String error = "";
                    String correct = "";
                    error += word.charAt(i);
                    error += word.charAt(i + 1);
                    correct += word.charAt(i + 1);
                    correct += word.charAt(i);
                    double value = cmr.getConfusionCount(error, correct) / cmr.getTotal() * cr.getSmoothedCount(newword);
                    if (value == 0.0) {
                        value =  1.0 / cmr.getTotal() * cr.getSmoothedCount(newword);
                    }
                    MapOfWords.put(newword, value);
                }
            }
        }
        //Add substitutions:
        for (int i = 0; i < word.length(); i++) {
            for (char c : ALPHABET) {
                if (word.charAt(i) != c) {
                    sb = new StringBuilder(word);
                    sb.setCharAt(i, c);
                    String newword = sb.toString();
                    if (cr.inVocabulary(newword)) {
                        String error = "";
                        String correct = "";
                        error += word.charAt(i);
                        correct += c;
                        double value = cmr.getConfusionCount(error, correct) / cmr.getTotal() * cr.getSmoothedCount(newword);
                        if (value == 0.0) {
                            value =  1.0 / cmr.getTotal() * cr.getSmoothedCount(newword);
                        }
                        MapOfWords.put(newword, value);
                    }
                }
            }
        }
        return MapOfWords;
    }
    
    final public double calculateChannelModelProbability(String suggested, String incorrect) {
        char[] suggestedChars = (suggested + " ").toCharArray();
        char[] incorrectChars = (incorrect + " ").toCharArray();
        int slength = suggested.length();
        int ilength = incorrect.length();
        String errorString = "";
        String correctString = "";
        
        if (slength < ilength) { //deletion
            if (suggestedChars[0] == incorrectChars[1]) {
                //first character got deleted: >a | >
                errorString += ">" + incorrectChars[0];
                correctString += ">";
                return cmr.getConfusionCount(errorString, correctString);
            } else {
                //check char 1 to suggestedChars.length for differences
                for (int i = 1; i < ilength; i++) {
                    //Confusion matrix deletion: ea | e
                    if (suggestedChars[i] != incorrectChars[i]) {
                        int shift = 0;
                        if (incorrectChars[i - 1] == incorrectChars[i]){
                            shift = 1; //when error is double char: xx|x doesn't exist in confusion matrix so we take the chars one to the left
                        }
                        errorString += incorrectChars[i - 1 - shift];
                        errorString += incorrectChars[i - shift];
                        correctString += suggestedChars[i - 1 - shift];
                        return cmr.getConfusionCount(errorString, correctString);
                    }
                }
            }
        } else if (slength > ilength) { //insertion
            if (suggestedChars[1] == incorrectChars[0]) {
                //first character got inserted: > | >a
                errorString += ">";
                correctString += ">" + suggestedChars[0];
                return cmr.getConfusionCount(errorString, correctString);
            } else {
                //check char 1 to incorrectChars.length for differences
                for (int i = 1; i < slength; i++) {
                    //Confusion matrix insertion: e | ea
                    if (suggestedChars[i] != incorrectChars[i]) {
                        int shift = 0;
                        if (suggestedChars[i - 1] == suggestedChars[i]) {
                            shift = 1; //when error is forgotten double char: x|xx doesn't exist in confusion matrix so we take the chars one to the left
                        }
                        errorString += incorrectChars[i - 1 - shift];
                        correctString += suggestedChars[i - 1 - shift];
                        correctString += suggestedChars[i - shift];
                        return cmr.getConfusionCount(errorString, correctString);
                    }
                }
            }
        } else { //slength == ilength
            for (int i = 0; i < slength; i++) {
                //Confusion matrix transposition: ae | ea
                //Confusion matrix substitution: a | e
                if (suggestedChars[i] != incorrectChars[i]) {
                    errorString += incorrectChars[i];
                    correctString += suggestedChars[i];
                }
            }
            return cmr.getConfusionCount(errorString, correctString);
        }
        
        return cmr.getConfusionCount(errorString, correctString);
    }
      
    final public HashSet<String> getCandidateWords(String word) {
        
        HashSet<String> ListOfWords = new HashSet<String>();
        StringBuilder sb;
        
        //Add itself
        ListOfWords.add(word);
        //Add deletions:
        for (int i = 0; i < word.length(); i++) {
            sb = new StringBuilder(word);
            sb.deleteCharAt(i);
            ListOfWords.add(sb.toString());
        }
        //Add insertions:
        for (int i = 0; i <= word.length(); i++) {
            for (char c : ALPHABET) {
                sb = new StringBuilder(word);
                sb.insert(i, c);
                ListOfWords.add(sb.toString());
            }
        }
        //Add transpositions:
        if (word.length() >= 2) {
            for (int i = 0; i < word.length() - 1; i++) {
                sb = new StringBuilder(word);
                sb.setCharAt(i, word.charAt(i + 1));
                sb.setCharAt(i + 1, word.charAt(i));
                ListOfWords.add(sb.toString());
            }
        }
        //Add substitutions:
        for (int i = 0; i < word.length(); i++) {
            for (char c : ALPHABET) {
                sb = new StringBuilder(word);
                sb.setCharAt(i, c);
                ListOfWords.add(sb.toString());
            }
        }
        return cr.inVocabulary(ListOfWords);
    }          
}