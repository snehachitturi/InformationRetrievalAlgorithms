package lab2;

import java.util.ArrayList;
import java.util.HashMap;



public class PositionalIndex {
	String[] myDocs;
	ArrayList<String> termList;
	ArrayList<ArrayList<DocId>> docLists;

	/**
	 * Construct a positional index 
	 * @param docs List of input strings or file names
	 * 
	 */
	public PositionalIndex(String[] docs)
	{
		myDocs = docs;
		termList = new ArrayList<String>();
		docLists = new ArrayList<ArrayList<DocId>>();
		ArrayList<DocId> docList;
		for(int i=0;i<myDocs.length;i++){
			String[] tokens = myDocs[i].split(" ");
			String token;
			for(int j=0;j<tokens.length;j++){
				token = tokens[j];
				if(!termList.contains(token)){
					termList.add(token);
					docList = new ArrayList<DocId>();
					DocId doid = new DocId(i,j);
					docList.add(doid);
					docLists.add(docList);
				}
				else{ //existing term
					int index = termList.indexOf(token);
					docList = docLists.get(index);
					int k=0;
					boolean match = false;
					//search the postings for a document id, if match, insert a new position
					//number to the document id
					for(DocId doid:docList)
					{
						if(doid.docId==i)
						{
							doid.insertPosition(j);
							docList.set(k, doid);
							match = true;
							break;
						}
						k++;
					}
					//if no match, add a new document id along with the position number
					if(!match)
					{
						DocId doid = new DocId(i,j);
						docList.add(doid);
					}
				}
			}
		}
	}

	/**
	 * Return the string representation of a positional index
	 */
	public String toString()
	{
		String matrixString = new String();
		ArrayList<DocId> docList;
		for(int i=0;i<termList.size();i++){
			matrixString += String.format("%-15s", termList.get(i));
			docList = docLists.get(i);
			for(int j=0;j<docList.size();j++)
			{
				matrixString += docList.get(j)+ "\t";
			}
			matrixString += "\n";
		}
		return matrixString;
	}

	/**
	 * 
	 * @param l1 first postings
	 * @param l2 second postings
	 * @return merged result of two postings
	 */
	public ArrayList<DocId> intersect(ArrayList<DocId> l1, ArrayList<DocId> l2)
	{
		ArrayList<DocId> mergedList = new ArrayList<DocId>();
		int id1=0, id2=0;
		while(id1<l1.size()&&id2<l2.size()){
			//if both terms appear in the same document
			if(l1.get(id1).docId==l2.get(id2).docId){
				//get the position information for both terms
				ArrayList<Integer> pp1 = l1.get(id1).positionList;
				ArrayList<Integer> pp2 = l2.get(id2).positionList;
				int pid1 =0, pid2=0;
				while(pid1<pp1.size()){
					boolean match = false;
					while(pid2<pp2.size()){
						//if the two terms appear together, we find a match
						if(Math.abs(pp1.get(pid1)-pp2.get(pid2))<=1){
							match = true;
							mergedList.add(new DocId(l1.get(id1).docId));
							break;
						}
						else if(pp2.get(pid2)>pp1.get(pid1))
							break;
						pid2++;
					}
					if(match) //if a match if found, the search for the current document can be stopped
						break;
					pid1++;
				}
				id1++;
				id2++;
			}
			else if(l1.get(id1).docId<l2.get(id2).docId)
				id1++;
			else
				id2++;
		}		
		return mergedList;
	}

	/**
	 * 
	 * @param query a phrase query that consists of any number of terms in the sequential order
	 * @return ids of documents that contain the phrase
	 */
	public ArrayList<DocId> phraseQuery(String[] query)
	{
		//maintaining postings list for each query term
		HashMap<Integer,ArrayList<DocId>> docMap = new HashMap<Integer,ArrayList<DocId>>();
		ArrayList<Integer>[] resultList = new ArrayList[query.length];
		ArrayList<DocId> interSectionList;
		ArrayList<DocId> finalDocList = new ArrayList<DocId>();
		int count=0,j=0;
		//for each query term
		for(String s : query)
		{
			if(termList.contains(s))
			{
				//adding docList into the posting list
				docMap.put(count,docLists.get(termList.indexOf(s)));
				count++;
			}
			else
			{
				continue;
			} 
		}
		for(int i=0;i<query.length-1;i++)
		{
			resultList[i]=new ArrayList();
			//for each of the two successive query terms
			if(docMap.get(i)!=null && docMap.get(i+1)!=null)
			{
				//getting an intersection list of DocIds using the intersect method
				interSectionList = intersect(docMap.get(i),docMap.get(i+1));
				//add the document Ids into the resultList ; this is an intermediate list
				for(DocId d: interSectionList )
				{
					resultList[i].add(d.docId);
				}

			}
			else
			{
				System.out.println("No matches at intermedite list");
				continue;
			}
		}
		ArrayList<Integer> finalIdList = new ArrayList<Integer>(resultList[0]);
		// for each of the query terms
		while(j<query.length-1)
		{
			if(resultList[j]== null)
			{
				break;
			}
			else
			{
				// getting the intersection of all the resiltList integers(DocIds) using retainAll() function
				finalIdList.retainAll(resultList[j]);
				j++;
			}
		}
		// preparing the DocId objects to return 
		for(int docid :finalIdList)
		{
			finalDocList.add(new DocId(docid));
		}
		return finalDocList;

	}


	public static void main(String[] args)
	{
		String[] docs = {"new home sales top forecasts",
				"home sales rise in july",
				"increase in home sales in july",
				"july new home sales rise"
		};
		
		PositionalIndex pi = new PositionalIndex(docs);
		System.out.print(pi);
		pi.preProcess("new","home");
		//TASK4: TO BE COMPLETED: design and test phrase queries with 2-5 terms
		ArrayList<DocId> phraseResult = pi.phraseQuery(args);
		if(phraseResult!=null)
		{
			for(DocId d:phraseResult)
			{
				System.out.println("Match Found with: "+docs[d.docId]);
			}
		}
		else
		{
			System.out.println("No Math Found");
		}

	}

	private void preProcess(String q1, String q2) {
		ArrayList<DocId> l1 = docLists.get(termList.indexOf(q1));
		ArrayList<DocId> l2 = docLists.get(termList.indexOf(q2));
		intersect(l1,l2);
	}
}

/**
 * 
 * @author qyuvks
 * Document id class that contains the document id and the position list
 */
class DocId{
	int docId;
	ArrayList<Integer> positionList;
	public DocId(int did)
	{
		docId = did;
		positionList = new ArrayList<Integer>();
	}
	public DocId(int did, int position)
	{
		docId = did;
		positionList = new ArrayList<Integer>();
		positionList.add(new Integer(position));
	}

	public void insertPosition(int position)
	{
		positionList.add(new Integer(position));
	}

	public String toString()
	{
		String docIdString = ""+docId + ":<";
		for(Integer pos:positionList)
			docIdString += pos + ",";
		docIdString = docIdString.substring(0,docIdString.length()-1) + ">";
		return docIdString;		
	}
}
