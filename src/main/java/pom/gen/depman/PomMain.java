package pom.gen.depman;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.dom4j.DocumentException;
import org.dom4j.Element;

public class PomMain {
	/**
	 * 
	 * @param args
	 * @throws DocumentException
	 * @throws IOException
	 */
	public static void main(String[] args) throws DocumentException, IOException {
		//设置项目目录 set project parent dir
		//PomRewrite.parentFile="D:\\workspace-master\\clearing";
		PomRewrite.parentFile="D:\\workspace-master\\exchange";
		List<String> poms = PomRewrite.getDirs();
        PomRewrite parent=new PomRewrite(PomRewrite.getPomFile(null));
        //删除ManagementDependencies 中重复依赖 remove dup ManagementDependencies
        List<Element> list = PomRewrite.removeManagementsDups(parent.getManagementDependencies());
       
        //根据 scope添加  add by scope firt compile last test.
   	    String[] scopes=new String[]{null,"provided","runtime","system","test"};
        for(String scope:scopes){
	        for(String file:poms){
	        	PomRewrite.addToDm(file, list,scope);
	        }
        }
      //根据 group name 排序 order by Name
        list=PomRewrite.orderByName(list);
       
  	   //根据 scope排序 order by scope
        List<Element> lastResult=new ArrayList<Element>(list.size());
  	   for(String scope:scopes){
  		 PomRewrite.addType(list, lastResult,scope);
  	   }
  	   
  	   //删除ManagementDependencies 中重复依赖 remove dup ManagementDependencies
  	   lastResult = PomRewrite.removeManagementsDups(lastResult);

  	   //设置ManagementDependencies set ManagementDependencies
  	    PomRewrite.setManagementDependencies(parent.document,lastResult);
  	    // 写入文件 write to File
  	    PomRewrite.writeToFile(parent.document,"output.xml");
        ///删除子module versions, del module version
        for(String file:poms){
        	 PomRewrite.removeVersion(file);
	    }
	}
}
