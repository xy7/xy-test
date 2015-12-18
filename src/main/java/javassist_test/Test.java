package javassist_test;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.concurrent.ConcurrentHashMap;

import javassist.CannotCompileException;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtField;
import javassist.CtMethod;
import javassist.NotFoundException;

public class Test {
	public static void main(String[] args) 
			throws NotFoundException, CannotCompileException, InstantiationException, IllegalAccessException
			, IllegalArgumentException, InvocationTargetException, NoSuchMethodException, SecurityException {
		ClassPool classPoll = ClassPool.getDefault();
		CtClass ctClass = classPoll.get("javassist_test.AAA");
		CtField[] ctFields = ctClass.getDeclaredFields();
		for(CtField ctf:ctFields){
			String fieldName = ctf.getName();
			CtClass fieldCtClass = ctf.getType();
			String fieldType = fieldCtClass.getName();
			//System.out.println(ctf.toString() + " :" + fieldName + " - " + fieldType);
			//public CtMethod(CtClass returnType, String mname,
            //CtClass[] parameters, CtClass declaring) {
			String mName = "set" + fieldName.substring(0, 1).toUpperCase() + (fieldName.length()>1?fieldName.substring(1):"");
			CtClass[] margs = new CtClass[1];
			margs[0] = fieldCtClass;
			CtMethod m = new CtMethod(CtClass.voidType, mName, margs, ctClass);
			m.setBody("{this."+fieldName+"=$1;System.out.println(1234);}");
			ctClass.addMethod(m);
			
			String getName = "get" + fieldName.substring(0, 1).toUpperCase() + (fieldName.length()>1?fieldName.substring(1):"");
			CtMethod getM = new CtMethod(fieldCtClass, getName, null, ctClass);
			getM.setBody("{System.out.println(2345);return this."+fieldName+";}");
			ctClass.addMethod(getM);
		}
		AAA a=(AAA)ctClass.toClass().newInstance();
		System.out.println(a);
		
		Method ms = a.getClass().getDeclaredMethod("setI", Integer.TYPE);
		ms.invoke(a, 123);
		Method getms = a.getClass().getDeclaredMethod("getI", null);
		System.out.println(getms.invoke(a));
		System.out.println(a);
		
		//ConcurrentHashMap c;
	}

}
