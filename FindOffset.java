import net.didion.jwnl.JWNL;
import net.didion.jwnl.data.IndexWord;
import net.didion.jwnl.data.POS;
import net.didion.jwnl.data.Synset;
import net.didion.jwnl.dictionary.Dictionary;

import java.io.*;
import java.util.*;

public class FindOffset {

    HashSet<MetaChainNode> tset;
    HashMap<Long,HashSet<MetaChainNode>> offsetHash;  //Cannot use HashMap<long> long= primitive type;
    //HashMap<Long,String>  hashdict ;
    public static void main(String[] args)throws Exception{
        FindOffset f =new FindOffset();
        f.find();
        //f.write();

    }
    /*public void find() throws Exception{
        tset = new TreeSet<>();
        TreeSet<String> funSet ;
        offsetHash = new HashMap<>();
        hashdict = new HashMap<>();
        JWNL.initialize(new FileInputStream("F:\\TwiiterSarcasmProject\\Topic segmentation\\Topic Segmentation\\src\\file_properties.xml"));
        Dictionary dict = Dictionary.getInstance();
        IndexWord word ;
        Synset[] senses;
        long offset;
        BufferedReader br =new BufferedReader(new FileReader("F:\\TwiiterSarcasmProject\\Topic segmentation\\Topic Segmentation\\src\\English Text\\UpdateNounsList.txt"));
        //BufferedWriter bw = new BufferedWriter((new FileWriter("F:\\TwiiterSarcasmProject\\Topic segmentation\\Topic Segmentation\\src\\English Text\\NounsOffset.txt")));
        String line = br.readLine();
        int firstindex = 2;
        int lastindex;
        int count =0;
        int index=0;

        while(line!=null) {
             lastindex = line.indexOf(".n");
             String str = line.substring(firstindex, lastindex);
             word = dict.lookupIndexWord(POS.NOUN, str);
             senses = word.getSenses();
             count = count + senses.length;
            for (int i=0; i<senses.length; i++) {
                offset = senses[i].getOffset();
                hashdict.put(offset,str);
                if(offsetHash.containsKey(offset))
                   {
                      tset=offsetHash.get(offset);
                      tset.add(str);
                      offsetHash.put(offset,tset);
                   }
                else{
                        funSet = new TreeSet<>();
                        funSet.add(str);
                        offsetHash.put(offset,funSet);
                     }
            }
            line = br.readLine();
        }
        br.close();
        System.out.println("count is "+count);
        System.out.println(offsetHash.size()+" "+hashdict.size());
    }*/


    public void find() throws Exception{
        //tset = new TreeSet<>();
        //TreeSet<MetaChainNode> funSet ;  //For the values which are not yet generated.
        offsetHash = new HashMap<>();
        //hashdict = new HashMap<>();
        JWNL.initialize(new FileInputStream("F:\\TwiiterSarcasmProject\\Topic segmentation\\Topic Segmentation\\src\\file_properties.xml"));
        Dictionary dict = Dictionary.getInstance();
        IndexWord word ;
        Synset[] senses;
        long offset;
        BufferedReader br =new BufferedReader(new FileReader("F:\\TwiiterSarcasmProject\\Topic segmentation\\Topic Segmentation\\src\\English Text\\UpdateNounsList.txt"));
        //BufferedWriter bw = new BufferedWriter((new FileWriter("F:\\TwiiterSarcasmProject\\Topic segmentation\\Topic Segmentation\\src\\English Text\\NounsOffset.txt")));
        String line = br.readLine();
        int firstindex = 2;
        int lastindex;
        int count =0;


        while(line!=null) {
            lastindex = line.indexOf(".n");
            String str = line.substring(firstindex, lastindex);
            word = dict.lookupIndexWord(POS.NOUN, str);
            senses = word.getSenses();
            count = count + senses.length;
            for (int i=0; i<senses.length; i++) {
                offset = senses[i].getOffset();
                //hashdict.put(offset,str);
                tset=new HashSet<>();
                //tset.add(str);
                offsetHash.put(offset,tset);
            }
            line = br.readLine();
        }
        br.close();
        System.out.println("count is "+count);
        //System.out.println(offsetHash.get(4196803));
    }

    public HashMap<Long, HashSet<MetaChainNode>> get()
    {
        return offsetHash;
    }

    public void write() throws Exception
    {
        BufferedWriter br =new BufferedWriter(new FileWriter("F:\\TwiiterSarcasmProject\\Topic segmentation\\Topic Segmentation\\src\\English Text\\MetaChainHashset.txt"));
        String key,value, both;
        //System.out.println(offsetHash);
        //int size = hashdict.size();
        //System.out.println(size);

        Iterator it = offsetHash.entrySet().iterator();
        while(it.hasNext()){
            System.out.println("came in");
            Map.Entry pair = (Map.Entry) it.next();
            key = pair.getKey().toString();
            value = pair.getValue().toString();
            both = key+" --> "+ value;
            //System.out.println(both);
            br.write(both);
            br.newLine();
            br.newLine();

        }
        br.close();
    }
}
