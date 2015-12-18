package freemark;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;

import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;

public class FMTest {
	
	public static Template newSyntaxFreemarkerTemplate(Reader reader,Configuration conf) throws IOException {
        String templateString = IOUtils.toString(reader);
        conf.setTagSyntax(Configuration.SQUARE_BRACKET_TAG_SYNTAX);
        templateString = StringUtils.replace(templateString, "${", "${'$'}{");
        templateString = StringUtils.replace(templateString, "@{", "${");
        Template t = new Template("A",new StringReader(templateString),conf);
        return t;
	}
	
	public static String processTemplateIntoString(Template template, Object model)
			throws IOException, TemplateException {
		StringWriter result = new StringWriter();
		template.process(model, result);
		return result.toString();
	}
	
	public static String fromXml(Configuration conf, Map params,InputStream input) throws IOException, TemplateException {
		
		InputStreamReader reader = new InputStreamReader(input);
		Template template = newSyntaxFreemarkerTemplate(reader,conf);
		String xml = processTemplateIntoString(template, params);
		return xml;
	}

	public static void main(String[] args) throws Exception{
		Configuration cfg = new Configuration();
		cfg.setDirectoryForTemplateLoading(new File("D:/developments/eclipse-workspaces/ws-jee/xy-test/src/main/resources/templates") );
		
		Map<String, String> params = new HashMap<>();
//		InputStream input;
//		String s = fromXml(cfg, params, input);
//		System.out.println(s);
		
		File f = new File("D:/developments/eclipse-workspaces/ws-jee/xy-test/src/main/resources/templates/main.xml");
		FileReader fr = new FileReader(f);
		
		params.put("day", "2015-01-01");
		//Template temp = cfg.getTemplate("main.xml");
		Template temp = newSyntaxFreemarkerTemplate(fr, cfg);
		
		Writer out = new OutputStreamWriter(System.out);
		temp.process(params, out);
		out.flush();
	}
}
