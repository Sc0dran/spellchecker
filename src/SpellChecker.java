

import java.io.IOException;
import java.util.Scanner;


public class SpellChecker {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) 
    {
        boolean inPeach = false; // set this to true if you submit to peach!!!
        
        try {
            CorpusReader cr = new CorpusReaderKNSmooth();
            ConfusionMatrixReader cmr = new ConfusionMatrixReader();
            SpellCorrector sc = new SpellCorrector(cr, cmr);
            if (inPeach) {
                peachTest(sc);
            } else {
                nonPeachTest(sc);
            }
        } catch (Exception ex) {
            System.out.println(ex);
            ex.printStackTrace();
        }
    }
    
    static void nonPeachTest(SpellCorrector sc) throws IOException { 
            String[] sentences = {
                "this assay allowed us to measure a wide variety of conditions",
                "this assay allowed us to measure a wide variety of conitions",
                "this assay allowed us to meassure a wide variety of conditions",
                "this assay allowed us to measure a wide vareity of conditions",
                "at the home locations there were traces of water",
                "at the hme locations there were traces of water",
                "at the hoome locations there were traces of water",
                "at the home locasions there were traces of water",
                "the development of diabetes is present in mice that carry a transgene",
                "the development of diabetes is present in moce that carry a transgene",
                "the development of idabetes is present in mice that carry a transgene",
                "the development of diabetes us present in mice that harry a transgene",
                "the development of diabetes is present in moce that carry a transgen",
                "essentially there has been no change in japan",
                "esentially there has been no change in japan",
                "a response may be any measurable biological prameter that is corelated with the toxicant",
                "she still refers to me as a fiend but i feel i am treated quite badly",
                "she still refers to me as a friendd but i feel i am traeted quite badly",
                "this addvice is taking into consideration the fact that the goverment bans political parties",
                "this advise is taking into consideration the fact that the govenrment bans political parties",
                "ancient china was one of the longest lasting sosieties in the history of the world",
                "ancient china wqs one of the longest lasting societies in the histori of the world",
                "anicent china was one of the longest lasting societties in the history of the world",
                "ancient china was one of the longst lasting societies iin the history of the world",
                "boxing glowes shield the knockles not the head",
                "boing gloves shield the knuckles nut the head",
                "boxing loves shield the knuckles nots the head",
                "playing in the national football laegue was my draem",
                "laying in the national footbal league was my dream"
                
            };
            
            for(String s0: sentences) {
                System.out.println("Input : " + s0);
                String result=sc.correctPhrase(s0);
                System.out.println("Answer: " +result);
                System.out.println();
            }
    }
    
    static void peachTest(SpellCorrector sc) throws IOException {
            Scanner input = new Scanner(System.in);
            
            String sentence = input.nextLine();
            System.out.println("Answer: " + sc.correctPhrase(sentence));  
    } 
}