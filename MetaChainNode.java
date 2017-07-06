import java.util.ArrayList;
import java.util.HashMap;

//This class is node class for our metachain array of linkedlist.
public class MetaChainNode {
    String token;
    MetaChainNode next;
    ArrayList<MetaChainNode> children;
    HashMap<MetaChainNode,Integer> relation;
    public MetaChainNode(String str)
    {
        //System.out.println("came here");
        token=str;
        next=null;
        children = new ArrayList<>();
        relation = new HashMap<>();

    }

}
