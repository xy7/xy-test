
public class TestFunc {
	/**
     * @author lcq
     * @date 2013-4-8
     * @param args
     */
    public static void main(String[] args) {
        String a = "a";
        String b = "b";
        operator(a, b);
        StringBuilder x = new StringBuilder("x"); 
        StringBuilder y = new StringBuilder("y");
        operator(x, y);
        System.out.println(a + "," + b);
        System.out.println(x + "," + y);
    }

    public static void operator(String a, String b) {
        a += b;
        b = a;
    }

    public static void operator(StringBuilder a, StringBuilder b) {
        a.append(b);
        b = a;
    }
}
