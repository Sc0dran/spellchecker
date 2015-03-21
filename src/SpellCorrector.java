import java.util.HashSet;

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
        
        return finalSuggestion.trim();
    }
    
    public double calculateChannelModelProbability(String suggested, String incorrect) 
    {
         /** CODE TO BE ADDED **/
        
        return 0.0;
    }
         
      
    public HashSet<String> getCandidateWords(String word)
    {
        HashSet<String> ListOfWords = new HashSet<String>();
        
        StringBuilder sb;
        
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
        for (int i = 0; i < word.length(); i++) {
            for (int j = i + 1; j < word.length(); j++) {
                sb = new StringBuilder(word);
                sb.setCharAt(i, word.charAt(j));
                sb.setCharAt(j, word.charAt(i));
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