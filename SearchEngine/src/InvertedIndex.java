
import java.util.*;
public class InvertedIndex {
	String[] myDocs;
	ArrayList<String> termList;
	ArrayList<ArrayList<Integer>> docLists;
	
	public InvertedIndex(String[] docs)
	{
		myDocs = docs;
		termList = new ArrayList<String>();
		docLists = new ArrayList<ArrayList<Integer>>();
		ArrayList<Integer> docList;
		for(int i=0;i<myDocs.length;i++)
		{
			String[] tokens = myDocs[i].split("[\\p{P} \\t\\n\\r]");
			for(String token:tokens){
				if(!termList.contains(token)){//creating a new term and its postings
					termList.add(token);
					docList = new ArrayList<Integer>();
					docList.add(new Integer(i));
					docLists.add(docList);
				}
				else //existing term, update the postings for the term
				{
					int index = termList.indexOf(token);
					docList = docLists.get(index);
					if(!docList.contains(new Integer(i))){
						docList.add(new Integer(i));
						docLists.set(index, docList);
					}
				}
			}
		}
	}
	
	public String toString()
	{
		String matrixString = new String();
		ArrayList<Integer> docList;
		for(int i=0;i<termList.size();i++){
			matrixString += String.format("%-15s", termList.get(i));
			docList = docLists.get(i);
			for(int j=0;j<docList.size();j++){
				matrixString += docList.get(j) + "\t";
			}
			matrixString += "\n";
		}
		return matrixString;
	}
	
	public ArrayList<Integer> search(String query)
	{
		int index = termList.indexOf(query);
		if(index <0)
			return null;
		return docLists.get(index);
	}
	
	public static void main(String[] args){
		String[] docs = {"new home sales top forecasts",
						 "home sales rise in july",
						 "increase in home sales in july",
						 "july new home sales rise"
		};
		InvertedIndex inverted = new InvertedIndex(docs);
		System.out.println(inverted);
		ArrayList<Integer> result = inverted.search(args[0]);
		if(result!=null)
		{
			for(Integer i:result)
				System.out.println(docs[i.intValue()]);
		}
		else
			System.out.println("No match!");
	}
}
