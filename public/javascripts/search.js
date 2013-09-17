angular
		.module('search', [])
		.directive(
				'lrsearch',
				function() {
					return {
						restrict : "E",
						transclude : true,
						scope : {
							searchUrl : "=searchUrl"
						},
						link : function($scope, $element, $attrs) {
							$scope.searchurl = $attrs.searchurl
						},
						controller : function($scope, $http) {
							$(window)
									.bind(
											"scroll",
											function() {
												var nVScroll = document.documentElement.scrollTop
														|| document.body.scrollTop;
												var scrollHeight = document.documentElement.scrollHeight
														|| document.body.scrollHeight;
												if (((nVScroll / scrollHeight) * 100) >= 66.0
														&& !$scope.updating) {
													$scope.page++;
													console.log($scope.page);
													updateResults();
												}
											});
							$scope.page = 0;
							$scope.updating = false;
							$scope.results = [];
							var updateResults = function() {
								$scope.updating = true;
								$http.get($scope.searchurl, {
									params : {
										terms : $scope.query,
										page : $scope.page
									}
								}).then(
										function(data) {
											angular.copy(data.data.data,
													$scope.results);
											$scope.updating = false;

										});
							}
							$scope.query = "";
							$scope.search = function() {
								$scope.results = [];
								$scope.page = 0;
								updateResults();
							}
						},
						template : '<div class="container">'
								+ '<div class="jumbotron">'
								+ '<form ng-submit="search()">'
								+ '<input placeholder="Search" id="q" ng-model="query""></input>'
								+ '<button id="search" class="btn btn-primary" type="submit">Search</button>'
								+ '</form>'
								+ '</div>'
								+ '</div>'
								+ '<div class="container">'
								+ '<dl ng-repeat="result in results">'
								+ '<dt><a href="{{result.url}}">{{result.title}}</a></dt>'
								+ '<dd>'
								+ '<p class="text-muted">{{result.url}}</p>'
								+ '<p>{{result.description}}</p>' + '</dd>'
								+ '</dl>' + '</div>'
					}
				}).directive('lrresults', function() {

		});