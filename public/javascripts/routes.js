angular
		.module('lr', [ "ngRoute", "lr.search", "lr.review" ])
		.config(
				[
						"$routeProvider",
						function($routeProvider) {
							$routeProvider
									.when(
											"/",
											{
												template : function() {
													var hashTag = location.href
															.indexOf("#");
													var rootUrl = location.href
															.substr(0, hashTag);
													return '<div class="container"><div class="jumbotron"><lrsearch endpoint="'
															+ rootUrl
															+ '"/></div></div><div class="container"><lrresults/></div> ';
												}
											})
									.when(
											"/review",
											{
												template : function() {
													var hashTag = location.href
															.indexOf("#");
													var rootUrl = location.href
															.substr(0, hashTag);
													return '<div class="container"><div class="jumbotron"><display endpoint="'
													+ rootUrl
													+ '"/></div></div><div class="container"><review endpoint="'
													+ rootUrl
													+ '"/></div> ';
												}
											})
									.when(
											"/review/:docId",
											{
												template : function() {
													var hashTag = location.href
															.indexOf("#");
													var rootUrl = location.href
															.substr(0, hashTag);
													return '<div class="container"><div class="jumbotron"><display endpoint="'
															+ rootUrl
															+ '"/></div></div><div class="container"><review endpoint="'
															+ rootUrl
															+ '"/></div> ';
												}
											});

						} ]);
