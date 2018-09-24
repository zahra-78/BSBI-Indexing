/**
 * Query.java
 * Project 1
 * YouGle: Your First Search Engine
 * Created by 
 * 1. Peerachai  Banyongrakkul  Sec.1  5988070
 * 2. Sakunrat  Nunthavanich  Sec.1  5988095
 * 3. Boonyada  Lojanarungsiri  Sec.1  5988153
 */
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.RandomAccessFile;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class Query {

	// Term id -> position in index file
	private  Map<Integer, Long> posDict = new TreeMap<Integer, Long>();
	// Term id -> document frequency
	private  Map<Integer, Integer> freqDict = new TreeMap<Integer, Integer>();
	// Doc id -> doc name dictionary
	private  Map<Integer, String> docDict = new TreeMap<Integer, String>();
	// Term -> term id dictionary
	private  Map<String, Integer> termDict = new TreeMap<String, Integer>();
	// Index
	private  BaseIndex index = null;
	

	//indicate whether the query service is running or not
	private boolean running = false;
	private RandomAccessFile indexFile = null;
	
	/* 
	 * Read a posting list with a given termID from the file 
	 * You should seek to the file position of this specific
	 * posting list and read it back.
	 * */
	private  PostingList readPosting(FileChannel fc, int termId)
			throws IOException {
		/*
		 * TODO: Your code here
		 */
		PostingList posList;
		if(posDict.containsKey(termId))
		{
			posList = index.readPosting(fc.position(posDict.get(termId)));
			return posList; 
		}
		else
		{
			return null;
		}
	}
	
	
	public void runQueryService(String indexMode, String indexDirname) throws IOException
	{
		//Get the index reader
		try {
			Class<?> indexClass = Class.forName(indexMode+"Index");
			index = (BaseIndex) indexClass.newInstance();
		} catch (Exception e) {
			System.err
					.println("Index method must be \"Basic\", \"VB\", or \"Gamma\"");
			throw new RuntimeException(e);
		}
		
		//Get Index file
		File inputdir = new File(indexDirname);
		if (!inputdir.exists() || !inputdir.isDirectory()) {
			System.err.println("Invalid index directory: " + indexDirname);
			return;
		}
		
		/* Index file */
		indexFile = new RandomAccessFile(new File(indexDirname,
				"corpus.index"), "r");

		String line = null;
		/* Term dictionary */
		BufferedReader termReader = new BufferedReader(new FileReader(new File(
				indexDirname, "term.dict")));
		while ((line = termReader.readLine()) != null) {
			String[] tokens = line.split("\t");
			termDict.put(tokens[0], Integer.parseInt(tokens[1]));
		}
		termReader.close();

		/* Doc dictionary */
		BufferedReader docReader = new BufferedReader(new FileReader(new File(
				indexDirname, "doc.dict")));
		while ((line = docReader.readLine()) != null) {
			String[] tokens = line.split("\t");
			docDict.put(Integer.parseInt(tokens[1]), tokens[0]);
		}
		docReader.close();

		/* Posting dictionary */
		BufferedReader postReader = new BufferedReader(new FileReader(new File(
				indexDirname, "posting.dict")));
		while ((line = postReader.readLine()) != null) {
			String[] tokens = line.split("\t");
			posDict.put(Integer.parseInt(tokens[0]), Long.parseLong(tokens[1]));
			freqDict.put(Integer.parseInt(tokens[0]),
					Integer.parseInt(tokens[2]));
		}
		postReader.close();
		
		this.running = true;
	}
    
	public List<Integer> retrieve(String query) throws IOException
	{	
		if(!running) 
		{
			System.err.println("Error: Query service must be initiated");
		}
		/*
		 * TODO: Your code here
		 *       Perform query processing with the inverted index.
		 *       return the list of IDs of the documents that match the query
		 *      
		 */
		FileChannel g;
		g = indexFile.getChannel();
		//pos List is to keep postinglist of the queries that contains in the term
		List<PostingList> pos = new ArrayList<PostingList>();
		//listDocID List is to keep the docId of the queries that contains in term
		List<Integer> listDocID = new LinkedList<Integer>();
		int check = 0;
		//check whether the query have whitespace(" ") or not, if yes
		//then, split the whitespace(" ") and check the split query whether it contains in the terms or not
		//if yes, then add its postinglist to the pos List
		if(query.contains(" "))
		{
			String newTerm[];
			newTerm = query.split(" ");
			for(int i = 0 ; i < newTerm.length ; i++)
			{
				for(String term : termDict.keySet()) 
				{
					  if(newTerm[i].equals(term))
					  {
						  PostingList p = readPosting(g,termDict.get(term));
						  pos.add(p);
						  check++;
					  }
				}
			}
			if(check != newTerm.length)
			{
				return null;
			}
			//if pos List have more than 1 postinglist(the query contains whitespace(" "))
			//intersect its postinglists and add into pos List
			while(pos.size() > 1)
			{
				int i = 0;
				PostingList newPos = intersect(pos.get(i),pos.get(i+1));
				pos.remove(i);
				pos.add(newPos);
				pos.remove(i);
			}
			for(int i = 0; i<pos.get(0).getList().size() ; i++)
			{
				listDocID.add(pos.get(0).getList().get(i));
				
			}
		}
		else
		{
			for(String term : termDict.keySet())
			{
				if(query.equals(term))
				{
					PostingList p = readPosting(g,termDict.get(term));
					for(int i = 0 ; i < p.getList().size() ; i++)
					{
						
						listDocID.add(p.getList().get(i));
						check = 1;
					}
				}
			}
		}
		if(check == 0)
		{
			return null;
		}
		
		
		return listDocID;
		
	}
	
    String outputQueryResult(List<Integer> res) {
        /*
         * TODO: 
         * 
         * Take the list of documents ID and prepare the search results, sorted by lexicon order. 
         * 
         * E.g.
         * 	0/fine.txt
		 *	0/hello.txt
		 *	1/bye.txt
		 *	2/fine.txt
		 *	2/hello.txt
		 *
		 * If there no matched document, output:
		 * 
		 * no results found
		 * 
         * */
    	String result = "";
    	if(res != null) 
    	{
        	String[] output = new String[res.size()];
    		for(int i = 0 ; i < res.size() ; i++)
    		{
				if(i == res.size()-1)
				{
					output[i] = docDict.get(res.get(i));
				}
				else
				{
					output[i] = docDict.get(res.get(i))+"\n";
				}
    		}
        	for(int i = 0 ; i<output.length-1 ; i++)
        	{
        		for(int j = 0 ; j< output.length-i-1 ; j++)
        		{
        			if(output[j].compareTo(output[j+1]) > 0)
        			{
        				String temp = output[j];
        				output[j] = output[j+1];
        				output[j+1] = temp;
        			}
        		}
        	}
        	for(int i = 0 ; i < output.length ; i++)
        	{
        		result += output[i];
        	}
        	return result;
    	}
    	else
    	{
        	return new String("no results found");
    	}
    }
    
    private PostingList intersect(PostingList p1, PostingList p2)
    {
    	PostingList newP;
    	ArrayList<Integer> docList = new ArrayList<Integer>();
    	int i = 0;
    	int j = 0;
    	
    	while(true)
    	{
    		if(i>p1.getList().size()-1 || j>p2.getList().size()-1)
    		{
    			break;
    		}
			
	    	if(p1.getList().get(i).equals(p2.getList().get(j)))
	    	{
	    		docList.add(p1.getList().get(i));
	    		i++;
	    		j++;	
	    		
	   		}
	    	else
	    	{
    			if(p1.getList().get(i) > p2.getList().get(j))
	   			{
	    			j++;
	    		}
	    		else if(p1.getList().get(i) < p2.getList().get(j))
    			{
	   				i++;
	   			}
	    	}
	    	
	    	
			
    	}
    	newP = new PostingList(p1.getTermId(),docList);
    	return newP;
    }
	
	public static void main(String[] args) throws IOException {
		/* Parse command line */
		if (args.length != 2) {
			System.err.println("Usage: java Query [Basic|VB|Gamma] index_dir");
			return;
		}

		/* Get index */
		String className = null;
		try {
			className = args[0];
		} catch (Exception e) {
			System.err
					.println("Index method must be \"Basic\", \"VB\", or \"Gamma\"");
			throw new RuntimeException(e);
		}

		/* Get index directory */
		String input = args[1];
		
		Query queryService = new Query();
		queryService.runQueryService(className, input);
		
		/* Processing queries */
		BufferedReader br = new BufferedReader(new InputStreamReader(System.in));

		/* For each query */
		String line = null;
		while ((line = br.readLine()) != null) {
			List<Integer> hitDocs = queryService.retrieve(line);
			queryService.outputQueryResult(hitDocs);
		}
		
		br.close();
	}
	
	protected void finalize()
	{
		try {
			if(indexFile != null)indexFile.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
 
