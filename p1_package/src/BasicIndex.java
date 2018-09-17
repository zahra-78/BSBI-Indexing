
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
		
		buf = ByteBuffer.allocate(4);
		try {
			fc.read(buf);
		} catch (IOException e) {
			e.printStackTrace();
		}
		buf.flip();
		docFreq = buf.getInt();
		
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
		//System.out.println(termID+" "+docFreq+" "+eachPostList.toString());
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

