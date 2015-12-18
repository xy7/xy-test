package mahout;

import org.apache.mahout.math.RandomAccessSparseVector;
import org.apache.mahout.math.Vector;
import org.junit.Test;

public class TxtRecordFactoryTest {

	@Test
	public void test() {
		String line = "0.548616112\t0.405350996\t0.682121007\t0.606676886\t0.106404701";
		String[] columns = line.split("\t");

		Vector input = new RandomAccessSparseVector(5);

		for(int i=0;i<columns.length;i++){
			double d = Double.parseDouble(columns[i] );
			input.setQuick(i,  d);
			System.out.println(columns[i] + ": " + d);
		}
		
		System.out.println(input);
	}

}
