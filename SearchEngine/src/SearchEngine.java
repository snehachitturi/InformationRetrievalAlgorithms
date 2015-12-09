import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

//import sun.security.mscapi.KeyStore.MY;


public class SearchEngine {
	
	String[] myDocs;
	ArrayList<String> termList;
	ArrayList<ArrayList<Integer>> docLists;
	
	
	HashSet<String> stopWords = new HashSet<String>();
	
	public String[] parse(String fileName)
	{
		String[] tokens = null;
		try{
			BufferedReader reader = new BufferedReader(new FileReader(fileName));
			String allLines = new String();
			String line = null;
			while((line=reader.readLine())!=null){
				allLines += line.toLowerCase(); //case folding
			}
			tokens = allLines.split("[\" .,?!:;$%()]+");
			
		}catch(IOException ioe){
			ioe.printStackTrace();
		}
		
		return tokens;
	}
	
	private void readStopWords(String stopFileName) throws IOException {
	
		try{
			BufferedReader reader = new BufferedReader(new FileReader(stopFileName));
			String line;
			while((line=reader.readLine())!=null){
				stopWords.add(line.toLowerCase()); //case folding
			}
		}catch(IOException ioe){
			ioe.printStackTrace();
		}
	}
	
	public void InvertedIndex(String[] docs)
	{
		myDocs = docs;
		termList = new ArrayList<String>();
		docLists = new ArrayList<ArrayList<Integer>>();
		ArrayList<Integer> docList;
		for(int i=0;i<myDocs.length;i++)
		{
			String[] tokens = myDocs[i].split(" ");
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
	
	public ArrayList<Integer> searchOr(String[] query)
	{
		ArrayList<Integer> mergeList=null;
		int docListLength;
		HashMap<Integer,List<String>> hp = new HashMap<Integer,List<String>>();
		List<String> keywords = Arrays.asList(query);
		for(String s : keywords)
		{
			docListLength = search(s).size();
			System.out.println("Keyword: "+s+" Length of DocList: "+docListLength);
			if(!hp.containsKey(docListLength))
			{
				hp.put(docListLength, new ArrayList<String>());
			}
			hp.get(docListLength).add(s);
		}
		System.out.println("AND Operation");
		for(Integer key:hp.keySet())
		{
			for(String shortWord:hp.get(key))
			{
				System.out.println("keyword: "+shortWord+" List: "+search(shortWord));
				if(search(shortWord)!=null)
				{
				if(mergeList==null)
				{
					mergeList = new ArrayList<Integer>(search(shortWord));
					
				}
				else
				{
					mergeList.retainAll(search(shortWord));
					System.out.println("And Operation result: "+mergeList);
				}
				}
			}
		}
		if(mergeList==null||mergeList.size()==0)
		{
			
			System.out.println("AND operation resulted no docs so performing OR operation");
			for(String s : query)
			{
				if(mergeList==null)
				{
					mergeList = new ArrayList<Integer>(search(s));
				}
				else
				{
					mergeList.addAll(search(s));
				}
			}
		}
		
		
			return mergeList;
	}
	public ArrayList<Integer> search(String query)
	{
		Stemmer st = new Stemmer();
		st.add(query.toCharArray(),query.length());
		st.stem();
		int index = termList.indexOf(st.toString());
		if(index <0)
			return null;
		return docLists.get(index);
	}
	
	public static void main(String[] args) throws IOException{
		SearchEngine se = new SearchEngine();
		String StopFileName="C://Users/sneha/Desktop/IR/SearchEngine/SearchEngine/src/stopwords.txt";
		se.readStopWords(StopFileName);
		ArrayList<String> docs = se.readFiles("C://Users/sneha/Desktop/IR/SearchEngine/SearchEngine/src/Lab1_Data");
		String[] docarr = docs.toArray(new String[docs.size()]);
		se.InvertedIndex(docarr);
		//System.out.println("Result for AND query!");
		ArrayList<Integer> result = se.searchOr(args);
		for(Integer i : result)
		{
			System.out.println(i.intValue());
		}
	}
	

	private ArrayList<String> readFiles(String folderName) {
		ArrayList<String> ret = new ArrayList<String>();
		File folder = new File(folderName);
		File[] listOfFiles = folder.listFiles();
		String[] docsInFolder = new String[listOfFiles.length];
		
		for(int i=0;i<listOfFiles.length;i++)
		{
			docsInFolder[i] = listOfFiles[i].getName();
		}
		for(int i=0;i<docsInFolder.length;i++){
			String[] tokens = parse(folderName + "/" + docsInFolder[i]);
			List<String> tokenList = Arrays.asList(tokens);
			String finalList = "";
			for(String token:tokenList){
				if(stopWords.contains(token)){
				}else{
					//System.out.println("token "+ token);
					Stemmer st = new Stemmer();
					st.add(token.toCharArray(),token.length());
					st.stem();
					finalList+= st.toString()+" ";
				}
			}
			ret.add(finalList);
			
		}
		return ret;
		
	}

	
}
