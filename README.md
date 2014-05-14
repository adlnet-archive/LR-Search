LR Search
========
[![Build Status](https://travis-ci.org/adlnet/LR-Search.png?branch=master)](https://travis-ci.org/adlnet/LR-Search)

# Search API

This is the primary API endpoint for searching the Learning Registry

It is accessible at ```/search```

It supports a number of query string params

| Param | Description                   | Required |
|:------|:-----------------------------|:---------:|
|terms  |phrase to search for           |true|
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
