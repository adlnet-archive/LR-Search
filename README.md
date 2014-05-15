LR Search
========
[![Build Status](https://travis-ci.org/adlnet/LR-Search.png?branch=master)](https://travis-ci.org/adlnet/LR-Search)

This project is a companion to [LR-Data](https://github.com/adlnet/LR-Data) and acts as a set of search and data services for the ElasticSearch built by LR-Data.

# Search API

This is the primary API endpoint for searching the Learning Registry

It is accessible at ```/search```

It supports a number of query string params

| Param | Description                   | Required |
|:------|:-----------------------------|:---------:|
|terms  |phrase to search for, or a Common Core standards ID           |true|
|filter |generic filter for results     |false|
|page   |0 based index for paged results|false|
|contentType|filter specifically for content type|false|
|accessibility|filter for accessibility metadata as defined in LRMI|false|

The response format for the search results is as follows

```json
{

    "count": 1,
    "data": [
        {
            "hasScreenshot": true,
            "url": "http://www.example.com",
            "accessibilityFeature": [ ],
            "description": "Sampel Description for content",
            "_id": "1e4b2d8ec9849beffedef9ddc392f2b1",
            "publisher": "Sample Publisher",
            "title": "Sample Title"
        }
    ]
}
```

# Screenshot API

The ```/screenshot/{id}``` endpoint returns a screenshot of resource linked to the ID paraameter

|Status|cause|
|:----:|:---:|
|200|normal|
|404|screenshot not available|

404 errors can occur for a number of reason, the most common being that the image is not cached, and creating the screenshot times out while accessing the remote site


# Standards API

The standards API exposes date concerning Common Core standards

The ```/new/standards``` endpoint returns a list of all the Common Core standards the system knows about

```json
[
    "standard"
]
```

The ```/new/standards/{id}``` endpoint returns all the Common Core standards for the supplied ID

```json
{
    "title": "title",
    "description": "description",
    "id": "id",
    "children": [...]
}
```

Where the contents of ```children``` have the same structure as the parent node

# Data API

If you have the ID for a document in the system you can access it's metadata via the ```/data``` endpoint

You can either supply the ID as ```/data/{id}``` or you can get multiple documents via ```/data?keys={ids}``` where ids is a json encoded
list of IDs

If you request multiple documents the response will be formated as

```json
{

    "count": 1,
    "data": [
        {
            "hasScreenshot": true,
            "url": "http://www.example.com",
            "accessibilityFeature": [ ],
            "description": "Sampel Description for content",
            "_id": "1e4b2d8ec9849beffedef9ddc392f2b1",
            "publisher": "Sample Publisher",
            "title": "Sample Title"
        }
    ]
}
```

If you requests a single ID the response will be formated as

```json
{
    "hasScreenshot": true,
    "url": "http://www.example.com",
    "accessibilityFeature": [ ],
    "description": "Sampel Description for content",
    "_id": "1e4b2d8ec9849beffedef9ddc392f2b1",
    "publisher": "Sample Publisher",
    "title": "Sample Title"
}
```





