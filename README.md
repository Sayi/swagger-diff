# swagger-diff
![Build Status](https://travis-ci.org/Sayi/swagger-diff.svg?branch=master)

自动生成API ChangeLog组件  
用来比较两个由Swagger生成的API文档，对参数、返回类型、路径进行深度比较，并输出差异，适用于自动生成接口变更文档。 


特性如下：

* Support swagger1.2 and swagger2.0   
* Support HTTP请求方法比较: get post put delete...
* Support Requestbody参数比较
* Support API文档的鉴权Auth读取
* EL(Expression Language)表达式展现
* HTML 渲染输出
* markdown 渲染
 

# 使用
```xml
    <dependency>
        <groupId>com.deepoove</groupId>
        <artifactId>swagger-diff</artifactId>
	    <version>1.0.1</version>
    </dependency>
```

# Usage示例(SwaggerDiffTest)

```java
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
```

# HTML 渲染
![image](./changelog.png)

# Markdown 渲染
	### What's New
	---
	
	### What's Deprecated
	---
	
	### What's Changed
	---
	* `PUT` /pet Update an existing pet  
	    参数
	
	        Add body.newFeild //a feild demo by sayi
	        Add body.category.newCatFeild
	        Delete body.category.name
	* `POST` /pet Add a new pet to the store  
	    参数
	
	        Add tags //add new query param demo
	        Add body.newFeild //a feild demo by sayi
	        Add body.category.newCatFeild
	        Delete body.category.name
	* `GET` /pet/{petId} Find pet by ID  
	    返回类型
	
	        Add newFeild //a feild demo by sayi
	        Add category.newCatFeild
	        Delete category.name
	* `POST` /pet/{petId} Updates a pet in the store with form data  
	    参数
	
	        Add newFormDataParam //form data param demo
	* `DELETE` /pet/{petId} Deletes a pet  
	    参数
	
	        Add newHeaderParam
	* `POST` /user Create user  
	    参数
	
	        Add body.newUserFeild //a new user feild demo
	        Delete body.phone
	* `GET` /user/login Logs user into the system  
	    参数
	
	        Delete password //The password for login in clear text
	* `GET` /user/{username} Get user by user name  
	    返回类型
	
	        Add newUserFeild //a new user feild demo
	        Delete phone
	* `PUT` /user/{username} Updated user  
	    参数
	
	        Add body.newUserFeild //a new user feild demo
	        Delete body.phone


# 思路
![image](./swagger-diff.png)



