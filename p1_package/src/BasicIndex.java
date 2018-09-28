/**
 * BasicIndex.java
 * Project 1
 * YouGle: Your First Search Engine
 * Created by 
 * 1. Peerachai  Banyongrakkul  Sec.1  5988070
 * 2. Sakunrat  Nunthavanich  Sec.1  5988095
 * 3. Boonyada  Lojanarungsiri  Sec.1  5988153
 */
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.List;



public class BasicIndex implements BaseIndex {

	@Override
	public PostingList readPosting(FileChannel fc) {
		/*
		 * TODO: Your code here
		 *       Read and return the postings list from the given file.
		 */
		ByteBuffer buf;
		int termID, docFreq, docID;
		List<Integer> eachPostList = new ArrayList<Integer>();;
		buf = ByteBuffer.allocate(4);		// Integer are 32 bits and Byte are 8 bits so 32/8 = 4
		//reading file to get termID for creating PostingList
		try {
			if(fc.read(buf) <= 0)
			{
				return null;
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		buf.flip();
		termID = buf.getInt();
		
		//reading file to get dicFreq for creating PostingList
		buf = ByteBuffer.allocate(4);
		try {
			fc.read(buf);
		} catch (IOException e) {
			e.printStackTrace();
		}
		buf.flip();
		docFreq = buf.getInt();
		//looping for keeping all docID that contains the term for storing in PostingList
		for(int i = 0 ; i < docFreq ; i++)
		{
			buf = ByteBuffer.allocate(4);
			try {
				fc.read(buf);
			} catch (IOException e) {
				e.printStackTrace();
			}
			buf.flip();
			docID = buf.getInt();
			eachPostList.add(docID);
		}
		
		PostingList postList = new PostingList(termID,eachPostList);
		return postList;
	}

	@Override
	public void writePosting(FileChannel fc, PostingList p) {
		/*
		 * TODO: Your code here
		 *       Write the given postings list to the given file.
		 */
		ByteBuffer buf;
		int termID, docFreq;
		buf = ByteBuffer.allocate((p.getList().size()+1+1)*4);
		termID = p.getTermId();
		docFreq = p.getList().size();
		buf.putInt(termID);
		buf.putInt(docFreq);
		for(int i = 0 ; i < p.getList().size() ; i++)
		{
			buf.putInt(p.getList().get(i));
		}
		buf.flip();
		while(buf.hasRemaining())
		{
			try {
				fc.write(buf);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

	}
}

