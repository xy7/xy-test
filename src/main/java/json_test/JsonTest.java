package json_test;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAccessor;
import java.util.List;
import java.util.Locale;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;

public class JsonTest implements Serializable{
	
	public String trade_no = "abc123";
	public int money = 100;
	
	@Override
	public String toString() {
		return JSON.toJSONString(this);
	}
	
	//保存对象到文件中
	public static void writeObjectToFile(List<Object> objs){
        File file =new File("C:/Users/wangxiaoyang.RDEV/Desktop/tmp2/test.dat");
        FileOutputStream out;
        try {
            out = new FileOutputStream(file);
 
            for(Object obj:objs){
            	out.write(obj.toString().getBytes());
            	out.write('\n');
            }
            	
            System.out.println("write object success!");
        } catch (IOException e) {
            System.out.println("write object failed");
            e.printStackTrace();
        }
    }
	
	//从文件中读取对象
	public static Object readObjectFromFile(){
        Object temp=null;
        File file =new File("C:/Users/wangxiaoyang.RDEV/Desktop/tmp2/test.dat");
        FileInputStream in;
        try {
            BufferedReader br = new BufferedReader(new FileReader(file));
            String str;
            while ((str = br.readLine())!=null){
            	Class<JsonTest> clazz = JsonTest.class;
            	JSONObject jo = JSON.parseObject(str);
            	System.out.println(jo.getString("trade_no"));
            	System.out.println(jo.getIntValue("money"));
            }
            System.out.println("read object success!");
        } catch (IOException e) {
            System.out.println("read object failed");
            e.printStackTrace();
        }
        return temp;
    }

	public static void main(String[] args){

		/*String s = "2015-10-09";
		DateTimeFormatter dayFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd", Locale.CHINA);
		System.out.println(LocalDateTime.parse(s, dayFormatter));*/
		
		DateTimeFormatter minuteFormatter2 = DateTimeFormatter.ofPattern("yyyy-MM-dd");
		
	
		TemporalAccessor ta = minuteFormatter2.parse("2013-12-30");
		
		System.out.println(ta);
		
		LocalDateTime ldt = LocalDateTime.from(ta);

		System.out.println(ldt);
		//LocalDateTime ldt = LocalDateTime.parse("2015-10-09 19:06:10", minuteFormatter);
		//System.out.println(ldt);
		

	}
}
