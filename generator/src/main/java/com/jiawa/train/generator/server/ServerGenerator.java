package com.jiawa.train.generator.server;

import com.jiawa.train.generator.util.FreemarkerUtil;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Node;
import org.dom4j.io.SAXReader;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

public class ServerGenerator {
    static String servicePath = "[module]/src/main/java/com/jiawa/train/[module]/service/";
    static String pomPath ="generator/pom.xml";
    static {
      new  File(servicePath).mkdirs();
    }
    public static void main(String[] args) throws Exception {
        /*获取mybatis-generator*/
        String generatorPath = getGeneratorPath();
        /*比如generator-config-member.xml，得到module = member*/
        String module = generatorPath.replace("src/main/resources/generator-config-","").replace(".xml","");
        System.out.println("module: "+module);

        servicePath = servicePath.replace("[module]",module);//将[module]替换成真正的模块名字
        System.out.println("servicePath " + servicePath);

        //读取table节点 取generator-config-xxx.xml文件中的table属性
        Document document = new SAXReader().read("generator/" + generatorPath);/*generatorPath是src/main/resources/generator-config-，前面填上gen
                                                                                        erator模块名 加起来就是generator/src/main/resources/generator-config-*/
        Node table = document.selectSingleNode("//table");//取generator-config-xxx.xml文件中的table属性
        System.out.println(table);
        Node tableName = table.selectSingleNode("@tableName");//前面加上@就是查询属性 读取tableName，domainObjectName这两个属性
        Node domainObjectName = table.selectSingleNode("@domainObjectName");
        System.out.println(tableName.getText() + "/" + domainObjectName.getText());

/*更改ftl模板文件中的￥{domain}变量*/
        // 示例：表名 jiawa_test
        // Domain = JiawaTest
        String Domain = domainObjectName.getText();//获取domain中的单词 开头是大写的
        // domain = jiawaTest
        String domain = Domain.substring(0, 1).toLowerCase() + Domain.substring(1);//将大写开头换为小写
        // do_main = jiawa-test
        String do_main = tableName.getText().replaceAll("_", "-");

        // 组装参数
        Map<String, Object> param = new HashMap<>();
        param.put("Domain", Domain);
        param.put("domain", domain);
        param.put("do_main", do_main);
        System.out.println("组装参数：" + param);

        FreemarkerUtil.initConfig("service.ftl");//生成代码要使用的模板名字（需要更改）

        FreemarkerUtil.generator(servicePath + Domain + "Service.java",param);//生成代码要使用的路径，和生成文件的类名

    }
    /*读一个xml文件*/
    private static String getGeneratorPath() throws DocumentException {
        SAXReader saxReader = new SAXReader();
        Map<String, String> map = new HashMap<String, String>();
        map.put("pom", "http://maven.apache.org/POM/4.0.0");
        saxReader.getDocumentFactory().setXPathNamespaceURIs(map);
        Document document = saxReader.read(pomPath);//读上面定义好的pom.xml文件中的内容 指向generator-config-xxx.xml
        Node node = document.selectSingleNode("//pom:configurationFile");
        System.out.println(node.getText());
        return node.getText();
    }

}
