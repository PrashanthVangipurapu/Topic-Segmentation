import java.io.FileInputStream;
import net.didion.jwnl.JWNL;
import net.didion.jwnl.JWNLException;
import net.didion.jwnl.data.*;
import net.didion.jwnl.dictionary.Dictionary;import net.didion.jwnl.data.IndexWord;
import net.didion.jwnl.data.POS;

public class FindMissingWordWordNet {
    public static void main(String [] args) throws JWNLException{
        FindMissingWordWordNet f = new FindMissingWordWordNet();
        boolean t=f.check("Bahia","Noun");
        System.out.println("returned value is "+t);
        System.out.println(t);
    }
    public boolean check(String str, String posTag) throws JWNLException {
        boolean flag = true;
        configureJWordNet();
        Dictionary dictionary = Dictionary.getInstance();
        IndexWord word;
        if (posTag.equals("Noun"))//Verb
        {
            word = dictionary.lookupIndexWord(POS.NOUN, str);
        }
        else
        {
            word = null;
        }
        if (word == null)
            return false;
        else
        {
            Synset[] senses = word.getSenses();
            //System.out.println(senses.length);
            if (senses != null && senses.length > 0)
            {
                return true;
                  /*if(senses[0].toString().toLowerCase().contains(str)|| senses[0].toString().contains(str.replace(" ","_")))
                {
                    System.out.println("sense;;;; "+senses[0].toString());flag=false;
                }*/
            }
            else
               return false;

        }
    }


    public static void configureJWordNet() {
// WARNING: This still does not work in Java 5!!!
        try {
// initialize JWNL (this must be done before JWNL can be used)
// See the JWordnet documentation for details on the properties file
            JWNL.initialize(new FileInputStream("F:\\TwiiterSarcasmProject\\Topic segmentation\\Topic Segmentation\\src\\file_properties.xml"));
        } catch (Exception ex) {
            ex.printStackTrace();
            System.exit(-1);
        }
    }

}
