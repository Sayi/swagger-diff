# swagger-diff

![Build Status](https://travis-ci.org/Sayi/swagger-diff.svg?branch=master)

Compare two swagger API specifications(1.x or v2.0) and render the difference to html file or markdown file.

# Requriements
`jdk1.6+`

# Feature
* Supports swagger spec v1.x and v2.0.
* Depth comparison of parameters, responses, notes, http method(GET,POST,PUT,DELETE...)
* Supports swagger api Authorization
* Render difference of property with Expression Language
* html & markdown render

# Maven

```xml
<dependency>
        <groupId>com.deepoove</groupId>
        <artifactId>swagger-diff</artifactId>
	<version>1.1.0</version>
</dependency>
```

# Usage
SwaggerDiff can read swagger api spec from json file or http.
```java
final String SWAGGER_V2_DOC1 = "petstore_v2_1.json";
final String SWAGGER_V2_DOC2 = "http://petstore.swagger.io/v2/swagger.json";

SwaggerDiff diff = SwaggerDiff.compareV2(SWAGGER_V2_DOC1, SWAGGER_V2_DOC2);
```
# Swagger version
v1.x
```java
SwaggerDiff.compareV1(SWAGGER_V1_DOC1, SWAGGER_V1_DOC2);
```

v2.0
```java
SwaggerDiff.compareV2(SWAGGER_V2_DOC1, SWAGGER_V2_DOC2);
```

# Render difference
#### HTML
```java
String html = new HtmlRender("Changelog",
        "http://deepoove.com/swagger-diff/stylesheets/demo.css")
                .render(diff);

try {
    FileWriter fw = new FileWriter(
            "testNewApi.html");
    fw.write(html);
    fw.close();

} catch (IOException e) {
    e.printStackTrace();
}
```
![image](./changelog.png)

#### Markdown
```java
String render = new MarkdownRender().render(diff);
try {
    FileWriter fw = new FileWriter(
            "testDiff.md");
    fw.write(render);
    fw.close();
    
} catch (IOException e) {
    e.printStackTrace();
}
```
```markdown
### What's New
---
* `GET` /pet/{petId} Find pet by ID

### What's Deprecated
---
* `POST` /pet/{petId} Updates a pet in the store with form data

### What's Changed
---
* `PUT` /pet Update an existing pet  
    Parameter

        Add body.newFeild //a feild demo by sayi
        Add body.category.newCatFeild
        Delete body.category.name
* `POST` /pet Add a new pet to the store  
    Parameter

        Add tags //add new query param demo
        Add body.newFeild //a feild demo by sayi
        Add body.category.newCatFeild
        Delete body.category.name
* `DELETE` /pet/{petId} Deletes a pet  
    Parameter

        Add newHeaderParam
* `POST` /pet/{petId}/uploadImage uploads an image for pet  
    Parameter

        petId change into not required Notes ID of pet to update change into ID of pet to update, default false
* `POST` /user Create user  
    Parameter

        Add body.newUserFeild //a new user feild demo
        Delete body.phone
* `GET` /user/login Logs user into the system  
    Parameter

        Delete password //The password for login in clear text
* `GET` /user/{username} Get user by user name  
    Return Type

        Add newUserFeild //a new user feild demo
        Delete phone
* `PUT` /user/{username} Updated user  
    Parameter

        Add body.newUserFeild //a new user feild demo
        Delete body.phone

```

# License
swagger-diff is released under the Apache License 2.0.

# How it works
![image](./swagger-diff.png)

# Documents
[中文文档](https://github.com/Sayi/swagger-diff/wiki/%E4%B8%AD%E6%96%87%E6%96%87%E6%A1%A3)





