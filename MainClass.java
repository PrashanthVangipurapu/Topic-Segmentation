import java.io.*;
import java.util.*;
import net.didion.jwnl.JWNLException;
import net.didion.jwnl.data.IndexWord;
import net.didion.jwnl.data.POS;
import net.didion.jwnl.data.PointerUtils;
import net.didion.jwnl.data.Synset;
import net.didion.jwnl.data.list.PointerTargetNode;
import net.didion.jwnl.data.list.PointerTargetNodeList;
import net.didion.jwnl.dictionary.Dictionary;
import opennlp.tools.postag.POSModel;
import opennlp.tools.postag.POSTaggerME;
import opennlp.tools.tokenize.SimpleTokenizer;
public class MainClass {
    int nouncount =0;
    int num = 1382 + 146312; //Total of 1,46,312 nouns are there in wordnet 3.0 and there are 1382 nouns in the given text file to be processed.
    HashMap<Long,HashSet<MetaChainNode>> offsetHash;  //Size = 82115;
    HashMap<Long, String> hashdict;
    ArrayList<String> hypernyms;// = new ArrayList<>();
    ArrayList<String> synonyms;// = new ArrayList<>();
    ArrayList<String> hyponyms;
    ArrayList<String> siblings;
    MetaChainNode[] m =new MetaChainNode[num];
    ArrayList<String> split,stripsplit,sublist,hyperlist,hypolist;
    Dictionary dictionary;
    //IndexWord word;
    Synset[] senses;
    public static void main(String[] args) throws Exception{
        MainClass mc = new MainClass();
        mc.buildHashSet();
        mc.buildChain(mc.offsetHash);
        //mc.displayArray();

    }

    public void buildHashSet() throws Exception{
       offsetHash = new HashMap<>();
       FindOffset f =new FindOffset();
       f.find();
       offsetHash = f.get();
       System.out.println(offsetHash.size());
    }

    public void buildChain(HashMap<Long,HashSet<MetaChainNode>> m) throws Exception
    {
        //This function reads the whole text token by token and starts checking with our meta chain. We process only nouns present in the text.
        hashdict=new HashMap<>();
        BufferedReader br =new BufferedReader(new FileReader("F:\\TwiiterSarcasmProject\\Topic segmentation\\Topic Segmentation\\src\\English Text\\ModifiedEnglish.txt")); //Read the tokens each from this file.
        String line = br.readLine();
        SimpleTokenizer simpleTokenizer;
        String tokens[];
        String[] tags;
        boolean flag;
        String[] nountags={"NN","NNS","NNP","NNPS","PRP","PRP$","WP","WP$"};
        String[] adjtags =  {"JJ","JJR","JJS"};
        InputStream inputStream = new FileInputStream("C:\\OpenNLP_models/en-pos-maxent.bin");
        POSModel model = new POSModel(inputStream);
        POSTaggerME tagger = new POSTaggerME(model);
        HashMap<String, HashSet<Long>> offsetIndex; //Keeps track of all offsets where our string is stored.
        while(line!=null)
        {
            simpleTokenizer = SimpleTokenizer.INSTANCE; //This line is used for creating object/instance for the class SimpleTokenizer.
            tokens = simpleTokenizer.tokenize(line);
            tags = tagger.tag(tokens);
            for(int i=0;i<tags.length;i++)
            {
                if (Arrays.asList(nountags).contains(tags[i]) || Arrays.asList(adjtags).contains(tags[i]))  //If the corresponding tag of the given token is a noun.
                {
                    FindMissingWordWordNet f =new FindMissingWordWordNet();
                    flag = f.check(tokens[i],"Noun"); //This is used to check whether the word is present in wordnet or not.
                    if(flag) //Word is present in wordnet if flag==true.
                    {
                        MetaChainNode mnode = new MetaChainNode(tokens[i]);
                        offsetIndex = fillArray(mnode);
                        findHypernyms(tokens[i],offsetIndex,mnode,1); //flag:1
                        findSynonyms(tokens[i],offsetIndex,mnode,2);  //flag:2
                        findHyponym(tokens[i],offsetIndex,mnode,3);   //flag:3
                    }
                }
            }
            System.out.println("Line is "+line);
            line =br.readLine();
        }
    }

    public HashMap<String,HashSet<Long>> fillArray(MetaChainNode mnode) throws Exception{

        offsetHash = new HashMap<>();

        // we have the meta chain array with senses in it. We need to traverse each noun and fill the empty metachain array with the noun tokens that we have.
        HashMap<String,HashSet<Long>> h = new HashMap<>();//  This is a hashMap which will have keys equal to string
        HashSet<Long> longSet = new HashSet<>();          //   and value equal to their offsets.
        HashSet<Long> t;


        HashSet<MetaChainNode> tset;// = new TreeSet<>(); //This is our original Hashmap's treeset which will have nodes as values.'
        Synset[] senses;
        long offset=0;
        //System.out.println(mnode.token);
        Dictionary dictionary = Dictionary.getInstance();
        IndexWord word = dictionary.lookupIndexWord(POS.NOUN,mnode.token);
        senses = word.getSenses();

         //If an elemnt is present in hashmap.
            for (int i = 0; i < senses.length; i++) {
                offset = senses[i].getOffset();
                //System.out.println("offset has is " + offsetHash);
                //For our original HashMap.
                    if (offsetHash.keySet().contains(offset)) {
                        tset = offsetHash.get(offset);
                        tset.add(mnode);
                        offsetHash.put(offset, tset);
                    } else {
                        tset = new HashSet<>();
                        tset.add(mnode);
                        offsetHash.put(offset, tset);
                    }

                    //For our returned HashMap.
                    if (h.keySet().contains(mnode.token)) {
                        t = h.get(mnode.token);
                        t.add(offset);
                        h.put(mnode.token, t);
                    } else {
                        t = new HashSet<>();
                        t.add(offset);
                        h.put(mnode.token, t);
                    }
            }
       return h; //For each token that we have passed we find all its senses and fill those senses offset with the token.
    }

    public void findHypernyms(String str,HashMap<String,HashSet<Long>> offsetIndex, MetaChainNode mnode, int flag) throws Exception
    {
        //str = str+" : " +flag;
        HashSet<Long> addSet=new HashSet<>();
        HashSet<MetaChainNode> tset;
        hypernyms = getHypernyms(str); //This returns the set of hypernyms of a given word.
        addToMetaChain(str,hypernyms,offsetIndex,mnode,flag);
    }


    public ArrayList<String> getHypernyms(String str) throws Exception{
        //This function is used for finding the set of hyper-nyms for the given word or token.

        //System.out.println("The given string is "+str);
        dictionary= Dictionary.getInstance();
        IndexWord word = dictionary.lookupIndexWord(POS.NOUN, str);
        senses=word.getSenses();
        //System.out.println("sens length "+senses.length);
        sublist = new ArrayList<>();
        hyperlist = new ArrayList<>();
        PointerTargetNodeList hypernyms;
        String prst;
        for(int i=0;i<senses.length;i++){
            hypernyms = PointerUtils.getInstance().getDirectHypernyms(word.getSense(i+1));
            prst=hypernyms.get(0).toString();
            //System.out.println(prst);
            sublist = process(prst);
            hyperlist.addAll(sublist);
        }
        //System.out.println(hyperlist);
        return hyperlist;
    }


    public void findSynonyms(String str,HashMap<String,HashSet<Long>> offsetIndex, MetaChainNode mnode, int flag) throws Exception{
        HashSet<Long> addSet=new HashSet<>();
        HashSet<MetaChainNode> tset;
        String str1;
        synonyms = new ArrayList<>();
        Dictionary dictionary = Dictionary.getInstance();
        IndexWord word = dictionary.lookupIndexWord(POS.NOUN, str);
        Synset[] senses=word.getSenses();
        for (Synset s:senses){
            str1=s.toString();
            sublist=process(str1);
            sublist = findUniqueList(sublist);
            synonyms.addAll(sublist);
        }
        System.out.println("str is "+str+" and its synonyms are"+synonyms);
        addToMetaChain(str,synonyms,offsetIndex,mnode,flag);


    }




    public void findHyponym(String str,HashMap<String,HashSet<Long>> offsetIndex, MetaChainNode mnode, int flag) throws Exception{
        hyponyms = getHyponym(str);
        addToMetaChain(str,hyponyms,offsetIndex,mnode,flag);
        System.out.println("hyponyms of "+str+" are "+hyponyms);
    }

    public ArrayList<String> getHyponym(String str) throws JWNLException{
        dictionary= Dictionary.getInstance();
        IndexWord word = dictionary.lookupIndexWord(POS.NOUN, str);
        senses=word.getSenses();
        //System.out.println("sens length "+senses.length);
        sublist = new ArrayList<>();
        hypolist = new ArrayList<>();
        PointerTargetNodeList hyponyms;
        String prst;
        for(int i=0;i<senses.length;i++){
            hyponyms = PointerUtils.getInstance().getDirectHyponyms(word.getSense(i+1));
            if(hyponyms.size()>0) {
                prst = hyponyms.get(0).toString();
                System.out.println(prst);
                sublist = process(prst);
                System.out.println(sublist.getClass().getName());
                hypolist.addAll(sublist);
            }
        }
        //System.out.println(hyperlist);
        return hypolist;

    }

    public void addToMetaChain(String str,ArrayList<String> list,HashMap<String,HashSet<Long>> offsetIndex, MetaChainNode mnode, int flag){


        HashSet<Long> addSet=new HashSet<>();
        HashSet<MetaChainNode> tset;
        for(String s:list){
            if(offsetIndex.containsKey(s))  //If the hypernyms string is already present in our list of nouns present in the array.
            {
                for(Map.Entry entry : offsetIndex.entrySet())
                {
                    if (str.equals(entry.getKey()))
                    {
                        addSet = offsetIndex.get(str);  //Get all offset value for every Hypernym and store it in one TreeSet called addSet.
                    }
                }
                for(long each: addSet)
                {  // In this loop we add children to the node.
                    //For each offset value.
                    tset = offsetHash.get(each); //Get its corresponding key.
                    for(MetaChainNode eachnode: tset) //For each node in the key.
                        if(s.equals(eachnode.token)) //If the key's token is equal to our hypernym.
                        {
                            eachnode.children.add(mnode); // Add that to the of parent node.
                            mnode.relation.put(mnode,flag);
                        }
                }
            }
        }

    }



    public ArrayList<String> process(String processtr){
        //This function takes the set of hypenyms as a single string and then splits them on the basis of ',' using split() function.

        //String str = processtr.toString();
        split = new ArrayList<>();
        stripsplit = new ArrayList<>();
        String str;
        int start = processtr.indexOf(" ",processtr.indexOf("Words"));
        int last = processtr.indexOf("--");
        String list = processtr.substring(start,last);
        split = new ArrayList<String>(Arrays.asList(list.split(",")));
        //System.out.println("The substring in process function is "+split);
        if(split.size()==0){
            System.out.println("The split size is 0");
            str=list;
            str=str.trim();
            stripsplit.add(str);

        }
        else {
            for (int i = 0; i < split.size(); i++) {
                str = split.get(i);
                str = str.trim();
                //System.out.println(i);
                stripsplit.add(str);
            }
        }
        return stripsplit;
    }


  public ArrayList<String> findUniqueList(ArrayList<String> list){
      Set<String> s = new HashSet<>(list);
      ArrayList<String> sublist = new ArrayList<>(s);
      return sublist;
  }

  public void displayArray() throws Exception{
        //This is a dummy display function. You can see what nouns are there in the meta-chains array using this function.

        //System.out.println(m[43113].token);
        //System.out.println(m[43113].next.token);
        BufferedWriter bw =new BufferedWriter(new FileWriter("F:\\TwiiterSarcasmProject\\Topic segmentation\\Topic Segmentation\\src\\English Text\\MetaChainsList.txt"));
        HashSet<MetaChainNode> hc = new HashSet<>();
        String key,value;
        System.out.println(offsetHash.size());
        /*if(offsetHash.size() > 0){
            System.out.println("Entered for death");
            return;
        }*/
        for(Map.Entry<Long,HashSet<MetaChainNode>> entry:offsetHash.entrySet() ){
            key = entry.getKey().toString();
            bw.write(key);
            bw.newLine();
            hc = entry.getValue();
            bw.write("DETAILS:");
            int relation;
            for(MetaChainNode mc:hc){
                bw.write(mc.token);
                bw.write("ITS CHILDREN:");
                for(MetaChainNode childmc : mc.children ){
                    bw.write(childmc.token);
                    bw.write("--");
                    relation = mc.relation.get(childmc);
                    bw.write(relation);
                }
            }
        }
        bw.flush();
        bw.close();
        System.out.println("Done writing");
    }

    /*public void display() throws Exception{
        MetaChainNode mc;
        int count =0 ;
        BufferedWriter bw =new BufferedWriter(new FileWriter("F:\\TwiiterSarcasmProject\\Topic segmentation\\Topic Segmentation\\src\\English Text\\AllNounsInMetaChainArray.txt"));
        for(int i=0;i<m.length;i++){
            mc = m[i];
            while(mc!=null){
                bw.write(mc.token);
                bw.write("-->");
                mc=mc.next;
            }
            count = count +1;
            bw.newLine();
            //bw.newLine();
        }
        //System.out.println(m[36862].token);
        //System.out.println("The value of count is "+count);
    }*/


}


