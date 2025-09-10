package com.jiawa.train.generator.gen;

import com.jiawa.train.generator.util.DbUtil;
import com.jiawa.train.generator.util.Field;
import com.jiawa.train.generator.util.FreemarkerUtil;
import freemarker.template.TemplateException;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Node;
import org.dom4j.io.SAXReader;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static com.jiawa.train.generator.util.DbUtil.getJavaTypes;

public class ServerGenerator {
    static boolean readOnly = false;/*权限管理，如果不是会员，部分功能不会对其开放。*/
    static String vuePath = "admin/src/views/main/";
    static String serverPath = "[module]/src/main/java/com/jiawa/train/[module]/";
    static String pomPath ="generator/pom.xml";
    static {
      new  File(serverPath).mkdirs();
    }
    public static void main(String[] args) throws Exception {
        /*获取mybatis-generator*/
        String generatorPath = getGeneratorPath();
        /*比如generator-config-member.xml，得到module = member*/
        String module = generatorPath.replace("src/main/resources/generator-config-","").replace(".xml","");
        System.out.println("module: "+module);

        serverPath = serverPath.replace("[module]",module);//将[module]替换成真正的模块名字
        System.out.println("servicePath " + serverPath);

        //读取table节点 取generator-config-xxx.xml文件中的table属性
        Document document = new SAXReader().read("generator/" + generatorPath);/*generatorPath是src/main/resources/generator-config-，前面填上gen
                                                                                        erator模块名 加起来就是generator/src/main/resources/generator-config-*/
        Node table = document.selectSingleNode("//table");//取generator-config-xxx.xml文件中的table属性
        System.out.println(table);
        Node tableName = table.selectSingleNode("@tableName");//前面加上@就是查询属性 读取tableName，domainObjectName这两个属性
        Node domainObjectName = table.selectSingleNode("@domainObjectName");
        System.out.println(tableName.getText() + "/" + domainObjectName.getText());




        // 为DbUtil设置数据源,连接数据库的配置
        Node connectionURL = document.selectSingleNode("//@connectionURL");
        Node userId = document.selectSingleNode("//@userId");
        Node password = document.selectSingleNode("//@password");
        System.out.println("url: " + connectionURL.getText());
        System.out.println("user: " + userId.getText());
        System.out.println("password: " + password.getText());
        DbUtil.url = connectionURL.getText();
        DbUtil.user = userId.getText();
        DbUtil.password = password.getText();


        /*更改ftl模板文件中的￥{domain}变量*/
        // 示例：表名 jiawa_test
        // Domain = JiawaTest
        String Domain = domainObjectName.getText();//获取domain中的单词 开头是大写的
        // domain = jiawaTest
        String domain = Domain.substring(0, 1).toLowerCase() + Domain.substring(1);//将大写开头换为小写
        // do_main = jiawa-test
        String do_main = tableName.getText().replaceAll("_", "-");

        // 表中文名
        String tableNameCn = DbUtil.getTableComment(tableName.getText());
        List<Field> fieldList = DbUtil.getColumnByTableName(tableName.getText());
        Set<String> typeSet = getJavaTypes(fieldList);

        // 组装参数
        Map<String, Object> param = new HashMap<>();
        param.put("module", module);
        param.put("Domain", Domain);
        param.put("domain", domain);
        param.put("do_main", do_main);
        param.put("tableNameCn", tableNameCn);
        param.put("fieldList", fieldList);
        param.put("typeSet", typeSet);
        param.put("readOnly", readOnly);
        System.out.println("组装参数：" + param);

        gen(Domain, param,"service","service");
        gen(Domain, param,"controller","controller");
        gen(Domain, param,"req","saveReq");
        gen(Domain, param, "req", "queryReq");
        gen(Domain, param, "resp", "queryResp");
        genVue(do_main, param);
    }
    /*将输出写成一个方法*/
    private static void gen(String Domain, Map<String, Object> param,String packageName,String target) throws IOException, TemplateException {
        FreemarkerUtil.initConfig(target + ".ftl");//生成代码要使用的模板名字（需要更改）
        String toPath = serverPath + packageName + "/";
        new  File(toPath).mkdirs();
        String Target = target.substring(0,1).toUpperCase() + target.substring(1);
        String fileName =toPath + Domain + Target + ".java";
        System.out.println("开始生成: " + fileName);
        FreemarkerUtil.generator(fileName, param);//生成代码要使用的路径，和生成文件的类名
    }

    private static void genVue(String do_main, Map<String, Object> param) throws IOException, TemplateException {
        FreemarkerUtil.initConfig("vue.ftl");
        new File(vuePath).mkdirs();
        String fileName = vuePath + do_main + ".vue";
        System.out.println("开始生成：" + fileName);
        FreemarkerUtil.generator(fileName, param);
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
