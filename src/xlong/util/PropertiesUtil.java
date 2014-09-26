package xlong.util;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Enumeration;
import java.util.Properties;


/**
 * This is a utility class for managing properties.
 * 
 * @author Xiang Long (longx13@mails.tsinghua.edu.cn)
 *
 */
public class PropertiesUtil {
	
	private static final String defaultPropertiesFile = "default.properties";
	private static final String propertiesFile = "info.properties";
	private static final String[][] defaultPropertiesList = {
		{"DBpedia_external_links.nt", "E:\\longx\\data\\external_links_en.nt"},
		{"DBpedia_instance_types.nt", "E:\\longx\\data\\instance_types_en.nt"},
		{"DBpedia_ontology.owl", "E:\\longx\\data\\dbpedia_2014.owl"},
		{"mySpliter", " |-| "},
		{"mySpliterReg", " \\|-\\| "},
		};
	
	private static Properties properties = null;
    
    public static void listAllProperties(String filePath) throws IOException {
        Properties pps = new Properties();
        
        BufferedInputStream in = 
        		new BufferedInputStream(
        				new FileInputStream(filePath));
        pps.load(in);
        in.close();
        Enumeration<?> en = pps.propertyNames();
        
        while(en.hasMoreElements()) {
            String strKey = (String) en.nextElement();
            String strValue = pps.getProperty(strKey);
            System.out.println(strKey + " = " + strValue);
        }   
    }
    
    public static void listAllProperties() throws IOException {
    	listAllProperties(propertiesFile);
    }
    
    public static boolean loadProperties(String filePath) {
    	Properties pps = new Properties();
        try {
        	BufferedInputStream in = 
        			new BufferedInputStream (
        					new FileInputStream(filePath));  
            pps.load(in);
            in.close();
            properties = pps;
            return true;
            
        }catch (IOException e) {
        	System.out.println("Can't load properties file.");
            return false;
        }  	
    }
    
    public static boolean loadProperties() {
    	if (Files.exists(Paths.get(propertiesFile))) {
    		return loadProperties(propertiesFile);
    	} else {
    		System.out.println("Can't find properties file \"info.properties\"");
    		return false;
    	}
    }
    
    public static String getProperty(String key) {
    	if (properties == null) {
    		return null;
    	} else {
    		return properties.getProperty(key);
    	}
    }
    
    public static void WriteDefaultProperties(String file) throws IOException {
    	
    	System.out.println("Create default properties file \"" + file + "\"");
    	
        Properties pps = new Properties();
        
        BufferedOutputStream out =
        		new BufferedOutputStream(
        				new FileOutputStream(file));
        
        for (int i = 0; i < defaultPropertiesList.length; i++) {
        	pps.setProperty(defaultPropertiesList[i][0], defaultPropertiesList[i][1]);
        }
        
        pps.store(out, "Default properties.");
        out.close();
    }
    
    public static void setDefault() throws IOException {
    	WriteDefaultProperties(propertiesFile);
    }
    
    public static void init() throws IOException {
    	WriteDefaultProperties(defaultPropertiesFile);
    	WriteDefaultProperties(propertiesFile);
    }
    
    public static void main(String [] args) throws IOException{
    	System.out.println(String.valueOf(defaultPropertiesList.length));
    	init();
    	loadProperties();
    	System.out.println(getProperty("DBpedia_ontology.owl"));
    	listAllProperties();
    }

}
