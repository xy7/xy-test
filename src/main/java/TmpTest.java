import java.util.HashMap;
import java.util.Map;


public class TmpTest {

	public static void main(String[] args) {
		Map<Object, Integer> map = new HashMap<>();
		int maxValue = -1;
		Object maxKey = null;
		for(Object key:map.keySet()){
			if(map.get(key) > maxValue){
				maxKey = key;
				maxValue = map.get(key);
			}
		}
	}
}
