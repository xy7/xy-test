package file_oper;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.LineIterator;

public class FileOper {

	public static void main(String[] args) throws IOException {
		
		Map<String, Map<Integer, Integer>> map = new HashMap<>();
		
		File file = new File("E:/tmp/000000_0");
		LineIterator it = FileUtils.lineIterator(file);
		int i = 0;
		while(it.hasNext()){
  			String line = it.nextLine();
  			String[] arr = line.split("\1");
  			if(arr.length < 3){
  				System.out.println("error:" + line);
  				continue;
  			}
  			
  			int daydiff = Integer.parseInt(arr[0]);
  			String accountId = arr[1];
  			int cnt = Integer.parseInt(arr[2]);
  			if(cnt > 28)
  				cnt = 28;
  			
  			Map<Integer, Integer> m = map.getOrDefault(accountId, new HashMap<Integer, Integer>());
  			m.put(daydiff, cnt);
  			map.put(accountId, m);
  			if(++i%1000 == 0)
  				System.out.println(i);
		}
		if(it != null){
			LineIterator.closeQuietly(it);
		}
		
		
		File resFile = new File("E:/tmp/1105_account_heartcnt");
		int ii = 0;
		List<String> dataList = new ArrayList<>();
		for(Map.Entry<String, Map<Integer, Integer>> e:map.entrySet()){
			String accountId = e.getKey();
			Map<Integer, Integer> m = e.getValue();
			
			StringBuilder sb = new StringBuilder();
			sb.append(accountId);
			for(int j=0;j<30;j++){
				sb.append("\t");
				sb.append(m.getOrDefault(j, 0));
			}
			String data = sb.toString();
			dataList.add(data);
			if(++ii%1000 == 0)
  				System.out.println(ii);
		}
		
		dataList.sort(new Comparator<String>(){

			@Override
			public int compare(String o1, String o2) {
				// TODO Auto-generated method stub
				int i1 = 0;
				String[] arr1 = o1.split("\t");
				for(int j=1;j<30;j++)
					i1 += Integer.parseInt(arr1[j]);
				int i2 = 0;
				String[] arr2 = o2.split("\t");
				for(int j=1;j<30;j++)
					i2 += Integer.parseInt(arr2[j]);
				
				if(i1>i2)
					return -1;
				else if(i1==i2)
					return 0;
				else 
					return 1;
			}
			
		});
		FileUtils.writeLines(resFile, dataList);

	}

}
