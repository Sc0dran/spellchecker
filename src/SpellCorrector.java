import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

public class SpellCorrector {
    final private CorpusReader cr;
    final private ConfusionMatrixReader cmr;
    
    final char[] ALPHABET = "abcdefghijklmnopqrstuvwxyz'".toCharArray();
    
    
    public SpellCorrector(CorpusReader cr, ConfusionMatrixReader cmr) 
    {
        this.cr = cr;
        this.cmr = cmr;
    }
    
    public String correctPhrase(String phrase)
    {
        if(phrase == null || phrase.length() == 0)
        {
            throw new IllegalArgumentException("phrase must be non-empty.");
        }
            
        String[] words = phrase.split(" ");
        String finalSuggestion = "";
        
        /** CODE TO BE ADDED **/
        for (int i = 0; i < words.length; i++){
            double bestvalue = 0;
            String bestword = words[i];
            
            for (String word : getCandidateWords(words[i])){
                double channel = calculateChannelModelProbability(word, words[i]);
                double oneGramCount = cr.getSmoothedCount(word);
                double preNGramCount = i==(words.length-1)  ? 1 : cr.getSmoothedCount(words[i-1] + word);
                double postNGramCount = i==0                ? 1 : cr.getSmoothedCount(words[i-1] + word);
                double twoGramCount = preNGramCount*postNGramCount;
                
//                if ( > bestvalue){
//                    bestword = word;
//                    bestvalue = 
//                }
            }
        }

        return finalSuggestion.trim();
    }
    
    public double calculateChannelModelProbability(String suggested, String incorrect) 
    {
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
                System.out.println(errorString + "|" + correctString + " :1");
                return cmr.getConfusionCount(errorString, correctString);
            } else {
                //check char 1 to suggestedChars.length for differences
                for (int i = 1; i < ilength; i++) {
                    //Confusion matrix deletion: ea | e
                    if (suggestedChars[i] != incorrectChars[i]) {
                        int shift = 0;
                        if (incorrectChars[i-1] == incorrectChars[i]){
                            shift = 1; //when error is double char: xx|x doesn't exist in confusion matrix so we take the chars one to the left
                        }
                        errorString += incorrectChars[i-1-shift];
                        errorString += incorrectChars[i-shift];
                        correctString += suggestedChars[i-1-shift];
                        System.out.println(errorString + "|" + correctString + " :2");
                        return cmr.getConfusionCount(errorString, correctString);
                    }
                }
            }
        } else if (slength > ilength) { //insertion
            if (suggestedChars[1] == incorrectChars[0]) {
                //first character got inserted: > | >a
                errorString += ">";
                correctString += ">" + suggestedChars[0];
                System.out.println(errorString + "|" + correctString + " :4");
                return cmr.getConfusionCount(errorString, correctString);
            } else {
                //check char 1 to incorrectChars.length for differences
                for (int i = 1; i < slength; i++) {
                    //Confusion matrix insertion: e | ea
                    if (suggestedChars[i] != incorrectChars[i]) {
                        int shift = 0;
                        if (suggestedChars[i-1] == suggestedChars[i]) {
                            shift = 1; //when error is forgotten double char: x|xx doesn't exist in confusion matrix so we take the chars one to the left
                        }
                        errorString += incorrectChars[i-1-shift];
                        correctString += suggestedChars[i-1-shift];
                        correctString += suggestedChars[i-shift];
                        System.out.println(errorString + "|" + correctString + " :5");
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
            System.out.println(errorString + "|" + correctString + " :7");
            return cmr.getConfusionCount(errorString, correctString);
        }
        
        return cmr.getConfusionCount(errorString, correctString);
    }
    
    public HashMap<String,Double> getCandidateWordsChannel(String word)
    {
        HashMap<String,Double> MapOfWords = new HashMap<String,Double>();
        
        StringBuilder sb;
        
        //Add itself
        if(cr.inVocabulary(word)){
            MapOfWords.put(word,0.95);
        }
        //Add deletions:
        for (int i = 0; i < word.length(); i++) {
            sb = new StringBuilder(word);
            sb.deleteCharAt(i);
            String newword = sb.toString();
            if(cr.inVocabulary(newword)){
                int shift = 0;
                if (word.charAt(i-1) == word.charAt(i)){
                    shift = 1; //when error is double char: xx|x doesn't exist in confusion matrix so we take the chars one to the left
                }
                String error = "";
                String correct = "";
                error += (i==0 ? ">" : word.charAt(i-1-shift));
                error += word.charAt(i-shift);
                correct += (i==0 ? ">" : newword.charAt(i-1-shift));
                double value = cmr.getConfusionCount(error, correct);
                MapOfWords.put(newword, value);
            }
        }
        //Add insertions:
        for (int i = 0; i <= word.length(); i++) {
            for (char c : ALPHABET) {
                sb = new StringBuilder(word);
                sb.insert(i, c);
                String newword = sb.toString();
                if(cr.inVocabulary(newword)){
                    int shift = 0;
                    if (newword.charAt(i-1) == newword.charAt(i)){
                        shift = 1; //when error is forgotten double char: x|xx doesn't exist in confusion matrix so we take the chars one to the left
                    }
                    String error = "";
                    String correct = "";
                    error += (i==0 ? ">" : word.charAt(i-1-shift));
                    correct += (i==0 ? ">" : newword.charAt(i-1-shift));
                    correct += newword.charAt(i-shift);
                    double value = cmr.getConfusionCount(error, correct);
                    MapOfWords.put(newword, value);
                }
            }
        }
        //Add transpositions:
        if(word.length() >= 2){
            for (int i = 0; i < word.length()-1; i++) {
                sb = new StringBuilder(word);
                sb.setCharAt(i, word.charAt(i+1));
                sb.setCharAt(i+1, word.charAt(i));
                String newword = sb.toString();
                if(cr.inVocabulary(newword)){
                    String error = "";
                    String correct = "";
                    error += word.charAt(i);
                    error += word.charAt(i+1);
                    correct += newword.charAt(i+1);
                    correct += newword.charAt(i);
                    double value = cmr.getConfusionCount(error, correct);
                    MapOfWords.put(newword, value);
                }
            }
        }
        //Add substitutions:
        for (int i = 0; i < word.length(); i++) {
            for (char c : ALPHABET) {
                sb = new StringBuilder(word);
                sb.setCharAt(i, c);
                String newword = sb.toString();
                if(cr.inVocabulary(newword)){
                    String error = "";
                    String correct = "";
                    error += word.charAt(i);
                    correct += c;
                    System.out.println(error + "|" + correct);
                    double value = cmr.getConfusionCount(error, correct);
                    MapOfWords.put(newword, value);
                }
            }
        }
        
        return MapOfWords;
    }
      
    public HashSet<String> getCandidateWords(String word)
    {
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
        if(word.length() >= 2){
            for (int i = 0; i < word.length()-1; i++) {
                sb = new StringBuilder(word);
                sb.setCharAt(i, word.charAt(i+1));
                sb.setCharAt(i+1, word.charAt(i));
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