## Version 1.0.0 to 1.0.2
---
### What's New
---
* `GET` /pet/{petId} Find pet by ID

### What's Deprecated
---
* `POST` /pet/{petId} Updates a pet in the store with form data

### What's Changed
---
`POST` /pet Add a new pet to the store  
    Parameters

        Add tags //add new query param demo
        Insert body.newFeild //a feild demo by sayi
        Insert body.category.newCatFeild
        Insert body.owner.newUserFeild //a new user feild demo
        Delete body.category.name
        Delete body.owner.phone
        Modify body.name
`PUT` /pet Update an existing pet  
    Parameters

        Insert body.newFeild //a feild demo by sayi
        Insert body.category.newCatFeild
        Insert body.owner.newUserFeild //a new user feild demo
        Delete body.category.name
        Delete body.owner.phone
        Modify body.name
`DELETE` /pet/{petId} Deletes a pet  
    Parameters

        Add newHeaderParam
`POST` /pet/{petId}/uploadImage uploads an image for pet  
    Parameters

        Add petId //ID of pet to update, default false
        petId change into not required Notes ID of pet to update change into ID of pet to update, default false
`POST` /user Create user  
    Parameters

        Insert body.newUserFeild //a new user feild demo
        Insert body.favorite.newFeild //a feild demo by sayi
        Insert body.favorite.category.newCatFeild
        Delete body.phone
        Delete body.favorite.category.name
        Modify body.favorite.name
`GET` /user/login Logs user into the system  
    Parameters

        Delete password //The password for login in clear text
`PUT` /user/{username} Updated user  
    Parameters

        Insert body.newUserFeild //a new user feild demo
        Insert body.favorite.newFeild //a feild demo by sayi
        Insert body.favorite.category.newCatFeild
        Delete body.phone
        Delete body.favorite.category.name
        Modify body.favorite.name
`GET` /user/{username} Get user by user name  
    Return Type

        Insert newUserFeild //a new user feild demo
        Insert favorite.newFeild //a feild demo by sayi
        Insert favorite.category.newCatFeild
        Delete phone
        Delete favorite.category.name
        Modify favorite.name
