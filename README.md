# swagger-diff
自动生成API ChangeLog组件  
用来比较两个由Swagger生成的API文档，对参数、返回类型、路径进行深度比较，并输出差异，适用于自动生成接口变更文档。 


特性如下：

* Support swagger1.2 and swagger2.0   
* Support HTTP请求方法比较: get post put delete...
* Support Requestbody参数比较
* Support API文档的鉴权Auth读取
* EL(Expression Language)表达式展现
* HTML 渲染输出
 

# 使用
    <dependency>
        <groupId>com.deepoove</groupId>
        <artifactId>swagger-diff</artifactId>
	    <version>1.0.0</version>
    </dependency>

# Usage示例(SwaggerDiffTest)
    SwaggerDiff diff = new SwaggerDiff(SWAGGER_V1_DOC, SWAGGER_V2_DOC,
				SwaggerDiff.SWAGGER_VERSION_V2).compare();
	List<Endpoint> newEndpoints = diff.getNewEndpoints(); //新增api
	List<Endpoint> missingEndpoints = diff.getMissingEndpoints(); //过时的api
	List<ChangedEndpoint> changedEndPoints = diff.getChangedEndpoints(); //变更的api
	
	String html = new HtmlRender("Changelog",
				"http://deepoove.com/swagger-diff/stylesheets/demo.css")
						.render(diff);
	try {
		FileWriter fw = new FileWriter("src/test/resources/testDiff.html");
		fw.write(html);
		fw.close();
	} catch (IOException e) {
		e.printStackTrace();
	} 

# HTML 渲染
![image](./changelog.png)

# 思路
![image](./swagger-diff.png)



