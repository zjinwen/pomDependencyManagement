package pom.gen.depman;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.SAXReader;
import org.dom4j.io.XMLWriter;



public class PomRewrite {
	
	private String fileName;
	Document document;

	PomRewrite(String fileName) throws DocumentException{
		this.fileName=fileName;
		init();
	}
	
	private void init() throws DocumentException{
		  SAXReader reader = new SAXReader();
	      document = reader.read(fileName);
	}
	public  List<Element> getDependencies(){
		 @SuppressWarnings("unchecked")
		 List<Element> dependencies = document.getRootElement().element("dependencies").elements("dependency");
		 return dependencies;
	}
	public  List<Element> getManagementDependencies(){
		 @SuppressWarnings("unchecked")
		 List<Element> dependencies = document.getRootElement().element("dependencyManagement").element("dependencies").elements("dependency");
		 return dependencies;
	}
	
	
	public void testDup(){
		  //查找重复的
		  List<Element> dependencies = getDependencies();
	      int i=0;
	      int size=dependencies.size();
	      int countDup=0;
	      for(Element d:dependencies){
	    	 String groupId= text(d,"groupId");
	    	 String artifactId=  text(d,"artifactId");
	    	      i++;
	    	      for(int k=i;k<size;k++){
	    	    	    Element dSub = dependencies.get(k);
	    	    	     String groupIdSub= text(dSub,"groupId");
	    		    	 String artifactIdSub=  text(dSub,"artifactId");
	    		    	 if(groupId.equals(groupIdSub)&&artifactId.equals(artifactIdSub)){
	    		    		 System.out.println(this.fileName+" error dup。。"+countDup);
	    		    		 System.out.println("error dup。。"+countDup);
	    		    		  System.out.print(text(d,"groupId") +" ");
	    			    	  System.out.print(text(d,"artifactId") +" ");
	    			    	  System.out.print(text(d,"version")+" "+text(dSub,"version") +" ");
	    			    	  System.out.println(text(d,"scope"));
	    			    	  countDup++;
	    		    		 break;
	    		    	 }
	    	      }
	      }
	     
	      if(countDup>0){
	    	  System.out.println("有重复的。。"+countDup);
              throw new RuntimeException("有重复的。。"+countDup);
	      }
	}
	
	public List<Element> findHasVersion(String scopeHas){
		  testDup();
		
	      List<Element> versions=new ArrayList<Element>();
		  for(Element d:getDependencies()){
		    	 /*String groupId= text(d,"groupId");
		    	 String artifactId=  text(d,"artifactId");*/
		    	 String version=  text(d,"version");
		    	 String scope=  text(d,"scope");
		    	 boolean sp=false;
		    	 if(scopeHas==null){
		    		 sp=scope==null||scope.toLowerCase().trim().equals("compile");
		    	 }else{
		    		 if(scope==null){
		    			 sp=false;
		    		 }else{
		    			 sp=scope.trim().equalsIgnoreCase(scopeHas.trim());
		    		 }
		    	 }
		    	 if(sp&&version!=null&&version.length()>0){
			    	  versions.add(d);
		    	 }
		  }
	      return versions;
	}
	
	public static void print(List<Element> list){
		  for(Element d:list){
			  System.out.print(text(d,"groupId") +" ");
	    	  System.out.print(text(d,"artifactId") +" ");
	    	  System.out.print(text(d,"version") +" ");
	    	  System.out.println(text(d,"scope"));  
		  }
	}
	public static  String parentFile=null;
	

	public static List<String> getDirs() {
		if(parentFile==null)throw new RuntimeException("没有设置parent File ");
		File dir=new File(parentFile);
	      File[] dirs = dir.listFiles();
	      List<String> poms=new ArrayList<String>();
		    for(File file: dirs ){
		    	if(file.isDirectory()){
		    	String name=	file.getName();
		    	  if(!name.startsWith("."))
		    		 poms.add(name);
		    	}
		    };
		return poms;
	}

	static List<Element> removeManagementsDups(List<Element> managements) {
		List<Element> list=new ArrayList<Element>();
        for(Element e:managements){
        	  System.out.print(text(e,"groupId") +" ");
	    	  System.out.print(text(e,"artifactId") +" ");
	    	  System.out.print(text(e,"version") +" ");
	    	  System.out.println(text(e,"scope"));
	    	  
	    	  String groupId=text(e,"groupId") ;
	    	  String artifactId=text(e,"artifactId") ;
	    	  String version=text(e,"version");
	    	  String scope=text(e,"scope");
	    	  
	    	  boolean exist=false;
	    	  for(Element s:list){
	    		  String groupIdS=text(s,"groupId") ;
		    	  String artifactIdS=text(s,"artifactId") ;
		    	  String versionS=text(s,"version");
		    	  String scopeS=text(s,"scope");
		    	  if(groupId.equals(groupIdS)&&artifactId.equals(artifactIdS)){
		    		  if((version==null||version.equals(versionS))&&(scope==null||scope.equals(scopeS))){
		    			  exist=true;
		    			  break;
		    		  }else{
		    			  throw new RuntimeException(groupIdS+" "+artifactIdS+" has two versions");
		    		  }
		    	  }
	    	  }
	    	  if(!exist){
	    		  list.add(e.createCopy());
	    	  }
	    	  
        }
		return list;
	}

	static void addType(List<Element> list, List<Element> nl,String type) {
		for(Element e:list){
			    String scope=text(e,"scope");
			    if(type==null){
			    	if(scope==null||scope.equals("compile")){
				    	nl.add(e.createCopy());
				    }
			    }else if(scope!=null&&scope.equals(type)){
			    	nl.add(e.createCopy());
			    }
		   }
	}
	
	
	static void removeVersion(String file) throws DocumentException, IOException {
		String pom = getPomFile(file);
		PomRewrite pm = new PomRewrite(pom);
		List<Element> dependencies =pm.getDependencies();
        for(Element e:dependencies){
	       	e.remove(e.element("version"));
        }
        writeToFile(pm.document,pom);
        
	}
	public static void setManagementDependencies(Document document, List<Element>  ms){
		  Element root = document.getRootElement();
		  Element dm = root.element("dependencyManagement").element("dependencies");
		  boolean success=root.element("dependencyManagement").remove(dm);
		  if(!success)throw new RuntimeException("删除元素失败");
		  Element elemntNew = root.element("dependencyManagement").addElement("dependencies") ; 
		  for(Element e:ms){
			  elemntNew.add(e);
		  }
         
		  
	}
	

	
	static List<Element> orderByName(List<Element> managements) {
		 List<Element> result=new ArrayList<Element>(managements.size());
		 for(Element e:managements){
			 result.add(e.createCopy());
         }
		Collections.sort(result, new Comparator<Element>(){
			@Override
			public int compare(Element o1, Element o2) {
				 String o1groupId = text(o1,"groupId");
				 String o1artifactId=text(o1,"artifactId");
				 String o2groupId = text(o2,"groupId");
				 String o2artifactId=text(o2,"artifactId");
				 if(o1groupId.equals(o2groupId)){
					return  o1artifactId.compareTo(o2artifactId);
				 }else{
					 return  o1groupId.compareTo(o2groupId);
				 }
			}
			
		});
		 return result;
		
	}

	static void addToDm(String pnkCore, List<Element> managements,String scope) throws DocumentException {
		List<Element> hasVersion = new PomRewrite(getPomFile(pnkCore)).findHasVersion(scope);
        for(Element e:hasVersion){
       	 Element copy = e.createCopy();
       	 if(!managements.contains(copy)){
       		 String st = text(copy,"scope");
       		 if(st!=null&&st.equals("compile")){
       			copy.remove(copy.element("scope"));
       		 };
       		 managements.add(copy);
       	 }else{
       		 System.out.println("已经包含");
       	 }
        }
	}
	
	public static void writeToFile(Document document,String filname) throws IOException{
		    OutputFormat format = OutputFormat.createPrettyPrint();
	        XMLWriter writer = new XMLWriter(  new BufferedWriter(new OutputStreamWriter(new FileOutputStream(filname ),"utf-8")  ), format );
	        writer.write( document );
	        writer.close();
	}

	static String getPomFile( String pnkCore) {
		String file=parentFile;
		if(pnkCore!=null){
			file= new File(file,pnkCore).getAbsolutePath();
		}
		return new File(file,"pom.xml").getAbsolutePath();
	}

	private static String text(Element d,String tag) {
		if(d.element(tag)==null)return null;
		return d.elementText(tag).replace(" ","");
	}
}
