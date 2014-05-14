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
